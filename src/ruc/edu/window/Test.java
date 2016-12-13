package ruc.edu.window;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class Test {
	public static void main(String[] args) {
		List<Integer> test = new ArrayList<Integer>();
		test.add(2);
		System.out.println("test size: " + test.size());
		test.clear();	
		System.out.println("test size: " + test.size());
		
		int[] temp = new int[]{1,2,3};
		
		int[] B = temp.clone();
		
		if( temp.equals(B)) {
			System.out.println("same");
		}
		
	}
}
