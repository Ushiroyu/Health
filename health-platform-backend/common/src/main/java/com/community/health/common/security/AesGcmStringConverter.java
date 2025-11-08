package com.community.health.common.security;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

@Converter
public class AesGcmStringConverter implements AttributeConverter<String, String> {
  private static final String ALG = "AES";
  private static final String CIPHER = "AES/GCM/NoPadding";
  private static final int GCM_TAG_LENGTH = 128; // bits
  private static final int IV_LENGTH = 12; // bytes
  private static final SecureRandom RNG = new SecureRandom();

  private static SecretKey loadKey() {
    String keyB64 = System.getenv("CRYPTO_KEY");
    if (keyB64 == null || keyB64.isBlank()) {
      // fallback: generate ephemeral key (not recommended for prod)
      try {
        KeyGenerator kg = KeyGenerator.getInstance(ALG);
        kg.init(256);
        return kg.generateKey();
      } catch (Exception e) {
        throw new IllegalStateException("Cannot init AES key", e);
      }
    }
    byte[] raw = Base64.getDecoder().decode(keyB64);
    return new SecretKeySpec(raw, ALG);
  }

  private static final SecretKey KEY = loadKey();

  @Override
  public String convertToDatabaseColumn(String attribute) {
    if (attribute == null) return null;
    try {
      byte[] iv = new byte[IV_LENGTH];
      RNG.nextBytes(iv);
      Cipher cipher = Cipher.getInstance(CIPHER);
      cipher.init(Cipher.ENCRYPT_MODE, KEY, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
      byte[] cipherText = cipher.doFinal(attribute.getBytes(StandardCharsets.UTF_8));
      ByteBuffer bb = ByteBuffer.allocate(iv.length + cipherText.length);
      bb.put(iv).put(cipherText);
      return Base64.getEncoder().encodeToString(bb.array());
    } catch (Exception e) {
      throw new IllegalStateException("Encrypt failed", e);
    }
  }

  @Override
  public String convertToEntityAttribute(String dbData) {
    if (dbData == null) return null;
    try {
      byte[] all = Base64.getDecoder().decode(dbData);
      ByteBuffer bb = ByteBuffer.wrap(all);
      byte[] iv = new byte[IV_LENGTH];
      bb.get(iv);
      byte[] cipherText = new byte[bb.remaining()];
      bb.get(cipherText);
      Cipher cipher = Cipher.getInstance(CIPHER);
      cipher.init(Cipher.DECRYPT_MODE, KEY, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
      byte[] plain = cipher.doFinal(cipherText);
      return new String(plain, StandardCharsets.UTF_8);
    } catch (Exception e) {
      // return raw if not in expected format (for legacy data)
      return dbData;
    }
  }
}

