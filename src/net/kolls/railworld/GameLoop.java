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




import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import net.kolls.railworld.opening.Options;


/**
 * Represents a time stepping update loop.  The loop performs given behavior as often as possible
 * whilst still being consistent.  To that end, the system maintains a count of the time required
 * to perform the operation and narrows its wait to be close to that time.
 * The wait time is automatically adjusted as the program progresses; thus, it is essential
 * that the behavior pays attention to the {@link #CLOCK_WAIT} value to determine "how much"
 * of behavior or movement it should do at each step.
 * 
 * Note that if the user provides a clock wait through options, this will override
 * the adjusting algorithm and be fixed.
 * 
 * @author Steve Kollmansberger
 *
 */
public abstract class GameLoop {
	private RailCanvas jdb;


	private boolean adjust;
	
	private boolean running;
	
	/**
	 * Set to <code>true</code> to suspend behavior but retain painting. 
	 * Note that the loop continues to run and update the display, so display
	 * events (scrolling, etc) are still possible.
	 */
	public boolean paused;
	
	
	/**
	 * Milliseconds of playtime that have elapsed.
	 */
	public long elapsed;
    
	/**
	 * Time between each run of the behavior, in milliseconds.  Applications
	 * should sample this every time the behavior is called, and they may reasonably
	 * assume that in this amount of time the behavior will be called again.
	 */
    public static int CLOCK_WAIT = 200;


    
    // given a MPH
	// how many pixels should we advance
	// in ONE CLOCK STEP
    /**
     * Given a velocity in MPH, how many feet should we move per clock step?
     * This is a convenience method.
     * 
     * @param vel The velocity in MPH, not to exceed {@link Train#MAX_SPEED_MPH}.
     * @return A <code>double</code> indicating how many feet to move this clock step.
     */
	public static double feetPerStepSpeed(double vel) {
		double rv = vel / Train.MAX_SPEED_MPH;
		// what percentage of max speed?

		// convert mph into feet per second
		double fps = (int)(Train.MAX_SPEED_MPH * 1.4666);
		
		rv *= fps; // how many feet per second

		rv *= (CLOCK_WAIT / 1000.0); // scale by clock step

		return rv;
	}
	
	/**
	 * Constructs a gameloop.
	 * 
	 * @param rc A {@link RailCanvas} this loop should paint.
	 */
	public GameLoop(RailCanvas rc) {
		jdb = rc;
		running = true;
		paused = false;
		adjust = true;
		todos = new LinkedBlockingQueue<Runnable>();
		elapsed = 0;
		
		if (Options.getFPS() != 0) {
			CLOCK_WAIT = (int)((1.0 / Options.getFPS()) * 1000);
			adjust = false;
		}
		
	}
	
	/**
	 * Calculate the current target frames per second
	 * 
	 * @return The current target FPS
	 */
	public int fps() {
		return (int)(1.0 / (CLOCK_WAIT / 1000.0));
	}
	
	/**
	 * Begin the loop.  This method should be called in a separate thread, as it will run until
	 * the canvas is finished, either by user command or by error.  At that point,
	 * the method will return and the canvas and its helpers may be safely disposed.
	 *
	 */
	public void gameLoop() {
		long t = System.currentTimeMillis();
		long delta = CLOCK_WAIT, delta2 = CLOCK_WAIT, delta3 = CLOCK_WAIT, delta4 = CLOCK_WAIT;
		long avgd;
		long stepstoadjust = 20; // right out of the gate, let the system get stabilized
		
		while(running) {
			delta4 = delta3;
			delta3 = delta2;
			delta2 = delta;
			delta = CLOCK_WAIT - (System.currentTimeMillis() - t);
			if (delta < 0) 
				System.out.println(delta);
			
			avgd = (delta+delta2+delta3+delta4) / 4;
			
			if (stepstoadjust == 0 && adjust) {
				if (avgd < 0) {
					// slow timer down
					CLOCK_WAIT *= 1.1;
					System.out.println("Clock speed ("+CLOCK_WAIT+") slowed to " + fps() + " frames per second.");
				} 
				if (avgd > (CLOCK_WAIT * 0.3) && CLOCK_WAIT > 40) {
					// speed time up
					CLOCK_WAIT -= (avgd * 0.2);
					System.out.println("Clock speed ("+CLOCK_WAIT+") increased to " + fps() + " frames per second.");
				}
				stepstoadjust = 5;
			} else stepstoadjust--;
			
			
			try {
				Thread.sleep(avgd);
			} catch (Exception e) {  }
			
			long ot = t;
			t = System.currentTimeMillis();
			if (!paused)
				elapsed += (t - ot);
			
			step();
			
		}

	}
	
	/**
	 * Stop the game loop.  This method returns immediately, but the gameloop method will complete
	 * its current iteration before returning.
	 *
	 */
	public void stop() {
		running = false;
		
	}
	
	private BlockingQueue<Runnable> todos;
	
	/**
	 * Run some commands inside the game loop.  Given command will be run
	 * at the beginning of the next game loop.  This ensures appropriate
	 * synchronization with train activity.
	 * 
	 * @param r The commands to run.
	 */
	public void runInLoop(Runnable r) {
		synchronized (todos) {
			try {
				todos.put(r);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void step() {


		synchronized (todos) {
			
			Runnable r = todos.poll();
			
			while (r != null) {
				r.run();
				r = todos.poll();
			}
				
		}
		
		
		if (!paused) prePaint();
		
		
		
		jdb.drawCanvas();
		
		
		if (!paused) run();
		
	
	}
	
	/**
	 * Behavior to perform before painting occurs.
	 *
	 */
	protected abstract void prePaint();
	
	/**
	 * Behavior to perform after painting occurs.
	 *
	 */
	protected abstract void run();
	
}
