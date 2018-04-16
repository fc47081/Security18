package com.sc010.manusers;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class Test {

	public static void main(String[] args) {

		String password = "123";

		SecureRandom sr = new SecureRandom();
		byte[] salt = "}SâÅï¿m×{\"»–ƒ2".getBytes();
		//sr.nextBytes(salt);
		System.out.println("Hashing our password");
		byte[] enc = hashPassword(password.toCharArray(), salt, 20, 256);
		for (byte b : enc) {
			System.out.print(b);
		}
		System.out.println("");
		System.out.println("Checking if expected password is right");
		if (isExpectedPassword("´Ñð3ÚŒ‰f&íŽ¡­òCš©¨¸¤¸v_agß‰".toCharArray(), salt, enc)) {
			System.out.println("Password correct");
		} else {
			System.out.println("Password incorrect");
		}

	}

	public static byte[] hashPassword(final char[] password, final byte[] salt, final int iterations,
			final int keyLength) {

		try {
			SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
			PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, keyLength);
			SecretKey key = skf.generateSecret(spec);
			byte[] res = key.getEncoded();
			return res;

		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Returns true if the given password and salt match the hashed value, false
	 * otherwise.<br>
	 * Note - side effect: the password is destroyed (the char[] is filled with
	 * zeros)
	 *
	 * @param password
	 *            the password to check
	 * @param salt
	 *            the salt used to hash the password
	 * @param expectedHash
	 *            the expected hashed value of the password
	 *
	 * @return true if the given password and salt match the hashed value, false
	 *         otherwise
	 */
	public static boolean isExpectedPassword(char[] password, byte[] salt, byte[] expectedHash) {
		byte[] pwdHash = hashPassword(password, salt, 20, 256);
		Arrays.fill(password, Character.MIN_VALUE);
		if (pwdHash.length != expectedHash.length)
			return false;
		for (int i = 0; i < pwdHash.length; i++) {
			if (pwdHash[i] != expectedHash[i])
				return false;
		}
		return true;
	}
}
