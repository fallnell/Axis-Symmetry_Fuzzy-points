package jp.sagalab;

/**
 * 範囲を表すクラスです。
 *
 * @author Akira Nishikawa
 */
public class Range {

    /**
     * 範囲を生成します。
     *
     * @param _start 開始値
     * @param _end   終了値
     * @return 範囲
     * @throws IllegalArgumentException 開始値が無限大または非数の場合
     * @throws IllegalArgumentException 終了値が無限大または非数の場合
     * @throws IllegalArgumentException 開始値が終了地より大きい場合
     */
    public static Range create(double _start, double _end) {
        if (Double.isNaN(_start) || Double.isInfinite(_start)) {
            throw new IllegalArgumentException("_start is illegal number(_start: " + _start + ")");
        }
        if (Double.isNaN(_end) || Double.isInfinite(_end)) {
            throw new IllegalArgumentException("_end is illegal number(_end: " + _end + ")");
        }
        if (_start > _end) {
            throw new IllegalArgumentException("_start > _end(_start: " + _start + ", _end: " + _end + ")");
        }
        return new Range(_start, _end);
    }

    /**
     * 0から1の範囲を返します。
     *
     * @return 0から1の範囲
     */
    public static Range zeroToOne() {
        if (c_zeroOne == null) {
            c_zeroOne = new Range(0, 1);
        }
        return c_zeroOne;
    }

    /**
     * 開始値を返します。
     *
     * @return 開始値
     */
    public double start() {
        return m_start;
    }

    /**
     * 終了値を返します。
     *
     * @return 終了値
     */
    public double end() {
        return m_end;
    }

    /**
     * 中央値を返します
     * @return 中央値
     */
    public double middle() {
        return (length() / 2) + start();
    }

    /**
     * 範囲長を返します。
     *
     * @return 範囲長
     */
    public double length() {
        return m_end - m_start;
    }

    /**
     * 指定された値が範囲内にあるかを返します。
     *
     * @param _d チェックする値
     * @return 範囲内にある場合はtrue
     */
    public boolean isInner(double _d) {
        return !Double.isNaN(_d) && (m_start <= _d && _d <= m_end);
    }

    /**
     * 指定された範囲が範囲内にあるかを返します。
     *
     * @param _range チェックする範囲
     * @return 範囲内にある場合はtrue
     */
    public boolean isInner(Range _range) {
        return isInner(_range.m_start) && isInner(_range.m_end);
    }

    /**
     * この Range と指定された Object が等しいかどうかを比較します。
     *
     * @param obj この Range と比較される Object
     * @return 指定された Object が、このオブジェクトと
     * 開始値、終了値がまったく同じ Range である限りtrue
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Range other = (Range) obj;
        if (this.m_start != other.m_start) {
            return false;
        }
        return (this.m_end == other.m_end);
    }

    /**
     * この Range の文字列表現を返します。
     *
     * @return 開始値、終了値を表す String
     */
    @Override
    public String toString() {
        return String.format("s:%.3f e:%.3f", m_start, m_end);
    }

    /**
     * 範囲の複製を取得します.
     *
     * @return 複製した範囲
     */
    public Range copy() {
        return Range.create(m_start, m_end);
    }


    /**
     * コンストラクタ
     *
     * @param _start 開始値
     * @param _end   終了値
     */
    public Range(double _start, double _end) {
        m_start = _start;
        m_end = _end;
    }

    /** 開始値 */
    private final double m_start;
    /** 終了値 */
    private final double m_end;
    /** [ 0 - 1 ] */
    private static Range c_zeroOne;
}