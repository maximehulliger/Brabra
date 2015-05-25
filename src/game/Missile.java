package game;

import processing.core.PImage;
import processing.core.PVector;
import game.geo.Cube;
import game.geo.Sphere;
import game.physic.Collider;

public class Missile extends Cube {
	private final float classe;
	private final Collider launcher;
	
	public Missile(PVector location, PVector orientation, Collider launcher, float classe) {
		super(location, orientation, classe, vec(2*classe, 2*classe, 10*classe) );
		this.launcher = launcher;
		this.classe = classe;
	}
	
	public void display() {
		app.fill(255, 0, 0);
		super.display();
	}
	
	public boolean doCollideFast(Collider col) {
		return col != launcher && super.doCollideFast(col);
	}
	
	protected void addForces() {
		avance(classe);
	}
	
	public void onCollision(Collider col, PVector impact) {
		app.physic.toRemove.add( this );
		app.physic.effectsToAdd.add( new Effect.Explosion( impact, 30 ) );
		
		//réaction objectif
		if (col instanceof Objectif) {
			((Objectif)col).damage(classe);
		}
	}
	
	// une sphère à détruire
	public static class Objectif extends Sphere {
		public float life;
		
		public Objectif(PVector location, float life) {
			super(location, 30, 50);
			this.life = life;
		}
		
		public void display() {
			app.fill(255, 255, 0);
			super.display();
		}
		
		public void addForces() {
			freine(0.01f);
		}
		
		public void damage(float damage) {
			life -= damage;
			if (life < 0) {
				app.physic.toRemove.add(this);
				System.out.println("détruit !");
			}
		}
	}
	
	public static class LanceMissile {
		private final static int tAffichageErreur = 15;
		public static PImage missileImg;
		private final Collider parent;
		private final PVector loc;
		private final int classe; 
		private final int tRecharge;
		public int tempsRestant;
		private int indicateurErreur = 0;
		
		public LanceMissile(Collider parent, PVector loc, int classe, int tRecharge) {
			this.parent = parent;
			this.loc = loc;
			this.classe = classe;
			this.tRecharge = tRecharge;
			tempsRestant = tRecharge;
		}
		
		public void update() {
			if (tempsRestant > 0)
				tempsRestant--;
			if (indicateurErreur > 0)
				indicateurErreur--;
		}
		
		public void tire() {
			if (tempsRestant > 0) {
				indicateurErreur = tAffichageErreur;
				System.out.println("encore "+tempsRestant+" frame(s) !");
			} else {
				tempsRestant = tRecharge;
				Missile m = new Missile(parent.absolute(loc), parent.orientation, parent, classe);
				m.rotation = parent.rotation.get();
				ProMaster.app.physic.toAdd.add( m );
			}
		}
		
		//retourne le nouveau point bas gauche
		public PVector displayGui(PVector basGauche) {
			app.noStroke();
			PVector imgDim = new PVector(missileImg.width, missileImg.height);
			imgDim.mult(classe/7f);
			app.image(missileImg, basGauche.x, basGauche.y-imgDim.y, imgDim.x, imgDim.y);
			if (tempsRestant > 0) {
				app.fill(255, 0, 0, 70);
				float hauteur = imgDim.y*(1f-((float)tempsRestant)/tRecharge);
				app.rect(basGauche.x, basGauche.y-hauteur, imgDim.x, hauteur);
				if (indicateurErreur > 0) {
					app.fill(255, 0, 0, 150*(((float)indicateurErreur)/tAffichageErreur));
					app.rect(basGauche.x, basGauche.y-imgDim.y, imgDim.x, imgDim.y);
				}
			} else {
				app.fill(90, 255, 90, 70);
				app.rect(basGauche.x, basGauche.y-imgDim.y, imgDim.x, imgDim.y);
			}
			return new PVector(basGauche.x+imgDim.x, basGauche.y);
		}
	}
	
	public static class Armement {
		//private final Collider parent;
		private final LanceMissile[] lmissiles;
		
		public Armement(Collider parent) {
			//this.parent = parent;
			lmissiles = new LanceMissile[] {
					new LanceMissile(parent, vec(parent.radiusEnveloppe*0.65f, 0, -20), 3, 30),
					new LanceMissile(parent, vec(parent.radiusEnveloppe*0.15f, 0, 0), 5, 90),
					new LanceMissile(parent, vec(parent.radiusEnveloppe*-0.15f, 0, 0), 5, 90),
					new LanceMissile(parent, vec(parent.radiusEnveloppe*-0.65f, 0, -20), 3, 30),
			};
		}
		
		public void tire(int idx) {
			lmissiles[idx].tire();
		}
		
		public void update() {
			for (LanceMissile lm : lmissiles)
				lm.update();
		}
		
		// tire le premier missile disponible
		public void tire() {
			for (int i=0; i<lmissiles.length; i++)
				if (lmissiles[i].tempsRestant == 0) {
					lmissiles[i].tire();
					return;
				}
			System.out.println("rien de prêt, capitaine !");
		}

		//affiche l'état des missiles dans la gui
		public void displayGui() {
			PVector basGauche = new PVector(app.width/2-160, app.height);
			for(int i = 0; i<lmissiles.length; i++) {
				basGauche = lmissiles[i].displayGui(basGauche);
			}
		}
		
	}
}
