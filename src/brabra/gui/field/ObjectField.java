package brabra.gui.field;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

import java.util.ArrayList;

import brabra.game.physic.Body;
import brabra.game.physic.Collider;
import brabra.game.scene.Movable;
import brabra.game.scene.Object;

/** A field containing an object. */
public class ObjectField extends Field implements Observer {

	public final Object object;
	
	private final Movable asMovable;
	private final Body asBody;
	private final Collider asCollider;
	
	private float oldValidMass;
	
	// for the view
	private final List<Field> fields = new ArrayList<>();
	
	public ObjectField(Object object) {
		super.setName(object.toString());
		
		//--- View:
		Pane fieldHolder = new VBox();
		add(fieldHolder, 1, 1);
		ObservableList<Node> children = fieldHolder.getChildren();
		
	    /*
		
		grid.add(btns[i],0,i);
		
		labels[i] = new Label();
		labels[i].setId("objectName");
		labels[i].setText(obj.toString());
		grid.add(labels[i],1,i);*/
		
	    
	    //--- Fields:
	    // > first Object
		this.object = object;
		// location
		children.add(new VectorField("location rel", object.locationRel()));
		// rotation
		// > if Movable
		asMovable = object.as(Movable.class);
		if (asMovable != null) {
			// velocity (rel)
			children.add(new VectorField("velocity rel", asMovable.velocityRel()));
			// rotVelotity (still always relative)
		}
		// > if Body
		asBody = object.as(Body.class);
		if (asBody != null) {
			// mass
			oldValidMass = asBody.mass();
			children.add(new FloatField("mass", 
					m -> asBody.setMass(m), 
					() -> asBody.mass()));
			// affectedByCollision
			children.add(new BooleanField("affected by col", 
					ac -> asBody.setMass(ac ? this.oldValidMass : -1), 
					() -> asBody.affectedByCollision()));	
		}
		// > if Collider
		asCollider = object.as(Collider.class);
		if (asCollider != null) {
			// display collider
			children.add(new BooleanField("display collider", 
					dc -> asCollider.setDisplayCollider(dc), 
					() -> asCollider.displayCollider()));
		}
	}

	public void update(Observable o, java.lang.Object arg) {
		if (arg == this.object) {
			if (asBody != null && asBody.mass() > 0)
				oldValidMass = asBody.mass();
			// let the fields update themselves
			fields.forEach(f -> f.update(o, arg));
		}
	}
}
