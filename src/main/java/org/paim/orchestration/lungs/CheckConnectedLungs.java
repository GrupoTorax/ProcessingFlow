package org.paim.orchestration.lungs;

import org.paim.commons.BinaryImage;
import org.paim.pdi.ExtractedObject;
import org.paim.pdi.Process;

/**
 * Checks if two lungs are connected
 */
public class CheckConnectedLungs implements Process {

    /** Original image */
    private final ExtractedObject object;
    /** If the lungs are connected */
    private boolean connected;

    /**
     * Crates a new process that checks if two lungs are connected
     * 
     * @param object
     */
    public CheckConnectedLungs(ExtractedObject object) {
        this.object = object;
    }
    
    @Override
    public void process() {
        int sizeLeft = 0;
        int sizeRight = 0;
        BinaryImage labeledMatrix = object.getMatrix();
        for (int y = 0; y < labeledMatrix.getWidth(); y++) {
            for (int x = 0; x < (labeledMatrix.getHeight() / 2); x++) {
                if (labeledMatrix.get(x, y)) {
                    sizeLeft++;
                }
            }
            for (int x = (labeledMatrix.getWidth() / 2); x < labeledMatrix.getWidth(); x++) {
                if (labeledMatrix.get(x, y)) {
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
