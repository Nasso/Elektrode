package io.github.nasso.elektrode.model;

public class NoLogicGate extends LogicGate {
	public NoLogicGate(){
		super(1);
		
		setInverted(true);
	}

	public boolean logicGate(boolean[] in) {
		return in[0];
	}
}
