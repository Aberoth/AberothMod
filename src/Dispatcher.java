import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

public class Dispatcher implements KeyEventDispatcher {

    private double lastHButtonPressTime = 0;
    private double lastRButtonPressTime = 0;
    private double lastGButtonPressTime = 0;

    @Override
    public boolean dispatchKeyEvent(KeyEvent e) {
        if (AberothMod.mL != null) {
            if(AberothMod.isChatting) {
                if((""+e.getKeyChar()).matches("^[A-Za-z0-9!@#$%^&*)(-]$") && e.getKeyLocation() == 0)
                    AberothMod.chatMessageBeingSent += e.getKeyChar();
            } else if (AberothMod.canDraw && e.getKeyChar() == 'g' && e.getKeyLocation() == 0) {
                if (System.currentTimeMillis() - lastGButtonPressTime <= 500) {

                    AberothMod.pickupNearbyItems = !AberothMod.pickupNearbyItems;
                    if (AberothMod.pickupNearbyItems)
                        AberothMod.MessageUpdate("Auto-pickup activated.");
                    else
                        AberothMod.MessageUpdate("Auto-pickup deactivated.");



                } else
                    lastGButtonPressTime = System.currentTimeMillis();
            } else if (AberothMod.A != null && AberothMod.canDraw && e.getKeyChar() == '`' && e.getKeyLocation() == 0
                    && AberothMod.CoordsOverItem(new Point(AberothMod.A.getMousePosition().x, AberothMod.A.getMousePosition().y))) {
                AberothMod.ItemProtection(new Point(AberothMod.A.getMousePosition().x, AberothMod.A.getMousePosition().y));
            } else if (AberothMod.A != null && AberothMod.canDraw && e.getKeyChar() == '\t' && e.getKeyLocation() == 0) {
                AberothMod.DropItems();
            } else if (AberothMod.canDraw && e.getKeyCode() == KeyEvent.VK_H && e.getID() == KeyEvent.KEY_RELEASED) {
                if (System.currentTimeMillis() - lastHButtonPressTime <= 500) {
                    if ((e.getModifiers() & KeyEvent.CTRL_MASK) == 0) {
                        AberothMod.attackAble = !AberothMod.attackAble;
                        if (AberothMod.attackAble)
                            AberothMod.MessageUpdate("Auto-attack activated.");
                        else
                            AberothMod.MessageUpdate("Auto-attack deactivated.");
                    } else {
                        AberothMod.moveAble = !AberothMod.moveAble;

                        if (AberothMod.moveAble)
                            AberothMod.MessageUpdate("Auto-move activated.");
                        else
                            AberothMod.MessageUpdate("Auto-move deactivated.");
                    }

                } else
                    lastHButtonPressTime = System.currentTimeMillis();
            } else if (AberothMod.canDraw && e.getKeyChar() == 'r' && e.getKeyLocation() == 0) {
                if (System.currentTimeMillis() - lastRButtonPressTime <= 500) {

                    AberothMod.mL.mousePressed(AberothMod.createMouseEventDown(AberothMod.A, InputEvent.BUTTON1_MASK,
                            100 + AberothMod.randomGenerator.nextInt(20), 100 + AberothMod.randomGenerator.nextInt(20)));

                    AberothMod.MessageUpdate("Mouse held.");
                } else
                    lastRButtonPressTime = System.currentTimeMillis();
            } else if (AberothMod.canDraw && e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                AberothMod.drawStringTime = System.currentTimeMillis();
            }

        }
        return false;
    }
}
