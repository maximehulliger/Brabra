package brabra.game.physic.geo;

import org.ode4j.ode.DMass;
import org.ode4j.ode.DSpace;
import org.ode4j.ode.DWorld;
import org.ode4j.ode.OdeHelper;

import brabra.Debug;
import brabra.game.physic.Collider;
import brabra.game.scene.Object;
import brabra.game.scene.SceneLoader.Attributes;

public class Box extends Collider {
	
	/** Total size (local). */
	public final Vector size = new Vector();
	/** Size / 2. */
	private Vector dim;
	/** Create a cube with arretes of lenght dim. */
	public Box(Vector size) {
	    super.setName("Cube");
	    setSize(size);
	}

	public void copy(Object o) {
		super.copy(o);
		Box ob = this.as(Box.class);
		if (ob != null) {
			setSize(ob.size);
		}
	}
	
	// --- Getters ---

	/** Return the size of the box (edges length). */
	public Vector size() {
		return size;
	}

	/** Return half of the size of the box. */
	public Vector dim() {
		return dim;
	}
	
	// --- Setters ---
	
	public void setSize(Vector size) {
	    if (size.x <= 0 || size.y <= 0 || size.z <= 0) {
	    	Debug.err("A box shouldn't have any size component null or smaller than 0: keeping "+this.size+" instead of "+size+".");
	    } else {
		    // set
		    this.size.set(size);
		    this.dim = size.multBy(0.5f);
		    
		    // notify
		    model.notifyChange(Change.Size);
	    }
	}
	
	public void setMass(float mass) {
		super.setMass(mass);
		if (inverseMass > 0 && body != null) {
			DMass m = OdeHelper.createMass();
			m.setBoxTotal(mass, size.x, size.y, size.z);
			super.body.setMass (m);
		}
	}
	
	// --- life cycle ---

	public void display() {
		pushLocal();
		displayInteractionMaybe();
		if (!displayColliderMaybe()) {
			color.fill();
			displayShape();
		}
		popLocal();
	}
	
	public void displayShape() {
		app.box(size.x, size.y, size.z);
	}

	// --- life cycle ---

	public void validate(Attributes atts) {
		super.validate(atts);
		
		final String size = atts.getValue("size");
		if (size != null)
			setSize(vec(size));
	}
	
	@Override
	public void addToScene(DWorld world, DSpace space) {
		super.body = OdeHelper.createBody (world);
		//mass
		if (inverseMass > 0) {
			DMass m = OdeHelper.createMass();
			m.setBoxTotal(mass, size.x, size.y, size.z);
			super.body.setMass (m);
		} else
			body.setKinematic();
		//shape
		super.geom = OdeHelper.createBox(space, size.x, size.y, size.z);
		super.geom.setBody(super.body);
		//location & rotation
		body.setPosition(position.toOde());
		body.setQuaternion(rotation.toOde());
	}
}
