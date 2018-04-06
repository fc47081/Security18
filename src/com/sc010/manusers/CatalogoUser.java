package com.sc010.manusers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * @author Felipe
 *
 */
public class CatalogoUser {

	private ArrayList<User> users;
	private File db;

	/**
	 * Construtor
	 */
	public CatalogoUser() {
		users = new ArrayList<User>();

		db = new File("Users/users.txt");
		populate(db);

	}

	/**
	 * Lista de users
	 * 
	 * @return users
	 */
	public ArrayList<User> lista() {
		return users;
	}

	/**
	 * Find user name
	 * 
	 * @param user
	 *            - nome do user
	 * @return boolean de verificacao
	 */
	public boolean find(String user) {
		for (int i = 0; i < users.size(); i++) {
			if (user.equals(users.get(i).getUserName()))
				return true;
		}
		return false;
	}

	/**
	 * Get nome do user
	 * 
	 * @return UserName
	 */
	public User getUser(String username) {
		for (int i = 0; i < users.size(); i++) {
			if (username.equals(users.get(i).getUserName()))
				return users.get(i);
		}
		return null;
	}

	/**
	 * Verifica se password esta correta
	 * 
	 * @param user
	 *            - user name
	 * @param pwd
	 *            - passwrod
	 * @return boolean de verificacao
	 */
	public boolean pwdCerta(String user, String pwd) {
		for (int i = 0; i < users.size(); i++) {
			if (user.equals(users.get(i).getUserName())) {
				if (pwd.equals(users.get(i).getPassword()))
					return true;
			}
		}
		return false;
	}

	/**
	 * Get password do user user
	 * 
	 * @param user
	 *            - user name
	 * @return users.get(i).getPassword()
	 */
	public String getUserPwd(String user) {
		for (int i = 0; i < users.size(); i++) {
			if (user.equals(users.get(i).getUserName())) {
				return users.get(i).getPassword();
			}
		}
		return null;
	}

	/**
	 * Popular o catalogo com users
	 * 
	 * @param utilizadores
	 *            - ficheiro de users
	 * @throws IOException
	 */
	public void populate(File utilizadores) {

		try {
			BufferedReader reader = new BufferedReader(new FileReader(utilizadores));
			String line = "";
			User user;
			while ((line = reader.readLine()) != null) {
				String[] split = line.split(":");
				user = new User(split[0], split[1]);
				users.add(user);
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Adiciona utilizador e a sua password ao catalogo de utilizadores. De forma
	 * segura.
	 * 
	 * @param user
	 *            Utilizador a ser adicionado
	 * @param password
	 *            Password a ser adicionada
	 */
	public void add(String user, String password) {

		// Criar utilizador.
		User u = new User(user, password);

		if (this.lista().contains(u)) {
			System.out.println("Utilizador já existe");
		} else {
			this.lista().add(u);
		}

		// Cypher and persist

	}

	public void del(String user) {
		boolean exists = false;
		for (User u : this.lista()) {
			if (u.getUserName().equals(user)) {
				exists = true;
			}
		}
		if (!exists) {
			System.out.println("User nao existe");
		}
		// Cypher and persist

	}

	public void update(String username, String password) {

		// Criar utilizador.
		User u = new User(username, password);

		if (this.lista().contains(u)) {
			System.out.println("Utilizador já existe");
		} else {
			for (User user : this.lista()) {
				if(user.getUserName().equals(username)) {
					user.setPwd(password);
				}
			}
			
			// Cypher and persist
		}
	}
}
