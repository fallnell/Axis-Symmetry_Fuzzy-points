package jp.sagalab;

import java.util.Arrays;

/**
 * m×n行列を表すクラスです。
 *
 * @author Akira Nishikawa
 */
public class Matrix {

    /**
     * m×n行列を生成します。
     *
     * @param _elements [m][n]の行列要素群
     * @return m×nの行列
     * @throws NullPointerException     引数の型にnullが指定された場合にスローします。
     * @throws IllegalArgumentException 行数が０の場合、列数が０の場合、
     *                                  列数にばらつきがある場合にスローします。
     */
    public static Matrix create(double[][] _elements) {
        if (_elements == null) {
            throw new NullPointerException("_elements is null.");
        }
        // 行数チェック
        int rowSize = _elements.length;
        if (rowSize == 0) {
            throw new IllegalArgumentException("row size is 0.");
        }
        // 列数チェック
        int columnSize = 0;
        if (_elements[0] != null) {
            columnSize = _elements[0].length;
            if (columnSize == 0) {
                throw new IllegalArgumentException("column size is 0.");
            }
        }
        // 列数のばらつきチェック
        for (double[] column : _elements) {
            if (column == null) {
                throw new NullPointerException("_elements is included null.");
            }
            if (columnSize != column.length) {
                throw new IllegalArgumentException();
            }
            for (double d : column) {
                if (Double.isInfinite(d)) {
                    throw new IllegalArgumentException(
                            String.format("_elements:%s has POSITIVE_INFINITY", Arrays.deepToString(_elements)));
                }
                if (Double.isNaN(d)) {
                    throw new IllegalArgumentException(
                            String.format("_elements:%s has NaN", Arrays.deepToString(_elements)));
                }
            }
        }

        // 要素のコピーを作る
        double[] elements = new double[rowSize * columnSize];
        for (int i = 0; i < rowSize; ++i) {
            int offset = i * columnSize;
            System.arraycopy(_elements[i], 0, elements, offset, columnSize);
        }

        return new Matrix(elements, columnSize);
    }

    /**
     * m×n行列を生成します。
     *
     * @param _elements   行列要素群
     * @param _columnSize 列数
     * @return 指定した列数の行列
     * @throws NullPointerException     引数の型にnullが指定された場合にスローします。
     * @throws IllegalArgumentException 行列要素群の配列サイズが０の場合、
     *                                  列数が０以下の場合、m×n行列とならない場合にスローします。
     */
    private static Matrix create(double[] _elements, int _columnSize) {
        if (_elements == null) {
            throw new NullPointerException("_elements is null.");
        }
        if (_elements.length == 0) {
            throw new IllegalArgumentException("_elements is empty.");
        }
        if (_columnSize <= 0) {
            throw new IllegalArgumentException("_columnSize is negative or zero.");
        }
        // 行数チェック
        if (_elements.length % _columnSize != 0) {
            throw new IllegalArgumentException("illegal length of _elements.");
        }

        return new Matrix(_elements, _columnSize);
    }

    /**
     * 単位行列を生成します。
     *
     * @param _size 行列のサイズ
     * @return 単位行列
     */
    public static Matrix identity(int _size) {
        double[] elements = new double[_size * _size];

        for (int i = 0; i < _size; ++i) {
            elements[i * _size + i] = 1.0;
        }

        return Matrix.create(elements, _size);
    }

    /**
     * 行列の要素を返します。
     *
     * @param _i 行番号
     * @param _j 列番号
     * @return 行列の要素
     * @throws ArrayIndexOutOfBoundsException 指定された列番号または行番号が行列のサイズ以上の場合にスローします。
     */
    public double get(int _i, int _j) {
        int rowSize = rowSize();
        int columnSize = columnSize();
        if (_i >= rowSize || _j >= columnSize) {
            throw new ArrayIndexOutOfBoundsException("Row size or column size is wrong.");
        }

        return m_elements[_i * columnSize + _j];
    }

    /**
     * 行列要素群を返します。
     *
     * @return 行列の要素群
     */
    public double[][] elements() {
        int rowSize = rowSize();
        int columnSize = columnSize();
        double[][] elements = new double[rowSize][columnSize];
        for (int i = 0; i < rowSize; ++i) {
            int offset = i * columnSize;
            System.arraycopy(m_elements, offset, elements[i], 0, columnSize);
        }
        return elements;
    }

    /**
     * 行数を返します。
     *
     * @return 行数
     */
    public int rowSize() {
        int columnSize = columnSize();
        return m_elements.length / columnSize;
    }

    /**
     * 列数を返します。
     *
     * @return 列数
     */
    public int columnSize() {
        return m_columnSize;
    }

    /**
     * 拡大縮小を行います。
     *
     * @param _ratio 拡大縮小率
     * @return 拡大縮小後の行列
     * @throws IllegalArgumentException 指定された拡大縮小率がNaNまたはInfiniteの場合にスローします。
     */
    public Matrix magnify(double _ratio) {
        if (Double.isNaN(_ratio) || Double.isInfinite(_ratio)) {
            throw new IllegalArgumentException("Ratio of Matrix is NaN or Infinite.");
        }
        int columnSize = columnSize();
        double[] elements = m_elements.clone();
        for (int i = 0; i < elements.length; ++i) {
            elements[i] *= _ratio;
        }
        return Matrix.create(elements, columnSize);
    }

    /**
     * 転置行列を返します。
     *
     * @return 転置行列
     */
    public Matrix transpose() {
        int rowSize = rowSize();
        int columnSize = columnSize();
        double[] elements = new double[m_elements.length];
        for (int i = 0; i < rowSize; ++i) {
            int offset = i * columnSize;
            for (int j = 0; j < columnSize; ++j) {
                elements[j * rowSize + i] = m_elements[offset + j];
            }
        }
        return Matrix.create(elements, rowSize);
    }

    /**
     * 指定された行列との和を求めます。
     *
     * @param _right 右辺行列
     * @return 和演算の結果
     * @throws IllegalArgumentException 指定された行列の列数、行数がこの行列の列数、行数に
     *                                  等しくない場合にスローします。
     */
    public Matrix plus(Matrix _right) {
        int rowSize = rowSize();
        int columnSize = columnSize();
        int oRowSize = _right.rowSize();
        int oColumnSize = _right.columnSize();

        if (rowSize != oRowSize || columnSize != oColumnSize) {
            throw new IllegalArgumentException(
                    "Other matrix's row size or column size not equals this matrix's.");
        }

        double[] elements = m_elements.clone();
        for (int i = 0; i < elements.length; ++i) {
            elements[i] += _right.m_elements[i];
        }

        return Matrix.create(elements, columnSize);
    }

    /**
     * 指定された行列との差を求めます。(この行列-指定された行列)
     *
     * @param _right 右辺行列
     * @return 差演算の結果
     * @throws IllegalArgumentException 指定された行列の列数、行数がこの行列の列数、行数に
     *                                  等しくない場合にスローします。
     */
    public Matrix minus(Matrix _right) {
        int rowSize = rowSize();
        int columnSize = columnSize();
        int oRowSize = _right.rowSize();
        int oColumnSize = _right.columnSize();

        if (rowSize != oRowSize || columnSize != oColumnSize) {
            throw new IllegalArgumentException(
                    "Other matrix's row size or column size not equals this matrix's.");
        }

        double[] elements = m_elements.clone();
        for (int i = 0; i < elements.length; ++i) {
            elements[i] -= _right.m_elements[i];
        }

        return Matrix.create(elements, columnSize);
    }

    /**
     * 指定された行列との積を求めます。(この行列×指定された行列)
     *
     * @param _right 右辺行列
     * @return 積演算の結果
     * @throws IllegalArgumentException 指定された行列の列数、行数がこの行列の列数、行数に
     *                                  等しくない場合にスローします。
     */
    public Matrix product(Matrix _right) {
        int columnSize = columnSize();
        int oRowSize = _right.rowSize();

        if (columnSize != oRowSize) {
            throw new IllegalArgumentException(
                    "Other matrix's row size or column size not equals this matrix's.");
        }

        int rowSize = rowSize();
        int oColumnSize = _right.columnSize();

        double[] elements = new double[rowSize * oColumnSize];
        for (int i = 0; i < rowSize; ++i) {
            int offset = i * columnSize;
            int resultOffset = i * oColumnSize;
            for (int j = 0; j < columnSize; ++j) {
                if (m_elements[offset + j] != 0.0) {
                    int oOffset = j * oColumnSize;
                    for (int k = 0; k < oColumnSize; ++k) {
                        if (_right.m_elements[oOffset + k] != 0.0) {
                            elements[resultOffset + k] += m_elements[offset + j] * _right.m_elements[oOffset + k];
                        }
                    }
                }
            }
        }

        return Matrix.create(elements, oColumnSize);
    }

    /**
     * 指定された行列との解を求めます。
     * <p>
     * Ax = b （ただし　A(this)、b(_right)）の x を求めます。
     * </p>
     * <p>
     * 解が求められない場合はnullを返します。
     * </p>
     *
     * @param _right 右辺行列
     * @return 解行列
     * @throws IllegalArgumentException 指定された行列の行数とこの行列の行数が
     *                                  等しくない場合にスローします。
     */
    public Matrix solve(Matrix _right) {
        //-- LU分解 --
        final int rowSize = rowSize();

        if (rowSize != _right.rowSize()) {
            throw new IllegalArgumentException("not equals other matrix's row size.");
        }

        // 行交換情報
        int[] processHistory = new int[rowSize];
        for (int i = 0; i < rowSize; ++i) {
            processHistory[i] = i;
        }

        double[][] elem = elements();

        // ピボット操作によるLU分解
        for (int i = 0; i < rowSize; ++i) {
            pivot(elem, processHistory, i);

            double denominator = elem[i][i];
            for (int j = i + 1; j < rowSize; ++j) {
                if (elem[j][i] != 0.0) {
                    elem[j][i] /= denominator;
                    if (Double.isNaN(elem[j][i]) || Double.isInfinite(elem[j][i])) {
                        System.err.println("Warning: fault solving in Matrix.");
                        return null;
                    }
                    for (int k = i + 1; k < rowSize; ++k) {
                        elem[j][k] -= elem[j][i] * elem[i][k];
                    }
                }
            }
        }

        //-- 前進消去・後退代入 --
        int rRowSize = _right.rowSize();
        int rColumnSize = _right.columnSize();

        double[] result = new double[rRowSize * rColumnSize];

        // 前進消去
        for (int i = 0; i < rowSize; ++i) {
            int rOffset = processHistory[i] * rColumnSize;
            int resultOffset = i * rColumnSize;
            for (int j = 0; j < rColumnSize; ++j) {
                double d = _right.m_elements[rOffset + j];
                for (int k = 0; k < i; ++k) {
                    d -= elem[i][k] * result[k * rColumnSize + j];
                }
                result[resultOffset + j] = d;
            }
        }

        // 後退代入
        for (int i = rowSize - 1; i >= 0; --i) {
            double denominator = elem[i][i];
            int offset = i * rColumnSize;
            for (int j = 0; j < rColumnSize; ++j) {
                int index = offset + j;
                double dd = result[index];
                for (int k = i + 1; k < rowSize; ++k) {
                    dd -= elem[i][k] * result[k * rColumnSize + j];
                }
                result[index] = dd / denominator;
                if (Double.isNaN(result[index]) || Double.isInfinite(result[index])) {
                    System.err.println("Warning: fault solving in Matrix.");
                    return null;
                }
            }
        }

        return Matrix.create(result, rColumnSize);
    }

    /**
     * ピボット操作を行います。
     *
     * @param _elements       要素群
     * @param _processHistory 行交換情報
     * @param _n              対象となる列番号
     */
    static void pivot(double[][] _elements, int[] _processHistory, int _n) {
        // 指定された列
        double maxVal = 0;
        // 交換行番号
        int swapRowNum = _n;
        for (int i = _n; i < _elements.length; ++i) {
            double tmp = Math.abs(_elements[i][_n]);
            if (tmp > maxVal) {
                maxVal = tmp;
                swapRowNum = i;
            }
        }

        if (swapRowNum > _n) {
            double[] tmpRow = _elements[_n];
            _elements[_n] = _elements[swapRowNum];
            _elements[swapRowNum] = tmpRow;
            int tmpNum = _processHistory[_n];
            _processHistory[_n] = _processHistory[swapRowNum];
            _processHistory[swapRowNum] = tmpNum;
        }
    }

    /**
     * 2つの Matrix を垂直方向に結合した Matrix を返します。
     *
     * @param _top 上の Matrix
     * @param _bottom 下の Matrix
     * @return 結合後の Matrix
     * @throws IllegalArgumentException 2つの Matrix の列の数か
     *                                  等しくない場合にスローします。
     */
    public static Matrix concatVertical(Matrix _top, Matrix _bottom) {
        if (_top.columnSize() != _bottom.columnSize()) {
            throw new IllegalArgumentException("both column size must be same.");
        }

        double[][] elements = new double[_top.rowSize() + _bottom.rowSize()][_top.columnSize()];

        for (int i = 0; i < _top.rowSize(); ++i) {
            for (int j = 0; j < _top.columnSize(); ++j) {
                elements[i][j] = _top.get(i, j);
            }
        }

        for (int i = _top.rowSize(); i < elements.length; ++i) {
            for (int j = 0; j < _bottom.columnSize(); ++j) {
                elements[i][j] = _bottom.get(i - _top.rowSize(), j);
            }
        }

        return create(elements);
    }

    /**
     * 2つの Matrix を水平方向に結合した Matrix を返します。
     *
     * @param _left 左の Matrix
     * @param _right 右の Matrix
     * @return 結合後の Matrix
     * @throws IllegalArgumentException 2つの Matrix の行の数か
     *                                  等しくない場合にスローします。
     */
    public static Matrix concatHorizontal(Matrix _left, Matrix _right) {
        if (_left.rowSize() != _right.rowSize()) {
            throw new IllegalArgumentException("both row size must be same.");
        }

        double[][] elements = new double[_left.rowSize()][_left.columnSize() + _right.columnSize()];

        for (int i = 0; i < _left.rowSize(); ++i) {
            for (int j = 0; j < _left.columnSize(); ++j) {
                elements[i][j] = _left.get(i, j);
            }
        }

        for (int i = 0; i < _right.rowSize(); ++i) {
            for (int j = _left.columnSize(); j < elements[0].length; ++j) {
                elements[i][j] = _right.get(i, j - _left.columnSize());
            }
        }

        return create(elements);
    }

    /**
     * この Matrix と指定された Object が等しいかどうかを比較します。
     *
     * @param obj この Matrix と比較される Object
     * @return 指定された Object が、このオブジェクトと要素、列数がまったく同じ Matrix である限りtrue
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Matrix other = (Matrix) obj;
        if (!Arrays.equals(this.m_elements, other.m_elements)) {
            return false;
        }

        return m_columnSize == other.m_columnSize;
    }

    /**
     * この Matrix のハッシュコードを返します。
     *
     * @return この Matrix のハッシュコード
     */
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + Arrays.hashCode(this.m_elements);
        hash = 59 * hash + this.m_columnSize;
        return hash;
    }

    /**
     * この Matrix の文字列表現を返します。
     *
     * @return 列数と行数、各要素を表す String
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("size:[").append(rowSize()).append(",").append(columnSize()).append("]\n");
        builder.append("elements:\n");
        int rowSize = rowSize();
        int columnSize = columnSize();
        for (int i = 0; i < rowSize; ++i) {
            builder.append("| ");
            int offset = i * columnSize;
            for (int j = 0; j < columnSize; ++j) {
                builder.append(String.format("%.12f ", m_elements[offset + j]));
            }
            builder.append(" |\n");
        }

        return builder.toString();
    }


    /**
     * コンストラクタ
     *
     * @param _elements   行列要素群
     * @param _columnSize 列数
     */
    public Matrix(double[] _elements, int _columnSize) {
        m_elements = _elements;
        m_columnSize = _columnSize;
    }

    /** 行列の要素 */
    private final double[] m_elements;
    /** 行列の列数 */
    private final int m_columnSize;
}

