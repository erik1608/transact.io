package com.snee.transactio.crypto;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

public class AesGcmCipher {
	private static final Logger LOG = LogManager.getLogger(AesGcmCipher.class);
	private static final SecureRandom secureRandom = new SecureRandom();

	private final SecretKey secretKey;
	private int block_size = 0;
	Cipher cipher = null;

	public AesGcmCipher(SecretKey secretKey) {
		this.secretKey = secretKey;
		try {
			cipher = Cipher.getInstance("AES/GCM/NoPadding");
			this.block_size = cipher.getBlockSize();
		} catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
			LOG.error("Failed to get the requested crypto that implements \"AES/GCM/NoPadding\" transformation.", e);
		}
	}

	/**
	 * Encrypt a plain string
	 *
	 * @param strInput the input plain string.
	 * @param padding  boolean indicating whether to add padding or not.
	 * @return returns encrypted Base64 encoded string if successful
	 * @throws GeneralSecurityException if the encryption is failed.
	 */
	public String encrypt(String strInput, boolean padding) throws GeneralSecurityException {
		byte[] input = strInput.getBytes(StandardCharsets.UTF_8);

		if (padding) {
			input = addPadding(input);
		}

		byte[] iv = getSecureRandom(cipher);

		this.cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(iv.length * Byte.SIZE, iv));
		byte[] encrypted = cipher.doFinal(input);

		byte[] output = new byte[iv.length + encrypted.length];
		// output = IV + encrypted
		System.arraycopy(iv, 0, output, 0, iv.length);
		System.arraycopy(encrypted, 0, output, iv.length, encrypted.length);
		return Base64.getUrlEncoder().encodeToString(output);
	}

	/**
	 * Decrypts an encrypted base64 encoded string
	 *
	 * @param strInput encrypted base64 encoded string.
	 * @param padding  boolean indicating whether the data has padding or not.
	 * @return returns plain string if successful
	 * @throws GeneralSecurityException if the decryption is failed.
	 */
	public String decrypt(String strInput, boolean padding) throws GeneralSecurityException {
		byte[] input = Base64.getUrlDecoder().decode(strInput);

		byte[] iv = Arrays.copyOfRange(input, 0, cipher.getBlockSize());
		byte[] encrypted = Arrays.copyOfRange(input, iv.length, input.length);
		cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(iv.length * Byte.SIZE, iv));
		byte[] decrypted = cipher.doFinal(encrypted);

		if (padding) {
			decrypted = removePadding(decrypted);
		}

		return new String(decrypted, StandardCharsets.UTF_8);
	}

	/**
	 * Retrieve a cryptographically strong random number
	 *
	 * @param cipher - instance of cryptographic crypto for encryption and
	 *               decryption
	 * @return returns random number equal to the 16 block size
	 */
	private byte[] getSecureRandom(Cipher cipher) {
		byte[] random = new byte[cipher.getBlockSize()];
		secureRandom.nextBytes(random);
		return random;
	}

	/**
	 * Adds padding bytes to the end of input.
	 *
	 * @param input - the input buffer
	 * @return returns padded buffer
	 */
	private byte[] addPadding(byte[] input) {
		int padLen = block_size - (input.length % block_size);
		byte[] padOutput = new byte[input.length + padLen];
		System.arraycopy(input, 0, padOutput, 0, input.length);
		byte paddingOctet = (byte) (padLen & 0xff);
		for (int i = 0; i < padLen; i++) {
			padOutput[input.length + i] = paddingOctet;
		}
		return padOutput;
	}

	/**
	 * Remove padding bytes from the end of input.
	 *
	 * @param input - the input buffer
	 * @return returns buffer without padding.
	 */
	private byte[] removePadding(byte[] input) throws BadPaddingException {
		byte lastByte = input[input.length - 1];
		int padValue = (int) lastByte & 0xff;
		if ((padValue < 0x01) || (padValue > block_size)) {
			throw new BadPaddingException();
		}

		int outLen = input.length - ((int) lastByte & 0xff);
		byte[] output = new byte[outLen];
		System.arraycopy(input, 0, output, 0, outLen);
		return output;
	}
}
