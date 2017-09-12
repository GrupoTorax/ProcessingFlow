package org.paim.orchestration.lungs;

import org.paim.commons.Exam;
import org.paim.commons.Image;
import org.paim.commons.ImageHelper;
import org.paim.orchestration.ExamResult;
import org.paim.orchestration.StructureType;
import org.paim.pdi.BinaryLabelingProcess;
import org.paim.pdi.BinaryLabelingProcess.ObjectList;
import org.paim.pdi.GaussianBlurProcess;
import org.paim.pdi.HistogramProcess;
import org.paim.pdi.ShadowCastingProcess;
import org.paim.pdi.ThresholdEdgeTrimProcess;
import org.paim.pdi.ThresholdLimitProcess;
import org.paim.pdi.ThresholdProcess;

/**
 * Identifies and segments the lungs in the exam
 */
public class SegmentLungs {

    /** Exam */
    private final Exam exam;
    /** Exam result */
    private final ExamResult result;

    /**
     * Creates the flow for the lung segmentation
     * 
     * @param exam
     * @param exameSegmentado 
     */
    public SegmentLungs(Exam exam, ExamResult exameSegmentado) {
        this.exam = exam;
        this.result = exameSegmentado;
    }

    /**
     * Segments the lungs
     */
    public void segmenta() {
        for (int indiceFatia = 0; indiceFatia < exam.getNumberOfSlices(); indiceFatia++) {
            segmentSlice(indiceFatia);
        }
    }

    /**
     * Segments a specified slice
     * 
     * @param sliceIndex 
     */
    private void segmentSlice(int sliceIndex) {
        // Converte qualquer valor fora da faixa de valores válidos para um valor conhecido de background
        Image image = ImageHelper.create(exam.getExamSlice(sliceIndex).getCoefficientMatrix(), new org.paim.commons.Range<>(-4000, 4000));
        ThresholdLimitProcess limit = new ThresholdLimitProcess(image, Integer.MIN_VALUE, 4000, -1000, -1000);
        limit.process();
        image = limit.getOutput();
        // Se a espessura da fatia for menor que 5mm
        if (exam.getExamSlice(sliceIndex).getSliceThickness() <= 5) {
            image = applyGauss(image);
        }
        // Cria uma "sombra" do objeto para todos os lados
        ShadowCastingProcess shadowProcess = new ShadowCastingProcess(image, -200, ShadowCastingProcess.Orientation.LEFT, ShadowCastingProcess.Orientation.RIGHT, ShadowCastingProcess.Orientation.TOP, ShadowCastingProcess.Orientation.BOTTOM);
        shadowProcess.process();
        // Histograma
        HistogramProcess histogramProcess = new HistogramProcess(ImageHelper.create(shadowProcess.getOutput(), new org.paim.commons.Range<>(-1000, -200)));
        histogramProcess.process();
        int maxOccurrence = histogramProcess.getOutput().getValueWithMaxOccurences(new org.paim.commons.Range<>(-1000, -201));
        int limiar = histogramProcess.getOutput().getValueWithLeastOccurences(new org.paim.commons.Range<>(maxOccurrence, -200));
        // Converte a matriz para tons de cinza
        image = ImageHelper.create(shadowProcess.getOutput(), new org.paim.commons.Range<>(255, 0));
        ThresholdProcess process = new ThresholdProcess(image, limiar);
        process.process();
        // Executa o corte das bordas, para remover a mesa do tomógrafo
        ThresholdEdgeTrimProcess edgeProcess = new ThresholdEdgeTrimProcess(image, 0, ThresholdEdgeTrimProcess.Orientation.BOTTOM);
        edgeProcess.process();
        // Retira a mesa do tomógrafo, se existente
        image = ImageHelper.create(process.getOutput(), new org.paim.commons.Range<>(0, 1));
        process = new ThresholdProcess(image, 100);
        process.process();
        image = ImageHelper.create(process.getOutput(), new org.paim.commons.Range<>(-4000, 4000));
        BinaryLabelingProcess binaryLabelingProcess = new BinaryLabelingProcess(image);
        binaryLabelingProcess.process();
        BinaryLabelingProcess.ObjectList objects = binaryLabelingProcess.getExtractedObjects();
        // Busca os dois maiores objetos da imagem
        ObjectList twoLargest = objects.sortBySizeLargestFirst().subList(2);
        BinaryLabelingProcess.ExtractedObject maiorO1 = twoLargest.get(0);
        // Verifica se os pulmões estão conectados, sendo reconhecidos como somente 1 objeto
        CheckConnectedLungs checkConnected = new CheckConnectedLungs(maiorO1);
        checkConnected.process();
        if (checkConnected.isConnected()) {
            //separa os pulmões (altera mtzTrabalho, separando os pulmões
            SplitConnectedLungs splitConnectedLungs = new SplitConnectedLungs(image, maiorO1.getMatrix());
            splitConnectedLungs.process();
            binaryLabelingProcess = new BinaryLabelingProcess(image);
            binaryLabelingProcess.process();
            // Busca os dois maiores objetos da imagem
            objects = binaryLabelingProcess.getExtractedObjects();
            twoLargest = objects.sortBySizeLargestFirst().subList(2);
        }
        // Ordena pela posição horizontal, e separa o pulmão esquerdo do direito
        twoLargest.sort((o1, o2) -> o1.getMatrix().getBounds().x - o2.getMatrix().getBounds().x);
        result.getStructure(StructureType.LEFT_LUNG).getSlice(sliceIndex).setBinaryLabel(twoLargest.get(0).getMatrix());
        result.getStructure(StructureType.RIGHT_LUNG).getSlice(sliceIndex).setBinaryLabel(twoLargest.get(1).getMatrix());
    }

    /**
     * Applies the Gauss process
     * 
     * @param image
     * @return Image
     */
    private Image applyGauss(Image image) {
        GaussianBlurProcess process = new GaussianBlurProcess(image, /* Sigma = */ 1.76, /* Mask size = */ 5);
        process.process();
        return process.getOutput();
    }

}
