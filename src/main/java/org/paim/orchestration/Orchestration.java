package org.paim.orchestration;

import org.paim.commons.Exam;
import org.paim.orchestration.aorta.SegmentAorta;
import org.paim.orchestration.heart.SegmentHeart;
import org.paim.orchestration.lungs.SegmentLungs;

/**
 *
 * @author Nícolas Pohren
 */
public class Orchestration {

    private final Exam exam;

    public Orchestration(Exam exam) {
        this.exam = exam;
    }

    public ExamResult segmenta() {
        ExamResult result = new ExamResult(exam);
        SliceStructureSegmenter segmenter = new SliceStructureSegmenter(exam, result);
        SegmentLungs lungSegmentation = new SegmentLungs(exam, result);
        lungSegmentation.segmenta();
        segmenter.process(StructureType.AORTA, new SegmentAorta(), new ClosestToCentralityAverageContextSegmenter());
        segmenter.process(StructureType.HEART, new SegmentHeart(), new BestCandidateContextSegmenter());
        return result;
    }
    
}
