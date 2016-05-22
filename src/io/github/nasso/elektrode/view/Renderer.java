package io.github.nasso.elektrode.view;

import io.github.nasso.elektrode.model.Output;
import io.github.nasso.elektrode.model.World;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;

public interface Renderer {
	public void render(
			Canvas cvs,
			Output originWireOutput,
			Point2D mousePos,
			World world,
			double delta,
			long nowms);
}
