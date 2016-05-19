package io.github.nasso.elektrode.model;

import io.github.nasso.elektrode.view.Renderable;

import java.util.ArrayList;
import java.util.List;

public abstract class Node implements Renderable, Cloneable {
	private List<Input> inputs = new ArrayList<Input>();
	private List<Output> outputs = new ArrayList<Output>();
	
	private double x = 0, y = 0, width = 1, height = 1;
	private int orientation = 0;
	
	// Actions
	public abstract void onAction();
	
	// Utilities
	public void connectTo(Node n, int out, int in){
		this.getOutput(out).addDestination(n.getInput(in));
	}
	
	public void disconnectFrom(Node n, int out, int in){
		this.getOutput(out).removeDestination(n.getInput(in));
	}
	
	// Outputs
	public Output[] getOutputs() {
		return outputs.toArray(new Output[outputs.size()]);
	}
	
	public Output getOutput(int index){
		return outputs.get(index);
	}
	
	public Output addOutput(){
		Output out = new Output(this, this.outputs.size());
		
		this.outputs.add(out);
		
		return out;
	}
	
	public Output removeOutput(int index){
		return this.outputs.remove(index);
	}
	
	public void clearOutputs(){
		for(Output out : outputs){
			out.setOn(false);
			out.clearDestinations();
		}
		
		this.outputs.clear();
	}
	
	public void setOutputValue(int i, boolean value){
		if(!hasOutput(i)){
			return;
		}
		
		this.getOutput(i).setOn(value);
	}
	
	public boolean hasInput(int i){
		return i < inputs.size() && inputs.get(i) != null;
	}
	
	public boolean hasOutput(int i){
		return i < outputs.size() && outputs.get(i) != null;
	}
	
	// Inputs
	public Input[] getInputs() {
		return inputs.toArray(new Input[inputs.size()]);
	}
	
	public Input getInput(int index){
		return this.inputs.get(index);
	}
	
	public Input addInput(){
		Input in = new Input(this, this.inputs.size());
		
		this.inputs.add(in);
		
		return in;
	}
	
	public Input removeInput(int index){
		return this.inputs.remove(index);
	}
	
	public void clearInputs(){
		for(Input in : inputs){
			Output origin = in.getOrigin();
			
			if(origin != null){
				origin.removeDestination(in);
			}
		}
		
		this.inputs.clear();
	}

	public boolean getInputValue(int i){
		if(!hasInput(i)){
			return false;
		}
		
		return this.inputs.get(i).isOn();
	}
	
	public boolean getOutputValue(int i){
		if(!hasOutput(i)){
			return false;
		}
		
		return this.outputs.get(i).isOn();
	}
	
	public double getX() {
		return x;
	}

	public void setX(float x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(float y) {
		this.y = y;
	}

	public double getWidth() {
		return width;
	}

	public void setWidth(float width) {
		this.width = width;
	}

	public double getHeight() {
		return height;
	}

	public void setHeight(float height) {
		this.height = height;
	}
	
	public int getOrientation() {
		return orientation;
	}
	
	public void setOrientation(int o){
		this.orientation = o;
		
		if(orientation >= 4){
			orientation = orientation % 4; // rest of index/size so just the offset B)
		}else if(orientation < 0){
			orientation = 4 + (orientation % 4); // index/size will be negative, just add to the total-1
		}
	}
	
	public void turnRight(){
		this.turn(1);
	}
	
	public void turnLeft(){
		this.turn(-1);
	}
	
	public void turn(int offset){
		this.orientation += offset;

		if(orientation >= 4){
			orientation = orientation % 4; // rest of index/size so just the offset B)
		}else if(orientation < 0){
			orientation = 4 + (orientation % 4); // index/size will be negative, just add to the total-1
		}
	}

	public int getOutputIndex(Output output) {
		return outputs.indexOf(output);
	}
}
