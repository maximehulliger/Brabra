package brabra.gui;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import brabra.Brabra;
import brabra.gui.view.CreationView;
import brabra.gui.view.MyScenesView;
import brabra.gui.view.ParametersView;
import brabra.gui.view.SceneView;
import brabra.gui.view.StoreView;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.control.Tooltip;
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
	private static final String[] Tooltip = {"Scene", "Para", "Create","MyScene","Store"};
	
	private Brabra app;
	private Stage stage;
	private Scene scene;
	private boolean closing = false;
	private boolean visible = false;
	private static FeedbackPopup feedbackPopup = new FeedbackPopup();
	private static final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
	
	/** Launch the JavaFX app and run it. */
	public static void launch() {
		Application.launch();
	}
	
	public ToolWindow() {
	}
	
	/** Called to start the JavaFX application. */
    public void start(Stage stage) {
		readyLock.lock();
		this.stage = stage;
		this.app = Brabra.app;
    	Brabra.app.fxApp = this;	// let the pro thread go
    	
    	//--- View: 
    	
    	// processing dependent stuff
    	scene = new Scene(initRoot(), width, Brabra.height);
    	updateStageLoc();
    	
    	// init the scene/show (but doesn't show it)
    	stage.setTitle(name);
    	stage.setScene(scene);
        scene.getStylesheets().add("resource/gui/gui.css");
    	
    	//--- Control:
    	
    	// to keep both windows shown (at least tool -> game)
    	stage.addEventHandler(WindowEvent.WINDOW_SHOWN, e -> app.setVisible(true));
    	// to exit main app when  the tool window is closed
    	//TODO catch the close with cross event
    	stage.setOnCloseRequest(e -> {
    		e.consume();
    		this.closing = true; 
    		app.runLater(() -> app.exit());
    	});
    	
    	// general keyboard events: alt-f4 to clean exit, h to hide/show.
    	stage.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
    		if(e.getCode() == KeyCode.F4 && e.isAltDown()) {
        		e.consume();
        		this.closing = true; 
        		app.runLater(() -> { app.exit(); });
    		} else if (e.getCode() == KeyCode.H) {
    			app.setToolWindow(!visible);
    		}
    	});
    	
    	readyLock.unlock();
		
        // intitialized:
    	app.debug.info(3, "tool window ready");
    	stage.show();
    }
    
    /** 
     * Display a message in the ToolWindow window. <p>
     * 	ok: if true display the msg in green, or in red to announce an error. <p>
     * 	time: the time in second during which the msg should be displayed. 
     **/
    public static void displayMessage(String msg, boolean ok, float time) {
    	final Label label = new Label(msg);
    	label.getStyleClass().add(ok ? "popup-ok" : "popup-err");
    	feedbackPopup.addContent(label);
    	
    	ToolWindow.runLater(() -> feedbackPopup.removeContent(label), time);
    }
    
    /** The default time in seconds during which a msg should be displayed. */
    private static final float defaultMsgTime = 2f;
    
    /** 
     * Display a message in the ToolWindow window. <p>
     * 	ok: if true display the msg in green, or in red to announce an error. <p>
     * 	time: the time in second to display the msg. 
     **/
    public static void displayMessage(String msg, boolean ok) {
    	displayMessage(msg, ok, defaultMsgTime);
    }

    /** Init the javaFX components (MVC). Return the root. */
    private Pane initRoot() {
    	StackPane root = new StackPane();
    	
    	// > The Tabs
    	final String[] tabNames = new String[] {"Scene", "Para", "Create","MyScene","Store"};
    	final TabPane tabsHolder = new TabPane();
    	final Tab[] tabs = new Tab[tabNames.length];
    	for (int i=0; i<tabNames.length; i++) {
        	tabs[i] = new Tab();
    		tabs[i].setTooltip(new Tooltip(Tooltip[i]));
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
        displayMessage("2sec", true, 2);
        displayMessage("5sec", false, 5);
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
    public static void runLater(Runnable f) {
    	if (Brabra.app.fxApp != null && !Brabra.app.fxApp.closing)
    		Platform.runLater(f);
    }

    /** Ask for something to run on the JavaFX Application Thread in at least time seconds. */
    public static void runLater(Runnable f, float time) {
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