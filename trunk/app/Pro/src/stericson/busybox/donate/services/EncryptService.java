package stericson.busybox.donate.services;

import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import android.util.Base64;

public class EncryptService {
    public static Cipher prepareCipher(int mode) {
        String ENCRYPTION = "AES",
                ALGORITHM = "SHA1PRNG",
                SEED = "BusyB0x_D3v3l0per";

        try {
            KeyGenerator keygen = KeyGenerator.getInstance(ENCRYPTION);

            SecureRandom secrand = SecureRandom.getInstance(ALGORITHM);

            secrand.setSeed(SEED.getBytes());

            keygen.init(128, secrand);

            SecretKey seckey = keygen.generateKey();

            byte[] rawKey = seckey.getEncoded();

            SecretKeySpec skeySpec = new SecretKeySpec(rawKey, ENCRYPTION);

            Cipher cipher = Cipher.getInstance(ENCRYPTION);

            cipher.init(mode, skeySpec);

            return cipher;
        } catch (Exception e) {
            return null;
        }
    }

    public static String encrypt(String text) {
        try {
            Cipher cipher = prepareCipher(Cipher.ENCRYPT_MODE);

            String encrypted = Base64.encodeToString(cipher.doFinal(text.getBytes()), Base64.DEFAULT);

            return encrypted;
        } catch (Exception e) {
            return null;
        }
    }

    public static String decrypt(byte[] text) {
        try {
            Cipher cipher = prepareCipher(Cipher.DECRYPT_MODE);

            String encrypted = new String(cipher.doFinal(text));

            return encrypted;
        } catch (Exception e) {
            return null;
        }
    }
}
