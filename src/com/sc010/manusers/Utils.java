package com.sc010.manusers;

public class Utils {
	
	public static boolean check(String[] args) {
		if( args.length != 3 )
		{
			System.out.println("Argumentos inválidos: " + args.length);
			return false;
		} else if ( !(args[0].equals("-add") || args[0].equals("-del") || args[0].equals("-update") || args[0].equals("-quit")) ){
			return false;
		}
		return true;
	}

}
