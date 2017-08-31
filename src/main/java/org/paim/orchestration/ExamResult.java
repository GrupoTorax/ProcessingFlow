package org.paim.orchestration;

import java.util.HashMap;
import java.util.Map;
import org.paim.commons.Exam;

/**
 * Exam result
 */
public class ExamResult {
    
    /** Structures contained in this result */
    private final Map<StructureType, Structure> structures;
    /** Original exam */
    private final Exam exam;

    /**
     * Creates a new exam result
     * 
     * @param exam 
     */
    public ExamResult(Exam exam) {
        structures = new HashMap<>();
        for (StructureType key : StructureType.values()) {
            structures.put(key, new Structure(key, exam));
        }
        this.exam = exam;
    }

    /**
     * 
     * @return 
     */
    public Map<StructureType, Structure> getStructures() {
        return structures;
    }

    /**
     * Returns the structure by the type
     * 
     * @param structureType
     * @return 
     */
    public Structure getStructure(StructureType structureType) {
        return structures.get(structureType);
    }
    
    /**
     * Returns a slice of the exam
     * 
     * @param index
     * @return ExamResultSlice
     */
    public ExamResultSlice getSlice(int index) {
        Map<StructureType, StructureSlice> structures = new HashMap<>();
        for (Structure structure : this.structures.values()) {
            structures.put(structure.getType(), structure.getSlice(index));
        }
        return new ExamResultSlice(structures, exam.getExamSlice(index));
    }
    
}
