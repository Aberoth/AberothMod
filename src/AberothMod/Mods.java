package AberothMod;

import AberothMod.Aberoth.*;
import AberothMod.Utils.*;

import java.awt.*;
import java.awt.event.*;
import java.lang.Character;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import gameclient.*;
import a.c;
import a.z;
import a.n;
import a.k;

public class Mods {

    public static boolean isRunning;
    public static final String aberothModVersion = "2.0";
    public static String postedMOTDVersion;

    public static String MostSkillfulHTML = "init";
    public static int HTMLDownloadCount = 0;

    public static Map<String, Color> healthMap = new ConcurrentHashMap<>();

    public static boolean colors = false;

    public static a A = null;
    public static c C = null;

    public static boolean coinFlip = false;

    public static boolean isChatting = false;
    public static double chatTimeStamp = 0;

    public static String modChatMessage = "";
    public static boolean modChat = false;

    public static boolean canDraw = false;
    public static String stringToDraw = null;
    public static double drawStringTime = 0;

    public static MouseListener mL = null;
    public static KeyListener kL = null;

    public static boolean pickupNearbyItems = false;

    public static PrivateMessaging privateMessaging;
    public static ServerCommunicator serverCommunicator;

    public static void initialize() {
        RSAEncryption.GenerateKey();

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new Timers.updateChampionListTimer(), 0, 1800000);

        timer = new Timer();
        timer.scheduleAtFixedRate(new Timers.coinFlipTimer(), 0, 400);

        timer = new Timer();
        timer.scheduleAtFixedRate(new Timers.pickupItemsTimer(), 0, 100);

        timer = new Timer();
        timer.scheduleAtFixedRate(new Timers.drawTimer(), 0, 50);

        DefaultKeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new Dispatcher());
    }

    public static void LoggingIn(String pName) {
        Mods.isRunning = true;

        String name = Character.toUpperCase(pName.charAt(0)) + pName.substring(1);
        System.out.println("Welcome, " + name + "!");
        World.myPlayer = new Player(name);

        World.screenWidth = (int) (640 * World.screenRatio);
        World.screenHeight = (int) (480 * World.screenRatio);

        serverCommunicator = new ServerCommunicator();
        privateMessaging = new PrivateMessaging();

        Settings.LoadSettings();
    }

    public static void LoggedOut() {
        System.out.println("Logged Out!");

        serverCommunicator.Disconnect();

        healthMap = new ConcurrentHashMap<>();

        Mods.kL = null;
        Mods.mL = null;

        Mods.isRunning = false;
        canDraw = false;
        A = null;
    }

    public static void CalculateHealth() {
        int currentHealth = 0;
        for (String i : Mods.healthMap.keySet())
            if (Mods.healthMap.get(i).equals(new Color(255, 64, 64)))
                currentHealth++;
        currentHealth--;

        if(currentHealth != World.myPlayer.health)
            PaintHook.Repaint();

        World.myPlayer.health = currentHealth;
    }

    public static void CalculateFocusExp(int expBarWidth) {
        double focusExp = (double) expBarWidth / (World.screenWidth - (int) (22*World.screenRatio));

        if(focusExp != World.myPlayer.focusExperience)
            PaintHook.Repaint();

        World.myPlayer.focusExperience = focusExp;
    }

    public static void MessageUpdate(String message) {
        if (stringToDraw == message) {
            message = " " + message;
        }
        stringToDraw = message;
        drawStringTime = System.currentTimeMillis() + 2000;

        PaintHook.Repaint();
    }

    public static void SendChatMessage(String message)
    {
        if(C != null){
            ArrayList arrayParam = new java.util.ArrayList(1);
            char ch = 2;

            arrayParam.add(new n('\n', ch));
            for(char chM : message.toCharArray())
                arrayParam.add(new n(chM, ch));
            arrayParam.add(new n('\n', ch));

            k ak = new k(arrayParam);
            z az = new z(1, ak);
            C.a(az);
        }
    }

    public static String toHex(String arg) {
        return String.format("%040x", new java.math.BigInteger(1, arg.getBytes(/*YOUR_CHARSET?*/)));
    }

    public static int getEntityLevel(String entityName) {

        int n = entityName.indexOf("!");
        if(n != -1)
        {
            entityName = entityName.substring(0, n);
        }

        if(entityName.matches("[a-zA-Z]+") && MostSkillfulHTML != "init" && !entityName.contains("Thief") && !entityName.contains("Alchemist")) {
            if (!World.cachedCharacters.containsKey(entityName) || (World.cachedCharacters.get(entityName) instanceof Player && HTMLDownloadCount > ((Player) World.cachedCharacters.get(entityName)).HTMLDownloadCount)) {
                n = MostSkillfulHTML.indexOf("<td>" + entityName + "</td>");
                if (n != -1) {
                    String subStr = MostSkillfulHTML.substring(n + 13 + entityName.length(), n + 26 + entityName.length());
                    n = subStr.indexOf("</td>");

                    subStr = subStr.substring(0, n);
                    int totalLevel = Integer.parseInt(subStr);

                    World.cachedCharacters.put(entityName, new Player(entityName, totalLevel, HTMLDownloadCount));

                    World.cachedCharacters.get(entityName).SetColor(null);

                    return totalLevel;
                } else
                    World.cachedCharacters.put(entityName, new NPC(entityName, 0));
            } else
                return World.cachedCharacters.get(entityName).totalLevel;
        }

        return 0;
    }

    public static java.awt.Color makePlayerColor(Object playerObject) {
        Player player = (Player) playerObject;

        if (player.nameColor == null) {

            Double red = Math.floor(Math.sin(0.01 * player.totalLevel + 0) * 127 + 128);
            Double grn = Math.floor(Math.sin(0.01 * player.totalLevel + 2) * 127 + 128);
            Double blu = Math.floor(Math.sin(0.01 * player.totalLevel + 4) * 127 + 128);

            player.SetColor(new java.awt.Color(red.intValue(), grn.intValue(), blu.intValue()));
            World.cachedCharacters.put(player.name, player);

            return player.nameColor;
        } else
            return player.nameColor;

    }

    public static MouseEvent createMouseEventDown(a aInst, int type, int x, int y) {
        return new MouseEvent(aInst, MouseEvent.MOUSE_PRESSED, System.currentTimeMillis(), type, x, y, 1, false);
    }

    public static MouseEvent createMouseEventUp(a aInst, int type, int x, int y) {
        return new MouseEvent(aInst, MouseEvent.MOUSE_RELEASED, System.currentTimeMillis(), type, x, y, 1, false);
    }

    public static KeyEvent createKeyEventDown(a aInst, char keyChar, int keyCode) {
        KeyEvent key = new KeyEvent(aInst, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, keyCode, keyChar);
        return key;
    }

    public static KeyEvent createKeyEventUp(a aInst, char keyChar, int keyCode) {
        KeyEvent key = new KeyEvent(aInst, KeyEvent.KEY_RELEASED, System.currentTimeMillis(), 0, keyCode, keyChar);
        return key;
    }
}
