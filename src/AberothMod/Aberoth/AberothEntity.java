package AberothMod.Aberoth;

import java.awt.*;

public class AberothEntity {

    public String name;
    public Rectangle bounds;

    public AberothEntity(String _name) {
        name = _name;
    }

    public AberothEntity(String _name, Rectangle _bounds) {
        name = _name;
        bounds = _bounds;
    }

}