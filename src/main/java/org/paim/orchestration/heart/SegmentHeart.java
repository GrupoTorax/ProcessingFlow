package org.paim.orchestration.heart;

import org.paim.commons.BinaryImage;
import org.paim.commons.Exam;
import org.paim.commons.Image;
import org.paim.commons.ImageFactory;
import org.paim.commons.ImageHelper;
import org.paim.orchestration.ExamResult;
import org.paim.orchestration.StructureType;

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

        
        result.getSlice(sliceIndex).getStructure(StructureType.HEART).setBinaryLabel(binaryMatrix);
    }

}
