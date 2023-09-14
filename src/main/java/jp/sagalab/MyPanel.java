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
import java.util.List;

import static java.lang.Math.ceil;
import static java.lang.Math.max;

public class MyPanel extends JPanel implements MouseListener, MouseMotionListener {

    public void execute(SplineCurve sc, Point begin, Point end) {
        // スプライン曲線を保存
        m_splineCurve = sc;
        // 赤の点。前側の点（nullの場合がある）
        m_begin = begin;
        // 青の点。後ろ側の点（nullの場合がある）
        m_end = end;

//        System.out.println(m_splineCurve);        //cp:(x, y, z, t, f), knots, degree, range[s,e)

        // ↓↓↓ここに何か処理を入れよう↓↓↓↓


        int num = max((int) ceil(m_splineCurve.range().length() / 1e-2), 2);       //1点あっても線にならないから、2より大きいのを取るためにmaxで2より大きくしてる
        Point[] points = m_splineCurve.evaluateAll(num, ParametricEvaluable.EvaluationType.TIME);    //points: スプライン曲線を等時間間隔でnum点で評価した点列
//        System.out.println("points(x, y, z, 時間, f),   " + "長さ: " + points.length);
//        for(int i=0; i<=points.length-1; i++){
//            System.out.println(points[i]);              //pointsの表示, 入力点列(x, y, z, 時間, f)
//        }

        //Lに距離を足す。Lは全長
        double L = 0;
        for (int i=0; i<points.length-1; i++){
            L += distance(points[i].x(), points[i+1].x(), points[i].y(), points[i+1].y());
        }

        // 弧長パラメータを0から始まるようにシフトしておく.
        Range lRange = Range.create(0.0, L);
        // 点列の時系列を正規化する.
        List<Point> normalizedPoints = normalizePoints(lRange, points);
        //shiftedPointsに時刻を入れて表示する
        List<Point> shiftedPoints = shiftPointsTimeZero(points);
//    for(int i=0; i<shiftedPoints.size(); i++){
//      System.out.println(shiftedPoints.get(i));    //表示
//    }

        //disListに距離入れてく（points）
        List<Double> disList = new ArrayList<>();
        double dis = 0.0;
//        System.out.println("disList");        //表示
        for (int i=0; i<points.length; i++){
            if (i == 0){
                disList.add(0.0);
            }
            else {
                dis += distance(points[i-1].x(), points[i].x(), points[i-1].y(), points[i].y());
                disList.add(dis);
            }
//            System.out.println(disList.get(i));     //表示
        }


        //m_beginから入力の1点目の距離Ls, m_endから入力点の最後の点の距離Le
        double Ls = distance(points[0].x(), m_begin.x(), points[0].y(), m_begin.y());
        double Le = distance(points[points.length-1].x(), m_end.x(), points[points.length-1].y(), m_end.y());
//        System.out.println("Ls: " + Ls);         //Lsの表示
//        System.out.println("L+Le: " + (L+Le));       //L+Leの表示

        //disListのi番目のx, y, 距離をnormalizedPointsにsetする
        //normalizedPointsの中身は(x, y, 距離)
        for (int i=0; i<points.length-1; i++){
            normalizedPoints.set(i, Point.createXYT(normalizedPoints.get(i).x(), normalizedPoints.get(i).y(), disList.get(i)));
        }
        //normalizedPointsに点S(s.x, s.y, -Ls), 点E(e.x, e.y, L+Le-0.000001)を加える
        normalizedPoints.add(0, Point.createXYT(m_begin.x(), m_end.y(), -Ls));
        normalizedPoints.add(Point.createXYT(m_begin.x(), m_end.y(), L+Le-0.000001));

        // リストを配列に変換する.
        Point[] points2 = normalizedPoints.toArray(new Point[0]);

//    System.out.println("points2(x, y, z, 距離, f),   " + "長さ: " + points2.length);
//    for(int i=0; i<points.length; i++){
//      System.out.println(points2[i]);   //表示
//    }

        // 次数
        int degree = 3;

        //kotyoListに距離を入れる
        //[-Ls, 0, 0.1の距離, 0.2の距離, ... , L+Le]
        List<Double> kotyoList = new ArrayList<>();
        kotyoList.add(-Ls);
        for(double t=points[0].time(); t<=points[points.length-1].time(); t+=0.01){
            for(int i=0; i<=points.length-1; i++){
                if(points[i].time() > t){
                    double u = t - points[i-1].time();           //比
                    double v = points[i].time() - t;             //比
                    double a = disList.get(i-1);                       //距離
                    double b = disList.get(i);                         //距離
                    double d = (v*a + u*b)/(u + v);                    //内分
                    kotyoList.add(d);
                    break;
                }
            }
        }
        //kotyoList.add(L);
        kotyoList.add(L+Le);


        //knot_2はkotyoListと付加節点
        double[] knot_2;
        knot_2 = new double[kotyoList.size()+4];
        knot_2[0] = kotyoList.get(0);
        knot_2[1] = kotyoList.get(0);
        for(int i=0; i< kotyoList.size(); i++){
            knot_2[i+2] = kotyoList.get(i);
        }
        knot_2[kotyoList.size()+2] = kotyoList.get(kotyoList.size()-1);
        knot_2[kotyoList.size()+3] = kotyoList.get(kotyoList.size()-1);

//        System.out.println("knot_2:");
//        for(int i=0; i<knot_2.length; i++){
//            System.out.println(knot_2[i]);          //knot_2の表示
//        }


        // スプライン補間を行う
        // SplineCurveInterpolator.interpolateの引数は(点列(Point[]型), 次数(int型), 節点間隔(double型))にする.
        jp.sagalab.SplineCurve splineCurve = SplineCurveInterpolator.interpolate(points2, degree, knot_2);


        // スプライン曲線の評価点を求める↓
        double Start = splineCurve.range().start();
        double End = splineCurve.range().end();
        List<Point> evaluateList = new ArrayList<>();


        for (double t = Start; t < End; t += 0.01) {
            evaluateList.add(splineCurve.evaluate(t));
        }


//        // SplineCurveの描画
//        for (int i = 1; i < evaluateList.size(); i++) {
//            drawLine(evaluateList.get(i-1), evaluateList.get(i), Color.RED);              //評価点（赤）
//        }
//
//        Point[] controlList = splineCurve.controlPoints();
//        for (int i = 0; i< controlList.length ;i++){
//            drawPoint(controlList[i].x(),controlList[i].y(),3,Color.blue);         //制御点（青）
//        }
//        for (int i = 1; i <= controlList.length - 1; i++) {
//            drawLine(controlList[i-1],controlList[i] , Color.blue);                       //制御点をつなぐ（青線）
//        }


    }


    //距離計算
    public double distance(double _x1, double _x2, double _y1, double _y2){
        double X = _x2 - _x1;
        double Y = _y2 - _y1;
        double L = Math.sqrt(Math.pow(X, 2) + Math.pow(Y, 2));
        return L;
    }

    /**
     * 点列の時刻パラメータが0始まりになるように全体をシフトします.
     */
    public List<Point> shiftPointsTimeZero(Point[] points) {
        return normalizePoints(Range.create(0, points[points.length-1].time() - points[0].time()), points);
    }

    /**
     * 点列の時刻パラメータの正規化をします.
     * m_points全体の時刻パラメータが_range区間に収まるように正規化します.
     *
     * @param _range 正規化後の時刻パラメータの範囲
     */
    public List<Point> normalizePoints(Range _range, Point[] points) {
        double startTime = m_points.get(0).time();
        double timeLength = m_points.get(m_points.size() - 1).time() - startTime;
        double rangeLength = _range.length();
        List<Point> points3 = new ArrayList<>();
        for (Point point : m_points) {
            points3.add(Point.createXYT(point.x(), point.y()
                    , _range.start() + (point.time() - startTime) * (rangeLength / timeLength)));
        }

        return points3;
    }

//    /**
//     * 点を描画する.
//     *
//     * @param _x      x座標
//     * @param _y      y座標
//     * @param _radius 点の半径
//     * @param _color  点の色
//     */
//    public void drawPoint(double _x, double _y, double _radius, Color _color) {
//        Graphics2D g = (Graphics2D) m_canvas.getGraphics();
//        g.setColor(_color);
//
//        Ellipse2D.Double oval = new Ellipse2D.Double(_x - _radius, _y - _radius, _radius * 2, _radius * 2);
//        g.draw(oval);
//    }
//
//    /**
//     * 線を描画する.
//     *
//     * @param _p1    始点
//     * @param _p2    終点
//     * @param _color 線の色
//     */
//    public void drawLine(Point _p1, Point _p2, Color _color) {
//        Graphics2D g = (Graphics2D)m_canvas.getGraphics();
//        g.setColor(_color);
//
//        Line2D.Double line = new Line2D.Double(_p1.x(), _p1.y(), _p2.x(), _p2.y());
//        g.draw(line);
//    }


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
            int num = max((int) ceil(splineCurve.range().length() / 1e-2), 2);       //1点あっても線にならないから、2より大きいのを取るためにmaxで2より大きくしてる
            Point[] points = splineCurve.evaluateAll(num, ParametricEvaluable.EvaluationType.TIME);    //points: スプライン曲線を等時間間隔でnum点で評価した点列
//            System.out.println("points: ");
//            for(int i=0; i<=points.length-1; i++){
//                System.out.println(points[i]);              //pointsの表示, 入力点列(x, y, z, 時間, f)
//            }

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

    /** キャンバスを表す変数 */
    private final Canvas m_canvas = new Canvas();
}
