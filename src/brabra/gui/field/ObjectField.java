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
import brabra.game.scene.Object.Change;

/** A field containing an object. */
public class ObjectField extends Field implements Observer {

	public final Object object;
	
	private final Movable asMovable;
	private final Body asBody;
	private final Collider asCollider;
	
	private float oldValidMass;
	
	public ObjectField(Object object) {
		super(object.toString());
		
	    //--- Fields:
		final List<Field> fields = new ArrayList<>();
		// > first Object
		this.object = object;
		//name
		fields.add(
				new StringField("name",
						nm -> object.setName(nm),
						() -> object.toString())
				.respondingTo(Change.Name)
				);
		// location
		fields.add(
				new VectorField("location rel", object.locationRel())
				.respondingTo(Change.Location)
				);
		// rotation
		fields.add(new QuaternionField("quaternion",object.rotation()).respondingTo(Change.Rotation));
		// > if Movable
		asMovable = object.as(Movable.class);
		if (asMovable != null) {
			// velocity (rel)
			fields.add(
					new VectorField("velocity rel", asMovable.velocityRel())
					.respondingTo(Change.Velocity)
					);
			// rotVelotity (still always relative)
			fields.add(new QuaternionField("quaternion",asMovable.rotationRelVel()).respondingTo(Change.RotVelocity));
		}
		// > if Body
		asBody = object.as(Body.class);
		if (asBody != null) {
			// mass
			oldValidMass = Master.min(asBody.mass(), 1);
			fields.add(
					new FloatField("mass", 
							m -> asBody.setMass(m), 
							() -> asBody.mass())
					.respondingTo(Change.Mass)
					);
			// affectedByCollision (mass)
			fields.add(
					new BooleanField("affected by col", 
							ac -> {
								if (ac != validMassForPhysic(asBody.mass())) { //if change, could be removed if model is well updated
									final float newMass = ac ? this.oldValidMass : -1;
									asBody.setMass(newMass);
									if (validMassForPhysic(newMass))
										oldValidMass = newMass;
								}
							}, 
							() -> asBody.affectedByCollision())
					.respondingTo(Change.Mass)
					);
		}
		// > if Collider
		asCollider = object.as(Collider.class);
		if (asCollider != null) {
			// display collider
			fields.add(
					new BooleanField("display collider", 
							dc -> asCollider.setDisplayCollider(dc), 
							() -> asCollider.displayCollider())
					.respondingTo(Change.DisplayCollider)
					);
		}

		//--- View:
		subfieldHolder.getChildren().addAll(fields);
		
		//--- Control:
		object.addObserver(this);
		fields.forEach(f -> object.addObserver(f));
	}

	public void update(Observable o, java.lang.Object arg) {
		if (arg == Change.Name) 
			super.setName(object.toString());
	}
	
	private boolean validMassForPhysic(float mass) {
		return mass > 0 && mass < Float.POSITIVE_INFINITY;
	}
}
