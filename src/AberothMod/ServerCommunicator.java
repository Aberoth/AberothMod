package AberothMod;

import AberothMod.Aberoth.World;
import AberothMod.Utils.MD5Encryption;
import AberothMod.Utils.RSAEncryption;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.PublicKey;
import java.util.Base64;

public class ServerCommunicator extends Thread {

    /**
     * Socket connection variables.
     */
    private Socket socket;
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;

    /**
     * This client's identity.
     */
    private User clientUser;

    /**
     * Create the ServerCommunicator.
     */
    public ServerCommunicator() {
        try {
            // connect to server
            socket = new Socket("52.3.93.214", 9090);

            System.out.println("Connected to EC2 Server!");

            // initialize streams
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            inputStream = new ObjectInputStream(socket.getInputStream());

            // create this client's identity and send it to the server
            clientUser = new User(MD5Encryption.Encrypt(World.myPlayer.name), RSAEncryption.publicKey);
            SendToServer(clientUser);

            // start thread handler
            (new Thread(this)).start();
        } catch(Exception e) {
            System.out.println("Couldn't connect to server!");
        }
    }

    /**
     * Send object to server.
     *
     * @param obj : Object to send to server
     */
    public void SendToServer(Object obj)
    {
        try {
            outputStream.writeObject(obj);
            outputStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Disconnects from server.
     */
    public void Disconnect() {
        try {
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Thread handler of messages received from the server.
     */
    public void run()
    {
        try {
            while(Mods.isRunning) {

                // receive Object from server
                Object obj = inputStream.readObject();

                // if the Object is a PublicKey - it's the server's reply to a /getPublicKey request
                if (obj instanceof PublicKey) {
                    PublicKey publicKey = (PublicKey) obj;
                    Mods.privateMessaging.CachePublicKey(publicKey);
                    Mods.privateMessaging.EncryptAndPM(publicKey);

                // if the Object is a String
                } else if (obj instanceof String) {
                    String string = (String) obj;

                    // if the String is the MOTD - it's the server giving us a message to show the user upon logging in.
                    if (string.matches("^MOTD:\n(?s:.+)")) {
                        Mods.privateMessaging.MOTD = string;
                        Mods.privateMessaging.ShowMOTD(true);
                    }
                    // if the String matches "PlayerOffline" - it's the server's reply to a /getPublicKey or /pm request telling us playerToMessage is offline
                    else if (string.matches("PlayerOffline")) {
                        Mods.privateMessaging.PlayerIsOffline();
                    }
                    // if the String matches "MessageSent" - it's the server's reply to a /pm request telling us the message was successfully sent to playerToMessage
                    else if (string.matches("MessageSent")) {
                        Mods.privateMessaging.MessageSentSuccessfully();
                    }
                    // else - it's the server sending us a message sent by another Aberoth player
                    else {
                        // decrypt message with private key
                        String message = RSAEncryption.Decrypt(Base64.getDecoder().decode(string.getBytes()));

                        Mods.privateMessaging.DisplayPrivateMessageOnScreen(message);
                    }

                // if the Object is not recognized
                } else if(obj != null)
                    System.out.println("Received a message of unknown type: " + obj.getClass());

                Thread.sleep(500);

            }
        } catch (Exception e) {
            // internet disconnect?
            try {
                e.printStackTrace();
                socket.close();
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }

}