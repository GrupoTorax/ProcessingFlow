package org.paim.orchestration.lungs;

import org.paim.commons.Exam;
import org.paim.orchestration.ExamResult;

/**
 * Identifies and segments the heart in the exam
 */
public class SegmentHeart {

    /** Exam */
    private final Exam exam;
    /** Exam result */
    private final ExamResult result;

    /**
     * Creates the flow for the heart segmentation
     * 
     * @param exam
     * @param result 
     */
    public SegmentHeart(Exam exam, ExamResult result) {
        this.exam = exam;
        this.result = result;
    }

    /**
     * Segments the heart
     */
    public void process() {
        for (int slideIndex = 0; slideIndex < exam.getNumberOfSlices(); slideIndex++) {
            segmentSlice(slideIndex);
        }
    }

    /**
     * Segments a specified slice
     * 
     * @param sliceIndex 
     */
    private void segmentSlice(int sliceIndex) {
        
    }

}
