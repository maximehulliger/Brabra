package brabra.calibration;


import processing.core.PApplet;

public class HScrollbar {
	PApplet parent;
	
	float barWidth;  // Bar's width in pixels
	float barHeight; // Bar's height in pixels
	float xPosition; // Bar's x position in pixels
	float yPosition; // Bar's y position in pixels
	float sliderPosition, newSliderPosition; 	// Position of slider
	float sliderPositionMin, sliderPositionMax; // Max and min values of slider
	float etatMax;
	boolean mouseOver;
	boolean locked = false;
	static boolean oneLocked = false;

	// Is the mouse over the slider?
	// Is the mouse clicking and dragging the slider now?
	/**
	 * @brief Creates a new horizontal scrollbar
	 * @param x	The x position of the top left corner of the bar in pixels
	 * @param y The y position of the top left corner of the bar in pixels
	 * @param w The width of the bar in pixels
	 * @param h The height of the bar in pixels
	 */
	HScrollbar(PApplet p, float x, float y, float w, float h, float etat, float etatMax) {
		parent = p;
		barWidth = w;
		barHeight = h;
		xPosition = x;
		yPosition = y;
		this.etatMax = etatMax;
		sliderPosition = xPosition + barWidth / 2 - barHeight / 2;
		sliderPositionMin = xPosition;
		sliderPositionMax = xPosition + barWidth - barHeight;
		newSliderPosition = sliderPositionMin + (sliderPositionMax-sliderPositionMin)*etat/etatMax;
	}
	
	public void setEtat(float etat) {
		newSliderPosition = sliderPositionMin + (sliderPositionMax-sliderPositionMin)*PApplet.min(etat, etatMax)/etatMax;
	}

	public void setEtat(float etat, float etatMax) {
		this.etatMax = etatMax;
		setEtat(etat);
	}

	/**
	 * @brief Updates the state of the scrollbar according to the mouse movement
	 */
	void update() {
		mouseOver = isMouseOver();
		
		if (parent.mousePressed && mouseOver && !oneLocked) {
			locked = true;
			oneLocked = true;
		}
		if (!parent.mousePressed && locked) {
			locked = false;
			oneLocked = false;
		}
		if (locked) {
			newSliderPosition = PApplet.constrain(parent.mouseX - barHeight / 2,
					sliderPositionMin, sliderPositionMax);
		}
		if (PApplet.abs(newSliderPosition - sliderPosition) > 1) {
			sliderPosition = newSliderPosition;
		}
	}

	/**
	 * @brief Gets whether the mouse is hovering the scrollbar
	 * @return Whether the mouse is hovering the scrollbar
	 */
	boolean isMouseOver() {
		return (parent.mouseX > xPosition && parent.mouseX < xPosition + barWidth
				&& parent.mouseY > yPosition && parent.mouseY < yPosition + barHeight);
	}

	/**
	 * @brief Draws the scrollbar in its current state
	 */
	void display() {
		parent.noStroke();
		parent.fill(204);
		parent.rect(xPosition, yPosition, barWidth, barHeight);
		if (mouseOver || locked) {
			parent.fill(0, 0, 0);
		} else {
			parent.fill(102, 102, 102);
		}
		parent.rect(sliderPosition, yPosition, barHeight, barHeight);
	}

	/**
	 * @brief Gets the slider position
	 * @return The slider position in the interval [0,1] corresponding to
	 *         [leftmost position, rightmost position]
	 */
	float getEtat() {
		return (sliderPosition - xPosition) / (barWidth - barHeight) * etatMax;
	}
}