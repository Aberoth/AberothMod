package AberothMod;

import AberothMod.Aberoth.World;
import AberothMod.Utils.MD5Encryption;
import AberothMod.Utils.RSAEncryption;

import java.security.PublicKey;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PrivateMessaging extends Thread  {

    /**
     * Message queue containing plain-text private messages to encrypt and send to server.
     */
    private ArrayList<String> messageQueue = new ArrayList<>();

    /**
     * Add private message to message queue.
     *
     * @param playerName : Plain text of receiver's Aberoth player name
     * @param message    : Plain text of message to send
     */
    public void AddPrivateMessage(String playerName, String message) {
        messageQueue.add(playerName+" "+message);
    }

    /**
     * Cached incoming player public keys.
     */
    private Map<String, PublicKey> userPublicKeys = new Hashtable<>();

    /**
     * Processes private message from message queue.
     *
     * @param message - Private message
     */
    public void ProcessMessage(String message) {
        // split message into playerToMessage and messageToSend
        String[] splitter = message.split(" ", 2);
        playerToMessage = splitter[0];
        messageToSend = splitter[1];

        // message is about to be sent
        isSendingMessage = true;

        // encrypt playerToMessage with MD5 algorithm
        String MD5PlayerName = MD5Encryption.Encrypt(playerToMessage);

        // if playerToMessage's public key is not cached, get it with a /getPublicKey server request
        if (!userPublicKeys.containsKey(MD5PlayerName))
            GetPublicKey(MD5PlayerName);
        // else encrypt with cached public key and send a /pm server request
        else
            EncryptAndPM(userPublicKeys.get(MD5PlayerName));

    }

    /**
     * Variables to handle delay between server requests /getPublicKey [encryptedName] and /pm [encryptedName] [encryptedMessage].
     */
    private boolean isSendingMessage = false;
    private String playerToMessage = "";
    private String messageToSend = "";

    /**
     * Send server request for an Aberoth player's public key.
     *
     * Sends to server:
     *      MD5 encrypted playerToMessage.
     *
     * @param MD5PlayerName : MD5 encrypted playerToMessage
     */
    public void GetPublicKey(String MD5PlayerName) {
        // send getPublicKey request to server
        Mods.serverCommunicator.SendToServer("/getPublicKey " + MD5PlayerName);
    }

    /**
     * Encrypt playerName and messageToSend with playerToMessage's public key.
     * Send server request for encrypted message to be sent to playerToMessage.
     *
     * Sends to server:
     *      RSA 2k-bit encrypted messageToSend.
     *      RSA 2k-bit encrypted playerName.
     *      MD5 encrypted playerToMessage.
     *
     * @param publicKey : playerToMessage's public key
     */
    public void EncryptAndPM(PublicKey publicKey) {
        // encrypt playerName and messageToSend with RSA 2k-bit algorithm
        byte[] encryptedMessage = RSAEncryption.Encrypt((World.myPlayer.name+": "+messageToSend), publicKey);

        // encrypt playerToMessage with MD5 algorithm
        String byteArrayStr = new String(Base64.getEncoder().encode(encryptedMessage));

        // send pm request to server
        Mods.serverCommunicator.SendToServer("/pm "+ MD5Encryption.Encrypt(playerToMessage)+" "+byteArrayStr);
    }

    /**
     * Cache playerToMessage's public key.
     *
     * @param publicKey : playerToMessage's public key
     */
    public void CachePublicKey(PublicKey publicKey) {
        userPublicKeys.put(MD5Encryption.Encrypt(Mods.privateMessaging.playerToMessage), publicKey);
    }

    /**
     * Called by ServerCommunicator when message doesn't go through.
     */
    public void PlayerIsOffline() {
        isSendingMessage = false;
        Mods.MessageUpdate("\"" + playerToMessage + "\" is either offline or not using AberothMod.");
    }

    /**
     * Called by ServerCommunicator when message is successfully sent.
     */
    public void MessageSentSuccessfully() {
        isSendingMessage = false;
    }

    /**
     * Last player to private message user.
     */
    private String lastPlayerWhoMessaged = "";

    /**
     * Set last player who sent a private message to user.
     *
     * @param playerName : Plain-text Aberoth player name
     */
    public void SetLastPlayerWhoMessaged(String playerName) {
        lastPlayerWhoMessaged = playerName;
    }

    /**
     * Get last player who sent a private message to user.
     *
     * @return Last player to private message user
     */
    public String GetLastPlayerWhoMessaged() {
        return lastPlayerWhoMessaged;
    }

    /**
     * User's private message ignore list.
     */
    public List<String> ignoreList = new ArrayList<>();

    /**
     * Received private messages and the time they were received. Used for displaying messages on-screen.
     */
    private ConcurrentSkipListMap<Long, String> onScreenPrivateMessages = new ConcurrentSkipListMap<>();

    /**
     * Check and add plain-text message to onScreenPrivateMessages Map.
     *
     * @param message : The plain-text message received
     */
    public void DisplayPrivateMessageOnScreen(String message) {
        // get the name of the Aberoth player who sent the message
        String messageSender = message.substring(0, message.indexOf(':'));

        // if messageSender is not being ignored
        if(!ignoreList.contains(messageSender)) {
            // check if MOTD is currently showing
            if (showingMessage) {
                // clear it if it is
                ClearPrivateMessages();
                showingMessage = false;
            }

            // set lastPlayerWhoMessaged
            Mods.privateMessaging.SetLastPlayerWhoMessaged(messageSender);

            // get current time in milliseconds
            Long milliTime = System.currentTimeMillis();

            // if the message is too long for half the screen
            if(PaintHook.aberothFontMetrics.stringWidth(message) > (World.screenWidth)/2) {
                ArrayList<String> messages = new ArrayList<>();

                // divide it up a bit
                while(PaintHook.aberothFontMetrics.stringWidth(message) > (World.screenWidth)/2) {
                    String messageToCut = "";

                    for (int i = 1; PaintHook.aberothFontMetrics.stringWidth(messageToCut) <= (World.screenWidth) / 2; i++) {
                        messageToCut = message.substring(0, i);
                    }

                    int positionToCut = messageToCut.lastIndexOf(" ");

                    if(positionToCut > messageSender.length()+2) {
                        messageToCut = messageToCut.substring(0, positionToCut);
                    }

                    messages.add(messageToCut);
                    message = message.substring(messageToCut.length(), message.length());
                }

                messages.add(message);

                // add messages to onScreenPrivateMessages
                for(int i = 0; i < messages.size(); i++)
                    onScreenPrivateMessages.put(System.currentTimeMillis() - i, messages.get(i));
            } else {
                // add message to onScreenPrivateMessages
                onScreenPrivateMessages.put(milliTime, message);
            }

            // repaint screen
            PaintHook.Repaint();
        }
    }

    /**
     * Get Map of private messages to display on screen.
     *
     * @return Map of private messages to display on screen
     */
    public ConcurrentSkipListMap<Long, String> GetPrivateMessagesToDisplay() {
        return onScreenPrivateMessages;
    }

    /**
     * Clears private messages on screen.
     */
    public void ClearPrivateMessages() {
        onScreenPrivateMessages.clear();
    }

    /**
     * The maximum number of private messages that can be displayed on screen.
     */
    private int onScreenPrivateMessagesMax = 0;

    /**
     * Set the maximum number of private messages that can be displayed on screen.
     *
     * @param maxPrivateMessages : Maximum number of private messages that can be displayed on screen
     */
    public void SetMaxPrivateMessages(int maxPrivateMessages) {
        onScreenPrivateMessagesMax = maxPrivateMessages;
    }

    /**
     * Get the maximum number of private messages that can be displayed on screen.
     *
     * @return Maximum number of private messages that can be displayed on screen
     */
    public int GetMaxPrivateMessages() {
        return onScreenPrivateMessagesMax;
    }

    /**
     * The lifetime of private messages displayed on screen in milliseconds.
     */
    private final int onScreenPrivateMessageDisplayLifetime = 1800000;

    /**
     * Start the private messaging thread handler.
     */
    public PrivateMessaging() {
        (new Thread(this)).start();
    }

    /**
     * Thread handler of messageQueue and onScreenPrivateMessages.
     */
    public void run()
    {
        while(Mods.isRunning) {

            try {
                // handle messageQueue
                if (!isSendingMessage && !messageQueue.isEmpty()) {
                    // pop message from messageQueue
                    ProcessMessage(messageQueue.remove(0));
                }

                // handle onScreenPrivateMessages
                int count = 1;
                boolean didRemove = false;
                Iterator<Map.Entry<Long, String>> iterator = onScreenPrivateMessages.descendingMap().entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<Long, String> entry = iterator.next();
                    // remove private messages that exceed maximum message count or display lifetime
                    if (onScreenPrivateMessagesMax!= 0 && (count > onScreenPrivateMessagesMax || entry.getKey() + onScreenPrivateMessageDisplayLifetime <= System.currentTimeMillis())) {
                        iterator.remove();
                        didRemove = true;
                    }
                    count++;
                }

                // if message was removed, repaint screen
                if(didRemove)
                    PaintHook.Repaint();

                Thread.sleep(500);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    public void DisplayMessageOnScreen(String message, Long milliTime) {
        // add message to onScreenPrivateMessages
        onScreenPrivateMessages.put(milliTime, message);

        // repaint screen
        PaintHook.Repaint();
    }

    public String MOTD = "";

    public String motdVersion = "";

    public boolean showingMessage = false;

    public void ShowMOTD(boolean fromServer) {
        ClearPrivateMessages();

        String[] splitter = MOTD.split("[\\r\\n]+");

        Pattern p = Pattern.compile("[.0-9]+");
        Matcher m = p.matcher(splitter[1]);
        if (m.find()) {
            int positionStart = m.start();
            int positionEnd = m.end();

            motdVersion = splitter[1].substring(positionStart, positionEnd);

            if(motdVersion.equals(Mods.aberothModVersion)) {
                if(fromServer)
                    if(motdVersion.equals(Mods.postedMOTDVersion))
                        return;
                    else
                        Settings.SaveSettings();

                for (int i = splitter.length - 1; i >= 1; i--)
                    Mods.privateMessaging.DisplayMessageOnScreen(splitter[i], System.currentTimeMillis() - i);
            }
            else {
                Mods.privateMessaging.DisplayMessageOnScreen("AberothMod update: v" + Mods.aberothModVersion + " -> v" + motdVersion, System.currentTimeMillis()+2);
                Mods.privateMessaging.DisplayMessageOnScreen("Check GitHub or Reddit", System.currentTimeMillis()-2);
            }

            showingMessage = true;
        }
    }

}