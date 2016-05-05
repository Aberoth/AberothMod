package AberothMod.Utils;

public class MD5Encryption {

    /**
     * Encrypt plain text with MD5 algorithm.
     *
     * @param text : Original plain text
     * @return MD5 encrypted text
     */
    public static String Encrypt(String text) {
        try {
            // encrypt the plain text with MD5 encryption
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            byte[] array = md.digest(text.getBytes());

            // format the encrypted data to hexadecimal and put it in a string
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < array.length; ++i) {
                sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1,3));
            }
            return sb.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

}