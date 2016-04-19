package brabra.gui.field;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

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
		super(object.toString(), true);
		
	    //--- Fields:
		
		final ArrayList<Field.Pro> fieldsList = new ArrayList<>(16);
		
		// > first Object
		this.object = object;
		//name
		fieldsList.add(
				new StringField.Pro("name",
						nm -> object.setName(nm),
						() -> object.toString(),
						true)
				.respondingTo(Change.Name)
				);
		// location
		fieldsList.add(new VectorField.Pro("location", object.locationRel(), true).respondingTo(Change.Location));
		// rotation
		fieldsList.add(new QuaternionField.Pro("rotation",object.rotation()).respondingTo(Change.Rotation));
		
		// > if Movable
		asMovable = object.as(Movable.class);
		if (asMovable != null) {
			// velocity (rel)
			fieldsList.add(new VectorField.Pro("velocity", asMovable.velocityRel(), true).respondingTo(Change.Velocity));
			// rotVelotity (still always relative)
			fieldsList.add(new QuaternionField.Pro("rot vel",asMovable.rotationRelVel()).respondingTo(Change.RotVelocity));
		}
		
		// > if Body
		asBody = object.as(Body.class);
		if (asBody != null) {
			// mass
			oldValidMass = Master.min(asBody.mass(), 1);
			fieldsList.add(
					new FloatField.Pro("mass", 
							m -> asBody.setMass(m), 
							() -> asBody.mass(),
							true)
					.respondingTo(Change.Mass)
					);
			// affectedByCollision (mass)
			fieldsList.add(
					new BooleanField.Pro("affected by col", 
							ac -> {
								final float oldMass = asBody.mass();
								if (ac != validMassForPhysic(oldMass)) { //if change, could be removed if model is well updated
									if (validMassForPhysic(oldMass))
										oldValidMass = oldMass;
									final float newMass = ac ? oldValidMass : -1;
									asBody.setMass(newMass);
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
			fieldsList.add(
					new BooleanField.Pro("display collider", 
							dc -> asCollider.setDisplayCollider(dc), 
							() -> asCollider.displayCollider())
					.respondingTo(Change.DisplayCollider)
					);
		}
		
		// TODO: add fields for Box, Sphere & Plan.
		
		// get them back as Fields
		List<Field> fields = new ArrayList<>();
		fieldsList.forEach(fPro -> fields.add((Field)fPro));
		
		//--- View:
		subfieldHolder.getChildren().addAll(fields);
		
		//--- Control:
		object.addObserver(this);
		object.addObservers(fields);
	}

	public void update(Observable o, java.lang.Object arg) {
		if (isVisible()) {
			if (arg == Change.Name)
				super.setName(object.toString());
		}
	}
	
	private boolean validMassForPhysic(float mass) {
		return mass > 0 && mass < Float.POSITIVE_INFINITY;
	}
}
