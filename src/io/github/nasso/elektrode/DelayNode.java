package io.github.nasso.elektrode;

import java.util.Timer;
import java.util.TimerTask;

public class DelayNode extends Node {
	private long delay = 1000;
	private Timer timer;
	
	private LongDialog dial;
	
	public DelayNode(){
		dial = new LongDialog();
		timer = new Timer(false);
		
		addOutput();
		
		addInput().addStateListener(new BooleanListener() {
			public void valueChanged(boolean newValue) {
				timer.schedule(new TimerTask() {
					public void run() {
						setOutputValue(0, newValue);
					}
				}, delay);
			}
		});
	}

	public void onAction(){
		this.setDelay(dial.show());
	}
	
	public long getDelay() {
		return delay;
	}
	
	/**
	 * Sets the delay in milliseconds
	 * @param delay
	 */
	public void setDelay(long delay) {
		this.delay = delay;
	}
}
