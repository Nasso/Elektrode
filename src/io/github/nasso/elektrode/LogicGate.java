package io.github.nasso.elektrode;

public abstract class LogicGate extends Node implements BooleanListener {
	private boolean inverted = false;
	private int inputCount = 0;
	
	public LogicGate(int inputCount){
		this.inputCount = inputCount;
		
		for(int i = 0; i < inputCount; i++){
			this.addInput().addStateListener(this);
		}
		
		this.addOutput();
		
		update();
	}
	
	public void onAction(){
		inverted = !inverted;
		
		update();
	}
	
	public boolean isInverted(){
		return inverted;
	}
	
	public void setInverted(boolean v){
		this.inverted = v;
		
		update();
	}
	
	public void valueChanged(boolean newValue) {
		update();
	}
	
	private void update(){
		boolean[] inputs = new boolean[inputCount];
		
		for(int i = 0; i < inputCount; i++){
			inputs[i] = getInputValue(i);
		}
		
		boolean result = logicGate(inputs);
		
		if(inverted){
			result = !result;
		}
		
		this.setOutputValue(0, result);
	}
	
	public abstract boolean logicGate(boolean[] inputs);
}
