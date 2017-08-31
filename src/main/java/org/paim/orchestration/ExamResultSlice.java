package org.paim.orchestration;

import java.util.Map;
import org.paim.commons.ExamSlice;

/**
 * Exam result slice
 */
public class ExamResultSlice {
    
    /** Structures */
    private final Map<StructureType, StructureSlice> structures;
    /** Exam slice */
    private final ExamSlice exam;

    /**
     * Creates a new slice
     * 
     * @param structures
     * @param exam 
     */
    public ExamResultSlice(Map<StructureType, StructureSlice> structures, ExamSlice exam) {
        this.structures = structures;
        this.exam = exam;
    }

    /**
     * Returns the structures
     * 
     * @return {@code Map<StructureType, StructureSlice>}   
     */
    public Map<StructureType, StructureSlice> getStructures() {
        return structures;
    }

    /**
     * Returns a structure
     * 
     * @param type
     * @return StructureSlice
     */
    public StructureSlice getStructure(StructureType type) {
        return structures.get(type);
    }

    /**
     * Returns the exam
     * 
     * @return ExamSlice
     */
    public ExamSlice getExam() {
        return exam;
    }    

}
