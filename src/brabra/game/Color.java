package brabra.game;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;

import brabra.ProMaster;

// --- Couleurs ---

public class Color extends ProMaster {
	private static final Map<String, Color> colors = new HashMap<String, Color>() {
		private static final long serialVersionUID = -4177973193569192575L;
		{
			put("grey", new Color(150, 150));
			put("white", new Color(255, 150));
			put("red", new Color(255, 0, 0, 200));
			put("green", new Color(0, 255, 0, 200));
			put("blue", new Color(0, 0, 255, 200));
			put("grass", new Color(128,200,128));
			put("yellow", new Color(255,255,0,150));
			put("pink", new Color(255, 105, 180));
			put("blueDark", new Color(50, 100, 125));
		}
	};
	
	public static final Color basic = new Color("yellow", true);
	
	private final int[] c;
	private final int[] s;
	
	/**
	 * understand 4 argument for a color: c, ca, rgb, rgba equivalent to:
	 * (c,c,c,255), (c,c,c,a), (r,g,b,255), or (r,g,b,a).
	 * If more than 4 arguments, the rest set the stroke in a similar way.
	 */
	public Color(int... rgba) {
		this(fromUF(Arrays.copyOfRange(rgba, 0, min(rgba.length, 4))),
				rgba.length <= 4 ? null : fromUF(Arrays.copyOfRange(rgba, 4, min(rgba.length, 8))));
	}

	/** Return a new color from a string. if withSameStroke, stroke=color otherwise stroke=noStroke. */
	public Color(String color, boolean withSameStroke) {
		this(get(color).c, withSameStroke ? get(color).c : null);
	}

	/** Return a new color from 2 string. if stroke is null, set stroke to noStroke. */
	public Color(String color, String stroke) {
		this(get(color).c, stroke==null ? null : get(stroke).c);
	}

	private Color(int[] color, int[] stroke) {
		if (color.length==0 || (stroke!=null && stroke.length==0)) 
			throw new IllegalArgumentException("give me something for a color, plz m8");
		this.c = color;
		this.s = stroke;
	}
	
	/** Return a new color with this stroke. */
	public Color withStroke(int... stroke) {
		return new Color(c, stroke);
	}
	
	/** Return a new color with this stroke. */
	public Color withStroke(String stroke) {
		return new Color(c, get(stroke).c);
	}
	
	/** applique la couleur primaire et le stroke si set */
	public void fill() {
		app.fill(c[0], c[1], c[2], c[3]);
		if (s != null)
			app.stroke(s[0], s[1], s[2], s[3]);
		else
			app.noStroke();
	}
	
	private static Color get(String color) {
		if (color == null) {
			return null;
		} else {
			Color c = getExistingColor(color);
			if (c != null) {
				return c;
			} else {
				Matcher matcher = intPattern.matcher(color);
				int[] values = new int[12];
				int i=0;
				for (; i<values.length && matcher.find(); i++)
					values[i] = Integer.parseInt(matcher.group());
				if (i == 0) {
					debug.err("Unkno color format for \""+color+"\", taking basic");
					return basic;
				} else if (i >= 8) {
					debug.err("wrong color format for \""+color+"\" (max 8, taking basic");
					return basic;
				} else {
					int[] ret = new int[i];
					System.arraycopy(values, 0, ret, 0, i);
					values = ret;
					return new Color(values);
				}
			}
		}
	}
	
	/** get an existing color from string. Default stroke is noStroke. */
	private static Color getExistingColor(String color) {
		return colors.get(color);
	}
	
	private static int[] fromUF(int[] rgba) {
		assert(rgba.length > 0 && rgba.length <= 4);
		if (rgba.length == 1)
			return new int[] {rgba[0], rgba[0], rgba[0], 255};
		else if (rgba.length == 2)
			return new int[] {rgba[0], rgba[0], rgba[0], rgba[1]};
		else if (rgba.length == 3)
			return new int[] {rgba[0], rgba[1], rgba[2], 255};
		else // rgba.length = 4
			return rgba;
	}
}