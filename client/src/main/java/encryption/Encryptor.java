package encryption;

import org.apache.commons.codec.binary.Base64;


import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import java.security.*;
/**
 * Created by alexanderweiss
 * Class for encrypting our translations
 */
public class Encryptor {

    private Cipher aesCipherForEncryption;
    final int AES_KEYLENGTH = 128;	// change this as desired for the security level you want
    private byte[] iv = new byte[AES_KEYLENGTH / 8];
    private static final String HEX    = "0123456789ABCDEF";
    private static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(Encryptor.class);

    public Encryptor(){
        generateInitializationVector();
    }

    /**
     * Generate an random 128 bit aes key
     * @return SecretKey
     */
    public static SecretKey generateKey(){
        logger.info("Generate secret key for encryption");
        KeyGenerator keyGen = null;
        SecretKey secretKey = null;
        try {
            keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(128);
            secretKey = keyGen.generateKey();
        } catch (NoSuchAlgorithmException ex) {
            logger.error("No such algorithm implemented " + "\n" + ex);
        }
        return secretKey;
    }

    /**
     * Generate the initialization vector for creating encrypted messages
     */
    private void generateInitializationVector(){// Save the IV bytes or send it in plaintext with the encrypted data so you can decrypt the data later
        logger.info("Generate secret key for initialization vector");
        SecureRandom prng = new SecureRandom();
        prng.nextBytes(iv);
    }

    /**
     * Set up the cipher for the encryptor
     */
    private void createCipher(){
        logger.info("Create AES/CBC/PKCS5PADDING cipher");
        try {
            aesCipherForEncryption = Cipher.getInstance("AES/CBC/PKCS5PADDING"); // Must specify the mode explicitly as most JCE providers default to ECB mode!! ECB is unsafe
        } catch (NoSuchAlgorithmException ex) {
            logger.error("No such algorithm implemented " + "\n" + ex);
        } catch (NoSuchPaddingException ex) {
            logger.error("No such padding implemented " + "\n" + ex);
        }
    }

    /**
     * Encrypt a string
     * @param textToEncrypt
     * @param secretKey
     * @return String
     */
    public String encrypt(String textToEncrypt, SecretKey secretKey){
        try {
            createCipher();
            aesCipherForEncryption.init(Cipher.ENCRYPT_MODE, secretKey,new IvParameterSpec(iv));
            byte[] byteDataToEncrypt = textToEncrypt.getBytes();
            byte[] byteCipherText = aesCipherForEncryption.doFinal(byteDataToEncrypt);
            return Base64.encodeBase64String(byteCipherText);
        } catch (InvalidAlgorithmParameterException ex) {
            logger.error("Invalid algorithm parameter " + "\n" + ex);
        } catch (InvalidKeyException ex) {
            logger.error("Invalid key parameter " + "\n" + ex);
        } catch (BadPaddingException ex) {
            logger.error("bad padding " + "\n" + ex);
        } catch (IllegalBlockSizeException ex) {
            logger.error("Illegal block size " + "\n" + ex);
        }
        return null;
    }

    /**
     * Decrypt a string
     * @param textToDecrypt
     * @param secretKey
     * @return String
     */
    public String decrypt(String textToDecrypt, Key secretKey){
        try{
            logger.info("Decrypt message");
            Cipher aesCipherForDecryption = Cipher.getInstance("AES/CBC/PKCS5PADDING"); // Must specify the mode explicitly as most JCE providers default to ECB mode!!s
            aesCipherForDecryption.init(Cipher.DECRYPT_MODE, secretKey,new IvParameterSpec(iv));
            byte[] byteDecryptedText = aesCipherForDecryption.doFinal(new Base64().decode(textToDecrypt.getBytes()));
            return new String(byteDecryptedText);
        }catch (Exception ex){
            logger.error("Error with decrypting the message. " + "\n" +ex);
        }
        return null;
    }

    /**
     * Get a hex representation from raw data
     * @param raw
     * @return String
     * Credit: https://stackoverflow.com/questions/2817752/java-code-to-convert-byte-to-hexadecimal/26975031#26975031
     */
    public static String getHex(byte[] raw) {
        final StringBuilder hex = new StringBuilder(2 * raw.length);
        for (final byte b : raw) {
            hex.append(HEX.charAt((b & 0xF0) >> 4)).append(HEX.charAt((b & 0x0F)));
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
