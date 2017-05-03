package org.torax.orchestration;

/**
 * Slice of a structure, such as lungs, heart etc.
 */
public class StructureSlice {

    /** Binary label that represents the structure slice */
    private boolean[][] binaryLabel;
    /** Area */
    private int area;

    /**
     * Creates a new structure
     *
     * @param binaryLabel
     */
    public StructureSlice(boolean[][] binaryLabel) {
        setBinaryLabel(binaryLabel);
    }

    /**
     * Sets the binary label of this object
     * 
     * @param binaryLabel 
     */
    public final void setBinaryLabel(boolean[][] binaryLabel) {
        this.binaryLabel = binaryLabel;
        area = 0;
        for (int i = 0; i < binaryLabel.length; i++) {
            for (int j = 0; j < binaryLabel[i].length; j++) {
                if (binaryLabel[i][j]) {
                    area++;
                }
            }
        }
    }

    /**
     * Returns the binary label
     * 
     * @return boolean[][]
     */
    public boolean[][] getBinaryLabel() {
        return binaryLabel;
    }

    /**
     * Returns the area in pixels
     * 
     * @return int
     */
    public int getArea() {
        return area;
    }

}
