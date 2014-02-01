package net.kolls.railworld.play.script;

import net.kolls.railworld.Train;

/**
 * A train-action-listener condition includes
 * a train action listener, a train, and an event name.
 * The listener is notified when the given event
 * occurs for the given train.  The train may be
 * null which means the listener will be notified
 * for the given event for any train.
 * 
 * @author Steve Kollmansberger
 *
 */
public class TalCondition {
	/**
	 * Listener to notify
	 */
	public TrainActionListener tal;
	/**
	 * Train to watch for, or null for any train.
	 */
	public Train t;
	/**
	 * Event name to watch for.  The event names are given in {@link TrainActionListener#trainAction(Train, String)}.
	 */
	public String event;
	/**
	 * Create a condition
	 * 
	 * @param tal See {@link #tal}
	 * @param t See {@link #t}
	 * @param event See {@link #event}
	 */
	public TalCondition(TrainActionListener tal, Train t, String event) {
		this.tal = tal;
		this.t = t;
		this.event = event;
	}
	
	@Override
	public boolean equals(Object o) {
		try {
			TalCondition op = (TalCondition)o;
			return (op.tal == tal && op.t == t && op.event.equals(event));
		} catch (Exception ex) { return false; }
		
		
	}
}