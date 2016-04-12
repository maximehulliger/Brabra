package brabra.gui;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import brabra.Brabra;
import brabra.gui.model.AppModel;
import brabra.gui.model.SceneModel;
import brabra.gui.view.ParametersView;
import brabra.gui.view.SceneView;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
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
	public static final int width = 360;
	public static final Lock readyLock = new ReentrantLock();
	
	private Brabra app;
	private Stage stage;
	private Scene scene;
	private boolean visible = false;
	
	/** Launch the JavaFX app and run it. */
	public static void launch() {
		Application.launch();
	}
	
	/** Called to start the JavaFX application. */
    public void start(Stage stage) {
    	readyLock.lock();
    	this.stage = stage;
    	this.app = Brabra.app;
    	Brabra.app.fxApp = this;
    	
    	//--- Control:
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

    	//--- View: 
    	stage.setTitle(name);
    	// init the scene/show (but doesn't show it)
    	scene = new Scene(initRoot(), width, Brabra.height);
        stage.setScene(scene);
        scene.getStylesheets().add("data/gui.css");
        // intitialized:
    	app.debug.info(3, "tool window ready");
    	updateStageLoc();
    	stage.show();
    	
    	readyLock.unlock();
    }

    /** Init the javaFX components (MVC). Return the root. */
    private Pane initRoot() {
    	AppModel appModel = new AppModel(app);
    	SceneModel sceneModel = new SceneModel(app.game.scene);
    	StackPane root = new StackPane();
    	Tab[] tabs = tabs(root, new String[] {"Scene", "Parameters"});
    	
    	tabs[0].setContent(new SceneView(sceneModel));
    	tabs[1].setContent(new ParametersView(appModel));
    	
    	//TODO (@max) add others views & controller.
    	
    	return root;
    }
    
    /** Create the tabs and return an array of the tabs root. */
    private Tab[] tabs(Pane root, String[] names) {
    	TabPane tabsHolder = new TabPane();
    	root.getChildren().add(tabsHolder);
    	Tab[] tabs = new Tab[names.length];
    	for (int i=0; i<names.length; i++) {
        	tabs[i] = new Tab();
        	tabsHolder.getTabs().add(tabs[i]);
        	tabs[i].setText(names[i]);
    	}
    	return tabs;
    }
    
    // --- Window with Processing managment ---

    public boolean isVisible() {
    	return visible;
    }

    /** Ask for something to run on the JavaFX Application Thread. */
    public static void run(Runnable f) {
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
    	//TODO generalize insets for all os (now just windows)
    	final double borderTop = 38;//(scene.getWindow().getWidth() - scene.getWidth() - scene.getX());
    	final double borderRight = 8;//(scene.getWindow().getHeight() - scene.getHeight() - scene.getY());
    	stage.setX(app.stageLoc.x - ToolWindow.width - borderRight*3); // for window borders.
		stage.setY(app.stageLoc.y - borderTop);
	}
    
    
}