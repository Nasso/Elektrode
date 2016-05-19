package io.github.nasso.elektrode.view;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class LongDialog {
	private Stage stg = null;
	
	private long value = 0;
	
	public LongDialog(){
		Stage stage = new Stage();
		
		stage.setTitle("Enter a value");
		
		HBox root = new HBox();
		Scene sce = new Scene(root);
		
		root.setPadding(new Insets(8));
		root.setAlignment(Pos.CENTER);
		root.setSpacing(8);
		
		TextField field = new TextField();
		field.lengthProperty().addListener(new ChangeListener<Number>(){
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				if(newValue.intValue() > oldValue.intValue()){
					char ch = field.getText().charAt(oldValue.intValue());
					
					if(!(ch >= '0' && ch <= '9' )){
						field.setText(field.getText().substring(0,field.getText().length()-1)); 
					}
				}
			}
		});
		field.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent event) {
				value = Long.valueOf(field.getText());
				stage.hide();
			}
		});
		
		field.setMinWidth(256);
		
		Label l = new Label("Enter a value:");
		l.setFont(Font.font("Arial", 18));
		
		root.getChildren().add(new Label("Enter a value: "));
		root.getChildren().add(field);
		
		stage.setScene(sce);
		stage.sizeToScene();
		
		this.stg = stage;
	}
	
	public long show(){
		stg.showAndWait();
		
		return value;
	}
}
