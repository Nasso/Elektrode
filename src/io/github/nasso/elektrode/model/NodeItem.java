package io.github.nasso.elektrode.model;

import java.util.Optional;


public class NodeItem extends InventoryItem {
	public NodeItem(){
		
	}
	
	public NodeItem(Class<? extends Node> sourceClass){
		this(sourceClass, sourceClass.getSimpleName());
	}
	
	public NodeItem(Class<? extends Node> sourceClass, String displayName){
		this.setSource(sourceClass);
		this.setDisplayName(displayName);
	}
	
	public Node createNodeFromSource(){
		Optional<Class<? extends Node>> oc = getSource();
		
		if(!oc.isPresent()){
			return null;
		}
		
		try {
			if(oc.isPresent()){
				return oc.get().newInstance();
			}
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		
		return null; // Return null if sad
	}

	@SuppressWarnings("unchecked")
	public Optional<Class<? extends Node>> getSource() {
		Optional<Class<? extends Node>> oc = Optional.empty();
		
		try {
			// oc = new Optional<Class<? extends Node>>((Class<? extends Node>) Class.forName((String) getProperty("sourceClass")));
			oc = Optional.of((Class<? extends Node>) Class.forName((String) getProperty("sourceClass")));
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		return oc;
	}

	public void setSource(Class<? extends Node> source) {
		setProperty("sourceClass", source.getCanonicalName());
	}
	
	// Unused method for inventory item
	public double getX() {
		return 0;
	}
	
	public double getY() {
		return 0;
	}
	
	public double getWidth() {
		return 1;
	}
	
	public double getHeight() {
		return 1;
	}
	
	public int getOrientation() {
		return 0;
	}
	
	public String getDisplayName() {
		return (String) getProperty("displayName");
	}
	
	public void setDisplayName(String name){
		setProperty("displayName", name);
	}
}
