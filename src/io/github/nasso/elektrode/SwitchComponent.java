package io.github.nasso.elektrode;

public class SwitchComponent extends Node {
	private boolean state = false;
	
	public SwitchComponent(){
		addInput().addStateListener(new BooleanListener() {
			public void valueChanged(boolean newValue) {
				update();
			}
		});
		
		addOutput();
	}
	
	private void update(){
		if(state){
			this.setOutputValue(0, this.getInputValue(0));
		}else{
			this.setOutputValue(0, false);
		}
	}
	
	public void onAction(){
		toggle();
	}
	
	public boolean getState(){
		return this.state;
	}
	
	public void toggle(){
		this.state = !state;
		
		update();
	}
}
