package AberothMod;

import AberothMod.Aberoth.World;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.TimerTask;

public class Timers {

    static class updateChampionListTimer extends TimerTask {
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

                Mods.MostSkillfulHTML = a.toString();

                Mods.HTMLDownloadCount = Mods.HTMLDownloadCount + 1;

                System.out.println("Updated MostSkillfulHTML.");

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    static class pickupItemsTimer extends TimerTask {

        public void run() {
            if (Mods.A != null && Mods.pickupNearbyItems) {

                for (int x = -12; x <= 12; x += 4) {
                    for (int y = -12; y <= 12; y += 4) {
                        if (x * x + y * y <= 12 * 12) {

                            Mods.mL.mousePressed(Mods.createMouseEventDown(Mods.A, InputEvent.BUTTON1_MASK, x + World.myPlayer.location.x,
                                    y + World.myPlayer.location.y));
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }

                Mods.mL.mouseReleased(Mods.createMouseEventUp(Mods.A, InputEvent.BUTTON1_MASK, 0, 0));
            }
        }
    }

    static class coinFlipTimer extends TimerTask {
        public void run(){
            if (!Mods.isChatting && Mods.coinFlip) {
                Mods.SendChatMessage("coinflip");
            }
        }
    }

    static class drawTimer extends TimerTask {

        // variable used for black magic
        public static boolean drawStringOn = true;

        public void run() {
            // straight up black magic
            if(Mods.drawStringTime > System.currentTimeMillis()) {
                drawStringOn = true;
            } else if(drawStringOn) {
                drawStringOn = false;
                PaintHook.Repaint();
            }

            // check if no longer chatting
            if(Mods.isChatting && Mods.chatTimeStamp + 500 < System.currentTimeMillis())
                Mods.isChatting = false;

            // white magic
            if(Mods.A != null && PaintHook.ShouldRepaint()) {
                Mods.A.repaint();
                PaintHook.JustRepainted();
            }
        }
    }

}
