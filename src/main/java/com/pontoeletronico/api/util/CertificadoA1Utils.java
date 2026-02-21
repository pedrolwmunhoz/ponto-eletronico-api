package com.pontoeletronico.api.util;

import java.io.ByteArrayInputStream;
import java.security.MessageDigest;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;

/**
 * Extrai data de expiração e hash de certificado A1 (PFX/P12). ICP-Brasil.
 * Front envia apenas o arquivo; o backend obtém a data do cert.
 */
public final class CertificadoA1Utils {

    private CertificadoA1Utils() {}

    /**
     * Lê o arquivo PFX/P12 e retorna a data de expiração do primeiro certificado da cadeia.
     * @param pfxBytes conteúdo do arquivo .pfx ou .p12
     * @param senha senha do keystore (obrigatória na maioria dos A1)
     * @return data de expiração em LocalDateTime (fuso do sistema)
     * @throws IllegalArgumentException se arquivo inválido ou senha incorreta
     */
    public static Optional<LocalDateTime> extrairDataExpiracao(byte[] pfxBytes, char[] senha) {
        if (pfxBytes == null || pfxBytes.length == 0) {
            return Optional.empty();
        }
        char[] pwd = senha != null ? senha : new char[0];
        try {
            KeyStore ks = KeyStore.getInstance("PKCS12");
            ks.load(new ByteArrayInputStream(pfxBytes), pwd);
            String alias = ks.aliases().hasMoreElements() ? ks.aliases().nextElement() : null;
            if (alias == null) {
                throw new IllegalArgumentException("Certificado inválido ou senha incorreta.");
            }
            java.security.cert.Certificate cert = ks.getCertificate(alias);
            if (!(cert instanceof X509Certificate x509)) {
                throw new IllegalArgumentException("Certificado inválido ou senha incorreta.");
            }
            java.util.Date notAfter = x509.getNotAfter();
            if (notAfter == null) {
                throw new IllegalArgumentException("Certificado inválido ou senha incorreta.");
            }
            Instant instant = notAfter.toInstant();
            return Optional.of(LocalDateTime.ofInstant(instant, ZoneId.systemDefault()));
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("Certificado inválido ou senha incorreta.", e);
        }
    }

    /**
     * Gera SHA-256 (hex) do certificado X.509 para auditoria e fingerprint.
     * Gera sobre o cert.getEncoded(), não sobre o .pfx inteiro.
     */
    public static Optional<String> extrairCertificadoHash(byte[] pfxBytes, char[] senha) {
        if (pfxBytes == null || pfxBytes.length == 0) {
            return Optional.empty();
        }
        char[] pwd = senha != null ? senha : new char[0];
        try {
            KeyStore ks = KeyStore.getInstance("PKCS12");
            ks.load(new ByteArrayInputStream(pfxBytes), pwd);
            String alias = ks.aliases().hasMoreElements() ? ks.aliases().nextElement() : null;
            if (alias == null) return Optional.empty();
            java.security.cert.Certificate cert = ks.getCertificate(alias);
            if (!(cert instanceof X509Certificate x509)) return Optional.empty();
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(x509.getEncoded());
            return Optional.of(HexFormat.of().formatHex(hash));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /** OID do CNPJ no subject (ICP-Brasil). Retorna só dígitos (14 caracteres) ou empty. */
    public static Optional<String> extrairCnpjCertificado(byte[] pfxBytes, char[] senha) {
        if (pfxBytes == null || pfxBytes.length == 0) return Optional.empty();
        char[] pwd = senha != null ? senha : new char[0];
        try {
            KeyStore ks = KeyStore.getInstance("PKCS12");
            ks.load(new ByteArrayInputStream(pfxBytes), pwd);
            String alias = ks.aliases().hasMoreElements() ? ks.aliases().nextElement() : null;
            if (alias == null) return Optional.empty();
            java.security.cert.Certificate cert = ks.getCertificate(alias);
            if (!(cert instanceof X509Certificate x509)) return Optional.empty();
            // 1) OID ICP-Brasil (2.16.76.1.3.1); 2) fallback: 14 dígitos no subject (ex.: CN=Empresa Teste 12345678000190)
            String subject = x509.getSubjectX500Principal().getName();
            Optional<String> cnpj = extrairCnpjDoSubject(subject);
            if (cnpj.isPresent()) return cnpj;
            try {
                String rfc2253 = x509.getSubjectX500Principal().getName("RFC2253");
                if (!rfc2253.equals(subject)) cnpj = extrairCnpjDoSubject(rfc2253);
                if (cnpj.isPresent()) return cnpj;
            } catch (Exception ignored) { }
            return extrairCnpj14DigitosDoSubject(subject);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /** Procura OID ICP-Brasil do CNPJ (2.16.76.1.3.1 ou OID.2.16.76.1.3.1) no subject. */
    private static Optional<String> extrairCnpjDoSubject(String subject) {
        if (subject == null || subject.isEmpty()) return Optional.empty();
        java.util.regex.Pattern p = java.util.regex.Pattern.compile("(?:2\\.16\\.76\\.1\\.3\\.1|OID\\.2\\.16\\.76\\.1\\.3\\.1)\\s*=\\s*([^,\\\\]+)");
        java.util.regex.Matcher m = p.matcher(subject);
        if (!m.find()) return Optional.empty();
        String cnpjVal = m.group(1).trim();
        String apenasDigitos = cnpjVal.replaceAll("\\D", "");
        return (apenasDigitos.length() == 14) ? Optional.of(apenasDigitos) : Optional.empty();
    }

    /** Fallback: qualquer sequência de 14 dígitos no subject (ex.: CN=Empresa Teste 12345678000190). */
    private static Optional<String> extrairCnpj14DigitosDoSubject(String subject) {
        if (subject == null || subject.isEmpty()) return Optional.empty();
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("\\d{14}").matcher(subject);
        return m.find() ? Optional.of(m.group()) : Optional.empty();
    }

    /** Resultado da assinatura com A1: assinatura em Base64 e número de série do certificado. */
    public static record AssinaturaA1Result(String assinaturaBase64, String certificadoSerial) {}

    /**
     * Assina digitalmente o payload com o certificado A1 (chave privada). ICP-Brasil.
     * @param pfxBytes conteúdo do arquivo .pfx/.p12
     * @param senha senha do keystore
     * @param payload bytes a assinar (ex.: registroId|createdAt|usuarioId|lat|long)
     * @return assinatura Base64 e serial do certificado, ou empty se falhar
     */
    public static Optional<AssinaturaA1Result> assinarPayload(byte[] pfxBytes, char[] senha, byte[] payload) {
        if (pfxBytes == null || pfxBytes.length == 0 || payload == null) return Optional.empty();
        char[] pwd = senha != null ? senha : new char[0];
        try {
            KeyStore ks = KeyStore.getInstance("PKCS12");
            ks.load(new ByteArrayInputStream(pfxBytes), pwd);
            String alias = ks.aliases().hasMoreElements() ? ks.aliases().nextElement() : null;
            if (alias == null) return Optional.empty();
            PrivateKey privateKey = (PrivateKey) ks.getKey(alias, pwd);
            java.security.cert.Certificate cert = ks.getCertificate(alias);
            if (privateKey == null || !(cert instanceof X509Certificate x509)) return Optional.empty();
            Signature sig = Signature.getInstance("SHA256withRSA");
            sig.initSign(privateKey);
            sig.update(payload);
            byte[] signatureBytes = sig.sign();
            String assinaturaBase64 = Base64.getEncoder().encodeToString(signatureBytes);
            String certificadoSerial = x509.getSerialNumber().toString();
            return Optional.of(new AssinaturaA1Result(assinaturaBase64, certificadoSerial));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Valida a assinatura do registro de ponto.
     * Quem valida precisa do mesmo payload que foi assinado (mesma string em UTF-8) e do certificado (chave pública).
     *
     * <p>Payload usado neste projeto: {@code registroId + "|" + createdAt + "|" + usuarioId + "|" + lat + "|" + lon}
     * (lat/long podem ser vazios se não havia geo).
     *
     * <p>Com o PFX (e senha): usa este método. Com apenas o certificado X.509 (ex.: .cer): use
     * {@link #verificarAssinatura(java.security.cert.X509Certificate, byte[], String)}.
     *
     * @return true se a assinatura é válida para o payload e o cert do keystore
     */
    public static boolean verificarAssinatura(byte[] pfxBytes, char[] senha, byte[] payload, String assinaturaBase64) {
        if (pfxBytes == null || payload == null || assinaturaBase64 == null || assinaturaBase64.isBlank()) return false;
        char[] pwd = senha != null ? senha : new char[0];
        try {
            KeyStore ks = KeyStore.getInstance("PKCS12");
            ks.load(new ByteArrayInputStream(pfxBytes), pwd);
            String alias = ks.aliases().hasMoreElements() ? ks.aliases().nextElement() : null;
            if (alias == null) return false;
            X509Certificate cert = (X509Certificate) ks.getCertificate(alias);
            return cert != null && verificarAssinatura(cert, payload, assinaturaBase64);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Valida a assinatura usando apenas o certificado (chave pública). Não precisa da senha do PFX.
     * Útil quando se armazena ou recebe só o .cer.
     */
    public static boolean verificarAssinatura(X509Certificate cert, byte[] payload, String assinaturaBase64) {
        if (cert == null || payload == null || assinaturaBase64 == null || assinaturaBase64.isBlank()) return false;
        try {
            byte[] signatureBytes = Base64.getDecoder().decode(assinaturaBase64);
            if (signatureBytes == null || signatureBytes.length == 0) return false;
            Signature sig = Signature.getInstance("SHA256withRSA");
            sig.initVerify(cert);
            sig.update(payload);
            return sig.verify(signatureBytes);
        } catch (Exception e) {
            return false;
        }
    }
}
