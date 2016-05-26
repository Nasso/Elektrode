package io.github.nasso.elektrode.controller;

import java.util.Objects;

import io.github.nasso.elektrode.model.Node;
import io.github.nasso.elektrode.model.World;

public abstract class AbstractWorldController {
	private CommandManager comManager = new CommandManager();
	private World world;
	
	public AbstractWorldController(World world){
		this.world = world;
	}
	
	public abstract void placeNode(Node n, int x, int y);
	public abstract void removeNode(Node n);
	public abstract void removeNode(int x, int y);
	
	// Accessors
	public CommandManager getCommandManager() {
		return comManager;
	}

	public void setCommandManager(CommandManager manager) {
		this.comManager = manager;
	}
	
	public World getWorld() {
		return world;
	}
	
	public void setWorld(World world) {
		Objects.requireNonNull(world);
		
		this.world = world;
	}
}
