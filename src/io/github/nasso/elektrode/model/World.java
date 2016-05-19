package io.github.nasso.elektrode.model;

import java.util.ArrayList;
import java.util.List;

public class World {
	private List<Node> nodes = new ArrayList<Node>();

	public List<Node> getNodes() {
		return nodes;
	}

	public void setNodes(List<Node> nodes) {
		this.nodes = nodes;
	}
}
