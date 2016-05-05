package AberothMod;

import AberothMod.Aberoth.Character;
import AberothMod.Aberoth.World;

import java.awt.*;
import java.awt.event.KeyEvent;

public class Dispatcher implements KeyEventDispatcher {

    private double lastShiftRelease = 0;

    @Override
    public boolean dispatchKeyEvent(KeyEvent e) {
        // if user released the Shift key
        if (e.getKeyCode() == KeyEvent.VK_SHIFT && e.getID() == KeyEvent.KEY_RELEASED) {
            lastShiftRelease = System.currentTimeMillis();

        } else if (Mods.canDraw && Mods.A != null && Mods.kL != null && Mods.mL != null) {

            // if user is typing a /command
            if(Mods.modChat) {
                // if user released the Enter key
                if(e.getKeyCode() == KeyEvent.VK_ENTER && e.getID() == KeyEvent.KEY_RELEASED) {
                    Mods.drawStringTime = System.currentTimeMillis();

                    Mods.modChatMessage = Mods.modChatMessage.replace("\n", "");
                    Commands.CheckForCommand(Mods.modChatMessage);

                    Mods.modChatMessage = "";
                    Mods.modChat = false;
                    PaintHook.Repaint();
                // else if user pressed the Escape key
                } else if(e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    Mods.drawStringTime = System.currentTimeMillis();

                    Mods.modChatMessage = "";
                    Mods.modChat = false;
                    PaintHook.Repaint();
                // else if user pressed the Back Space key
                } else if(e.getKeyCode() == KeyEvent.VK_BACK_SPACE && e.getID() == KeyEvent.KEY_PRESSED && Mods.modChatMessage.length() > 1) {
                    Mods.modChatMessage = Mods.modChatMessage.substring(0, Mods.modChatMessage.length() - 1);
                    Mods.MessageUpdate(Mods.modChatMessage);
                // else if its a key that is being pressed down
                } else if(e.getKeyLocation() == 0) {
                    java.lang.Character.UnicodeBlock block = java.lang.Character.UnicodeBlock.of(e.getKeyChar());
                    // if the key contains a printable char
                    if ((!java.lang.Character.isISOControl(e.getKeyChar())) &&
                            e.getKeyChar() != KeyEvent.CHAR_UNDEFINED &&
                            block != null &&
                            block != java.lang.Character.UnicodeBlock.SPECIALS) {
                        Mods.modChatMessage += e.getKeyChar();
                        Mods.MessageUpdate(Mods.modChatMessage);
                    }
                }

                // if user released the 'R' key
                if (Mods.coinFlip && e.getKeyCode() == KeyEvent.VK_R && e.getID() == KeyEvent.KEY_RELEASED)
                    Mods.coinFlip = false;

                return true;
            }

            // if user released the '/' key
            if (e.getKeyCode() == KeyEvent.VK_SLASH && e.getID() == KeyEvent.KEY_RELEASED && e.getModifiers() == 0 && lastShiftRelease + 250 <= System.currentTimeMillis()) {
                if (Mods.isChatting) {
                    Mods.kL.keyPressed(Mods.createKeyEventDown(Mods.A, KeyEvent.CHAR_UNDEFINED, KeyEvent.VK_ESCAPE));
                    Mods.kL.keyReleased(Mods.createKeyEventDown(Mods.A, KeyEvent.CHAR_UNDEFINED, KeyEvent.VK_ESCAPE));
                }
                Mods.modChat = true;
                Mods.modChatMessage = "/";
                Mods.MessageUpdate(Mods.modChatMessage);

            // user is chatting
            } else if(Mods.isChatting) {
                return false;

            // else if user released the Tab key
            } else if (e.getKeyCode() == KeyEvent.VK_TAB && e.getID() == KeyEvent.KEY_RELEASED) {
                Point coords = new Point(Mods.A.getMousePosition().x, Mods.A.getMousePosition().y);

                Character.Friendliness friendliness = World.myPlayer.friendliness;
                if(friendliness != null) {
                    if(friendliness == Character.Friendliness.FRIENDLY) {
                        Mods.SendChatMessage("unfriendly");
                        Mods.MessageUpdate("Became unfriendly!");
                    } else if(friendliness == Character.Friendliness.UNFRIENDLY) {
                        Mods.SendChatMessage("friendly");
                        Mods.MessageUpdate("Became friendly!");
                    }

                }

            // else if user pressed the Escape key
            } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                if(Mods.privateMessaging.showingMessage) {
                    Mods.privateMessaging.ClearPrivateMessages();
                    Mods.privateMessaging.showingMessage = false;
                }

                Mods.drawStringTime = System.currentTimeMillis();

                PaintHook.Repaint();
            }
        }

        return false;
    }

}