package io.github.nasso.elektrode.model;

public class AndLogicGate extends LogicGate {
	public AndLogicGate(){
		super(2);
	}
	
	public boolean logicGate(boolean[] in) {
		return in[0] && in[1];
	}
}
