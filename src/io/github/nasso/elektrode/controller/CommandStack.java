package io.github.nasso.elektrode.controller;

import java.util.ArrayList;
import java.util.List;

public class CommandStack {
	private int maxSize = 30;
	
	private List<Command> commandStack = new ArrayList<Command>();
	
	public CommandStack(){
		this(30);
	}
	
	public CommandStack(int size){
		setMaxStackSize(size);
	}
	
	public void setMaxStackSize(int v){
		maxSize = v;
	}
	
	public int getMaxStackSize(){
		return maxSize;
	}
	
	public void push(Command c){
		commandStack.add(c);
		
		// :D
		while(commandStack.size() > maxSize){
			commandStack.remove(0);
		}
	}
	
	public Command pop(){
		return commandStack.get(commandStack.size()-1);
	}
}
