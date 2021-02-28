package brig.concord.run.ui;


import brig.concord.ConcordBundle;
import brig.concord.sdk.ConcordSdkType;
import brig.concord.sdk.SdkDownloader;
import com.intellij.execution.ExecutionBundle;
import com.intellij.icons.AllIcons;
import com.intellij.ide.util.BrowseFilesListener;
import com.intellij.openapi.project.ProjectBundle;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkModel;
import com.intellij.openapi.projectRoots.SdkType;
import com.intellij.openapi.projectRoots.impl.JavaSdkImpl;
import com.intellij.openapi.roots.ui.OrderEntryAppearanceService;
import com.intellij.openapi.roots.ui.configuration.projectRoot.ProjectSdksModel;
import com.intellij.openapi.roots.ui.configuration.projectRoot.SdkDownload;
import com.intellij.openapi.roots.ui.configuration.projectRoot.SdkDownloadTask;
import com.intellij.openapi.roots.ui.configuration.projectRoot.SdkDownloadTracker;
import com.intellij.openapi.ui.BrowseFolderRunnable;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.TextComponentAccessor;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.ColoredListCellRenderer;
import com.intellij.ui.SimpleColoredComponent;
import com.intellij.ui.SortedComboBoxModel;
import com.intellij.ui.components.JBTextField;
import com.intellij.ui.components.fields.ExtendableTextField;
import com.intellij.util.Consumer;
import com.intellij.util.ui.EmptyIcon;
import com.intellij.util.ui.JBInsets;
import com.intellij.util.ui.StatusText;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxEditor;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class SdkChooserField extends JPanel {

    private static final TextComponentAccessor<ComboBox<JreComboBoxItem>> TEXT_COMPONENT_ACCESSOR = new JreComboBoxTextComponentAccessor();

    private final ComboBox<JreComboBoxItem> component;
    private final SortedComboBoxModel<JreComboBoxItem> comboBoxModel;

    public SdkChooserField() {
        this.comboBoxModel = new SortedComboBoxModel<>((o1, o2) -> {
            int result = Comparing.compare(o1.getOrder(), o2.getOrder());
            if (result != 0) {
                return result;
            }
            return o1.getPresentableText().compareToIgnoreCase(o2.getPresentableText());
        }) {
            @Override
            public void setSelectedItem(Object anItem) {
                if (anItem instanceof AddJreItem) {
                    getComponent().hidePopup();
                    getBrowseRunnable().run();
                } else if (anItem instanceof DownloadSdkItem) {
                    ConcordSdkType type = ConcordSdkType.getInstance();

                    SdkDownload downloadExtension = SdkDownload.EP_NAME.findFirstSafe(it -> it.supportsDownload(type));

                    String text = ProjectBundle.message("sdk.configure.download.action", type.getPresentableName());
                    String title = ProjectBundle.message("sdk.configure.download.actionTitle", type.getPresentableName());
                    String subText = ProjectBundle.message("sdk.configure.download.subAction", type.getPresentableName());

                    getComponent().hidePopup();
                    doDownload(downloadExtension, getComponent(), type, sdk -> System.out.println(">>>>>>: " + sdk));
                } else {
                    super.setSelectedItem(anItem);
                }
            }
        };

        buildModel(this.comboBoxModel);

        ComboBox<JreComboBoxItem> comboBox = new ComboBox<>(this.comboBoxModel);
        comboBox.setEditable(true);
        comboBox.setEditor(new JreComboboxEditor(this.comboBoxModel));
        comboBox.setRenderer(new ColoredListCellRenderer<>() {
            {
                setIpad(JBInsets.create(1, 0));
                setMyBorder(null);
            }

            @Override
            protected void customizeCellRenderer(@NotNull JList<? extends JreComboBoxItem> list,
                                                 JreComboBoxItem value,
                                                 int index,
                                                 boolean selected,
                                                 boolean hasFocus) {
                if (value != null) {
                    value.render(this, selected);
                }
            }
        });
        this.component = comboBox;

        FlowLayout rootLayout = (FlowLayout) getLayout();
        rootLayout.setHgap(0);
        rootLayout.setVgap(0);
        add(this.component, BorderLayout.CENTER);

        updateUI();
    }

    public void doDownload(@NotNull SdkDownload downloadExtension,
                           @NotNull JComponent parent,
                           @NotNull final SdkType type,
                           @NotNull final Consumer<? super Sdk> callback) {
        downloadExtension.showDownloadUI(type, new SdkModel() {
            @Override
            public Sdk @NotNull [] getSdks() {
                return new Sdk[0];
            }

            @Override
            public @Nullable Sdk findSdk(String sdkName) {
                return null;
            }

            @Override
            public void addSdk(@NotNull Sdk sdk) {

            }

            @Override
            public void addListener(@NotNull Listener listener) {

            }

            @Override
            public void removeListener(@NotNull Listener listener) {

            }

            @Override
            public @NotNull Listener getMulticaster() {
                return null;
            }
        },
        parent, null, sdk -> setupInstallableSdk(type, sdk, callback));
    }

    public void setupInstallableSdk(@NotNull SdkType type,
                                    @NotNull SdkDownloadTask item,
                                    @Nullable Consumer<? super Sdk> callback) {

    }

    public void doAdd(@NotNull Sdk newSdk, @Nullable Consumer<? super Sdk> updateTree) {
    }

    public ComboBox<JreComboBoxItem> getComponent() {
        return component;
    }

    public String getJrePathOrName() {
        JreComboBoxItem jre = getSelectedJre();
        if (jre == null) {
            return null;
        }
        return jre.getPathOrName();
    }

    public void setPathOrName(@Nullable String pathOrName) {
        JreComboBoxItem toSelect = null;
        if (!StringUtil.isEmpty(pathOrName)) {
            toSelect = findOrAddCustomJre(pathOrName);
        }
        getComponent().setSelectedItem(toSelect);
    }

    private JreComboBoxItem findOrAddCustomJre(@NotNull String pathOrName) {
        for (JreComboBoxItem item : comboBoxModel.getItems()) {
            if (item instanceof CustomJreItem && FileUtil.pathsEqual(pathOrName, ((CustomJreItem)item).path)
                    || pathOrName.equals(item.getPathOrName())) {
                return item;
            }
        }

        CustomJreItem item = new CustomJreItem(pathOrName);
        comboBoxModel.add(item);
        return item;
    }

    private static void buildModel(SortedComboBoxModel<JreComboBoxItem> comboBoxModel) {
        List<Sdk> allJavaJdks = Arrays.stream(ProjectJdkTable.getInstance().getAllJdks())
                .filter(j -> j.getSdkType() == ConcordSdkType.getInstance())
                .collect(Collectors.toList());
        for (Sdk sdk : allJavaJdks) {
            comboBoxModel.add(new SdkAsJreItem(sdk));
        }

        comboBoxModel.add(new AddJreItem());
        comboBoxModel.add(new DownloadSdkItem());
    }

    private JreComboBoxItem getSelectedJre() {
        ComboBox<?> comboBox = getComponent();
        return comboBox.isEditable() ? (JreComboBoxItem)comboBox.getEditor().getItem() : (JreComboBoxItem)comboBox.getSelectedItem();
    }

    @NotNull
    @SuppressWarnings("UnstableApiUsage")
    private Runnable getBrowseRunnable() {
        return new BrowseFolderRunnable<>(ExecutionBundle.message("run.configuration.select.alternate.jre.label"),
                ExecutionBundle.message("run.configuration.select.jre.dir.label"),
                null,
                BrowseFilesListener.SINGLE_DIRECTORY_DESCRIPTOR,
                getComponent(),
                TEXT_COMPONENT_ACCESSOR);
    }

    private class JreComboboxEditor extends BasicComboBoxEditor {

        private final SortedComboBoxModel<JreComboBoxItem> comboBoxModel;

        JreComboboxEditor(SortedComboBoxModel<JreComboBoxItem> comboBoxModel) {
            this.comboBoxModel = comboBoxModel;
        }

        @Override
        @SuppressWarnings("UnstableApiUsage")
        protected JTextField createEditorComponent() {
            JBTextField field = new ExtendableTextField().addBrowseExtension(getBrowseRunnable(), null);
            field.setBorder(null);
            field.addFocusListener(new FocusListener() {
                @Override
                public void focusGained(FocusEvent e) {
                    update(e);
                }
                @Override
                public void focusLost(FocusEvent e) {
                    update(e);
                }

                private void update(FocusEvent e) {
                    Component c = e.getComponent().getParent();
                    if (c != null) {
                        c.revalidate();
                        c.repaint();
                    }
                }
            });
            StatusText emptyText = field.getEmptyText();
            emptyText.setText(ConcordBundle.message("jre.select.placeholder"));
            return field;
        }

        @Override
        public void setItem(Object anObject) {
            editor.setText(anObject == null ? "" : ((JreComboBoxItem)anObject).getPresentableText());
        }

        @Override
        public Object getItem() {
            String text = editor.getText().trim();
            for (JreComboBoxItem item : comboBoxModel.getItems()) {
                if (item.getPresentableText().equals(text)) {
                    return item;
                }
            }
            return new CustomJreItem(FileUtil.toSystemIndependentName(text));
        }
    }

    private static class JreComboBoxTextComponentAccessor implements TextComponentAccessor<ComboBox<JreComboBoxItem>> {

        @Override
        public String getText(ComboBox<JreComboBoxItem> component) {
            JreComboBoxItem item = component.isEditable() ? component.getItem() : (JreComboBoxItem)component.getEditor().getItem();
            return item != null ? item.getPresentableText() : "";
        }

        @Override
        public void setText(ComboBox<JreComboBoxItem> component, @NotNull String text) {
            CustomJreItem item = new CustomJreItem(FileUtil.toSystemIndependentName(text));
            if (component.isEditable()) {
                component.getEditor().setItem(item);
            }
            else {
                ((SortedComboBoxModel<JreComboBoxItem>)component.getModel()).add(item);
                component.setItem(item);
            }
        }
    }

    interface JreComboBoxItem {

        void render(SimpleColoredComponent component, boolean selected);

        String getPresentableText();

        @Nullable
        String getPathOrName();

        int getOrder();
    }

    private static class AddJreItem implements JreComboBoxItem {

        @Override
        public void render(SimpleColoredComponent component, boolean selected) {
            component.append(getPresentableText());
            component.setIcon(EmptyIcon.ICON_16);
        }

        @Override
        public String getPresentableText() {
            return ExecutionBundle.message("run.configuration.select.alternate.jre.action");
        }

        @Override
        public @Nullable String getPathOrName() {
            return null;
        }

        @Override
        public int getOrder() {
            return Integer.MAX_VALUE;
        }
    }

    private static class DownloadSdkItem implements JreComboBoxItem {

        @Override
        public void render(SimpleColoredComponent component, boolean selected) {
            component.append(getPresentableText());
        }

        @Override
        public String getPresentableText() {
            return "Download SDK";
        }

        @Override
        public @Nullable String getPathOrName() {
            return null;
        }

        @Override
        public int getOrder() {
            return Integer.MAX_VALUE - 1;
        }
    }

    private static class SdkAsJreItem implements JreComboBoxItem {

        private final Sdk sdk;

        SdkAsJreItem(Sdk sdk) {
            this.sdk = sdk;
        }

        @Override
        public void render(SimpleColoredComponent component, boolean selected) {
            OrderEntryAppearanceService.getInstance().forJdk(sdk, false, selected, true).customize(component);
        }

        @Override
        public String getPresentableText() {
            return sdk.getName();
        }

        @Override
        public String getPathOrName() {
            return sdk.getName();
        }

        @Override
        public int getOrder() {
            return 1;
        }
    }

    static class CustomJreItem implements JreComboBoxItem {

        private final String path;
        private final String name;

        CustomJreItem(String path) {
            this(path, null);
        }

        CustomJreItem(String path, String name) {
            this.path = path;
            this.name = name;
        }

        @Override
        public void render(SimpleColoredComponent component, boolean selected) {
            component.append(getPresentableText());
            component.setIcon(AllIcons.Nodes.Folder);
        }

        @Override
        public String getPresentableText() {
            return name != null && !name.equals(path) ? name : FileUtil.toSystemDependentName(path);
        }

        @Override
        public String getPathOrName() {
            return path;
        }

        @Override
        public int getOrder() {
            return 2;
        }
    }
}
