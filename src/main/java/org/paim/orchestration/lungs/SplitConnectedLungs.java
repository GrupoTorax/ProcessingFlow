package org.paim.orchestration.lungs;

import org.paim.commons.Image;
import org.paim.pdi.Process;

/**
 * Process for splitting two connected lungs
 */
public class SplitConnectedLungs implements Process {

    /** Original image */
    private final Image image;
    /** Labeled matrix */
    private final boolean[][] labeledMatrix;

    /**
     * Crates a new process for splitting connected lungs
     * 
     * @param image
     * @param labeledMatrix 
     */
    public SplitConnectedLungs(Image image, boolean[][] labeledMatrix) {
        this.labeledMatrix = labeledMatrix;
        this.image = image;
    }
    
    @Override
    public void process() {
        int tamProcDir = 0;
        int tamProcEsq = 0;
        int menorColuna = 0;
        int menorTamanho = image.getWidth();
        int metadeMatriz = image.getWidth() / 2;
        // Vai testar 40 colunas a partir do meio da imagem para a direita e 40 para a esquerda
        for (int x = 0; x < 40; x++) {
            // Varre até a metade das linhas
            for (int y = 0; y < metadeMatriz; y++) {
                // Esquerda
                if (labeledMatrix[metadeMatriz - x][y]) {
                    tamProcEsq++;
                }
                // Direita
                if (labeledMatrix[metadeMatriz + x][y]) {
                    tamProcDir++;
                }
            }
            // Se encontrou coluna menor do lado direito
            if (tamProcDir != 0 && tamProcDir < menorTamanho) {
                menorTamanho = tamProcDir;
                menorColuna = metadeMatriz + x;
            }
            // Se encontoru coluna menor do lado esquerdo
            if (tamProcEsq != 0 && tamProcEsq < menorTamanho) {
                menorTamanho = tamProcEsq;
                menorColuna = metadeMatriz - x;
            }
            // Inicializa contadores
            tamProcDir = 0;
            tamProcEsq = 0;
        }
        // Se encontrou a menor colua
        if (menorColuna != 0) {
            // Varre todas as linhas, inicializando a coluna como fundo
            for (int y = 0; y < metadeMatriz; y++) {
                image.set(0, menorColuna, y, 0);
            }
        }
    }

}
