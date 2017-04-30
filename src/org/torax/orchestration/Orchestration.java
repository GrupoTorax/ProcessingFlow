package org.torax.orchestration;

import org.torax.commons.Exam;
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
        ExamResult exameSegmentado = new ExamResult(exam);
        SegmentLungs segPul = new SegmentLungs(exam, exameSegmentado);
        segPul.segmenta();
        return exameSegmentado;
    }
}
