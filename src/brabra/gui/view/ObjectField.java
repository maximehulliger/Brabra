package brabra.gui.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

import java.util.ArrayList;

import brabra.game.physic.Body;
import brabra.game.scene.Movable;
import brabra.game.scene.Object;
import brabra.gui.TriangleButton;

/** A field containing an object. */
public class ObjectField extends Observable implements Observer {

	private final Object object;
	protected final Label textClosed;
	private final TriangleButton triangleButton;
	private final List<Field> fields = new ArrayList<>();
	protected final HBox content;				//current status of object shown when Field is closed
	protected final VBox hidedcontent;			//changeable status of object shown when Field is open
	
	public ObjectField(GridPane root, Object object) {
		this.object = object;
	    
		content = new HBox();
		content.setSpacing(10);
		content.setId("objectName");
		content.setAlignment(Pos.CENTER_LEFT);
		content.setPadding(new Insets(0,0,0,4));
		
		hidedcontent = new VBox();
		hidedcontent.setSpacing(10);
		hidedcontent.setId("objectName");
	    
	    textClosed = new Label();
	    textClosed.setMaxWidth(100);
	    triangleButton = new TriangleButton();  
	    content.getChildren().addAll(triangleButton, textClosed, hidedcontent);
	    hidedcontent.setVisible(false);
	    hidedcontent.setManaged(false);
		
		//TODO: create all the sub fields and keep them hidden (while closed)
		
		//> object fields: location & rotation (relative, always)
		
		if (object instanceof Movable) {
			// velocity & rotVelotity (still always relative)
		}
		
		Body asBody = object.as(Body.class);
		if (asBody != null) {
			
			// mass, affectedByCollision, 
			new FloatField(root, f -> {}, () -> {return asBody.mass();}, "mass");
			
		}
		
	}

	/** Not called for ObjectField as subfields do their work. */
	public void onChange() {}

	@Override
	public void update(Observable o, java.lang.Object arg) {
		if (arg == this.object){
			for (Field f : fields){
			f.update();
			}
		}
	}
}
