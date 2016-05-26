package io.github.nasso.elektrode.data;

import io.github.nasso.elektrode.model.Input;
import io.github.nasso.elektrode.model.InventoryItem;
import io.github.nasso.elektrode.model.Node;
import io.github.nasso.elektrode.model.Output;
import io.github.nasso.elektrode.model.World;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class XMLWorldCodec extends WorldCodec {
	private DocumentBuilderFactory dbf;
	private DocumentBuilder builder;
	
	private TransformerFactory tf;
	private Transformer tr;
	
	public XMLWorldCodec(){
		dbf = DocumentBuilderFactory.newInstance();
		dbf.setIgnoringElementContentWhitespace(true);
		
		try {
			builder = dbf.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		
		
		tf = TransformerFactory.newInstance();
		
		try {
			tr = tf.newTransformer();
			tr.setOutputProperty(OutputKeys.INDENT, "yes");
			tr.setOutputProperty(OutputKeys.METHOD, "xml");
			tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		}
	}
	
	public void encode(OutputStream out, World world) throws IOException {
		Document dom = builder.newDocument();
		Comment codecInfos = dom.createComment(" Generator: "+getClass().getSimpleName()+" ");
		
		Element root = dom.createElement("world");
		root.appendChild(codecInfos);
		
		// Save viewport
		Element viewportElem = dom.createElement("viewport");
		viewportElem.setAttribute("scale", String.valueOf(world.getViewport().getScale()));
		viewportElem.setAttribute("transX", String.valueOf(world.getViewport().getTranslateX()));
		viewportElem.setAttribute("transY", String.valueOf(world.getViewport().getTranslateY()));

		// Save inventory
		Element inventoryElem = dom.createElement("inventory");
		InventoryItem[] items = world.getInventory().getContent();
		
		inventoryElem.setAttribute("itemCount", String.valueOf(items.length));
		inventoryElem.setAttribute("selected", String.valueOf(world.getInventory().getSelectedSlot()));
		
		for(int i = 0; i < items.length; i++){
			InventoryItem item = items[i];
			
			Element itemElem = dom.createElement("item");
			itemElem.setAttribute("type", item.getClass().getCanonicalName());
			itemElem.setAttribute("hid", String.valueOf(i));
			
			Element pe = dom.createElement("properties");
			for(String pname : item.getProperties().keySet()){
				Object prop = item.getProperty(pname);
				
				Element prope = dom.createElement("prop");
				prope.setAttribute("name", pname);
				prope.setAttribute("type", prop.getClass().getCanonicalName());
				
				if(isPrimitiveBinding(prop.getClass())){ // Writes it only if it is a primitive type (so I can parse it)
					prope.setTextContent(prop.toString());
				}
				
				pe.appendChild(prope);
			}
			itemElem.appendChild(pe);
			
			inventoryElem.appendChild(itemElem);
		}
		
		// Save nodes
		Element nodesRoot = dom.createElement("nodes");
		nodesRoot.setAttribute("nodeCount", String.valueOf(world.getNodes().size()));
		List<Node> nodes = world.getNodes();
		for(Node n : nodes){
			Element nodeElem = dom.createElement("node");
			nodeElem.setAttribute("type", n.getClass().getCanonicalName());
			nodeElem.setAttribute("hid", String.valueOf(nodes.indexOf(n)));
			
			// Position infos
			nodeElem.appendChild(createDOMNode(dom, "worldX", n.getX()));
			nodeElem.appendChild(createDOMNode(dom, "worldY", n.getY()));
			nodeElem.appendChild(createDOMNode(dom, "orientation", n.getOrientation()));
			
			// Adds all properties in a <properties> node
			Element pe = dom.createElement("properties");
			for(String pname : n.getProperties().keySet()){
				Object prop = n.getProperty(pname);
				
				Element prope = dom.createElement("prop");
				prope.setAttribute("name", pname);
				prope.setAttribute("type", prop.getClass().getCanonicalName());
				
				if(isPrimitiveBinding(prop.getClass())){ // Writes it only if it is a primitive type (so I can parse it)
					prope.setTextContent(prop.toString());
				}
				
				pe.appendChild(prope);
			}
			nodeElem.appendChild(pe);
			
			// Adds all outputs in a <outs> node
			int outputCount = n.getOutputs().length;
			Element outputsElem = dom.createElement("outputs");
			outputsElem.setAttribute("outputCount", String.valueOf(outputCount));
			for(int i = 0; i < outputCount; i++){
				Output o = n.getOutput(i);
				
				Element outElem = dom.createElement("out");
				for(Input dest : o.getDestinations()){
					Element destElem = dom.createElement("dest");
					destElem.setAttribute("targetHID", String.valueOf(nodes.indexOf(dest.getOwner())));
					destElem.setAttribute("targetInput", String.valueOf(dest.getOwnerIndex()));
					outElem.appendChild(destElem);
				}
				outElem.setAttribute("outID", String.valueOf(i));
				outElem.setAttribute("state", String.valueOf(o.isOn()));
				outputsElem.appendChild(outElem);
			}
			nodeElem.appendChild(outputsElem);
			
			Element inputElem = dom.createElement("inputs");
			inputElem.setAttribute("inputCount", String.valueOf(n.getInputs().length));
			nodeElem.appendChild(inputElem);
			
			// Adds to the world the node
			nodesRoot.appendChild(nodeElem);
		}
		
		root.appendChild(viewportElem);
		root.appendChild(inventoryElem);
		root.appendChild(nodesRoot);
		dom.appendChild(root);
		
		try {
			tr.transform(new DOMSource(dom), new StreamResult(out));
		} catch (TransformerException e) {
			e.printStackTrace();
		}
	}

	public World decode(InputStream in) throws IOException {
		Document dom = null;
		try {
			dom = builder.parse(in);
		} catch (SAXException e) {
			e.printStackTrace();
		}
		
		// Return a new world if fail
		if(dom == null) return new World();
		
		Element root = (Element) dom.getElementsByTagName("world").item(0);
		
		// Viewport
		Element viewportElem = (Element) root.getElementsByTagName("viewport").item(0);
		double scale = Double.parseDouble(viewportElem.getAttribute("scale"));
		double transX = Double.parseDouble(viewportElem.getAttribute("transX"));
		double transY = Double.parseDouble(viewportElem.getAttribute("transY"));
		
		// Inventory
		Element inventoryElem = (Element) root.getElementsByTagName("inventory").item(0);
		int itemCount = Integer.parseInt(inventoryElem.getAttribute("itemCount"));
		int selectedSlot = Integer.parseInt(inventoryElem.getAttribute("selected"));
		
		InventoryItem[] items = new InventoryItem[itemCount];
		
		NodeList itemNodes = inventoryElem.getElementsByTagName("item");
		for(int i = 0; i < itemNodes.getLength(); i++){
			Element itemElem = (Element) itemNodes.item(i);
			
			int hid = Integer.parseInt(itemElem.getAttribute("hid"));
			
			if(hid < 0 || hid >= items.length){
				continue; // never trust external data
			}
			
			String type = itemElem.getAttribute("type");
			
			InventoryItem item = null;
			Class<?> classType = null;
			try {
				classType = Class.forName(type);
				
				if(InventoryItem.class.isAssignableFrom(classType)){
					item = (InventoryItem) classType.newInstance();
				}
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
			
			// Properties
			Element propertiesElem = (Element) itemElem.getElementsByTagName("properties").item(0);
			NodeList propsDOMNodes = propertiesElem.getElementsByTagName("prop");
			for(int j = 0; j < propsDOMNodes.getLength(); j++){
				Element propElem = (Element) propsDOMNodes.item(j);
				String propClassName = propElem.getAttribute("type");
				
				Class<?> propClass = null;
				
				try {
					propClass = Class.forName(propClassName);
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
				
				if(propClass == null){
					System.out.println("Skip unknown class: "+propClassName);
					continue; // skip if invalid
				}
				
				item.setProperty(
					propElem.getAttribute("name"),
					parsePrimitiveOrString(
						propClass,
						propElem.getTextContent()
					)
				);
			}
			
			items[i] = item;
		}
		
		// Nodes
		Element nodesRoot = (Element) root.getElementsByTagName("nodes").item(0);
		
		int nodeCount = Integer.parseInt(nodesRoot.getAttribute("nodeCount"));
		Node[] nodes = new Node[nodeCount];
		NodeList nodesElements = nodesRoot.getElementsByTagName("node");
		
		// First loop pass: Add all nodes to the nodes[] array
		for(int i = 0; i < nodeCount; i++){
			Element ne = (Element) nodesElements.item(i);
			
			int hid = Integer.parseInt(ne.getAttribute("hid"));
			String className = ne.getAttribute("type");
			
			if(hid >= nodes.length || hid < 0){
				continue; // Pass this node...
			}
			
			try {
				Class<?> nodeClass = Class.forName(className);
				
				if(Node.class.isAssignableFrom(nodeClass)){
					Node n = (Node) nodeClass.newInstance();
					nodes[hid] = n;
				}
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		
		// second pass: setups
		for(int i = 0; i < nodeCount; i++){
			Element ne = (Element) nodesElements.item(i);
			
			// Hierarchy ID of this node
			int hid = Integer.parseInt(ne.getAttribute("hid"));
			
			// Get it...
			Node n = nodes[hid];
			
			// And skip if null (shouldn't but never trust external data :p !)
			if(n == null) continue;
			
			// Setup
			n.setX(Float.parseFloat(getChildText(ne, "worldX")));
			n.setY(Float.parseFloat(getChildText(ne, "worldY")));
			n.setOrientation(Integer.parseInt(getChildText(ne, "orientation")));
			
			// Inputs
			Element inputsElem = (Element) ne.getElementsByTagName("inputs").item(0);
			int inputCount = Integer.parseInt(inputsElem.getAttribute("inputCount"));
			n.changeInputCount(inputCount);
			
			// Outputs
			Element outputsElem = (Element) ne.getElementsByTagName("outputs").item(0);
			int outputCount = Integer.parseInt(outputsElem.getAttribute("outputCount"));
			n.changeOutputCount(outputCount);
			
			// Properties
			Element propertiesElem = (Element) ne.getElementsByTagName("properties").item(0);
			NodeList propsDOMNodes = propertiesElem.getElementsByTagName("prop");
			for(int j = 0; j < propsDOMNodes.getLength(); j++){
				Element propElem = (Element) propsDOMNodes.item(j);
				String propClassName = propElem.getAttribute("type");
				
				Class<?> propClass = null;
				
				try {
					propClass = Class.forName(propClassName);
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
				
				if(propClass == null){
					System.out.println("Skip unknown class: "+propClassName);
					continue; // skip if invalid
				}
				
				n.setProperty(
					propElem.getAttribute("name"),
					parsePrimitiveOrString(
						propClass,
						propElem.getTextContent()
					)
				);
			}
		}
		
		// third pass: the wires
		for(int i = 0; i < nodeCount; i++){
			Element ne = (Element) nodesElements.item(i);
			
			// Hierarchy ID of this node
			int hid = Integer.parseInt(ne.getAttribute("hid"));
			
			// Get it...
			Node n = nodes[hid];
			
			// And skip if null (shouldn't but never trust external data :p !)
			if(n == null) continue;
			
			// Outputs destinations setup
			Element outputsElem = (Element) ne.getElementsByTagName("outputs").item(0);
			NodeList outNodes = outputsElem.getElementsByTagName("out");
			int outputCount = Integer.parseInt(outputsElem.getAttribute("outputCount"));
			
			for(int j = 0; j < outputCount; j++){
				Element outElem = (Element) outNodes.item(j);
				
				int outID = Integer.parseInt(outElem.getAttribute("outID"));
				
				if(!n.hasOutput(outID)){
					continue; // never trust
				}
				
				boolean state = Boolean.parseBoolean(outElem.getAttribute("state"));
				n.setOutputValue(outID, state);
				
				NodeList destNodes = outElem.getElementsByTagName("dest");
				for(int k = 0; k < destNodes.getLength(); k++){
					Element destElem = (Element) destNodes.item(k);
					
					int targetHID = Integer.parseInt(destElem.getAttribute("targetHID"));
					int targetInput = Integer.parseInt(destElem.getAttribute("targetInput"));
					
					if(targetHID >= nodeCount || targetHID < 0 || nodes[targetHID] == null){
						continue; // Pass this dest...
					}
					
					Node targetNode = nodes[targetHID];
					if(targetNode.hasInput(targetInput)){
						n.connectTo(targetNode, outID, targetInput);
					}
				}
			}
		}
		
		// Construct the world
		World w = new World();
		
		// Add all nodes
		for(Node n : nodes){
			w.getNodes().add(n);
		}
		
		// Add all items
		w.getInventory().addAllItems(items);
		w.getInventory().setSelectedSlot(selectedSlot);
		
		// Set the viewport
		w.getViewport().setScale(scale);
		w.getViewport().setTranslateX(transX);
		w.getViewport().setTranslateY(transY);
		
		// Returns the world :D
		return w;
	}
	
	// Utils
	private Element createDOMNode(Document dom, String name, Object content){
		Element e = dom.createElement(name);
		
		e.setTextContent(content.toString());
		
		return e;
	}
	
	private String getChildText(Element parent, String tagName){
		return ((Element) parent.getElementsByTagName(tagName).item(0)).getTextContent();
	}
	
	private boolean isPrimitiveBinding(Class<?> cls){
		if(cls.isPrimitive()){
			return true;
		}
		
		Class<?>[] primitiveClasses = {
				Boolean.class,
				Byte.class,
				Short.class,
				Integer.class,
				Long.class,
				Float.class,
				Double.class,
				String.class
		};
		
		for(Class<?> pc : primitiveClasses){
			if(pc.isAssignableFrom(cls)){
				return true;
			}
		}
		
		return false;
	}
	
	private Object parsePrimitiveOrString(Class<?> cls, String str){
		// The primitive Java types: boolean, byte, char, short, int, long, float, and double
		if(isPrimitiveBinding(cls)){
			if(Boolean.class.isAssignableFrom(cls)){
				return Boolean.parseBoolean(str);
			}
			if(Byte.class.isAssignableFrom(cls)){
				return Byte.parseByte(str);
			}
			if(Short.class.isAssignableFrom(cls)){
				return Short.parseShort(str);
			}
			if(Integer.class.isAssignableFrom(cls)){
				return Integer.parseInt(str);
			}
			if(Long.class.isAssignableFrom(cls)){
				return Long.parseLong(str);
			}
			if(Float.class.isAssignableFrom(cls)){
				return Float.parseFloat(str);
			}
			if(Double.class.isAssignableFrom(cls)){
				return Double.parseDouble(str);
			}
		}
		
		return str;
	}
}
