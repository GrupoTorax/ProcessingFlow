package org.torax.orchestration.lungs;

import org.torax.orchestration.ExamResult;
import org.torax.commons.Exam;
import org.torax.commons.Image;
import org.torax.commons.ImageHelper;
import org.torax.pdi.BinaryLabelingProcess;
import org.torax.pdi.GaussianBlurProcess;
import org.torax.pdi.HistogramProcess;
import org.torax.pdi.ShadowCastingProcess;
import org.torax.pdi.ThresholdEdgeTrimProcess;
import org.torax.pdi.ThresholdLimitProcess;
import org.torax.pdi.ThresholdProcess;

/**
 *
 * @author Nícolas Pohren
 */
public class SegmentLungs {

    private final Exam exam;
    private final ExamResult exameSegmentado;
    private int[][] mtzTrabalho;
    private int labelMaior1, labelMaior2, tamanho, maior1, maior2;

    public SegmentLungs(Exam exam, ExamResult exameSegmentado) {
        this.exam = exam;
        this.exameSegmentado = exameSegmentado;
    }

    public void segmenta() {
        for (int indiceFatia = 0; indiceFatia < exam.getNumberOfSlices(); indiceFatia++) {
            segmentaFatia(indiceFatia);
        }
    }

    private void segmentaFatia(int indiceFatia) {
        // Deve copiar o conteudo da matriz pois NÃO deve alterar o matriz de coeficientes original
        mtzTrabalho = copyArray(exam.getExamSlice(indiceFatia).getCoefficientMatrix());
        // Converte qualquer valor fora da faixa de valores válidos para um valor conhecido de background
        Image image = ImageHelper.create(mtzTrabalho, new org.torax.commons.Range<>(-4000, 4000));
        ThresholdLimitProcess limit = new ThresholdLimitProcess(image, Integer.MIN_VALUE, 4000, -1000, -1000);
        limit.process();
        image = limit.getOutput();
        // Se a espessura da fatia for menor que 5mm
        if (exam.getExamSlice(indiceFatia).getSliceThickness() <= 5) {
            image = aplicaGauss(image);
        }
        // Cria uma "sombra" do objeto para todos os lados
        ShadowCastingProcess shadowProcess = new ShadowCastingProcess(image, -200, ShadowCastingProcess.Orientation.LEFT, ShadowCastingProcess.Orientation.RIGHT, ShadowCastingProcess.Orientation.TOP, ShadowCastingProcess.Orientation.BOTTOM);
        shadowProcess.process();
        mtzTrabalho = ImageHelper.getData(shadowProcess.getOutput());
        // Histograma
        HistogramProcess histogramProcess = new HistogramProcess(ImageHelper.create(mtzTrabalho, new org.torax.commons.Range<>(-1000, -200)));
        histogramProcess.process();
        int maxOccurrence = histogramProcess.getOutput().getValueWithMaxOccurences(new org.torax.commons.Range<>(-1000, -201));
        int limiar = histogramProcess.getOutput().getValueWithLeastOccurences(new org.torax.commons.Range<>(maxOccurrence, -200));
        // Converte a matriz para tons de cinza
        image = ImageHelper.create(mtzTrabalho, new org.torax.commons.Range<>(255, 0));
        ThresholdProcess process = new ThresholdProcess(image, limiar);
        process.process();
        // Executa o corte das bordas, para remover a mesa do tomógrafo
        ThresholdEdgeTrimProcess edgeProcess = new ThresholdEdgeTrimProcess(image, 0, ThresholdEdgeTrimProcess.Orientation.BOTTOM);
        edgeProcess.process();
        mtzTrabalho = ImageHelper.getData(process.getOutput());
        // Retira a mesa do tomógrafo, se existente
        image = ImageHelper.create(mtzTrabalho, new org.torax.commons.Range<>(0, 1));
        process = new ThresholdProcess(image, 100);
        process.process();
        mtzTrabalho = ImageHelper.getData(process.getOutput());
        image = ImageHelper.create(mtzTrabalho, new org.torax.commons.Range<>(-4000, 4000));
        BinaryLabelingProcess binaryLabelingProcess = new BinaryLabelingProcess(image);
        binaryLabelingProcess.process();
        // Busca os dois maiores objetos da imagem
        buscaDoisMaiores(binaryLabelingProcess);
        //refaz a rotulação, agora com os pulmões separados
        image = ImageHelper.create(mtzTrabalho, new org.torax.commons.Range<>(-4000, 4000));
        // Verifica se os pulmões estão conectados, sendo reconhecidos como somente 1 objeto
        if (verificaConectados(binaryLabelingProcess.getMatrix(labelMaior1), maior1)) {
            System.out.println("Conectados! Fatia: " + (indiceFatia + 1));
            //separa os pulmões (altera mtzTrabalho, separando os pulmões
            SplitConnectedLungs splitConnectedLungs = new SplitConnectedLungs(image, binaryLabelingProcess.getMatrix(labelMaior1));
            splitConnectedLungs.process();
            binaryLabelingProcess = new BinaryLabelingProcess(image);
            binaryLabelingProcess.process();
            //busca os dois maiores objetos da imagem
            buscaDoisMaiores(binaryLabelingProcess);
        }
        // Verifica qual dos dois objetos começa mais a esquerda
        // ATENÇÃO, a matriz retornada abaixo não está com o "offset" de objetos, por isso é necessário acrescentar 1 na comparação, mas não no get da matriz booleana!
        mtzTrabalho = ImageHelper.getData(binaryLabelingProcess.getOutput().getImage());
        for (int x = 0; x < mtzTrabalho.length; x++) {
            for (int y = 0; y < mtzTrabalho[0].length; y++) {
                if (mtzTrabalho[x][y] == (labelMaior1 + 1)) {
                    exameSegmentado.getFatiaExameSegmentado(indiceFatia).setMatrizPulmaoEsq(binaryLabelingProcess.getMatrix(labelMaior1));
                    exameSegmentado.getFatiaExameSegmentado(indiceFatia).setMatrizPulmaoDir(binaryLabelingProcess.getMatrix(labelMaior2));
                    x = mtzTrabalho.length;
                    break;
                }
                if (mtzTrabalho[x][y] == (labelMaior2 + 1)) {
                    exameSegmentado.getFatiaExameSegmentado(indiceFatia).setMatrizPulmaoEsq(binaryLabelingProcess.getMatrix(labelMaior2));
                    exameSegmentado.getFatiaExameSegmentado(indiceFatia).setMatrizPulmaoDir(binaryLabelingProcess.getMatrix(labelMaior1));
                    x = mtzTrabalho.length;
                    break;
                }
            }
        }
    }

    private Image aplicaGauss(Image image) {
        double sigma = 1.76;
        int tam = 5;
        GaussianBlurProcess process = new GaussianBlurProcess(image, sigma, tam);
        process.process();
        return process.getOutput();
    }

    private boolean verificaConectados(boolean[][] matrizBinLabel, int maior) {
        int tamEsquerda = 0;
        int tamDireita = 0;
        for (int y = 0; y < mtzTrabalho[0].length; y++) {
            for (int x = 0; x < (mtzTrabalho.length / 2); x++) {
                if (matrizBinLabel[x][y]) {
                    tamEsquerda++;
                }
            }
            for (int x = (mtzTrabalho.length / 2); x < mtzTrabalho.length; x++) {
                if (matrizBinLabel[x][y]) {
                    tamDireita++;
                }
            }
        }
        // Retorna verdadeiro se cada um dos lados tem pelo menos 30% do objeto, indicando que os pulmões estão conectados
        return (tamEsquerda >= (maior * 0.3)) & (tamDireita >= (maior * 0.3));
    }

    private void buscaDoisMaiores(BinaryLabelingProcess binaryLabelingProcess) {
        maior1 = 0;
        maior2 = 0;
        labelMaior1 = 0;
        labelMaior2 = 0;
        for (int i = 1; i <= binaryLabelingProcess.getOutput().getSize(); i++) {
            tamanho = binaryLabelingProcess.getSize(i);
            if (tamanho > maior1) {
                maior2 = maior1;
                labelMaior2 = labelMaior1;
                maior1 = tamanho;
                labelMaior1 = i;
            } else if (tamanho > maior2) {
                maior2 = tamanho;
                labelMaior2 = i;
            }
        }
    }

    public static int[][] copyArray(final int[][] array) {
        final int[][] copy = new int[array.length][];
        for (int i = 0; i < array.length; i++) {
            copy[i] = new int[array[i].length];
            System.arraycopy(array[i], 0, copy[i], 0, array[i].length);
        }
        return copy;
    }

}
