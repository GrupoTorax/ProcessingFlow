package org.paim.orchestration.heart;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.paim.commons.BinaryImage;
import org.paim.commons.Bounds;
import org.paim.commons.ExamSlice;
import org.paim.commons.Image;
import org.paim.commons.ImageFactory;
import org.paim.commons.ImageHelper;
import org.paim.commons.Point;
import org.paim.orchestration.ExamResultSlice;
import org.paim.orchestration.StructureSegmenter;
import org.paim.orchestration.StructureType;
import org.paim.pdi.BinaryLabelingProcess;
import org.paim.pdi.DilationProcess;
import org.paim.pdi.ErosionProcess;
import org.paim.pdi.ObjectList;
import org.paim.pdi.SnakeProcess;
import org.paim.pdi.ThresholdLimitProcess;
import org.paim.pdi.ZhangSuenProcess;

/**
 * Identifies and segments the heart in the exam
 */
public class SegmentHeart implements StructureSegmenter {

    @Override
    public List<BinaryImage> segmentSlice(int sliceIndex, ExamSlice slice, ExamResultSlice resultSlice) {
        // Creates the image
        Image image = ImageHelper.create(slice.getCoefficientMatrix(), new org.paim.commons.Range<>(-4000, 4000));
        // Obtains the label of the other structures
        BinaryImage aortaLabel = resultSlice.getStructure(StructureType.AORTA).getBinaryLabel();
        BinaryImage leftLungLabel = resultSlice.getStructure(StructureType.LEFT_LUNG).getBinaryLabel();
        BinaryImage rightLungLabel = resultSlice.getStructure(StructureType.RIGHT_LUNG).getBinaryLabel();
        int bottom = 0;
        for (int y = aortaLabel.getHeight() - 1; y >= 0; y--) {
            for (int x = 0; x < aortaLabel.getWidth(); x++) {
                if (aortaLabel.get(x, y)) {
                    bottom = y;
                    break;
                }
            }
        }
        int top = 0;
        for (int y = leftLungLabel.getHeight() - 1; y >= 0; y--) {
            for (int x = 0; x < leftLungLabel.getWidth(); x++) {
                if (leftLungLabel.get(x, y) || rightLungLabel.get(x, y)) {
                    top = y;
                    break;
                }
            }
        }
        BinaryImage binaryMatrix = ImageFactory.buildBinaryImage(image.getWidth(), image.getHeight());
        for (int x = 0; x < binaryMatrix.getWidth(); x++) {
            for (int y = top; y < bottom; y++) {
                binaryMatrix.set(x, y, true);
            }
        }
        // Removes the left lung and right lung from the heart area
        binaryMatrix = binaryMatrix.difference(leftLungLabel).difference(rightLungLabel);
        int leftTopX = 0;
        for (int y = leftLungLabel.getHeight() - 1; y >= 0; y--) {
            for (int x = 0; x < leftLungLabel.getWidth(); x++) {
                if (leftLungLabel.get(x, y)) {
                    leftTopX = x;
                    break;
                }
            }
        }
        for (int x = 0; x < leftTopX; x++) {
            for (int y = 0; y < leftLungLabel.getHeight(); y++) {
                if (leftLungLabel.get(x, y)) {
                    break;
                }
                binaryMatrix.set(x, y, false);
            }
        }
        int rightTopX = 0;
        for (int y = rightLungLabel.getHeight() - 1; y >= 0; y--) {
            for (int x = 0; x < rightLungLabel.getWidth(); x++) {
                if (rightLungLabel.get(x, y)) {
                    rightTopX = x;
                    break;
                }
            }
        }
        for (int x = rightTopX; x < image.getWidth(); x++) {
            for (int y = 0; y < rightLungLabel.getHeight(); y++) {
                if (rightLungLabel.get(x, y)) {
                    break;
                }
                binaryMatrix.set(x, y, false);
            }
        }
        BinaryLabelingProcess binaryLabeling = new BinaryLabelingProcess(binaryMatrix);
        binaryLabeling.process();
        ObjectList objects = binaryLabeling.getExtractedObjects().sortBySizeLargestFirst();
        if (objects.isEmpty()) {
            return null;
        }
        binaryMatrix = objects.get(0).getMatrix();
        // Apply a threshold painting the range 100-2000 HU in white, all the rest in black
        ThresholdLimitProcess limit = new ThresholdLimitProcess(image, -190, 30, -4000, -4000, 4000);
        limit.process();
        Image limitImage = limit.getOutput();
        binaryLabeling = new BinaryLabelingProcess(limitImage);
        binaryLabeling.process();
        objects = binaryLabeling.getExtractedObjects().sortBySizeLargestFirst();
        if (objects.isEmpty()) {
            return null;
        }
        BinaryImage thresholdedMatrix = objects.get(0).getMatrix();
        binaryMatrix = thresholdedMatrix.intersection(binaryMatrix);
        // Implementação do algoritmo de Figueiredo:
        Bounds bounds = binaryMatrix.getBounds();
        Point center = bounds.center();
        List<Integer> xPoints = new ArrayList<>();
        List<Integer> yPoints = new ArrayList<>();
        double r = Math.max(bounds.width, bounds.height) * 0.8;
        for (int i = 0; i < 360; i += 1) {
            int xHigherFrequency = -1;
            int yHigherFrequency = -1;
            int higherFrequency = 0;
            for (double lr = r * 0.2; lr < r; lr++) {
                int x = (int) ((double)center.x + lr * Math.cos(Math.toRadians(i)));
                int y = (int) ((double)center.y + lr * Math.sin(Math.toRadians(i)));
                if (bounds.contains(x, y) && binaryMatrix.get(x, y)) {
                    if (limitImage.get(0, x, y) > higherFrequency) {
                        higherFrequency = limitImage.get(0, x, y);
                        xHigherFrequency = x;
                        yHigherFrequency = y;
                    }
                    
                }
            }
            if (xHigherFrequency > 0) {
                xPoints.add(xHigherFrequency);
                yPoints.add(yHigherFrequency);
            }
        }
        binaryMatrix = ImageFactory.buildBinaryImage(binaryMatrix.getWidth(), binaryMatrix.getHeight());
        for (int i = 0; i < xPoints.size(); i++) {
            binaryMatrix.set(xPoints.get(i), yPoints.get(i), true);
        }
        // Two erosion processes
        ErosionProcess erosionProcessFirst = new ErosionProcess(binaryMatrix);
        erosionProcessFirst.process();
        ErosionProcess erosionProcessSecond = new ErosionProcess(erosionProcessFirst.getOutput());
        erosionProcessSecond.process();
        binaryMatrix = new BinaryImage(erosionProcessSecond.getOutput());
        // Two dilatation processes
        DilationProcess dilationProcessFirst = new DilationProcess(binaryMatrix);
        dilationProcessFirst.process();
        DilationProcess dilationProcessSecond = new DilationProcess(dilationProcessFirst.getOutput());
        dilationProcessSecond.process();
        binaryMatrix = new BinaryImage(dilationProcessSecond.getOutput());
        // Skeletonize
        ZhangSuenProcess zhangSuenProcess = new ZhangSuenProcess(binaryMatrix);
        zhangSuenProcess.process();
        binaryMatrix = new BinaryImage(zhangSuenProcess.getOutput());
        // Remove pontos sozinhos
        // TODO: Criar novo processo
        for (int x = 1; x < binaryMatrix.getWidth() - 1; x++) {
            for (int y = 1; y < binaryMatrix.getHeight() - 1; y++) {
                if (!binaryMatrix.get(x-1, y-1) && 
                        !binaryMatrix.get(x, y-1) && 
                        !binaryMatrix.get(x, y+1) &&
                        !binaryMatrix.get(x-1, y) && 
                        !binaryMatrix.get(x+1, y) && 
                        !binaryMatrix.get(x-1, y+1) &&
                        !binaryMatrix.get(x, y+1) &&
                        !binaryMatrix.get(x+1, y+1)) {
                    binaryMatrix.set(x, y, false);
                }
            }
        }
        // Two erosion processes
        erosionProcessFirst = new ErosionProcess(binaryMatrix);
        erosionProcessFirst.process();
        erosionProcessSecond = new ErosionProcess(erosionProcessFirst.getOutput());
        erosionProcessSecond.process();
        binaryMatrix = new BinaryImage(erosionProcessSecond.getOutput());
        // Two dilatation processes
        dilationProcessFirst = new DilationProcess(binaryMatrix);
        dilationProcessFirst.process();
        dilationProcessSecond = new DilationProcess(dilationProcessFirst.getOutput());
        dilationProcessSecond.process();
        binaryMatrix = new BinaryImage(dilationProcessSecond.getOutput());
        // Snake
        SnakeProcess snakeProcess = new SnakeProcess(binaryMatrix, 1000, 1, 1, 1);
        snakeProcess.process();
        return Arrays.asList(snakeProcess.getOutput());
    }

}
