package io.github.nasso.elektrode.model;

import io.github.nasso.elektrode.view.Viewport;

import java.util.ArrayList;
import java.util.List;

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

	public Inventory getInventory() {
		return inventory;
	}

	public void setInventory(Inventory inventory) {
		this.inventory = inventory;
	}
}
