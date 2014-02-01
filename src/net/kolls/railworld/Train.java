
package net.kolls.railworld;

/*
 * Copyright (C) 2010 Steve Kollmansberger
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */



import net.kolls.railworld.tc.*;

/**
 * Represents a train (sequence of cars), including its position and controller.
 * 
 * @author Steve Kollmansberger
 *
 */
public class Train  {
	private Car[] carray;
	private double velocity;
	private int throttle;
	private boolean brake;
	private TrainControl trc;
	
	/**
	 * The maximum throttle setting for the train (not related to maximum speed)
	 */
	public static final int MAX_THROTTLE = 8;

	
	
	/**
	 * 
	 * @return <code>true</code> if the train has one or more engines.
	 */
	public boolean hasEngine() {

		for (int i = 0; i < carray.length; i++)
			if (carray[i].isEngine()) return true;
		return false;
		
	}
	
	/**
	 * The weight of the train is the sum of the weights of the cars.
	 * 
	 * @return Weight of the trains, in US Tons
	 */
	public int weight() {
		
		int w = 0;
		for (int i = 0; i < carray.length; i++)
			w += carray[i].weight();
		
		return w;
		
	}
	
	/**
	 * Create a train from a given array of cars.  A new {@link MixControl}ler is assigned by default.
	 * 
	 * 
	 * @param cars Array of {@link Car}s.
	 */
	public Train(Car[] cars) {
		
		velocity = 0;
		throttle = 0;
		brake = followMe = reverse = followMeOnce = false;
		carray = cars;
		pos = new CLoc();
		TrainControl s = new UserControl();
		TrainControl ds = new AutoControl();
		//s.attach(this);
		//ds.attach(this);
		setController(new MixControl(s, ds));
	}
	
	/**
	 * Returns controller for this train. 
	 * 
	 * @return The train's {@link TrainControl}.
	 */
	public TrainControl getController() {
		
		return trc;
	}
	
	/**
	 * Updates a train's controller.
	 * 
	 * @param controller The new controller to use.
	 */
	public void setController(TrainControl controller) {
		trc = controller;
		controller.attach(this);
	}
	
	/**
	 * The length is the length of all cars, with the distance between cars also added in.
	 * 
	 * @return A {@link Distance} measuring the length of the train.
	 */
	public Distance length() {
		double l = 0;
		for (int i = 0; i < carray.length; i++) {
			l += carray[i].length().feet();
			l += Car.DIST_BETWEEN_CARS.feet(); // feet in between cars
		}
		
		return new Distance(l, Distance.Measure.FEET);
	}
	
	/**
	 * 
	 * @return Velocity, in MPH.
	 */
	public double vel() {
		return velocity;
	}
	
	/**
	 * Directly and immediately alter the train's velocity.  This should be used only sparingly.
	 * In general, you should alter the throttle and brake and let the train adjust its speed
	 * more naturally.
	 * 
	 * @param v New velocity, MPH.
	 */
	public void setVel(double v) {
		velocity = v;
	}
	
	/**
	 * 
	 * @return Current throttle setting.
	 */
	public int getThrottle() {
		return throttle;
	}
		

	/**
	 * 
	 * @return <code>true</code> if the brake is active.
	 */
	public boolean getBrake() { return brake; }
	
	/**
	 * Update the throttle.  Valid range is 0 (idle) to {@link #MAX_THROTTLE}
	 * @param t Throttle setting
	 */
	public void setThrottle(int t) { if (t >= 0 && t <= MAX_THROTTLE) throttle = t;}
	
	/**
	 * Set or release the brake.
	 * @param b 
	 */
	public void setBrake(boolean b) {
		System.out.println("Set brake " + b);
		
		if (b && brake == false && vel() > 0)
			Sounds.brake.play();
		brake = b; 
	}
	
	/**
	 * 
	 * @return Array of {@link Car}s that compose this train.
	 */
	public Car[] array() { return carray; }
	
	private double getAccel(int currThrottle, boolean currBrake) {
		double portionSec = GameLoop.CLOCK_WAIT / 1000.0;
		
		
		
		double accel = 0;
		
		double tmaxv = currThrottle * (Train.MAX_SPEED_MPH / MAX_THROTTLE);
		
		// throttle acceleration
		if (velocity <= tmaxv) accel += currThrottle / 2.0;

		// count number of engines
		int e = 0;
		for (int i = 0; i < carray.length; i++) 
			if (carray[i].isEngine()) e++;
		if (accel > 0) accel *= e;
		
		if (e == 0) setThrottle(0); // if no engines, throttle must be zero!

		// brake decelartion
		if (currBrake) accel = -2 + Math.sqrt(currThrottle);
		
		// adjust for weight of train
		accel /= weight() / 1000.0;

		// apply friction constant regardless of mass
		accel -= 0.1;

		

		accel *= portionSec;
		
		return accel;
	}
	
	/**
	 * Calculates the trains acceleration and velocity based on the controls. 
	 * This should be called once every clock step.  The amount of adjustment
	 * is proportional to the {@link GameLoop#CLOCK_WAIT}.
	 *
	 */
	public void adjust() {
		// called every clock cycle
		// determine that length
		
		double accel = getAccel(throttle, brake);
		
		
		
		// this can be activated at high throttles when the brake is pushed
		// thus abruptly lowering the speed
		// because the accel is positive; still outweights the brakes
		// so check if its close
		double tmaxv = throttle * (Train.MAX_SPEED_MPH / MAX_THROTTLE);
		
		if (accel > 0 && velocity >= tmaxv
				&& velocity - tmaxv <= 2.0)  {
			
			velocity = throttle * (Train.MAX_SPEED_MPH / MAX_THROTTLE); 
			return; 
		}
		
		
		velocity += accel;
		if (velocity > Train.MAX_SPEED_MPH) { 
			velocity = Train.MAX_SPEED_MPH; 
			return; 
		}
		if (velocity < 0) {velocity = 0; return; }


	}

	/**
	 * Calculate, if the throttle were set to 0 and the brake applied, how many feet it would take
	 * for the train to slow to the requested velocity.
	 * 
	 * @param vel Target velocity, in MPH.  May be 0 for stopped.
	 * @return Number of feet that would be travered before reaching target velocity.
	 */
	public double feetToSlow(double vel) {
		double feet = 0;
		double iv = vel();
		
		double accel = getAccel(0, true);



		while (iv > vel) {

			feet += GameLoop.feetPerStepSpeed(iv);



			iv += accel; // accel is negative
			if (iv < 0) iv = 0;
			

		}
		return feet;
	}

	// housekeeping for railcanvas
	
	/**
	 * Trains position.
	 */
	public CLoc pos;
	
	/**
	 * Should the display track this train.  Only one train at a time should have this be true.
	 */
	public boolean followMe;
	
	/**
	 * Does this train request to be reversed?  Controller must ensure train is stopped first.
	 */
	public boolean reverse;
	
	/**
	 * Does this train request to be split?  Controller must ensure train is stopped
	 * and car other than the first selected.
	 */
	public boolean split;
	
	/**
	 * The display should jump to this train's location, but not continue to follow it.
	 */
	public boolean followMeOnce;
	
	/**
	 * Maximum speed (mph) for the train.
	 */
	public static final int MAX_SPEED_MPH = 40;
	
	

}
