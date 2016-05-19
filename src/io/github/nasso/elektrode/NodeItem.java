package io.github.nasso.elektrode;

public class NodeItem implements InventoryItem {
	private Class<? extends Node> source;
	
	private String displayName = "missingno";
	
	public NodeItem(Class<? extends Node> sourceClass){
		this(sourceClass, sourceClass.getSimpleName());
	}
	
	public NodeItem(Class<? extends Node> sourceClass, String displayName){
		this.source = sourceClass;
		this.displayName = displayName;
	}
	
	public Node createNodeFromSource(){
		if(source == null){
			return null;
		}
		
		try {
			return source.newInstance();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		
		return null; // Return null if sad
	}

	public Class<? extends Node> getSource() {
		return source;
	}

	public void setSource(Class<? extends Node> source) {
		this.source = source;
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
		return displayName;
	}
}
