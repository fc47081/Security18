package com.sc010.manusers;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Scanner;

public class ManUsers {

	private static CatalogoUser catalogo;

	public static void main(String[] args) throws NoSuchAlgorithmException, InvalidKeySpecException {
		try {
			File pasta = new File("Users");
			if (!pasta.exists()) {
				pasta.mkdir();
			}
			File utilizadores = new File("Users/users.txt");
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
			System.out.println("[ add <user> <password> | del <user> <password> | update <user> <password> | quit ]");

			argumentos = sc.nextLine().split(" ");
			if (Utils.check(argumentos)) {
				switch (argumentos[0]) {
				case "add":
					catalogo.add(argumentos[1], argumentos[2]);
					System.out.printf("Adicionado %s %s\n", argumentos[1], argumentos[2]);
					break;
				case "del":
					catalogo.del(argumentos[1]);
					System.out.printf("Removido %s\n", argumentos[1]);
					break;
				case "update":
					catalogo.update(argumentos[1], argumentos[2]);
					System.out.printf("Atualizado %s %s\n", argumentos[1], argumentos[2]);
					break;
				case "quit":
					end = !end;
					System.out.println("Saindo...");
				default:
				}
			}
		}
		sc.close();
	}

}
