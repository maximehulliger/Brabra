package cs211.tangiblegame;

import java.util.Arrays;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cs211.tangiblegame.geo.Quaternion;
import cs211.tangiblegame.realgame.RealGame;
import processing.core.PApplet;
import processing.core.PVector;

// Processing master
public abstract class ProMaster {
	protected static TangibleGame app;
	protected static RealGame game;
	protected static Random random;
	private static final Pattern floatPattern = Pattern.compile("[+-]?\\d+[.]?\\d*");
	private static final Pattern intPattern = Pattern.compile("[+]?\\d+");
	protected static int colorButtonOk, colorButtonRejected, colorQuad;
	protected static int color0, color255;
	
	
	public static final PVector zero = new PVector(0, 0, 0);
	public static final PVector farfarAway = new PVector(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
	public static final PVector left = new PVector(1, 0, 0);
	public static final PVector right = new PVector(-1, 0, 0);
	public static final PVector up = new PVector(0, 1, 0);
	public static final PVector down = new PVector(0, -1, 0);
	public static final PVector front = new PVector(0, 0, -1);
	public static final PVector behind = new PVector(0, 0, 1);
	public static final Quaternion identity = new Quaternion();
	public static final float small = 0.05f;
	public static final float far = 10_000;
	

	public static void init(TangibleGame app) {
		ProMaster.app = app;
		
		random = new Random();

		color0 = app.color(0);
		color255 = app.color(255);
		colorButtonOk = app.color(0, 255, 0, 150);
		colorButtonRejected = app.color(255, 0, 0, 150);
		colorQuad = app.color(200, 100, 0, 120);
	}

	//---- general mastery

	public static float random(float min, float max) {
		return min + (max-min) * random.nextFloat();
	}

	public static float sq(float t) {
		return t*t;
	}

	public static float sqrt(float t) {
		return PApplet.sqrt(t);
	}

	//retourne true si v E [min, max]
	public static boolean isConstrained(float v, float min, float max) {
		return v>=min && v<=max;
	}

	public static float min(float a, float b) {
		return a<b ? a : b;
	}

	public static float max(float a, float b) {
		return a>b ? a : b;
	}

	public static float entrePiEtMoinsPi(float a) {
		if (a > PApplet.PI) return a - PApplet.TWO_PI;
		else if (a < -PApplet.PI) return a + PApplet.TWO_PI;
		else return a;
	}

	//secondes -> frames
	public static int toFrame(float seconde) {
		return TangibleGame.round(seconde * app.frameRate);
	}

	public static int sgn(float a) {
		if (isZeroEps(a))
			return 0;
		else if (a>0)
			return 1;
		else 
			return -1;
	}

	public static float[] copy(float[] tab) {
		float[] ret = new float[tab.length];
		for (int i=0; i<tab.length; i++)
			ret[i] = tab[i];
		return ret;
	}

	//---- processing mastery

	protected static PVector vec(float x, float y, float z) {
		return new PVector(x, y, z);
	}

	protected static PVector vec(String vec) {
		if (vec==null || vec.equals("zero"))
			return zero;
		else if (vec.equals("front"))
			return front;
		else if (vec.equals("behind") || vec.equals("back"))
			return behind;
		else if (vec.equals("right"))
			return right;
		else if (vec.equals("left"))
			return left;
		else if (vec.equals("up"))
			return up;
		else if (vec.equals("down"))
			return down;
		
		Matcher matcher = floatPattern.matcher(vec);
		float[] values = new float[3];
		for (int i=0; i<3; i++) {
			if (matcher.find()) {
				values[i] = Float.parseFloat(matcher.group());
			} else {
				System.out.println("wrong vector format for \""+vec+"\", taking zero");
				return zero;
			}
		}
		return vec(values[0],values[1],values[2]);
	}
	
	public static PVector add(PVector v1, PVector v2) {
		return PVector.add(v1, v2);
	}

	public static PVector[] copy(PVector[] vectors) {
		PVector[] ret = new PVector[vectors.length];
		for (int i=0; i<vectors.length; i++)
			ret[i] = vectors[i].copy();
		return ret;
	}

	public static PVector[] absolute(PVector[] v, PVector trans, Quaternion rotation) {
		PVector[] ret = new PVector[v.length];
		for (int i=0; i<v.length; i++)
			ret[i] = absolute(v[i], trans, rotation);
		return ret;
	}

	public static float distSq(PVector p1, PVector p2) {
		return PVector.sub(p1, p2).magSq();
	}

	public static boolean equalsEps(PVector p1, PVector p2) {
		return isZeroEps( PVector.sub(p1, p2), false);
	}

	public static boolean isZeroEps(PVector p, boolean setToZero) {
		if (p.equals(zero))
			return true;
		else {
			if (setToZero) {
				if (p.x != 0 && isZeroEps(p.x))
					p.x = 0;
				if (p.y != 0 && isZeroEps(p.y))
					p.y = 0;
				if (p.z != 0 && isZeroEps(p.z))
					p.z = 0;
				return p.equals(zero);
			} else {
				return isZeroEps(p.x) && isZeroEps(p.y) && isZeroEps(p.z);
			}

		}
	}

	public static boolean isZeroEps(float f) {
		return f==0 || (f <= TangibleGame.EPSILON && f >= -TangibleGame.EPSILON);	
	}
	
	protected void line(PVector v1, PVector v2) {
		app.line(v1.x,v1.y,v1.z,v2.x,v2.y,v2.z);
	}
	
	protected PVector normalized(PVector p) {
		PVector pp = p.copy();
		return pp.normalize();
	}

	public static PVector moyenne(PVector[] points) {
		PVector moyenne = zero.copy();
		for (PVector p : points)
			moyenne.add(p);
		moyenne.div(points.length);
		return moyenne;
	}

	// effectue une multiplication slot par slot. (utile pour L = Iw)
	public static PVector multMatrix(PVector matriceDiagOnly, PVector vector) {
		return new PVector(
				matriceDiagOnly.x * vector.x,
				matriceDiagOnly.y * vector.y,
				matriceDiagOnly.z * vector.z );
	}

	//------ Transformations (location, rotation)

	public static PVector absolute(PVector rel, PVector trans, Quaternion rotation) {
		app.pushMatrix();
		translate(trans);
		app.pushMatrix();
		rotate(rotation);
		PVector ret = new PVector( app.modelX(rel.x, rel.y, rel.z), app.modelY(rel.x, rel.y, rel.z), app.modelZ(rel.x, rel.y, rel.z) );
		app.popMatrix();
		app.popMatrix();
		return ret;
	}

	public static PVector local(PVector abs, PVector trans, Quaternion rotation) {
		return absolute( PVector.sub(abs, trans), zero, rotation.withOppositeAngle());
	}

	protected static void translate(PVector t) {
		app.translate(t.x, t.y, t.z);
	}

	protected static void rotate(Quaternion rotation) {
		rotate(rotation.rotAxis());
	}

	protected static void rotate(PVector rotation) {
		if (rotation == null)
			return;
		app.rotate(rotation.mag(), rotation.x, rotation.y, rotation.z);
	}

	public PVector screenPos(PVector pos3D) {
		return new PVector( app.screenX(pos3D.x, pos3D.y, pos3D.z), app.screenY(pos3D.x, pos3D.y, pos3D.z) );
	}

	// --- Couleurs ---
	
	public static class Color {
		public static final Color grey = new Color(150, 150);
		public static final Color white = new Color(255, 150);
		public static final Color red = new Color(255, 0, 0, 200);
		public static final Color green = new Color(0, 255, 0, 200);
		public static final Color blue = new Color(0, 0, 255, 200);
		public static final Color grass = new Color(128,200,128);
		public static final Color yellow = new Color(255,255,0,150, 255);
		public static final Color pink = new Color(255, 105, 180);
		public static final Color basic = yellow;
		
		private final int[] c;
		private final int[] s;

		/**
		 * c,c,c,255; c,c,c,a; r,g,b,255; or r,g,b,a;
		 * si plus de 4 arguments, le reste set le stroke.
		 */
		public Color(int... rgba) {
			if (rgba.length <= 4) {
				c = fromUF(rgba);
				s = null;
			} else {
				c = fromUF(Arrays.copyOfRange(rgba, 0, 4));
				s = fromUF(Arrays.copyOfRange(rgba, 4, rgba.length));
			}
		}
		
		public Color(String color, String stroke) {
			Color c = getColor(color);
			if (c == null) {
				System.err.println("Color is not set, taking basic");
				c = basic;
			}
			Color s = getColor(stroke);
			if (s == null) {
				this.c = c.get();
				this.s = c.getStroke();
			} else {
				this.c = c.get();
				this.s = s.get();
			}
		}
		
		/** retourne un clone du tableau de couleur primaire */
		private int[] get() {
			return c.clone();
		}
		
		private int[] getStroke() {
			if (s == null)
				return null;
			else
				return s.clone();
		}

		/** applique la couleur primaire et le stroke si set */
		public void fill() {
			app.fill(c[0], c[1], c[2], c[3]);
			if (s != null)
				app.stroke(s[0], s[1], s[2], s[3]);
			else
				app.noStroke();
		}

		private static Color getColor(String color) {
			if (color == null) {
				return null;
			} else if (color.equals("basic")) 
				return basic;
			else if (color.equals("grey")) 
				return grey;
			else if (color.equals("white")) 
				return white;
			else if (color.equals("red")) 
				return red;
			else if (color.equals("blue")) 
				return blue;
			else if (color.equals("green")) 
				return green;
			else if (color.equals("yellow")) 
				return yellow;
			else if (color.equals("grass")) 
				return grass;
			else if (color.equals("pink"))
				return pink;
			else {
				Matcher matcher = intPattern.matcher(color);
				int[] values = new int[4];
				int i=0;
				for (; i<=3 && matcher.find(); i++) {
					values[i] = Integer.parseInt(matcher.group());
				}
				if (i == 0) {
					System.out.println("wrong color format for \""+color+"\", taking basic");
					return basic;
				} else if (i < 4) {
					int[] ret = new int[i];
					System.arraycopy(values, 0, ret, 0, i);
					values = ret;
				}
				return new Color(values);
			}
		}
		
		private static int[] fromUF(int[] rgba) {
			switch (rgba.length) {
			case 1:
				return new int[] {rgba[0], rgba[0], rgba[0], 255};
			case 2:
				return new int[] {rgba[0], rgba[0], rgba[0], rgba[1]};
			case 3:
				return new int[] {rgba[0], rgba[1], rgba[2], 255};
			case 4:
				return rgba;
			default:
				System.err.println("no cool color input: "+rgba);
				return white.c.clone();
			}
		}
	}
}
