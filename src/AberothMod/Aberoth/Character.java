package AberothMod.Aberoth;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Character extends AberothEntity {

    public enum Friendliness {ALLY, FRIENDLY, NEUTRAL, UNFRIENDLY, ARCHENEMY}

    public Point location;
    public boolean attackable;
    public Friendliness friendliness;
    public int health;
    public int totalLevel;
    public int life;
    public int bindWounds;
    public int drinking;
    public int dagger;
    public int shield;
    public int blunt;
    public int axe;
    public int magic;

    // AberothMod variables
    public java.awt.Color nameColor;

    public Character(String name) {
        super(name);
    }

    public Character(String name, int _totalLevel) {
        super(name);

        totalLevel = _totalLevel;
        nameColor = null;
    }

    public void SetColor(java.awt.Color _nameColor)
    {
        nameColor = _nameColor;
    }

}