package brabra.gui.field;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

import java.util.ArrayList;

import brabra.Master;
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
	private final UpdateNode updateNode = new UpdateNode();
	
	private float oldValidMass;
	
	public ObjectField(Object object) {
		super(object.toString());
		
	    //--- Fields:
		final List<Field> fields = new ArrayList<>();
		// > first Object
		this.object = object;
		// location
		fields.add(new VectorField("location rel", object.locationRel()));
		// rotation
		// > if Movable
		asMovable = object.as(Movable.class);
		if (asMovable != null) {
			// velocity (rel)
			fields.add(new VectorField("velocity rel", asMovable.velocityRel()));
			// rotVelotity (still always relative)
		}
		// > if Body
		asBody = object.as(Body.class);
		if (asBody != null) {
			// mass
			oldValidMass = Master.min(asBody.mass(), 1);
			fields.add(new FloatField("mass", 
					m -> asBody.setMass(m), 
					() -> asBody.mass()));
			// affectedByCollision
			fields.add(new BooleanField("affected by col", 
					ac -> asBody.setMass(ac ? this.oldValidMass : -1), 
					() -> asBody.affectedByCollision()));	
		}
		// > if Collider
		asCollider = object.as(Collider.class);
		if (asCollider != null) {
			// display collider
			fields.add(new BooleanField("display collider", 
					dc -> asCollider.setDisplayCollider(dc), 
					() -> asCollider.displayCollider()));
		}

		//--- View:
		subfieldHolder.getChildren().addAll(fields);
		
	    /*labels[i] = new Label();
		grid.add(labels[i],1,i);*/
		
		//--- Control:
		fields.forEach(f -> updateNode.addObserver(f));
	}

	public void update(Observable o, java.lang.Object arg) {
		if (arg == this.object) {
			if (asBody != null && asBody.mass() > 0)
				oldValidMass = asBody.mass();
			// let the fields update themselves
			updateNode.update(this);
		}
	}
	
	/** A simple observer that the fields will follow. */
	private class UpdateNode extends Observable {
		public void update(java.lang.Object arg) {
			setChanged();
			notifyObservers(arg);
		}
	}
}
