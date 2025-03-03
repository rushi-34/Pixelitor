/*
 * Copyright 2023 Laszlo Balazs-Csiki and Contributors
 *
 * This file is part of Pixelitor. Pixelitor is free software: you
 * can redistribute it and/or modify it under the terms of the GNU
 * General Public License, version 3 as published by the Free
 * Software Foundation.
 *
 * Pixelitor is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Pixelitor. If not, see <http://www.gnu.org/licenses/>.
 */

package pixelitor.filters;

import pixelitor.GUIMode;
import pixelitor.Views;
import pixelitor.colors.Colors;
import pixelitor.filters.gui.AngleParam;
import pixelitor.filters.gui.ColorParam;
import pixelitor.filters.gui.GroupedRangeParam;
import pixelitor.filters.gui.ImagePositionParam;
import pixelitor.gui.GUIText;
import pixelitor.layers.Drawable;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.Serial;

import static java.awt.RenderingHints.*;
import static pixelitor.colors.Colors.TRANSPARENT_BLACK;
import static pixelitor.filters.gui.ColorParam.TransparencyPolicy.USER_ONLY_TRANSPARENCY;

/**
 * A filter for arbitrary affine transforms.
 */
public class TransformLayer extends ParametrizedFilter {
    @Serial
    private static final long serialVersionUID = 9039647019976237114L;

    public static final String NAME = "Transform Layer";

    private final ImagePositionParam center = new ImagePositionParam("Pivot Point");
    private final AngleParam angleParam = new AngleParam("Rotate Angle", 0);
    private final ColorParam bgColorParam = new ColorParam(GUIText.BG_COLOR, TRANSPARENT_BLACK, USER_ONLY_TRANSPARENCY);
    private final GroupedRangeParam scaleParam = new GroupedRangeParam("Scale (%)", -500, 100, 500);
    private final GroupedRangeParam shearParam = new GroupedRangeParam("Shear", -500, 0, 500, false);

    public TransformLayer() {
        super(true);

        bgColorParam.setPresetKey("Background Color");

        setParams(
            center,
            angleParam,
            scaleParam,
            shearParam,
            bgColorParam
        );
        shearParam.setLinked(false);
    }

    @Override
    public BufferedImage doTransform(BufferedImage src, BufferedImage dest) {
        Graphics2D g = createDestGraphics(dest);
        fillWithBgColor(dest, g);

        AffineTransform transform = calcTransform(src);
        g.drawImage(src, transform, null);

        g.dispose();
        return dest;
    }

    private static Graphics2D createDestGraphics(BufferedImage dest) {
        Graphics2D g = dest.createGraphics();
        g.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON);
        g.setRenderingHint(KEY_INTERPOLATION, VALUE_INTERPOLATION_BICUBIC);
        return g;
    }

    private void fillWithBgColor(BufferedImage dest, Graphics2D g) {
        Colors.fillWith(bgColorParam.getColor(), g, dest.getWidth(), dest.getHeight());
    }

    private AffineTransform calcTransform(BufferedImage src) {
        Point2D centerShift = calcCenterShift(src);
        var transform = calcRotateTransform(centerShift);
        applyScale(transform, centerShift);
        applyShear(transform, centerShift);
        return transform;
    }

    private Point2D calcCenterShift(BufferedImage src) {
        int tx = 0;
        int ty = 0;
        // if this can run as a smart filter, then it shouldn't assume
        // that the active layer is the owner of the image
        if (!GUIMode.enableExperimentalFeatures) {
            Drawable dr = Views.getActiveDrawableOrThrow();
            tx = -dr.getTx();
            ty = -dr.getTy();
        }
        double centerShiftX = (tx + src.getWidth()) * center.getRelativeX();
        double centerShiftY = (ty + src.getHeight()) * center.getRelativeY();
        return new Point2D.Double(centerShiftX, centerShiftY);
    }

    private AffineTransform calcRotateTransform(Point2D centerShift) {
        double theta = angleParam.getValueInRadians();
        return AffineTransform.getRotateInstance(
            theta, centerShift.getX(), centerShift.getY());
    }

    private void applyScale(AffineTransform transform, Point2D centerShift) {
        int scaleX = scaleParam.getValue(0);
        int scaleY = scaleParam.getValue(1);
        if (scaleX != 100 || scaleY != 100) {
            transform.translate(centerShift.getX(), centerShift.getY());
            transform.scale(scaleX / 100.0, scaleY / 100.0);
            transform.translate(-centerShift.getX(), -centerShift.getY());
        }
    }

    private void applyShear(AffineTransform transform, Point2D centerShift) {
        int shearX = shearParam.getValue(0);
        int shearY = shearParam.getValue(1);
        if (shearX != 0 || shearY != 0) {
            transform.translate(centerShift.getX(), centerShift.getY());
            transform.shear(shearX / 100.0, shearY / 100.0);
            transform.translate(-centerShift.getX(), -centerShift.getY());
        }
    }
}