package org.torax.orchestration;

import java.util.ArrayList;
import java.util.List;
import org.torax.commons.Exam;

/**
 * Structure, such as lungs, heart, etc
 */
public class Structure {

    /** Type of structure */
    private final StructureType type;
    /** Slices of this structure */
    private final List<StructureSlice> slices;

    /**
     * Creates a new structure
     *
     * @param type
     * @param exam
     */
    public Structure(StructureType type, Exam exam) {
        this.type = type;
        this.slices = new ArrayList<>();
        for (int i = 0; i < exam.getNumberOfSlices(); i++) {
            this.slices.add(new StructureSlice(exam.getExamSlice(i)));
        }
    }

    /**
     * Adds a slice
     * 
     * @param structureSlice 
     */
    public void addSlice(StructureSlice structureSlice) {
        slices.add(structureSlice);
    }

    /**
     * Returns the structure type
     * 
     * @return Structure type
     */
    public StructureType getType() {
        return type;
    }
    
    /**
     * Returns the slice in the specified index
     * 
     * @param index
     * @return StructureSlice
     */
    public StructureSlice getSlice(int index) {
        return slices.get(index);
    }

}
