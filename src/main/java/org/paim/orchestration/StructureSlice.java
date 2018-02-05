package org.paim.orchestration;

import org.paim.commons.BinaryImage;
import org.paim.commons.ExamSlice;
import org.paim.commons.ImageFactory;

/**
 * Slice of a structure, such as lungs, heart etc.
 */
public class StructureSlice {

    /** Binary label that represents the structure slice */
    private BinaryImage binaryLabel;
    /** Area */
    private int area;

    /**
     * Creates a new structure
     *
     * @param binaryLabel
     */
    public StructureSlice(BinaryImage binaryLabel) {
        setBinaryLabel(binaryLabel);
    }

    /**
     * Creates a new empty slice based on the exam
     * @param exam 
     */
    public StructureSlice(ExamSlice exam) {
        setBinaryLabel(ImageFactory.buildBinaryImage(exam.getColumns(), exam.getColumns()));
    }

    /**
     * Sets the binary label of this object
     * 
     * @param binaryLabel 
     */
    public final void setBinaryLabel(BinaryImage binaryLabel) {
        this.binaryLabel = binaryLabel;
        area = 0;
        for (int x = 0; x < binaryLabel.getWidth(); x++) {
            for (int y = 0; y < binaryLabel.getHeight(); y++) {
                if (binaryLabel.get(x, y)) {
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
        if (binaryLabel.get(x, y) != value) {
            if (value) {
                area++;
            } else {
                area--;
            }
            binaryLabel.set(x, y, value);
        }
    }

    /**
     * Returns the binary label
     * 
     * @return BinaryImage
     */
    public BinaryImage getBinaryLabel() {
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
        return binaryLabel.getWidth();
    }

    /**
     * Returns the height of the slice
     * 
     * @return int
     */
    public int getHeight() {
        return binaryLabel.getHeight();
    }

}
