/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.paim.orchestration;

import java.util.List;
import org.paim.commons.BinaryImage;
import org.paim.commons.Exam;
import org.paim.commons.Point;

/**
 *
 * @author Pichau
 */
public class ClosestToCentralityAverageContextSegmenter implements ContextSegmenter {

    /** Average center position */
    private Point averageCenter;

    @Override
    public void initialize(Exam exam, ExamResult result, List<List<BinaryImage>> allSlicesCandidates) {
        int averageX = 0;
        int averageY = 0;
        int processed = 0;
        for (List<BinaryImage> candidate : allSlicesCandidates) {
            if (candidate == null || candidate.isEmpty()) {
                continue;
            }
            averageX += candidate.get(0).getBounds().center().x;
            averageY += candidate.get(0).getBounds().center().y;
            processed++;
        }
        averageX = averageX / processed;
        averageY = averageY / processed;
        averageCenter = new Point(averageX, averageY);
    }

    @Override
    public BinaryImage chooseCandidate(List<BinaryImage> candidates, int slice) {
        candidates.sort((c1, c2) -> {
            return c1.getBounds().center().distance(averageCenter) - c2.getBounds().center().distance(averageCenter) < 0 ? -1 : 1;
        });
        return candidates.get(0);
    }
    
}
