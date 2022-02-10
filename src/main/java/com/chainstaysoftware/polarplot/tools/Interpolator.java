package com.chainstaysoftware.polarplot.tools;

import com.chainstaysoftware.polarplot.data.XYChartItem;

import java.util.LinkedList;
import java.util.List;

public final class Interpolator {
   private Interpolator() {}

   /**
    * Interpolate between points in list so that there are points at 1 degree increments.
    * Assumes input points are in degrees.
    * Wrap indicates if points should be interpolated between the last and first points
    * to loop around.
    */
   public static List<XYChartItem> interpolate(final List<XYChartItem> points,
                                               final boolean wrap) {
      if (points.size() < 2) {
         return points;
      }

      final var list = new LinkedList<XYChartItem>();
      for (int i = 0; i < points.size() - 1; i++) {
         list.addAll(interpolate(points.get(i), points.get(i + 1)));
      }

      if (wrap) {
         final var firstPoint = points.get(0);
         // rewrite the first point to have an angle greater than the end point.
         // Code assumes first point and last point in points are from [0..360]
         final var firstPointDisambiguated = new XYChartItem(firstPoint.getX() + 360, firstPoint.getY(),
            firstPoint.getName(), firstPoint.getFill(), firstPoint.getStroke(),
            firstPoint.getSymbol());
         list.addAll(interpolate(points.get(points.size() - 1), firstPointDisambiguated));
      }

      return list;
   }

   /**
    * Interpolate between points in list so that there are points at 1 degree increments.
    * Assumes input points are in radians.
    * Wrap indicates if points should be interpolated between the last and first points
    * to loop around.
    */
   public static List<XYChartItem> interpolateRadians(final List<XYChartItem> points,
                                                      final boolean wrap) {
      if (points.size() < 2) {
         return points;
      }

      final var list = new LinkedList<XYChartItem>();
      for (int i = 0; i < points.size() - 1; i++) {
         list.addAll(interpolateRadians(points.get(i), points.get(i + 1)));
      }

      if (wrap) {
         final var firstPoint = points.get(0);
         // rewrite the first point to have an angle greater than the end point.
         // Code assumes first point and last point in points are from [0..2pi]
         final var firstPointDisambiguated = new XYChartItem(firstPoint.getX() + (2 * Math.PI), firstPoint.getY(),
            firstPoint.getName(), firstPoint.getFill(), firstPoint.getStroke(),
            firstPoint.getSymbol());
         list.addAll(interpolateRadians(points.get(points.size() - 1), firstPointDisambiguated));
      }

      return list;
   }

   private static List<XYChartItem> interpolate(final XYChartItem p1,
                                                final XYChartItem p2) {
      final var theta1 = p1.getX();
      final var r1 = p1.getY();
      final var theta2 = p2.getX();
      final var r2 = p2.getY();

      final var list = new LinkedList<XYChartItem>();
      list.add(p1);

      final var steps = Math.floor(theta2 - theta1);
      final var stepSize = 1;
      if (theta1 != theta2) {
         for (double i = Math.toRadians(1); i < steps; i = i + stepSize) {
            final var theta = theta1 + i;
            list.add(interpolate(r1, theta1, r2, theta2, theta));
         }
      }

      list.add(p2);
      return list;
   }

   private static List<XYChartItem> interpolateRadians(final XYChartItem p1,
                                                       final XYChartItem p2) {
      final var theta1 = p1.getX();
      final var r1 = p1.getY();
      final var theta2 = p2.getX();
      final var r2 = p2.getY();

      final var list = new LinkedList<XYChartItem>();
      list.add(p1);

      final var steps = theta2 - theta1;
      final var stepSize = Math.toRadians(1);
      if (theta1 != theta2) {
         for (double i = Math.toRadians(1); i < steps; i = i + stepSize) {
            final var theta = theta1 + i;
            list.add(interpolate(r1, theta1, r2, theta2, theta));
         }
      }

      list.add(p2);
      return list;
   }

   private static XYChartItem interpolate(final double r1,
                                   final double theta1,
                                   final double r2,
                                   final double theta2,
                                   final double theta) {
      //r = r1 + { (r2 - r1) * [ (theta - theta1) / (theta2 - theta1) ] }
      final var r = r1 + ((r2 - r1) *
         (theta - theta1) / (theta2 - theta1));
      return new XYChartItem(theta, r);
   }
}
