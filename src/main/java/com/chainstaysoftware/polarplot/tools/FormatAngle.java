/*
 * Copyright 2020 © Denmar Technical Services Inc
 * The U.S. Government has unlimited rights per DFAR 252.227-7014, all other
 * rights reserved.
 *
 * WARNING - This software contains Technical Data whose export is restricted by
 * the Arms Export Control Act (Title 22, U.S.C., Sec 2751, et seq.) or the
 * Export Administration Act of 1979, as amended (Title 50, U.S. C. App. 2401
 * et seq.). Violations of these export laws are subject to severe criminal
 * penalties.
 */
package com.chainstaysoftware.polarplot.tools;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class FormatAngle {
   private static final Double EPSILON = .001;
   private static final String PI = "\u03C0";
   private static final Map<Double, String> radiansTable;
   static {
      radiansTable = new HashMap<>();
      radiansTable.put(0.0, "0");
      radiansTable.put(Math.toRadians(15), PI + "/12");
      radiansTable.put(Math.toRadians(30), PI + "/6");
      radiansTable.put(Math.toRadians(45), PI + "/4");
      radiansTable.put(Math.toRadians(60), PI + "/3");
      radiansTable.put(Math.toRadians(75), "5" + PI + "/12");
      radiansTable.put(Math.toRadians(90), PI + "/2");
      radiansTable.put(Math.toRadians(105), "7" + PI + "/12");
      radiansTable.put(Math.toRadians(120), "2" + PI + "/3");
      radiansTable.put(Math.toRadians(135), "3" + PI + "/4");
      radiansTable.put(Math.toRadians(150), "5" + PI + "/6");
      radiansTable.put(Math.toRadians(165), "11" + PI + "/12");
      radiansTable.put(Math.toRadians(180), PI);
      radiansTable.put(Math.toRadians(195), "13" + PI + "/12");
      radiansTable.put(Math.toRadians(210), "7" + PI + "/6");
      radiansTable.put(Math.toRadians(225), "5" + PI + "/4");
      radiansTable.put(Math.toRadians(240), "4" + PI + "/3");
      radiansTable.put(Math.toRadians(255), "17" + PI + "/12");
      radiansTable.put(Math.toRadians(270), "3" + PI + "/2");
      radiansTable.put(Math.toRadians(285), "19" + PI + "/12");
      radiansTable.put(Math.toRadians(300), "5" + PI + "/3");
      radiansTable.put(Math.toRadians(315), "7" + PI + "/4");
      radiansTable.put(Math.toRadians(330), "11" + PI + "/6");
      radiansTable.put(Math.toRadians(345), "23" + PI + "/12");
      radiansTable.put(Math.toRadians(360), "0");
   }

   private FormatAngle() {}

   public static String format(final double angle,
                               final boolean isRadians) {
      return isRadians
         ? formatRadian(angle)
         : String.format(Locale.US, "%.0f", angle);
   }

   private static String formatRadian(final double angle) {
      return radiansTable.entrySet()
         .stream()
         .filter(entry -> Helper.epsilonEquals(angle, entry.getKey(), EPSILON))
         .findFirst()
         .map(Map.Entry::getValue)
         .orElseGet(() -> String.format(Locale.US, "%.2f", angle));
   }
}
