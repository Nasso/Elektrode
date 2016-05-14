package io.github.nasso.elektrode;

import java.util.ArrayList;
import java.util.List;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.stage.Stage;

public class Elektrode extends Application {
	// FPS counter
	private int fps = 0;
	private int frameNum = 0;
	private long lastFPSUpdate = 0;
	private long lastFrameTime = -1;
	
	private AnimationTimer timer;
	
	private Canvas cvs = null;
	
	private Viewport viewport = new Viewport(50);
	private double lastX = -1;
	private double lastY = -1;
	
	private Renderer renderer = new ClassicRenderer();
	private List<Node> nodes = new ArrayList<Node>();
	
	private Inventory inventory = new Inventory();
	private Output originWireOut = null;
	private Point2D mousePos = new Point2D(0, 0);
	
 	private Scene createScene(Stage stg){
		Group g = new Group();
		g.getChildren().add(cvs = new Canvas());
		
		Scene sce = new Scene(g);
		
		sce.setOnScroll(new EventHandler<ScrollEvent>() {
			public void handle(ScrollEvent event) {
				if(event.isControlDown()){
					double scale = viewport.getScale();
					
					scale += event.getDeltaY() / 10;
					
					scale = Math.min(scale, 200);
					scale = Math.max(10, scale);
					
					viewport.setScale(scale);
				}else{
					inventory.moveSelectedSlot((int) -Math.signum(event.getDeltaY()));
				}
			}
		});
		
		sce.setOnMousePressed(new EventHandler<MouseEvent>(){
			public void handle(MouseEvent event) {
				if(event.getButton() == MouseButton.PRIMARY){
					lastX = event.getSceneX();
					lastY = event.getSceneY();
				}else if(event.getButton() == MouseButton.SECONDARY){
					InventoryItem item = inventory.getSelectedItem();
					
					if(item instanceof NodeItem){
						Node clone = ((NodeItem) item).createNodeFromSource();
						
						int cx = (int) Math.floor(sceneToWorldX(event.getSceneX()));
						int cy = (int) Math.ceil(sceneToWorldY(sce.getHeight() - event.getSceneY()));
						
						for(Node n : nodes){
							if(n.getX() == cx && n.getY() == cy){ // if there is already a node
								nodes.remove(n);
								break;
							}
						}
						
						clone.setX(cx);
						clone.setY(cy);
						
						nodes.add(clone);
					}else if(item instanceof WireItem){
						double sx = event.getSceneX();
						double sy = event.getSceneY();
						
						Output out = getOutputAt(sx, sy); // returns null if unfound
						
						if(out != null){ // if found
							originWireOut = out; // take a wire from there
						}else{
							Input in = getInputAt(sx, sy); // returns null if unfound
							
							if(in != null){ // if found
								if(originWireOut != null && in.getOwner() != originWireOut.getOwner()){ // relies the wire if there is one
									originWireOut.addDestination(in);
									
									originWireOut = null;
								}else if(in.getOrigin() != null){ // take the wire
									originWireOut = in.getOrigin();
									originWireOut.removeDestination(in); // remove the old
								}
							}
						}
					}else if(item instanceof ActionItem){
						double sx = event.getSceneX();
						double sy = event.getSceneY();
						
						Node n = getNodeAt(sx, sy);
						
						if(n != null){
							n.onAction();
						}
					}
				}else if(event.getButton() == MouseButton.MIDDLE){
					InventoryItem item = inventory.getSelectedItem();
					
					if(item instanceof ActionItem){
						double sx = event.getSceneX();
						double sy = event.getSceneY();
						
						Node n = getNodeAt(sx, sy);
						
						if(n != null){
							n.turnRight();
						}
					}
				}
			}
		});
		
		sce.setOnMouseDragged(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent event) {
				if(event.getButton() == MouseButton.PRIMARY){
					viewport.translate((event.getSceneX() - lastX) / viewport.getScale(), -(event.getSceneY() - lastY) / viewport.getScale());
					
					lastX = event.getSceneX();
					lastY = event.getSceneY();
				}
			}
		});
		
		sce.setOnMouseMoved(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent event) {
				mousePos = new Point2D(event.getSceneX(), sce.getHeight() - event.getSceneY());
			}
		});
		
		sce.setOnKeyPressed(new EventHandler<KeyEvent>() {
			public void handle(KeyEvent event) {
				if(event.getCode() == KeyCode.ESCAPE){
					// release the wire !!
					
					originWireOut = null;
				}
			}
		});
		
		cvs.widthProperty().bind(sce.widthProperty());
		cvs.heightProperty().bind(sce.heightProperty());
		
		return sce;
	}
 	
 	private Input getInputAt(double sceneX, double sceneY){
 		Input found = null;
 		
 		GraphicsContext gtx = cvs.getGraphicsContext2D(); // Just to ez
 		
 		double inRadius = 0.2;
 		
 		gtx.save();			
			gtx.translate(cvs.getWidth()/2, cvs.getHeight()/2);
			gtx.scale(1, -1);
			
			gtx.scale(viewport.getScale(), viewport.getScale());
			gtx.translate(viewport.getTranslateX(), viewport.getTranslateY());
			
	 		for(Node n : nodes){
	 			double ix = n.getX() - n.getWidth()/2 - inRadius;
	 			
	 			Input[] ins = n.getInputs();
	 			for(int i = 0; i < ins.length; i++){
	 				gtx.save();
	 					double iy = n.getY() + n.getHeight()/2 - n.getHeight() * (i+0.5f) / ins.length - inRadius;
	 					
	 					gtx.beginPath();
		 					gtx.rect(
		 						ix,
		 						iy + inRadius/2,
		 						inRadius,
		 						inRadius
		 					);
		 					
		 					if(gtx.isPointInPath(sceneX, sceneY)){
		 						found = ins[i];
		 					}
	 					gtx.closePath();
	 				gtx.restore();
	 				
	 				if(found != null){
	 					break;
	 				}
	 			}
	 			
	 			if(found != null){
	 				break;
	 			}
	 		}
	 	gtx.restore();
	 	
 		return found;
 	}
 	
 	private Output getOutputAt(double sceneX, double sceneY){
 		Output found = null;
 		
 		GraphicsContext gtx = cvs.getGraphicsContext2D(); // Just to ez
 		
 		double outRadius = 0.2;
 		
 		gtx.save();			
			gtx.translate(cvs.getWidth()/2, cvs.getHeight()/2);
			gtx.scale(1, -1);
			
			gtx.scale(viewport.getScale(), viewport.getScale());
			gtx.translate(viewport.getTranslateX(), viewport.getTranslateY());
			
	 		for(Node n : nodes){
	 			double ix = n.getX() + n.getWidth()/2 - outRadius;
	 			
	 			Output[] outs = n.getOutputs();
	 			for(int i = 0; i < outs.length; i++){
	 				gtx.save();
	 					double iy = n.getY() + n.getHeight()/2 - n.getHeight() * ((float) i+0.5f / outs.length) - outRadius;
	 					
	 					gtx.beginPath();
		 					gtx.rect(
		 						ix,
		 						iy + outRadius/2,
		 						outRadius*2,
		 						outRadius
		 					);
		 					
		 					if(gtx.isPointInPath(sceneX, sceneY)){
		 						found = outs[i];
		 					}
	 					gtx.closePath();
	 				gtx.restore();
	 				
	 				if(found != null){
	 					break;
	 				}
	 			}
	 			
	 			if(found != null){
	 				break;
	 			}
	 		}
	 	gtx.restore();
	 	
 		return found;
 	}
	
 	private Node getNodeAt(double sceneX, double sceneY){
 		Node found = null;
 		
 		GraphicsContext gtx = cvs.getGraphicsContext2D(); // Just to ez
 		
 		gtx.save();			
			gtx.translate(cvs.getWidth()/2, cvs.getHeight()/2);
			gtx.scale(1, -1);
			
			gtx.scale(viewport.getScale(), viewport.getScale());
			gtx.translate(viewport.getTranslateX(), viewport.getTranslateY());
			
	 		for(Node n : nodes){
	 			gtx.beginPath();
	 				gtx.rect(n.getX() - n.getWidth()/2, n.getY() - n.getHeight()/2, n.getWidth(), n.getHeight());
	 				
	 				if(gtx.isPointInPath(sceneX, sceneY)){
	 					found = n;
		 				break;
	 				}
	 			gtx.closePath();
	 		}
	 	gtx.restore();
	 	
 		return found;
 	}
 	
	public void start(Stage stg) throws Exception {
		stg.setMaximized(true);
		stg.setTitle("Elektrode");
		stg.setScene(createScene(stg));
		stg.show();
		
		timer = new AnimationTimer(){
			public void handle(long now) {
				long nowms = now / 1000000l;
				
				if(lastFrameTime < 0){
					lastFrameTime = nowms;
				}
				
				double delta = nowms - lastFrameTime;
				
				frameNum++;
				if(nowms - lastFPSUpdate > 1000l){
					Elektrode.this.fps = frameNum;
					stg.setTitle("Elogic - "+Elektrode.this.fps+"FPS");
					frameNum = 0;
					lastFPSUpdate = nowms;
				}
				
				loopUpdate(delta, nowms);
				
				lastFrameTime = nowms;
			}
		};
		
		timer.start();
		
		initElogic();
	}
	
	private double sceneToWorldX(double x){
		double wx = (x - cvs.getWidth()/2) / viewport.getScale() + 0.5 - viewport.getTranslateX();
		
		return wx;
	}
	
	private double sceneToWorldY(double y){
		double wy = (y - cvs.getHeight()/2) / viewport.getScale() - 0.5 - viewport.getTranslateY();
		
		return wy;
	}
	
	private void initElogic(){
		// Inventory
		inventory.addItem(new WireItem());
		inventory.addItem(new ActionItem());
		inventory.addItem(new NodeItem(Generator.class));
		inventory.addItem(new NodeItem(NoLogicGate.class));
		inventory.addItem(new NodeItem(AndLogicGate.class));
		inventory.addItem(new NodeItem(OrLogicGate.class));
		inventory.addItem(new NodeItem(LampComponent.class));
		inventory.addItem(new NodeItem(SwitchComponent.class));
	}
	
	private void loopUpdate(double delta, long nowms){
		renderer.render(cvs, viewport, inventory, originWireOut, mousePos, nodes, delta, nowms);
	}
	
	public static void main(String[] args) {
		Elektrode.launch(args);
	}
}
