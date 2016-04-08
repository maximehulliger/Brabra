package brabra.gui.view;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;

public class CreateView {

	public CreateView(Pane root) {
		
		GridPane gridpane = new GridPane();
        gridpane.setPadding(new Insets(5));
        gridpane.setHgap(10);
        gridpane.setVgap(10);
        
        Button Returnbtn = new Button("Return");
        Returnbtn.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
            	//setDisplayTextAreaContent(brabra.gui.ToolWindow.tabs[3]);
            }
        });
        
        Returnbtn.setPrefWidth(100);
        Returnbtn.setPrefHeight(100);
        
        gridpane.add(Returnbtn, 0, 1);
        
        Button Createbtn = new Button("Create");
        Createbtn.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
            	//tabPane.getTabs().remove( aboutTab );
            }
        });
        
        Createbtn.setPrefWidth(100);
        Createbtn.setPrefHeight(100);
        
        gridpane.add(Createbtn, 1, 1);
        
        Button Placebtn = new Button("Place");
        Placebtn.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
            	//tabPane.getTabs().remove( aboutTab );
            }
        });
        
        Placebtn.setPrefWidth(100);
        Placebtn.setPrefHeight(100);
        
        gridpane.add(Placebtn, 2, 1);
        
        //create triangle
        //gridpane.add(Triangle, 0, 2);
        
        root.getChildren().add(gridpane);
		
	}
	
}
