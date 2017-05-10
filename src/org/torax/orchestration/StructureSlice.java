package org.torax.orchestration;

import org.torax.commons.ExamSlice;

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
     * Creates a new empty slice based on the exam
     * @param exam 
     */
    public StructureSlice(ExamSlice exam) {
        boolean[][] label = new boolean[exam.getSize().width][exam.getSize().height];
        for (int i = 0; i < exam.getSize().width; i++) {
            for (int j = 0; j < exam.getSize().height; j++) {
                label[i][j] = false;
            }
        }
        setBinaryLabel(label);
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
     * Sets the value of a point
     * 
     * @param x
     * @param y
     * @param value
     */
    public final void setPoint(int x, int y, boolean value) {
        if (binaryLabel[x][y] != value) {
            if (value) {
                area++;
            } else {
                area--;
            }
            binaryLabel[x][y] = value;
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

    /**
     * Returns the width of the slice
     * 
     * @return int
     */
    public int getWidth() {
        return binaryLabel.length;
    }

    /**
     * Returns the height of the slice
     * 
     * @return int
     */
    public int getHeight() {
        return binaryLabel[0].length;
    }

}
