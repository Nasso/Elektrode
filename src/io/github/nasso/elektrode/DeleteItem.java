package io.github.nasso.elektrode;

public class DeleteItem implements InventoryItem {
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
		return "Delete item";
	}
}
