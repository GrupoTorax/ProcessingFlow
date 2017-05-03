package org.torax.orchestration;

import java.util.HashMap;
import java.util.Map;
import org.torax.commons.Exam;

/**
 * Exam result
 */
public class ExamResult {
    
    /** Structures contained in this result */
    private final Map<StructureType, Structure> structures;

    /**
     * Creates a new exam result
     * 
     * @param exam 
     */
    public ExamResult(Exam exam) {
        structures = new HashMap<>();
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
        if (!structures.containsKey(structureType)) {
            structures.put(structureType, new Structure(structureType));
        }
        return structures.get(structureType);
    }
}
