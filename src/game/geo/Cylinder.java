package game.geo;


import game.Game;
import game.ProMaster;

import java.util.ArrayList;

import processing.core.*;

public class Cylinder extends ProMaster {
	//-- parametres
	public static final float cylinderRadius = 20;  //radius
	public static final float cylinderHeight = 30;    
	private static final int cylinderResolution = 42;  //# point
	
	//-- interne
	private static PShape cylinder;
	public static ArrayList<PVector> cylindersPos = new ArrayList<>();
	
	public static void displayCylinders() {
		for (PVector v : cylindersPos) {
		  printCylinder(v);
		}
	}
	
	public static void printCylinder(PVector cylinderPos) {
		app.pushMatrix();
		  translate(cylinderPos);
		  app.fill(255, 255, 0);
		  app.shape(cylinder);
		app.popMatrix();
	}
	
	public static void initCylinder() {
		//les points du cerlce du cylindre
		float angle;
		float[] x = new float[cylinderResolution + 1];
		float[] z = new float[cylinderResolution + 1];
		//get the x and y position on a circle for all the sides
		for(int i = 0; i < x.length; i++) {
		  angle = (Game.TWO_PI / cylinderResolution) * i;
		  x[i] = Game.sin(angle) * cylinderRadius;
		  z[i] = Game.cos(angle) * cylinderRadius;
		}
		
		//le tube
		PShape openCylinder = app.createShape();
		openCylinder.beginShape(Game.QUAD_STRIP);
		for(int i = 0; i < x.length; i++) {
		  openCylinder.vertex(x[i], 0, z[i]);
		  openCylinder.vertex(x[i], cylinderHeight, z[i]);
		}
		openCylinder.endShape();
		
		//le fond (dessus)
		PShape topCylinder = app.createShape();
		topCylinder.beginShape(Game.TRIANGLE_FAN);
		topCylinder.vertex(0, cylinderHeight, 0);
		for(int i = 0; i < x.length; i++) {
		  topCylinder.vertex(x[i], cylinderHeight, z[i]);
		}
		topCylinder.endShape();
		
		//on groupe le tout en cylindre
		cylinder = app.createShape(Game.GROUP);
		cylinder.addChild(topCylinder);
		cylinder.addChild(openCylinder);
	}
}