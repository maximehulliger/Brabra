package brabra.gui.field;

import java.util.Observable;
import java.util.Observer;

import brabra.gui.TriangleButton;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/** A common class for all the fields. a field can be closed and display in text his value or open and is editable. */
public abstract class Field extends GridPane implements Observer {

	protected final HBox contentClosed = new HBox(), contentOpen = new HBox();
	protected final VBox subfieldHolder = new VBox();
	private final Label nameText = new Label();
	
	protected final ObservableList<Node> firstLine;
    private final TriangleButton triangleButton = new TriangleButton();
	private boolean open = false;
	
	
	public Field(String name) {
		this.setName(name);
	    this.setOpenForMe(open);
		
	    //--- View:
		setPadding(new Insets(2,0,2,4));
		setAlignment(Pos.CENTER_LEFT);
		// the triangle button
		add(triangleButton, 0, 0);
		// a pane containing open and closed content.
		final HBox firstLine = new HBox();
		this.firstLine = firstLine.getChildren();
		add(firstLine, 1, 0);
		// the contents
		firstLine.setPadding(new Insets(2,0,2,4));
		firstLine.setAlignment(Pos.CENTER_LEFT);
		this.firstLine.addAll(nameText, contentClosed, contentOpen);
		add(subfieldHolder, 1, 1);
	    //--- Control:
	    setOnMouseClicked(e -> { this.setOpen(!this.open); e.consume();});
	}

	public void setName(String name){
		nameText.setText(name+":");
	}

	/** Called when the field should update his value from the model. */
	public abstract void update(Observable o, java.lang.Object arg);

	/** To override to react when the field is shown or hidden. Should be called. */
	public void setOpen(boolean open) {
		setOpenForMe(open);
	}

	private void setOpenForMe(boolean open) {
	    this.open = open;
	    triangleButton.setOpen(open);
//	    nameText.setScaleX(contentOpen.getScaleX());
//	    nameText.setScaleY(contentOpen.getScaleY());
	    contentClosed.setVisible(!open);
	    contentClosed.setManaged(!open);
	    contentOpen.setVisible(open);
	    contentOpen.setManaged(open);
	    subfieldHolder.setVisible(open);
	    subfieldHolder.setManaged(open);
	}
}
