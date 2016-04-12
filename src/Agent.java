import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.Timer;
import java.awt.DefaultKeyboardFocusManager;

import javassist.*;
import javassist.expr.*;

public class Agent implements ClassFileTransformer {

    static Timer timer;

    public static void premain(String agentArgs, Instrumentation inst) {
        timer = new Timer();
        timer.scheduleAtFixedRate(new Timers.updateChampionListTimer(), 0, 1800000);

        timer = new Timer();
        timer.scheduleAtFixedRate(new Timers.autoAttackTimer(), 2000, 2000);

        timer = new Timer();
        timer.scheduleAtFixedRate(new Timers.pickupItemsTimer(), 100, 100);

        timer = new Timer();
        timer.scheduleAtFixedRate(new Timers.drawTimer(), 200, 200);

        DefaultKeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new Dispatcher());

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
                            methods[i].insertBefore("{ AberothMod.GetName($1); }");
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
                                                "           AberothMod.UnderFire();" +
                                                "   }" +
                                                "   if($3 != null && $6 > 0)" +
                                                "   { " +
                                                "       String newName = AberothMod.getEntityLevel($3);" +
                                                "       $3 = newName;" +
                                                "       if(AberothMod.colors && newName.indexOf(\"[\") > 0)" +
                                                "       {" +
                                                "           if(newName.indexOf(\"!\") == -1 && nameColor.getRed() == 255 && nameColor.getGreen() == 175 && nameColor.getBlue() == 175)" +
                                                "               $0.a(new java.awt.Color(0, 191, 255));" +
                                                "           else" +
                                                "               $0.a(AberothMod.makeColor(newName));" +
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
                                                        "   if(!AberothMod.canDraw)" +
                                                        "       AberothMod.canDraw = true;" +
                                                        "   if(!AberothMod.rehide && $1.matches(\"You fall asleep.\")) {" +
                                                        "       AberothMod.rehide = true;" +
                                                        "       AberothMod.SendChatMessage(\"hide\");" +
                                                        "   }" +
                                                        "   else if(AberothMod.rehide && $1.matches(\"You are now hiding.\"))" +
                                                        "       AberothMod.rehide = false;" +
                                                        "   if(AberothMod.drawStringTime > System.currentTimeMillis())" +
                                                        "       $proceed(AberothMod.stringToDraw, 10, 55);" +
                                                        "   if(AberothMod.playerName.equals($1)) {" +
                                                        "       AberothMod.playerLocation.x = $2 + 4 + AberothMod.A.getGraphics().getFontMetrics().stringWidth($1) / 2;" +
                                                        "       AberothMod.playerLocation.y = $3 + 108;" +
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
                                                        "{" + "   if(!AberothMod.isDropping && $1.getHeight(null) == 32 && $1.getWidth(null) == 32 && !AberothMod.AlreadyStoredAberothItem(new java.awt.Point($2, $3)))"
                                                                + "       AberothMod.items.add(new AberothMod.AberothItem(new java.awt.Rectangle($2, $3, 32, 32)));"
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
                                        m.replace("AberothMod.C = $proceed($$); System.out.println(\"Got C: \"+AberothMod.C); $_ = AberothMod.C;");
                                }
                            });
                        }
                        else if (methods[i].toString().contains("public final a (ILa/u;)V")) {
                            methods[i].insertBefore("" +
                                    "{" +
                                    "   if(AberothMod.toHex($2.a()).matches(\"0000000000000000000000000000000000100a02\")) {" + //Enter KeyPress Message
                                    "       if(AberothMod.isChatting)" +
                                    "           if(!AberothMod.CheckChatMessage()) {" +
                                    "               AberothMod.isChatting = !AberothMod.isChatting;" +
                                    "               $2 = new a.n('', '');" +
                                    "               AberothMod.kL.keyPressed(new java.awt.event.KeyEvent(AberothMod.A, java.awt.event.KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, java.awt.event.KeyEvent.VK_ESCAPE, java.awt.event.KeyEvent.CHAR_UNDEFINED));" +
                                    "               AberothMod.kL.keyReleased(new java.awt.event.KeyEvent(AberothMod.A, java.awt.event.KeyEvent.KEY_RELEASED, System.currentTimeMillis(), 0, java.awt.event.KeyEvent.VK_ESCAPE, java.awt.event.KeyEvent.CHAR_UNDEFINED));" +
                                    "           } else" +
                                    "               AberothMod.isChatting = !AberothMod.isChatting;" +
                                    "       else" +
                                    "           AberothMod.isChatting = !AberothMod.isChatting;" +
                                    "   } else if(AberothMod.toHex($2.a()).matches(\"0000000000000000000000000000000000101b01\")) {" + //Escape KeyRelease Message
                                    "       if(AberothMod.isChatting) {" +
                                    "           AberothMod.isChatting = false;" +
                                    "           AberothMod.chatMessageBeingSent = \"\";" +
                                    "       }" +
                                    "   } else if(AberothMod.toHex($2.a()).matches(\"0000000000000000000000000000000000100802\")) {" + //Backspace KeyPress Message
                                    "       if(AberothMod.isChatting) {" +
                                    "           AberothMod.chatMessageBeingSent = AberothMod.chatMessageBeingSent.substring(0, AberothMod.chatMessageBeingSent.length()-1);" +
                                    "       }" +
                                    "   }" + //Backspace KeyPress Message
                                    "}");
                        }
                    }

                    cl.detach();
                    return cl.toBytecode();
                } else if (cl.getName().contains("gameclient.c")) {

                    CtConstructor[] constra = cl.getConstructors();
                    constra[0].insertBefore("{AberothMod.RetriveA($1);}");

                    CtMethod[] methods = cl.getDeclaredMethods();
                    for (int i = 0; i < methods.length; i++) {

                        if (methods[i].toString().contains("mouseReleased")) {

                            methods[i].insertBefore("if($1.getButton() == 1){ AberothMod.MouseDown = false; }");

                        } else if (methods[i].toString().contains("mousePressed")) {
                            methods[i].insertBefore(
                                    "if($1.getButton() == 3 || $1.getButton() == 1 && ($1.getModifiersEx() & java.awt.event.MouseEvent.SHIFT_DOWN_MASK) != 0)"
                                            + "                   AberothMod.CheckForItemDrop(new java.awt.Point($1.getX(), $1.getY()));");

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