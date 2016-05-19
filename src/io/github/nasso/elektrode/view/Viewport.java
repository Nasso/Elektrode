package io.github.nasso.elektrode.view;

public class Viewport {
	private double scale = 1;
	private double translateX = 0;
	private double translateY = 0;
	
	public Viewport(){
		this(1);
	}
	
	public Viewport(double scale){
		setScale(scale);
	}
	
	public void translateX(double v){
		this.translateX += v;
	}
	
	public void translateY(double v){
		this.translateY += v;
	}
	
	public void translate(double x, double y){
		this.translateX += x;
		this.translateY += y;
	}
	
	public double getScale() {
		return scale;
	}
	
	public void setScale(double scale) {
		this.scale = scale;
	}
	
	public double getTranslateX() {
		return translateX;
	}
	
	public void setTranslateX(double translateX) {
		this.translateX = translateX;
	}
	
	public double getTranslateY() {
		return translateY;
	}
	
	public void setTranslateY(double translateY) {
		this.translateY = translateY;
	}
}
