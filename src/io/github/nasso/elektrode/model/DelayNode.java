package io.github.nasso.elektrode.model;

import io.github.nasso.elektrode.view.LongDialog;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

public class DelayNode extends Node {
	private static final AtomicInteger delayID = new AtomicInteger(0);
	public static final String DELAY_PROP_NAME = "delay";
	
	private Timer timer;
	
	private LongDialog dial;
	
	public DelayNode(){
		this.setDelay(1000);
		
		dial = new LongDialog();
		timer = new Timer("DelayNode Timer-"+delayID.getAndIncrement(), true);
		
		addOutput();
		
		addInput().addStateListener(new BooleanListener() {
			public void valueChanged(boolean newValue) {
				timer.schedule(new TimerTask() {
					public void run() {
						setOutputValue(0, newValue);
					}
				}, getDelay());
			}
		});
	}

	public void onAction(){
		this.setDelay(dial.show());
	}
	
	public long getDelay() {
		return (long) getProperty(DELAY_PROP_NAME);
	}
	
	/**
	 * Sets the delay in milliseconds
	 * @param delay
	 */
	public void setDelay(long delay) {
		this.setProperty(DELAY_PROP_NAME, delay);
	}
}
