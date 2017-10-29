package org.paim.orchestration;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import org.paim.commons.BinaryImage;
import org.paim.commons.Exam;

/**
 * Segments a structure
 */
public class SliceStructureSegmenter {
    
    /** Exam */
    private final Exam exam; 
    /** Exam result */
    private final ExamResult result;

    /**
     * Creates a new SliceStructureSegmenter
     * 
     * @param exam
     * @param result
     */
    public SliceStructureSegmenter(Exam exam, ExamResult result) {
        this.exam = exam;
        this.result = result;
    }
    
    /**
     * Segments a structure index by index
     * 
     * @param type
     * @param segmenter
     * @param contextSegmenter
     */
    public void process(StructureType type, StructureSegmenter segmenter, ContextSegmenter contextSegmenter) {
        List<List<BinaryImage>> candidatesPerSlice = new ArrayList<>();
        for (int sliceIndex = 0; sliceIndex < exam.getNumberOfSlices(); sliceIndex++) {
            candidatesPerSlice.add(segmenter.segmentSlice(sliceIndex, exam.getExamSlice(sliceIndex), result.getSlice(sliceIndex)));
        }
        contextSegmenter.initialize(exam, result, candidatesPerSlice);
        IntStream.range(0, exam.getNumberOfSlices()).parallel().forEach(sliceIndex -> {
            List<BinaryImage> candidates = candidatesPerSlice.get(sliceIndex);
            if (candidates == null || candidates.isEmpty()) {
                return;
            }
            BinaryImage choice = contextSegmenter.chooseCandidate(candidates, sliceIndex);
            if (choice != null) {
                result.getSlice(sliceIndex).getStructure(type).setBinaryLabel(choice);
            }
        });
    }
    
}
