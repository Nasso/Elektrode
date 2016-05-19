package io.github.nasso.elektrode.view;

import io.github.nasso.elektrode.model.ActionItem;
import io.github.nasso.elektrode.model.AndLogicGate;
import io.github.nasso.elektrode.model.DelayNode;
import io.github.nasso.elektrode.model.DeleteItem;
import io.github.nasso.elektrode.model.Generator;
import io.github.nasso.elektrode.model.Input;
import io.github.nasso.elektrode.model.Inventory;
import io.github.nasso.elektrode.model.LampComponent;
import io.github.nasso.elektrode.model.LogicGate;
import io.github.nasso.elektrode.model.NoLogicGate;
import io.github.nasso.elektrode.model.Node;
import io.github.nasso.elektrode.model.NodeItem;
import io.github.nasso.elektrode.model.OrLogicGate;
import io.github.nasso.elektrode.model.Output;
import io.github.nasso.elektrode.model.SwitchComponent;
import io.github.nasso.elektrode.model.WireItem;
import io.github.nasso.elektrode.model.World;

import java.util.List;

import javafx.geometry.Point2D;
import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.Paint;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.ArcType;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.scene.transform.Affine;

import com.sun.javafx.tk.Toolkit;

public class ClassicRenderer implements Renderer {
	/*
	
	How to add a component renderer:
	- Add it to the RenderType enum
	- Add it to the determineRenderType method
	- Then render it in the renderRenderableAt method
	
	so ez m8 itz just meh mahself and i
	
	*/
	
	public static final Color
		BACK = Color.rgb(32, 32, 32),
		DISABLED_WIRE = Color.rgb(96, 70, 8),
		ENABLED_WIRE = Color.rgb(237, 171, 18),
		PASSIVE_NODE_STROKE = Color.WHITE,
		PASSIVE_NODE_FILL = Color.WHITE,
		ACTIVE_NODE_STROKE = ENABLED_WIRE,
		ACTIVE_NODE_FILL = ENABLED_WIRE,
		LAMP_ON = Color.rgb(255, 210, 0),
		LAMP_OFF = BACK,
		GRID = Color.rgb(48, 48, 48),
		INPUT = Color.WHITE,
		OUTPUT = Color.WHITE,
		HUD_TEXT = Color.WHITE,
		HUD_BACK = Color.rgb(16, 16, 16, 0.5),
		TOOL_ITEM_PRIMARY = Color.WHITE,
		TOOL_ITEM_SECONDARY = Color.rgb(237, 171, 18);
	
	public static final Font
		HUD_FONT = Font.font("Arial", 20);
	
	public static enum RenderType {
		UNKNOWN,
		
		NODE,
		GENERATOR_NODE,
		NO_LOGIC_GATE_NODE,
		AND_LOGIC_GATE_NODE,
		OR_LOGIC_GATE_NODE,
		
		OFF_LAMP_COMPONENT_NODE,
		ON_LAMP_COMPONENT_NODE,
		OPEN_SWITCH_COMPONENT,
		CLOSE_SWITCH_COMPONENT,
		DELAY_NODE,
		
		ITEM,
		WIRE_ITEM,
		ACTION_ITEM,
		DELETE_ITEM
	};
	
	private Canvas cvs = null;
	private double scale, translateX, translateY;
	private Inventory inventory = null;
	private Output originWireOutput = null;
	private Point2D mousePos = null;
	private List<Node> nodes = null;
	// private double delta = 0;
	private long nowms = 0;
	
	public void render(
			Canvas cvs,
			Viewport v,
			Inventory inventory,
			Output originWireOutput,
			Point2D mousePos,
			World world,
			double delta,
			long nowms) {
		this.cvs = cvs;
		this.scale = v.getScale();
		this.translateX = v.getTranslateX();
		this.translateY = v.getTranslateY();
		this.inventory = inventory;
		this.originWireOutput = originWireOutput;
		this.mousePos = mousePos;
		this.nodes = world.getNodes();
		// this.delta = delta;
		this.nowms = nowms;
		
		double scale = v.getScale();
		double translateX = v.getTranslateX();
		double translateY = v.getTranslateY();
		
		GraphicsContext gtx = cvs.getGraphicsContext2D();
		
		gtx.setFill(BACK);
		gtx.fillRect(0, 0, cvs.getWidth(), cvs.getHeight());
		
		gtx.save();
			gtx.translate(cvs.getWidth()/2, cvs.getHeight()/2);
			gtx.scale(1, -1);
			
			gtx.scale(scale, scale);
			gtx.translate(translateX, translateY);
			
			// Draw grid
			renderGrid();
			
			// Draw input/outputs if the wire is selected
			if(inventory.getSelectedItem() instanceof WireItem){
				renderIOMarks();
			}
			
			// Draw wires
			renderWires();
			
			// Draw nodes
			renderNodes();
			
			// Draw HUD (inventory...)
			renderHUD();
		gtx.restore();
	}
	
	private void renderIOMarks(){
		GraphicsContext gtx = cvs.getGraphicsContext2D();
		
		double inSize = 0.15;
		double outRadius = 0.3;
		double outAngle = 75;
		
		Paint inputPaint = new RadialGradient(0.0, 0.0, inSize, inSize, inSize, false, CycleMethod.NO_CYCLE, new Stop(0, INPUT), new Stop(1, Color.TRANSPARENT));
		Paint outputPaint = new RadialGradient(0.0, 0.0, outRadius, outRadius, outRadius, false, CycleMethod.NO_CYCLE, new Stop(0, OUTPUT), new Stop(1, Color.TRANSPARENT));
		
		gtx.save();
			gtx.setLineWidth(2 / scale);
			for(Node n : nodes){
				// Draw inputs
				Input[] ins = n.getInputs();
				for(int i = 0; i < ins.length; i++){
					Point2D ip = getInputPos(n, i);
					
					gtx.save();
	 					gtx.translate(n.getX() + ip.getX(), n.getY() + ip.getY());
	 					gtx.rotate(-90 * n.getOrientation());
	 					gtx.translate(-inSize, -inSize);
	 					
						gtx.setFill(inputPaint);
						
						gtx.fillRect(
							0,
							inSize/2,
							inSize,
							inSize
						);
					gtx.restore();
				}
				
				// Draw outputs
				Output[] outs = n.getOutputs();
				for(int i = 0; i < outs.length; i++){
					Point2D op = getOutputPos(n, i);
					
					gtx.save();
	 					gtx.translate(n.getX() + op.getX(), n.getY() + op.getY());
	 					gtx.rotate(-90 * n.getOrientation());
	 					
	 					gtx.translate(-outRadius, -outRadius);
						
						gtx.setFill(outputPaint);
						
						gtx.fillArc(
							0,
							0,
							outRadius*2,
							outRadius*2,
							-outAngle/2,
							outAngle,
							ArcType.ROUND
						);
					gtx.restore();
				}
			}
		gtx.restore();
	}
	
	private void renderHUD(){
		GraphicsContext gtx = cvs.getGraphicsContext2D();
		
		gtx.save();
			gtx.setEffect(new GaussianBlur(0.8));
			
			gtx.setGlobalAlpha(1.0);
			renderSquareAt(Color.BLACK, 50, cvs.getWidth()/100, 1, 1, 1);
			
			gtx.setGlobalAlpha(0.3);
			renderSquareAt(Color.BLACK, 50, cvs.getWidth()/100 + 1.2, 0.8, 1, 1);
			renderSquareAt(Color.BLACK, 50, cvs.getWidth()/100 - 1.2, 0.8, 1, 1);
			
			gtx.setGlobalAlpha(0.1);
			renderSquareAt(Color.BLACK, 50, cvs.getWidth()/100 + 2.4, 0.8, 1, 1);
			renderSquareAt(Color.BLACK, 50, cvs.getWidth()/100 - 2.4, 0.8, 1, 1);
			
			gtx.setEffect(null);
			
			gtx.setGlobalAlpha(1.0);
			renderRenderableAt(inventory.getItemFromSelected(0), cvs.getWidth()/100, 1, 50);
			
			gtx.setGlobalAlpha(0.4);
			renderRenderableAt(inventory.getItemFromSelected(1), cvs.getWidth()/100 + 1.2, 0.8, 50);
			
			renderRenderableAt(inventory.getItemFromSelected(-1), cvs.getWidth()/100 - 1.2, 0.8, 50);
			
			gtx.setGlobalAlpha(0.1);
			
			renderRenderableAt(inventory.getItemFromSelected(2), cvs.getWidth()/100 + 2.4, 0.8, 50);
			
			renderRenderableAt(inventory.getItemFromSelected(-2), cvs.getWidth()/100 - 2.4, 0.8, 50);
		gtx.restore();
		
		// Item name
		gtx.save();
			gtx.setTransform(new Affine());
			
			// BACKGROUND
			String displayName = inventory.getSelectedItem().getDisplayName();
			
			double tw = Toolkit.getToolkit().getFontLoader().getFontMetrics(HUD_FONT).computeStringWidth(displayName);
			
			double vMargin = 4;
			double hMargin = 8;
			
			gtx.setFill(HUD_BACK);
			
			gtx.fillRect(cvs.getWidth()/2 - tw/2 - hMargin, cvs.getHeight()-100 - HUD_FONT.getSize()/2 - vMargin, tw + hMargin*2, HUD_FONT.getSize() + vMargin*2);
			
			// END_BACKGROUND
			
			gtx.setFill(HUD_TEXT);
			gtx.setFont(HUD_FONT);
			
			gtx.setTextAlign(TextAlignment.CENTER);
			gtx.setTextBaseline(VPos.CENTER);
			
			gtx.fillText(displayName, cvs.getWidth()/2, cvs.getHeight() - 100);
		gtx.restore();
	}
	
	private void renderNodes(){
		// Draw components
		for(Node n : nodes){
			renderRenderableInWorld(n);
		}
	}
	
	private void renderWires(){
		GraphicsContext gtx = cvs.getGraphicsContext2D();
		
		gtx.save();
			gtx.setLineWidth(0.05);
			
			for(Node n : nodes){
				gtx.save();
					Output[] outs = n.getOutputs();
					for(int i = 0; i < outs.length; i++){
						Output out = outs[i];
						
						Point2D outp = getOutputPos(n, i);
						
						gtx.setStroke(DISABLED_WIRE);
						
						for(int j = 0; j < out.getDestinations().length; j++){
							Input dest = out.getDestination(j);
							
							Node destOwner = dest.getOwner();
							
							Point2D dip = getInputPos(destOwner, j);
							
							gtx.strokeLine(
								n.getX() + outp.getX(),
								n.getY() + outp.getY(),
								
								destOwner.getX() + dip.getX(),
								destOwner.getY() + dip.getY()
							);
							
							if(out.isOn()){
								gtx.save();
									gtx.setStroke(ENABLED_WIRE);
									
									gtx.setEffect(new GaussianBlur(16 / scale));
									
									gtx.setLineDashOffset((nowms * -0.0005));
									gtx.setLineDashes(0.1);
									gtx.strokeLine(
										n.getX() + outp.getX(),
										n.getY() + outp.getY(),
										
										destOwner.getX() + dip.getX(),
										destOwner.getY() + dip.getY()
									);
									
									gtx.setEffect(null);
									
									gtx.strokeLine(
										n.getX() + outp.getX(),
										n.getY() + outp.getY(),
										
										destOwner.getX() + dip.getX(),
										destOwner.getY() + dip.getY()
									);
								gtx.restore();
							}
						}
						
						// Then render to the mouse if it is the origin wire
						if(out == originWireOutput){
							double mx = sceneToWorldX(mousePos.getX());
							double my = sceneToWorldY(mousePos.getY());
							
							Point2D op = getOutputPos(n, out.getOwnerIndex());
							
							gtx.setStroke(DISABLED_WIRE);
							
							gtx.strokeLine(
									n.getX() + op.getX(),
									n.getY() + op.getY(),
									
									mx,
									my
								);
						}
					}
				gtx.restore();
			}
		gtx.restore();
	}
	
	private void renderGrid(){
		GraphicsContext gtx = cvs.getGraphicsContext2D();
		
		gtx.save();
			gtx.setStroke(GRID);
			gtx.setLineWidth(1 / scale);
			
			int startX = 0;
			int startY = 0;
			int endX = 0;
			int endY = 0;
			
			int caseCountX = (int) (cvs.getWidth() / scale) + 1;
			int caseCountY = (int) (cvs.getHeight() / scale) + 1;
			
			startX = -caseCountX / 2 - 1;
			startY = -caseCountY / 2 - 1;
			
			startX -= translateX + 1;
			startY -= translateY + 1;
			
			endX = startX + caseCountX + 1;
			endY = startY + caseCountY + 1;
			
			for(int x = startX; x < endX; x++){
				gtx.strokeLine(x+0.5, startY + 0.5, x+0.5, endY + 0.5);
			}
			for(int y = startY; y < endY; y++){
				gtx.strokeLine(startX + 0.5, y+0.5, endX + 0.5, y+0.5);
			}
		gtx.restore();
	}
	
	private void renderRenderableInWorld(Renderable r){
		renderRenderableAt(r, r.getX() + translateX + cvs.getWidth()/2 / scale, r.getY() + translateY + cvs.getHeight()/2 / scale);
	}
	
	private void renderRenderableAt(Renderable r, double x, double y){
		this.renderRenderableAt(r, x, y, scale);
	}
	
	private void renderRenderableAt(Renderable r, double x, double y, double scale){
		this.renderRenderableAt(r, x, y, r.getWidth(), r.getHeight(), scale);
	}
	
	/**
	 * This is where everything (almost) is done
	 * @param r
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @param scale
	 */
	private void renderRenderableAt(Renderable r, double x, double y, double width, double height, double scale){
		GraphicsContext gtx = cvs.getGraphicsContext2D();
		
		RenderType renderType = determineRenderType(r); 
		
		boolean isActive = false;
		boolean isInverted = false;
		
		// Just check activation if Node.
		if(r instanceof Node){
			Node n = (Node) r;
			
			for(Output out : n.getOutputs()){
				if(out.isOn()){
					isActive = true;
					break;
				}
			}
			
			if(n instanceof LogicGate){
				isInverted = ((LogicGate) n).isInverted();
			}
		}
		
		gtx.save();
			gtx.setLineWidth(1/scale);
			gtx.setFont(Font.font("Arial", 0.5 * scale));
	
			gtx.setTextBaseline(VPos.CENTER);
			gtx.setTextAlign(TextAlignment.CENTER);
			
			gtx.setTransform(new Affine());
			
			gtx.translate(0, cvs.getHeight());
			gtx.scale(scale, -scale);
			
			// Rotate !
			gtx.translate(x, y);
			gtx.rotate(-90 * r.getOrientation());
			
			// Rendering :D
			gtx.setFill(BACK);
			if(isActive){
				gtx.setStroke(ACTIVE_NODE_STROKE);
			}else{
				gtx.setStroke(PASSIVE_NODE_STROKE);
			}
			
			if(renderType == RenderType.GENERATOR_NODE){
				renderSquareStringAt("G", isActive ? ACTIVE_NODE_FILL : PASSIVE_NODE_FILL, scale, 0, 0, width, height);
			}else if(renderType == RenderType.NO_LOGIC_GATE_NODE){
				gtx.fillRect(-width/2, -height/2, width, height);
				gtx.strokeRect(-width/2, -height/2, width, height);
	
				if(isActive){
					gtx.setFill(ACTIVE_NODE_FILL);
				}else{
					gtx.setFill(PASSIVE_NODE_FILL);
				}
				
				gtx.beginPath();
					gtx.moveTo(-width/4, height/4);
					gtx.lineTo(width/4, 0);
					gtx.lineTo(-width/4, -height/4);
				gtx.closePath();
				
				gtx.fill();
				
				if(!isInverted){
					gtx.setLineWidth(0.01);
					gtx.strokeOval(width/4, -0.05, 0.1, 0.1);
				}
			}else if(renderType == RenderType.AND_LOGIC_GATE_NODE){
				gtx.fillRect(-width/2, -height/2, width, height);
				gtx.strokeRect(-width/2, -height/2, width, height);
	
				if(isActive){
					gtx.setFill(ACTIVE_NODE_FILL);
				}else{
					gtx.setFill(PASSIVE_NODE_FILL);
				}
				
				gtx.beginPath();
					gtx.rect(-width/4, height/4, width/4, -height/2);
					gtx.arc(0, 0, width/4, height/4, -90, 180);
				gtx.closePath();
				
				gtx.fill();
				
				if(isInverted){
					gtx.setLineWidth(0.01);
					gtx.strokeOval(width/4, -0.05, 0.1, 0.1);
				}
			}else if(renderType == RenderType.OR_LOGIC_GATE_NODE){
				gtx.fillRect(-width/2, -height/2, width, height);
				gtx.strokeRect(-width/2, -height/2, width, height);
	
				if(isActive){
					gtx.setFill(ACTIVE_NODE_FILL);
				}else{
					gtx.setFill(PASSIVE_NODE_FILL);
				}
				
				gtx.beginPath();
					gtx.arc(-width/4, 0, width/2, height/4, -90, 180);
					gtx.arc(-width/4, 0, width/8, height/4, 90, -180);
				gtx.closePath();
				
				gtx.fill();
				
				if(isInverted){
					gtx.setLineWidth(0.01);
					gtx.strokeOval(width/4, -0.05, 0.1, 0.1);
				}
			}else if(renderType == RenderType.ON_LAMP_COMPONENT_NODE || renderType == RenderType.OFF_LAMP_COMPONENT_NODE){
				if(renderType == RenderType.ON_LAMP_COMPONENT_NODE){
					gtx.setFill(LAMP_ON);
					
					gtx.setEffect(new GaussianBlur(1));
					gtx.fillOval(-width * 0.7, -height * 0.7, width * 1.4, height * 1.4);
					
					gtx.setEffect(null);
				}else{
					gtx.setFill(LAMP_OFF);
				}
				
				gtx.fillOval(-width/2, -height/2, width, height);
				gtx.strokeOval(-width/2, -height/2, width, height);
				
				gtx.save();
					gtx.translate(0, 0);
					gtx.rotate(45);
					gtx.strokeLine(-width/2, 0, width/2, 0);
					gtx.rotate(90);
					gtx.strokeLine(-width/2, 0, width/2, 0);
				gtx.restore();
			}else if(renderType == RenderType.OPEN_SWITCH_COMPONENT || renderType == RenderType.CLOSE_SWITCH_COMPONENT){
				gtx.setFill(PASSIVE_NODE_FILL);
				
				gtx.save();
					gtx.translate(-width/2, 0);
					if(renderType == RenderType.OPEN_SWITCH_COMPONENT){
						gtx.rotate(20);
					}
					
					gtx.setStroke(DISABLED_WIRE);
					gtx.setLineWidth(0.05);
					
					gtx.strokeLine(0, 0, width, 0);
					
					if(isActive){
						gtx.save();
							gtx.setStroke(ENABLED_WIRE);
							
							gtx.setEffect(new GaussianBlur(16 / scale));
							
							gtx.setLineDashOffset((nowms * -0.0005));
							gtx.setLineDashes(0.1);
							gtx.strokeLine(
								0,
								0,
								
								width,
								0
							);
							
							gtx.setEffect(null);
							
							gtx.strokeLine(
								0,
								0,
								
								width,
								0
							);
						gtx.restore();
					}
					
					gtx.setFill(PASSIVE_NODE_FILL);
					gtx.fillOval(-width/20, -height/20, width/10, height/10);
				gtx.restore();
				
				gtx.fillOval(width/2 - width/20, -height/20, width/10, height/10);
			}else if(renderType == RenderType.WIRE_ITEM){
				// Render like a wire portion
				
				gtx.setStroke(PASSIVE_NODE_STROKE);
				gtx.fillRect(-width/2, -height/2, width, height);
				gtx.strokeRect(-width/2, -height/2, width, height);
				
				gtx.setStroke(DISABLED_WIRE);
				gtx.strokeLine(-width/4, 0, width/4, 0);
			}else if(renderType == RenderType.ACTION_ITEM){
				// Render a wheel
				gtx.setStroke(PASSIVE_NODE_STROKE);
				gtx.fillRect(-width/2, -height/2, width, height);
				gtx.strokeRect(-width/2, -height/2, width, height);
				
				int toothCount = 6;
				double toothAngle = 45;
				double toothEAngle = 20;
				double toothOffset = nowms * 0.05;
				
				double innerRadius = width/10;
				double radius = width/4;
				double outerRadius = (width*3)/8; // width/4 + width/8
				
				// First pass, with blur
				gtx.setEffect(new GaussianBlur(0.08));
				gtx.setFill(TOOL_ITEM_SECONDARY);
				for(int i = 0; i < toothCount; i++){
					double percent = (double) i / (double) toothCount;
					double ang = 360.0 * percent + toothOffset - 90 - toothAngle/2; // minus 90 because better to start north !

					gtx.beginPath();
						gtx.arc(0, 0, radius, radius, ang, toothAngle);
						gtx.arc(0, 0, outerRadius, outerRadius, ang + toothAngle - (toothAngle - toothEAngle)/2, -toothEAngle);
						gtx.fill();
					gtx.closePath();
				}

				gtx.setFill(TOOL_ITEM_PRIMARY);
				gtx.beginPath();
					gtx.arc(0, 0, innerRadius, innerRadius, 0, 360);
					gtx.arc(0, 0, radius, radius, 0, -360);
					gtx.fill();
				gtx.closePath();

				// Second pass, without blur
				gtx.setEffect(null);
				gtx.setFill(TOOL_ITEM_SECONDARY);
				for(int i = 0; i < toothCount; i++){
					double percent = (double) i / (double) toothCount;
					double ang = 360.0 * percent + toothOffset - 90 - toothAngle/2; // minus 90 because better to start north !

					gtx.beginPath();
						gtx.arc(0, 0, radius, radius, ang, toothAngle);
						gtx.arc(0, 0, outerRadius, outerRadius, ang + toothAngle - (toothAngle - toothEAngle)/2, -toothEAngle);
						gtx.fill();
					gtx.closePath();
				}

				gtx.setFill(TOOL_ITEM_PRIMARY);
				gtx.beginPath();
					gtx.arc(0, 0, innerRadius, innerRadius, 0, 360);
					gtx.arc(0, 0, radius, radius, 0, -360);
					gtx.fill();
				gtx.closePath();
			}else if(renderType == RenderType.DELETE_ITEM){
				// Render like a wire portion
				
				gtx.setStroke(PASSIVE_NODE_STROKE);
				gtx.fillRect(-width/2, -height/2, width, height);
				gtx.strokeRect(-width/2, -height/2, width, height);
				
				gtx.setStroke(TOOL_ITEM_PRIMARY);
				gtx.setLineWidth(0.05);
				
				gtx.beginPath();
					gtx.moveTo(-width/4, -height/4);
					gtx.lineTo(width/4, height/4);
					gtx.moveTo(-width/4, height/4);
					gtx.lineTo(width/4, -height/4);
					gtx.stroke();
				gtx.closePath();
			}else if(renderType == RenderType.DELAY_NODE){
				// Render like a wire portion
				gtx.setStroke(PASSIVE_NODE_STROKE);
				gtx.fillRect(-width/2, -height/2, width, height);
				gtx.strokeRect(-width/2, -height/2, width, height);
				
				// Oval
				gtx.setLineWidth(0.04);
				gtx.strokeOval(-width/4, -height/4, width/2, height/2);
				
				// Time marks
				int timeMarksCount = 24;
				
				gtx.setLineWidth(0.01);
				for(int i = 0; i < timeMarksCount; i++){
					double percent = (double) i / (double) timeMarksCount;
					double ang = 360 * percent - nowms * 0.005;
					
					gtx.save();
						gtx.rotate(ang);
						
						gtx.strokeLine(width/4-0.05, 0, width/4-0.08, 0);
					gtx.restore();
				}
				
				if(r instanceof DelayNode){ // IF IT IS A DELAY NODE LOL
					DelayNode dn = (DelayNode) r;
					
					if(getNodeAt(mousePos.getX(), mousePos.getY()) == dn){ // IF WE ARE POINTING AT IT OMG
						gtx.save();
							gtx.setTransform(new Affine());
							
							gtx.setFont(HUD_FONT);
							
							gtx.setFill(PASSIVE_NODE_FILL);
							gtx.setTextBaseline(VPos.BOTTOM);
							gtx.setTextAlign(TextAlignment.LEFT);
							gtx.fillText(dn.getDelay()+" ms", mousePos.getX(), cvs.getHeight() - mousePos.getY());
						gtx.restore();
					}
				}
			}else{
				renderSquareStringAt("?", isActive ? ACTIVE_NODE_FILL : PASSIVE_NODE_FILL, scale, 0, 0, width, height);
			}
			
		gtx.restore();
	}

	private void renderSquareAt(Paint p, double scale, double x, double y, double width, double height){
		GraphicsContext gtx = cvs.getGraphicsContext2D();
		
		gtx.save();
			gtx.setTransform(new Affine());
			
			gtx.translate(0, cvs.getHeight());
			gtx.scale(scale, -scale);
			
			// Filling :D
			gtx.setFill(p);
			gtx.fillRect(x - width/2, y - height/2, width, height);
		gtx.restore();
	}
	
	private void renderSquareStringAt(String c, Paint color, double scale, double x, double y, double width, double height){
		GraphicsContext gtx = cvs.getGraphicsContext2D();
		
		gtx.fillRect(x - width/2, y - height/2, width, height);
		gtx.strokeRect(x - width/2, y - height/2, width, height);
		
		gtx.setFill(color);
		
		gtx.save();
			gtx.scale(1/scale, -1/scale);
			double textX = x * scale;
			double textY = y * scale;
			
			gtx.fillText(String.valueOf(c), textX, -textY);
		gtx.restore();
	}
	
	// utilities
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
	
	private double sceneToWorldX(double x){
		double wx = (x - cvs.getWidth()/2) / scale - translateX;
		
		return wx;
	}
	
	private double sceneToWorldY(double y){
		double wy = (y - cvs.getHeight()/2) / scale - translateY;
		
		return wy;
	}
	
	private int sceneToWorldXCase(double x){
 		int cx = (int) Math.floor(sceneToWorldX(x) + 0.5);
		return cx;
	}
	
	private int sceneToWorldYCase(double y){
		int cy = (int) Math.ceil(sceneToWorldY(y) - 0.5);
		return cy;
	}
	
 	private Node getNodeAt(double sceneX, double sceneY){
 		int cx = sceneToWorldXCase(sceneX);
		int cy = sceneToWorldYCase(sceneY);
		
		for(Node n : nodes){
			if(n.getX() == cx && n.getY() == cy){ // if there is already a node
				return n;
			}
		}
	 	
 		return null;
 	}
	
	private RenderType determineRenderType(Renderable r){
		Class<? extends Renderable> c = null;
		
		if(r instanceof NodeItem){
			c = ((NodeItem) r).getSource();
		}else{
			c = r.getClass();
		}
		
		RenderType rt = RenderType.UNKNOWN;
		
		if(c == Generator.class){
			rt = RenderType.GENERATOR_NODE;
		}else if(c == NoLogicGate.class){
			rt = RenderType.NO_LOGIC_GATE_NODE;
		}else if(c == AndLogicGate.class){
			rt = RenderType.AND_LOGIC_GATE_NODE;
		}else if(c == OrLogicGate.class){
			rt = RenderType.OR_LOGIC_GATE_NODE;
		}else if(c == LampComponent.class){
			if(r instanceof LampComponent){ // If the renderable IS a lamp
				if(((LampComponent) r).isActivated()){
					rt = RenderType.ON_LAMP_COMPONENT_NODE;
				}else{
					rt = RenderType.OFF_LAMP_COMPONENT_NODE;
				}
			}else{ // else, it's just a NodeItem of a lamp
				rt = RenderType.OFF_LAMP_COMPONENT_NODE;
			}
		}else if(c == SwitchComponent.class){
			if(r instanceof SwitchComponent){
				if(((SwitchComponent) r).getState()){
					rt = RenderType.CLOSE_SWITCH_COMPONENT;
				}else{
					rt = RenderType.OPEN_SWITCH_COMPONENT;
				}
			}else{
				rt = RenderType.OPEN_SWITCH_COMPONENT;
			}
		}else if(c == WireItem.class){
			rt = RenderType.WIRE_ITEM;
		}else if(c == ActionItem.class){
			rt = RenderType.ACTION_ITEM;
		}else if(c == DeleteItem.class){
			rt = RenderType.DELETE_ITEM;
		}else if(c == DelayNode.class){
			rt = RenderType.DELAY_NODE;
		}
		
		return rt;
	}
}
