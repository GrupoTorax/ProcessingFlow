package org.paim.orchestration.lungs;

import org.paim.pdi.BinaryLabelingProcess;
import org.paim.pdi.Process;

/**
 * Checks if two lungs are connected
 */
public class CheckConnectedLungs implements Process {

    /** Original image */
    private final BinaryLabelingProcess.ExtractedObject object;
    /** If the lungs are connected */
    private boolean connected;

    /**
     * Crates a new process that checks if two lungs are connected
     * 
     * @param object
     */
    public CheckConnectedLungs(BinaryLabelingProcess.ExtractedObject object) {
        this.object = object;
    }
    
    @Override
    public void process() {
        int sizeLeft = 0;
        int sizeRight = 0;
        boolean [][] labeledMatrix = object.getMatrix();
        int width = labeledMatrix.length;
        int height = labeledMatrix[0].length;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < (width / 2); x++) {
                if (labeledMatrix[x][y]) {
                    sizeLeft++;
                }
            }
            for (int x = (width / 2); x < width; x++) {
                if (labeledMatrix[x][y]) {
                    sizeRight++;
                }
            }
        }
        // Returns true if each side has at least 30% of the object, then the lungs are connected
        connected = (sizeLeft >= (object.getSize() * 0.3)) & (sizeRight >= (object.getSize() * 0.3));
    }

    /**
     * Returns if the two objects are connected
     * 
     * @return boolean
     */
    public boolean isConnected() {
        return connected;
    }
    
}
