package io.github.nasso.elektrode.controller;


public abstract class Command {
	public abstract void execute();
	public abstract void undo();
}
