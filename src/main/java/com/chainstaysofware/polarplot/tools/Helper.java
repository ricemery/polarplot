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

package com.chainstaysofware.polarplot.tools;

import com.chainstaysofware.polarplot.data.XYChartItem;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;


public class Helper {
    public static final int clamp(final int MIN, final int MAX, final int VALUE) {
        if (VALUE < MIN) return MIN;
        if (VALUE > MAX) return MAX;
        return VALUE;
    }
    public static final long clamp(final long MIN, final long MAX, final long VALUE) {
        if (VALUE < MIN) return MIN;
        if (VALUE > MAX) return MAX;
        return VALUE;
    }
    public static final double clamp(final double MIN, final double MAX, final double VALUE) {
        if (Double.compare(VALUE, MIN) < 0) return MIN;
        if (Double.compare(VALUE, MAX) > 0) return MAX;
        return VALUE;
    }
    public static final Instant clamp(final Instant MIN, final Instant MAX, final Instant VALUE) {
        if (VALUE.isBefore(MIN)) return MIN;
        if (VALUE.isAfter(MAX)) return MAX;
        return VALUE;
    }
    public static final LocalDateTime clamp(final LocalDateTime MIN, final LocalDateTime MAX, final LocalDateTime VALUE) {
        if (VALUE.isBefore(MIN)) return MIN;
        if (VALUE.isAfter(MAX)) return MAX;
        return VALUE;
    }
    public static final LocalDate clamp(final LocalDate MIN, final LocalDate MAX, final LocalDate VALUE) {
        if (VALUE.isBefore(MIN)) return MIN;
        if (VALUE.isAfter(MAX)) return MAX;
        return VALUE;
    }

    public static final void rotateCtx(final GraphicsContext CTX, final double X, final double Y, final double ANGLE) {
        CTX.translate(X, Y);
        CTX.rotate(ANGLE);
        CTX.translate(-X, -Y);
    }

    public static final void saveAsPng(final Node NODE, final String FILE_NAME) {
        final WritableImage SNAPSHOT = NODE.snapshot(new SnapshotParameters(), null);
        final String        NAME     = FILE_NAME.replace("\\.[a-zA-Z]{3,4}", "");
        final File          FILE     = new File(NAME + ".png");

        try {
            ImageIO.write(SwingFXUtils.fromFXImage(SNAPSHOT, null), "png", FILE);
        } catch (IOException exception) {
            // handle exception here
        }
    }

    public static final Point[] subdividePointsRadial(final Point[] POINTS, final int SUB_DIVISIONS){
        assert POINTS != null;
        assert POINTS.length >= 3;
        int    noOfPoints = POINTS.length;

        Point[] subdividedPoints = new Point[((noOfPoints - 1) * SUB_DIVISIONS) + 1];

        double increments = 1.0 / (double) SUB_DIVISIONS;

        for (int i = 0 ; i < noOfPoints - 1 ; i++) {
            Point p0 = i == 0 ? POINTS[noOfPoints - 2] : POINTS[i - 1];
            Point p1 = POINTS[i];
            Point p2 = POINTS[i + 1];
            Point p3 = (i == (noOfPoints - 2)) ? POINTS[1] : POINTS[i + 2];

            CatmullRom<Point> crs = new CatmullRom<>(p0, p1, p2, p3);

            for (int j = 0 ; j <= SUB_DIVISIONS ; j++) {
                subdividedPoints[(i * SUB_DIVISIONS) + j] = crs.q(j * increments);
            }
        }

        return subdividedPoints;
    }

    public static final Point[] subdividePoints(final Point[] POINTS, final int SUB_DIVISIONS) {
        assert POINTS != null;
        assert POINTS.length >= 3;
        int    noOfPoints = POINTS.length;

        Point[] subdividedPoints = new Point[((noOfPoints - 1) * SUB_DIVISIONS) + 1];

        double increments = 1.0 / (double) SUB_DIVISIONS;

        for (int i = 0 ; i < noOfPoints - 1 ; i++) {
            Point p0 = i == 0 ? POINTS[i] : POINTS[i - 1];
            Point p1 = POINTS[i];
            Point p2 = POINTS[i + 1];
            Point p3 = (i+2 == noOfPoints) ? POINTS[i + 1] : POINTS[i + 2];

            CatmullRom<Point> crs = new CatmullRom<>(p0, p1, p2, p3);

            for (int j = 0 ; j <= SUB_DIVISIONS ; j++) {
                subdividedPoints[(i * SUB_DIVISIONS) + j] = crs.q(j * increments);
            }
        }

        return subdividedPoints;
    }

    public static final <T> Predicate<T> not(Predicate<T> predicate) { return predicate.negate(); }

    public static final void orderXYChartItemsByX(final List<XYChartItem> ITEMS, final Order ORDER) {
        if (Order.ASCENDING == ORDER) {
            Collections.sort(ITEMS, Comparator.comparingDouble(XYChartItem::getX));
        } else {
            Collections.sort(ITEMS, Comparator.comparingDouble(XYChartItem::getX).reversed());
        }
    }

    public static final CtxDimension getTextDimension(final String TEXT, final Font FONT) {
        Text text = new Text(TEXT);
        text.setFont(FONT);
        double textWidth  = text.getBoundsInLocal().getWidth();
        double textHeight = text.getBoundsInLocal().getHeight();
        text = null;
        CtxDimension dim = new CtxDimension(textWidth, textHeight);
        return dim;
    }

    public static final void drawTextWithBackground(final GraphicsContext CTX,
                                                    final String TEXT,
                                                    final Font FONT,
                                                    final Color TEXT_BACKGROUND,
                                                    final Color TEXT_FILL,
                                                    final double X,
                                                    final double Y) {
        CtxDimension dim = getTextDimension(TEXT, FONT);
        double textWidth  = dim.getWidth() * 1.2;
        double textHeight = dim.getHeight();
        CTX.save();
        CTX.setFont(FONT);
        CTX.setTextBaseline(VPos.CENTER);
        CTX.setTextAlign(TextAlignment.CENTER);
        CTX.setFill(TEXT_BACKGROUND);
        CTX.fillRect(X - textWidth * 0.5, Y - textHeight * 0.5, textWidth, textHeight);
        CTX.setFill(TEXT_FILL);
        CTX.fillText(TEXT, X, Y);
        CTX.restore();
    }
}
