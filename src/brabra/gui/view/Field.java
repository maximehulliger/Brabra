package brabra.gui.view;

import brabra.game.physic.geo.Vector;
import javafx.scene.layout.Pane;

/** A common class for all the fields. a field can be closed and display in text his value or open and is editable. */
public abstract class Field {

	//private final TriangleButton triangleButton;
	//private final GUIText? textClosed;
	
	private boolean open = false;
	
	public Field(Pane root) {
		
		//TODO: add to the root all the element of the field (triangle & short text at the begining, as closed)
		
	}
	
	/** Called when the value of the field has changed, to update the model. */
	public abstract void onChange();
	
	/** Called when the field should update his value from the model. */
	public abstract void update();
	
	public boolean open() {
		return open;
	}
	
	/** To override to react when the field is shown or hidden. */
	public void setOpen(boolean open) {
		this.open = open;
		
		//TODO: change triangle
		
	}
	
	// --- Some concrete simple fields ---

	public class FloatField {
		
		//private final Float value;
		
		//TODO: think how we'll do that (no float pointer in java)
		
	}

	public class BooleanField {
		
		//private final Boolean value;
		
		//TODO: think how we'll do that (no boolean pointer in java)
		
	}
	
	public class VectorField extends Field {

		private final Vector vector;
		
		//TODO: array of 3 num fields (editable)
		
		public VectorField(Pane root, Vector vector) {
			super(root);
			this.vector = vector;
		}

		public void onChange() {

			//TODO: update vector value
			final float f = Float.parseFloat("-55.2");
			vector.set(f, 0, 0);
			
		}

		public void update() {
			
			//TODO: update gui values from vector
			
		}
		
		public void setOpen(boolean open) {
			super.setOpen(open);
			
			//TODO: show/hide vector fields
			
		}
	}
}
