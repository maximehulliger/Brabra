package brabra.gui.view;

import brabra.game.physic.geo.Box;
import brabra.game.physic.geo.Plane;
import brabra.game.physic.geo.Sphere;
import brabra.game.scene.Movable;
import brabra.game.scene.Object;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;

public class CreateView extends View {

	GridPane objectButtons = new GridPane(); //first
	GridPane creationControlButtons = new GridPane();	//second
	StackPane secondView = new StackPane();
	
	private <T extends Object> void loadCreateDetails(Class <T> type) {
		// hide current view  
		// create new root´ & add it to root
    	secondView.getChildren().remove(objectButtons);
    	secondView.getChildren().add(creationControlButtons);
		// add button already have
		
		// create object
		Object newObject = null;
		
		// add fields
		
		// object
		
		if (Movable.class.isInstance(newObject)) {
			//add movable fields
		}
		
	}
	
	public CreateView() {
		//System.out.println("path: "+ToolWindow.app.dataPathTo("gui/ball.png"));
		//Image imageDecline = new Image(getClass().getResourceAsStream(ToolWindow.app.dataPathTo("gui/ball.png")));
		//Button button5 = new Button();
		//button5.setGraphic(new ImageView(imageDecline));

        objectButtons.setPadding(new Insets(5));
        objectButtons.setHgap(10);
        objectButtons.setVgap(10);
        creationControlButtons.setPadding(new Insets(5));
        creationControlButtons.setHgap(10);
        creationControlButtons.setVgap(10);
        
        //create first view
        
		ImageView ballimage = new ImageView(new Image("data/gui/ball.png"));
		ballimage.setFitHeight(50);
		ballimage.setFitWidth(50);
        Button sphereBtn = new Button("Ball", ballimage);
        sphereBtn.setContentDisplay(ContentDisplay.TOP);
        sphereBtn.setPrefWidth(150);
        sphereBtn.setPrefHeight(150);
        
        objectButtons.add(sphereBtn, 0, 1);
        
		ImageView boximage = new ImageView(new Image("data/gui/box.png"));
		boximage.setFitHeight(50);
		boximage.setFitWidth(50);
        Button boxBtn = new Button("Box", boximage);
        boxBtn.setContentDisplay(ContentDisplay.TOP);
        boxBtn.setPrefWidth(150);
        boxBtn.setPrefHeight(150);
        
        objectButtons.add(boxBtn, 2, 1);
        
		ImageView planeimage = new ImageView(new Image("data/gui/plane.png"));
		planeimage.setFitHeight(50);
		planeimage.setFitWidth(50);
        Button planeBtn = new Button("Plane", planeimage);
        planeBtn.setContentDisplay(ContentDisplay.TOP);
        planeBtn.setPrefWidth(150);
        planeBtn.setPrefHeight(150);
        
        objectButtons.add(planeBtn, 0, 2);
        
        //Create second view
        
		ImageView returnimage = new ImageView(new Image("data/gui/return.png"));
		returnimage.setFitHeight(50);
		returnimage.setFitWidth(50);
        Button returnBtn = new Button("Return", returnimage);
        returnBtn.setContentDisplay(ContentDisplay.TOP);
        returnBtn.setPrefWidth(150);
        returnBtn.setPrefHeight(150);
        
        creationControlButtons.add(returnBtn, 0, 1);
        
		ImageView createimage = new ImageView(new Image("data/gui/hammer.png"));
		createimage.setFitHeight(50);
		createimage.setFitWidth(50);
        Button createBtn = new Button("Create", createimage);
        createBtn.setContentDisplay(ContentDisplay.TOP);
        createBtn.setPrefWidth(150);
        createBtn.setPrefHeight(150);
       
        creationControlButtons.add(createBtn, 1, 1);
        
		ImageView placeimage = new ImageView(new Image("data/gui/drag.png"));
		placeimage.setFitHeight(50);
		placeimage.setFitWidth(50);
        Button placeBtn = new Button("Place", placeimage);
        placeBtn.setContentDisplay(ContentDisplay.TOP);
        placeBtn.setPrefWidth(150);
        placeBtn.setPrefHeight(150);
        
        creationControlButtons.add(placeBtn, 2, 1);
      
        secondView.getChildren().add(objectButtons);
        // TODO getChildren().add(secondView);  
         
        // --- Controller ---
        
        sphereBtn.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
            	loadCreateDetails(Sphere.class);
            }
        });
        
        boxBtn.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
            	loadCreateDetails(Box.class);
            }
        });
        
        planeBtn.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
            	loadCreateDetails(Plane.class);
            }
        });
        
        returnBtn.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
            	secondView.getChildren().remove(creationControlButtons);
            	secondView.getChildren().add(objectButtons);
            }
        });
        
        createBtn.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
            	//tabPane.getTabs().remove( aboutTab );
            }
        });
        
        placeBtn.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
            	//tabPane.getTabs().remove( aboutTab );
            }
        });
		
	}
	
}
