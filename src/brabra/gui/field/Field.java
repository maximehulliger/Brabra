package brabra.gui.field;

import java.util.Observable;
import java.util.Observer;

import brabra.gui.TriangleButton;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/** A common class for all the fields. a field can be closed and display in text his value or open and is editable. */
public abstract class Field extends GridPane implements Observer {

	// first line
	private final Label nameText = new Label(), valueLabel = new Label();
	private final HBox firstLine = new HBox();
	protected final HBox contentClosed = new HBox(), contentOpen = new HBox();
	// subfields / triangle
	private final VBox subfieldHolder = new VBox();
	private final TriangleButton triangleButton = new TriangleButton();
	// current state
	private boolean closable = false, withTriangle = false, open = true;
	private String name = "";

	
	/** Create a field with this name. If subfields is false, no triangle or subfields (null) and the field is always open. */
	public Field() {
		//TODO: in css firstLine.setPadding(new Insets(2,0,2,4));
		//firstLine.setAlignment(Pos.CENTER_LEFT);
		//setPadding(new Insets(2,0,2,4));
		//setAlignment(Pos.CENTER_LEFT);
		
		//--- View:
		
		// > first line: a horizontal box containing open and closed content.
		// the name label
		nameText.getStyleClass().add("parameter-label");
		// the value label
		contentClosed.getChildren().add(valueLabel);
		contentClosed.getStyleClass().add("parameter-value-closed");
		contentOpen.getStyleClass().add("parameter-value-open");
		// open true by default
		contentClosed.setVisible(false);
	    contentClosed.setManaged(false);
		// link the first line
		firstLine.getChildren().addAll(nameText, contentClosed, contentOpen);
		updateGrid();
		
		//--- Control:
		
		// close on click if openable (without triangle buttons is always open)
	    setOnMouseClicked(e -> { 
	    	if (closable)
		    	this.setOpen(!open()); 
	    	e.consume();
	    });
	}
	
	/** Called when the field should update his value from the model. */
	public abstract void update(Observable o, java.lang.Object arg);

	/** To override to react when the field is shown or hidden. Should be called. */
	protected void setOpen(boolean open) {
	    this.open = open;
	    triangleButton.setOpen(open);
	    contentOpen.setVisible(open);
	    contentOpen.setManaged(open);
	    subfieldHolder.setVisible(open);
	    subfieldHolder.setManaged(open);
	    contentClosed.setVisible(!open);
	    contentClosed.setManaged(!open);
	}

	/** Set the name label. if name is null nothing is shown. */
	protected Field setName(String name){
		this.name = name == null ? "" : name;
		nameText.setText(this.name+": ");
		return this;
	}
	
	/** Reset the field with those settings. Return this for convenience. <p>
	 * name: Set the name label. If name is null nothing is shown. <p>
	 * closable: Set if the field should open/close itself on click. <p>
	 * withTriangle: Set if the field should have a triangle to indicate openness. 
	 *  */
	public Field set(String name, boolean open, boolean closable, boolean withTriangle) {
		this.withTriangle = withTriangle;
		this.closable = closable;
		setOpen(open);
		setName(name);
		updateGrid();
		return this;
	}

	public void updateGrid() {
		getChildren().clear();
		// We replace everything
		int contentCol;
		if (withTriangle) {
			add(triangleButton, 0, 0);	
			contentCol = 1;	
		} else {
			contentCol = 0;
		}
		add(firstLine, contentCol, 0);
		add(subfieldHolder, contentCol, 1);	
	}


	public ObservableList<Node> subfields() {
		return subfieldHolder.getChildren();
	}
	
	protected final void setTextValue(String textValue) {
		valueLabel.setText(textValue);
	}

	protected boolean open() {
		return open;
	}
	
	protected String name() {
		return name;
	}

	/** Interface to discriminate the field modeled in the processing thread. */
	public static interface Pro {};
}
