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

                AberothMod.championListHTML = a.toString();

                AberothMod.HTMLCount = AberothMod.HTMLCount + 1;

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
            if (AberothMod.A != null) {

                if (AberothMod.kL == null) {
                    AberothMod.kL = AberothMod.A.getKeyListeners()[0];
                }

                if (AberothMod.mL == null) {
                    AberothMod.mL = AberothMod.A.getMouseListeners()[0];
                }

                if (AberothMod.attackAble) {

                    if (AberothMod.A.getFocusListeners().length > 0) {

                        AberothMod.A.removeFocusListener(AberothMod.A.getFocusListeners()[0]);
                    }

                    if (AberothMod.Fire == true && AberothMod.moveAble) {
                        if (upMove == true) {
                            upMove = false;

                            AberothMod.kL.keyPressed(AberothMod.createKeyEventDown(AberothMod.A, 'W', KeyEvent.VK_W));
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            AberothMod.kL.keyReleased(AberothMod.createKeyEventUp(AberothMod.A, 'W', KeyEvent.VK_W));
                        } else {
                            upMove = true;

                            AberothMod.kL.keyPressed(AberothMod.createKeyEventDown(AberothMod.A, 'D', KeyEvent.VK_S));
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            AberothMod.kL.keyReleased(AberothMod.createKeyEventUp(AberothMod.A, 'D', KeyEvent.VK_S));
                        }
                    }

                    if (AberothMod.Fire == true && AberothMod.MouseDown == false) {

                        AberothMod.mL.mousePressed(AberothMod.createMouseEventDown(AberothMod.A, InputEvent.BUTTON1_MASK,
                                100 + AberothMod.randomGenerator.nextInt(20), 100 + AberothMod.randomGenerator.nextInt(20)));

                        AberothMod.MouseDown = true;

                    } else if (AberothMod.MouseDown && AberothMod.Fire == false) {

                        AberothMod.SendChatMessage("hide");
                        AberothMod.mL.mouseReleased(AberothMod.createMouseEventUp(AberothMod.A, InputEvent.BUTTON1_MASK, 0, 0));

                        AberothMod.MouseDown = false;
                    }
                }

                if (AberothMod.Fire == true) {
                    AberothMod.Fire = false;
                }
            }
        }

    }

    static class pickupItemsTimer extends TimerTask {

        public void run() {
            if (AberothMod.A != null && AberothMod.pickupNearbyItems) {

                for (int x = -12; x <= 12; x += 4) {
                    for (int y = -12; y <= 12; y += 4) {
                        if (x * x + y * y <= 12 * 12) {

                            AberothMod.mL.mousePressed(AberothMod.createMouseEventDown(AberothMod.A, InputEvent.BUTTON1_MASK, x + AberothMod.playerLocation.x,
                                    y + AberothMod.playerLocation.y));
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }

                AberothMod.mL.mouseReleased(AberothMod.createMouseEventUp(AberothMod.A, InputEvent.BUTTON1_MASK, 0, 0));
            }
        }
    }

    static class drawTimer extends TimerTask {

        public void run() {
            if (AberothMod.A != null && AberothMod.drawStringTime + 500 > System.currentTimeMillis())
                AberothMod.A.repaint(10, 40, 500, 50);
        }
    }

}
