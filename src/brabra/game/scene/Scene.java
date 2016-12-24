package brabra.game.scene;

import static org.ode4j.ode.OdeConstants.dContactApprox1;
import static org.ode4j.ode.OdeConstants.dContactSlip1;
import static org.ode4j.ode.OdeConstants.dContactSlip2;
import static org.ode4j.ode.OdeConstants.dContactSoftCFM;
import static org.ode4j.ode.OdeConstants.dContactSoftERP;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.function.Consumer;

import org.ode4j.ode.DBody;
import org.ode4j.ode.DContact;
import org.ode4j.ode.DContactBuffer;
import org.ode4j.ode.DGeom;
import org.ode4j.ode.DJoint;
import org.ode4j.ode.DJointGroup;
import org.ode4j.ode.DSpace;
import org.ode4j.ode.DWorld;
import org.ode4j.ode.OdeHelper;
import org.ode4j.ode.DGeom.DNearCallback;

import brabra.Brabra;
import brabra.Debug;
import brabra.game.RealGame;
import brabra.game.physic.Body;
import brabra.game.physic.geo.Box;
import brabra.game.physic.geo.Plane;
import brabra.game.physic.geo.Sphere;
import brabra.game.physic.geo.Vector;
import brabra.game.scene.fun.Starship;
import brabra.game.scene.weapons.MissileLauncher;
import brabra.game.scene.weapons.Target;
import brabra.game.scene.weapons.Weaponry;

/** Object representing the active working scene (model). **/
public class Scene {

	public final List<Object> objects = new ArrayList<>();
	
	public DWorld world = OdeHelper.createWorld();
	public DSpace space = OdeHelper.createSimpleSpace();
	private DJointGroup contactgroup = OdeHelper.createJointGroup();
	
	public static final SceneLoader loader = new SceneLoader();
	public static final SceneProviderLocal providerLocal = new SceneProviderLocal();
	public static final SceneProviderDistant providerDistant = new SceneProviderDistant(() -> Brabra.app.para.serverAdress());
	
	public final Model model;
	
	private final RealGame game;
	
	public Scene(RealGame game) {
		this.game = game;
		this.model = game.sceneModel;
	}
	
	//--- Modifiers ---

	public void forEachObjects(Consumer<Object> f) {
		objects.forEach(f);
	}

	/** Add an object to the scene. */
	public void add(Object o) {
		assert (!objects.contains(o));
		Brabra.app.runLater(() -> {
			o.addToScene(world, space);
			objects.add(o);
			model.notifyChange(Model.Change.ObjectAdded, o);
		});
	}
	
	/** Remove an object from the scene. */
	public void remove(Object o) {
		assert (objects.contains(o));
		Brabra.app.runLater(() -> {
			objects.remove(o);
			o.onDelete();
			model.notifyChange(Model.Change.ObjectRemoved, o);
		});
	}
	
	/** Remove all object from the scene. */
	public void clear() {
		objects.forEach(o -> remove(o));
		world.destroy();
		contactgroup.clear();
		world = OdeHelper.createWorld();
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
				for (int i=0; i<n; i++) {
					DContact contact = contacts.get(i);
					contact.surface.mode = dContactSlip1 | dContactSlip2 | dContactSoftERP | dContactSoftCFM | dContactApprox1;
					contact.surface.mu = 0.5;
					contact.surface.slip1 = 0.0;
					contact.surface.slip2 = 0.0;
					contact.surface.soft_erp = 0.8;
					contact.surface.soft_cfm = 0.01;
					DJoint c = OdeHelper.createContactJoint(world,contactgroup,contact);
					c.attach (o1.getBody(), o2.getBody());
				}
			}
		}
	};
	
	/** Update the colliders and effects (parents first (automatic)). */
	public void updateAll() {
		space.collide(null, nearCallback);
		world.step (1);
		contactgroup.empty();
		Debug.setCurrentWork("objects update");
		for (Object o : objects)
			if (!o.hasParent())
				o.update();
	}

	/** Display all colliders and effects in the scene. */
	public void displayAll() {
		Debug.setCurrentWork("display objects");
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
}
