package jp.sagalab;

import com.panayotis.gnuplot.JavaPlot;
import com.panayotis.gnuplot.plot.AbstractPlot;
import com.panayotis.gnuplot.plot.Plot;
import jp.sagalab.graph.PointsGraph;
import jp.sagalab.jflib.FSCITask;
import jp.sagalab.jflib.parameter.FSCIParameter;
import jp.sagalab.jftk.Point;
import jp.sagalab.jftk.curve.ParametricEvaluable;
import jp.sagalab.jftk.curve.Range;
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

        System.out.println(m_splineCurve);        //cp:(x, y, z, t, f), knots, degree, range[s,e)


        // ↓↓↓ここに何か処理を入れよう↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓

        List<Double> sc_knots = new ArrayList<>();
        for(int i=0; i<m_splineCurve.knots().length; i++){
            sc_knots.add(m_splineCurve.knots()[i]);
        }
//        //sc_knotsの表示
//        System.out.println(sc_knots);
//        System.out.println("sc_knotsの大きさ" + sc_knots.size());

        //1点あっても線にならないから、2より大きいのを取るためにmaxで2より大きくしてる
        int num = max((int) ceil(m_splineCurve.range().length() / 1e-2), 2);
        //points: スプライン曲線を等時間間隔でnum点で評価した点列
        Point[] points = m_splineCurve.evaluateAll(num, ParametricEvaluable.EvaluationType.TIME);
//        //pointsの表示, 入力点列(x, y, z, 時間, f)
//        System.out.println("points(x, y, z, 時間, f),   " + "長さ: " + points.length);
//        for(int i=0; i<=points.length-1; i++){
//            System.out.println(points[i]);
//        }

        //disListに距離入れてく（points), 全長disList.get(points.length-1)
        List<Double> disList = new ArrayList<>();
        double dis = 0.0;
        for (int i=0; i<points.length; i++){
            if (i == 0){
                disList.add(0.0);
            }
            else {
                dis += distance(points[i-1].x(), points[i].x(), points[i-1].y(), points[i].y());
                disList.add(dis);
            }
        }


        // 弧長パラメータを0から始まるようにシフトしておく.
        Range lRange = Range.create(0.0, disList.get(points.length-1));
        // 点列の時系列を正規化する.
        List<Point> normalizedPoints = normalizePoints(lRange, points);

        //disListのi番目のx, y, 距離をnormalizedPointsにsetする
        //normalizedPointsの中身は(x, y, 距離)
        for (int i=0; i<points.length; i++){
            normalizedPoints.set(i, Point.createXYT(normalizedPoints.get(i).x(), normalizedPoints.get(i).y(), disList.get(i)));
        }

        //次数
        int degree = 3;

        //kotyoListに距離を入れる
        List<Double> kotyoList = new ArrayList<>();
        //m_beginがnullじゃなかったら-Lsをいれる。nullなら何もなし。
        if(m_begin != null) {
            //m_beginから入力の1点目の距離Ls
            double Ls = distance(points[0].x(), m_begin.x(), points[0].y(), m_begin.y());
            normalizedPoints.add(0, Point.createXYT(m_begin.x(), m_begin.y(), -Ls));
            kotyoList.add(-Ls);
        }
        for(int i=0; i<sc_knots.size(); i++){
            if(points[0].time() <= sc_knots.get(i) && sc_knots.get(i) <= points[points.length-1].time()){
                for(int t=1; t<=points.length; t++) {
                    if(points[t-1].time() <= sc_knots.get(i) &&  sc_knots.get(i)<= points[t].time()) {
                        double u = sc_knots.get(i) - points[t-1].time();
                        double v = points[t].time() - sc_knots.get(i);
                        double a = disList.get(t-1);
                        double b = disList.get(t);
                        double d = (v * a + u * b) / (u + v);
                        kotyoList.add(d);
                        break;
                    }
                }
            }
        }
        //m_endがnullじゃなかったらdisList.get(points.length-1) + Leをいれる。nullなら何もなし。
        if(m_end != null) {
            //m_endから入力点の最後の点の距離Le
            double Le = distance(points[points.length-1].x(), m_end.x(), points[points.length-1].y(), m_end.y());
            normalizedPoints.add(Point.createXYT(m_end.x(), m_end.y(), disList.get(points.length-1)+Le-0.000001));
            kotyoList.add(disList.get(points.length-1) + Le);
        }
        else {
            kotyoList.add(disList.get(points.length-1)+0.000001);
        }

//        //disListの表示
//        System.out.println("disList: ");
//        for(int i=0; i<disList.size(); i++) {
//            System.out.println(disList.get(i));
//        }
//        System.out.println("disListの最後: " + disList.get(points.length-1));

        // リストを配列に変換する.
        Point[] points2 = normalizedPoints.toArray(new Point[0]);
//        //points2の表示
//        System.out.println("points2(x, y, z, 距離, f),   " + "長さ: " + points2.length);
//        for(int i=0; i<points.length; i++){
//            System.out.println(points2[i]);
//        }

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

//        //knot_2の表示
//        System.out.println("knot_2:    " + "長さ: " + knot_2.length);
//        for(int i=0; i<knot_2.length; i++){
//            System.out.println(knot_2[i]);
//        }

        // スプライン補間を行う
        // SplineCurveInterpolator.interpolateの引数は(点列(Point[]型), 次数(int型), 節点間隔(double型))にする.
        SplineCurve splineCurve = SplineCurveInterpolator.interpolate(points2, degree, knot_2);

        m_sc = splineCurve;


        /**グラフ作成　-------------------------------------------------------------------------------------------------*/

        Point[] sc_cp = m_sc.controlPoints();

        //sc_cp_disListに距離入れてく（sc_cp)
        List<Double> sc_cp_disList = new ArrayList<>();
        double dis2 = 0.0;
//        System.out.println("sc_cp_disList");        //表示
        for (int i=0; i<sc_cp.length; i++){
            if (i == 0){
                sc_cp_disList.add(0.0);
            }
            else {
                dis2 += distance(sc_cp[i-1].x(), sc_cp[i].x(), sc_cp[i-1].y(), sc_cp[i].y());
                sc_cp_disList.add(dis2);
            }
//            System.out.println(sc_cp_disList.get(i));     //表示
        }

        int num2 = max((int) ceil(m_sc.range().length() / 1e-2), 2);
        Point[] points01 = m_sc.evaluateAll(num2, ParametricEvaluable.EvaluationType.TIME);

//        System.out.println("points01");
//        for(int i=0; i<points01.length-1; i++) {
//            System.out.println(points01[i]);
//        }

        //sc_cp_disListに距離入れてく（sc_cp)
        List<Double> sc_ep_disList = new ArrayList<>();
        double dis3 = 0.0;
//        System.out.println("sc_ep_disList");        //表示
        for (int i=0; i<points01.length; i++){
            if (i == 0){
                sc_ep_disList.add(0.0);
            }
            else {
                dis3 += distance(points01[i-1].x(), points01[i].x(), points01[i-1].y(), points01[i].y());
                sc_ep_disList.add(dis3);
            }
//            System.out.println(sc_ep_disList.get(i));     //表示
        }

//        System.out.println("制御点の個数" + m_sc.controlPoints().length);

        //グラフ作成1（制御点x, 評価点x  -弧長l）
        double[][] cpx_l_graph = new double[m_sc.controlPoints().length][2];
        for(int i=0; i<m_sc.controlPoints().length; i++){
            cpx_l_graph[i][0] = knot_2[i+1];
            cpx_l_graph[i][1] = sc_cp[i].x();
        }
        double[][] epx_l_graph = new double[points01.length][2];
        for(int i=0; i<points01.length-1; i++){
            epx_l_graph[i][0] = points01[i].time();
            epx_l_graph[i][1] = points01[i].x();
        }
        double[][] epx_l_graph_s = new double[points.length][2];
        for(int i=0; i<points.length; i++){
            epx_l_graph_s[i][0] = disList.get(i);
            epx_l_graph_s[i][1] = points[i].x();
        }

        JavaPlot javaPlot = new JavaPlot();
        javaPlot.addPlot(cpx_l_graph);
        javaPlot.addPlot(epx_l_graph);
        javaPlot.addPlot(epx_l_graph_s);
        AbstractPlot plot = (AbstractPlot) javaPlot.getPlots().get(0);
        plot.setTitle("cp");
        AbstractPlot plot2 = (AbstractPlot) javaPlot.getPlots().get(1);
        plot2.setTitle("ep");
        AbstractPlot plot3 = (AbstractPlot) javaPlot.getPlots().get(2);
        plot3.setTitle("ep_s");
        javaPlot.setTitle("x");
        javaPlot.set("xlabel", "'arc length'");
        javaPlot.set("grid", "");
        javaPlot.set("key", "right outside");
        javaPlot.plot();

        //グラフ作成2(制御点y, 評価点y  -弧長l)
        double[][] cpy_l_graph = new double[m_sc.controlPoints().length][2];
        for(int i=0; i<m_sc.controlPoints().length; i++){
            cpy_l_graph[i][0] = knot_2[i+1];
            cpy_l_graph[i][1] = sc_cp[i].y();
        }
        double[][] epy_l_graph = new double[points01.length][2];
        for(int i=0; i<points01.length-1; i++){
            epy_l_graph[i][0] = points01[i].time();
            epy_l_graph[i][1] = points01[i].y();
        }
        double[][] epy_l_graph_s = new double[points.length][2];
        for(int i=0; i<points.length; i++){
            epy_l_graph_s[i][0] = disList.get(i);
            epy_l_graph_s[i][1] = points[i].y();
        }
        JavaPlot javaPlot2 = new JavaPlot();
        javaPlot2.addPlot(cpy_l_graph);
        javaPlot2.addPlot(epy_l_graph);
        javaPlot2.addPlot(epy_l_graph_s);
        AbstractPlot plot4 = (AbstractPlot) javaPlot2.getPlots().get(0);
        plot4.setTitle("cp");
        AbstractPlot plot5 = (AbstractPlot) javaPlot2.getPlots().get(1);
        plot5.setTitle("ep");
        AbstractPlot plot6 = (AbstractPlot) javaPlot2.getPlots().get(2);
        plot6.setTitle("ep_s");
        javaPlot2.setTitle("y");
        javaPlot2.set("xlabel", "'arc length'");
        javaPlot2.set("grid", "");
        javaPlot2.set("key", "right outside");
        javaPlot2.plot();

    }


    //距離計算
    public double distance(double _x1, double _x2, double _y1, double _y2){
        double X = _x2 - _x1;
        double Y = _y2 - _y1;
        double L = Math.sqrt(Math.pow(X, 2) + Math.pow(Y, 2));
        return L;
    }

    /**
     * 点列の時刻パラメータの正規化をします.
     * m_points全体の時刻パラメータが_range区間に収まるように正規化します.
     *
     * @param _range 正規化後の時刻パラメータの範囲
     */
    public List<Point> normalizePoints(Range _range, Point[] points) {
        double startTime = points[0].time();
        double timeLength = points[points.length-1].time() - startTime;
        double rangeLength = _range.length();
        List<Point> points3 = new ArrayList<>();
        for (Point point : points) {
            points3.add(Point.createXYT(point.x(), point.y()
                    , _range.start() + (point.time() - startTime) * (rangeLength / timeLength)));
        }
        return points3;
    }

    @Override
    public void paint(Graphics _g) {
        super.paint(_g);
        Graphics2D g = (Graphics2D)_g;

        // 入力点列を表示
        for (Point p : m_points) {
            _g.drawOval((int) p.x() - 3, (int) p.y() - 3, 6, 6);
        }

        // スプライン曲線を表示（緑）
        _g.setColor(Color.GREEN);
        if (m_splineCurve != null) {
            g.setStroke(new BasicStroke(5));
            drawSplineCurve(_g, m_splineCurve);
        }

        // 前の点を表示（赤)
        _g.setColor(Color.RED);
        if (m_begin != null) {
            g.setStroke(new BasicStroke(1));
            _g.fillOval((int) m_begin.x() - 4, (int) m_begin.y() - 4, 8, 8);
            _g.drawOval((int) (m_begin.x() - m_begin.fuzziness()),
                    (int) (m_begin.y() - m_begin.fuzziness()),
                    (int) (2 * m_begin.fuzziness()), (int) (2 * m_begin.fuzziness()));
        }

        // 後ろの点を表示（青）
        _g.setColor(Color.BLUE);
        if (m_end != null) {
            g.setStroke(new BasicStroke(1));
            _g.fillOval((int) m_end.x() - 4, (int) m_end.y() - 4, 8, 8);
            _g.drawOval((int) (m_end.x() - m_end.fuzziness()),
                    (int) (m_end.y() - m_end.fuzziness()),
                    (int) (2 * m_end.fuzziness()),
                    (int) (2 * m_end.fuzziness()));
        }

        //m_scの描画（オレンジ）
        _g.setColor(Color.orange);
        if(m_sc != null){
            g.setStroke(new BasicStroke(2));
            drawSplineCurve(_g, m_sc);
        }

        //m_scの制御点を表示
        _g.setColor(Color.CYAN);
        if(m_sc != null) {
            for (Point p : m_sc.controlPoints()) {
                _g.drawOval((int) p.x() - 3, (int) p.y() - 3, 6, 6);
            }
        }
    }


    //点列と節点列(knot_2)の表示
    public static void createPointsGraph(Point[] _points, double[] knot_2) {
        PointsGraph pointsGraph = PointsGraph.create(_points, knot_2);
        POINTS_GRAPH_FRAME.getContentPane().removeAll();
        POINTS_GRAPH_FRAME.getContentPane().add(pointsGraph);
        POINTS_GRAPH_FRAME.pack();
        POINTS_GRAPH_FRAME.setVisible(true);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        m_points.clear();
        m_points.add(Point.createXYT(e.getX(), e.getY(), System.currentTimeMillis() * 1e-3));
        m_splineCurve = null;
        m_begin = null;
        m_end = null;
        m_sc = null;
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
            // 1秒間に100点程度表示する。1点あっても線にならないから、2より大きいのを取るためにmaxで2より大きくしてる
            int num = max((int) ceil(splineCurve.range().length() / 1e-2), 2);
            //points: スプライン曲線を等時間間隔でnum点で評価した点列
            Point[] points = splineCurve.evaluateAll(num, ParametricEvaluable.EvaluationType.TIME);

//            System.out.println("points");
//            for(int i=0; i<points.length-1; i++) {
//                System.out.println(points[i]);
//            }

//            // ファジネスを表示
//            for (Point p : points) {
//                _g.drawOval((int) (p.x() - p.fuzziness()), (int) (p.y() - p.fuzziness()), (int) (2 * p.fuzziness()), (int) (2 * p.fuzziness()));
//            }

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
            if(index >= 0) {
                execute(fragments[index].curve(), begin, end);
            }
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

    private SplineCurve m_sc = null;

    /** 前の点 */
    private Point m_begin = null;
    /** 後ろの点 */
    private Point m_end = null;

    /** PointsGraphを保持するためのJFrame */
    private static final JFrame POINTS_GRAPH_FRAME = new JFrame();
}
