package brabra.gui;

import brabra.Brabra;
import brabra.gui.controller.CreateViewController;
import brabra.gui.controller.ParametersViewController;
import brabra.gui.controller.SceneViewController;
import brabra.gui.model.AppModel;
import brabra.gui.model.SceneModel;
import brabra.gui.view.CreateView;
import brabra.gui.view.ParametersView;
import brabra.gui.view.SceneView;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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
	public static Brabra app;
	
	/** Reference to the main app. */
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
    	Scene scene = new Scene(initRoot(), width, Brabra.height);
        scene.getStylesheets().add("data/gui.css");
        stage.setScene(scene);
    	// show
        stage.setScene(scene);
    	stage.show();
    	app.debug.info(3, "tool window ready");
    }

    /** Init the javaFX components (MVC). Return the root. */
    private Pane initRoot() {
    	AppModel appModel = new AppModel(app);
    	SceneModel sceneModel = new SceneModel(app.game().scene);
    	StackPane root = new StackPane();
    	Pane[] tabs = tabs(root, new String[] {"Scene", "Para","+"});
    	
    	SceneView sv = new SceneView(tabs[0], sceneModel);
    	new SceneViewController(sv, sceneModel);
    	ParametersView pv = new ParametersView(tabs[1], appModel);
    	new ParametersViewController(pv, appModel);
    	CreateView cv = new CreateView(tabs[2]); 
    	new CreateViewController(cv);
    	
    	//TODO (@max) add others views & controller.
    	
    	return root;
    }
    
    /** Create the tabs and return an array of the tabs root. */
    private Pane[] tabs(Pane root, String[] names) {
    	//connect new tabs holder with root
    	TabPane tabs = new TabPane();
    	tabs.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
    	root.getChildren().add(tabs);
    	//get result array
    	Pane[] tabsRoot = new Pane[names.length];
    	// we have to add a root in each 
    	for (int i=0; i<names.length; i++) {
        	Tab tab = new Tab();
        	tabsRoot[i] = new StackPane();
        	tabs.getTabs().add(tab);
        	tab.setText(names[i]);
        	//tabsRoot.setAlignment(Pos.CENTER);
        	tab.setContent(tabsRoot[i]);
    	}
    	return tabsRoot;
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