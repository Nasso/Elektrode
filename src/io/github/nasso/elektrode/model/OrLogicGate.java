package io.github.nasso.elektrode.model;

public class OrLogicGate extends LogicGate {
	public OrLogicGate() {
		super(2);
	}

	public boolean logicGate(boolean[] in) {
		return in[0] || in[1];
	}
}
