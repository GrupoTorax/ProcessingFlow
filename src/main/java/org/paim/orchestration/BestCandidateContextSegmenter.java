/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.paim.orchestration;

import java.util.List;
import org.paim.commons.BinaryImage;
import org.paim.commons.Exam;

/**
 * ContextSegmenter that just chooses the best candidate
 */
public class BestCandidateContextSegmenter implements ContextSegmenter {

    @Override
    public BinaryImage chooseCandidate(List<BinaryImage> candidates, int slice) {
        return candidates.get(0);
    }

    @Override
    public void initialize(Exam exam, ExamResult result, List<List<BinaryImage>> allSlicesCandidates) {
    }

}
