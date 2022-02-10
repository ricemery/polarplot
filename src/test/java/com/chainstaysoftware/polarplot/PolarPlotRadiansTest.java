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

package com.chainstaysoftware.polarplot;

import com.chainstaysoftware.polarplot.data.XYChartItem;
import com.chainstaysoftware.polarplot.series.XYSeries;
import com.chainstaysoftware.polarplot.tools.Helper;
import com.chainstaysoftware.polarplot.tools.Order;
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
import java.util.stream.Collectors;


public class PolarPlotRadiansTest extends Application {
    private PolarPlot<XYChartItem> polarPlot;
    private Legend legend;

    @Override
    public void init() {
        List<XYChartItem> xyItems1 = new ArrayList<>();
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

        final var xyItems1Radians = toRadians(xyItems1);
        final var xyItems2Radians = toRadians(xyItems2);

        final var xySeries1 = new XYSeries(xyItems1Radians, ChartType.INTERPOLATE_POLAR_RADIANS,
           Color.TRANSPARENT, Color.DODGERBLUE);
        xySeries1.setSymbol(Symbol.NONE);
        xySeries1.setWithWrapping(true);

        final var xySeries2 = new XYSeries(xyItems2Radians, ChartType.INTERPOLATE_POLAR_RADIANS,
           Color.TRANSPARENT, Color.GREEN);
        xySeries2.setSymbol(Symbol.NONE);

        final var polarPane = new XYPane(xySeries1, xySeries2);
        polarPane.setLowerBoundY(-100);
        polarPane.setUpperBoundY(20);
        polarPane.setPolarTickStep(PolarTickStep.THIRTY);
        polarPane.setThresholdY(0);
        polarPane.setThresholdYVisible(true);
        polarPane.setThresholdYColor(Color.RED);
        final var polarRingValues = List.of(-75.0, -50.0, -25.0, 0.0, 20.0);
        polarPane.setYPolarRingValues(polarRingValues);

        polarPlot = new PolarPlot<>(polarPane);

        final var legendItems1 = new LegendItem("xyItems1", Color.DODGERBLUE);
        final var legendItems2 = new LegendItem("xyItems2", Color.GREEN);
        legend = new Legend();
        legend.setLegendItems(legendItems1, legendItems2);
        legend.setOrientation(Orientation.VERTICAL);
        legend.setPrefHeight(50);
    }

    private List<XYChartItem> toRadians(List<XYChartItem> xyItems1) {
        return xyItems1.stream().map(item -> new XYChartItem(Math.toRadians(item.getX()), item.getY()))
        .collect(Collectors.toList());
    }

    @Override
    public void start(Stage stage) {
        final var pane = new BorderPane();
        pane.setCenter(polarPlot);

        pane.setBottom(legend);

        pane.setPadding(new Insets(10));

        final var scene = new Scene(new StackPane(pane));

        stage.setTitle("Polar Chart");
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() {
        System.exit(0);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
