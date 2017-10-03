
package org.paim.orchestration;

import java.awt.Color;

/**
 * Type of structure
 */
public enum StructureType {

    LEFT_LUNG("Left lung", new Color(0x1B4079)),
    RIGHT_LUNG("Right lung", new Color(0x51A3A3)),
    HEART("Heart", new Color(0xA63446)),
    AORTA("Aorta", new Color(0x16DB93)),
    ;
    
    /** Name of the structure */
    private final String name;
    /** Color that represents the structure */
    private final Color color;

    /**
     * Creates a new Structure type
     * 
     * @param name
     * @param color 
     */
    private StructureType(String name, Color color) {
        this.name = name;
        this.color = color;
    }

    /**
     * Returns the name of the structure
     * 
     * @return String
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the color that represents the structure
     * 
     * @return Color
     */
    public Color getColor() {
        return color;
    }
    
}
