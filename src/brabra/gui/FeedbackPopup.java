package brabra.gui;

import brabra.Brabra;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class FeedbackPopup extends VBox {
	
	public ToolWindow fxApp;
	
	public FeedbackPopup (ToolWindow fxApp) {
		this.fxApp = fxApp;
		setMaxHeight(Brabra.height/3f);
		setMaxWidth(ToolWindow.width*3f/4);
		setAlignment(Pos.BOTTOM_CENTER);
		getStyleClass().add("popup-box");
		setPopupVisible(false);
	}

    /** The default time in seconds during which a msg should be displayed. */
    private static final float defaultMsgTime = 2f;
    
	/** 
     * Display a message in the ToolWindow window. <p>
     * 	ok: if true display the msg in green, or in red to announce an error. <p>
     * 	time: the time in second during which the msg should be displayed. 
     **/
    public void displayMessage(String msg, boolean ok, float time) {
    	fxApp.runLater(() -> {
	    	final Label label = new Label(msg);
	    	label.getStyleClass().add(ok ? "popup-ok" : "popup-err");
	    	this.addContent(label);
	    	fxApp.runLater(() -> this.removeContent(label), time);
    	});
    }
    
    /** 
     * Display a message in the ToolWindow window. <p>
     * 	ok: if true display the msg in green, or in red to announce an error. 
     **/
    public void displayMessage(String msg, boolean ok) {
    	displayMessage(msg, ok, defaultMsgTime);
    }
    
    // --- Private ---

	private void addContent(Label n) {
		if (getChildren().size() == 0)
			setPopupVisible(true);
		getChildren().add(n);
	}
	
	private void removeContent(Label n) {
		n.setVisible(false);
		getChildren().remove(n);
		if (getChildren().size() == 0)
			setPopupVisible(false);
	}
	
	private void setPopupVisible(boolean visible) {
		setVisible(visible);
		setManaged(visible);
	}
}
