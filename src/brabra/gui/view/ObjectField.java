package brabra.gui.view;

import javafx.scene.layout.Pane;

import java.util.List;
import java.util.ArrayList;

import brabra.game.physic.Body;
import brabra.game.scene.Movable;
import brabra.game.scene.Object;

/** A field containing an object. */
public class ObjectField extends Field {

	//private final Object object;
	private final List<Field> fields = new ArrayList<>();
	
	public ObjectField(Pane root, Object object) {
		super(root);
		//this.object = object;
		
		//TODO: create all the sub fields and keep them hidden (while closed)
		
		//> object fields: location & rotation (relative, always)
		
		if (object instanceof Movable) {
			// velocity & rotVelotity (still always relative)
		}
		
		if (object instanceof Body) {
			// mass, affectedByCollision, 
		}
		
	}

	/** Not called for ObjectField as subfields do their work. */
	public void onChange() {}

	public void update() {
		for (Field f : fields)
			f.update();
	}

}
