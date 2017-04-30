package org.torax.orchestration;

/**
 * Slice of processed exam
 */
public class ExamResultSlice {

    private boolean[][] matrizPulmaoEsq;
    private int tamanhoPulmaoEsq = 0;
    private boolean[][] matrizPulmaoDir;
    private int tamanhoPulmaoDir = 0;

    public void setMatrizPulmaoEsq(boolean[][] matrizPulmaoEsq) {
        this.matrizPulmaoEsq = matrizPulmaoEsq;
        tamanhoPulmaoEsq = 0;
        for (int i = 0; i < matrizPulmaoEsq.length; i++) {
            for (int j = 0; j < matrizPulmaoEsq[i].length; j++) {
                if (matrizPulmaoEsq[i][j]) {
                    tamanhoPulmaoEsq++;
                }
            }
        }
    }

    public boolean[][] getPulmaoEsq() {
        return matrizPulmaoEsq;
    }

    public int getTamanhoPulmaoEsq() {
        return tamanhoPulmaoEsq;
    }

    public void setMatrizPulmaoDir(boolean[][] matrizPulmaoDir) {
        this.matrizPulmaoDir = matrizPulmaoDir;
        tamanhoPulmaoDir = 0;
        for (int i = 0; i < matrizPulmaoDir.length; i++) {
            for (int j = 0; j < matrizPulmaoDir[i].length; j++) {
                if (matrizPulmaoDir[i][j]) {
                    tamanhoPulmaoDir++;
                }
            }
        }
    }

    public boolean[][] getPulmaoDir() {
        return matrizPulmaoDir;
    }

    public int getTamanhoPulmaoDir() {
        return tamanhoPulmaoDir;
    }

}
