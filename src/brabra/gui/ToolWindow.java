package brabra.gui;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import brabra.Brabra;
import brabra.Debug;
import brabra.gui.view.CreationView;
import brabra.gui.view.MyScenesView;
import brabra.gui.view.ParametersView;
import brabra.gui.view.SceneView;
import brabra.gui.view.StoreView;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/** Class responsible for the JavaFX thread. */
public class ToolWindow extends Application {
	
	public static final String name = "Tool Window";
	public static final int width = 360;
	public static final Lock launchedLock = new ReentrantLock();
	public static FeedbackPopup feedbackPopup;
	
	private Brabra app;
	private Stage stage;
	private Scene scene;
	private boolean closing = false;
	private boolean visible = false;
	private static final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
	
	/** Launch the JavaFX app and run it. */
	public static void launch() {
		Application.launch();
	}
	
	/** Called to start the JavaFX application. */
    public void start(Stage stage) {
    	this.app = Brabra.app;
		this.app.fxApp = this;
		this.stage = stage;
		
    	//--- View: 
    	
    	// processing dependent stuff
		feedbackPopup = new FeedbackPopup(this);
    	scene = new Scene(initRoot(), width, Brabra.height);
    	updateStageLoc();
    	
    	// init the scene and stage. show it.
    	stage.setTitle(name);
    	scene.getStylesheets().add("resource/gui/gui.css");
    	stage.setScene(scene);
        stage.show();
    	Debug.info(3, "tool window ready");
    	
    	//--- Control:
    	
    	// to exit main app when the tool window is closed by cross or alt-f4 event.
        stage.addEventHandler(
        		WindowEvent.WINDOW_CLOSE_REQUEST, 
        		e -> {
        			e.consume(); 
        			this.closing = true;
        			app.runLater(() -> app.exit());
        		});

    }
    
    /** The name of the tabs (as displayed in the tab holder) */
    private static final String[] tabNames = new String[] {"Scene", "Para", " + ","MyScene","Store"};
	
    private static final String[] tabTooltip = {"Scene", "Para", "Create","MyScene","Store"};
	
    /** Init the javaFX components (MVC). Return the root. */
    private Pane initRoot() {
    	StackPane root = new StackPane();
    	
    	// > The Tabs
    	final TabPane tabsHolder = new TabPane();
    	final Tab[] tabs = new Tab[tabNames.length];
    	for (int i=0; i<tabNames.length; i++) {
        	tabs[i] = new Tab();
    		tabs[i].setTooltip(new Tooltip(tabTooltip[i]));
        	tabsHolder.getTabs().add(tabs[i]);
        	tabs[i].setText(tabNames[i]);
    	}
    	
    	//--- View:
    	
    	tabsHolder.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
    	
    	// add the Views to the tabs.
    	tabs[0].setContent(new SceneView(app.game.scene));
    	tabs[1].setContent(new ParametersView(app.para));
    	tabs[2].setContent(new CreationView());
    	tabs[3].setContent(new MyScenesView(app.game.scene));
    	tabs[4].setContent(new StoreView(app.game.scene));
    	   	
        // link everything
    	root.getChildren().addAll(tabsHolder, feedbackPopup);
    	
    	return root;
    }
    
    // --- Window with Processing managment ---

    public boolean isVisible() {
    	return visible;
    }

    public boolean isClosing() {
    	return closing;
    }

    /** Ask for something to run on the JavaFX Application Thread. */
    public void runLater(Runnable f) {
    	if (Brabra.app.fxApp != null && !Brabra.app.fxApp.closing)
    		Platform.runLater(f);
    }

    /** Ask for something to run on the JavaFX Application Thread in at least time seconds. */
    public void runLater(Runnable f, float time) {
    	runLater(() -> 
    	executor.schedule(
    			() -> runLater(f),
    			(long) (1000*time), 
    			TimeUnit.MILLISECONDS
    			)
    	);
    }
    
    /** To set the window visible or invisible (iconified). */
    public void setVisible(boolean visible) {
    	if (this.visible != visible)
        	runLater(() -> { this.visible = visible; updateStageLoc(); stage.setIconified(!visible); });
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