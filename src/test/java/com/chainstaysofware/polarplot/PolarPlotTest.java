/*
 * Copyright (c) 2018 by Gerrit Grunwald
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.chainstaysofware.polarplot;

import com.chainstaysofware.polarplot.data.XYChartItem;
import com.chainstaysofware.polarplot.series.XYSeries;
import com.chainstaysofware.polarplot.tools.Helper;
import com.chainstaysofware.polarplot.tools.Order;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;


public class PolarPlotTest extends Application {
    private XYSeries<XYChartItem> xySeries1;

    private PolarPlot<XYChartItem> polarPlot;

    @Override public void init() {
        List<XYChartItem> xyItems1 = new ArrayList<>();
        xyItems1.add(new XYChartItem(0.0, -18.0));
        xyItems1.add(new XYChartItem(25.0, -18.0));
        xyItems1.add(new XYChartItem(25.0, -5.0));
        xyItems1.add(new XYChartItem(32.0, -5.0));
        xyItems1.add(new XYChartItem(32.0, -20.0));
        xyItems1.add(new XYChartItem(40.0, -20.0));
        xyItems1.add(new XYChartItem(40.0, -8.0));
        xyItems1.add(new XYChartItem(50.0, -8.0));
        xyItems1.add(new XYChartItem(50.0, -20.0));
        xyItems1.add(new XYChartItem(78.0, -20.0));
        xyItems1.add(new XYChartItem(78.0, -10.0));
        xyItems1.add(new XYChartItem(110.0, -10.0));
        xyItems1.add(new XYChartItem(110.0, -20.0));
        xyItems1.add(new XYChartItem(150.0, -20.0));
        xyItems1.add(new XYChartItem(150.0, -15.0));
        xyItems1.add(new XYChartItem(210.0, -15.0));
        xyItems1.add(new XYChartItem(210.0, -20.0));
        xyItems1.add(new XYChartItem(260.0, -20.0));
        xyItems1.add(new XYChartItem(260.0, -10.0));
        xyItems1.add(new XYChartItem(280.0, -10.0));
        xyItems1.add(new XYChartItem(280.0, -20.0));
        xyItems1.add(new XYChartItem(310.0, -20.0));
        xyItems1.add(new XYChartItem(310.0, -9.0));
        xyItems1.add(new XYChartItem(325.0, -9.0));
        xyItems1.add(new XYChartItem(325.0, -20.0));
        xyItems1.add(new XYChartItem(328.0, -20.0));
        xyItems1.add(new XYChartItem(328.0, -5.0));
        xyItems1.add(new XYChartItem(335.0, -5.0));
        xyItems1.add(new XYChartItem(335.0, -18.0));
        xyItems1.add(new XYChartItem(360.0, -18.0));
        Helper.orderXYChartItemsByX(xyItems1, Order.ASCENDING);

        List<XYChartItem> xyItems2 = new ArrayList<>();
        xyItems2.add(new XYChartItem(0, -30));
        xyItems2.add(new XYChartItem(30, -32));
        xyItems2.add(new XYChartItem(45, -30));
        xyItems2.add(new XYChartItem(90, -45));
        xyItems2.add(new XYChartItem(95, -20));
        xyItems2.add(new XYChartItem(100, -44));
        xyItems2.add(new XYChartItem(200, -50));
        xyItems2.add(new XYChartItem(222, -53));
        xyItems2.add(new XYChartItem(228, -50));
        xyItems2.add(new XYChartItem(300, -15));

        xySeries1 = new XYSeries(xyItems1, ChartType.INTERPOLATE_POLAR,
           Color.TRANSPARENT, Color.DODGERBLUE);
        //xySeries1.setShowPoints(false);
//        xySeries1.setStroke(Color.rgb(90, 90, 90));
//        xySeries1.setSymbolStroke(Color.LIME);
//        xySeries1.setSymbolFill(Color.GREEN);
        xySeries1.setSymbol(Symbol.NONE);

        final var xySeries2 = new XYSeries(xyItems2, ChartType.INTERPOLATE_POLAR,
           Color.TRANSPARENT, Color.GREEN);
        //xySeries1.setShowPoints(false);
//        xySeries1.setStroke(Color.rgb(90, 90, 90));
//        xySeries1.setSymbolStroke(Color.LIME);
//        xySeries1.setSymbolFill(Color.GREEN);
        xySeries2.setSymbol(Symbol.NONE);


        XYPane polarPane = new XYPane(xySeries1, xySeries2);
//        polarPane.setLowerBoundY(polarPane.getDataMinY());
//        polarPane.setUpperBoundY(polarPane.getDataMaxY());
        polarPane.setLowerBoundY(-100);
        polarPane.setUpperBoundY(20);
        polarPane.setPolarTickStep(PolarTickStep.THIRTY);
        polarPane.setThresholdY(0);
        polarPane.setThresholdYVisible(true);
        polarPane.setThresholdYColor(Color.RED);
        polarPane.setYPolarRingValues(List.of(-75.0, -50.0, -25.0, 0.0, 20.0));

        polarPlot = new PolarPlot<>(polarPane);
    }

    @Override public void start(Stage stage) {
        BorderPane pane = new BorderPane();
        pane.setCenter(polarPlot);

        final var legendItems1 = new LegendItem("xyItems1", Color.DODGERBLUE);
        final var legendItems2 = new LegendItem("xyItems2", Color.GREEN);
        final var legend = new Legend();
        legend.setLegendItems(legendItems1, legendItems2);
        legend.setOrientation(Orientation.VERTICAL);
        legend.setPrefHeight(50);

        pane.setBottom(legend);

        pane.setPadding(new Insets(10));

        Scene scene = new Scene(new StackPane(pane));

        stage.setTitle("Polar Chart");
        stage.setScene(scene);
        stage.show();

        //timer.start();
    }

    @Override public void stop() {
        System.exit(0);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
