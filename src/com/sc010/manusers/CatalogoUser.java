package com.sc010.manusers;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.ArrayList;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import javax.xml.bind.DatatypeConverter;

import com.sun.org.apache.bcel.internal.generic.NEW;

import sun.misc.BASE64Encoder;

/**
 * @author sc010
 *
 */
public class CatalogoUser {
	private static final String PathTemp = "Users/tempPass.txt";
	private static final String usersFile = "Users/users.txt";
	private byte[] encrypted;
	private String encryptedtext;
    private String decrypted;


	private ArrayList<User> users;
	private File db;

	/**
	 * Construtor
	 */
	public CatalogoUser() {
		users = new ArrayList<User>();

		db = new File(usersFile);
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
	 * @throws InvalidKeyException 
	 * @throws NoSuchPaddingException 
	 * @throws BadPaddingException 
	 * @throws IllegalBlockSizeException 
	 * @throws IOException 
	 * @throws InvalidAlgorithmParameterException 
	 */
	public void add(String user, String password) throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, IOException, InvalidAlgorithmParameterException {

		// Criar utilizador.
		User u = new User(user, password);

		if (this.find(user)) {
			System.out.println("Utilizador ja existe");

		} else {
			//System.out.println("Utilizador nao existe");
			this.lista().add(u);
			// Cypher and persist
			// Create salt
			byte[] salt = new byte[16];
			SecureRandom sr = new SecureRandom();
			sr.nextBytes(salt);

			byte[] ivBytes = new byte[16];
			sr.nextBytes(salt);
			cypher(password, ivBytes, salt);

			BASE64Encoder encoder = new BASE64Encoder();

			try {
				FileWriter fw = new FileWriter(this.db, true);
				BufferedWriter bw = new BufferedWriter(fw);
				bw.write(user+":"+encoder.encode(salt)+ encoder.encode(password.getBytes())+":"+cypher(password, ivBytes, salt));
				bw.newLine();
				bw.close();
				fw.close();
				System.out.printf("Adicionado %s %s\n",user, password);
			} catch (IOException e) {
				e.printStackTrace();	
			}

			System.out.println("cifrado : "+cypher(password, ivBytes, salt));
			System.out.println("decifrado : "+decypher(cypher(password, ivBytes, salt), password,ivBytes, salt));


		}	



	}

	public void del(String user) {
		boolean exists = false;
		for (User u : this.lista()) {
			if (u.getUserName().equals(user)) {
				try {
					File tempFile = new File("myTempFile.txt");

					BufferedReader reader = new BufferedReader(new FileReader(this.db));
					BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));

					String currentLine;

					while ((currentLine = reader.readLine()) != null) {
						// trim newline when comparing with lineToRemove
						String trimmedLine = currentLine.trim();
						String[] userpass = trimmedLine.split(":");
						if (userpass[0].equals(user))
							continue;
						writer.write(currentLine + System.getProperty("line.separator"));
					}
					writer.close();
					reader.close();
					Files.move(tempFile.toPath(), this.db.toPath(), StandardCopyOption.REPLACE_EXISTING);
					System.out.printf("Removido %s\n", user);
					exists = true;
				} catch (IOException e) {
					e.printStackTrace();
				}
				break;

			} 

		} 
		if (!exists)
			System.out.println("User nao existe");
	}

	public void update(String username, String password) throws NoSuchAlgorithmException, InvalidKeySpecException {

		// Criar utilizador.
		User u = new User(username, password);

		if (this.lista().contains(u)) {
			System.out.println("Utilizador j� existe");
		} else {
			for (User user : this.lista()) {
				if (user.getUserName().equals(username)) {
					user.setPwd(password);
				}
			}
		}
		// update to file.
		try {
			File tempFile = new File("myTempFile.txt");

			BufferedReader br = new BufferedReader(new FileReader(this.db));
			BufferedWriter bw = new BufferedWriter(new FileWriter(tempFile));

			String currentLine;
			while ((currentLine = br.readLine()) != null) {
				// trim newline when comparing with lineToRemove
				String trimmedLine = currentLine.trim();
				String[] userpass = trimmedLine.split(":");
				if (userpass[0].equals(username))
					continue;
				bw.write(currentLine + System.getProperty("line.separator"));
			}

			// Create salt
			byte[] salt = new byte[16];
			SecureRandom sr = new SecureRandom();
			sr.nextBytes(salt);

			KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 128); // Why 20. PDF.
			SecretKeyFactory kf = SecretKeyFactory.getInstance("PBEWithHmacSHA256AndAES_128");
			SecretKey key = kf.generateSecret(spec);

			String encodedKey = Base64.getEncoder().encodeToString(key.getEncoded());
			String userLine = u.getUserName().concat(":").concat(Base64.getEncoder().encodeToString(salt)).concat(":")
					.concat(encodedKey);
			bw.write(userLine);
			bw.newLine();
			bw.close();
			br.close();
			Files.move(tempFile.toPath(), this.db.toPath(), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}



	public String cypher(String password,byte[] ivBytes, byte[] salt) {
		PBEKeySpec keySpec = new PBEKeySpec(password.toCharArray());
		SecretKeyFactory kf;
		//String cifra= "";
		try {
			kf = SecretKeyFactory.getInstance("PBEWithHmacSHA256AndAES_128");

			SecretKey key = kf.generateSecret(keySpec); 


			IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);
			PBEParameterSpec spec = new PBEParameterSpec(salt, 20, ivSpec);

			Cipher c = Cipher.getInstance("PBEWithHmacSHA256AndAES_128");
			c.init(Cipher.ENCRYPT_MODE, key, spec);

			byte[] encrypted = c.doFinal(password.getBytes());
			encryptedtext = DatatypeConverter.printBase64Binary(encrypted);
			
			
			
			//byte[] cypher = c.doFinal(dados);
		    
			//cifra = new String(cypher);
	
		}catch (Exception e) {
			System.out.println("houve algum erro ao cifrar");
		}
		return encryptedtext;
	}

	public String decypher(String cifrado,String pass,byte[] ivBytes,byte[] salt) throws IOException {		
		PBEKeySpec keySpec = new PBEKeySpec(pass.toCharArray());
		SecretKeyFactory kf;
		//String cifra = ""; 
		try {
			kf = SecretKeyFactory.getInstance("PBEWithHmacSHA256AndAES_128");

			SecretKey key = kf.generateSecret(keySpec); 

			IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);
			PBEParameterSpec spec = new PBEParameterSpec(salt, 20, ivSpec);

			Cipher c = Cipher.getInstance("PBEWithHmacSHA256AndAES_128");
			c.init(Cipher.DECRYPT_MODE, key, spec);
			
			encrypted = DatatypeConverter.parseBase64Binary(cifrado);
            decrypted = new String(c.doFinal(encrypted)); 
		    
			//String encripted = DatatypeConverter.printBase64Binary(cypher);
		    
			//cifra = new String(encripted); 
		}catch (Exception e) {
			e.printStackTrace();
			//System.out.println("houve algum erro ao decifrar");
		}

		return decrypted;

	}




}
