package brabra.game.scene.fun;

import brabra.game.physic.geo.Vector;
import brabra.game.scene.Object;

public class ParticleSystem<P extends ParticleSystem.Particle> extends Object {
	
	private static class State {
		
	}
	
	public class Particle {
		private Vector location;
	}
}
