package io.github.nasso.elektrode;

public class NoLogicGate extends LogicGate {
	public NoLogicGate(){
		super(1);
	}

	public boolean logicGate(boolean[] in) {
		return !in[0];
	}
}
