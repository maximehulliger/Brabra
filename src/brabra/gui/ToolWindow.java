package brabra.gui;

import brabra.Brabra;
import brabra.gui.controller.ParametersViewController;
import brabra.gui.model.AppModel;
import brabra.gui.view.ParametersView;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/** Class responsible for the JavaFX thread. */
public class ToolWindow extends Application {
	public static final String name = "Tool Window";
	public static final int width = 300;
	/** Reference to the main app. */
	private static Brabra app;
	
	private Stage stage;
	private boolean visible = false;
	
	/** Launch the JavaFX app and run it. */
	public static void run(Brabra app) {
		ToolWindow.app = app;
		launch(new String[] { name });
	}
	
	/** Called to start the JavaFX application. */
    public void start(Stage stage) {
    	stage.setTitle("ToolSelection");
    	app.fxApp = this;
    	this.stage = stage;
    	// to keep both windows shown (at least tool -> game)
    	stage.addEventHandler(WindowEvent.WINDOW_SHOWN, e -> app.setVisible(true));
    	// to exit main app when  the tool window is closed
    	stage.setOnCloseRequest(e -> app.exit());
    	// general keyboard events: alt-f4 to clean exit, h to hide/show.
    	stage.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
    		if(e.getCode() == KeyCode.F4 && e.isAltDown()) {
    			e.consume();
    			app.exit();
    		} else if (e.getCode() == KeyCode.H) {
    			app.setToolWindow(!visible);
    		}
    	});
    	updateStageLoc();
    	// init the scene
    	
        stage.setScene(new Scene(initRoot(), width, Brabra.height));
    	// show
        app.debug.info(3, "tool window ready");
        stage.show();
    }
    
    /** Init the javaFX components (MVC). 
     * @return */
    private Pane initRoot() {
    	StackPane root = new StackPane();
    	TabPane tabs = new TabPane();
    	root.getChildren().add(tabs);

    	Tab tabB = new Tab();
    	tabB.setText("objectState");
    	//Add something in Tab
    	StackPane tabB_stack = new StackPane();
    	tabB_stack.setAlignment(Pos.CENTER);
    	tabB_stack.getChildren().add(new Label("Label@Tab B"));
    	tabB.setContent(tabB_stack);
    	tabs.getTabs().add(tabB);

    	Tab tabA = new Tab();
    	tabA.setText("Parameters");
    	//Add something in Tab
    	StackPane tabA_stack = new StackPane();
    	tabA_stack.setAlignment(Pos.CENTER);
    	tabA_stack.getChildren().add(new Label("Label@Tab B")); 
    	tabA.setContent(tabA_stack);
    	tabs.getTabs().add(tabA);



    	// first the models
    	AppModel appModel = new AppModel(app);
    	//SceneModel sceneModel = new SceneModel(app.game().scene);
    	
    	// then per view
    	// > Parameters view
    	ParametersView pv = new ParametersView(tabA_stack, appModel);
    	new ParametersViewController(pv, appModel);
    	// > ...
    	
    	
    	return root;
    }
    
    // --- Window with Processing managment ---

    public boolean isVisible() {
    	return visible;
    }

    /** Ask for something to run on the JavaFX Application Thread. */
    public void run(Runnable f) {
    	Platform.runLater(f);
    }
    
    /** To set the window visible or invisible (iconified). */
    public void setVisible(boolean visible) {
    	if (this.visible != visible) {
    		this.visible = visible;
        	run(() -> {updateStageLoc(); stage.setIconified(!visible);} );
    	}
	}
    
    /** Set the window location according to the main Processing window. */
    private void updateStageLoc() {
    	if (visible) {
			stage.setX(app.windowLoc.x - ToolWindow.width - 18); // for window borders.
			stage.setY(app.windowLoc.y);
    	}
	}
}