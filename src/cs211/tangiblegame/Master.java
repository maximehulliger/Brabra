package cs211.tangiblegame;

import java.awt.MouseInfo;
import java.awt.Point;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.regex.Pattern;

import processing.core.PApplet;

/** 
 * An abstract class mastering Java. 
 * Provide a lot of useful methods, mostly syntactic sugar. 
 * Free to use once extended :) 
 * */
public class Master {
	protected static final Pattern floatPattern = Pattern.compile("[+-]?\\d+[.]?\\d*");
	protected static final Pattern intPattern = Pattern.compile("[+]?\\d+");
	protected static final Random random = new Random();
	protected static final Debug debug = new Debug();
	protected static final Map<String, String> env = System.getenv();
	
	// --- General mastery ---
	
	/** angle en radian => [-pi, pi] */
	protected static float entrePiEtMoinsPi(float a) {
		if (a > PApplet.PI) return a - PApplet.TWO_PI;
		else if (a < -PApplet.PI) return a + PApplet.TWO_PI;
		else return a;
	}
	
	/** [min, max] => [min2, max2] */
	protected static float map(float val, float min, float max, float min2, float max2, boolean constrain) {
		return (clamp(val, min, max, constrain)-min)/(max-min)*(max2-min2) + min2;
	}
	
	/** [min, max] => [0, 1] */
	protected static float clamp(float val, float min, float max, boolean constrain) {
		if (constrain)
			val = PApplet.constrain(val, min, max);
		return (val - min)/(max - min);
	}
	
	/** [min, max] */
	protected static float random(float min, float max) {
		return min + (max-min) * random.nextFloat();
	}
	
	/** Return the current mouse location in absolute (from the screen). */
	public static Point mouseLoc() {
		return MouseInfo.getPointerInfo().getLocation();
	}
	
	/** Execute the function in another thread and return the thread. */
	public static Thread launch(Runnable function) {
		Thread t = new Thread(function);
		t.start();
		return t;
	}
	
	/** To iterate over all of those */
	public static <T> AllOfIter<T> both(Iterable<? extends T> first, Iterable<? extends T> second) {
		return new AllOfIter<T>(first, second);
	}

	/** Collection whose iterator iterates over all those collections */
	public static class AllOfIter<T>  implements Iterable<T>, Iterator<T> {
		private int nextItem = 1;
		private Iterable<? extends T>[] items;
		private Iterator<? extends T> currentIter;
	    
	    @SafeVarargs
		public AllOfIter(Iterable<? extends T>... items) {
	    	this.items = items;
	    	currentIter = items[0].iterator();
	    }

		public Iterator<T> iterator() {
			return this;
		}
	    
	    public boolean hasNext() {
			if (currentIter.hasNext()) 
				return true;
			else if (nextItem < items.length) {
				currentIter = items[nextItem++].iterator();
				return hasNext();
			} else
				return false;
		}
	    
		public T next() {
			return (T) currentIter.next();
		}
	}

	/** An iterator over an array. */
	public static class ArrayIter<T>  implements Iterable<T>, Iterator<T> {
		private int idx = 0;
		private T[] array;
	    
	    public ArrayIter(T[] array) {
	    	this.array = array;
	    }

		public Iterator<T> iterator() {
			return this;
		}
	    
	    public boolean hasNext() {
			return (idx < array.length);
		}
	    
		public T next() {
			return array[idx++];
		}
	}
		
	// --- Syntactic sugar ---

	protected static int sgn(float a) {
		if (a == 0)
			return 0;
		else if (a>0)
			return 1;
		else 
			return -1;
	}
	
	/** a random value in [-1, 1] */ 
	protected static float randomBi() {
		return random(-1, 1);
	}
	
	protected static float sq(float t) {
		return t*t;
	}

	protected static float sqrt(float t) {
		return PApplet.sqrt(t);
	}

	protected static float min(float a, float b) {
		return a<b ? a : b;
	}

	protected static float max(float a, float b) {
		return a>b ? a : b;
	}

	/** retourne true si v E [min, max] */
	protected static boolean isConstrained(float v, float min, float max) {
		return v>=min && v<=max;
	}
	
	/** clear the console except if in Eclipse (set by the 'export' environment variable). */
	public final static void clearConsole() {
		if (env.containsKey("export")) {
			try {
				boolean inEclipse = "false".equalsIgnoreCase( env.get("export") );
				if (!inEclipse) {
					final String os = System.getProperty("os.name");
					if (os.contains("Windows"))
						Runtime.getRuntime().exec("cls");
					else
						Runtime.getRuntime().exec("clear");
				}
			} catch (final Exception e) {
				e.printStackTrace();
			}
		}
	}
}