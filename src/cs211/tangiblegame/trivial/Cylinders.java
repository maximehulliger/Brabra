package cs211.tangiblegame.trivial;

import java.util.ArrayList;

import cs211.tangiblegame.ProMaster;
import processing.core.PApplet;
import processing.core.PShape;
import processing.core.PVector;

public class Cylinders extends ProMaster {
	//-- parametres
	public static final float cylinderRadius = 25;  //radius
	public static final float cylinderHeight = 60;   
	public static final int cylinderResolution = 42;  //# point
	private static final PVector tTer = TrivialGame.tailleTerrain;
	
	//-- interne
	public static TrivialGame trivialGame;
	private PShape cylinder;
	ArrayList<PVector> cylindersPos = new ArrayList<>();

	public void placeCylinder() {
		//nouveau cylindre ! :D
		//on trouve la position sur l'ecran (en pixel) de l'extrêmité du terrain
		PVector pos3D = new PVector(-tTer.x, 0, tTer.z);
		PVector pos2D = new PVector( app.screenX(pos3D.x, pos3D.y, pos3D.z), app.screenY(pos3D.x, pos3D.y, pos3D.z) );
		PVector pos2DCentre = new PVector( pos2D.x - app.width/2, pos2D.y - app.height/2 );
		//on trouve l'échelle du terrain sur l'écran (par rapport au centre de l'écran)
		PVector echelle = new PVector( tTer.x/pos2DCentre.x, tTer.z/pos2DCentre.y );
		PVector newCylinderPos = new PVector( -(app.mouseX - app.width/2)*echelle.x , 0, (app.mouseY - app.height/2)*echelle.y );

		//on l'ajoute uniquement s'il est sur le terrain
		if (standInTerrain2D(newCylinderPos))
			cylindersPos.add(newCylinderPos);
	}

	//over x and z coordonate
	boolean standInTerrain2D(PVector pos) {
		return standIn2D(pos, -tTer.x/2, tTer.x/2, -tTer.z/2, tTer.z/2);
	}
	//over x and z coordonate
	boolean standIn2D(PVector pos, float minX, float maxX, float minZ, float maxZ) {
		return pos.x == PApplet.constrain(pos.x, minX, maxX) && pos.z == PApplet.constrain(pos.z, minZ, maxZ);
	}

	void displayCylinders() {
		for (PVector v : cylindersPos) {
			printCylinder(v);
		}
	}

	void printCylinder(PVector cylinderPos) {
		app.pushMatrix();
		app.translate(cylinderPos.x, cylinderPos.y, cylinderPos.z);
		app.noStroke();
		app.fill(255, 255, 0);
		app.shape(cylinder);
		app.popMatrix();
	}

	void initCylinder() {
		//les points du cerlce du cylindre
		float angle;
		float[] x = new float[cylinderResolution + 1];
		float[] z = new float[cylinderResolution + 1];
		//get the x and y position on a circle for all the sides
		for(int i = 0; i < x.length; i++) {
			angle = (PApplet.TWO_PI / cylinderResolution) * i;
			x[i] = PApplet.sin(angle) * cylinderRadius;
			z[i] = PApplet.cos(angle) * cylinderRadius;
		}

		//le tube
		PShape openCylinder = app.createShape();
		openCylinder.beginShape(PApplet.QUAD_STRIP);
		for(int i = 0; i < x.length; i++) {
			openCylinder.vertex(x[i], 0, z[i]);
			openCylinder.vertex(x[i], cylinderHeight, z[i]);
		}
		openCylinder.endShape();

		//le fond (dessus)
		PShape topCylinder = app.createShape();
		topCylinder.beginShape(PApplet.TRIANGLE_FAN);
		topCylinder.vertex(0, cylinderHeight, 0);
		for(int i = 0; i < x.length; i++) {
			topCylinder.vertex(x[i], cylinderHeight, z[i]);
		}
		topCylinder.endShape();

		//on groupe le tout en cylindre
		cylinder = app.createShape(PApplet.GROUP);
		cylinder.addChild(topCylinder);
		cylinder.addChild(openCylinder);
	}
}
