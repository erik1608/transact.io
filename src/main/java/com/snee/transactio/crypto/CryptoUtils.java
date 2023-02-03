package com.snee.transactio.crypto;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class CryptoUtils {
	private static final SecureRandom mSecureRandom = new SecureRandom();

	public static PublicKey rsaPemToPublicKey(String pemPublicKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		X509EncodedKeySpec X509publicKey = new X509EncodedKeySpec(Base64.getDecoder().decode(pemPublicKey
				.replaceAll("-----BEGIN PUBLIC KEY-----\n", "")
				.replaceAll("-----END PUBLIC KEY-----", "")
				.replaceAll("\n", "")));
		return keyFactory.generatePublic(X509publicKey);
	}

	public static boolean isRSAPublicKey(String key) {
		try {
			rsaPemToPublicKey(key);
			return true;
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			return false;
		}
	}

	public static String generateChallenge() {
		byte[] challenge = new byte[32];
		mSecureRandom.nextBytes(challenge);
		return Base64.getEncoder().encodeToString(challenge);
	}
}
