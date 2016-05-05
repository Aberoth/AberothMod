package AberothMod;

import AberothMod.Aberoth.World;

import java.util.ArrayList;

public class Commands {
    
    public static void CheckForCommand(String command) {

        if(command.matches("(?i)^/help")) {
            Mods.privateMessaging.ClearPrivateMessages();

            long i = System.currentTimeMillis();
            Mods.privateMessaging.DisplayMessageOnScreen("/motd  -  shows version info", i--);
            Mods.privateMessaging.DisplayMessageOnScreen("/pm Playername Message  -  private message a player", i--);
            Mods.privateMessaging.DisplayMessageOnScreen("/reply Message (/r Message)  -  reply to a pm", i--);
            Mods.privateMessaging.DisplayMessageOnScreen("/level Playername  -  get a player's level", i--);
            Mods.privateMessaging.DisplayMessageOnScreen("/colors  -  turn colors on and off", i--);
            Mods.privateMessaging.DisplayMessageOnScreen("/ignore Playername  -  ignore a player's pm's", i--);
            Mods.privateMessaging.DisplayMessageOnScreen("/unignore Playername  -  unignore a player", i--);
            Mods.privateMessaging.DisplayMessageOnScreen("/ignorelist  -  list ignored players", i--);
            Mods.privateMessaging.DisplayMessageOnScreen("/clear  -  clear messages on screen", i--);
            Mods.privateMessaging.DisplayMessageOnScreen("/pickup  -  turn auto-pickup on and off", i--);
            Mods.privateMessaging.DisplayMessageOnScreen("/coinflip  -  flip 'dem coins", i--);
            Mods.privateMessaging.DisplayMessageOnScreen("/help  -  shows this", i--);

            Mods.privateMessaging.showingMessage = true;
        } else if (command.matches("(?i)^/MOTD")) {
            Mods.privateMessaging.ShowMOTD(false);
        } else if (command.matches("(?i)^/pm [a-zA-Z]+ .+") || command.matches("/message [a-zA-Z]+ .+")) {
            String[] splitter = command.split(" ", 3);
            String playerName = splitter[1];
            playerName = Character.toUpperCase(playerName.charAt(0)) + playerName.toLowerCase().substring(1);
            String message = splitter[2];
            if(message.length() < 200)
                Mods.privateMessaging.AddPrivateMessage(playerName, message);
            else
                Mods.MessageUpdate("Message too long to send.");
        } else if (command.matches("(?i)^/r .+") || command.matches("/reply .+")) {
            if (Mods.privateMessaging.GetLastPlayerWhoMessaged() != "")
                Mods.privateMessaging.AddPrivateMessage(Mods.privateMessaging.GetLastPlayerWhoMessaged(), command.split(" ", 2)[1]);
            else
                Mods.MessageUpdate("Noticed.");
        } else if(command.matches("(?i)^/level [a-zA-Z]+")) {
            String playerName = command.split(" ", 2)[1];
            playerName = Character.toUpperCase(playerName.charAt(0)) + playerName.toLowerCase().substring(1);

            int totalLevel = Mods.getEntityLevel(playerName);

            if(totalLevel > 0)
                Mods.MessageUpdate(playerName + "'s total level is " + Mods.getEntityLevel(playerName) + ".");
            else
                Mods.MessageUpdate("\"" + playerName + "\" not found in high score list.");
        } else if(command.matches("(?i)^/colors")) {
            Mods.colors = !Mods.colors;
            if(Mods.colors)
                Mods.MessageUpdate("Colors activated.");
            else
                Mods.MessageUpdate("Colors deactivated.");
            Settings.SaveSettings();
        } else if(command.matches("(?i)^/ignore [a-zA-Z]+")) {
            String playerName = command.split(" ", 2)[1];
            playerName = Character.toUpperCase(playerName.charAt(0)) + playerName.toLowerCase().substring(1);
            if(!Mods.privateMessaging.ignoreList.contains(playerName)) {
                if(playerName.length() < 20) {
                    Mods.privateMessaging.ignoreList.add(playerName);
                    Mods.MessageUpdate(playerName + " ignored.");
                    Settings.SaveSettings();
                } else
                    Mods.MessageUpdate("Invalid player name.");
            } else
                Mods.MessageUpdate("Already ignoring "+playerName+".");
        } else if(command.matches("(?i)^/unignore [a-zA-Z]+")) {
            String playerName = command.split(" ", 2)[1];
            playerName = Character.toUpperCase(playerName.charAt(0)) + playerName.toLowerCase().substring(1);
            if(Mods.privateMessaging.ignoreList.contains(playerName)) {
                Mods.privateMessaging.ignoreList.remove(playerName);
                Mods.MessageUpdate(playerName+" unignored.");
                Settings.SaveSettings();
            } else
                Mods.MessageUpdate("\""+playerName+"\" is not ignored.");
        } else if(command.matches("(?i)^/ignorelist")) {
            Mods.privateMessaging.ClearPrivateMessages();
            Mods.privateMessaging.DisplayMessageOnScreen("Ignore List:", System.currentTimeMillis() + 10);

            ArrayList<String> playerNames = new ArrayList<>();
            playerNames.add("");
            int indexCount = 0;
            for(String playerName : Mods.privateMessaging.ignoreList) {
                if(playerNames.get(indexCount) == "")
                    playerNames.set(indexCount, playerName);
                else {
                    playerNames.set(indexCount, playerNames.get(indexCount)+", "+playerName);
                }
                if(PaintHook.aberothFontMetrics.stringWidth(playerNames.get(indexCount)) > (World.screenWidth)/2) {
                    indexCount++;
                    playerNames.add("");
                }
            }
            for(int i = 0; i < playerNames.size(); i++)
                Mods.privateMessaging.DisplayMessageOnScreen(playerNames.get(i), System.currentTimeMillis() - i);
            Mods.privateMessaging.showingMessage = true;
        } else if(command.matches("(?i)^/clear")) {
            Mods.privateMessaging.ClearPrivateMessages();
        } else if(command.matches("(?i)^/pickup")) {
            Mods.pickupNearbyItems = !Mods.pickupNearbyItems;
            if(Mods.pickupNearbyItems)
                Mods.MessageUpdate("Auto-pickup activated.");
            else
                Mods.MessageUpdate("Auto-pickup deactivated.");
        } else if(command.matches("(?i)^/coinflip")) {
            Mods.coinFlip = !Mods.coinFlip;
            if(Mods.coinFlip)
                Mods.MessageUpdate("Coin-flip activated.");
            else
                Mods.MessageUpdate("Coin-flip deactivated.");
        } else {
            Mods.MessageUpdate("Invalid command.");
            System.out.println(command);
        }
    }
    
}
