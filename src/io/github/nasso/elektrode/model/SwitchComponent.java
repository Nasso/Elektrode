package io.github.nasso.elektrode.model;

public class SwitchComponent extends Node {
	public static final String STATE_PROP_NAME = "state";
	
	public SwitchComponent(){
		setState(false);
		
		addInput().addStateListener(new BooleanListener() {
			public void valueChanged(boolean newValue) {
				update();
			}
		});
		
		addOutput();
	}
	
	private void update(){
		if(getState()){
			this.setOutputValue(0, this.getInputValue(0));
		}else{
			this.setOutputValue(0, false);
		}
	}
	
	public void onAction(){
		toggle();
	}
	
	public boolean getState(){
		return (boolean) this.getProperty(STATE_PROP_NAME);
	}
	
	public void setState(boolean s){
		setProperty(STATE_PROP_NAME, s);
	}
	
	public void toggle(){
		this.setState(!getState());
		
		update();
	}
}
