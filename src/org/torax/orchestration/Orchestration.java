package org.torax.orchestration;

import org.torax.commons.Exam;
import org.torax.orchestration.lungs.SegmentAorta;
import org.torax.orchestration.lungs.SegmentHeart;
import org.torax.orchestration.lungs.SegmentLungs;

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
        SegmentLungs lungSegmentation = new SegmentLungs(exam, result);
        lungSegmentation.segmenta();
        SegmentAorta aortaSegmentation = new SegmentAorta(exam, result);
        aortaSegmentation.process();
        SegmentHeart heartSegmentation = new SegmentHeart(exam, result);
        heartSegmentation.process();
        return result;
    }
}
