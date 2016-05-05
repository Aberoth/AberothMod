package AberothMod;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

import javassist.*;
import javassist.expr.*;

public class Agent implements ClassFileTransformer {

    public static ClassPool pool;

    public static void premain(String agentArgs, Instrumentation inst) {

        pool = ClassPool.getDefault();

        // import AberothMod packages
        pool.importPackage("AberothMod");
        pool.importPackage("AberothMod.Aberoth");
        pool.importPackage("AberothMod.Aberoth.Character");

        inst.addTransformer(new Agent());

        Mods.initialize();
    }

    public byte[] transform(ClassLoader loader, String className, Class<?> redefiningClass, ProtectionDomain domain, byte[] bytes) throws IllegalClassFormatException {
        // return null if not an AberothClient package
        if (!className.contains("gameclient/") && className.indexOf("a/") != 0)
            return null;

        else {
            try {
                // insert AberothMod class path
                pool.insertClassPath(new ClassClassPath(this.getClass()));

                CtClass cl = pool.get(className.replaceAll("/", "."));

                if (cl.getName().contains("a.c")) {
                    /**
                     * Socket Handling Class
                     */

                    // get class methods
                    CtMethod[] methods = cl.getDeclaredMethods();

                    // loop through class methods
                    for (int i = 0; i < methods.length; i++) {

                        if (methods[i].toString().contains("Ljava/lang/String;Ljava/lang/String;ILa/q;IIILjava/lang/String;Ljava/lang/String;ZZIZZ")) {
                            /**
                             * Function : boolean Login(String paramString1, String paramString2, int paramInt1, q paramq, int paramInt2, int paramInt3, int paramInt4, String paramString3, String paramString4, boolean paramBoolean1, boolean paramBoolean2, int paramInt5, boolean paramBoolean3, boolean paramBoolean4)
                             * Purpose  : Login to server.
                             */

                            methods[i].insertBefore("{ World.screenRatio = (double) $5 / (double) $6; Mods.LoggingIn($1); }");
                        }

                    }

                    cl.detach();
                    return cl.toBytecode();


                } else if (cl.getName().contains("gameclient.K")) {
                    /**
                     * Rendering Class
                     */

                    // create additional class fields
                    CtField f = CtField.make("java.awt.Graphics localGraphics = null;", cl);
                    cl.addField(f);

                    f = CtField.make("public java.awt.Color currentColor = null;", cl);
                    cl.addField(f);

                    f = CtField.make("public String previousWhiteStatsInLowerLeftText = \"\";", cl);
                    cl.addField(f);

                    f = CtField.make("public String previousYellowStatsInLowerLeftText = \"\";", cl);
                    cl.addField(f);

                    // get class methods
                    CtMethod[] methods = cl.getDeclaredMethods();

                    // loop through class methods
                    for (int i = 0; i < methods.length; i++) {

                        if (methods[i].toString().contains("(Ljava/awt/Color;)V")) {
                            /**
                             * Function : void a(Color paramColor)
                             * Purpose  : Sets the color. (we need this because graphics.getColor() returns something else)
                             */

                            methods[i].insertBefore("{ currentColor = $1; }");

                        } else if(methods[i].toString().contains("[public final a (IIII)V]")) {
                            /**
                             * Function : void a(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
                             * Purpose  : Draws fillRect's on screen.
                             */

                            methods[i].insertBefore("if(currentColor.equals(new java.awt.Color(255, 64, 64)) || currentColor.equals(new java.awt.Color(64, 64, 64))) {" +
                                                    "   Mods.healthMap.put(String.valueOf($2), currentColor);" +
                                                    "   Mods.CalculateHealth();" +
                                                    "} else if(currentColor.equals(new java.awt.Color(128, 128, 128))) {" +
                                                    "   if($2 == Math.floor(World.screenRatio))" +
                                                    "       Mods.CalculateFocusExp($1);" +
                                                    "}");

                        } else if (methods[i].toString().contains("Ljava/lang/String;I")) {
                            /**
                             * Function : Rectangle ProcessOnScreenText(int paramInt1, char arg2, String arg3, int paramInt2, int paramInt3, int paramInt4, char paramChar2, boolean paramBoolean, int paramInt5)
                             * Purpose  : Process text received from Aberoth Server.
                             */

                            methods[i].insertBefore("{" +
                                            "   if($3 != null && $6 > 0)" +
                                            "   { " +
                                            "       int totalLevel = Mods.getEntityLevel($3);" +
                                            "       " +
                                            "       if($3.equals(World.myPlayer.name) && $3.indexOf(\"!\") == -1 && currentColor.getRed() == 255 && currentColor.getGreen() == 175 && currentColor.getBlue() == 175)" +
                                            "           World.myPlayer.friendliness = AberothMod.Aberoth.Character.Friendliness.UNFRIENDLY;" +
                                            "       else if($3.equals(World.myPlayer.name) && $3.indexOf(\"!\") == -1)" +
                                            "           World.myPlayer.friendliness = AberothMod.Aberoth.Character.Friendliness.FRIENDLY;" +
                                            "       " +
                                            "       if(Mods.colors && World.cachedCharacters.containsKey($3.replace(\"!\", \"\")) && World.cachedCharacters.get($3.replace(\"!\", \"\")) instanceof Player)" +
                                            "       {" +
                                            "           if($3.indexOf(\"!\") == -1 && currentColor.getRed() == 255 && currentColor.getGreen() == 175 && currentColor.getBlue() == 175)" +
                                            "               $0.a(new java.awt.Color(0, 191, 255));" +
                                            "           else if($3.indexOf(\"!\") != -1)" +
                                            "               $0.a(new java.awt.Color(65, 105, 255));" +
                                            "           else" +
                                            "               $0.a(Mods.makePlayerColor(World.cachedCharacters.get($3)));" +
                                            "       }" +
                                            "       " +
                                            "       if(totalLevel > 0)" +
                                            "           $3 = $3 + \" [\" + totalLevel + \"]\";" +
                                            "   }" +
                                            "}");

                        } else if (methods[i].toString().contains("Ljava/awt/Graphics;L")) {
                            /**
                             * Function : void a(Graphics paramGraphics, GraphicsConfiguration arg2, Rectangle paramRectangle)
                             * Purpose  : Draw text and images on screen. (Requires graphics.repaint() to be called externally to see effects.)
                             */

                            methods[i].insertBefore("localGraphics = $1;");
                            methods[i].instrument(new ExprEditor() {

                                // edit drawString() function
                                public void edit(MethodCall m) {
                                    if (m.getClassName().equals("java.awt.Graphics")
                                            && m.getMethodName().equals("drawString"))
                                        try {
                                            m.replace("{" +
                                                    "   if(!Mods.canDraw)" +
                                                    "       Mods.canDraw = true;" +
                                                    "   " +
                                                    "   String textToDraw = $1;" +
                                                    "   " +
                                                    "   if(textToDraw.contains(\"     Defense: \")) {" +
                                                    "       $1 = \"Health: \" + World.myPlayer.health + \"     \" + $1;" +
                                                    "       if(textToDraw != previousWhiteStatsInLowerLeftText)" +
                                                    "           PaintHook.Repaint();" +
                                                    "       previousWhiteStatsInLowerLeftText = textToDraw;" +
                                                    "   } else if(textToDraw.contains(\" (focus): Level\") || textToDraw.contains(\" (focus,recovering): Level\")) {" +
                                                    "       $1 = $1 + \" (\" + new java.text.DecimalFormat(\"#0.0000%\").format(World.myPlayer.focusExperience) + \")\";" +
                                                    "       if(textToDraw != previousYellowStatsInLowerLeftText)" +
                                                    "           PaintHook.Repaint();" +
                                                    "       previousYellowStatsInLowerLeftText = textToDraw;" +
                                                    "   }" +
                                                    "   " +
                                                    "   if(textToDraw.matches(\"Say: .*\")) {" +
                                                    "       Mods.isChatting = true;" +
                                                    "       Mods.chatTimeStamp = System.currentTimeMillis();" +
                                                    "   }"+
                                                    "   if(textToDraw.indexOf(\" [\") > 0)" +
                                                    "       textToDraw = textToDraw.substring(0, textToDraw.indexOf(\" [\"));" +
                                                    "   if(World.myPlayer.name.equals(textToDraw)) {" +
                                                    "       int x = $2 + 4 + localGraphics.getFontMetrics().stringWidth($1) / 2;" +
                                                    "       int y = $3 + 108;" +
                                                    "       World.myPlayer.location = new java.awt.Point(x, y);" +
                                                    "       if(PaintHook.aberothFontMetrics == null)\n" +
                                                    "            PaintHook.aberothFontMetrics = localGraphics.getFontMetrics();" +
                                                    "   }" +
                                                    "   $_ = $proceed($$);" +
                                                    "}");
                                        } catch (Exception e) {
                                            System.out.println("Cannot compile expression! " + e.toString());
                                        }
                                }
                            });
                        }

                    }

                    cl.detach();
                    return cl.toBytecode();

                } else if (cl.getName().contains("gameclient.GameClient")) {

                    // get class methods
                    CtMethod[] methods = cl.getDeclaredMethods();

                    // loop through class methods
                    for (int i = 0; i < methods.length; i++) {
                        if (methods[i].toString().contains("public final b ()V")) {
                            methods[i].instrument(new ExprEditor() {
                                public void edit(NewExpr m) throws CannotCompileException {
                                    if(m.getClassName().matches("a.c"))
                                        m.replace("Mods.C = $proceed($$); $_ = Mods.C;");
                                }
                                public void edit(MethodCall m) throws CannotCompileException {
                                    if (m.getMethodName().matches("c")) {
                                        m.replace("Mods.LoggedOut(); $_ = $proceed();");
                                    }
                                }
                            });
                        }
                    }

                    cl.detach();
                    return cl.toBytecode();

                } else if (cl.getName().contains("gameclient.c")) {

                    CtConstructor[] constra = cl.getConstructors();
                    constra[0].insertBefore("Mods.A = $1;");

                    // get class methods
                    CtMethod[] methods = cl.getDeclaredMethods();

                    // loop through class methods
                    for (int i = 0; i < methods.length; i++) {

                        if (methods[i].toString().contains("mousePressed")) {

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

                } else if (cl.getName().contains("gameclient.a")) {

                    CtConstructor[] constra = cl.getConstructors();
                    constra[0].insertAfter("Mods.kL = Mods.A.getKeyListeners()[0];" +
                                            "Mods.mL = Mods.A.getMouseListeners()[0];");

                    // get class methods
                    CtMethod[] methods = cl.getDeclaredMethods();

                    // loop through class methods
                    for (int i = 0; i < methods.length; i++) {
                        if (methods[i].toString().contains("private a (Ljava/awt/Graphics;)V")) {

                            methods[i].insertAfter("PaintHook.Paint($1);");

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