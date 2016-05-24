package io.github.nasso.elektrode.model;

import io.github.nasso.elektrode.view.Renderable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.geometry.Point2D;
import javafx.scene.transform.Affine;

public abstract class Node implements Renderable, Cloneable {
	private List<Input> inputs = new ArrayList<Input>();
	private List<Output> outputs = new ArrayList<Output>();
	
	private double x = 0, y = 0, width = 1, height = 1;
	private int orientation = 0;
	
	private Map<String, Object> properties = new HashMap<String, Object>();
	
	// Constr
	public Node(){
		
	}
	
	public Node(Map<String, Object> properties){
		this.setProperties(properties);
	}
	
	// Actions
	public abstract void onAction();
	
	// Utilities
 	public Point2D getInputPos(int in){
		double percent = (in + 0.5) / (double) this.getInputs().length;
		
		// Relative to the center
		double x = -this.getWidth()/2;
		double y = this.getHeight()/2 - this.getHeight() * percent;

		double ang = -90 * this.getOrientation();
		
		Affine rotation = new Affine();
		rotation.appendRotation(ang);
		
		return rotation.transform(x, y);
	}
	
 	public Point2D getOutputPos(int out){
		double percent = (out + 0.5) / (double) this.getOutputs().length;
		
		// Relative to the center
		double x = this.getWidth()/2;
		double y = this.getHeight()/2 - this.getHeight() * percent;

		double ang = -90 * this.getOrientation();
		
		Affine rotation = new Affine();
		rotation.appendRotation(ang);

		// - 0.05 on x just for swag, no calc errors, i promise (to myself)
		return rotation.transform(x - 0.05, y);
	}
	
	public void connectTo(Node n, int out, int in){
		this.getOutput(out).addDestination(n.getInput(in));
	}
	
	public void disconnectFrom(Node n, int out, int in){
		this.getOutput(out).removeDestination(n.getInput(in));
	}
	
	public void changeInputCount(int targetInputs, BooleanListener l){
		int effectiveInputs = this.getInputs().length;
		
		if(effectiveInputs < targetInputs){ // There are not enough inputs
			for(int i = 0, count = targetInputs - effectiveInputs; i < count; i++){
				this.addInput().addStateListener(l);
			}
		}else if(effectiveInputs > targetInputs){ // There are too much inputs
			for(int i = 0, count = effectiveInputs - targetInputs; i < count; i++){
				this.removeInput(effectiveInputs-i-1); // Removes the last input
			}
		}
	}
	
	public void changeInputCount(int targetInputs){
		changeInputCount(targetInputs, null);
	}
	
	public void changeOutputCount(int targetOutputs){
		int effectiveOutputs = this.getOutputs().length;
		
		if(effectiveOutputs < targetOutputs){ // There are not enough outputs
			for(int i = 0, count = targetOutputs - effectiveOutputs; i < count; i++){
				this.addOutput();
			}
		}else if(effectiveOutputs > targetOutputs){ // There are too much outputs
			for(int i = 0, count = effectiveOutputs - targetOutputs; i < count; i++){
				this.removeOutput(effectiveOutputs-i-1); // Removes the last output
			}
		}
	}
	
	public void setProperty(String name, Object o){
		properties.put(name, o);
	}
	
	public Object getProperty(String name){
		return properties.get(name);
	}
	
	public boolean hasProperty(String name){
		return this.properties.containsKey(name);
	}
	
	public Map<String, Object> getProperties(){
		return properties;
	}
	
	public void setProperties(Map<String, Object> properties){
		this.properties.putAll(properties);
	}
	
	public void clearProperties(){
		this.properties.clear();
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
	
	public String toString(){
		return "["+this.getClass().getSimpleName()+"]";
	}
}
