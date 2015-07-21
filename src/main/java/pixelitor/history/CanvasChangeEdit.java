/*
 * Copyright 2015 Laszlo Balazs-Csiki
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

package pixelitor.history;

import pixelitor.Composition;
import pixelitor.layers.ContentLayer;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

/**
 * A PixelitorEdit that changes the canvas size,
 * such as resize, crop, enlarge layer, or rotation.
 * Always part of a MultiLayerEdit.
 */
public class CanvasChangeEdit extends PixelitorEdit {
    private int backupCanvasWidth;
    private int backupCanvasHeight;

    private TranslationEdit translationEdit;

    /**
     * This constructor must be called before the change
     */
    public CanvasChangeEdit(Composition comp, String name) {
        super(comp, name);
        embedded = true;

        // the translation of the mask should be the same as the
        // translation of the main image
        ContentLayer layer = comp.getAnyContentLayer();
        if (layer != null) { // could be null, if there are only text layers
            translationEdit = new TranslationEdit(comp, layer);
        }

        backupCanvasWidth = comp.getCanvasWidth();
        backupCanvasHeight = comp.getCanvasHeight();
    }

    @Override
    public boolean canUndo() {
        return super.canUndo();
    }

    @Override
    public boolean canRedo() {
        return super.canRedo();
    }

    @Override
    public boolean canRepeat() {
        return false;
    }

    @Override
    public void undo() throws CannotUndoException {
        super.undo();
        swapCanvasDimensions();
        if (translationEdit != null) {
            translationEdit.undo();
        }
    }

    @Override
    public void redo() throws CannotRedoException {
        super.redo();
        swapCanvasDimensions();
        if (translationEdit != null) {
            translationEdit.redo();
        }
    }

    private void swapCanvasDimensions() {
        int tmpCanvasWidth = comp.getCanvasWidth();
        int tmpCanvasHeight = comp.getCanvasHeight();

        comp.getCanvas().updateSize(backupCanvasWidth, backupCanvasHeight);

        backupCanvasWidth = tmpCanvasWidth;
        backupCanvasHeight = tmpCanvasHeight;

        if (!embedded) {
            comp.updateAllIconImages();
            History.notifyMenus(this);
        }
    }

    @Override
    public void die() {
        super.die();

        translationEdit.die();
    }
}