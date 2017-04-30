package org.torax.orchestration;

import org.torax.commons.Exam;

/**
 * Exam result
 */
public class ExamResult {

    private final ExamResultSlice[] fatiaExameSegmentados;

    public ExamResult(Exam exam) {
        int tamanho = exam.getNumberOfSlices();
        fatiaExameSegmentados = new ExamResultSlice[tamanho];
        for (int i = 0; i < tamanho; i++) {
            fatiaExameSegmentados[i] = new ExamResultSlice();
        }
    }

    public ExamResultSlice getFatiaExameSegmentado(int indice) {
        return fatiaExameSegmentados[indice];
    }
}
