package brabra.gui.view;

import brabra.gui.TriangleButton;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.HBox;

/** A common class for all the fields. a field can be closed and display in text his value or open and is editable. */
public abstract class Field extends HBox{

	protected final Label textClosed;
	private final TriangleButton triangleButton;
	protected final HBox content;				//current status of object shown when Field is closed
	protected final HBox hidedcontent;			//changeable status of object shown when Field is open
	
	protected boolean open = false;
	
	public Field(Pane root) {
	    setSpacing(10);
	    setId("objectName");
	    setAlignment(Pos.CENTER_LEFT);
	    setPadding(new Insets(0,0,0,4));
	    
		content = new HBox();
		content.setSpacing(10);
		content.setId("objectName");
		
		hidedcontent = new HBox();
		hidedcontent.setSpacing(10);
		hidedcontent.setId("objectName");
	    
	    textClosed = new Label();
	    textClosed.setMaxWidth(100);
	    triangleButton = new TriangleButton();  
	    getChildren().addAll(triangleButton, textClosed, content, hidedcontent);
	    hidedcontent.setVisible(false);
	    hidedcontent.setManaged(false);
	    
	    setOpen(false);
		
	    // Control:
	    
	    setOnMouseClicked(
				e ->{
					this.setOpen(!open); {this.onChange();}
				});
	}

	public void setName(String name){
		this.textClosed.setText(name);
	}

	/** Called when the value of the field has changed, to update the model. */
	public abstract void onChange();
	
	/** Called when the field should update his value from the model. */
	public abstract void update();
	
	/** To override to react when the field is shown or hidden. */
	public void setOpen(boolean open) {
		content.setManaged(open);
	    content.setVisible(open);
	    triangleButton.setState(open);
	    hidedcontent.setManaged(!open);
	    hidedcontent.setVisible(!open);
	    this.open = open;
	}
}
