package cs211.tangiblegame;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;

import cs211.tangiblegame.ProMaster;

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
			put("yellow", new Color(255,255,0,150, 255));
			put("pink", new Color(255, 105, 180));
			put("blueDark", new Color(50, 100, 125));
		}
	};
	public static final Color basic = get("yellow");
	
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
			this.c = c.color();
			this.s = c.stroke();
		} else {
			this.c = c.color();
			this.s = s.color();
		}
	}

	/** get an existing color from string. */
	public static Color get(String color) {
		return colors.get(color);
	}

	/** applique la couleur primaire et le stroke si set */
	public void fill() {
		app.fill(c[0], c[1], c[2], c[3]);
		if (s != null)
			app.stroke(s[0], s[1], s[2], s[3]);
		else
			app.noStroke();
	}
	
	/** retourne un clone du tableau de couleur primaire */
	private int[] color() {
		return c.clone();
	}
	
	private int[] stroke() {
		if (s == null)
			return null;
		else
			return s.clone();
	}

	private static Color getColor(String color) {
		if (color == null) {
			return null;
		} else {
			Color c = get(color);
			if (c != null) {
				return c;
			} else {
				Matcher matcher = intPattern.matcher(color);
				int[] values = new int[4];
				int i=0;
				for (; i<=3 && matcher.find(); i++)
					values[i] = Integer.parseInt(matcher.group());
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
			return basic.color();
		}
	}
}