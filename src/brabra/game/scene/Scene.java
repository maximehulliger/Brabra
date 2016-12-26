package brabra.game.scene;

import static org.ode4j.ode.OdeConstants.dContactApprox1;
import static org.ode4j.ode.OdeConstants.dContactSoftCFM;
import static org.ode4j.ode.OdeConstants.dContactSoftERP;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.function.Consumer;

import org.ode4j.ode.DBody;
import org.ode4j.ode.DContact;
import org.ode4j.ode.DContactBuffer;
import org.ode4j.ode.DGeom;
import org.ode4j.ode.DGeom.DNearCallback;
import org.ode4j.ode.DJoint;
import org.ode4j.ode.DJointGroup;
import org.ode4j.ode.DSpace;
import org.ode4j.ode.DWorld;
import org.ode4j.ode.OdeHelper;

import brabra.Brabra;
import brabra.Parameters;
import brabra.game.RealGame;
import brabra.game.physic.Body;
import brabra.game.physic.geo.Vector;

/** Object representing the active working scene (model). **/
public class Scene implements Observer {

	public final List<Object> objects = new ArrayList<>();
	
	private DWorld world = OdeHelper.createWorld();
	private DSpace space = OdeHelper.createSimpleSpace();
	private DJointGroup contactgroup = OdeHelper.createJointGroup();
	
	public static final SceneLoader loader = new SceneLoader();
	public static final SceneProviderLocal providerLocal = new SceneProviderLocal();
	public static final SceneProviderDistant providerDistant = new SceneProviderDistant(() -> Brabra.app.para.serverAdress());
	
	public final Model model;
	
	public Scene(RealGame game) {
		this.model = game.sceneModel;
		world.setGravity(Brabra.app.para.gravity().toOde());
		Brabra.app.para.addObserver(this);
	}
	
	//--- Modifiers ---

	public void forEachObjects(Consumer<Object> f) {
		objects.forEach(f);
	}

	/** Add an object to the scene. */
	public void add(Object o) {
		assert (!objects.contains(o));
		Brabra.app.runLater(() -> {
			Body b = o.as(Body.class);
			if (b != null)
				b.addToScene(world, space);
			objects.add(o);
			model.notifyChange(Model.Change.ObjectAdded, o);
		});
	}
	
	/** Remove an object from the scene. */
	public void remove(Object o) {
		assert (objects.contains(o));
		Brabra.app.runLater(() -> removeNow(o));
	}
	
	private void removeNow(Object o) {
		assert (objects.contains(o));
		objects.remove(o);
		o.onDelete();
		model.notifyChange(Model.Change.ObjectRemoved, o);
	}
	
	/** Remove all object from the scene. */
	public void clear() {
		while (objects.size() > 0)
			removeNow(objects.get(0));
		world.destroy();
		contactgroup.clear();
		world = OdeHelper.createWorld();
		world.setGravity(Brabra.app.para.gravity().toOde());
	}
	
	// --- life cycle ---

	private DNearCallback nearCallback = new DNearCallback() {
		@Override
		public void call(java.lang.Object data, DGeom o1, DGeom o2) {
			DBody b1 = o1.getBody();
			DBody b2 = o2.getBody();
			if (b1!=null && b2!=null && OdeHelper.areConnected(b1, b2))
				return;

			final int N = 4;
			DContactBuffer contacts = new DContactBuffer(N);
			int n = OdeHelper.collide (o1,o2,N,contacts.getGeomBuffer());//[0].geom,sizeof(dContact));
			if (n > 0) {
				// collision reaction
				Body bb1 = (Body) b1.getData();
				Body bb2 = (Body) b2.getData();
				Vector impact = new Vector(contacts.get(0).getContactGeom().pos);
				if (bb1.isCollidingWith(bb2) && bb2.isCollidingWith(bb1)) {
					bb1.onCollision(bb2, impact);
					bb2.onCollision(bb1, impact);
					
					// create contact joint
					for (int i=0; i<n; i++) {
						DContact contact = contacts.get(i);
						contact.surface.mode = dContactSoftERP | dContactSoftCFM | dContactApprox1;
						contact.surface.mu = 0.5;
						contact.surface.soft_erp = 0.8;
						contact.surface.soft_cfm = 0.01;
						DJoint c = OdeHelper.createContactJoint(world,contactgroup,contact);
						c.attach (o1.getBody(), o2.getBody());
					}
				}
			}
		}
	};
	
	/** Update the colliders and effects (parents first (automatic)). */
	public void updateAll() {
		space.collide(null, nearCallback);
		world.step (1);
		contactgroup.empty();
		for (Object o : objects)
			o.update();
	}

	/** Display all colliders and effects in the scene. */
	public void displayAll() {
		for(Object o : objects)
			o.display();
	}
	
	// --- Observable ---
	
	public static class Model extends Observable {

		public Scene scene = null;
		
		public enum Change { ObjectAdded, ObjectRemoved, SceneFileChanged };
		
		public static class Arg {
			public final Change change;
			public final Object object;
			public Arg(Change change, Object object) {
				this.change = change;
				this.object = object;
			}
		}
		
		protected void notifyChange(Change change, Object o) {
			synchronized (this) {
				this.setChanged();
					this.notifyObservers(new Arg(change, o));
			}
		}
	}

	@Override
	public void update(Observable arg0, java.lang.Object arg1) {
		if (arg1 == Parameters.Change.Gravity)
			world.setGravity(((Parameters)arg0).gravity().toOde());
	}
}
