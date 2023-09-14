package jp.sagalab;

import jp.sagalab.graph.PointsGraph;
import jp.sagalab.jftk.Point;

import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        MyPanel panel = new MyPanel();
        frame.getContentPane().add(panel, BorderLayout.CENTER);
        JTextField textField = new JTextField("output/points.csv");
        frame.getContentPane().add(textField, BorderLayout.NORTH);
        JPanel bottom = new JPanel();
        JButton save = new JButton("ほぞん");
        save.addActionListener((e) -> panel.save());
        bottom.add(save);
        JButton load = new JButton("よみだし");
        load.addActionListener((e) -> panel.load(textField.getText()));
        bottom.add(load);
        frame.getContentPane().add(bottom, BorderLayout.SOUTH);
        frame.pack();
        frame.setVisible(true);
    }
    
}
