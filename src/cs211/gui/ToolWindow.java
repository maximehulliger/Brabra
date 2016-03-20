package cs211.gui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import cs211.tangiblegame.TangibleGame;

public class ToolWindow extends Application {
	public static final String name = "Tool Box";
	public static final int width = 300;
	
	public static TangibleGame app;
	
	public Stage stage;
	private boolean visible = false;
	
	/** Launch the JavaFX app and run it. */
	public static void run(TangibleGame app) {
		ToolWindow.app = app;
		launch(new String[] { name });
	}
	
	/** Start the fxApp without showing the window. */
    public void start(Stage stage) {
    	app.fxApp = this;
    	this.stage = stage;
    	
    	//to keep both windows shown (at least tool -> game)
    	stage.addEventHandler(WindowEvent.WINDOW_SHOWN, e -> app.setVisible(true));
    	
    	StackPane root = new StackPane();
    	Button btn = new Button();
        btn.setText("Say 'Hello World'");
        btn.setOnAction(e -> app.debug.msg(0, "Hello World !", "tool window"));
        stage.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
              if(e.getCode() == KeyCode.F4 && e.isAltDown()) {
                  e.consume();
                  app.exit();
              } else if (e.getCode() == KeyCode.H) {
            	  app.setToolWindow(!visible);
              }
        });
        root.getChildren().add(btn);
        
        setStageLoc();
    	stage.setScene(new Scene(root, width, TangibleGame.height));
        stage.setOnCloseRequest(e -> app.exit());
        
        app.debug.info(3, "tool window ready");
        stage.show();
    }
    
    public void setVisible(boolean visible) {
    	if (this.visible != visible) {
    		this.visible = visible;
        	run(() -> {setStageLoc(); stage.setIconified(!visible);} );
    	}
	}
    
    public boolean isVisible() {
    	return visible;
    }
    
    public void stop() {
    }
    
    /** Ask for something to run on the JavaFX Application Thread. */
    public void run(Runnable f) {
    	Platform.runLater(f);
    }
    
    private void setStageLoc() {
    	if (visible) {
			stage.setX(app.windowLoc.x - ToolWindow.width - 18); // for window borders.
			stage.setY(app.windowLoc.y);
    	}
	}
}