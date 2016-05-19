package io.github.nasso.elektrode;

import io.github.nasso.elektrode.model.ActionItem;
import io.github.nasso.elektrode.model.AndLogicGate;
import io.github.nasso.elektrode.model.DelayNode;
import io.github.nasso.elektrode.model.DeleteItem;
import io.github.nasso.elektrode.model.Generator;
import io.github.nasso.elektrode.model.Input;
import io.github.nasso.elektrode.model.Inventory;
import io.github.nasso.elektrode.model.InventoryItem;
import io.github.nasso.elektrode.model.LampComponent;
import io.github.nasso.elektrode.model.NoLogicGate;
import io.github.nasso.elektrode.model.Node;
import io.github.nasso.elektrode.model.NodeItem;
import io.github.nasso.elektrode.model.OrLogicGate;
import io.github.nasso.elektrode.model.Output;
import io.github.nasso.elektrode.model.SwitchComponent;
import io.github.nasso.elektrode.model.WireItem;
import io.github.nasso.elektrode.model.World;
import io.github.nasso.elektrode.view.ClassicRenderer;
import io.github.nasso.elektrode.view.Renderer;
import io.github.nasso.elektrode.view.Viewport;
import javafx.animation.Animation;
import javafx.animation.AnimationTimer;
import javafx.animation.Transition;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.transform.Affine;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Elektrode extends Application {
	/*
	 * 
	 * CTRL + SHIFT + DIVIDE
	 * 		collapse
	 * 
	 * CTRL + SHIFT + MULTIPLY
	 * 		uncollapse
	 * 
	 */
	
	// FPS counter
	private int fps = 0;
	private int frameNum = 0;
	private long lastFPSUpdate = 0;
	private long lastFrameTime = -1;
	
	private AnimationTimer timer;
	
	private Canvas cvs = null;
	private MenuBar menuBar = null;
	
	private Viewport viewport = new Viewport(50);
	private double lastX = -1;
	private double lastY = -1;
	
	private Renderer renderer = new ClassicRenderer();
	private World world = new World();
	
	private Inventory inventory = new Inventory();
	private Output originWireOut = null;
	private Point2D mousePos = new Point2D(0, 0);
	
 	private Scene createScene(Stage stg){
		Group g = new Group();
		menuBar = new MenuBar();
		
		g.getChildren().add(cvs = new Canvas());
		// TODO: g.getChildren().add(menuBar = new MenuBar());
		
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
						
						int cx = (int) Math.floor(sceneToWorldX(event.getSceneX()) + 0.5);
						int cy = (int) Math.ceil(sceneToWorldY(sce.getHeight() - event.getSceneY()) - 0.5);
						
						for(Node n : world.getNodes()){
							if(n.getX() == cx && n.getY() == cy){ // if there is already a node
								// Save in/outs
								for(int i = 0, count = Math.min(clone.getInputs().length, n.getInputs().length);
									i < count; i++){
									Output origin = n.getInput(i).getOrigin();
									
									if(origin != null){
										origin.addDestination(clone.getInput(i));
									}
								}
								
								for(int i = 0, count = Math.min(clone.getOutputs().length, n.getOutputs().length);
										i < count; i++){
									for(int j = 0; j < n.getOutput(i).getDestinations().length; j++){
										clone.connectTo(n.getOutput(i).getDestination(j).getOwner(), i, j);
									}
								}
								
								// Replace it but with style
								n.clearInputs();
								n.clearOutputs();
								world.getNodes().remove(n);
								break;
							}
						}
						
						clone.setX(cx);
						clone.setY(cy);
						
						world.getNodes().add(clone);
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
						double sy = sce.getHeight() - event.getSceneY();
						
						Node n = getNodeAt(sx, sy);
						
						if(n != null){
							n.onAction();
						}
					}else if(item instanceof DeleteItem){
						double sx = event.getSceneX();
						double sy = sce.getHeight() - event.getSceneY();
						
						Node n = getNodeAt(sx, sy);
						
						if(n != null){
							n.clearInputs();
							n.clearOutputs();
							world.getNodes().remove(n);
						}
					}
				}else if(event.getButton() == MouseButton.MIDDLE){
					// Uncomment if other actions needed, but I think not
					// InventoryItem item = inventory.getSelectedItem();
					
					// Always turn
					double sx = event.getSceneX();
					double sy = sce.getHeight() - event.getSceneY();
					
					Node n = getNodeAt(sx, sy);
					
					if(n != null){
						n.turnRight();
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
		
		// Cvs
		cvs.widthProperty().bind(sce.widthProperty());
		cvs.heightProperty().bind(sce.heightProperty());
		
		// Menubar
		Menu fileMenu = new Menu("File");
		
		MenuItem saveItem = new MenuItem("Save");
		MenuItem saveAsItem = new MenuItem("Save as");

		MenuItem loadItem = new MenuItem("Load");
		
		fileMenu.getItems().addAll(saveItem, saveAsItem, loadItem);
		
		menuBar.getMenus().addAll(fileMenu);
		
		double offOpacity = 0.5;
		double transTime = 100;
		final Animation fadeIn = new Transition() {
			{
				setCycleDuration(Duration.millis(transTime));
			}
			
			protected void interpolate(double frac) {
				menuBar.setOpacity(offOpacity + (1 - offOpacity) * frac);
			}
		};
		
		final Animation fadeOut = new Transition() {
			{
				setCycleDuration(Duration.millis(transTime));
			}
			
			protected void interpolate(double frac) {
				menuBar.setOpacity(1 - (1 - offOpacity) * frac);
			}
		};
		
		menuBar.prefWidthProperty().bind(sce.widthProperty());
		menuBar.setOnMouseEntered(new EventHandler<MouseEvent>(){
			public void handle(MouseEvent event) {
				fadeOut.stop();
				
				double t = (menuBar.getOpacity() - offOpacity) / (1 - offOpacity) * transTime;
				
				fadeIn.playFrom(Duration.millis(t));
			}
		});
		menuBar.setOnMouseExited(new EventHandler<MouseEvent>(){
			public void handle(MouseEvent event) {
				fadeIn.stop();
				
				double t = (menuBar.getOpacity() - offOpacity) / (1 - offOpacity) * transTime;
				
				fadeOut.playFrom(Duration.millis(transTime - t));
			}
		});
		menuBar.setOpacity(offOpacity);
		
		return sce;
	}
 	
 	private Point2D getInputPos(Node n, int in){
		double percent = (in + 0.5) / (double) n.getInputs().length;
		
		// Relative to the center
		double x = -n.getWidth()/2;
		double y = n.getHeight()/2 - n.getHeight() * percent;

		double ang = -90 * n.getOrientation();
		
		Affine rotation = new Affine();
		rotation.appendRotation(ang);
		
		return rotation.transform(x, y);
	}
	
	private Point2D getOutputPos(Node n, int out){
		double percent = (out + 0.5) / (double) n.getOutputs().length;
		
		// Relative to the center
		double x = n.getWidth()/2;
		double y = n.getHeight()/2 - n.getHeight() * percent;

		double ang = -90 * n.getOrientation();
		
		Affine rotation = new Affine();
		rotation.appendRotation(ang);

		// - 0.05 on x just for swag, no calc errors, i promise (to myself)
		return rotation.transform(x - 0.05, y);
	}
	
 	private Input getInputAt(double sceneX, double sceneY){
 		double inRadius = 0.2;
 		
 		for(Node n : world.getNodes()){
 			Input[] ins = n.getInputs();
 			for(int i = 0; i < ins.length; i++){
 				Point2D op = getInputPos(n, i).add(new Point2D(n.getX(), n.getY()));
 				Point2D sp = new Point2D(sceneToWorldX(sceneX), sceneToWorldY(cvs.getHeight() - sceneY));
 				
 				double distance = op.distance(sp);
 				
 				if(distance <= inRadius){ // "is in" trick with radius omg i'm a f*cking genius xd lol
 					return ins[i];
 				}
 			}
 		}
	 	
 		return null;
 	}
 	
 	private Output getOutputAt(double sceneX, double sceneY){
 		double outRadius = 0.3;
 		
 		for(Node n : world.getNodes()){
 			Output[] outs = n.getOutputs();
 			for(int i = 0; i < outs.length; i++){
 				Point2D op = getOutputPos(n, i).add(new Point2D(n.getX(), n.getY()));
 				Point2D sp = new Point2D(sceneToWorldX(sceneX), sceneToWorldY(cvs.getHeight() - sceneY));
 				
 				double distance = op.distance(sp);
 				
 				if(distance <= outRadius){ // "is in" trick with radius omg i'm a f*cking genius xd lol
 					return outs[i];
 				}
 			}
 		}
	 	
 		return null;
 	}
	
 	private Node getNodeAt(double sceneX, double sceneY){
 		int cx = (int) Math.floor(sceneToWorldX(sceneX) + 0.5);
		int cy = (int) Math.ceil(sceneToWorldY(sceneY) - 0.5);
		
		for(Node n : world.getNodes()){
			if(n.getX() == cx && n.getY() == cy){ // if there is already a node
				return n;
			}
		}
	 	
 		return null;
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
		double wx = (x - cvs.getWidth()/2) / viewport.getScale() - viewport.getTranslateX();
		
		return wx;
	}
	
	private double sceneToWorldY(double y){
		double wy = (y - cvs.getHeight()/2) / viewport.getScale() - viewport.getTranslateY();
		
		return wy;
	}
	
	private void initElogic(){
		// TODO: Inventory
		inventory.addItem(new WireItem());
		inventory.addItem(new ActionItem());
		inventory.addItem(new DeleteItem());
		
		inventory.addItem(new NodeItem(Generator.class, 		"Generator"));
		inventory.addItem(new NodeItem(NoLogicGate.class, 		"No logic gate"));
		inventory.addItem(new NodeItem(AndLogicGate.class, 		"And logic gate"));
		inventory.addItem(new NodeItem(OrLogicGate.class, 		"Or logic gate"));
		inventory.addItem(new NodeItem(LampComponent.class, 	"Lamp"));
		inventory.addItem(new NodeItem(SwitchComponent.class, 	"Switch"));
		inventory.addItem(new NodeItem(DelayNode.class, 		"Delay node"));
	}
	
	private void loopUpdate(double delta, long nowms){
		renderer.render(cvs, viewport, inventory, originWireOut, mousePos, world, delta, nowms);
	}
	
	public static void main(String[] args) {
		Elektrode.launch(args);
	}
}
