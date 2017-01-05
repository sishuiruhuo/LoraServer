package random;

import java.util.Random;

public class CreateRandom {
	public byte[] RandomArray(int number){
		byte[] random = new byte[number];
		Random ran = new Random();
		for(int i = 0 ; i < number ; i ++)
		{
			random[i] = (byte) (ran.nextInt(255) & 0xFF);
		
		}
		return random;
		
	}

}
