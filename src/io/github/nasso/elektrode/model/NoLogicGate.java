package io.github.nasso.elektrode.model;

public class NoLogicGate extends LogicGate {
	public NoLogicGate(){
		super(1);
		
		// To make a no logic gate, invert the yes
		this.setInverted(true);
	}

	public boolean logicGate(boolean[] in) {
		// Yes logic, but inverted -> no logic gate
		return in[0];
	}
}
