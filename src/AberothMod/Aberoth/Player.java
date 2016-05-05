package AberothMod.Aberoth;

public class Player extends Character {

    public double focusExperience;
    public double lifeExperience;
    public double bindWoundsExperience;
    public double drinkingExperience;
    public double daggerExperience;
    public double shieldExperience;
    public double bluntExperience;
    public double axeExperience;
    public double magicExperience;

    // AberothMod variables
    public int HTMLDownloadCount;

    public Player(String name) {
        super(name);
    }

    public Player(String name, int totalLevel, int _HTMLDownloadCount) {
        super(name, totalLevel);

        HTMLDownloadCount = _HTMLDownloadCount;
    }

}