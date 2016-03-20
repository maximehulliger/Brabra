package cs211.tangiblegame;

import cs211.tangiblegame.imageprocessing.ImageAnalyser;
import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PImage;

public class Menu extends Interface {
	//title & buttons
	private static final int titleOffsetX = TangibleGame.width/2+250, titleOffsetY = 100; 
	private static final int buttonFontSize = 60, titleFontSize = 120, helpFontSize = 16; 
	private static final int buttonSizeX = 400, buttonSizeY = 100;
	private static final int buttonDiffY = buttonSizeY + 60;
	private static final int nbButton = 3;
	private static final int buttonOffsetX = TangibleGame.width/2 - buttonSizeX - 100,
				buttonOffsetY = TangibleGame.height/2 - ((nbButton-1)*buttonDiffY + buttonSizeY)/2;
	//help: button, zone & text
	private static final int helpButtonOffsetX = TangibleGame.width/2+150, helpButtonOffsetY = 200;
	private static final int helpButtonSizeX = 150, helpButtonSizeY = 60;
	private static final int helpZoneSizeX = 400, helpZoneSizeY = 415;
	private static final int helpZoneOffsetX = helpButtonOffsetX + helpButtonSizeX/2 - helpZoneSizeX/2,
			helpZoneOffsetY = helpButtonOffsetY + helpButtonSizeY + 10;
	private static final int helpTextOffsetX = helpZoneOffsetX + 30,
			helpTextOffsetY = helpZoneOffsetY + 30;
	//paramètres transition
	private static final float ttransLat = 0.4f;
	private static final float ttransHaut = 0.5f;
	private static final float ttransSmile = 0.5f;
	private static final float ttransTotal = ttransLat + ttransHaut + ttransSmile;
		
	private static final String[] buttonTexts = {"Real game", "Trivial game", "Calibration"};
	private static final String title = "Brabra !";
	private static final String helpText = "Controls:\n"
			+ "  i -> change video input\n"
			+ "  p -> pause/play video input\n"
			+ "  q/Q -> reset / reset all parameters\n"
			+ "  esc -> quit or return\n"
			+ "Real game: \n"
			+ "  the goal is to destroy all the yellow balls.\n"
			+ "  right button, 1-4, e  -> launch missiles\n"
			+ "  left button, ws -> move\n"
			+ "  plate rotation, ad, mouse drag -> rotate the ship\n"
			+ "  <tab> -> change the view\n"
			+ "Trivial game:\n"
			+ "  hold shift to enter placement mode, \n"
			+ "  click to place obstacle on the plate.\n"
			+ "Calibration:\n"
			+ "  l/s -> load/save parameters\n";
			//+ "                       for more info, look in 'readmeplz.txt'";
	
	private PImage imgBoutonUp;
	private PImage imgBoutonDown;
	private PFont fontMainTitle;
	private PFont fontButton;
	private PFont fontHelpText;
	private PFont fontHelpButtonText;
	private int rainbow = 0;
	private float etatInfo = 0;
	
	public Menu() {
		imgBoutonUp = app.loadImage("boutonUp.png");
		imgBoutonDown = app.loadImage("boutonDown.png");
		fontMainTitle = app.createFont("Arial", titleFontSize, true);
		fontButton = app.createFont("Arial", buttonFontSize, true);
		fontHelpButtonText = app.createFont("Arial", helpButtonSizeY*3/5, true);
		fontHelpText = app.createFont("Arial", helpFontSize, true);
	}
	
	public void init() {}
	
	public void wakeUp() {
		app.imgAnalyser.play(false);
		app.imgAnalyser.forced = false;
		app.imgAnalyser.detectButtons = false;
		ImageAnalyser.displayQuadRejectionCause = false; 
	}

	public void draw() {
		app.background(0, 75, 150);
		app.textAlign(PApplet.CENTER, PApplet.CENTER);
		// les 3 boutons
		app.textFont(fontButton) ;
		int y = buttonOffsetY;
		PImage toDraw;
		for (int i=0; i<nbButton; i++, y+=buttonDiffY) {
			int diffx = app.mouseX - buttonOffsetX, diffy = app.mouseY - y;
			if (diffx > 0 && diffx < buttonSizeX && diffy > 0 && diffy < buttonSizeY) { 
				app.fill(100, 0, 200);
				toDraw = imgBoutonDown;
			} else {
				app.fill(255);
				toDraw = imgBoutonUp;
			}
			app.image(toDraw, buttonOffsetX, y, buttonSizeX, buttonSizeY);
			app.text(buttonTexts[i], buttonOffsetX + buttonSizeX/2 , y + buttonSizeY*3/7);
		}
		
		// le titre
		app.colorMode(PApplet.HSB);
		app.fill(rainbow++ % 255, 200, 255);
		app.colorMode(PApplet.RGB);
		app.textFont(fontMainTitle) ;
		app.text(title, titleOffsetX, titleOffsetY);
		
		// le fond du bouton d'info
		app.tint(255, 60);
		app.image(imgBoutonUp, helpButtonOffsetX, helpButtonOffsetY, helpButtonSizeX, helpButtonSizeY);
		app.tint(255, 255);
		
		float hzSizeX = helpZoneSizeX * PApplet.constrain(etatInfo/ttransLat, 0, 1),
				hzSizeY = 1 + (helpZoneSizeY-1) * PApplet.constrain((etatInfo-ttransLat)/ttransHaut, 0, 1);
		
		boolean etatGoesUp = false;
		boolean onHelpButton = false;
		
		// - sur le bouton ou  la zone
		int diffx = app.mouseX - helpButtonOffsetX, diffy = app.mouseY - helpButtonOffsetY;
		int diffZonex = app.mouseX - helpZoneOffsetX, diffZoney = app.mouseY - helpZoneOffsetY;

		if (diffx > 0 && diffx < helpButtonSizeX && diffy > 0 && diffy < helpButtonSizeY) { //dans le bouton
			onHelpButton = true;
			etatInfo = PApplet.constrain(etatInfo + 1/app.frameRate, 0, ttransTotal);
			etatGoesUp = true;
		} else if (diffZonex > 0 && diffZonex < helpZoneSizeX && diffZoney > 0 && diffZoney < hzSizeY) { //dans la zone
			etatInfo = PApplet.constrain(etatInfo + 1/app.frameRate, 0, ttransTotal);
			etatGoesUp = true;
		} else {
			etatInfo = PApplet.constrain(etatInfo - 1/app.frameRate, 0, ttransTotal);
			etatGoesUp = false;
		}
		
		// print le texte du bouton
		String textHelp;
		if (onHelpButton) {
			textHelp = ":D";
		} else if (etatInfo == 0) {
			textHelp = "Help ?";
		} else if (etatGoesUp) {
			if (etatInfo > ttransLat + ttransHaut && etatInfo < ttransLat + ttransHaut + ttransSmile) {
				textHelp = ":D";
			} else {
				textHelp = ":)";
			}
		} else { // etat goes down
			if (etatInfo > ttransLat + ttransHaut) {
				textHelp = ":o";
			} else if (etatInfo < ttransLat) {
				textHelp = ":'(";
			} else {
				textHelp = ":\\";
			}
		}
		app.fill(255);
		app.textFont(fontHelpButtonText);
		app.text(textHelp, helpButtonOffsetX + helpButtonSizeX/2, helpButtonOffsetY + helpButtonSizeY*3/7);
		
		// print la zone
		if (etatInfo > 0) {
			app.fill(50);
			app.rect(helpZoneOffsetX, helpZoneOffsetY, hzSizeX, hzSizeY);
		}
		// print le texte
		if (etatInfo > ttransLat + ttransHaut) {
			app.textAlign(PApplet.BASELINE);
			app.fill(50 + 205f * (etatInfo - ttransLat - ttransHaut) / ttransSmile);
			app.textFont(fontHelpText);
			app.text(helpText, helpTextOffsetX, helpTextOffsetY);
		}
	}
	
	public void mouseReleased() {
		int y = buttonOffsetY;
		for (int i=0; i<nbButton; i++, y+=buttonDiffY) {
			int diffx = app.mouseX - buttonOffsetX, diffy = app.mouseY - y;
			if (diffx > 0 && diffx < buttonSizeX && diffy > 0 && diffy < buttonSizeY) {
				switch (i) {
				case 0:
					app.setView(TangibleGame.View.RealGame);
					return;
				case 1:
					app.setView(TangibleGame.View.TrivialGame);
					return;
				case 2:
					app.setView(TangibleGame.View.Calibration);
					return;
				default:
					return;
				}
			} 
		}
	}
}
