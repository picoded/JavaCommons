package picoded.lang;

import static org.junit.Assert.*;
import org.junit.*;
import java.lang.reflect.Constructor;

public class AnonymousClass_test {
	
	//
	// Replicating steps on how to : "Class.newInstance for anonymous java classes"
	// as asked on stack overflow
	// http://stackoverflow.com/questions/41521966/class-newinstance-for-anonymous-java-classes
	//
	
	int testPort = 0; //Test port to use
	
	/// Class name is obviously for fun =)
	public static class HiveMind {
		/// It does have a public proper constructor
		public HiveMind() {
			onSetup();
		}
		
		/// With great ambition
		public void onSetup() {
			// Should we Take over the world?
		}
		
		/// Status string, to assert
		public String status = "relaxed";
		
		/// Spawn and instance of the current class
		public final HiveMind spawnInstance() throws RuntimeException {
			try {
				Class<? extends HiveMind> instanceClass = this.getClass();
				HiveMind ret = null;
				
				// Tries and create a new instance
				//
				// And for those who says "newInstance" is evil
				// This is an evil HiveMind then D=
				ret = instanceClass.newInstance();
				
				// Does some stuff here
				return ret;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	/// Testing standard class extensions
	public static class DefeatedHiveMind extends HiveMind {
		public void onSetup() {
			this.status = "defeated by hero =(";
		}
	}
	
	/// Making sure it works as expected
	@Test
	public void firstGenerationTest() {
		HiveMind victim = new DefeatedHiveMind();
		assertEquals("defeated by hero =(", victim.status);
		
		HiveMind sibling = victim.spawnInstance();
		assertEquals("defeated by hero =(", sibling.status);
	}
	
	/// This works fine and all.... until someone tries to use it as an anonymous class!
	@Test(expected = Exception.class)
	public void nextGenerationTest() {
		HiveMind nextGeneration = new HiveMind() {
			// New generation new mind set.
			public void onSetup() {
				this.status = "enslave humans"; // Yes we should
			}
		};
		assertEquals("enslave humans", nextGeneration.status);
		
		//
		// Sadly this fails =(
		//
		HiveMind newOverlord = nextGeneration.spawnInstance();
		assertEquals("enslave humans", newOverlord.status);
	}
}
