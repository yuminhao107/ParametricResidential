package lesson0104;

import java.util.Random;

public class HRand {
	private static long seed=Option.random_Seed;

	private static Random rand;
	
	public static double random(){
		if (rand==null) rand=new Random(seed);
		return rand.nextDouble();
	}

}
