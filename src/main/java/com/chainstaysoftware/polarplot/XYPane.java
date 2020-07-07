/*
 * Copyright (c) 2017 by Gerrit Grunwald
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
import com.chainstaysoftware.polarplot.data.XYItem;
import com.chainstaysoftware.polarplot.font.Fonts;
import com.chainstaysoftware.polarplot.series.XYSeries;
import com.chainstaysoftware.polarplot.tools.FormatAngle;
import com.chainstaysoftware.polarplot.tools.Helper;
import com.chainstaysoftware.polarplot.tools.Interpolator;
import com.chainstaysoftware.polarplot.tools.Point;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.BooleanPropertyBase;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.DoublePropertyBase;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ListPropertyBase;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.Paint;
import javafx.scene.paint.RadialGradient;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static com.chainstaysoftware.polarplot.tools.Helper.clamp;


/**
 * Created by hansolo on 16.07.17.
 */
public class XYPane<T extends XYItem> extends Region implements ChartArea {
   private static final double PREFERRED_WIDTH = 250;
   private static final double PREFERRED_HEIGHT = 250;
   private static final double MINIMUM_WIDTH = 0;
   private static final double MINIMUM_HEIGHT = 0;
   private static final double MAXIMUM_WIDTH = 4096;
   private static final double MAXIMUM_HEIGHT = 4096;
   private static final double MIN_SYMBOL_SIZE = 2;
   private static final double MAX_SYMBOL_SIZE = 6;
   private static final int SUB_DIVISIONS = 24;
   private static double aspectRatio;
   private boolean keepAspect;
   private double size;
   private double width;
   private double height;
   private Paint _chartBackground;
   private ObjectProperty<Paint> chartBackground;
   private List<XYSeries<T>> listOfSeries;
   private Canvas canvas;
   private GraphicsContext ctx;
   private double scaleX;
   private double scaleY;
   private double symbolSize;
   private int noOfBands;
   private double _lowerBoundX;
   private DoubleProperty lowerBoundX;
   private double _upperBoundX;
   private DoubleProperty upperBoundX;
   private double _lowerBoundY;
   private DoubleProperty lowerBoundY;
   private double _upperBoundY;
   private DoubleProperty upperBoundY;
   private boolean referenceZero;
   private Tooltip tooltip;
   private double _thresholdY;
   private DoubleProperty thresholdY;
   private boolean _thresholdYVisible;
   private BooleanProperty thresholdYVisible;
   private Color _thresholdYColor;
   private ObjectProperty<Color> thresholdYColor;
   private List<Double> _polarYRingValues;
   private ListProperty<Double> polarYRingValues;
   private PolarTickStep _polarTickStep;
   private ObjectProperty<PolarTickStep> polarTickStep;


   // ******************** Constructors **************************************
   public XYPane(final XYSeries<T>... SERIES) {
      this(Color.TRANSPARENT, 1, SERIES);
   }

   public XYPane(final int BANDS, final XYSeries<T>... SERIES) {
      this(Color.TRANSPARENT, BANDS, SERIES);
   }

   public XYPane(final Paint BACKGROUND, final int BANDS, final XYSeries<T>... SERIES) {
      getStylesheets().add(XYPane.class.getResource("polarplot.css").toExternalForm());
      aspectRatio = PREFERRED_HEIGHT / PREFERRED_WIDTH;
      keepAspect = false;
      _chartBackground = BACKGROUND;
      listOfSeries = FXCollections.observableArrayList(SERIES);
      scaleX = 1;
      scaleY = 1;
      symbolSize = 2;
      noOfBands = Helper.clamp(1, 5, BANDS);
      _lowerBoundX = 0;
      _upperBoundX = 100;
      _lowerBoundY = 0;
      _upperBoundY = 100;
      referenceZero = true;
      _thresholdY = 100;
      _thresholdYVisible = false;
      _thresholdYColor = Color.RED;
      _polarTickStep = PolarTickStep.FOURTY_FIVE;

      initGraphics();
      registerListeners();
   }


   // ******************** Initialization ************************************
   private void initGraphics() {
      if (Double.compare(getPrefWidth(), 0.0) <= 0 || Double.compare(getPrefHeight(), 0.0) <= 0 || Double.compare(getWidth(), 0.0) <= 0 ||
         Double.compare(getHeight(), 0.0) <= 0) {
         if (getPrefWidth() > 0 && getPrefHeight() > 0) {
            setPrefSize(getPrefWidth(), getPrefHeight());
         } else {
            setPrefSize(PREFERRED_WIDTH, PREFERRED_HEIGHT);
         }
      }

      getStyleClass().setAll("chart", "xy-chart");

      canvas = new Canvas(PREFERRED_WIDTH, PREFERRED_HEIGHT);
      ctx = canvas.getGraphicsContext2D();

      getChildren().setAll(canvas);
   }

   private void registerListeners() {
      widthProperty().addListener(o -> resize());
      heightProperty().addListener(o -> resize());

      listOfSeries.forEach(series -> series.setOnSeriesEvent(seriesEvent -> redraw()));
      canvas.setOnMouseClicked(e -> {
         final double LOWER_BOUND_X = getLowerBoundX();
         final double LOWER_BOUND_Y = getLowerBoundY();
         double x = (e.getX() - LOWER_BOUND_X) * scaleX;
         double y = height - (e.getY() - LOWER_BOUND_Y) * scaleY;
      });
   }


   // ******************** Methods *******************************************
   @Override
   protected double computeMinWidth(final double HEIGHT) {
      return MINIMUM_WIDTH;
   }

   @Override
   protected double computeMinHeight(final double WIDTH) {
      return MINIMUM_HEIGHT;
   }

   @Override
   protected double computePrefWidth(final double HEIGHT) {
      return super.computePrefWidth(HEIGHT);
   }

   @Override
   protected double computePrefHeight(final double WIDTH) {
      return super.computePrefHeight(WIDTH);
   }

   @Override
   protected double computeMaxWidth(final double HEIGHT) {
      return MAXIMUM_WIDTH;
   }

   @Override
   protected double computeMaxHeight(final double WIDTH) {
      return MAXIMUM_HEIGHT;
   }

   @Override
   public ObservableList<Node> getChildren() {
      return super.getChildren();
   }

   public Paint getChartBackground() {
      return null == chartBackground ? _chartBackground : chartBackground.get();
   }

   public void setChartBackground(final Paint PAINT) {
      if (null == chartBackground) {
         _chartBackground = PAINT;
         redraw();
      } else {
         chartBackground.set(PAINT);
      }
   }

   public ObjectProperty<Paint> chartBackgroundProperty() {
      if (null == chartBackground) {
         chartBackground = new ObjectPropertyBase<Paint>(_chartBackground) {
            @Override
            protected void invalidated() {
               redraw();
            }

            @Override
            public Object getBean() {
               return XYPane.this;
            }

            @Override
            public String getName() {
               return "chartBackground";
            }
         };
         _chartBackground = null;
      }
      return chartBackground;
   }

   public int getNoOfBands() {
      return noOfBands;
   }

   public void setNoOfBands(final int BANDS) {
      noOfBands = Helper.clamp(1, 5, BANDS);
      redraw();
   }

   public double getLowerBoundX() {
      return null == lowerBoundX ? _lowerBoundX : lowerBoundX.get();
   }

   public void setLowerBoundX(final double VALUE) {
      if (null == lowerBoundX) {
         _lowerBoundX = VALUE;
         resize();
      } else {
         lowerBoundX.set(VALUE);
      }
   }

   public DoubleProperty lowerBoundXProperty() {
      if (null == lowerBoundX) {
         lowerBoundX = new DoublePropertyBase(_lowerBoundX) {
            @Override
            protected void invalidated() {
               resize();
            }

            @Override
            public Object getBean() {
               return XYPane.this;
            }

            @Override
            public String getName() {
               return "lowerBoundX";
            }
         };
      }
      return lowerBoundX;
   }

   public double getUpperBoundX() {
      return null == upperBoundX ? _upperBoundX : upperBoundX.get();
   }

   public void setUpperBoundX(final double VALUE) {
      if (null == upperBoundX) {
         _upperBoundX = VALUE;
         resize();
      } else {
         upperBoundX.set(VALUE);
      }
   }

   public DoubleProperty upperBoundXProperty() {
      if (null == upperBoundX) {
         upperBoundX = new DoublePropertyBase(_upperBoundX) {
            @Override
            protected void invalidated() {
               resize();
            }

            @Override
            public Object getBean() {
               return XYPane.this;
            }

            @Override
            public String getName() {
               return "upperBoundX";
            }
         };
      }
      return upperBoundX;
   }

   public double getLowerBoundY() {
      return null == lowerBoundY ? _lowerBoundY : lowerBoundY.get();
   }

   public void setLowerBoundY(final double VALUE) {
      if (null == lowerBoundY) {
         _lowerBoundY = VALUE;
         resize();
      } else {
         lowerBoundY.set(VALUE);
      }
   }

   public DoubleProperty lowerBoundYProperty() {
      if (null == lowerBoundY) {
         lowerBoundY = new DoublePropertyBase(_lowerBoundY) {
            @Override
            protected void invalidated() {
               resize();
            }

            @Override
            public Object getBean() {
               return XYPane.this;
            }

            @Override
            public String getName() {
               return "lowerBoundY";
            }
         };
      }
      return lowerBoundY;
   }

   public double getUpperBoundY() {
      return null == upperBoundY ? _upperBoundY : upperBoundY.get();
   }

   public void setUpperBoundY(final double VALUE) {
      if (null == upperBoundY) {
         _upperBoundY = VALUE;
         resize();
      } else {
         upperBoundY.set(VALUE);
      }
   }

   public DoubleProperty upperBoundYProperty() {
      if (null == upperBoundY) {
         upperBoundY = new DoublePropertyBase(_upperBoundY) {
            @Override
            protected void invalidated() {
               resize();
            }

            @Override
            public Object getBean() {
               return XYPane.this;
            }

            @Override
            public String getName() {
               return "upperBoundY";
            }
         };
      }
      return upperBoundY;
   }

   public boolean isReferenceZero() {
      return referenceZero;
   }

   public void setReferenceZero(final boolean IS_ZERO) {
      referenceZero = IS_ZERO;
      redraw();
   }

   public double getRangeX() {
      return getUpperBoundX() - getLowerBoundX();
   }

   public double getRangeY() {
      return getUpperBoundY() - getLowerBoundY();
   }

   public double getDataMinX() {
      return listOfSeries.stream().mapToDouble(XYSeries::getMinX).min().getAsDouble();
   }

   public double getDataMaxX() {
      return listOfSeries.stream().mapToDouble(XYSeries::getMaxX).max().getAsDouble();
   }

   public double getDataMinY() {
      return listOfSeries.stream().mapToDouble(XYSeries::getMinY).min().getAsDouble();
   }

   public double getDataMaxY() {
      return listOfSeries.stream().mapToDouble(XYSeries::getMaxY).max().getAsDouble();
   }

   public double getDataRangeX() {
      return getDataMaxX() - getDataMinX();
   }

   public double getDataRangeY() {
      return getDataMaxY() - getDataMinY();
   }

   public List<XYSeries<T>> getListOfSeries() {
      return listOfSeries;
   }

   public double getThresholdY() {
      return null == thresholdY ? _thresholdY : thresholdY.get();
   }

   public void setThresholdY(final double THRESHOLD) {
      if (null == thresholdY) {
         _thresholdY = THRESHOLD;
         redraw();
      } else {
         thresholdY.set(THRESHOLD);
      }
   }

   public DoubleProperty thresholdYProperty() {
      if (null == thresholdY) {
         thresholdY = new DoublePropertyBase(_thresholdY) {
            @Override
            protected void invalidated() {
               redraw();
            }

            @Override
            public Object getBean() {
               return XYPane.this;
            }

            @Override
            public String getName() {
               return "thresholdY";
            }
         };
      }
      return thresholdY;
   }

   public boolean isThresholdYVisible() {
      return null == thresholdYVisible ? _thresholdYVisible : thresholdYVisible.get();
   }

   public void setThresholdYVisible(final boolean VISIBLE) {
      if (null == thresholdYVisible) {
         _thresholdYVisible = VISIBLE;
         redraw();
      } else {
         thresholdYVisible.set(VISIBLE);
      }
   }

   public BooleanProperty thresholdYVisibleProperty() {
      if (null == thresholdYVisible) {
         thresholdYVisible = new BooleanPropertyBase(_thresholdYVisible) {
            @Override
            protected void invalidated() {
               redraw();
            }

            @Override
            public Object getBean() {
               return XYPane.this;
            }

            @Override
            public String getName() {
               return "thresholdYVisible";
            }
         };
      }
      return thresholdYVisible;
   }

   public Color getThresholdYColor() {
      return null == thresholdYColor ? _thresholdYColor : thresholdYColor.get();
   }

   public void setThresholdYColor(final Color COLOR) {
      if (null == thresholdYColor) {
         _thresholdYColor = COLOR;
         redraw();
      } else {
         thresholdYColor.set(COLOR);
      }
   }

   public ObjectProperty<Color> thresholdYColorProperty() {
      if (null == thresholdYColor) {
         thresholdYColor = new ObjectPropertyBase<Color>(_thresholdYColor) {
            @Override
            protected void invalidated() {
               redraw();
            }

            @Override
            public Object getBean() {
               return XYPane.this;
            }

            @Override
            public String getName() {
               return "thresholdYColor";
            }
         };
         _thresholdYColor = null;
      }
      return thresholdYColor;
   }

   public List<Double> getYPolarRingValues() {
      return null == polarYRingValues
         ? _polarYRingValues
         : polarYRingValues.get();
   }

   public void setYPolarRingValues(final List<Double> values) {
      if (null == polarYRingValues) {
         _polarYRingValues = List.copyOf(values);
         drawChart();
      } else {
         polarYRingValues.get().setAll(values);
      }
   }

   public ListProperty<Double> polarYRingValuesProperty() {
      if (polarYRingValues == null) {
         polarYRingValues = new ListPropertyBase<Double>() {
            @Override
            protected void invalidated() {
               drawChart();
            }

            @Override
            public Object getBean() {
               return XYPane.this;
            }

            @Override
            public String getName() {
               return "polarYRingValues";
            }
         };
         _polarYRingValues = null;
      }

      return polarYRingValues;
   }

   public PolarTickStep getPolarTickStep() {
      return null == polarTickStep ? _polarTickStep : polarTickStep.get();
   }

   public void setPolarTickStep(final PolarTickStep STEP) {
      if (null == polarTickStep) {
         _polarTickStep = STEP;
         drawChart();
      } else {
         polarTickStep.set(STEP);
      }
   }

   public ObjectProperty<PolarTickStep> polarTickStepProperty() {
      if (null == polarTickStep) {
         polarTickStep = new ObjectPropertyBase<PolarTickStep>() {
            @Override
            protected void invalidated() {
               drawChart();
            }

            @Override
            public Object getBean() {
               return XYPane.this;
            }

            @Override
            public String getName() {
               return "polarTickStep";
            }
         };
         _polarTickStep = null;
      }
      return polarTickStep;
   }

   public boolean containsPolarChart() {
      for (XYSeries<T> series : listOfSeries) {
         ChartType type = series.getChartType();
         if (ChartType.INTERPOLATE_POLAR == type || ChartType.INTERPOLATE_POLAR_RADIANS == type) {
            return true;
         }
      }
      return false;
   }


   // ******************** Draw Chart ****************************************
   protected void redraw() {
      drawChart();
   }

   private void drawChart() {
      if (null == listOfSeries || listOfSeries.isEmpty()) return;

      ctx.clearRect(0, 0, width, height);
      ctx.setFill(getChartBackground());
      ctx.fillRect(0, 0, width, height);

      final double circleSize = 0.9 * size;
      final double range = .5 * circleSize;
      final double offset = 0;
      final boolean useRadians = listOfSeries.stream()
         .anyMatch(series -> ChartType.INTERPOLATE_POLAR_RADIANS.equals(series.getChartType()));

      drawPolarGrid(getPolarTickStep().get(),
         range,
         offset,
         useRadians);

      for (XYSeries<T> series : listOfSeries) {
         final ChartType TYPE = series.getChartType();
         switch (TYPE) {
            case INTERPOLATE_POLAR:
            case INTERPOLATE_POLAR_RADIANS:
               drawPolar(series, range, offset);
               break;
         }
      }

   }

   private void drawPolar(final XYSeries<T> series,
                          final double range,
                          final double offset) {
      final double xCenter = 0.5 * size;
      final double yCenter = xCenter;
      final double yLowerBound = getLowerBoundY();
      final double yRange = getRangeY();
      final boolean showPoints = series.getSymbolsVisible();


      // draw the chart data
      ctx.save();
      if (series.getFill() instanceof RadialGradient) {
         ctx.setFill(new RadialGradient(0, 0, size * 0.5, size * 0.5,
            size * 0.45, false, CycleMethod.NO_CYCLE, ((RadialGradient) series.getFill()).getStops()));
      } else {
         ctx.setFill(series.getFill());
      }
      ctx.setLineWidth(series.getStrokeWidth() > -1 ? series.getStrokeWidth() : size * 0.0025);
      ctx.setStroke(series.getStroke());
      ctx.setLineJoin(StrokeLineJoin.ROUND);

      T firstItem = series.getItems().get(0);
      Point[] points = null;

      if (ChartType.INTERPOLATE_POLAR == series.getChartType() || ChartType.INTERPOLATE_POLAR_RADIANS == series.getChartType()) {
         final boolean useRadians = ChartType.INTERPOLATE_POLAR_RADIANS == series.getChartType();
         //noinspection unchecked
         points = buildPoints(series, xCenter, yCenter, yLowerBound,
            yRange, range, offset, useRadians);
         final var interpolatedItems = interpolate(series, useRadians);
         //noinspection unchecked
         final var interpolatedPoints = buildPoints((List<T>) interpolatedItems, xCenter, yCenter, yLowerBound,
            yRange, range, offset, useRadians);

         if (series.isWithWrapping()) {
            ctx.beginPath();
            ctx.moveTo(interpolatedPoints[0].getX(), interpolatedPoints[0].getY());
            for (Point point : interpolatedPoints) {
               ctx.lineTo(point.getX(), point.getY());
            }
            ctx.closePath();
         } else {
            ctx.beginPath();
            final var xVals = Arrays.stream(interpolatedPoints)
               .mapToDouble(Point::getX)
               .toArray();
            final var yVals = Arrays.stream(interpolatedPoints)
               .mapToDouble(Point::getY)
               .toArray();
            ctx.strokePolyline(xVals, yVals, interpolatedPoints.length);
            ctx.closePath();
         }
      }

      ctx.fill();
      ctx.stroke();

      ctx.restore();

      if (showPoints) {
         drawPoints(series, points);
      }
   }

   private List<XYChartItem> interpolate(XYSeries<T> series, boolean useRadians) {
      //noinspection unchecked
      return useRadians
         ? Interpolator.interpolateRadians((List<XYChartItem>) series.getItems())
         : Interpolator.interpolate((List<XYChartItem>) series.getItems());
   }

   private Point[] buildPoints(XYSeries<T> series,
                               double xCenter,
                               double yCenter,
                               double yLowerBound,
                               double yRange,
                               double range,
                               double offset,
                               boolean useRadians) {
      final var items = series.getItems();
      return buildPoints(items, xCenter, yCenter, yLowerBound, yRange, range,
         offset, useRadians);
   }

   private Point[] buildPoints(List<T> items,
                               double xCenter,
                               double yCenter,
                               double yLowerBound,
                               double yRange,
                               double range,
                               double offset,
                               boolean useRadians) {
      if (useRadians) {
         return buildPointsRadians(items, xCenter, yCenter, yLowerBound,
            yRange, range, offset);
      } else {
         return buildPointsDegrees(items, xCenter, yCenter, yLowerBound,
            yRange, range, offset);
      }
   }

   private Point[] buildPointsRadians(List<T> items,
                                      double xCenter,
                                      double yCenter,
                                      double yLowerBound,
                                      double yRange,
                                      double range,
                                      double offset) {
      T item = items.get(0);
      double radAngle = Math.toRadians(180);
      int numOfItems = items.size();
      Point[] points = new Point[numOfItems];

      double r1 = (yCenter - (yCenter - offset - ((item.getY() - yLowerBound) / yRange) * range));
      double phi = Helper.clamp(0.0, Math.toRadians(360.0), item.getX());
      double x = xCenter + (-Math.sin(radAngle + phi) * r1);
      double y = yCenter + (+Math.cos(radAngle + phi) * r1);
      points[0] = new Point(x, y);

      for (int i = 1; i < numOfItems; i++) {
         item = items.get(i);
         r1 = (yCenter - (yCenter - offset - ((item.getY() - yLowerBound) / yRange) * range));
         phi = Helper.clamp(0.0, Math.toRadians(360.0), item.getX());
         x = xCenter + (-Math.sin(radAngle + phi) * r1);
         y = yCenter + (+Math.cos(radAngle + phi) * r1);
         points[i] = new Point(x, y);
      }
      return points;
   }

   private Point[] buildPointsDegrees(List<T> items,
                                      double xCenter,
                                      double yCenter,
                                      double yLowerBound,
                                      double yRange,
                                      double range,
                                      double offset) {
      T item = items.get(0);
      double radAngle = Math.toRadians(180);
      int numOfItems = items.size();
      Point[] points = new Point[numOfItems];

      double r1 = (yCenter - (yCenter - offset - ((item.getY() - yLowerBound) / yRange) * range));
      double phi = Math.toRadians(Helper.clamp(0.0, 360.0, item.getX()));
      double x = xCenter + (-Math.sin(radAngle + phi) * r1);
      double y = yCenter + (+Math.cos(radAngle + phi) * r1);
      points[0] = new Point(x, y);

      for (int i = 1; i < numOfItems; i++) {
         item = items.get(i);
         r1 = (yCenter - (yCenter - offset - ((item.getY() - yLowerBound) / yRange) * range));
         phi = Math.toRadians(Helper.clamp(0.0, 360.0, item.getX()));
         x = xCenter + (-Math.sin(radAngle + phi) * r1);
         y = yCenter + (+Math.cos(radAngle + phi) * r1);
         points[i] = new Point(x, y);
      }
      return points;
   }

   private void drawPolarGrid(final double angleStep,
                              final double range,
                              final double offset,
                              final boolean useRadians) {
      final double xCenter = 0.5 * size;
      final double yCenter = xCenter;
      final double circleSize = 0.90 * size;
      final double yRange = getRangeY();
      final double numSectors = 360.0 / angleStep;

      // draw star lines
      ctx.save();
      ctx.setStroke(Color.LIGHTGRAY);
      for (int i = 0; i < numSectors; i++) {
         ctx.strokeLine(xCenter, 0.05 * size, xCenter, 0.5 * size);
         Helper.rotateCtx(ctx, xCenter, yCenter, angleStep);
      }
      ctx.restore();

      drawConcentricRings(circleSize, yCenter, getLowerBoundY(), yRange, range, offset);

      // draw threshold circle
      if (isThresholdYVisible()) {
         drawCircle(getThresholdY(), yCenter, getLowerBoundY(), yRange, range, offset,
            1, getThresholdYColor());
      }

      ctx.setTextAlign(TextAlignment.CENTER);
      ctx.setTextBaseline(VPos.CENTER);
      ctx.setFill(Color.BLACK);

      // draw axis text
      ctx.save();
      ctx.setFont(Fonts.latoRegular(0.025 * size));
      for (int i = 0; i < numSectors; i++) {
         final var angle = useRadians
            ? Math.toRadians(i * angleStep)
            : i * angleStep;
         final var text = FormatAngle.format(angle, useRadians);
         ctx.fillText(text, xCenter, size * 0.02);
         Helper.rotateCtx(ctx, xCenter, yCenter, angleStep);
      }
      ctx.restore();
   }

   private void drawConcentricRings(double circleSize,
                                    double yCenter,
                                    double yLowerBound,
                                    double yRange,
                                    double range,
                                    double offset) {
      // draw concentric rings
      final var yPolarRingValues = getYPolarRingValues();
      if (yPolarRingValues == null || yPolarRingValues.isEmpty()) {
         ctx.setLineWidth(.5);
         ctx.setStroke(Color.LIGHTGRAY);
         double ringStepSize = size / 20.0;
         double pos = 0.5 * (size - circleSize);
         double ringSize = circleSize;
         for (int i = 0; i < 11; i++) {
            ctx.strokeOval(pos, pos, ringSize, ringSize);
            pos += ringStepSize;
            ringSize -= 2 * ringStepSize;
         }

         // draw min and max Text
         drawLabel(getLowerBoundY(), .5 * size, yCenter - size * 0.018);
         drawLabel(getUpperBoundY(), .5 * size, yCenter - circleSize * 0.48);
      } else {
         yPolarRingValues.forEach(val -> {
            drawCircle(val, yCenter, yLowerBound, yRange, range, offset, 1, Color.LIGHTGRAY);
         });
         drawLabel(getLowerBoundY(), .5 * size, yCenter - size * 0.018);
      }
   }

   private void drawCircle(double y,
                           double yCenter,
                           double yLowerBound,
                           double yRange,
                           double range,
                           double offset,
                           double lineWidth,
                           Color strokeColor) {
      double r = (yCenter - (yCenter - offset - ((y - yLowerBound) / yRange) * range));

      ctx.save();

      // double r = ((y - dataMinY) / dataRange);
      ctx.setLineWidth(lineWidth);
      ctx.setStroke(strokeColor);
      ctx.strokeOval(0.5 * size - r, 0.5 * size - r,
         2 * r, 2 * r);

      drawLabel(y, 0.5 * size, 0.5 * size - r);

      ctx.restore();
   }

   private void drawLabel(double value,
                          double x,
                          double y) {
      Font font = Fonts.latoRegular(0.025 * size);
      String valueText = String.format(Locale.US, "%.0f", value);
      ctx.save();
      ctx.setFont(font);
      Helper.drawTextWithBackground(ctx, valueText, font, Color.WHITE, Color.BLACK,
         x, y);
      ctx.restore();
   }

   private void drawPoints(XYSeries<T> series, Point[] points) {
      if (series.getItems().isEmpty()) {
         return;
      }

      final var firstItem = series.getItems().stream().findFirst().orElseThrow();

      Symbol seriesSymbol = series.getSymbol();
      Paint symbolFill = series.getSymbolFill();
      Paint symbolStroke = series.getSymbolStroke();
      Symbol itemSymbol = firstItem.getSymbol();
      Paint fill = firstItem.getFill();
      Paint stroke = firstItem.getStroke();
      double size = series.getSymbolSize() > -1 ? series.getSymbolSize() : symbolSize;
      for (Point point : points) {
         if (Symbol.NONE == itemSymbol) {
            drawSymbol(point.getX(), point.getY(), symbolFill, symbolStroke, seriesSymbol, size);
         } else {
            drawSymbol(point.getX(), point.getY(), fill, stroke, itemSymbol, size);
         }
      }
   }

   private void drawSymbol(final double X,
                           final double Y,
                           final Paint fill,
                           final Paint stroke,
                           final Symbol symbol,
                           final double symbolSize) {
      double halfSymbolSize = symbolSize * 0.5;
      ctx.save();
      switch (symbol) {
         case NONE:
            break;
         case SQUARE:
            ctx.setStroke(stroke);
            ctx.setFill(fill);
            ctx.fillRect(X - halfSymbolSize, Y - halfSymbolSize, symbolSize, symbolSize);
            ctx.strokeRect(X - halfSymbolSize, Y - halfSymbolSize, symbolSize, symbolSize);
            break;
         case TRIANGLE:
            ctx.setStroke(stroke);
            ctx.setFill(fill);
            ctx.beginPath();
            ctx.moveTo(X, Y - halfSymbolSize);
            ctx.lineTo(X + halfSymbolSize, Y + halfSymbolSize);
            ctx.lineTo(X - halfSymbolSize, Y + halfSymbolSize);
            ctx.lineTo(X, Y - halfSymbolSize);
            ctx.closePath();
            ctx.fill();
            ctx.stroke();
            break;
         case STAR:
            ctx.setStroke(stroke);
            ctx.setFill(null);
            ctx.strokeLine(X - halfSymbolSize, Y, X + halfSymbolSize, Y);
            ctx.strokeLine(X, Y - halfSymbolSize, X, Y + halfSymbolSize);
            ctx.strokeLine(X - halfSymbolSize, Y - halfSymbolSize, X + halfSymbolSize, Y + halfSymbolSize);
            ctx.strokeLine(X + halfSymbolSize, Y - halfSymbolSize, X - halfSymbolSize, Y + halfSymbolSize);
            break;
         case CROSS:
            ctx.setStroke(stroke);
            ctx.setFill(null);
            ctx.strokeLine(X - halfSymbolSize, Y, X + halfSymbolSize, Y);
            ctx.strokeLine(X, Y - halfSymbolSize, X, Y + halfSymbolSize);
            break;
         case CIRCLE:
         default:
            ctx.setStroke(stroke);
            ctx.setFill(fill);
            ctx.fillOval(X - halfSymbolSize, Y - halfSymbolSize, symbolSize, symbolSize);
            ctx.strokeOval(X - halfSymbolSize, Y - halfSymbolSize, symbolSize, symbolSize);
            break;
      }
      ctx.restore();
   }

   // ******************** Resizing ******************************************
   private void resize() {
      width = getWidth() - getInsets().getLeft() - getInsets().getRight();
      height = getHeight() - getInsets().getTop() - getInsets().getBottom();
      size = Math.min(width, height);

      if (keepAspect) {
         if (aspectRatio * width > height) {
            width = 1 / (aspectRatio / height);
         } else if (1 / (aspectRatio / height) > width) {
            height = aspectRatio * width;
         }
      }

      if (width > 0 && height > 0) {
         canvas.setWidth(width);
         canvas.setHeight(height);
         canvas.relocate((getWidth() - width) * 0.5, (getHeight() - height) * 0.5);

         symbolSize = Helper.clamp(MIN_SYMBOL_SIZE, MAX_SYMBOL_SIZE, size * 0.016);

         scaleX = width / getRangeX();
         scaleY = height / getRangeY();

         redraw();
      }
   }
}
