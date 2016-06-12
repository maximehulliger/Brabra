package brabra.gui.field;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import brabra.Brabra;
import brabra.game.physic.Body;
import brabra.game.physic.Collider;
import brabra.game.physic.geo.Box;
import brabra.game.physic.geo.Plane;
import brabra.game.physic.geo.Sphere;
import brabra.game.scene.Movable;
import brabra.game.scene.Object;
import brabra.game.scene.Object.Change;
import javafx.scene.control.Tooltip;

/** A field containing an object. */
public class ObjectField extends Field implements Observer {

	public final Object object;

	private final Movable asMovable;
	private final Body asBody;
	private final Collider asCollider;
	private final Box asBox;
	private final Sphere asSphere;
	private final Plane asPlane;

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
				.set("Name", false, true));
		// location abs (not modifiable)
		fields.add(new VectorField.Final(
				object.model.locationAbs)
				.respondingTo(Change.Location)
				.set("Absolute Location", false, false));
		// rotation abs (not modifiable)
		fields.add(new QuaternionField.Pro(
				object.model.rotationAbs)
				.respondingTo(Change.Rotation)
				.set("Absolute Rotation", false, false));
		// location rel
		fields.add(new VectorField.Final(object.locationRel)
				.respondingTo(Change.Location)
				.set("Relative Location", false, true));
		// rotation rel
		fields.add(new QuaternionField.Pro(object.rotationRel)
				.respondingTo(Change.Rotation)
				.set("Rotation rel", false, true));

		// > if Movable
		if ((asMovable = object.as(Movable.class)) != null) {
			// velocity (rel)
			fields.add(new VectorField.Final(asMovable.velocityRel())
					.respondingTo(Change.Velocity)
					.set("Velocity (rel)", false, true));
			// rotVelotity (still always relative)
			fields.add(new QuaternionField.Pro(asMovable.rotationRelVel())
					.respondingTo(Change.RotVelocity)
					.set("Rotational vel (rel)", false, true));
		}

		// > if Body
		if ((asBody = object.as(Body.class)) != null) {
			// mass
			oldValidMass = 100;
			fields.add(new FloatField.Pro(
					m -> asBody.setMass(m),
					() -> asBody.mass())
					.respondingTo(Change.Mass)
					.set("Mass", false, true));
			// affectedByCollision (mass)
			fields.add(new BooleanField.Pro(
					ac -> {
						if (realMassForPhysic(asBody.mass()))
							oldValidMass = asBody.mass();
						asBody.setMass(!ac ? oldValidMass : -1);
					},
					() -> !asBody.affectedByPhysic())
					.respondingTo(Change.Mass)
					.set("Heavy", false, true));
			// ghost (mass)
			fields.add(new BooleanField.Pro( 
					g -> {
						if (realMassForPhysic(asBody.mass()))
							oldValidMass = asBody.mass();
						asBody.setMass(g ? 0 : oldValidMass);
					},
					() -> asBody.ghost())
					.respondingTo(Change.Mass)
					.set("Ghost", false, true));
		}

		// > if Collider
		if ((asCollider = object.as(Collider.class)) != null) {
			// display collider
			fields.add(new BooleanField.Pro(
					dc -> asCollider.setDisplayCollider(dc), 
					() -> asCollider.displayCollider())
					.respondingTo(Change.DisplayCollider)
					.set("Display Collider", false, true));
		}

		// > if Box
		if ((asBox = object.as(Box.class)) != null) {
			// display collider
			fields.add(new VectorField(
					s -> asBox.setSize(s),
					() -> asBox.size())
					.respondingTo(Change.Size)
					.withValueValider(s ->  s.x!=0 && s.y!=0 && s.z!=0)
					.set("Size", false, true));
		}
				
		// > if Sphere
		if ((asSphere = object.as(Sphere.class)) != null) {
			// display collider
			fields.add(new FloatField.Pro(
					s -> asSphere.setRadius(s),
					() -> asSphere.radius())
					.respondingTo(Change.Size)
					.withValueValider(s ->  s>0)
					.set("Radius", false, true));
		}
		
		// > if Plane
		if ((asPlane = object.as(Plane.class)) != null) {
			// display collider
			fields.add(new VectorField(
					s -> asPlane.setSize(s),
					() -> asPlane.size())
					.respondingTo(Change.Size)
					.withValueValider(s ->  s.x!=0 && s.z!=0)
					.set("Size (x,?,z)", false, true));
		}
				
		//--- View:
		nameText.getStyleClass().add("objectField-name");
		subfields().addAll(fields);
		set(object.toString(), !closable, closable);
		setToolTip();
		
		//--- Control:
		object.model.addObserver(this);
		fields.forEach(f -> object.model.addObserver(f));
	}

	public void update(final Observable o, final java.lang.Object arg) {
		if (isVisible()) {
			Brabra.app.fxApp.runLater(() -> {
				if (arg == Change.Name)
					super.setName(object.toString());
				else if (arg == Change.Parent)
					setToolTip();
			});
		}
	}
	
	private void setToolTip() {
		super.nameText.setTooltip(object.hasParent() ? new Tooltip("child of "+object.parent()) : null);
	}

	/** Return true if this mass will result in a manipulable body. */
	private boolean realMassForPhysic(float mass) {
		return mass > 0 && mass < Float.POSITIVE_INFINITY;
	}
}
