package brabra;

import java.awt.MouseInfo;
import java.awt.Point;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;

import brabra.game.physic.Physic;
import processing.core.PApplet;

/** 
 * An abstract class mastering Java. 
 * Provide a lot of useful methods, mostly syntactic sugar. 
 * Free to use once extended :) 
 * */
public abstract class Master {
	
	public static final Pattern floatPattern = Pattern.compile("[+-]?\\d+[.]?\\d*");
	public static final Pattern intPattern = Pattern.compile("[+]?\\d+");
	public static final Random random = new Random();
	public static final Debug debug = new Debug();
	public static final Map<String, String> env = System.getenv();
	
	// --- General mastery ---
	
	public static void println(Object s) {
		System.out.println(s);
	}
	
	@SuppressWarnings("unchecked")
	/** To cast this object easily. return null if invalid. */
	public static <S, T extends S> T as(S obj, Class <T> as) {
		return as.isInstance(obj) ? (T)obj : null;
	}
	
	@SuppressWarnings("unchecked")
	/** To cast this object easily. return null if invalid. */
	public static <S, T> T asMaybe(S obj, Class <T> as) {
		return as.isInstance(obj) ? (T)obj : null;
	}
	
	/** If maybeFloat is valid, return a Float or a default value (if floatOnly 0 otherwise null). */
	public static Float getFloat(String maybeFloat, boolean floatOnly) {
		try {
	        return Float.parseFloat(maybeFloat);
        } catch (NumberFormatException e) {
        	return floatOnly ? 0f : null;
	    }
	}
	
	private static final DecimalFormat shortString = new DecimalFormat("####.###");
	/** Return this float formated (string) to hold in 7 char. */
	public static String formatFloat(float value, float epsilon) {
		if (Physic.isZeroEps(value)) //TODO add epsilon option
			return "0";
		else {
			final int intValue = (int) value;
			return value == intValue ? Integer.toString(intValue) : shortString.format(value);
		}
	}
	
	public static void println(float[] array) {
		println(Arrays.toString(array));
	}
	
	/** angle en radian => [-pi, pi] */
	public static float entrePiEtMoinsPi(float a) {
		if (a > PApplet.PI) return a - PApplet.TWO_PI;
		else if (a < -PApplet.PI) return a + PApplet.TWO_PI;
		else return a;
	}
	
	/** [min, max] => [min2, max2] */
	public static float map(float val, float min, float max, float min2, float max2, boolean constrain) {
		return (clamp(val, min, max, constrain)-min)/(max-min)*(max2-min2) + min2;
	}
	
	/** [min, max] => [0, 1] */
	public static float clamp(float val, float min, float max, boolean constrain) {
		if (constrain)
			val = constrain(val, min, max);
		return (val - min)/(max - min);
	}
	
	/** Return f % m for floats. f will be in [0, m[. m should be bigger than 0. */
	public static float mod(float f, float m) {
		assert(m>0);
		final float res = f - ((int)(f/m))*m;
		return res >= 0 ? res : res+m;
	}

	/** [min, max] */
	public static float random(float min, float max) {
		return min + (max-min) * random.nextFloat();
	}

	/** [min, max) */
	public static int random(int min, int max) {
		return min + round((max-min) * random.nextFloat());
	}
	
	/** Return the closest integer from f. */
	public static int round(float f) {
		return Math.round(f);
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

	public static <T> void forAllPairs(Iterable<T> col, BiConsumer<T,T> fun) {
		Iterator<T> it1 = col.iterator();
		while (it1.hasNext()) {
			T o1 = it1.next();
			Iterator<T> it2 = col.iterator();
			while (it2.hasNext()) {
				T o2 = it2.next();
				if (o1 == o2)
					break;
				else 
					fun.accept(o1,o2);
			}
		}
	}

	/** To iterate over all of those */
	public static <T> AllOfIter<T> both(Iterable<? extends T> first, Iterable<? extends T> second) {
		return new AllOfIter<T>(first, second);
	}

	/** To iterate over all of those */
	public static <T, N extends T, M extends T> AllOfIter<T> both(N[] first, M[] second) {
		return new AllOfIter<T>(Arrays.asList(first), Arrays.asList(second));
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

	public static int sgn(float a) {
		if (a == 0)
			return 0;
		else if (a>0)
			return 1;
		else 
			return -1;
	}
	
	public static int abs(int i) {
		return i<0 ? -i : i;
	}
	
	public static float abs(float f) {
		return f<0 ? -f : f;
	}
	
	/** a random value in [-1, 1] */ 
	public static float randomBi() {
		return random(-1f, 1f);
	}
	
	public static float sq(float t) {
		return t*t;
	}

	public static float sqrt(float t) {
		return PApplet.sqrt(t);
	}

	public static float min(float a, float b) {
		return a<b ? a : b;
	}

	public static float min(float... fs) {
		float min = fs[0];
		for (int i=1; i<fs.length; i++)
			min = min(min, fs[i]);
		return min;
	}

	public static float max(float a, float b) {
		return a>b ? a : b;
	}

	public static float max(float... fs) {
		float max = fs[0];
		for (int i=1; i<fs.length; i++)
			max = max(max, fs[i]);
		return max;
	}

	public static int min(int a, int b) {
		return a<b ? a : b;
	}

	public static int max(int a, int b) {
		return a>b ? a : b;
	}

	/** return v in [min, max]. */
	public static int constrain(int v, int min, int max) {
		if (v < min) 		return min;
		else if (v > max) 	return max;
		else 				return v;
	}

	/** return v in [min, max]. */
	public static float constrain(float v, float min, float max) {
		if (v < min) 		return min;
		else if (v > max) 	return max;
		else 				return v;
	}

	/** return true if v is in [min, max] */
	public static boolean isConstrained(float v, float min, float max) {
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