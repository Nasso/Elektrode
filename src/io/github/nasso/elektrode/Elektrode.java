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
import io.github.nasso.elektrode.model.MoveItem;
import io.github.nasso.elektrode.model.NoLogicGate;
import io.github.nasso.elektrode.model.Node;
import io.github.nasso.elektrode.model.NodeItem;
import io.github.nasso.elektrode.model.OrLogicGate;
import io.github.nasso.elektrode.model.Output;
import io.github.nasso.elektrode.model.SwitchComponent;
import io.github.nasso.elektrode.model.WireItem;
import io.github.nasso.elektrode.model.World;
import io.github.nasso.elektrode.view.ClassicView;
import io.github.nasso.elektrode.view.View;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Optional;

import javafx.animation.Animation;
import javafx.animation.AnimationTimer;
import javafx.animation.Transition;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
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
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Elektrode extends Application {
	/*
	 * 
	 * CTRL + SHIFT + MULTIPLY
	 * 		collapse
	 * 
	 * CTRL + MULTIPLY
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
	
	private View view = new ClassicView();
	private World world = new World();
	private WorldCodec codec = new XMLWorldCodec();
	private Path openedPath = null;
	private FileChooser fileChooser = null;
	
	private Output originWireOut = null;
	private Node movingNode = null;
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
		
		cvs.setOnScroll(new EventHandler<ScrollEvent>() {
			public void handle(ScrollEvent event) {
				if(event.isControlDown()){
					double scale = world.getViewport().getScale();
					
					scale += event.getDeltaY() / 10;
					
					scale = Math.min(scale, 200);
					scale = Math.max(10, scale);
					
					world.getViewport().setScale(scale);
				}else{
					world.getInventory().moveSelectedSlot((int) -Math.signum(event.getDeltaY()));

					InventoryItem item = world.getInventory().getSelectedItem();
					
					if(item instanceof MoveItem){
						cvs.setCursor(Cursor.MOVE);
					}else{
						cvs.setCursor(Cursor.DEFAULT);
					}
				}
			}
		});
		
		cvs.setOnMousePressed(new EventHandler<MouseEvent>(){
			public void handle(MouseEvent event) {
				boolean saveStateBefore = isSaved;
				
				InventoryItem item = world.getInventory().getSelectedItem();
				
				double sceneCX = event.getX() - cvs.getWidth()/2;
				double sceneCY = -event.getY() + cvs.getHeight()/2;
				
			 	if(event.getButton() == MouseButton.PRIMARY){
					lastX = event.getSceneX();
					lastY = event.getSceneY();
				}else if(event.getButton() == MouseButton.SECONDARY){
					if(item instanceof NodeItem){
						Node clone = ((NodeItem) item).createNodeFromSource();
						
						int cx = world.getCaseX(sceneCX);
						int cy = world.getCaseY(sceneCY);
						
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
						isSaved = false;
					}else if(item instanceof WireItem){
						Output out = world.getOutputAt(sceneCX, sceneCY); // returns null if unfound
						
						if(out != null){ // if found
							originWireOut = out; // take a wire from there
						}else{
							Input in = world.getInputAt(sceneCX, sceneCY); // returns null if unfound
							
							if(in != null){ // if found
								if(in.getOrigin() != null){ // take the wire
									originWireOut = in.getOrigin();
									originWireOut.removeDestination(in); // remove the old
									isSaved = false;
								}
							}
						}
					}else if(item instanceof ActionItem){
						Node n = world.getNodeOn(sceneCX, sceneCY);
						
						if(n != null){
							n.onAction();
							isSaved = false;
						}
					}else if(item instanceof MoveItem){
						movingNode = world.getNodeOn(sceneCX, sceneCY);
					}else if(item instanceof DeleteItem){
						Node n = world.getNodeOn(sceneCX, sceneCY);
						
						if(n != null){
							n.clearInputs();
							n.clearOutputs();
							world.getNodes().remove(n);
							isSaved = false;
						}
					}
				}else if(event.getButton() == MouseButton.MIDDLE){
					// Always turn
					Node n = world.getNodeOn(sceneCX, sceneCY);
					
					if(n != null){
						n.turnRight();
						isSaved = false;
					}
				}
			 	
			 	if(saveStateBefore != isSaved){ // If changes
			 		updateStageTitle();
			 	}
			}
		});
		
		cvs.setOnMouseReleased(new EventHandler<MouseEvent>(){
			public void handle(MouseEvent event) {
				boolean saveStateBefore = isSaved;
				
				double sceneCX = event.getX() - cvs.getWidth()/2;
				double sceneCY = -event.getY() + cvs.getHeight()/2;
				
				if(event.getButton() == MouseButton.SECONDARY){
					if(originWireOut != null){ // relies the wire if there is one
						Input in = world.getInputAt(sceneCX, sceneCY); // returns null if unfound
						
						if(in == null){
							originWireOut = null;
						}else if(in.getOwner() != originWireOut.getOwner()){
							originWireOut.addDestination(in);
							isSaved = false;
							
							originWireOut = null;
						}
					}
					
					movingNode = null;
				}
			 	
			 	if(saveStateBefore != isSaved){ // If changes
			 		updateStageTitle();
			 	}
			}
		});
		
		cvs.setOnMouseDragged(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent event) {
				double sceneCX = event.getX() - cvs.getWidth()/2;
				double sceneCY = -event.getY() + cvs.getHeight()/2;
				
				mousePos = new Point2D(sceneCX, sceneCY);
				
				if(event.getButton() == MouseButton.PRIMARY){
					world.getViewport().translate(
							(event.getSceneX() - lastX) / world.getViewport().getScale(),
							-(event.getSceneY() - lastY) / world.getViewport().getScale());
					
					lastX = event.getSceneX();
					lastY = event.getSceneY();
				}else if(event.getButton() == MouseButton.SECONDARY){
					InventoryItem item = world.getInventory().getSelectedItem();
					
					if(item instanceof MoveItem){
						if(movingNode != null){
							int cx = world.getCaseX(sceneCX);
							int cy = world.getCaseY(sceneCY);
							
							movingNode.setX(cx);
							movingNode.setY(cy);
						}
					}else if(item instanceof DeleteItem){
						Node n = world.getNodeOn(sceneCX, sceneCY);
						
						if(n != null){
							n.clearInputs();
							n.clearOutputs();
							world.getNodes().remove(n);
							isSaved = false;
						}
					}
				}
			}
		});
		
		cvs.setOnMouseMoved(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent event) {
				double sceneCX = event.getX() - cvs.getWidth()/2;
				double sceneCY = -event.getY() + cvs.getHeight()/2;
				
				mousePos = new Point2D(sceneCX, sceneCY);
			}
		});
		
		cvs.setOnKeyPressed(new EventHandler<KeyEvent>() {
			public void handle(KeyEvent event) {
				if(event.getCode() == KeyCode.ESCAPE){
					// release the wire !!
					
					originWireOut = null;
				}
			}
		});
		
		// Cvs
		cvs.layoutYProperty().bind(menuBar.heightProperty());
		cvs.widthProperty().bind(sce.widthProperty());
		cvs.heightProperty().bind(sce.heightProperty().subtract(menuBar.heightProperty()));
		
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
		if(!isSaved){
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
		
		isSaved = true;
		updateStageTitle();
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
		
		List<String> args = this.getParameters().getUnnamed();
		Path arg = null;
		
		if(!args.isEmpty()) arg = Paths.get(args.get(0));
		
		initElogic(arg);
	}
	
	private void initElogic(Path open){
		if(open != null){
			loadFromFile(open);
			openedPath = open;
		}else{
			world = new World();
		}
		
		originWireOut = null;
		
		world.getInventory().addAllItems(
				new WireItem(),
				new ActionItem(),
				new MoveItem(),
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
		stg.setTitle("Elektrode - "+getOpenedName()+(isSaved ? "" : "*")+" - "+Elektrode.this.fps+"FPS");
	}
	
	private void loopUpdate(double delta, long nowms){
		frameNum++;
		if(nowms - lastFPSUpdate > 1000l){
			Elektrode.this.fps = frameNum;
			updateStageTitle();
			
			frameNum = 0;
			lastFPSUpdate = nowms;
		}
		
		view.renderWorld(cvs, originWireOut, mousePos, world, delta, nowms);
	}
	
	public static void main(String[] args) {
		Elektrode.launch(args);
	}
}
