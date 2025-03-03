/*
 * Copyright 2022 Laszlo Balazs-Csiki and Contributors
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
package pixelitor.filters.impl;

import com.jhlabs.image.ImageMath;
import net.jafama.FastMath;
import pixelitor.filters.LittlePlanet;
import pixelitor.utils.Utils;

/**
 * The implementation of the {@link LittlePlanet} filter.
 * This is actually a rectangular -> polar filter with some extra features
 */
public class LittlePlanetFilter extends CenteredTransformFilter {
    private double rotateResult;
    private double zoom;
    private float innerZoom;
    private boolean inverted = false;

    public LittlePlanetFilter() {
        super(LittlePlanet.NAME);
    }

    @Override
    protected void transformInverse(int x, int y, float[] out) {
        double dx = x - cx;
        double dy = cy - y;
        double r = Math.sqrt(dx * dx + dy * dy);

        double radius = srcHeight * zoom / 2;
        double angle = Utils.atan2AngleToIntuitive(FastMath.atan2(dy, dx)) + rotateResult;

        if (angle > 2 * Math.PI) {
            angle -= 2 * Math.PI;
        }
        float nx = (float) (angle * srcWidth / (2 * Math.PI));
        float ratio = (float) (r / radius);
        float correctedRatio = ImageMath.bias(ratio, innerZoom);
        float ny = correctedRatio * srcHeight;
        if (!inverted) {
            ny = srcHeight - ny;
        }

        out[0] = nx;
        out[1] = ny;
    }

    public void setRotateResult(double turn) {
        rotateResult = turn;
    }

    public void setZoom(double zoom) {
        this.zoom = zoom;
    }

    public void setInverted(boolean inverted) {
        this.inverted = inverted;
    }

    public void setInnerZoom(double innerZoom) {
        this.innerZoom = (float) (1 - innerZoom / 2.0);
    }
}
