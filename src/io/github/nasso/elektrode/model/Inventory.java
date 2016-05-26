package io.github.nasso.elektrode.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Inventory {
	private int selectedInventorySlot = 0;
	private List<InventoryItem> items = new ArrayList<InventoryItem>();
	
	public Inventory(InventoryItem... items){
		addAllItems(items);
	}
	
	public Inventory(Collection<InventoryItem> initItems){
		this.items.addAll(initItems);
	}
	
	public int getSelectedSlot() {
		return selectedInventorySlot;
	}
	
	public void setSelectedSlot(int selectedInventorySlot) {
		this.selectedInventorySlot = selectedInventorySlot;

		// And there
		if(selectedInventorySlot >= items.size()){
			selectedInventorySlot = selectedInventorySlot % items.size(); // rest of index/size so just the offset B)
		}else if(selectedInventorySlot < 0){
			selectedInventorySlot = items.size() + (selectedInventorySlot % items.size()); // index/size will be negative, just add to the total-1
		}
	}
	
	public InventoryItem getSelectedItem(){
		return this.getItemFromSelected(0);
	}
	
	public void moveSelectedSlot(int offset){
		selectedInventorySlot += offset;
		
		// Security is here too, because it's the selected slot obviously
		if(selectedInventorySlot >= items.size()){
			selectedInventorySlot = selectedInventorySlot % items.size(); // rest of index/size so just the offset B)
		}else if(selectedInventorySlot < 0){
			selectedInventorySlot = items.size() + (selectedInventorySlot % items.size()); // index/size will be negative, just add to the total-1
		}
	}

	public InventoryItem[] getContent() {
		return items.toArray(new InventoryItem[items.size()]);
	}
	
	public void clear(){
		this.items.clear();
	}
	
	public void setContent(Collection<InventoryItem> inventory) {
		this.items.clear();
		this.items.addAll(inventory);
	}
	
	public void addItem(InventoryItem item){
		this.items.add(item);
	}
	
	public void addAllItems(InventoryItem... items){
		for(InventoryItem i : items){
			addItem(i);
		}
	}
	
	public void addItem(int index, InventoryItem item){
		if(index > this.items.size() || index < 0){
			return;
		}
		
		this.items.add(index, item);
	}
	
	public void removeItem(InventoryItem item){
		this.items.remove(item);
	}
	
	public void removeItem(int index){
		if(index >= this.items.size() || index < 0){
			return;
		}
		
		this.items.remove(index);
	}
	
	public void replaceItem(int index, InventoryItem item){
		if(index >= this.items.size() || index < 0){
			return;
		}
		
		this.items.set(index, item);
	}
	
	public InventoryItem getItem(int index){
		if(items.isEmpty()){
			throw new IllegalStateException("Trying to get the item #"+index+" on an empty inventory");
		}
		
		// Security is in this method
		if(index >= items.size()){
			index = index % items.size(); // rest of index/size so just the offset B)
		}else if(index < 0){
			index = items.size() + (index % items.size()); // index/size will be negative, just add to the total-1
		}
		
		return this.items.get(index);
	}
	
	public InventoryItem getItemFromSelected(int offset){
		return this.getItem(selectedInventorySlot + offset);
	}
}
