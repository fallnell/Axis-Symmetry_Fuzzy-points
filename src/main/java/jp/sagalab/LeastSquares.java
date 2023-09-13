package jp.sagalab;

/**
 * 最小二乗法を表すクラスです。
 *
 * @author Taiga Takashima
 */
public final class LeastSquares {
    /**
     * Ax = b を x について解きます。
     *
     * @param _A
     * @param _b
     * @return x
     */
    public static Matrix solve(Matrix _A, Matrix _b) {
        Matrix A_T = _A.transpose();
        Matrix A_TA = A_T.product(_A);

        return A_TA.solve(A_T.product(_b));
    }
}