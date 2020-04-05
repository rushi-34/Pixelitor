/*
 * Copyright 2020 Laszlo Balazs-Csiki and Contributors
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

package pixelitor.menus;

import com.bric.util.JVM;
import pixelitor.Build;
import pixelitor.NewImage;
import pixelitor.OpenImages;
import pixelitor.Pixelitor;
import pixelitor.TipsOfTheDay;
import pixelitor.automate.AutoPaint;
import pixelitor.automate.BatchFilterWizard;
import pixelitor.automate.BatchResize;
import pixelitor.colors.palette.ColorSwatchClickHandler;
import pixelitor.colors.palette.FullPalette;
import pixelitor.colors.palette.PalettePanel;
import pixelitor.compactions.EnlargeCanvas;
import pixelitor.compactions.Flip;
import pixelitor.compactions.ResizePanel;
import pixelitor.compactions.Rotate;
import pixelitor.filters.*;
import pixelitor.filters.animation.TweenWizard;
import pixelitor.filters.convolve.Convolve;
import pixelitor.filters.curves.ToneCurvesFilter;
import pixelitor.filters.jhlabsproxies.*;
import pixelitor.filters.levels.Levels;
import pixelitor.filters.lookup.ColorBalance;
import pixelitor.filters.lookup.Luminosity;
import pixelitor.filters.painters.TextFilter;
import pixelitor.gui.GlobalEvents;
import pixelitor.gui.HistogramsPanel;
import pixelitor.gui.ImageArea;
import pixelitor.gui.Navigator;
import pixelitor.gui.PixelitorWindow;
import pixelitor.gui.PreferencesPanel;
import pixelitor.gui.View;
import pixelitor.gui.WorkSpace;
import pixelitor.gui.utils.Dialogs;
import pixelitor.gui.utils.GUIUtils;
import pixelitor.gui.utils.Themes;
import pixelitor.guides.Guides;
import pixelitor.history.History;
import pixelitor.io.FileChoosers;
import pixelitor.io.OpenSave;
import pixelitor.io.OptimizedJpegSavePanel;
import pixelitor.layers.AddAdjLayerAction;
import pixelitor.layers.AddNewLayerAction;
import pixelitor.layers.ContentLayer;
import pixelitor.layers.DeleteActiveLayerAction;
import pixelitor.layers.Drawable;
import pixelitor.layers.DuplicateLayerAction;
import pixelitor.layers.ImageLayer;
import pixelitor.layers.Layer;
import pixelitor.layers.LayerMask;
import pixelitor.layers.LayerMoveAction;
import pixelitor.layers.MaskFromColorRangePanel;
import pixelitor.layers.MaskViewMode;
import pixelitor.layers.TextLayer;
import pixelitor.menus.edit.CopyAction;
import pixelitor.menus.edit.CopySource;
import pixelitor.menus.edit.FadeMenuItem;
import pixelitor.menus.edit.PasteAction;
import pixelitor.menus.edit.PasteDestination;
import pixelitor.menus.file.AnimGifExport;
import pixelitor.menus.file.MetaDataPanel;
import pixelitor.menus.file.OpenRasterExportPanel;
import pixelitor.menus.file.RecentFilesMenu;
import pixelitor.menus.file.ScreenCaptureAction;
import pixelitor.menus.help.AboutDialog;
import pixelitor.menus.help.UpdatesCheck;
import pixelitor.menus.view.ShowHideAllAction;
import pixelitor.menus.view.ShowHideHistogramsAction;
import pixelitor.menus.view.ShowHideLayersAction;
import pixelitor.menus.view.ShowHideStatusBarAction;
import pixelitor.menus.view.ShowHideToolsAction;
import pixelitor.menus.view.ZoomMenu;
import pixelitor.selection.SelectionActions;
import pixelitor.tools.brushes.CopyBrush;
import pixelitor.utils.FilterCreator;
import pixelitor.utils.Messages;
import pixelitor.utils.OpenInBrowserAction;
import pixelitor.utils.Tests3x3;
import pixelitor.utils.Texts;
import pixelitor.utils.debug.AppNode;
import pixelitor.utils.test.Events;
import pixelitor.utils.test.RandomGUITest;
import pixelitor.utils.test.SplashImageCreator;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.lang.management.ManagementFactory;
import java.util.ResourceBundle;

import static java.awt.BorderLayout.CENTER;
import static java.awt.BorderLayout.NORTH;
import static java.lang.String.format;
import static javax.swing.BorderFactory.createEmptyBorder;
import static pixelitor.Composition.ImageChangeActions.FULL;
import static pixelitor.OpenImages.duplicateActive;
import static pixelitor.OpenImages.getActiveComp;
import static pixelitor.OpenImages.getActiveCompositeImage;
import static pixelitor.OpenImages.getActiveLayer;
import static pixelitor.OpenImages.getActiveView;
import static pixelitor.OpenImages.onActiveDrawable;
import static pixelitor.OpenImages.onActiveImageLayer;
import static pixelitor.OpenImages.onActiveTextLayer;
import static pixelitor.OpenImages.reloadActiveFromFileAsync;
import static pixelitor.OpenImages.repaintActive;
import static pixelitor.colors.FillType.BACKGROUND;
import static pixelitor.colors.FillType.FOREGROUND;
import static pixelitor.colors.FillType.TRANSPARENT;
import static pixelitor.compactions.Flip.Direction.HORIZONTAL;
import static pixelitor.compactions.Flip.Direction.VERTICAL;
import static pixelitor.compactions.Rotate.SpecialAngle.ANGLE_180;
import static pixelitor.compactions.Rotate.SpecialAngle.ANGLE_270;
import static pixelitor.compactions.Rotate.SpecialAngle.ANGLE_90;
import static pixelitor.filters.jhlabsproxies.JHMotionBlur.Mode.MOTION_BLUR;
import static pixelitor.filters.jhlabsproxies.JHMotionBlur.Mode.SPIN_ZOOM_BLUR;
import static pixelitor.gui.ImageArea.Mode.FRAMES;
import static pixelitor.layers.LayerMaskAddType.FROM_LAYER;
import static pixelitor.layers.LayerMaskAddType.FROM_TRANSPARENCY;
import static pixelitor.layers.LayerMaskAddType.HIDE_ALL;
import static pixelitor.layers.LayerMaskAddType.REVEAL_ALL;
import static pixelitor.layers.LayerMaskAddType.REVEAL_SELECTION;
import static pixelitor.menus.EnabledIf.ACTION_ENABLED;
import static pixelitor.menus.EnabledIf.CAN_REPEAT;
import static pixelitor.menus.EnabledIf.REDO_POSSIBLE;
import static pixelitor.menus.EnabledIf.UNDO_POSSIBLE;
import static pixelitor.menus.MenuAction.AllowedLayerType.HAS_LAYER_MASK;
import static pixelitor.menus.MenuAction.AllowedLayerType.IS_TEXT_LAYER;
import static pixelitor.utils.Keys.*;
import static pixelitor.utils.Utils.debugImage;
import static pixelitor.utils.Utils.getJavaMainVersion;

/**
 * The menu bar of the app
 */
public class MenuBar extends JMenuBar {

    public MenuBar(PixelitorWindow pw) {
        ResourceBundle texts = Texts.getResources();

        add(createFileMenu(pw));
        add(createEditMenu(texts));
        add(createLayerMenu(pw));
        add(createSelectMenu());
        add(createImageMenu());
        add(createColorMenu());
        add(createFilterMenu());
        add(createViewMenu(pw));

        if (Build.isDevelopment()) {
            add(createDevelopMenu(pw));
        }

        add(createHelpMenu(pw));
    }

    private static JMenu createFileMenu(PixelitorWindow pw) {
        PMenu fileMenu = new PMenu("File", 'F');

        // new image
        fileMenu.buildAction(NewImage.getAction())
                .alwaysEnabled()
                .withKey(CTRL_N)
                .add();

        fileMenu.buildAction(new MenuAction("Open...") {
            @Override
            public void onClick() {
                FileChoosers.openAsync();
            }
        }).alwaysEnabled().withKey(CTRL_O).add();

        // recent files
        JMenu recentFiles = RecentFilesMenu.getInstance();
        fileMenu.add(recentFiles);

        fileMenu.addSeparator();

        fileMenu.addActionWithKey(new MenuAction("Save") {
            @Override
            public void onClick() {
                OpenSave.save(false);
            }
        }, CTRL_S);

        fileMenu.addActionWithKey(new MenuAction("Save As...") {
            @Override
            public void onClick() {
                OpenSave.save(true);
            }
        }, CTRL_SHIFT_S);

        fileMenu.addAction(new MenuAction("Export Optimized JPEG...") {
            @Override
            public void onClick() {
                BufferedImage image = getActiveCompositeImage();
                OptimizedJpegSavePanel.showInDialog(image, pw);
            }
        });

        fileMenu.addAction(new MenuAction("Export OpenRaster...") {
            @Override
            public void onClick() {
                OpenRasterExportPanel.showInDialog(pw);
            }
        });

        fileMenu.addSeparator();

        fileMenu.addAction(new MenuAction("Export Layer Animation...") {
            @Override
            public void onClick() {
                AnimGifExport.start(pw);
            }
        });

        fileMenu.addAction(new DrawableAction("Export Tweening Animation") {
            @Override
            protected void process(Drawable dr) {
                new TweenWizard(dr).start(pw);
            }
        });

        fileMenu.addSeparator();

        // reload
        fileMenu.addActionWithKey(new MenuAction("Reload") {
            @Override
            public void onClick() {
                reloadActiveFromFileAsync();
            }
        }, F12);

        fileMenu.addAction(new MenuAction("Show Metadata...") {
            @Override
            public void onClick() {
                MetaDataPanel.showInDialog(pw);
            }
        });

        fileMenu.addSeparator();

        // close
        fileMenu.addActionWithKey(OpenImages.CLOSE_ACTIVE_ACTION, CTRL_W);

        // close all
        fileMenu.addActionWithKey(OpenImages.CLOSE_ALL_ACTION, CTRL_ALT_W);

        fileMenu.addSeparator();

        fileMenu.add(createAutomateSubmenu(pw));

        if (!JVM.isMac) {
            fileMenu.addAlwaysEnabledAction(new ScreenCaptureAction());
        }

        fileMenu.addSeparator();

        // exit
        String exitName = JVM.isMac ? "Quit" : "Exit";
        fileMenu.addAlwaysEnabledAction(new MenuAction(exitName) {
            @Override
            public void onClick() {
                Pixelitor.exitApp(pw);
            }
        });

        return fileMenu;
    }

    private static JMenu createAutomateSubmenu(PixelitorWindow pw) {
        PMenu sub = new PMenu("Automate");

        sub.addAlwaysEnabledAction(new MenuAction("Batch Resize...") {
            @Override
            public void onClick() {
                BatchResize.start();
            }
        });

        sub.buildAction(new DrawableAction("Batch Filter") {
            @Override
            protected void process(Drawable dr) {
                new BatchFilterWizard(dr).start(pw);
            }
        }).add();

        // formats other than PNG are not supported in order
        // to avoid problems with translucency
        sub.addAction(new MenuAction("Export Layers to PNG...") {
            @Override
            public void onClick() {
                OpenSave.exportLayersToPNGAsync();
            }
        });

        sub.addAction(new DrawableAction("Auto Paint") {
            @Override
            protected void process(Drawable dr) {
                AutoPaint.showDialog(dr);
            }
        });

        return sub;
    }

    private static JMenu createEditMenu(ResourceBundle texts) {
        String editMenuName = texts.getString("edit");
        PMenu editMenu = new PMenu(editMenuName, 'E');

        // last op
        editMenu.buildAction(RepeatLast.INSTANCE)
                .enableIf(CAN_REPEAT)
                .withKey(CTRL_F)
                .add();

        editMenu.addSeparator();

        // undo
        editMenu.buildAction(History.UNDO_ACTION)
                .enableIf(UNDO_POSSIBLE)
                .withKey(CTRL_Z)
                .add();

        // redo
        editMenu.buildAction(History.REDO_ACTION)
                .enableIf(REDO_POSSIBLE)
                .withKey(CTRL_SHIFT_Z)
                .add();

        // fade
        FadeMenuItem.INSTANCE.setAccelerator(CTRL_SHIFT_F);
        editMenu.add(FadeMenuItem.INSTANCE);

        editMenu.addSeparator();

        // copy
        var copyLayerOrMask = new CopyAction(CopySource.LAYER_OR_MASK);
        editMenu.addActionWithKey(copyLayerOrMask, CTRL_C);

        var copyComposite = new CopyAction(CopySource.COMPOSITE);
        editMenu.addActionWithKey(copyComposite, CTRL_SHIFT_C);

        // paste
        var pasteAsNewImage = new PasteAction(PasteDestination.NEW_IMAGE);
        editMenu.buildAction(pasteAsNewImage)
                .alwaysEnabled().withKey(CTRL_V).add();

        var pasteAsNewLayer = new PasteAction(PasteDestination.NEW_LAYER);
        editMenu.addActionWithKey(pasteAsNewLayer, CTRL_SHIFT_V);

        var pasteAsMask = new PasteAction(PasteDestination.MASK);
        editMenu.addActionWithKey(pasteAsMask, CTRL_ALT_V);

        editMenu.addSeparator();

        // preferences
        String prefsMenuName = texts.getString("preferences");
        Action preferencesAction = new MenuAction(prefsMenuName) {
            @Override
            public void onClick() {
                PreferencesPanel.showInDialog();
            }
        };
        editMenu.add(preferencesAction);

        return editMenu;
    }

    private static JMenu createLayerMenu(PixelitorWindow pw) {
        PMenu layersMenu = new PMenu("Layer", 'L');

        layersMenu.add(AddNewLayerAction.INSTANCE);
        layersMenu.add(DeleteActiveLayerAction.INSTANCE);
        layersMenu.addActionWithKey(DuplicateLayerAction.INSTANCE, CTRL_J);

        layersMenu.addSeparator();

        layersMenu.addActionWithKey(new MenuAction("Merge Down") {
            @Override
            public void onClick() {
                getActiveComp().mergeActiveLayerDown(true);
            }
        }, CTRL_E);

        layersMenu.addAction(new MenuAction("Flatten Image") {
            @Override
            public void onClick() {
                getActiveComp().flattenImage(true, true);
            }
        });

        layersMenu.addActionWithKey(new MenuAction("New Layer from Composite") {
            @Override
            public void onClick() {
                getActiveComp().addNewLayerFromComposite();
            }
        }, CTRL_SHIFT_ALT_E);

        layersMenu.add(createLayerStackSubmenu());
        layersMenu.add(createLayerMaskSubmenu());
        layersMenu.add(createTextLayerSubmenu(pw));

        if (Build.enableAdjLayers) {
            layersMenu.add(createAdjustmentLayersSubmenu());
        }

        return layersMenu;
    }

    private static JMenu createLayerStackSubmenu() {
        PMenu sub = new PMenu("Layer Stack");

        sub.addActionWithKey(LayerMoveAction.INSTANCE_UP, CTRL_ALT_R);

        sub.addActionWithKey(LayerMoveAction.INSTANCE_DOWN, CTRL_ALT_L);

        sub.addActionWithKey(new MenuAction(LayerMoveAction.LAYER_TO_TOP) {
            @Override
            public void onClick() {
                getActiveComp().moveActiveLayerToTop();
            }
        }, CTRL_SHIFT_ALT_R);

        sub.addActionWithKey(new MenuAction(LayerMoveAction.LAYER_TO_BOTTOM) {
            @Override
            public void onClick() {
                getActiveComp().moveActiveLayerToBottom();
            }
        }, CTRL_SHIFT_ALT_L);

        sub.addSeparator();

        sub.addActionWithKey(new MenuAction(LayerMoveAction.RAISE_LAYER_SELECTION) {
            @Override
            public void onClick() {
                var comp = getActiveComp();
                comp.moveLayerSelectionUp();
            }
        }, CTRL_SHIFT_R);

        sub.addActionWithKey(new MenuAction(LayerMoveAction.LOWER_LAYER_SELECTION) {
            @Override
            public void onClick() {
                getActiveComp().moveLayerSelectionDown();
            }
        }, CTRL_SHIFT_L);

        return sub;
    }

    private static JMenu createLayerMaskSubmenu() {
        PMenu sub = new PMenu("Layer Mask");

        sub.addAction(new MenuAction("Add White (Reveal All)") {
            @Override
            public void onClick() {
                getActiveLayer().addMask(REVEAL_ALL);
            }
        });

        sub.addAction(new MenuAction("Add Black (Hide All)") {
            @Override
            public void onClick() {
                getActiveLayer().addMask(HIDE_ALL);
            }
        });

        sub.addAction(new MenuAction("Add from Selection") {
            @Override
            public void onClick() {
                getActiveLayer().addMask(REVEAL_SELECTION);
            }
        });

        sub.addAction(new MenuAction("Add from Transparency") {
            @Override
            public void onClick() {
                getActiveLayer().addMask(FROM_TRANSPARENCY);
            }
        });

        sub.addAction(new MenuAction("Add from Layer") {
            @Override
            public void onClick() {
                getActiveLayer().addMask(FROM_LAYER);
            }
        });

        sub.addAction(new MenuAction("Add/Replace from Color Range...") {
            @Override
            public void onClick() {
                MaskFromColorRangePanel.showInDialog();
            }
        });

        sub.addSeparator();

        sub.addAction(new MenuAction("Delete", HAS_LAYER_MASK) {
            @Override
            public void onClick() {
                OpenImages.onActiveLayer(layer ->
                        layer.deleteMask(true));
            }
        });

        sub.addAction(new MenuAction("Apply", HAS_LAYER_MASK) {
            @Override
            public void onClick() {
                Layer layer = getActiveLayer();

                if (!(layer instanceof ImageLayer)) {
                    Messages.showNotImageLayerError();
                    return;
                }

                ((ImageLayer) layer).applyLayerMask(true);

                // not necessary, as the result looks the same, but still
                // useful because eventual problems would be spotted early
                layer.getComp().imageChanged();
            }
        });

        sub.addSeparator();

        MaskViewMode.NORMAL.addToMainMenu(sub);
        MaskViewMode.SHOW_MASK.addToMainMenu(sub);
        MaskViewMode.EDIT_MASK.addToMainMenu(sub);
        MaskViewMode.RUBYLITH.addToMainMenu(sub);

        return sub;
    }

    private static JMenu createTextLayerSubmenu(PixelitorWindow pw) {
        PMenu sub = new PMenu("Text Layer");

        sub.addActionWithKey(new MenuAction("New...") {
            @Override
            public void onClick() {
                TextLayer.createNew(pw);
            }
        }, T);

        sub.addActionWithKey(new MenuAction("Edit...", IS_TEXT_LAYER) {
            @Override
            public void onClick() {
                onActiveTextLayer(textLayer -> textLayer.edit(pw));
            }
        }, CTRL_T);

        sub.addAction(new MenuAction("Rasterize", IS_TEXT_LAYER) {
            @Override
            public void onClick() {
                onActiveTextLayer(TextLayer::replaceWithRasterized);
            }
        });

        sub.addAction(new MenuAction("Selection from Text", IS_TEXT_LAYER) {
            @Override
            public void onClick() {
                getActiveComp().createSelectionFromTextLayer();
            }
        });

        return sub;
    }

    private static JMenu createAdjustmentLayersSubmenu() {
        PMenu sub = new PMenu("New Adjustment Layer");

        // not called "Invert" because of assertj test lookup confusion
        sub.addAction(new MenuAction("Invert Adjustment") {
            @Override
            public void onClick() {
                AddAdjLayerAction.INSTANCE.actionPerformed(null);
            }
        });

        return sub;
    }

    private static JMenu createSelectMenu() {
        PMenu selectMenu = new PMenu("Select", 'S');

        selectMenu.buildAction(SelectionActions.getDeselect())
                .enableIf(ACTION_ENABLED)
                .withKey(CTRL_D)
                .add();
        selectMenu.buildAction(SelectionActions.getShowHide())
                .enableIf(ACTION_ENABLED)
                .withKey(CTRL_H)
                .add();

        selectMenu.addSeparator();

        selectMenu.buildAction(SelectionActions.getInvert())
                .enableIf(ACTION_ENABLED)
                .withKey(CTRL_SHIFT_I)
                .add();

        selectMenu.addSelfControlledAction(SelectionActions.getModify());

        selectMenu.addSeparator();

        selectMenu.addSelfControlledAction(SelectionActions.getCopy());
        selectMenu.addSelfControlledAction(SelectionActions.getPaste());

        return selectMenu;
    }

    private static JMenu createImageMenu() {
        PMenu imageMenu = new PMenu("Image", 'I');

        // crop
        imageMenu.buildAction(SelectionActions.getCrop())
                .enableIf(ACTION_ENABLED).add();

        // resize
        imageMenu.addActionWithKey(new MenuAction("Resize...") {
            @Override
            public void onClick() {
                ResizePanel.resizeActiveImage();
            }
        }, CTRL_ALT_I);

        imageMenu.addAction(new MenuAction("Duplicate") {
            @Override
            public void onClick() {
                duplicateActive();
            }
        });

        imageMenu.addSeparator();

        imageMenu.addAction(EnlargeCanvas.getAction());

        imageMenu.addAction(new MenuAction("Layer to Canvas Size") {
            @Override
            public void onClick() {
                getActiveComp().activeLayerToCanvasSize();
            }
        });

        imageMenu.addSeparator();

        // rotate
        imageMenu.addAction(new Rotate(ANGLE_90));
        imageMenu.addAction(new Rotate(ANGLE_180));
        imageMenu.addAction(new Rotate(ANGLE_270));

        imageMenu.addSeparator();

        // flip
        imageMenu.addAction(new Flip(HORIZONTAL));
        imageMenu.addAction(new Flip(VERTICAL));

        return imageMenu;
    }

    private static JMenu createColorMenu() {
        PMenu colorsMenu = new PMenu("Color", 'C');

        colorsMenu.buildFilter("Color Balance", ColorBalance::new)
                .withKey(CTRL_B)
                .add();
        colorsMenu.buildFilter(HueSat.NAME, HueSat::new)
                .withKey(CTRL_U)
                .add();
        colorsMenu.buildFilter(Colorize.NAME, Colorize::new)
                .add();
        colorsMenu.buildFilter("Levels", Levels::new)
                .withKey(CTRL_L)
                .add();
        colorsMenu.buildFilter(ToneCurvesFilter.NAME, ToneCurvesFilter::new)
                .withKey(CTRL_M)
                .add();
        colorsMenu.buildFilter(BrightnessContrast.NAME, BrightnessContrast::new)
                .add();
        colorsMenu.buildFilter(Solarize.NAME, Solarize::new)
                .add();
        colorsMenu.buildFilter(Sepia.NAME, Sepia::new)
                .add();
        colorsMenu.buildFilter("Invert", Invert::new)
                .noGUI()
                .withKey(CTRL_I)
                .add();
        colorsMenu.buildFilter(ChannelInvert.NAME, ChannelInvert::new)
                .add();
        colorsMenu.buildFilter(ChannelMixer.NAME, ChannelMixer::new)
                .add();

        colorsMenu.add(createExtractChannelsSubmenu());
        colorsMenu.add(createReduceColorsSubmenu());
        colorsMenu.add(createFillSubmenu());

        return colorsMenu;
    }

    private static JMenu createExtractChannelsSubmenu() {
        PMenu sub = new PMenu("Extract Channels");

        sub.addFilter("Extract Channel", ExtractChannel::new);

        sub.addSeparator();

        sub.buildFilter(Luminosity.NAME, Luminosity::new)
                .noGUI()
                .extract()
                .add();

        sub.addFilter(ExtractChannelFilter.getValueChannelFA());
        sub.addFilter(ExtractChannelFilter.getDesaturateChannelFA());

        sub.addSeparator();

        sub.addFilter(ExtractChannelFilter.getHueChannelFA());
        sub.addFilter(ExtractChannelFilter.getHueInColorsChannelFA());
        sub.addFilter(ExtractChannelFilter.getSaturationChannelFA());

        return sub;
    }

    private static JMenu createReduceColorsSubmenu() {
        PMenu sub = new PMenu("Reduce Colors");

        sub.addFilter(JHQuantize.NAME, JHQuantize::new);
        sub.addFilter(Posterize.NAME, Posterize::new);
        sub.addFilter(Threshold.NAME, Threshold::new);
        sub.addFilter(ColorThreshold.NAME, ColorThreshold::new);

        sub.addSeparator();

        sub.addFilter(JHTriTone.NAME, JHTriTone::new);
        sub.addFilter(GradientMap.NAME, GradientMap::new);

        sub.addSeparator();

        sub.addFilter(JHDither.NAME, JHDither::new);

        return sub;
    }

    private static JMenu createFillSubmenu() {
        PMenu sub = new PMenu("Fill with");

        sub.buildFilter(FOREGROUND.asFillFilterAction())
                .withKey(ALT_BACKSPACE)
                .add();
        sub.buildFilter(BACKGROUND.asFillFilterAction())
                .withKey(CTRL_BACKSPACE)
                .add();
        sub.addFilter(TRANSPARENT.asFillFilterAction());

        sub.buildFilter(ColorWheel.NAME, ColorWheel::new)
                .withFillListName()
                .add();
        sub.buildFilter(JHFourColorGradient.NAME, JHFourColorGradient::new)
                .withFillListName().add();

        return sub;
    }

    private static JMenu createFilterMenu() {
        PMenu filterMenu = new PMenu("Filter", 'T');

        filterMenu.add(createBlurSharpenSubmenu());
        filterMenu.add(createDistortSubmenu());
        filterMenu.add(createDislocateSubmenu());
        filterMenu.add(createLightSubmenu());
        filterMenu.add(createNoiseSubmenu());
        filterMenu.add(createRenderSubmenu());
        filterMenu.add(createArtisticSubmenu());
        filterMenu.add(createFindEdgesSubmenu());
        filterMenu.add(createOtherSubmenu());

        // the text as filter is still useful for batch operations
        filterMenu.buildFilter(TextFilter.createFilterAction()).add();

        return filterMenu;
    }

    private static JMenu createBlurSharpenSubmenu() {
        PMenu sub = new PMenu("Blur/Sharpen");

        sub.addFilter(JHBoxBlur.NAME, JHBoxBlur::new);
        sub.addFilter(JHFocus.NAME, JHFocus::new);
        sub.addFilter(JHGaussianBlur.NAME, JHGaussianBlur::new);
        sub.addFilter(JHLensBlur.NAME, JHLensBlur::new);
        sub.addFilter(MOTION_BLUR.createFilterAction());
        sub.addFilter(JHSmartBlur.NAME, JHSmartBlur::new);
        sub.addFilter(SPIN_ZOOM_BLUR.createFilterAction());
        sub.addSeparator();
        sub.addFilter(JHUnsharpMask.NAME, JHUnsharpMask::new);

        return sub;
    }

    private static JMenu createDistortSubmenu() {
        PMenu sub = new PMenu("Distort");

        sub.addFilter(JHSwirlPinchBulge.NAME, JHSwirlPinchBulge::new);
        sub.addFilter(CircleToSquare.NAME, CircleToSquare::new);
        sub.addFilter(JHPerspective.NAME, JHPerspective::new);

        sub.addSeparator();

        sub.addFilter(JHLensOverImage.NAME, JHLensOverImage::new);
        sub.addFilter(Magnify.NAME, Magnify::new);

        sub.addSeparator();

        sub.addFilter(JHTurbulentDistortion.NAME, JHTurbulentDistortion::new);
        sub.addFilter(JHUnderWater.NAME, JHUnderWater::new);
        sub.addFilter(JHWaterRipple.NAME, JHWaterRipple::new);
        sub.addFilter(JHWaves.NAME, JHWaves::new);
        sub.addFilter(AngularWaves.NAME, AngularWaves::new);
        sub.addFilter(RadialWaves.NAME, RadialWaves::new);

        sub.addSeparator();

        sub.addFilter(GlassTiles.NAME, GlassTiles::new);
        sub.addFilter(PolarTiles.NAME, PolarTiles::new);
        sub.addFilter(JHFrostedGlass.NAME, JHFrostedGlass::new);

        sub.addSeparator();

        sub.addFilter(LittlePlanet.NAME, LittlePlanet::new);
        sub.addFilter(JHPolarCoordinates.NAME, JHPolarCoordinates::new);
        sub.addFilter(JHWrapAroundArc.NAME, JHWrapAroundArc::new);

        return sub;
    }

    private static JMenu createDislocateSubmenu() {
        PMenu sub = new PMenu("Dislocate");

        sub.addFilter(DrunkVision.NAME, DrunkVision::new);
        sub.addFilter(JHKaleidoscope.NAME, JHKaleidoscope::new);
        sub.addFilter(JHOffset.NAME, JHOffset::new);
        sub.addFilter(Mirror.NAME, Mirror::new);
        sub.addFilter(Slice.NAME, Slice::new);
        sub.addFilter(JHVideoFeedback.NAME, JHVideoFeedback::new);

        return sub;
    }

    private static JMenu createLightSubmenu() {
        PMenu sub = new PMenu("Light");

        sub.addFilter(Flashlight.NAME, Flashlight::new);
        sub.addFilter(JHGlint.NAME, JHGlint::new);
        sub.addFilter(JHGlow.NAME, JHGlow::new);
        sub.addFilter(JHRays.NAME, JHRays::new);
        sub.addFilter(JHSparkle.NAME, JHSparkle::new);

        return sub;
    }

    private static JMenu createNoiseSubmenu() {
        PMenu sub = new PMenu("Noise");

        sub.buildFilter(JHReduceNoise.NAME, JHReduceNoise::new)
                .noGUI()
                .add();
        sub.buildFilter(JHMedian.NAME, JHMedian::new)
                .noGUI()
                .add();

        sub.addSeparator();

        sub.addFilter(AddNoise.NAME, AddNoise::new);
        sub.addFilter(JHPixelate.NAME, JHPixelate::new);

        return sub;
    }

    private static JMenu createRenderSubmenu() {
        PMenu sub = new PMenu("Render");

        sub.addFilter(Clouds.NAME, Clouds::new);
        sub.addFilter(JHPlasma.NAME, JHPlasma::new);
        sub.addFilter(ValueNoise.NAME, ValueNoise::new);

        sub.addSeparator();

        sub.addFilter(JHBrushedMetal.NAME, JHBrushedMetal::new);
        sub.addFilter(JHCaustics.NAME, JHCaustics::new);
        sub.addFilter(JHCells.NAME, JHCells::new);
        sub.addFilter(Marble.NAME, Marble::new);
        sub.addFilter(Voronoi.NAME, Voronoi::new);
        sub.addFilter(JHWood.NAME, JHWood::new);

        sub.addSeparator();

        sub.add(createRenderFractalsSubmenu());
        sub.add(createRenderGeometrySubmenu());
        sub.add(createRenderShapesSubmenu());

        return sub;
    }

    private static JMenu createRenderShapesSubmenu() {
        PMenu sub = new PMenu("Shapes");

        sub.addFilter("Flower of Life", FlowerOfLife::new);
        sub.addFilter("Grid", RenderGrid::new);
        sub.addFilter("Lissajous Curve", Lissajous::new);
        sub.addFilter("Mystic Rose", MysticRose::new);
        sub.addFilter("Spirograph", Spirograph::new);

        return sub;
    }

    private static JMenu createRenderFractalsSubmenu() {
        PMenu sub = new PMenu("Fractals");

        sub.addFilter(ChaosGame.NAME, ChaosGame::new);
        sub.addFilter(FractalTree.NAME, FractalTree::new);
        sub.addFilter(JuliaSet.NAME, JuliaSet::new);
        sub.addFilter(MandelbrotSet.NAME, MandelbrotSet::new);

        return sub;
    }

    private static JMenu createRenderGeometrySubmenu() {
        PMenu sub = new PMenu("Geometry");

        sub.addFilter(JHCheckerFilter.NAME, JHCheckerFilter::new);
        sub.addFilter(Starburst.NAME, Starburst::new);

        return sub;
    }

    private static JMenu createArtisticSubmenu() {
        PMenu sub = new PMenu("Artistic");

        sub.addFilter(JHCrystallize.NAME, JHCrystallize::new);
        sub.addFilter(JHEmboss.NAME, JHEmboss::new);
        sub.addFilter(JHOilPainting.NAME, JHOilPainting::new);
        sub.addFilter(Orton.NAME, Orton::new);
        sub.addFilter(PhotoCollage.NAME, PhotoCollage::new);
        sub.addFilter(JHPointillize.NAME, JHPointillize::new);
        sub.addFilter(RandomSpheres.NAME, RandomSpheres::new);
        sub.addFilter(JHSmear.NAME, JHSmear::new);
        sub.addFilter(JHStamp.NAME, JHStamp::new);
        sub.addFilter(JHWeave.NAME, JHWeave::new);

        sub.add(createHalftoneSubmenu());

        return sub;
    }

    private static JMenu createHalftoneSubmenu() {
        PMenu sub = new PMenu("Halftone");

        sub.addFilter(JHStripedHalftone.NAME, JHStripedHalftone::new);
        sub.addFilter(JHConcentricHalftone.NAME, JHConcentricHalftone::new);
        sub.addFilter(JHColorHalftone.NAME, JHColorHalftone::new);

        return sub;
    }


    private static JMenu createFindEdgesSubmenu() {
        PMenu sub = new PMenu("Find Edges");

        sub.addFilter(JHConvolutionEdge.NAME, JHConvolutionEdge::new);
        sub.addAction(new FilterAction(JHLaplacian.NAME, JHLaplacian::new)
                .withoutGUI());
        sub.addFilter(JHDifferenceOfGaussians.NAME, JHDifferenceOfGaussians::new);
        sub.addFilter("Canny", Canny::new);

        return sub;
    }

    private static JMenu createOtherSubmenu() {
        PMenu sub = new PMenu("Other");

        sub.addFilter(JHDropShadow.NAME, JHDropShadow::new);
        sub.addFilter(Morphology.NAME, Morphology::new);
        sub.addFilter("Random Filter", RandomFilter::new);
        sub.addFilter("Transform Layer", TransformLayer::new);
        sub.addFilter(Transition2D.NAME, Transition2D::new);

        sub.addSeparator();

        sub.addFilter(Convolve.createFilterAction(3));
        sub.addFilter(Convolve.createFilterAction(5));

        sub.addSeparator();

        sub.addFilter(ChannelToTransparency.NAME, ChannelToTransparency::new);
        sub.buildFilter(JHInvertTransparency.NAME, JHInvertTransparency::new)
                .noGUI()
                .add();

        return sub;
    }

    private static JMenu createViewMenu(PixelitorWindow pw) {
        PMenu viewMenu = new PMenu("View", 'V');

        viewMenu.add(ZoomMenu.INSTANCE);

        viewMenu.addSeparator();

        viewMenu.addAction(new MenuAction("Show History...") {
            @Override
            public void onClick() {
                History.showHistory();
            }
        });
        viewMenu.addAction(new MenuAction("Show Navigator...") {
            @Override
            public void onClick() {
                Navigator.showInDialog(pw);
            }
        });

        viewMenu.addSeparator();

        viewMenu.add(createColorVariationsSubmenu(pw));
        viewMenu.addAlwaysEnabledAction(new MenuAction("Color Palette...") {
            @Override
            public void onClick() {
                PalettePanel.showDialog(pw, new FullPalette(),
                        ColorSwatchClickHandler.STANDARD);
            }
        });

        viewMenu.addSeparator();

        viewMenu.add(ShowHideStatusBarAction.INSTANCE);
        viewMenu.buildAction(ShowHideHistogramsAction.INSTANCE)
                .alwaysEnabled().withKey(F6).add();
        viewMenu.buildAction(ShowHideLayersAction.INSTANCE)
                .alwaysEnabled().withKey(F7).add();
        viewMenu.add(ShowHideToolsAction.INSTANCE);
        viewMenu.buildAction(ShowHideAllAction.INSTANCE)
                .alwaysEnabled().withKey(TAB).add();

        viewMenu.addAlwaysEnabledAction(new MenuAction("Set Default Workspace") {
            @Override
            public void onClick() {
                WorkSpace.resetDefaults(pw);
            }
        });

        JCheckBoxMenuItem showPixelGridMI = new OpenImageEnabledCheckBoxMenuItem("Show Pixel Grid");
        showPixelGridMI.addActionListener(e ->
                View.setShowPixelGrid(showPixelGridMI.getState()));
        viewMenu.add(showPixelGridMI);

        viewMenu.addSeparator();

        viewMenu.addAction(new MenuAction("Add Horizontal Guide...") {
            @Override
            public void onClick() {
                View view = getActiveView();
                Guides.showAddSingleGuideDialog(view, true);
            }
        });

        viewMenu.addAction(new MenuAction("Add Vertical Guide...") {
            @Override
            public void onClick() {
                View view = getActiveView();
                Guides.showAddSingleGuideDialog(view, false);
            }
        });

        viewMenu.addAction(new MenuAction("Add Grid Guides...") {
            @Override
            public void onClick() {
                View view = getActiveView();
                Guides.showAddGridDialog(view);
            }
        });

        viewMenu.addAction(new MenuAction("Clear Guides") {
            @Override
            public void onClick() {
                var comp = getActiveComp();
                comp.clearGuides();
            }
        });

        viewMenu.addSeparator();

        viewMenu.add(createArrangeWindowsSubmenu());

        return viewMenu;
    }

    private static JMenu createColorVariationsSubmenu(PixelitorWindow pw) {
        PMenu variations = new PMenu("Color Variations");
        variations.addAlwaysEnabledAction(new MenuAction("Foreground...") {
            @Override
            public void onClick() {
                PalettePanel.showFGVariationsDialog(pw);
            }
        });
        variations.addAlwaysEnabledAction(new MenuAction(
                "HSB Mix Foreground with Background...") {
            @Override
            public void onClick() {
                PalettePanel.showHSBMixDialog(pw, true);
            }
        });
        variations.addAlwaysEnabledAction(new MenuAction(
                "RGB Mix Foreground with Background...") {
            @Override
            public void onClick() {
                PalettePanel.showRGBMixDialog(pw, true);
            }
        });

        variations.addSeparator();

        variations.addAlwaysEnabledAction(new MenuAction("Background...") {
            @Override
            public void onClick() {
                PalettePanel.showBGVariationsDialog(pw);
            }
        });
        variations.addAlwaysEnabledAction(new MenuAction(
                "HSB Mix Background with Foreground...") {
            @Override
            public void onClick() {
                PalettePanel.showHSBMixDialog(pw, false);
            }
        });
        variations.addAlwaysEnabledAction(new MenuAction(
                "RGB Mix Background with Foreground...") {
            @Override
            public void onClick() {
                PalettePanel.showRGBMixDialog(pw, false);
            }
        });
        return variations;
    }

    private static JMenu createArrangeWindowsSubmenu() {
        PMenu sub = new PMenu("Arrange Windows");

        NamedAction cascadeAction = new NamedAction("Cascade") {
            @Override
            public void actionPerformed(ActionEvent e) {
                ImageArea.cascadeWindows();
            }
        };
        cascadeAction.setEnabled(ImageArea.currentModeIs(FRAMES));
        sub.addSelfControlledAction(cascadeAction);

        NamedAction tileAction = new NamedAction("Tile") {
            @Override
            public void actionPerformed(ActionEvent e) {
                ImageArea.tileWindows();
            }
        };
        tileAction.setEnabled(ImageArea.currentModeIs(FRAMES));
        sub.addSelfControlledAction(tileAction);

        // make sure that "Cascade" and "Tile" are grayed out in TABS mode
        ImageArea.addUIChangeListener(newMode -> {
            cascadeAction.setEnabled(newMode == FRAMES);
            tileAction.setEnabled(newMode == FRAMES);
        });

        return sub;
    }

    private static JMenu createDevelopMenu(PixelitorWindow pw) {
        PMenu developMenu = new PMenu("Develop", 'D');

        developMenu.add(createDebugSubmenu(pw));
        developMenu.add(createTestSubmenu());
        developMenu.add(createSplashSubmenu());
        developMenu.add(createExperimentalSubmenu());

        developMenu.addFilter(PorterDuff.NAME, PorterDuff::new);

        developMenu.addAlwaysEnabledAction(new MenuAction("Filter Creator...") {
            @Override
            public void onClick() {
                FilterCreator.showInDialog(pw);
            }
        });

        developMenu.addAction(new MenuAction("Dump Event Queue") {
            @Override
            public void onClick() {
                Events.dumpAll();
            }
        });

        developMenu.addAction(new MenuAction("Debug PID") {
            @Override
            public void onClick() {
                String vmRuntimeInfo = ManagementFactory.getRuntimeMXBean().getName();
                System.out.println(format("MenuBar::onClick: vmRuntimeInfo = '%s'",
                        vmRuntimeInfo));
            }
        });

        developMenu.addAction(new MenuAction("Debug Layer Mask") {
            @Override
            public void onClick() {
                ImageLayer imageLayer = (ImageLayer) getActiveLayer();
                debugImage(imageLayer.getImage(), "layer image");

                if (imageLayer.hasMask()) {
                    LayerMask layerMask = imageLayer.getMask();
                    BufferedImage maskImage = layerMask.getImage();
                    debugImage(maskImage, "mask image");

                    BufferedImage transparencyImage = layerMask.getTransparencyImage();
                    debugImage(transparencyImage, "transparency image");
                }
            }
        });

        developMenu.addAction(new MenuAction("Mask update transparency from BW") {
            @Override
            public void onClick() {
                ImageLayer imageLayer = (ImageLayer) getActiveLayer();
                if (imageLayer.hasMask()) {
                    imageLayer.getMask().updateFromBWImage();
                    imageLayer.getComp().imageChanged();
                } else {
                    Messages.showInfo("No Mask in Current image", "Error");
                }
            }
        });

        developMenu.addAction(new MenuAction("Debug getCanvasSizedSubImage") {
            @Override
            public void onClick() {
                onActiveDrawable(dr -> debugImage(dr.getCanvasSizedSubImage()));
            }
        });

        developMenu.addAction(new MenuAction("Tests3x3.dumpCompositeOfActive()") {
            @Override
            public void onClick() {
                Tests3x3.dumpCompositeOfActive();
            }
        });

        developMenu.addAction(new MenuAction("Debug translation and canvas") {
            @Override
            public void onClick() {
                onActiveImageLayer(layer ->
                        System.out.println(layer.toDebugCanvasString()));
            }
        });

        developMenu.addAction(new MenuAction("Debug Copy Brush") {
            @Override
            public void onClick() {
                CopyBrush.setDebugBrushImage(true);
            }
        });

        developMenu.addAction(new MenuAction("Create All Filters") {
            @Override
            public void onClick() {
                FilterUtils.createAllFilters();
            }
        });

        developMenu.addAlwaysEnabledAction(new MenuAction("Debug Java Main Version") {
            @Override
            public void onClick() {
                int version = getJavaMainVersion();
                Dialogs.showInfoDialog(pw, "Debug",
                        "Java Main Version = " + version);
            }
        });

        developMenu.addAlwaysEnabledAction(new MenuAction("Change UI") {
            @Override
            public void onClick() {
                ImageArea.changeUI();
            }
        }, CTRL_K);

        developMenu.addAlwaysEnabledAction(new MenuAction("frame size 1366x728") {
            @Override
            public void onClick() {
                PixelitorWindow.getInstance().setSize(1366, 728);
            }
        });

        return developMenu;
    }

    private static JMenu createDebugSubmenu(PixelitorWindow pw) {
        PMenu sub = new PMenu("Debug");

        sub.addAction(new MenuAction("repaint() on the active image") {
            @Override
            public void onClick() {
                repaintActive();
            }
        });

        sub.addAction(new MenuAction("imageChanged(FULL) on the active image") {
            @Override
            public void onClick() {
                getActiveComp().imageChanged(FULL, true);
            }
        });

        sub.addAlwaysEnabledAction(new MenuAction("revalidate() the main window") {
            @Override
            public void onClick() {
                pw.getContentPane().revalidate();
            }
        });

        sub.addAlwaysEnabledAction(new MenuAction("update all UI") {
            @Override
            public void onClick() {
                Themes.updateAllUI();
            }
        });

        sub.addAction(new MenuAction("reset the translation of current layer") {
            @Override
            public void onClick() {
                var comp = getActiveComp();
                Layer layer = comp.getActiveLayer();
                if (layer instanceof ContentLayer) {
                    ContentLayer contentLayer = (ContentLayer) layer;
                    contentLayer.setTranslation(0, 0);
                }
                if (layer.hasMask()) {
                    layer.getMask().setTranslation(0, 0);
                }
                comp.imageChanged();
            }
        });

        sub.addAction(new MenuAction("Update Histograms") {
            @Override
            public void onClick() {
                var comp = getActiveComp();
                HistogramsPanel.INSTANCE.updateFrom(comp);
            }
        });

        sub.addAction(new MenuAction("Debug ImageLayer Images") {
            @Override
            public void onClick() {
                onActiveDrawable(Drawable::debugImages);
            }
        });

        sub.addAction(new MenuAction("debug mouse to sys.out") {
            @Override
            public void onClick() {
                GlobalEvents.registerDebugMouseWatching(false);
            }
        });

        return sub;
    }

    private static JMenu createTestSubmenu() {
        PMenu sub = new PMenu("Test");

        sub.addFilter("ParamTest", ParamTest::new);

        sub.buildAction(new MenuAction("Random GUI Test") {
            @Override
            public void onClick() {
                RandomGUITest.start();
            }
        }).alwaysEnabled().withKey(CTRL_R).add();

        sub.addAction(new MenuAction("Save Current Image in All Formats...") {
            @Override
            public void onClick() {
                OpenSave.saveCurrentImageInAllFormats();
            }
        });

        return sub;
    }

    private static JMenu createSplashSubmenu() {
        PMenu sub = new PMenu("Splash");

        sub.addAlwaysEnabledAction(new MenuAction("Create Splash Image") {
            @Override
            public void onClick() {
                SplashImageCreator.createSplashComp();
            }
        });

        sub.addAlwaysEnabledAction(new MenuAction("Save Many Splash Images...") {
            @Override
            public void onClick() {
                SplashImageCreator.saveManySplashImages();
            }
        });

        return sub;
    }

    private static JMenu createExperimentalSubmenu() {
        PMenu sub = new PMenu("Experimental");

        sub.addFilter(Contours.NAME, Contours::new);
        sub.addFilter(JHCustomHalftone.NAME, JHCustomHalftone::new);

        sub.addSeparator();

        sub.addFilter(BlurredShapeTester.NAME, BlurredShapeTester::new);
        sub.addFilter(XYZTest.NAME, XYZTest::new);
        sub.addFilter(Droste.NAME, Droste::new);
        sub.addFilter(Sphere3D.NAME, Sphere3D::new);

        return sub;
    }

    private static JMenu createHelpMenu(PixelitorWindow pw) {
        PMenu helpMenu = new PMenu("Help", 'H');

        helpMenu.addAlwaysEnabledAction(new MenuAction("Tip of the Day") {
            @Override
            public void onClick() {
                TipsOfTheDay.showTips(pw, true);
            }
        });

        helpMenu.addSeparator();

        helpMenu.add(new OpenInBrowserAction("Report an Issue...",
                "https://github.com/lbalazscs/Pixelitor/issues"));

        helpMenu.add(new MenuAction("Internal State...") {
            @Override
            public void onClick() {
                AppNode node = new AppNode();
                String title = "Internal State";

                JTree tree = new JTree(node);

                JLabel explainLabel = new JLabel(
                        // TODO re-formulate
                        "<html>If you are reporting a bug that cannot be reproduced," +
                                "<br>please include the following information:");
                explainLabel.setBorder(createEmptyBorder(5, 5, 5, 5));

                JPanel form = new JPanel(new BorderLayout());
                form.add(explainLabel, NORTH);
                form.add(new JScrollPane(tree), CENTER);

                String text = node.toDetailedString();

                GUIUtils.showCopyTextToClipboardDialog(form, text, title);
            }
        });

        helpMenu.add(new MenuAction("Check for Update...") {
            @Override
            public void onClick() {
                UpdatesCheck.checkForUpdates();
            }
        });

        helpMenu.addAlwaysEnabledAction(new MenuAction("About") {
            @Override
            public void onClick() {
                AboutDialog.showDialog(pw);
            }
        });

        return helpMenu;
    }
}
