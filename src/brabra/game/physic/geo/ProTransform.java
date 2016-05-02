package brabra.game.physic.geo;

import processing.core.PApplet;

public class ProTransform {
	
	public static PApplet app;

	// --- Transformations (location, rotation) ---

	/** Return the absolute location of rel (translated & rotated, from reseted matrix)*/
	public static synchronized Vector absolute(Vector rel, Vector trans, Quaternion rotation) {
		boolean rotNull = rotation.equals(Quaternion.identity);
		boolean transNull = trans.equals(Vector.zero);
		if (rotNull && transNull)
			return rel;
		else if (rotNull)
			return rel.plus(trans);
		else {
			app.pushMatrix();
			app.resetMatrix();
			if (!transNull)
				translate(trans);
			if (!rotNull)
				rotateBy(rotation);
			Vector ret = model(rel);
			app.popMatrix();
			return ret;
		}
	}

	public static synchronized void translate(Vector t) {
		app.translate(t.x, t.y, t.z);
	}

	public static void rotateBy(Quaternion rotation) {
		rotateBy(rotation.rotAxis(), rotation.angle());
	}

	public static synchronized void rotateBy(Vector rotAxis, float angle) {
		if (rotAxis == null)
			return;
		app.rotate(angle, rotAxis.x, rotAxis.y, rotAxis.z);
	}

	public static synchronized Vector screenPos(Vector pos3D) {
		return new Vector( app.screenX(pos3D.x, pos3D.y, pos3D.z), app.screenY(pos3D.x, pos3D.y, pos3D.z) );
	}

	public static Vector relative(Vector abs, Vector trans, Quaternion rotation) {
		return absolute( abs.minus(trans), Vector.zero, rotation.withOppositeAngle());
	}

	public static Vector[] absolute(Vector[] v, Vector trans, Quaternion rotation) {
		Vector[] ret = new Vector[v.length];
		for (int i=0; i<v.length; i++)
			ret[i] = absolute(v[i], trans, rotation);
		return ret;
	}

	public static synchronized Vector model(Vector rel) {
		return new Vector( app.modelX(rel.x, rel.y, rel.z), app.modelY(rel.x, rel.y, rel.z), app.modelZ(rel.x, rel.y, rel.z) );
	}
}
