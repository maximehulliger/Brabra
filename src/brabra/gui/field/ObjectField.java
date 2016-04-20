package brabra.gui.field;

import java.util.ArrayList;
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

	public ObjectField(Object object, boolean closable) {
		this.object = object;

		//--- Fields:

		final ArrayList<Field> fields = new ArrayList<>(16);

		// > first Object
		//name
		fields.add(new StringField.Pro(
				nm -> object.setName(nm),
				() -> object.toString()
				).respondingTo(Change.Name)
				.set("Name", false, true, true));
		// location
		fields.add(new VectorField.Pro(object.locationRel())
				.respondingTo(Change.Location)
				.set("Location", false, true, true));
		// rotation
		fields.add(new QuaternionField.Pro(object.rotation())
				.respondingTo(Change.Rotation)
				.set("Rotation", false, true, true));

		// > if Movable
		if ((asMovable = object.as(Movable.class)) != null) {
			// velocity (rel)
			fields.add(new VectorField.Pro(asMovable.velocityRel())
					.respondingTo(Change.Velocity)
					.set("Location", false, true, true));
			// rotVelotity (still always relative)
			fields.add(new QuaternionField.Pro(asMovable.rotationRelVel())
					.respondingTo(Change.RotVelocity)
					.set("Rot Vel", false, true, true));
		}

		// > if Body
		if ((asBody = object.as(Body.class)) != null) {
			// mass
			oldValidMass = 100;
			fields.add(new FloatField.Pro(
					m -> asBody.setMass(m),
					() -> asBody.mass())
					.respondingTo(Change.Mass)
					.set("Mass", false, true, true));
			// affectedByCollision (mass)
			fields.add(new BooleanField.Pro(
					ac -> {
						if (realMassForPhysic(asBody.mass()))
							oldValidMass = asBody.mass();
						asBody.setMass(!ac ? oldValidMass : -1);
					},
					() -> !asBody.affectedByCollision())
					.respondingTo(Change.Mass)
					.set("Heavy", false, true, true));
			// ghost (mass)
			fields.add(new BooleanField.Pro( 
					g -> {
						if (realMassForPhysic(asBody.mass()))
							oldValidMass = asBody.mass();
						asBody.setMass(g ? 0 : oldValidMass);
					},
					() -> asBody.ghost())
					.respondingTo(Change.Mass)
					.set("Ghost", false, true, true));
		}

		// > if Collider
		if ((asCollider = object.as(Collider.class)) != null) {
			// display collider
			fields.add(new BooleanField.Pro(
					dc -> asCollider.setDisplayCollider(dc), 
					() -> asCollider.displayCollider())
					.respondingTo(Change.DisplayCollider)
					.set("Display Collider", false, true, true));
		}

		// TODO: add fields for Box, Sphere & Plan.

		// check that there are all Pro
		fields.forEach(fPro -> {assert(Master.asMaybe(fPro, Field.class)!=null);});

		//--- View:
		subfields().addAll(fields);
		set(object.toString(), !closable, closable, closable);

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

	/** Return true if this mass will result in a manipulable body. */
	private boolean realMassForPhysic(float mass) {
		return mass > 0 && mass < Float.POSITIVE_INFINITY;
	}
}
