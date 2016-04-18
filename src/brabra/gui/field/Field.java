package brabra.gui.field;

import java.util.Observable;
import java.util.Observer;

import brabra.gui.TriangleButton;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/** A common class for all the fields. a field can be closed and display in text his value or open and is editable. */
public abstract class Field extends GridPane implements Observer {

	// first line
	private final Label nameText = new Label();
	protected final HBox contentClosed = new HBox(), contentOpen = new HBox();
	
	// subfields / triangle
	protected final VBox subfieldHolder;
	private final TriangleButton triangleButton;
	private boolean open = false;
	private final Label valueLabel = new Label();
	
	/** Create a field with this name. If subfields is false, no triangle or subfields (null) and the field is always open. */
	public Field(String name, boolean withSubfields) {
		this.setName(name);
		
	    //--- View:
		setPadding(new Insets(2,0,2,4));
		setAlignment(Pos.CENTER_LEFT);
		// a horizontal box containing open and closed content (the first line).
		final HBox firstLine = new HBox();
		firstLine.setPadding(new Insets(2,0,2,4));
		firstLine.setAlignment(Pos.CENTER_LEFT);
			
		// the triangle button
		if (withSubfields) {
			this.triangleButton = new TriangleButton();
			this.subfieldHolder = new VBox();
			add(triangleButton, 0, 0);
			add(firstLine, 1, 0);
			add(subfieldHolder, 1, 1);			
		} else {
			this.triangleButton = null;
			this.subfieldHolder = null;
			// just the first line, no subfields
			add(firstLine, 0, 0);
		}
		
		// close or open everything
		this.setOpenForMe(open || !withSubfields);
		
		// link the contents
		contentClosed.getChildren().add(valueLabel);
		firstLine.getChildren().addAll(nameText, contentClosed, contentOpen);
		
		//--- Control:
	    setOnMouseClicked(e -> { 
	    	if (triangleButton != null)
		    	this.setOpen(!this.open); 
		    e.consume();
	    });
	}

	public void setName(String name){
		nameText.setText(name+":");
	}
	
	protected final void setValue(String textValue) {
		valueLabel.setText(textValue);
	}

	/** Called when the field should update his value from the model. */
	public abstract void update(Observable o, java.lang.Object arg);

	/** To override to react when the field is shown or hidden. Should be called. */
	public void setOpen(boolean open) {
		setOpenForMe(open);
	}

	private void setOpenForMe(boolean open) {
	    this.open = open;
	    if (triangleButton != null) {
	    	triangleButton.setOpen(open);
	    	subfieldHolder.setVisible(open);
		    subfieldHolder.setManaged(open);
	    }
	    contentClosed.setVisible(!open);
	    contentClosed.setManaged(!open);
	    contentOpen.setVisible(open);
	    contentOpen.setManaged(open);
	}
}
