package com.chainstaysoftware.polarplot.tools;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class FormatAngleTest {
   @Test
   void testFormat() {
      Assertions.assertThat(FormatAngle.format(0, false))
         .isEqualTo("0");
      Assertions.assertThat(FormatAngle.format(90.0, false))
         .isEqualTo("90");

      Assertions.assertThat(FormatAngle.format(0, true))
         .isEqualTo("0");
      Assertions.assertThat(FormatAngle.format(Math.toRadians(50), true))
         .isEqualTo("0.87");
      Assertions.assertThat(FormatAngle.format(Math.toRadians(90), true))
         .isEqualTo("\u03C0/2");
      Assertions.assertThat(FormatAngle.format(Math.toRadians(180), true))
         .isEqualTo("\u03C0");
      Assertions.assertThat(FormatAngle.format(Math.toRadians(180), true))
         .isEqualTo("\u03C0");
      Assertions.assertThat(FormatAngle.format(Math.toRadians(330), true))
         .isEqualTo("11\u03C0/6");
   }
}