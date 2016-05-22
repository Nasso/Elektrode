package io.github.nasso.elektrode;

import io.github.nasso.elektrode.data.WorldCodec;
import io.github.nasso.elektrode.data.XMLWorldCodec;
import io.github.nasso.elektrode.model.ActionItem;
import io.github.nasso.elektrode.model.AndLogicGate;
import io.github.nasso.elektrode.model.DelayNode;
import io.github.nasso.elektrode.model.DeleteItem;
import io.github.nasso.elektrode.model.Generator;
import io.github.nasso.elektrode.model.Input;
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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Optional;

import javafx.animation.Animation;
import javafx.animation.AnimationTimer;
import javafx.animation.Transition;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.transform.Affine;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
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
	private Stage stg = null;
	
	private Canvas cvs = null;
	private MenuBar menuBar = null;
	private String newShortcut = "Ctrl+N";
	private String saveShortcut = "Ctrl+S";
	private String saveAsShortcut = "Ctrl+Shift+S";
	private String loadShortcut = "Ctrl+O";
	private boolean isSaved = true;
	private Alert confirmAlert = null;
	
	private double lastX = -1;
	private double lastY = -1;
	
	private Renderer renderer = new ClassicRenderer();
	private World world = new World();
	private WorldCodec codec = new XMLWorldCodec();
	private Path openedPath = null;
	private FileChooser fileChooser = null;
	
	private Output originWireOut = null;
	private Point2D mousePos = new Point2D(0, 0);
	
	private Scene createScene(Stage stg){
		confirmAlert = new Alert(AlertType.CONFIRMATION, "", ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
		
		fileChooser = new FileChooser();
		fileChooser.setTitle("Choose a file");
		fileChooser.getExtensionFilters().add(new ExtensionFilter("Elektrode World File", "*.ewf"));
		
		Group g = new Group();
		g.getChildren().add(cvs = new Canvas());
		g.getChildren().add(menuBar = new MenuBar());
		
		Scene sce = new Scene(g);
		
		sce.setOnScroll(new EventHandler<ScrollEvent>() {
			public void handle(ScrollEvent event) {
				if(event.isControlDown()){
					double scale = world.getViewport().getScale();
					
					scale += event.getDeltaY() / 10;
					
					scale = Math.min(scale, 200);
					scale = Math.max(10, scale);
					
					world.getViewport().setScale(scale);
				}else{
					world.getInventory().moveSelectedSlot((int) -Math.signum(event.getDeltaY()));
				}
			}
		});
		
		sce.setOnMousePressed(new EventHandler<MouseEvent>(){
			public void handle(MouseEvent event) {
			 	if(event.getButton() == MouseButton.PRIMARY){
					lastX = event.getSceneX();
					lastY = event.getSceneY();
				}else if(event.getButton() == MouseButton.SECONDARY){
					InventoryItem item = world.getInventory().getSelectedItem();
					
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
								if(in.getOrigin() != null){ // take the wire
									originWireOut = in.getOrigin();
									originWireOut.removeDestination(in); // remove the old
								}else if(originWireOut != null){ // relies the wire if there is one
									if(in.getOwner() != originWireOut.getOwner()){
										originWireOut.addDestination(in);
										
										originWireOut = null;
									}
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
					world.getViewport().translate(
							(event.getSceneX() - lastX) / world.getViewport().getScale(),
							-(event.getSceneY() - lastY) / world.getViewport().getScale());
					
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
		MenuItem newItem = new MenuItem("New");
		MenuItem saveItem = new MenuItem("Save");
		MenuItem saveAsItem = new MenuItem("Save as");
		MenuItem loadItem = new MenuItem("Open");
		fileMenu.getItems().addAll(newItem, saveItem, saveAsItem, loadItem);
		menuBar.getMenus().addAll(fileMenu);
		
		double offOpacity = 1; // Useless to 1 but if you want you can change it, transition are ready :D
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

				if(offOpacity == 1){
					// Returns because it'll play from NaN (divide by zero, look carfully)
					return;
				}
				
				double t = (menuBar.getOpacity() - offOpacity) / (1 - offOpacity) * transTime;
				
				fadeIn.playFrom(Duration.millis(t));
			}
		});
		menuBar.setOnMouseExited(new EventHandler<MouseEvent>(){
			public void handle(MouseEvent event) {
				fadeIn.stop();
				
				if(offOpacity == 1){
					// Returns because it'll play from NaN (divide by zero, look carfully)
					return;
				}
				
				double t = (menuBar.getOpacity() - offOpacity) / (1 - offOpacity) * transTime;
				
				fadeOut.playFrom(Duration.millis(transTime - t));
			}
		});
		menuBar.setOpacity(offOpacity);
		
		// MenuBar style !!
		menuBar.setBackground(
			new Background(
				new BackgroundFill(
					new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
						new Stop(0, Color.grayRgb(255)),
						new Stop(1, Color.grayRgb(230))
					),
					CornerRadii.EMPTY,
					Insets.EMPTY
				)
			)
		);
		
		newItem.setAccelerator(KeyCombination.keyCombination(newShortcut));
		saveItem.setAccelerator(KeyCombination.keyCombination(saveShortcut));
		saveAsItem.setAccelerator(KeyCombination.keyCombination(saveAsShortcut));
		loadItem.setAccelerator(KeyCombination.keyCombination(loadShortcut));
		
		newItem.setOnAction(new EventHandler<ActionEvent>(){
			public void handle(ActionEvent event) {
				newWorld();
			}
		});
		saveItem.setOnAction(new EventHandler<ActionEvent>(){
			public void handle(ActionEvent event) {
				save();
			}
		});
		saveAsItem.setOnAction(new EventHandler<ActionEvent>(){
			public void handle(ActionEvent event) {
				saveAs();
			}
		});
		loadItem.setOnAction(new EventHandler<ActionEvent>(){
			public void handle(ActionEvent event) {
				File f = fileChooser.showOpenDialog(stg);
				
				if(f == null){
					return;
				}

				Path path = f.toPath();
				
				loadFromFile(path);
				setOpenedPath(path);
			}
		});
		
		return sce;
	}
 	
	private boolean saveCheck(){
		// Returns true if the user didn't cancel
		if(isSaved){
			confirmAlert.setHeaderText("Save changes to " + getOpenedName() + "?");
			Optional<ButtonType> btn = confirmAlert.showAndWait();
			
			btn.filter(r -> (r == ButtonType.OK)).ifPresent(r -> save());
			
			if(btn.filter(r -> (r == ButtonType.CANCEL)).isPresent()){
				return false; // Return false if canceled
			}
		}
		
		return true;
	}
	
	private void setOpenedPath(Path p){
		openedPath = p;
		// I thought I had to do something there but nope.
		// Well, I'll just update the title to be faster, but it'll be updated anyway on the next loop
		updateStageTitle();
	}
	
 	// Actions
	public void newWorld(){
		if(saveCheck()){
			initElogic();
		}
	}
	
	public void save(){
		if(openedPath != null){
			saveToFile(openedPath);
		}else{
			saveAs();
		}
	}
	
	public void saveAs(){
		File f = fileChooser.showSaveDialog(stg);
		
		if(f == null){
			return;
		}
		
		Path path = f.toPath();
		
		saveToFile(path);
		setOpenedPath(path);
	}
	
 	public void save(OutputStream str) throws IOException{
 		codec.encode(str, world);
 	}
 	
 	public void load(InputStream str) throws IOException{
 		world = codec.decode(str);
 	}
 	
 	public void saveToFile(Path p){
 		try {
 			// Creates the file if it doesn't exists
			save(Files.newOutputStream(p, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING));
		} catch (IOException e) {
			e.printStackTrace();
		}
 	}
 	
 	public void loadFromFile(Path p){
 		try {
			load(Files.newInputStream(p));
		} catch (IOException e) {
			e.printStackTrace();
		}
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
 	
	public void start(Stage s) throws Exception {
		this.stg = s;
		
		stg.setMaximized(true);
		stg.setTitle("Elektrode");
		stg.setMinWidth(580);
		stg.setMinHeight(460);
		stg.setScene(createScene(stg));
		stg.show();
		
		timer = new AnimationTimer(){
			public void handle(long now) {
				long nowms = now / 1000000l;
				
				if(lastFrameTime < 0){
					lastFrameTime = nowms;
				}
				
				double delta = nowms - lastFrameTime;
				
				loopUpdate(delta, nowms);
				
				lastFrameTime = nowms;
			}
		};
		
		timer.start();
		
		initElogic();
	}
	
	private double sceneToWorldX(double x){
		double wx = (x - cvs.getWidth()/2) / world.getViewport().getScale() - world.getViewport().getTranslateX();
		
		return wx;
	}
	
	private double sceneToWorldY(double y){
		double wy = (y - cvs.getHeight()/2) / world.getViewport().getScale() - world.getViewport().getTranslateY();
		
		return wy;
	}
	
	private void initElogic(Path open){
		if(open != null){
			loadFromFile(open);
		}else{
			world = new World();
		}
		
		originWireOut = null;
		
		world.getInventory().addAllItems(
				new WireItem(),
				new ActionItem(),
				new DeleteItem(),
				
				new NodeItem(Generator.class, "Generator"),
				new NodeItem(NoLogicGate.class, "No logic gate"),
				new NodeItem(AndLogicGate.class, "And logic gate"),
				new NodeItem(OrLogicGate.class, "Or logic gate"),
				new NodeItem(LampComponent.class, "Lamp"),
				new NodeItem(SwitchComponent.class, "Switch"),
				new NodeItem(DelayNode.class, "Delay node"));
	}
	
	private void initElogic(){
		initElogic(null);
	}
	
	private String getOpenedName(){
		String name = "Untitled";
		
		if(openedPath != null){
			name = openedPath.getFileName().toString();
		}
		
		return name;
	}
	
	private void updateStageTitle(){
		stg.setTitle("Elogic - "+getOpenedName()+" - "+Elektrode.this.fps+"FPS");
	}
	
	private void loopUpdate(double delta, long nowms){
		frameNum++;
		if(nowms - lastFPSUpdate > 1000l){
			Elektrode.this.fps = frameNum;
			updateStageTitle();
			
			frameNum = 0;
			lastFPSUpdate = nowms;
		}
		
		renderer.render(cvs, originWireOut, mousePos, world, delta, nowms);
	}
	
	public static void main(String[] args) {
		Elektrode.launch(args);
	}
}
