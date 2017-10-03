package org.paim.orchestration;

import java.util.List;
import org.paim.commons.BinaryImage;
import org.paim.commons.Exam;

/**
 * Segmenter based on the conext
 */
public interface ContextSegmenter {

    /**
     * Initialize the segmenter
     * 
     * @param exam
     * @param result
     * @param allSlicesCandidates 
     */
    public void initialize(Exam exam, ExamResult result, List<List<BinaryImage>> allSlicesCandidates);
    
    /**
     * Chooses a candidate from the candidate list based on the context
     * 
     * @param candidates
     * @param slice
     * @return BinaryImage
     */
    public BinaryImage chooseCandidate(List<BinaryImage> candidates, int slice);
    
}
