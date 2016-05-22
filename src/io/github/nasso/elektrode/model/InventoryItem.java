package io.github.nasso.elektrode.model;

import java.util.HashMap;
import java.util.Map;

import io.github.nasso.elektrode.view.Renderable;

public abstract class InventoryItem implements Renderable {
	private Map<String, Object> properties = new HashMap<String, Object>();
	
	public InventoryItem(){
		
	}
	
	public InventoryItem(Map<String, Object> properties){
		this.setProperties(properties);
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
	
	public abstract String getDisplayName();
}
