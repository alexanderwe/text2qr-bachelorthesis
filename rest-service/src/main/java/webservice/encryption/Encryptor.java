package webservice.encryption;

import org.apache.commons.codec.binary.Base64;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;

import sun.misc.*;

import java.security.*;

/**
 * Created by alexanderweiss
 * Class for encrypting our translations, same as in the client
 */
public class Encryptor {

    Cipher aesCipherForEncryption;
    private final int AES_KEYLENGTH = 128;	// change this as desired for the security level you want
    private byte[] iv = new byte[AES_KEYLENGTH / 8];
    private static final String    HEXES    = "0123456789ABCDEF";

    /**
     * Default constructor
     */
    public Encryptor(){
    }


    /**
     * Generate an random 128 bit aes key
     * @return
     */
    public static SecretKey generateKey(){
        KeyGenerator keyGen = null;
        SecretKey secretKey = null;
        try {
            keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(128);
            secretKey = keyGen.generateKey();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return secretKey;
    }

    private void generateInitializationVector(){// Save the IV bytes or send it in plaintext with the encrypted data so you can decrypt the data later
        SecureRandom prng = new SecureRandom();
        prng.nextBytes(iv);
    }


    /**
     * Create cipher with AES/CBC/PKCS5PADDING
     */
    private void createCipher(){
        try {
            aesCipherForEncryption = Cipher.getInstance("AES/CBC/PKCS5PADDING"); // Must specify the mode explicitly as most JCE providers default to ECB mode!! ECB is unsafe
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        }
    }

    /**
     * Encrypt text
     * @param textToEncrypt
     * @param secretKey
     * @return
     */
    public String encrypt(String textToEncrypt, SecretKey secretKey){
        try {
            createCipher();
            aesCipherForEncryption.init(Cipher.ENCRYPT_MODE, secretKey,new IvParameterSpec(iv));
            byte[] byteDataToEncrypt = textToEncrypt.getBytes();
            byte[] byteCipherText = aesCipherForEncryption.doFinal(byteDataToEncrypt);

            return new BASE64Encoder().encode(byteCipherText);
        } catch (InvalidAlgorithmParameterException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Decrypt text
     * @param textToDecrypt
     * @param secretKey
     * @return
     */
    public String decrypt(String textToDecrypt, Key secretKey){
        try{
            Cipher aesCipherForDecryption = Cipher.getInstance("AES/CBC/PKCS5PADDING"); // Must specify the mode explicitly as most JCE providers default to ECB mode!!s
            aesCipherForDecryption.init(Cipher.DECRYPT_MODE, secretKey,new IvParameterSpec(iv));
            byte[] byteDecryptedText = aesCipherForDecryption.doFinal(new Base64().decode(textToDecrypt.getBytes()));
            return new String(byteDecryptedText);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }


    /**
     * Get a hex representation from raw data
     * @param raw
     * @return String
     * Credit: https://stackoverflow.com/questions/2817752/java-code-to-convert-byte-to-hexadecimal/26975031#26975031
     */
    private static String getHex(byte[] raw) {
        final StringBuilder hex = new StringBuilder(2 * raw.length);
        for (final byte b : raw) {
            hex.append(HEXES.charAt((b & 0xF0) >> 4)).append(HEXES.charAt((b & 0x0F)));
        }
        return hex.toString();
    }


    /**
     * Convert hex String to to byte array
     * @param s
     * @return byte[]
     * Credit: https://stackoverflow.com/questions/140131/convert-a-string-representation-of-a-hex-dump-to-a-byte-array-using-java/140861#140861
     */
    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    public String getIVHex (){
        return getHex(iv);
    }


    public void setIv(byte [] iv){
        this.iv = iv;
    }

}
