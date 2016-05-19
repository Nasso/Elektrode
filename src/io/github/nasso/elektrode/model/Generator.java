package io.github.nasso.elektrode.model;

public class Generator extends Node {
	public Generator(){
		this.addOutput();
		this.setOutputValue(0, true);
	}
	
	public void onAction(){
		this.setOutputValue(0, !this.getOutputValue(0));
	}
}
