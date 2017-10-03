package org.paim.orchestration;

import java.util.List;
import org.paim.commons.BinaryImage;
import org.paim.commons.ExamSlice;
import org.paim.pdi.BinaryLabelingProcess;

/**
 * Segments a structure
 * 
 * @param <T>
 */
public interface StructureSegmenter<T extends StructureType> {
    
    /**
     * Segments a specified slice
     * 
     * @param sliceIndex 
     * @param slice 
     * @param resultSlice
     * @return List<BinaryImage>
     */
    public List<BinaryImage> segmentSlice(int sliceIndex, ExamSlice slice, ExamResultSlice resultSlice);
    
}
