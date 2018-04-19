package com.sc010.manusers;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class testeLeitura {

	public static void main(String[] args) throws IOException {
		BufferedReader buff = new BufferedReader(new FileReader("Users/users1.txt"));
		String pass = ""; 
		while ((pass=buff.readLine())!= null) {
			System.out.println(pass);
		}
		buff.close();
		
	}

}
