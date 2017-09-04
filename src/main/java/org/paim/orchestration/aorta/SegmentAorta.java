package org.paim.orchestration.aorta;

import java.util.List;
import java.util.stream.Collectors;
import org.paim.commons.Exam;
import org.paim.commons.Image;
import org.paim.commons.ImageHelper;
import org.paim.commons.Point;
import org.paim.orchestration.ExamResult;
import org.paim.orchestration.StructureType;
import org.paim.pdi.BinaryLabelingProcess;
import org.paim.pdi.GaussianBlurProcess;
import org.paim.pdi.ThresholdLimitProcess;
import org.paim.pdi.ThresholdProcess;

/**
 * Identifies and segments the Aorta in the exam
 */
public class SegmentAorta {

    /** Exam */
    private final Exam exam;
    /** Exam result */
    private final ExamResult result;

    /**
     * Creates the flow for the Aorta segmentation
     * 
     * @param exam
     * @param result 
     */
    public SegmentAorta(Exam exam, ExamResult result) {
        this.exam = exam;
        this.result = result;
    }

    /**
     * Segments the Aorta
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
        int w = image.getWidth();
        int h = image.getHeight();
        // Blurs the image
        GaussianBlurProcess gauss = new GaussianBlurProcess(image, /* Sigma = */ 1.76, /* Mask size = */ 5);
        gauss.process();
        // Apply a threshold painting the range 100-2000 HU in white, all the rest in black
        ThresholdLimitProcess limit = new ThresholdLimitProcess(gauss.getOutput(), 100, 2000, -4000, -4000, 4000);
        limit.process();
        // Binarizes the image
        image = ImageHelper.create(limit.getOutput(), new org.paim.commons.Range<>(0, 1));
        ThresholdProcess process = new ThresholdProcess(image, 100);
        process.process();
        image = ImageHelper.create(process.getOutput(), new org.paim.commons.Range<>(-4000, 4000));
        // Runs the labeling
        BinaryLabelingProcess binaryLabelingProcess = new BinaryLabelingProcess(image);
        binaryLabelingProcess.process();
        // Extracts the object
        List<BinaryLabelingProcess.ExtractedObject> objects = binaryLabelingProcess.getExtractedObjects();
        // Filters the objects
        objects = objects.stream().filter((object) -> object.getSize() > 1000 && object.getSize() < 2500).collect(Collectors.toList());
        objects = objects.stream().filter((object) -> object.getCircularity() > 0.7).collect(Collectors.toList());
        objects = objects.stream().filter((object) -> object.getBounds().center().x > w / 4).collect(Collectors.toList());
        objects = objects.stream().filter((object) -> object.getBounds().center().x < w / 4 + w / 2).collect(Collectors.toList());
        // If the aorta is not in the image, no objects will meet the filters
        if (objects.isEmpty()) {
            return;
        }
        // Sort the objects by the distance to the center of the image
        Point imageCenter = new Point(w / 2, h / 2);
        objects.sort((o1, o2) -> {
            double d1 = o1.getBounds().center().distance(imageCenter);
            double d2 = o2.getBounds().center().distance(imageCenter);
            return d2 < d1 ? 1 : -1;
        });
        // Fills the structure
        result.getStructure(StructureType.AORTA).getSlice(sliceIndex).setBinaryLabel(objects.get(0).getMatrix());
    }

}
