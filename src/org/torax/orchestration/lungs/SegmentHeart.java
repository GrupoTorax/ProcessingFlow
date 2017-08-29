package org.torax.orchestration.lungs;

import org.torax.orchestration.ExamResult;
import org.torax.commons.Exam;
import org.torax.commons.Image;
import org.torax.commons.ImageHelper;
import org.torax.orchestration.StructureType;
import org.torax.pdi.BinaryLabelingProcess;
import org.torax.pdi.BinaryLabelingProcess.ObjectList;
import org.torax.pdi.GaussianBlurProcess;
import org.torax.pdi.HistogramProcess;
import org.torax.pdi.ShadowCastingProcess;
import org.torax.pdi.ThresholdEdgeTrimProcess;
import org.torax.pdi.ThresholdLimitProcess;
import org.torax.pdi.ThresholdProcess;

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
