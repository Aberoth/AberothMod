package AberothMod.Utils;

import javax.crypto.Cipher;
import java.io.*;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;

public class RSAEncryption {

    /**
     * Client user's public and private keys.
     */
    private static PrivateKey privateKey;
    public static PublicKey publicKey;

    /**
     * Files of locally stored client user's public and private keys.
     */
    private static final File PRIVATE_KEY_FILE = new File("private.key");
    private static final File PUBLIC_KEY_FILE = new File("public.key");

    /**
     * Get public and private keys from local files.
     */
    public static void GenerateKey() {
        try {
            if(PRIVATE_KEY_FILE.exists() && PUBLIC_KEY_FILE.exists()) {
                System.out.println("Getting RSA 2k-bit key pair.");

                // get keys from local files
                ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(PRIVATE_KEY_FILE));
                privateKey = (PrivateKey) inputStream.readObject();

                inputStream = new ObjectInputStream(new FileInputStream(PUBLIC_KEY_FILE));
                publicKey = (PublicKey) inputStream.readObject();
            }

            /**
             * Or create them if they don't exist.
             */
            else {
                System.out.println("Generating RSA 2k-bit key pair.");

                // create a 2k-bit RSA keypair
                final KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
                keyGen.initialize(2048);
                final KeyPair key = keyGen.generateKeyPair();

                // retrieve the public and private keys
                privateKey = key.getPrivate();
                publicKey = key.getPublic();

                // store keys in local files
                ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(PRIVATE_KEY_FILE));
                outputStream.writeObject(privateKey);
                outputStream.close();

                outputStream = new ObjectOutputStream(new FileOutputStream(PUBLIC_KEY_FILE));
                outputStream.writeObject(publicKey);
                outputStream.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Encrypt plain text using a public key.
     *
     * @param text : Original plain text
     * @param key  : The public key
     * @return Encrypted text
     */
    public static byte[] Encrypt(String text, PublicKey key) {
        byte[] cipherText = null;
        try {
            final Cipher cipher = Cipher.getInstance("RSA");

            // encrypt the plain text using a public key
            cipher.init(Cipher.ENCRYPT_MODE, key);
            cipherText = cipher.doFinal(text.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cipherText;
    }

    /**
     * Decrypt encrypted text using the private key.
     *
     * @param text : Encrypted text
     * @return Plain text
     */
    public static String Decrypt(byte[] text) {
        byte[] decryptedText = null;
        try {
            final Cipher cipher = Cipher.getInstance("RSA");

            // decrypt the encrypted text using the private key
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            decryptedText = cipher.doFinal(text);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return new String(decryptedText);
    }

}