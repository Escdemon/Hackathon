package com.cgi.commons.security;

import java.security.NoSuchAlgorithmException;

import com.cgi.business.application.SecurityManager;
import com.cgi.commons.utils.TechnicalException;

/**
 * Utilitary tools for Security.
 */
public class SecurityUtils {

	/** Private constructor for utility class. */
	private SecurityUtils() {
		// Do not instantiate this.
	}

	/** SecurityManager singleton. */
	private static AbstractSecurityManager sm = new SecurityManager();

	/** 
	 * Gets security manager current instance.
	 * 
	 * @return the security manageR.
	 */ 
	public static AbstractSecurityManager getSecurityManager() {
		return sm;
	}

	/** 
	 * Computes Hash with algorithm.
	 * @param x	String to hash
	 * @param algo Kind of algo (for exemple SHA-1)
	 * @return hash of x (depends of the algo)
	 */
	private static byte[] computeHash(String x, String algo) {
		try {
			java.security.MessageDigest d = null;
			d = java.security.MessageDigest.getInstance(algo);
			d.reset();
			d.update(x.getBytes());
			return d.digest();
		} catch (NoSuchAlgorithmException ex) {
			throw new TechnicalException("Impossible to instantiate " + algo + " Algorithm");
		}
	}

	/**
	 * Converts a byte array into string.
	 * @param bytes	to convert	
	 * @return String representing bytes
	 */
	private static String byteArrayToHexString(byte[] bytes) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < bytes.length; i++) {
			if ((bytes[i] & 0xff) < 0x10) {
				sb.append("0");
			}
			sb.append(Long.toString(bytes[i] & 0xff, 16));
		}
		return sb.toString();
	}

	/**
	 * Hashes a password using SHA-1 algorithm.
	 * @param password	Password to hash
	 * @return	Hashed password
	 */
	public static String hash(String password) {
		return byteArrayToHexString(computeHash(password, "SHA-1"));
	}
	
	/**
	 * Hashes a password using MD5 algorithm.
	 * @param password	Password to hash
	 * @return	Hashed password
	 */
	public static String hashMD5(String password) {
		return byteArrayToHexString(computeHash(password, "MD5"));
	}

}
