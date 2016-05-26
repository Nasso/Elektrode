package io.github.nasso.elektrode.model;

public class LampComponent extends Node {
	public static final String ACTIVATED_PROP_NAME = "activated";
	
	public LampComponent(){
		setActivated(false);
		
		this.addInput().addStateListener(new BooleanListener() {
			public void valueChanged(boolean newValue) {
				setActivated(newValue);
			}
		});
	}

	public boolean isActivated() {
		return (boolean) getProperty(ACTIVATED_PROP_NAME);
	}
	
	private void setActivated(boolean v){
		setProperty(ACTIVATED_PROP_NAME, v);
	}
	
	public void onAction() {
		// Nothing
	}
}
