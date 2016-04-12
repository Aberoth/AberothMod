import java.awt.Point;
import java.awt.Rectangle;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.net.MalformedURLException;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.awt.DefaultKeyboardFocusManager;
import java.awt.KeyEventDispatcher;
import java.awt.event.*;
import java.util.HashMap;
import java.util.Map;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import gameclient.*;
import a.c;
import a.z;
import a.n;
import a.k;

import javassist.*;
import javassist.expr.*;

public class Agent implements ClassFileTransformer {

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

    static Timer timer;

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

    static class updateChampionListTimer extends TimerTask{
        public void run() {
            try {

                URL myUrl = null;
                myUrl = new URL("http://www.aberoth.com/highscore/Most_Skillful.html");

                URLConnection urlConnection = myUrl.openConnection();
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        urlConnection.getInputStream(), "UTF-8"));
                String inputLine;
                StringBuilder a = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    a.append(inputLine);
                }
                in.close();

                championListHTML = a.toString();

                HTMLCount = HTMLCount + 1;

                System.out.println("Updated championListHTML.");

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    static class autoAttackTimer extends TimerTask {
        boolean upMove = true;

        public void run() {
            if (A != null) {

                if (kL == null) {
                    kL = A.getKeyListeners()[0];
                }

                if (mL == null) {
                    mL = A.getMouseListeners()[0];
                }

                if (attackAble) {

                    if (A.getFocusListeners().length > 0) {

                        A.removeFocusListener(A.getFocusListeners()[0]);
                    }

                    if (Fire == true && moveAble) {
                        if (upMove == true) {
                            upMove = false;

                            kL.keyPressed(createKeyEventDown(A, 'W', KeyEvent.VK_W));
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            kL.keyReleased(createKeyEventUp(A, 'W', KeyEvent.VK_W));
                        } else {
                            upMove = true;

                            kL.keyPressed(createKeyEventDown(A, 'D', KeyEvent.VK_S));
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            kL.keyReleased(createKeyEventUp(A, 'D', KeyEvent.VK_S));
                        }
                    }

                    if (Fire == true && MouseDown == false) {

                        mL.mousePressed(createMouseEventDown(A, InputEvent.BUTTON1_MASK,
                                100 + randomGenerator.nextInt(20), 100 + randomGenerator.nextInt(20)));

                        MouseDown = true;

                    } else if (MouseDown && Fire == false) {

                        SendChatMessage("hide");
                        mL.mouseReleased(createMouseEventUp(A, InputEvent.BUTTON1_MASK, 0, 0));

                        MouseDown = false;
                    }
                }

                System.out.println(items.size());

                if (Fire == true) {
                    Fire = false;
                }
            }
        }

    }

    static class pickupItemsTimer extends TimerTask {

        public void run() {
            if (A != null && pickupNearbyItems) {

                for (int x = -12; x <= 12; x += 4) {
                    for (int y = -12; y <= 12; y += 4) {
                        if (x * x + y * y <= 12 * 12) {

                            mL.mousePressed(createMouseEventDown(A, InputEvent.BUTTON1_MASK, x + playerLocation.x,
                                    y + playerLocation.y));
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }

                mL.mouseReleased(createMouseEventUp(A, InputEvent.BUTTON1_MASK, 0, 0));
            }
        }
    }

    static class drawTimer extends TimerTask {

        public void run() {
            if (A != null && drawStringTime + 500 > System.currentTimeMillis())
                A.repaint(10, 40, 500, 50);
        }
    }

    static class dispatcher implements KeyEventDispatcher {
        private double lastHButtonPressTime = 0;
        private double lastRButtonPressTime = 0;
        private double lastGButtonPressTime = 0;

        @Override
        public boolean dispatchKeyEvent(KeyEvent e) {
            if (mL != null) {
                if(isChatting) {
                    if((""+e.getKeyChar()).matches("^[A-Za-z0-9!@#$%^&*)(-]$") && e.getKeyLocation() == 0)
                        chatMessageBeingSent += e.getKeyChar();
                } else if (canDraw && e.getKeyChar() == 'g' && e.getKeyLocation() == 0) {
                    if (System.currentTimeMillis() - lastGButtonPressTime <= 500) {

                        pickupNearbyItems = !pickupNearbyItems;
                        if (pickupNearbyItems)
                            MessageUpdate("Auto-pickup activated.");
                        else
                            MessageUpdate("Auto-pickup deactivated.");



                    } else
                        lastGButtonPressTime = System.currentTimeMillis();
                } else if (A != null && canDraw && e.getKeyChar() == '`' && e.getKeyLocation() == 0
                        && CoordsOverItem(new Point(A.getMousePosition().x, A.getMousePosition().y))) {
                    ItemProtection(new Point(A.getMousePosition().x, A.getMousePosition().y));
                } else if (A != null && canDraw && e.getKeyChar() == '\t' && e.getKeyLocation() == 0) {
                    DropItems();
                } else if (canDraw && e.getKeyCode() == KeyEvent.VK_H && e.getID() == KeyEvent.KEY_RELEASED) {
                    if (System.currentTimeMillis() - lastHButtonPressTime <= 500) {
                        if ((e.getModifiers() & KeyEvent.CTRL_MASK) == 0) {
                            attackAble = !attackAble;
                            if (attackAble)
                                MessageUpdate("Auto-attack activated.");
                            else
                                MessageUpdate("Auto-attack deactivated.");
                        } else {
                            moveAble = !moveAble;

                            if (moveAble)
                                MessageUpdate("Auto-move activated.");
                            else
                                MessageUpdate("Auto-move deactivated.");
                        }

                    } else
                        lastHButtonPressTime = System.currentTimeMillis();
                } else if (canDraw && e.getKeyChar() == 'r' && e.getKeyLocation() == 0) {
                    if (System.currentTimeMillis() - lastRButtonPressTime <= 500) {

                        mL.mousePressed(createMouseEventDown(A, InputEvent.BUTTON1_MASK,
                                100 + randomGenerator.nextInt(20), 100 + randomGenerator.nextInt(20)));

                        MessageUpdate("Mouse held.");
                    } else
                        lastRButtonPressTime = System.currentTimeMillis();
                } else if (canDraw && e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    drawStringTime = System.currentTimeMillis();
                }

            }
            return false;
        }
    }

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

    public static void premain(String agentArgs, Instrumentation inst) {
        timer = new Timer();
        timer.scheduleAtFixedRate(new updateChampionListTimer(), 0, 1800000);

        timer = new Timer();
        timer.scheduleAtFixedRate(new autoAttackTimer(), 2000, 2000);

        timer = new Timer();
        timer.scheduleAtFixedRate(new pickupItemsTimer(), 100, 100);

        timer = new Timer();
        timer.scheduleAtFixedRate(new drawTimer(), 200, 200);

        DefaultKeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new dispatcher());

        inst.addTransformer(new Agent());
    }

    public byte[] transform(ClassLoader loader, String className, Class<?> redefiningClass, ProtectionDomain domain,
                            byte[] bytes) throws IllegalClassFormatException {
        if (!className.contains("gameclient/") && className.indexOf("a/") != 0)
            return null;
        else {
            try {
                ClassPool pool = ClassPool.getDefault();
                pool.insertClassPath(new ClassClassPath(this.getClass()));

                CtClass cl = pool.get(className.replaceAll("/", "."));

                if (cl.getName().contains("a.c")) {
                    CtMethod[] methods = cl.getDeclaredMethods();

                    for (int i = 0; i < methods.length; i++) {
                        if (methods[i].toString().contains(
                                "Ljava/lang/String;Ljava/lang/String;ILa/q;IIILjava/lang/String;Ljava/lang/String;ZZIZZ")) {
                            methods[i].insertBefore("{ Agent.GetName($1); }");
                        }
                    }

                    cl.detach();
                    return cl.toBytecode();
                } else if (cl.getName().contains("gameclient.K")) {

                    CtField f = CtField.make("public java.awt.Color nameColor = null;", cl);
                    cl.addField(f);

                    CtMethod[] methods = cl.getDeclaredMethods();

                    for (int i = 0; i < methods.length; i++) {
                        // System.out.println(methods[i].toString());
                        if (!methods[i].toString().contains("final a ()V")) {
                            if (methods[i].toString().contains("(Ljava/awt/Color;)V")) {
                                //// COLOR OF TEXTS EACH RENDER
                                methods[i].insertBefore("{ nameColor = $1; }");
                            } else if (methods[i].toString().contains("Ljava/lang/String;I")) {
                                //// TEXT RENDERING
                                methods[i].insertBefore(
                                        "{" +
                                        "   if($3 != null && $3.length() > 2) {" +
                                        "       if( nameColor.getRed() == 255 && nameColor.getGreen() == 255 && nameColor.getBlue() == 0 )" +
                                        "           Agent.UnderFire();" +
                                        "   }" +
                                        "   if($3 != null && $6 > 0)" +
                                        "   { " +
                                        "       String newName = Agent.getEntityLevel($3);" +
                                        "       $3 = newName;" +
                                        "       if(Agent.colors && newName.indexOf(\"[\") > 0)" +
                                        "       {" +
                                        "           if(newName.indexOf(\"!\") == -1 && nameColor.getRed() == 255 && nameColor.getGreen() == 175 && nameColor.getBlue() == 175)" +
                                        "               $0.a(new java.awt.Color(0, 191, 255));" +
                                        "           else" +
                                        "               $0.a(Agent.makeColor(newName));" +
                                        "       }" +
                                        "   }" +
                                        "} ");
                            } else if (methods[i].toString().contains("Ljava/awt/Graphics;L")) {
                                //// RENDER
                                methods[i].instrument(new ExprEditor() {
                                    public int count = 1;

                                    public void edit(MethodCall m) {
                                        if (m.getClassName().equals("java.awt.Graphics")
                                                && m.getMethodName().equals("drawString"))
                                            //// DRAWSTRING FUNCTION IN RENDER
                                            try {
                                                m.replace("{" +
                                                        "   if(!Agent.canDraw)" +
                                                        "       Agent.canDraw = true;" +
                                                        "   if(!Agent.rehide && $1.matches(\"You fall asleep.\")) {" +
                                                        "       Agent.rehide = true;" +
                                                        "       Agent.SendChatMessage(\"hide\");" +
                                                        "   }" +
                                                        "   else if(Agent.rehide && $1.matches(\"You are now hiding.\"))" +
                                                        "       Agent.rehide = false;" +
                                                        "   if(Agent.drawStringTime > System.currentTimeMillis())" +
                                                        "       $proceed(Agent.stringToDraw, 10, 55);" +
                                                        "   if(Agent.playerName.equals($1)) {" +
                                                        "       Agent.playerLocation.x = $2 + 4 + Agent.A.getGraphics().getFontMetrics().stringWidth($1) / 2;" +
                                                        "       Agent.playerLocation.y = $3 + 108;" +
                                                        "   }" +
                                                        "   $_ = $proceed($$);" +
                                                        "}");
                                            } catch (Exception e) {
                                                System.out.println("Cannot compile expression! " + e.toString());
                                            }
                                        else if (m.getClassName().equals("java.awt.Graphics")
                                                && m.getMethodName().equals("drawImage"))
                                            //// DRAWIMAGE FUNCTION IN RENDER
                                            try {
                                                if (count == 1)
                                                    System.out.println("a");
                                                m.replace(
                                                        "{" + "   if(!Agent.isDropping && $1.getHeight(null) == 32 && $1.getWidth(null) == 32 && !Agent.AlreadyStoredAberothItem(new java.awt.Point($2, $3)))"
                                                                + "       Agent.items.add(new Agent.AberothItem(new java.awt.Rectangle($2, $3, 32, 32)));"
                                                                + "   $_ = $proceed($$);" + "}");
                                                count++;
                                            } catch (Exception e) {
                                                System.out.println("Cannot compile expression! " + e.toString());
                                            }
                                    }
                                });
                            } else if (methods[i].toString().contains("(Ljava/lang/String;)L")) {
                                //// NAME OF TEXTS EACH RENDER
                            } else if (methods[i].toString().contains("(Ljava/util/Collection;)L")) {
                                //// ?
                            }
                        }
                    }

                    cl.detach();
                    return cl.toBytecode();
                } else if (cl.getName().contains("gameclient.GameClient")) {

                    CtMethod[] methods = cl.getDeclaredMethods();
                    for (int i = 0; i < methods.length; i++) {
                        if (methods[i].toString().contains("public final b ()V")) {
                            methods[i].instrument(new ExprEditor() {
                                public void edit(NewExpr m) throws CannotCompileException {
                                    if(m.getClassName().matches("a.c"))
                                        m.replace("Agent.C = $proceed($$); System.out.println(\"Got C: \"+Agent.C); $_ = Agent.C;");
                                }
                            });
                        }
                        else if (methods[i].toString().contains("public final a (ILa/u;)V")) {
                            methods[i].insertBefore("" +
                                    "{" +
                                    "   if(Agent.toHex($2.a()).matches(\"0000000000000000000000000000000000100a02\")) {" + //Enter KeyPress Message
                                    "       if(Agent.isChatting)" +
                                    "           if(!Agent.CheckChatMessage()) {" +
                                    "               Agent.isChatting = !Agent.isChatting;" +
                                    "               $2 = new a.n('', '');" +
                                    "               Agent.kL.keyPressed(new java.awt.event.KeyEvent(Agent.A, java.awt.event.KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, java.awt.event.KeyEvent.VK_ESCAPE, java.awt.event.KeyEvent.CHAR_UNDEFINED));" +
                                    "               Agent.kL.keyReleased(new java.awt.event.KeyEvent(Agent.A, java.awt.event.KeyEvent.KEY_RELEASED, System.currentTimeMillis(), 0, java.awt.event.KeyEvent.VK_ESCAPE, java.awt.event.KeyEvent.CHAR_UNDEFINED));" +
                                    "           } else" +
                                    "               Agent.isChatting = !Agent.isChatting;" +
                                    "       else" +
                                    "           Agent.isChatting = !Agent.isChatting;" +
                                    "   } else if(Agent.toHex($2.a()).matches(\"0000000000000000000000000000000000101b01\")) {" + //Escape KeyRelease Message
                                    "       if(Agent.isChatting) {" +
                                    "           Agent.isChatting = false;" +
                                    "           Agent.chatMessageBeingSent = \"\";" +
                                    "       }" +
                                    "   } else if(Agent.toHex($2.a()).matches(\"0000000000000000000000000000000000100802\")) {" + //Backspace KeyPress Message
                                    "       if(Agent.isChatting) {" +
                                    "           Agent.chatMessageBeingSent = Agent.chatMessageBeingSent.substring(0, Agent.chatMessageBeingSent.length()-1);" +
                                    "       }" +
                                    "   }" + //Backspace KeyPress Message
                                    "}");
                        }
                    }

                    cl.detach();
                    return cl.toBytecode();
                } else if (cl.getName().contains("gameclient.c")) {

                    CtConstructor[] constra = cl.getConstructors();
                    constra[0].insertBefore("{Agent.RetriveA($1);}");

                    CtMethod[] methods = cl.getDeclaredMethods();
                    for (int i = 0; i < methods.length; i++) {

                        if (methods[i].toString().contains("mouseReleased")) {

                            methods[i].insertBefore("if($1.getButton() == 1){ Agent.MouseDown = false; }");

                        } else if (methods[i].toString().contains("mousePressed")) {
                            methods[i].insertBefore(
                                    "if($1.getButton() == 3 || $1.getButton() == 1 && ($1.getModifiersEx() & java.awt.event.MouseEvent.SHIFT_DOWN_MASK) != 0)"
                                            + "                   Agent.CheckForItemDrop(new java.awt.Point($1.getX(), $1.getY()));");

                            methods[i].instrument(new ExprEditor() {
                                public void edit(MethodCall m) throws CannotCompileException {
                                    if (m.getMethodName().matches("requestFocus")) {
                                        m.replace(";");
                                    }
                                }
                            });
                        }
                    }

                    cl.detach();
                    return cl.toBytecode();
                }

                cl.detach();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
