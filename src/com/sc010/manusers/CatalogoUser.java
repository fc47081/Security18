package com.sc010.manusers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.ArrayList;
import java.util.Base64;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * @author sc010
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
				// Ler linha a linha e separar por dois pontos.
				// Ex: felipe:salt:oaksdfnoij132l%
				String[] split = line.split(":");
				
				String username, password;
				
				// Username esta limpo no texto.
				username = split[0];
				
				// Salt e password hash split[1],[2]
				password = split[2];				
				
				
				// Ja esta decifrada podemos adicionar.
				user = new User(username, password);				
				
				// All done addiciona ao catalogo.
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
	 * @throws NoSuchAlgorithmException 
	 * @throws InvalidKeySpecException 
	 */
	public void add(String user, String password) throws NoSuchAlgorithmException, InvalidKeySpecException {

		// Criar utilizador.
		User u = new User(user, password);

		if (this.lista().contains(u)) {
			System.out.println("Utilizador j� existe");
		} else {
			this.lista().add(u);
		}

		// Cypher and persist
		
		// Create salt
		byte[] salt = new byte[16];
		SecureRandom sr = new SecureRandom();
		sr.nextBytes(salt);
		
		KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 128); // Why 20. PDF.
		SecretKeyFactory kf = SecretKeyFactory.getInstance("PBEWithHmacSHA256AndAES_128");
		SecretKey key = kf.generateSecret(spec);
		
		String encodedKey = Base64.getEncoder().encodeToString(key.getEncoded());
		String userLine =  user.concat(":").concat(Base64.getEncoder().encodeToString(salt)).concat(":").concat(encodedKey);
		
		// Append to file.
		try {
			FileWriter fw = new FileWriter(this.db, true);
			BufferedWriter bw = new BufferedWriter(fw);
			
			bw.write(userLine);
			bw.newLine();
			bw.close();
			fw.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}}

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
			System.out.println("Utilizador j� existe");
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
