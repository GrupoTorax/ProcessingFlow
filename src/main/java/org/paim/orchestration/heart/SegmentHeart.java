package org.paim.orchestration.heart;

import java.awt.Polygon;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import static jdk.nashorn.internal.objects.NativeArray.slice;
import org.paim.commons.BinaryImage;
import org.paim.commons.Bounds;
import org.paim.commons.Exam;
import org.paim.commons.Image;
import org.paim.commons.ImageFactory;
import org.paim.commons.ImageHelper;
import org.paim.commons.Point;
import org.paim.orchestration.ExamResult;
import org.paim.orchestration.StructureType;
import org.paim.pdi.BinaryLabelingProcess;
import org.paim.pdi.ThresholdLimitProcess;

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
        // Creates the image
        Image image = ImageHelper.create(exam.getExamSlice(sliceIndex).getCoefficientMatrix(), new org.paim.commons.Range<>(-4000, 4000));
        // Obtains the label of the other structures
        BinaryImage aortaLabel = result.getSlice(sliceIndex).getStructure(StructureType.AORTA).getBinaryLabel();
        BinaryImage leftLungLabel = result.getSlice(sliceIndex).getStructure(StructureType.LEFT_LUNG).getBinaryLabel();
        BinaryImage rightLungLabel = result.getSlice(sliceIndex).getStructure(StructureType.RIGHT_LUNG).getBinaryLabel();
        
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
        BinaryLabelingProcess.ObjectList objects = binaryLabeling.getExtractedObjects().sortBySizeLargestFirst();
        if (objects.isEmpty()) {
            return;
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
            return;
        }
        BinaryImage thresholdedMatrix = objects.get(0).getMatrix();
        binaryMatrix = thresholdedMatrix.intersection(binaryMatrix);

        // Implementação do algoritmo de Figueiredo:
        Bounds bounds = binaryMatrix.getBounds();
        Point center = bounds.center();
        
        List<Integer> xPoints = new ArrayList<>();
        List<Integer> yPoints = new ArrayList<>();
//        System.out.println(sliceIndex);
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
        Polygon polygon = new Polygon(array(xPoints), array(yPoints), xPoints.size());
        for (int x = 0; x < binaryMatrix.getWidth(); x++) {
            for (int y = 0; y < binaryMatrix.getHeight(); y++) {
                if (polygon.contains(x, y)) {
                    binaryMatrix.set(x, y, true);
                }
            }
        }
        result.getSlice(sliceIndex).getStructure(StructureType.HEART).setBinaryLabel(binaryMatrix);
    }
    
    /**
     * Converts a list to an array
     * 
     * @param ints
     * @return int[]
     */
    private int[] array(List<Integer> ints) {
        int[] array = new int[ints.size()];
        for (int i = 0; i < ints.size(); i++) {
            array[i] = ints.get(i);
        }
        return array;
    }

}
