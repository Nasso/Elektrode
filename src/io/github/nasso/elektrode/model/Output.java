package io.github.nasso.elektrode.model;

import java.util.ArrayList;
import java.util.List;

public class Output {
	private boolean on = false;
	private Node owner = null;
	private List<Input> destinations = new ArrayList<Input>();
	
	public Output(Node owner, int ownerIndex){
		this(owner, ownerIndex, false);
	}
	
	public Output(Node owner, int ownerIndex, boolean on){
		this.on = on;
		this.owner = owner;
	}
	
	public Input[] getDestinations() {
		return destinations.toArray(new Input[destinations.size()]);
	}
	
	public Input getDestination(int index){
		return destinations.get(index);
	}
	
	public Input addDestination(Input in){
		if(!this.destinations.contains(in)){
			this.destinations.add(in);
		}
		
		Output origin = in.getOrigin();
		
		if(origin != null){
			origin.destinations.remove(in);
		}
		
		in.setOn(this.isOn());
		in.setOrigin(this);
		
		return in;
	}
	
	public Input removeDestination(int index){
		return this.removeDestination(this.destinations.get(index));
	}
	
	public Input removeDestination(Input in){
		this.destinations.remove(in);
		
		in.setOrigin(null);
		in.setOn(false);
		
		return in;
	}
	
	public void clearDestinations(){
		for(Input in : destinations){
			in.setOrigin(null);
		}
		
		this.destinations.clear();
	}
	
	public boolean isOn() {
		return on;
	}
	
	public void setOn(boolean on) {
		if(on != this.on){
			this.on = on;
			
			refreshDestinations();
		}
	}
	
	public void refreshDestinations(){
		for(Input in : destinations){
			in.setOn(on);
			in.setOrigin(this);
		}
	}

	public Node getOwner() {
		return owner;
	}
	
	public int getOwnerIndex(){
		return this.owner.getOutputIndex(this);
	}
}
