package net.kolls.railworld.scripts.missions.support;

import java.util.PriorityQueue;


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


/**
 * A support class which allows the programmer to
 * add items to happen at a given elapsed time.
 * The class wraps a priority queue and returns items
 * in order they should occur, but never before the desired time.
 * 
 * Note this this class is not synchronized or thread safe.
 * 
 * 
 * @param <T> Any type which is to be scheduled
 * @author Steve Kollmansberger 
 */
public class TimeQueue<T> {

	/**
	 * Contains a value and an elapsed time that that value
	 * should be returned.
	 * 
	 * @author Steve Kollmansberger
	 *
	 * @param <T> Any type which is to be scheduled
	 */
	public class TimeValue<T> implements Comparable<TimeValue<T>> {
		/**
		 * The elapsed time at which to execute this value.
		 */
		public long elapsed;
		
		/**
		 * The value to return/execute.
		 */
		public T value;
		
		/**
		 * Convenience constructor.
		 * @param elapsed
		 * @param value
		 */
		public TimeValue(long elapsed, T value) {
			this.elapsed = elapsed;
			this.value = value;
		}
		@Override
		public int compareTo(TimeValue<T> o) {
			return new Long(elapsed).compareTo(new Long(o.elapsed));
		}
		
	}
	
	private PriorityQueue<TimeValue<T>> _queue;
	
	/**
	 * Construct a new, empty queue.
	 */
	public TimeQueue() {
		_queue = new PriorityQueue<TimeValue<T>>();
	}
	
	/**
	 * Retrieves and returns the next action to occur, if any.
	 * The action is removed from the queue.
	 * If the queue is empty, or the next action is not scheduled
	 * to occur yet, the method returns null.
	 * 
	 * @param elapsed The current elapsed time.
	 * @return The next action whose time is prior to elapsed, or null.
	 */
	public TimeValue<T> poll(long elapsed) {
		TimeValue<T> val = _queue.peek();
		if (val == null)
			return null;
		
		if (val.elapsed <= elapsed) {
			return _queue.poll();
		}
		
		return null;
	}
	
	/**
	 * Enqueue an item.
	 * 
	 * @param item
	 */
	public void add(TimeValue<T> item) {
		_queue.add(item);
	}
	
	/**
	 * Enqueue an item to occur at a specified time.
	 * 
	 * @param elapsed The time it should occur.
	 * @param value The item value to occur
	 * 
	 */
	public void add(long elapsed, T value) {
		_queue.add(new TimeValue<T>(elapsed, value));
	}
	
}
