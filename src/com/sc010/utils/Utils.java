package com.sc010.utils;

import javafx.util.Pair;

public class Utils {

	public static boolean check(String[] args) {
		if (!(args[0].equals("add") || args[0].equals("del") || args[0].equals("update") || args[0].equals("quit"))) {
			return false;
		}
		return true;
	}

	/**
	 * Decifra a password dada utilizando o salt dado.
	 * @param password
	 * @param salt
	 * @return	String of password decifrada
	 */
	public static String decifrar(String password, String salt) {
		// TODO
		
		return "";
	}
	
	/**
	 * Cifra a password dada utilizando o salt dado.
	 * @param password
	 * @param salt	
	 * @return Pair(Password, Salt) prontos para ser imprimido
	 */
	public static Pair<String, String> cifrar(String password, String salt) {
		// TODO
		
		return new Pair<String, String>("", "");
	}
}
