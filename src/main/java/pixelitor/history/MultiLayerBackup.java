package pixelitor.history;

import pixelitor.Composition;
import pixelitor.layers.ImageLayer;
import pixelitor.layers.Layer;
import pixelitor.selection.IgnoreSelection;
import pixelitor.selection.Selection;

import java.awt.Shape;
import java.awt.image.BufferedImage;

/**
 * Encapsulates the state needed by a MultiLayerEdit
 */
public class MultiLayerBackup {
    private final Composition comp;
    private final String editName;
    private ImageLayer layer;
    private CanvasChangeEdit canvasChangeEdit;
    private Shape backupShape;

    // Saved before the change, but the edit is
    // created after the change.
    // This way no image copy is necessary.
    private BufferedImage backupImage;
    private BufferedImage backupMaskImage;

    /**
     * This object needs to be created before the translations,
     * canvas changes or selection changes take place
     */
    public MultiLayerBackup(Composition comp, String editName, boolean changesCanvasDimensions) {
        this.comp = comp;
        this.editName = editName;

        if (changesCanvasDimensions) {
            canvasChangeEdit = new CanvasChangeEdit(comp, editName);
        }

        if (comp.hasSelection()) {
            Selection selection = comp.getSelectionOrNull();
            backupShape = selection.getShape();
        }

        int nrLayers = comp.getNrLayers();
        for (int i = 0; i < nrLayers; i++) {
            Layer compLayer = comp.getLayer(i);
            if (compLayer instanceof ImageLayer) {
                ImageLayer imageLayer = (ImageLayer) compLayer;
                this.layer = imageLayer;
                backupImage = imageLayer.getImage();
                if (layer.hasMask()) {
                    backupMaskImage = layer.getMask().getImage();
                }
                break;
            }
        }
    }

    public CanvasChangeEdit getCanvasChangeEdit() {
        return canvasChangeEdit;
    }

    public boolean hasSavedSelection() {
        return backupShape != null;
    }

    public ImageEdit createImageEdit() {
        assert backupImage != null;
        ImageEdit edit;
        if (backupMaskImage != null) {
            edit = new ImageAndMaskEdit(comp, editName, layer,
                    backupImage, backupMaskImage, false);
        } else {
            edit = new ImageEdit(comp, editName, layer,
                    backupImage, IgnoreSelection.YES, false);
        }
        edit.setEmbedded(true);
        return edit;
    }

    public SelectionChangeEdit createSelectionChangeEdit() {
        assert backupShape != null;
        SelectionChangeEdit edit = new SelectionChangeEdit(comp, backupShape, editName);
        edit.setEmbedded(true);
        return edit;
    }

    public DeselectEdit createDeselectEdit() {
        assert backupShape != null;
        DeselectEdit edit = new DeselectEdit(comp, backupShape, editName);
        edit.setEmbedded(true);
        return edit;
    }
}
