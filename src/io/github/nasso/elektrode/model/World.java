package io.github.nasso.elektrode.model;

import io.github.nasso.elektrode.view.Viewport;

import java.util.ArrayList;
import java.util.List;

import javafx.geometry.Point2D;

public class World {
	private List<Node> nodes = new ArrayList<Node>();
	private Viewport viewport = new Viewport(50);
	private Inventory inventory = new Inventory(); 

	public List<Node> getNodes() {
		return nodes;
	}

	public void setNodes(List<Node> nodes) {
		this.nodes = nodes;
	}

	public Viewport getViewport() {
		return viewport;
	}
	
	public void setViewport(Viewport viewport) {
		this.viewport = viewport;
	}
	
	public double toWorldX(double x){
		return viewport.untransformX(x);
	}
	
	public double toWorldY(double y){
		return viewport.untransformY(y);
	}
	
	public int getCaseX(double x){
		return (int) Math.floor(toWorldX(x) + 0.5);
	}
	
	public int getCaseY(double y){
		return (int) Math.ceil(toWorldY(y) - 0.5);
	}
	
	public Input getInputAt(double sceneX, double sceneY){
 		double inRadius = 0.2;
 		
 		for(Node n : this.getNodes()){
 			Input[] ins = n.getInputs();
 			for(int i = 0; i < ins.length; i++){
 				Point2D op = n.getInputPos(i).add(new Point2D(n.getX(), n.getY()));
 				Point2D sp = new Point2D(this.toWorldX(sceneX), this.toWorldY(sceneY));
 				
 				double distance = op.distance(sp);
 				
 				if(distance <= inRadius){ // "is in" trick with radius omg i'm a f*cking genius xd lol
 					return ins[i];
 				}
 			}
 		}
	 	
 		return null;
 	}
	
 	public Output getOutputAt(double sceneX, double sceneY){
 		double outRadius = 0.3;
 		
 		for(Node n : this.getNodes()){
 			Output[] outs = n.getOutputs();
 			for(int i = 0; i < outs.length; i++){
 				Point2D op = n.getOutputPos(i).add(new Point2D(n.getX(), n.getY()));
 				Point2D sp = new Point2D(this.toWorldX(sceneX), this.toWorldY(sceneY));
 				
 				double distance = op.distance(sp);
 				
 				if(distance <= outRadius){ // "is in" trick with radius omg i'm a f*cking genius xd lol
 					return outs[i];
 				}
 			}
 		}
	 	
 		return null;
 	}
	
 	public Node getNodeOn(double sceneX, double sceneY){
 		int cx = this.getCaseX(sceneX);
		int cy = this.getCaseY(sceneY);
		
 		return getNodeAt(cx, cy);
 	}
 	
 	public Node getNodeAt(int caseX, int caseY){
 		for(Node n : this.getNodes()){
			if(n.getX() == caseX && n.getY() == caseY){ // if there is a node
				return n;
			}
		}
	 	
 		return null;
 	}
	
	public Inventory getInventory() {
		return inventory;
	}

	public void setInventory(Inventory inventory) {
		this.inventory = inventory;
	}
}
