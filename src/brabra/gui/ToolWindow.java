package brabra.gui;

import brabra.Brabra;
import brabra.gui.controller.ParametersViewController;
import brabra.gui.model.AppModel;
import brabra.gui.model.SceneModel;
import brabra.gui.view.ParametersView;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/** Class responsible for the JavaFX thread. */
public class ToolWindow extends Application {
	public static final String name = "Tool Window";
	public static final int width = 300;
	/** Reference to the main app. */
	private static Brabra app;
	private static Scene scene;
	
	private Stage stage;
	private boolean visible = false;
	
	/** Launch the JavaFX app and run it. */
	public static void run(Brabra app) {
		ToolWindow.app = app;
		launch(new String[] { name });
	}
	
	/** Called to start the JavaFX application. */
    public void start(Stage stage) {
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
    	GridPane root = new GridPane();
    	initWindow(root);
    	scene = new Scene(root, width, Brabra.height);
        scene.getStylesheets().add("brabra/gui/gui.css");
        stage.setScene(scene);
    	// show
        app.debug.info(3, "tool window ready");
        stage.show();
    }
    
    /** Init the javaFX components (MVC). */
    private void initWindow(GridPane root) {
    	// first the models
    	AppModel appModel = new AppModel(app);
    	SceneModel sceneModel = new SceneModel(app.game().scene);
    	
    	// then per view
    	// > Parameters view
    	ParametersView pv = new ParametersView(root, appModel, sceneModel);
    	new ParametersViewController(pv, appModel);
    	// > ...
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