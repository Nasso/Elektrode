package io.github.nasso.elektrode.model;

public abstract class LogicGate extends Node implements BooleanListener {
	public static final String INPUT_COUNT_PROP_NAME = "inputCount", INVERTED_PROP_NAME = "inverted";

	public LogicGate(int inputCount){
		this.setInputCount(inputCount);
		this.setInverted(false);
		
		this.addOutput();
		
		update();
	}
	
	public LogicGate(){
		this(2);
	}
	
	public void onAction(){
		toggleInversion();
	}
	
	public boolean isInverted(){
		return (boolean) this.getProperty(INVERTED_PROP_NAME);
	}
	
	public void setInverted(boolean v){
		this.setProperty(INVERTED_PROP_NAME, v);
		
		update();
	}
	
	public void toggleInversion(){
		setInverted(!isInverted());
		
		update();
	}
	
	public void valueChanged(boolean newValue) {
		update();
	}
	
	public int getInputCount(){
		return (int) getProperty(INPUT_COUNT_PROP_NAME);
	}
	
	public void setInputCount(int count){
		setProperty(INPUT_COUNT_PROP_NAME, count);
		
		rearrangeInputs();
	}
	
	private void rearrangeInputs(){
		this.changeInputCount(getInputCount(), this);
	}
	
	private void update(){
		rearrangeInputs(); // rearrange if needed
		
		boolean[] inputs = new boolean[getInputCount()];
		
		for(int i = 0; i < getInputCount(); i++){
			inputs[i] = getInputValue(i);
		}
		
		boolean result = logicGate(inputs);
		
		if(isInverted()){
			result = !result;
		}
		
		this.setOutputValue(0, result);
	}
	
	public abstract boolean logicGate(boolean[] inputs);
}
