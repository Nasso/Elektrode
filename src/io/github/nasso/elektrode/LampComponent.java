package io.github.nasso.elektrode;

public class LampComponent extends Node {
	private boolean activated = false;
	
	public LampComponent(){
		this.addInput().addStateListener(new BooleanListener() {
			public void valueChanged(boolean newValue) {
				activated = newValue;
			}
		});
	}

	public boolean isActivated() {
		return activated;
	}
}
