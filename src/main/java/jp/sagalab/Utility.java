package jp.sagalab;

import jp.sagalab.jftk.Point;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Utility {
    public static void savePoints(List<Point> m_points) {
        File output = new File("output");
        if (!output.exists()) {
            output.mkdir();
        }
        try {
            PrintWriter pw = new PrintWriter(new File(output, "points_" + System.currentTimeMillis() + ".csv"));
            for (Point p : m_points) {
                pw.print(p.x());
                pw.print(",");
                pw.print(p.y());
                pw.print(",");
                pw.println(p.time());
            }
            pw.close();
        } catch (IOException ignore) {

        }
    }

    public static List<Point> loadPoints(String fineName) {
        List<Point> m_points = new ArrayList<>();
        try {
            Scanner scanner = new Scanner(new File(fineName));
            while (scanner.hasNext()) {
                String line = scanner.nextLine();
                String[] split = line.split(",");
                m_points.add(Point.createXYT(
                        Double.parseDouble(split[0]),
                        Double.parseDouble(split[1]),
                        Double.parseDouble(split[2])
                ));
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        return m_points;
    }
}
