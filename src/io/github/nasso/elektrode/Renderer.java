package io.github.nasso.elektrode;

import java.util.List;

import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;

public interface Renderer {
	public void render(
			Canvas cvs,
			Viewport v,
			Inventory inventory,
			Output originWireOutput,
			Point2D mousePos,
			List<Node> nodes,
			double delta,
			long nowms);
}
