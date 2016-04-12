package brabra.gui.field;

import java.util.Observable;
import java.util.Observer;

import brabra.gui.TriangleButton;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;

/** A common class for all the fields. a field can be closed and display in text his value or open and is editable. */
public abstract class Field extends GridPane implements Observer {

	protected final HBox contentClose = new HBox();
	protected final HBox contentOpen = new HBox();
	protected final Label basicText = new Label();
	
	private final TriangleButton triangleButton = new TriangleButton();
	private boolean open = false;
	
	
	public Field() {
		//super.setPadding(new Insets(0,0,0,4));
		//super.setSpacing(10);
		super.setAlignment(Pos.CENTER_LEFT);
	    
	    //--- View:
		super.setPadding(new Insets(2,0,2,4));
		//contentClose.setSpacing(10);
		//contentOpen.setSpacing(10);
	    add(triangleButton, 0, 0);
	    StackPane firstLine = new StackPane();
	    firstLine.getChildren().addAll(contentOpen, contentClose);
	    add(firstLine, 1, 0);
		    
	    //--- Control:
	    //setOnMouseClicked(e -> { this.setOpen(!open); e.consume(); });
	    
	    setOpen(open);
	}

	public void setName(String name){
		basicText.setText(name+":");
	}

	/** Called when the field should update his value from the model. */
	public abstract void update(Observable o, java.lang.Object arg);

	/** To override to react when the field is shown or hidden. Should be called. */
	public void setOpen(boolean open) {
	    this.open = open;
	    contentClose.setVisible(!open);
	    contentClose.setManaged(!open);
	    contentOpen.setVisible(open);
	    contentOpen.setManaged(open);
	    triangleButton.setOpen(open);
	}
}
