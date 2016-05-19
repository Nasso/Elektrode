package io.github.nasso.elektrode.model;

import java.util.ArrayList;
import java.util.List;

public class Input {
	private boolean on = false;
	
	private Output origin = null;
	
	private Node owner = null;
	private int ownerIndex = 0;
	
	private List<BooleanListener> stateListeners = new ArrayList<BooleanListener>();

	public Input(Node owner, int ownerIndex){
		this.owner = owner;
		this.ownerIndex = ownerIndex;
	}
	
	public boolean isOn() {
		// refresh because cool
		if(origin != null){
			on = origin.isOn();
		}
		
		return on;
	}

	public void setOn(boolean on) {
		if(this.on != on){
			this.on = on;
			
			for(BooleanListener l : stateListeners){
				l.valueChanged(this.on);
			}
		}
	}
	
	public BooleanListener addStateListener(BooleanListener l){
		this.stateListeners.add(l);
		
		return l;
	}
	
	public BooleanListener removeStateListener(BooleanListener l){
		this.stateListeners.remove(l);
		
		return l;
	}
	
	public int getOwnerIndex(){
		return this.ownerIndex;
	}
	
	public Node getOwner(){
		return owner;
	}

	public Output getOrigin() {
		return origin;
	}

	public void setOrigin(Output origin) {
		this.origin = origin;
	}
}
