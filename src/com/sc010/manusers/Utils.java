package com.sc010.manusers;

import java.io.File;
import java.io.IOException;

public class Utils {

	public static boolean check(String[] args) {
		if (!(args[0].equals("add") || args[0].equals("del") || args[0].equals("update")
				|| args[0].equals("quit"))) {
			return false;
		}
		return true;
	}
}
