package jp.sagalab;

import jp.sagalab.jflib.FSCITask;
import jp.sagalab.jflib.parameter.FSCIParameter;
import jp.sagalab.jftk.Point;
import jp.sagalab.jftk.curve.ParametricEvaluable;
import jp.sagalab.jftk.curve.SplineCurve;
import jp.sagalab.jftk.fragmentation.Fragment;
import jp.sagalab.jftk.fragmentation.IdentificationFragment;
import jp.sagalab.jftk.fragmentation.PartitionFragment;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;

import static java.lang.Math.ceil;
import static java.lang.Math.max;

public class MyPanel extends JPanel implements MouseListener, MouseMotionListener {

    public void execute(SplineCurve sc, Point begin, Point end) {
        // スプライン曲線を保存
        m_splineCurve = sc;
        // 前側の点（nullの場合がある）
        m_begin = begin;
        // 後ろ側の点（nullの場合がある）
        m_end = end;

        // ↓↓↓ここに何か処理を入れよう↓↓↓↓


    }

    @Override
    public void paint(Graphics _g) {
        super.paint(_g);

        // 入力点列を表示
        for (Point p : m_points) {
            _g.drawOval((int) p.x() - 3, (int) p.y() - 3, 6, 6);
        }

        // スプライン曲線を表示（緑）
        _g.setColor(Color.GREEN);
        if (m_splineCurve != null) {
            drawSplineCurve(_g, m_splineCurve);
        }

        // 前の点を表示（赤)
        _g.setColor(Color.RED);
        if (m_begin != null) {
            _g.fillOval((int) m_begin.x() - 4, (int) m_begin.y() - 4, 8, 8);
            _g.drawOval((int) (m_begin.x() - m_begin.fuzziness()),
                    (int) (m_begin.y() - m_begin.fuzziness()),
                    (int) (2 * m_begin.fuzziness()), (int) (2 * m_begin.fuzziness()));
        }

        // 後ろの点を表示（青）
        _g.setColor(Color.BLUE);
        if (m_end != null) {
            _g.fillOval((int) m_end.x() - 4, (int) m_end.y() - 4, 8, 8);
            _g.drawOval((int) (m_end.x() - m_end.fuzziness()),
                    (int) (m_end.y() - m_end.fuzziness()),
                    (int) (2 * m_end.fuzziness()),
                    (int) (2 * m_end.fuzziness()));
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        m_points.clear();
        m_points.add(Point.createXYT(e.getX(), e.getY(), System.currentTimeMillis() * 1e-3));
        repaint();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        m_points.add(Point.createXYT(e.getX(), e.getY(), System.currentTimeMillis() * 1e-3));
        repaint();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        m_points.add(Point.createXYT(e.getX(), e.getY(), System.currentTimeMillis() * 1e-3));
        createFSC();
        repaint();
    }

    public void drawSplineCurve(Graphics _g, SplineCurve splineCurve) {
        if (splineCurve != null) {
            // 1秒間に100点程度表示する
            int num = max((int) ceil(splineCurve.range().length() / 1e-2), 2);
            Point[] points = splineCurve.evaluateAll(num, ParametricEvaluable.EvaluationType.TIME);

            // ファジネスを表示
            for (Point p : points) {
                _g.drawOval((int) (p.x() - p.fuzziness()), (int) (p.y() - p.fuzziness()), (int) (2 * p.fuzziness()), (int) (2 * p.fuzziness()));
            }

            // 稜線を表示
            for (int i = 1; i < points.length; ++i) {
                _g.drawLine(
                        (int) points[i - 1].x(), (int) points[i - 1].y(),
                        (int) points[i].x(), (int) points[i].y()
                );
            }
        }
    }

    public void createFSC() {
        // FSC補間
        SplineCurve sc = m_fsci.createFsc(m_points.toArray(new Point[0]));
        m_origin = sc;
        if (sc != null) {
            // フラグメンテーションを行う
            Fragment[] fragments = m_fsci.fragmentation(sc);

            int index = -1;
            for (int i = 0; i < fragments.length; ++i) {
                // 同定単位を探す
                if (fragments[i] instanceof IdentificationFragment) {
                    index = i;
                    break;
                }
            }
            // 前側の点を探す
            Point begin = null;
            if (index > 0 && fragments[index - 1] instanceof PartitionFragment) {
                begin = ((PartitionFragment) fragments[index - 1]).body();
            }
            // 後ろ側の点を探す
            Point end = null;
            if (index < fragments.length - 1 && fragments[index + 1] instanceof PartitionFragment) {
                end = ((PartitionFragment) fragments[index + 1]).body();
            }
            execute(fragments[index].curve(), begin, end);
        }
    }


    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void mouseMoved(MouseEvent e) {

    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    /**
     * 点列の保存
     */
    public void save() {
        Utility.savePoints(m_points);
    }

    /**
     * 点列の読み出し
     * @param fineName ファイル名
     */
    public void load(String fineName) {
        m_points.clear();
        m_points.addAll(Utility.loadPoints(fineName));
        createFSC();
        repaint();
    }

    MyPanel() {
        super(null);
        setPreferredSize(new Dimension(1200, 900));
        addMouseListener(this);
        addMouseMotionListener(this);

        m_fsci = new FSCITask.Builder(new FSCIParameter.Builder().build())
                .useControlPointFSCCreator()
                .useSplineCurveBlender()
                .useReConnector()
                .useEvaluationPointFragmentation()
                .useSingleModelRecognizer()
                .useInfiniteResolutionFuzzyGridSnapping()
                .useCoaxialityObjectSnapper()
                .withoutFreeCurveReshaper()
                .build();
    }

    /** 入力点列 */
    private final ArrayList<Point> m_points = new ArrayList<>();
    /** FSCI */
    private final FSCITask m_fsci;
    /** オリジナルのファジィスプライン曲線 */
    private SplineCurve m_origin = null;
    /** フラグメンテーション後のファジィスプライン曲線 */
    private SplineCurve m_splineCurve = null;
    /** 前の点 */
    private Point m_begin = null;
    /** 後ろの点 */
    private Point m_end = null;
}
