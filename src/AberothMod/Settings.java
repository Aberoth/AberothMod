package AberothMod;

import AberothMod.Aberoth.World;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.util.ArrayList;
import java.util.Properties;

public class Settings {

    public static void SaveSettings() {
        // create Properties variable
        Properties oldProp = new Properties();
        InputStream input = null;

        Properties newProp = new Properties();
        OutputStream output = null;

        try {
            // file input name
            input = new FileInputStream("config.properties");

            // load the properties file
            oldProp.load(input);

            // close InputStream
            input.close();

            // file output name
            output = new FileOutputStream("config.properties");

            // create Gson
            Gson gson = new Gson();

            // set property values
            newProp.setProperty("Settings", gson.toJson("Saved!"));
            newProp.setProperty("postedMOTDVersion", gson.toJson(Mods.privateMessaging.motdVersion));
            newProp.setProperty("Colors", gson.toJson(Mods.colors));
            newProp.setProperty(World.myPlayer.name+"IgnoreList", gson.toJson(Mods.privateMessaging.ignoreList));

            // merge properties
            oldProp.putAll(newProp);

            // save properties to project root folder
            oldProp.store(output, null);

        } catch (IOException io) {
            io.printStackTrace();
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    public static void LoadSettings() {
        // create Properties variable
        Properties prop = new Properties();
        InputStream input = null;

        try {
            File file = new File("config.properties");
            if(!file.exists()) {
                file.createNewFile();
                Settings.SaveSettings();
            }

            // file input name
            input = new FileInputStream("config.properties");

            // load the properties file
            prop.load(input);

            // create Gson
            Gson gson = new Gson();

            // load property values
            if(prop.getProperty("Settings") != null) {
                Mods.postedMOTDVersion = gson.fromJson(prop.getProperty("postedMOTDVersion"), String.class);
                Mods.colors = gson.fromJson(prop.getProperty("Colors"), boolean.class);

                if(prop.getProperty(World.myPlayer.name+"IgnoreList") != null) {
                    ArrayList<String> playersToIgnore = gson.fromJson(prop.getProperty(World.myPlayer.name + "IgnoreList"), new TypeToken<ArrayList<String>>() {}.getType());
                    Mods.privateMessaging.ignoreList.addAll(playersToIgnore);
                }
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
