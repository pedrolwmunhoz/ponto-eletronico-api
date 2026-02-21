package com.pontoeletronico.api.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

/**
 * Criptografa/descriptografa certificado (PFX) e senha do certificado A1.
 * Chave em app.certificado.encryption-key (Base64, 32 bytes). Mesma chave para cert e senha.
 */
@Component
public class CertificadoSenhaCriptografiaService {

    private static final int KEY_LENGTH = 32;
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;
    private static final String ALG = "AES/GCM/NoPadding";

    private final byte[] keyBytes;

    public CertificadoSenhaCriptografiaService(
            @Value("${app.certificado.encryption-key:}") String encryptionKeyBase64) {
        if (encryptionKeyBase64 == null || encryptionKeyBase64.isBlank()) {
            this.keyBytes = null;
            return;
        }
        byte[] decoded = Base64.getDecoder().decode(encryptionKeyBase64);
        this.keyBytes = (decoded != null && decoded.length >= KEY_LENGTH)
                ? (decoded.length == KEY_LENGTH ? decoded : Arrays.copyOf(decoded, KEY_LENGTH))
                : null;
    }

    /** Criptografa a senha para persistir. Retorna null se senha vazia ou chave não configurada. */
    public String criptografar(String senha) {
        if (senha == null || senha.isBlank() || keyBytes == null) return null;
        try {
            SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");
            byte[] iv = new byte[GCM_IV_LENGTH];
            new SecureRandom().nextBytes(iv);
            Cipher cipher = Cipher.getInstance(ALG);
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
            byte[] ciphertext = cipher.doFinal(senha.getBytes(StandardCharsets.UTF_8));
            ByteBuffer buf = ByteBuffer.allocate(iv.length + ciphertext.length);
            buf.put(iv);
            buf.put(ciphertext);
            return Base64.getEncoder().encodeToString(buf.array());
        } catch (Exception e) {
            return null;
        }
    }

    /** Descriptografa para usar o cert (ex.: assinar AFD). Retorna null se falhar. */
    public String descriptografar(String criptografada) {
        if (criptografada == null || criptografada.isBlank() || keyBytes == null) return null;
        try {
            byte[] decoded = Base64.getDecoder().decode(criptografada);
            if (decoded == null || decoded.length < GCM_IV_LENGTH) return null;
            ByteBuffer buf = ByteBuffer.wrap(decoded);
            byte[] iv = new byte[GCM_IV_LENGTH];
            buf.get(iv);
            byte[] ciphertext = new byte[buf.remaining()];
            buf.get(ciphertext);
            SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");
            Cipher cipher = Cipher.getInstance(ALG);
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
            byte[] plain = cipher.doFinal(ciphertext);
            return new String(plain, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return null;
        }
    }

    /** Criptografa o blob do certificado (PFX) para persistir. Retorna null se falhar ou chave não configurada. */
    public byte[] criptografarBytes(byte[] plain) {
        if (plain == null || plain.length == 0 || keyBytes == null) return null;
        try {
            SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");
            byte[] iv = new byte[GCM_IV_LENGTH];
            new SecureRandom().nextBytes(iv);
            Cipher cipher = Cipher.getInstance(ALG);
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
            byte[] ciphertext = cipher.doFinal(plain);
            ByteBuffer buf = ByteBuffer.allocate(iv.length + ciphertext.length);
            buf.put(iv);
            buf.put(ciphertext);
            return buf.array();
        } catch (Exception e) {
            return null;
        }
    }

    /** Descriptografa o blob do certificado (PFX) para carregar no KeyStore. Retorna null se falhar. */
    public byte[] descriptografarBytes(byte[] encrypted) {
        if (encrypted == null || encrypted.length <= GCM_IV_LENGTH || keyBytes == null) return null;
        try {
            ByteBuffer buf = ByteBuffer.wrap(encrypted);
            byte[] iv = new byte[GCM_IV_LENGTH];
            buf.get(iv);
            byte[] ciphertext = new byte[buf.remaining()];
            buf.get(ciphertext);
            SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");
            Cipher cipher = Cipher.getInstance(ALG);
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
            return cipher.doFinal(ciphertext);
        } catch (Exception e) {
            return null;
        }
    }
}
