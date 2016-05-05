package AberothMod;

import AberothMod.Aberoth.World;

import java.awt.*;
import java.util.Iterator;
import java.util.Map;

public class PaintHook {

    private static boolean repaint = false;
    public static FontMetrics aberothFontMetrics;
    private static Graphics graphics;

    public static void Repaint() {
        repaint = true;
    }

    public static boolean ShouldRepaint() {
        return repaint;
    }

    public static void JustRepainted() {
        repaint = false;
    }

    private static void drawText(String text, int x, int y) {
        graphics.setColor(new Color(0, 0, 0));
        for(int i1 = -1; i1 < 2; i1++) {
            for(int i2 = -1; i2 < 2; i2++) {
                graphics.drawString(text, x+i1, y+i2);
            }
        }
        graphics.setColor(new Color(255, 255, 255));

        graphics.drawString(text, x, y);
    }

    public static void Paint(Graphics _graphics) {
        if(aberothFontMetrics != null) {
            graphics = _graphics;
            Font font = graphics.getFont();
            Color color = graphics.getColor();

            graphics.setFont(null);

            // set mat private messages based on font metrics and screen ratio
            if (Mods.privateMessaging.GetMaxPrivateMessages() == 0)
                Mods.privateMessaging.SetMaxPrivateMessages((int) ((World.screenHeight) / (aberothFontMetrics.getHeight() + 15 * World.screenRatio)));

            // draw MessageUpdate
            if (Mods.modChat || Mods.drawStringTime > System.currentTimeMillis())
                drawText(Mods.stringToDraw, 10, (int) ((aberothFontMetrics.getHeight() + (aberothFontMetrics.getHeight() / 5.6 + 18 * World.screenRatio))));

            // draw private messages
            int y = (int) (aberothFontMetrics.getHeight() + (aberothFontMetrics.getHeight() / 5.6 + 17 * World.screenRatio)*2);
            Map<Long, String> messages = Mods.privateMessaging.GetPrivateMessagesToDisplay().descendingMap();
            Iterator<Map.Entry<Long, String>> iterator = messages.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<Long, String> entry = iterator.next();
                drawText(entry.getValue(), 10, y);
                y += aberothFontMetrics.getHeight() + aberothFontMetrics.getHeight() / 5.6 * World.screenRatio;
            }

            graphics.setFont(font);
            graphics.setColor(color);
        }
    }

}
