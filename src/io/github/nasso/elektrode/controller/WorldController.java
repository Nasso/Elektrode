package io.github.nasso.elektrode.controller;

import io.github.nasso.elektrode.model.Node;
import io.github.nasso.elektrode.model.World;

public class WorldController extends AbstractWorldController {
	public WorldController(World world) {
		super(world);
	}

	public void placeNode(Node n, int x, int y) {
		
	}

	public void removeNode(Node n) {
		
	}
	
	public void removeNode(int x, int y){
		Node n = getWorld().getNodeAt(x, y);
		
		if(n != null){
			removeNode(n);
		}
	}
}
