import java.awt.*;
import java.awt.event.*;
import java.util.*;

import gameclient.*;
import a.c;
import a.z;
import a.n;
import a.k;

public class AberothMod {

    public static class AberothEntity {
        public String level;
        public java.awt.Color nameColor;
        public boolean npc;
        public int HTMLCount;

        AberothEntity(String _level, boolean _npc, int _HTMLCount)
        {
            level = _level;
            npc = _npc;
            HTMLCount = _HTMLCount;
            nameColor = null;
        }

        public void SetColor(java.awt.Color color)
        {
            nameColor = color;
        }
    }


    public static class AberothItem {
        public Rectangle itemImage;
        public boolean isProtectedFromAutoDrop;

        public AberothItem(Rectangle rect) {
            itemImage = rect;
            isProtectedFromAutoDrop = false;
        }
    }

    public static a A = null;
    public static c C = null;

    static Map<String, AberothEntity> championList = new HashMap<String, AberothEntity>();
    public static String championListHTML = "init";
    public static int HTMLCount = 0;
    public static boolean colors = false;

    public static String chatMessageBeingSent = "";
    public static boolean isChatting = false;

    public static ArrayList<AberothItem> items = new ArrayList<>();
    public static boolean isDropping = false;

    public static boolean canDraw = false;
    public static String stringToDraw = null;
    public static double drawStringTime = 0;

    static boolean Fire = false;
    public static boolean MouseDown = false;

    static MouseListener mL = null;
    public static KeyListener kL = null;

    static Random randomGenerator = new Random();

    public static boolean pickupNearbyItems = false;
    public static boolean moveAble = false;
    public static boolean attackAble = false;
    public static boolean rehide = false;

    public static String playerName = "";
    public static Point playerLocation = new Point(0, 0);

    public static void UnderFire() {
        Fire = true;
    }

    public static boolean GetFire() {
        return Fire;
    }

    public static void GetName(String pName) {
        String name = Character.toUpperCase(pName.charAt(0)) + pName.substring(1);
        System.out.println("Got name: " + name);
        playerName = name;
    }

    public static void MessageUpdate(String message) {
        if (stringToDraw == message) {
            message = " " + message;
        }
        stringToDraw = message;
        drawStringTime = System.currentTimeMillis() + 2000;
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

    public static boolean CheckChatMessage()
    {
        boolean ret = true;
        System.out.println(chatMessageBeingSent);
        if(chatMessageBeingSent.matches("!colors")) {
            colors = !colors;
            if(colors)
                MessageUpdate("Colors activated.");
            else
                MessageUpdate("Colors deactivated.");
            ret = false;
        }

        chatMessageBeingSent = "";
        return ret;
    }

    public static String toHex(String arg) {
        return String.format("%040x", new java.math.BigInteger(1, arg.getBytes(/*YOUR_CHARSET?*/)));
    }

    public static String getEntityLevel(String entityName) {
        String returnName = entityName;

        int n = entityName.indexOf("!");
        if(n != -1)
        {
            entityName = entityName.substring(0, n);
        }

        if(entityName.matches("[a-zA-Z]+") && championListHTML != "init" && !entityName.contains("Thief") && !entityName.contains("Alchemist")) {
            if (!championList.containsKey(entityName) || (!championList.get(entityName).npc && HTMLCount > championList.get(entityName).HTMLCount)) {
                n = championListHTML.indexOf("<td>" + entityName + "</td>");
                if (n != -1) {
                    String subStr = championListHTML.substring(n + 13 + entityName.length(), n + 26 + entityName.length());
                    n = subStr.indexOf("</td>");

                    subStr = subStr.substring(0, n);

                    championList.put(entityName, new AberothEntity(subStr, false, HTMLCount));

                    championList.get(entityName).SetColor(null);

                    returnName = returnName + (" [" + subStr + "]");
                } else
                    championList.put(entityName, new AberothEntity("?", true, HTMLCount));
            } else if (!(championList.get(entityName)).npc)
                returnName = returnName + (" [" + (championList.get(entityName)).level + "]");
        }

        return returnName;
    }

    public static java.awt.Color makeColor(String entityNameString) {
        String entityName;

        int n = entityNameString.indexOf("!");
        if(n != -1) {
            return new java.awt.Color(65, 105, 255);
        }
        else {
            entityName = entityNameString.substring(0, entityNameString.indexOf("[") - 1);

            if (championList.containsKey(entityName) && !championList.get(entityName).npc && championList.get(entityName).nameColor == null) {
                String levelS = entityNameString.substring(entityNameString.indexOf("[") + 1, entityNameString.length());
                levelS = levelS.substring(0, levelS.indexOf("]"));

                int level = Integer.parseInt(levelS);

                Double red = Math.floor(Math.sin(0.01 * level + 0) * 127 + 128);
                Double grn = Math.floor(Math.sin(0.01 * level + 2) * 127 + 128);
                Double blu = Math.floor(Math.sin(0.01 * level + 4) * 127 + 128);

                championList.get(entityName).SetColor(new java.awt.Color(red.intValue(), grn.intValue(), blu.intValue()));

                return championList.get(entityName).nameColor;
            }
            else if (championList.containsKey(entityName) && !championList.get(entityName).npc)
                return championList.get(entityName).nameColor;
            else
                return new java.awt.Color(255, 255, 255);
        }
    }

    public static boolean AlreadyStoredAberothItem(Point coords) {
        for (AberothItem item : items) {
            if (coords.x == item.itemImage.x && coords.y == item.itemImage.y)
                return true;
        }

        return false;
    }

    public static void ItemProtection(Point coords) {
        for (AberothItem item : items) {
            if (coords.x >= item.itemImage.x && coords.x <= item.itemImage.x + item.itemImage.width
                    && coords.y >= item.itemImage.y && coords.y <= item.itemImage.y + item.itemImage.height) {
                item.isProtectedFromAutoDrop = !item.isProtectedFromAutoDrop;
                if (item.isProtectedFromAutoDrop)
                    MessageUpdate("Item protected!");
                else
                    MessageUpdate("Item unprotected!");
            }
        }
    }

    public static void DropItems() {
        isDropping = true;

        Collections.sort(items, new Comparator<AberothItem>() {
            @Override
            public int compare(AberothItem item1, AberothItem item2) {
                return item1.itemImage.y - item2.itemImage.y;
            }

        });
        for (Iterator<AberothItem> i = items.iterator(); i.hasNext();) {
            AberothItem item = i.next();

            if (!item.isProtectedFromAutoDrop) {
                i.remove();

                mL.mousePressed(
                        createMouseEventDown(A, InputEvent.BUTTON3_MASK, item.itemImage.x + 15, item.itemImage.y + 15));

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        isDropping = false;
    }

    public static void CheckForItemDrop(Point coords) {
        for (Iterator<AberothItem> i = items.iterator(); i.hasNext();) {
            AberothItem item = i.next();

            if (coords.x >= item.itemImage.x && coords.x <= item.itemImage.x + item.itemImage.width
                    && coords.y >= item.itemImage.y && coords.y <= item.itemImage.y + item.itemImage.height)
                i.remove();
        }
    }

    public static boolean CoordsOverItem(Point coords) {
        for (AberothItem item : items) {
            if (coords.x >= item.itemImage.x && coords.x <= item.itemImage.x + item.itemImage.width
                    && coords.y >= item.itemImage.y && coords.y <= item.itemImage.y + item.itemImage.height)
                return true;
        }

        return false;
    }

    public static void RetriveA(a aInst) {
        if (A == null) {
            A = aInst;
            System.out.println("RETRIVED A " + aInst);

        }
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
