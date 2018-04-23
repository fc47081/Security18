package com.sc010.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import javax.xml.bind.DatatypeConverter;

public class Utils {

	public static boolean check(String[] args) {
		if (!(args[0].equals("add") || args[0].equals("del") || args[0].equals("update") || args[0].equals("quit"))) {
			return false;
		}
		return true;
	}


	/**
	 * Decifra a password dada utilizando o salt dado.
	 * 
	 * @param password
	 * @param salt
	 * @return String of password decifrada
	 * @throws IOException 
	 */
	public static String decifrar(File ficheiro,String user) throws IOException {
		BufferedReader UserReader = new BufferedReader(new FileReader(ficheiro));
		String linha = ""; 
		String[] User = null;
		// User[0] = user User[1] = salt User[2] = password
		int countUsers = 0; 
		while ((linha = UserReader.readLine())!= null) {
			countUsers++;
			User = linha.split(":");
			if (user.equals(User[0])) {
				break;
			}
			
		}
		
		UserReader.close();
		
		
		
		
		String password = User[2];
		byte[] salt = new byte[16];
		salt =	DatatypeConverter.parseHexBinary(User[1]);
		byte[] ivBytes = {0x11,0x37,0x69,0x1F,0x3D,0x5A,0x04,0x18,0x23,0x6B,0x1F,0x03,0x1D,0x1E,0x1F,0x20};
		
		
		
		String decrypted = null;
		
		
		
		PBEKeySpec keySpec = new PBEKeySpec("Tree Math Water".toCharArray());
		SecretKeyFactory kf;
		try {
			kf = SecretKeyFactory.getInstance("PBEWithHmacSHA256AndAES_128");

			SecretKey key = kf.generateSecret(keySpec);

			IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);
			PBEParameterSpec spec = new PBEParameterSpec(salt, 20, ivSpec);

			Cipher c = Cipher.getInstance("PBEWithHmacSHA256AndAES_128");
			c.init(Cipher.DECRYPT_MODE, key, spec);
			
			byte[] passwordBytes;

			passwordBytes = DatatypeConverter.parseHexBinary(password);
			decrypted = new String(c.doFinal(passwordBytes));
						
			


		} catch (Exception e) {
			e.printStackTrace();
		}

		return decrypted;

	}
	
	
	
	//  cifra o ficheiro com uma chave aleatoria
	public static void CifraFiles(File ficheiro) {
		
		
		try {
			KeyGenerator kg = KeyGenerator.getInstance("AES");
			kg.init(128);
			SecretKey key = kg.generateKey();
			
			// cifrar a chave privada
			cifraChavePrivada(key);
			
			
			Cipher c = Cipher.getInstance("AES");
			c.init(Cipher.ENCRYPT_MODE, key);
			// faltam buffered streams
			
			
			FileInputStream fis;
			FileOutputStream fos;
			CipherOutputStream cos;
			fis = new FileInputStream(ficheiro);
			fos = new FileOutputStream(ficheiro+".cif");
			cos = new CipherOutputStream(fos, c);
			byte[] b = new byte[16];
			
			
			int i;
			while ((i=fis.read(b) )!= -1) {
			cos.write(b, 0, i);
			}
			cos.close();
		
		
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		
		
		
		
		
	}
	
	
	
	
	//  cifra a chave privada com a chave publica
	public static void cifraChavePrivada(SecretKey key) {
		
		
		
		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

	/*
	public static String[] decifrarFileText(File file, SecretKey key ) {
		String [] ficheiro = null;
		try {
			Mac mac = Mac.getInstance("HmacSHA256");
			FileInputStream fos = new FileInputStream(file);
			mac.init(key);
			ObjectInputStream oos = new ObjectInputStream(fos);
			BufferedReader br = new BufferedReader(new FileReader(file));
			String data = "";
			
			br.lines().collect(Collectors.joining(data));
			mac.
			fos.close();
			br.close();
			oos.close();
		} catch (NoSuchAlgorithmException | InvalidKeyException | IOException e) {			
			e.printStackTrace();
		}

		return ficheiro;		
	}
	
	
*/
	
	
	
	/**
	 * 
	 * @param pathKS
	 * @param password
	 * @return
	 */
	public SecretKey loadKeystore(String pathKS, char[] password) {
		KeyStore ks = null;
		try {
			// Cria uma keystore.
			ks = KeyStore.getInstance("JCEKS");
			File keystore = new File(pathKS);

			// Se a keystore existe, dar load desse path.
			if (keystore.exists()) {
				ks.load(new FileInputStream(pathKS), password);

			} else {
				// Se n�o existe dar load com path a null e depois store para escrever no
				// ficheiro.
				ks.load(null, password);
				ks.store(new FileOutputStream(keystore), password);
			}
			// Cria a secretkey com a password dada e compara com a getKey da keystore
			SecretKey key = (SecretKey) ks.getKey("decifraTudo", password);

			// verifica se existe um mac
			if (key == null) {
				key = KeyGenerator.getInstance("HmacSHA256").generateKey();
				Certificate cert = ks.getCertificate("decifraTudo");
				Certificate[] certarray = { cert };
				ks.setKeyEntry("decifraTudo", key, password, certarray);
				ks.store(new FileOutputStream("Users/users.keystore"), password);
				System.out.println("Mac criado.");
			} else {
				System.out.println("jah existe o mac");
				return key;

			}
		} catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException
				| UnrecoverableKeyException e) {
			if (e instanceof IOException) {
				if (e.getMessage().contains("password was incorrect")) {
					System.out.println("Password errada");
					System.exit(-1);
				}
			}
			e.printStackTrace();
		}
		return null;

	}

}
