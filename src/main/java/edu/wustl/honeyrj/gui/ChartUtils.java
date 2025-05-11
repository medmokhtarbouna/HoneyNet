package edu.wustl.honeyrj.gui;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import javax.swing.*;
import java.util.Map;

public class ChartUtils {

    public static JPanel createPieChart(String title, Map<String, Integer> data) {
        DefaultPieDataset dataset = new DefaultPieDataset();
        for (Map.Entry<String, Integer> entry : data.entrySet()) {
            dataset.setValue(entry.getKey(), entry.getValue());
        }

        JFreeChart chart = ChartFactory.createPieChart(
                title,
                dataset,
                true, true, false);

        return new ChartPanel(chart);
    }

    public static JPanel createBarChart(String title, String categoryAxis, String valueAxis, Map<String, Integer> data) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (Map.Entry<String, Integer> entry : data.entrySet()) {
            dataset.addValue(entry.getValue(), entry.getKey(), "");
        }

        JFreeChart chart = ChartFactory.createBarChart(
                title,
                categoryAxis,
                valueAxis,
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false);

        return new ChartPanel(chart);
    }
}
