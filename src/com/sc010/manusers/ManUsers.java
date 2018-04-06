package com.sc010.manusers;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class ManUsers {
	
	private static CatalogoUser catalogo;
	public static void main(String[] args) {
		try {
			File pasta = new File("Users");
			if (!pasta.exists()) {
				pasta.mkdir();
			}
			File utilizadores = new File("users.txt");
			if (!utilizadores.exists())
				utilizadores.createNewFile();
		} catch (IOException e) {
			System.err.println("Erro ao criar user/pasta :" + e.toString());
		}
		Scanner sc = new Scanner(System.in);
		catalogo = new CatalogoUser();
		String argumentos[];
		boolean end = false;
		while (!end) {
			System.out.println("Escolha uma operacao:");
			System.out.println("[ -add <user> <password> | -del <user> <password> | -update <user> <password> | -quit ]\n");
			
			argumentos = sc.nextLine().split(" ");
			if(Utils.check(argumentos)) {
				switch (argumentos[0]) {
				case "-add":
					catalogo.add(argumentos[1], argumentos[2]);
					break;
				case "-del":
					catalogo.del(argumentos[1]);
					break;
				case "-update":
					catalogo.update(argumentos[1], argumentos[2]);
					break;
				case "-quit":
					end = !end;
				default:
				}
			}
		}
		sc.close();
	}

}
