package io.github.nasso.elektrode.controller;


public class CommandManager {
	private CommandStack comStack = new CommandStack();
	
	public CommandManager(){
		this(30);
	}
	
	public CommandManager(int commandStackSize){
		comStack.setMaxStackSize(commandStackSize);
	}
	
	public void execute(Command c){
		comStack.push(c);
	}
	
	public void undo(){
		comStack.pop().undo();
	}
}
