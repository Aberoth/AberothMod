package AberothMod;

import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.PublicKey;

/**
 * Client user identity sent to server.
 */
public class User implements Serializable {

    /**
     * Client user's MD5 encrypted Aberoth player name.
     */
    public String MD5PlayerName;

    /**
     * Client user's public key.
     */
    public PublicKey publicKey;

    /**
     * For server to implement. (Server's Socket OutputStream associated with this client user.)
     */
    public ObjectOutputStream outputStream;

    /**
     * Create a new User.
     *
     * @param _MD5PlayerName : MD5 encrypted Aberoth player name
     * @param _publicKey     : User's public key
     */
    public User(String _MD5PlayerName, PublicKey _publicKey) {
        // initialize variables
        MD5PlayerName = _MD5PlayerName;
        publicKey = _publicKey;
    }

}