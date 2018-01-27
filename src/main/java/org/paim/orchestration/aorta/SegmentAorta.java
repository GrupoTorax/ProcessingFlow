package org.paim.orchestration.aorta;

import java.util.List;
import java.util.stream.Collectors;
import org.paim.commons.BinaryImage;
import org.paim.commons.ExamSlice;
import org.paim.commons.Image;
import org.paim.commons.ImageHelper;
import org.paim.commons.Point;
import org.paim.orchestration.ExamResultSlice;
import org.paim.pdi.BinaryLabelingProcess;
import org.paim.pdi.GaussianBlurProcess;
import org.paim.pdi.ThresholdLimitProcess;
import org.paim.pdi.ThresholdProcess;
import org.paim.orchestration.StructureSegmenter;
import org.paim.pdi.ExtractedObject;

/**
 * Identifies and segments the Aorta in the exam
 */
public class SegmentAorta implements StructureSegmenter {

    @Override
    public List<BinaryImage> segmentSlice(int sliceIndex, ExamSlice slice, ExamResultSlice resultSlice) {
        // Creates the image
        Image image = ImageHelper.create(slice.getCoefficientMatrix(), new org.paim.commons.Range<>(-4000, 4000));
        int w = image.getWidth();
        int h = image.getHeight();
        // Blurs the image
        GaussianBlurProcess gauss = new GaussianBlurProcess(image, /* Sigma = */ 1.76, /* Mask size = */ 5);
        gauss.process();
        // Apply a threshold painting the range 100-2000 HU in white, all the rest in black
        ThresholdLimitProcess limit = new ThresholdLimitProcess(gauss.getOutput(), 100, 3000, -4000, -4000, 4000);
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
        List<ExtractedObject> objects = binaryLabelingProcess.getExtractedObjects();
        // Filters the objects
        objects = objects.stream().filter((object) -> object.getSize() > 1000 && object.getSize() < 2500).collect(Collectors.toList());
        objects = objects.stream().filter((object) -> object.getCircularity() > 0.7).collect(Collectors.toList());
        objects = objects.stream().filter((object) -> object.getMatrix().getBounds().center().x > w / 4).collect(Collectors.toList());
        objects = objects.stream().filter((object) -> object.getMatrix().getBounds().center().x < w / 4 + w / 2).collect(Collectors.toList());
        // If the aorta is not in the image, no objects will meet the filters
        if (objects.isEmpty()) {
            return null;
        }
        // Sort the objects by the distance to the center of the image
        Point imageCenter = new Point(w / 2, h / 2);
        objects.sort((o1, o2) -> {
            double d1 = o1.getMatrix().getBounds().center().distance(imageCenter);
            double d2 = o2.getMatrix().getBounds().center().distance(imageCenter);
            return d2 < d1 ? 1 : -1;
        });
        // Fills the structure
        return objects.stream().map(object -> object.getMatrix()).collect(Collectors.toList());
    }

}
