package com.sc010.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.stream.Collectors;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
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
	 * Gera um random salt com um array de bytes randomizado de forma segura.
	 * 
	 * @param sr
	 *            Gerador de numeros random seguro
	 * @return byte[] ivBytes
	 */
	public byte[] saltGenerator(SecureRandom sr) {
		byte[] salt = new byte[16];
		sr.nextBytes(salt);
		return salt;
	}

	/**
	 * Gera um random IV com um array de bytes randomizado de forma segura.
	 * 
	 * @param sr
	 *            Gerador de numeros random seguro
	 * @return byte[] salt
	 */
	public byte[] ivGenerator(SecureRandom sr) {
		byte[] ivBytes = new byte[16];
		sr.nextBytes(ivBytes);
		return ivBytes;
	}

	/**
	 * Decifra a password dada utilizando o salt dado.
	 * 
	 * @param password
	 * @param salt
	 * @return String of password decifrada
	 */
	public static String decifrar(String password, byte[] salt) {
		// badajoz ver como passar o iv
		byte[] ivBytes = null;
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
			System.out.println("houve algum erro ao decifrar");
		}

		return decrypted;

	}

	
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
				// Se não existe dar load com path a null e depois store para escrever no
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
