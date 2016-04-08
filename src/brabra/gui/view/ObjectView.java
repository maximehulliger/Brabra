package brabra.gui.view;

import java.util.Observable;
import java.util.Observer;

import brabra.gui.ToolWindow;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;



public class ObjectView {

	public ObjectView(Pane root) {
		
		// TODO (@Maxime) picture loading
		
		//System.out.println("path: "+ToolWindow.app.dataPathTo("gui/ball.png"));
		//Image imageDecline = new Image(getClass().getResourceAsStream(ToolWindow.app.dataPathTo("gui/ball.png")));
		//Button button5 = new Button();
		//button5.setGraphic(new ImageView(imageDecline));
		
		GridPane gridpane = new GridPane();
        gridpane.setPadding(new Insets(5));
        gridpane.setHgap(10);
        gridpane.setVgap(10);
        
        /*Label ALbl = new Label("Left");
        GridPane.setHalignment(ALbl, HPos.CENTER);
        gridpane.add(ALbl, 0, 0);
          
        Label BLbl = new Label("Right");
        gridpane.add(BLbl, 2, 0);
        GridPane.setHalignment(BLbl, HPos.CENTER);
        
        Label CLbl = new Label("Left");
        GridPane.setHalignment(CLbl, HPos.CENTER);
        gridpane.add(CLbl, 0, 1);*/
        
        Button Ballbtn = new Button("Ball");
        Ballbtn.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
            	//setDisplayTextAreaContent(brabra.gui.ToolWindow.tabs[3]);
            }
        });
        
        Ballbtn.setPrefWidth(150);
        Ballbtn.setPrefHeight(150);
        
        gridpane.add(Ballbtn, 0, 1);
        
        Button Boxbtn = new Button("Box");
        Boxbtn.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
            	//tabPane.getTabs().remove( aboutTab );
            }
        });
        
        Boxbtn.setPrefWidth(150);
        Boxbtn.setPrefHeight(150);
        
        gridpane.add(Boxbtn, 2, 1);
        
        Button Planebtn = new Button("Plane");
        Planebtn.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
           
            }
        });
        
        Planebtn.setPrefWidth(150);
        Planebtn.setPrefHeight(150);
        
        gridpane.add(Planebtn, 0, 2);
        
        /*
        VBox vbox = new VBox(5);
        vbox.getChildren().addAll(Ballbtn,Boxbtn,Planebtn);
        
        gridpane.add(vbox, 1, 1);
        GridPane.setConstraints(vbox, 1, 1, 1, 2,HPos.CENTER, VPos.CENTER);
        */
      
        root.getChildren().add(gridpane);        
	}

	//public void update(Observable o, java.lang.Object arg) {
		/*btn.setText("Say '"+((AppModel)o).textToPrint()+"'");*/
}
//}
	