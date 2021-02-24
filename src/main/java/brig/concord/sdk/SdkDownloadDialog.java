package brig.concord.sdk;

import brig.concord.ConcordBundle;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.fileChooser.FileChooserFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.*;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.ui.components.JBPanel;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.nio.file.Path;
import java.util.List;

public class SdkDownloadDialog extends DialogWrapper {

    private static final Insets spaceInsets =
            JBUI.insets(10, 10, 4, 10);

    private final JComponent panel;
    private final ComboBox<SdkItem> versionComboBox;
    private final TextFieldWithBrowseButton installDirTextField;

    public SdkDownloadDialog(Project project,
                             Component parentComponent,
                             List<SdkItem> items) {
        super(project, parentComponent, false, IdeModalityType.PROJECT);

        setTitle(ConcordBundle.message("dialog.title.download.sdk"));
        setResizable(false);

        this.versionComboBox = new ComboBox<>(items.toArray(new SdkItem[0]));
        this.versionComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof SdkItem) {
                    SdkItem item = (SdkItem) value;
                    setText(item.version());
                }
                return this;
            }
        });
        this.versionComboBox.setSwingPopup(false);
        this.versionComboBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                onVersionSelectionChange((SdkItem) e.getItem());
            }
        });

        this.installDirTextField = new TextFieldWithBrowseButton();
        installFileCompletionAndBrowseDialog(
                project,
                this.installDirTextField,
                this.installDirTextField.getTextField(),
                FileChooserDescriptorFactory.createSingleFolderDescriptor(),
                TextComponentAccessor.TEXT_FIELD_WHOLE_TEXT
        );

        this.panel = new JBPanel<>(new GridBagLayout());

        {
            JLabel label = new JLabel(ConcordBundle.message("dialog.row.sdk.version"));
            label.setHorizontalAlignment(JLabel.LEFT);
            addComponent(this.panel, label, 0, 0,
                    1, 1, spaceInsets, GridBagConstraints.LINE_START,
                    GridBagConstraints.HORIZONTAL);

            addComponent(this.panel, this.versionComboBox, 1, 0,
                    4, 1, spaceInsets, GridBagConstraints.LINE_START,
                    GridBagConstraints.HORIZONTAL);
        }

        {
            JLabel label = new JLabel(ConcordBundle.message("dialog.row.sdk.location"));
            label.setHorizontalAlignment(JLabel.LEFT);
            addComponent(this.panel, label, 0, 1,
                    1, 1, spaceInsets, GridBagConstraints.LINE_START,
                    GridBagConstraints.HORIZONTAL);

            addComponent(this.panel, this.installDirTextField, 1, 1,
                    4, 1, spaceInsets, GridBagConstraints.LINE_START,
                    GridBagConstraints.HORIZONTAL);

        }

        myOKAction.putValue(Action.NAME, ConcordBundle.message("dialog.button.download.sdk"));
        init();

        onVersionSelectionChange(items.get(0));
    }

    private static void addComponent(Container container, Component component,
                                     int gridx, int gridy, int gridwidth, int gridheight,
                                     Insets insets, int anchor, int fill) {
        GridBagConstraints gbc = new GridBagConstraints(gridx, gridy,
                gridwidth, gridheight, 1.0D, 1.0D, anchor,
                fill, insets, 0, 0);
        container.add(component, gbc);
    }

    private static void installFileCompletionAndBrowseDialog(Project project,
                                                             ComponentWithBrowseButton<JTextField> component,
                                                             JTextField textField,
                                                             FileChooserDescriptor fileChooserDescriptor,
                                                             TextComponentAccessor<JTextField> textComponentAccessor) {
        if (ApplicationManager.getApplication() == null) {
            return;
        }

        component.addActionListener(new ComponentWithBrowseButton.BrowseFolderActionListener<>(
                ConcordBundle.message("dialog.title.select.path.to.install.sdk"),
                null, component, project, fileChooserDescriptor, textComponentAccessor));

        FileChooserFactory.getInstance().installFileCompletion(textField, fileChooserDescriptor, true, null);
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        return panel;
    }

    @Override
    public ValidationInfo doValidate() {
        Pair<Path, String> result = SdkInstaller.getInstance().validateInstallDir(getSelectedDest());
        if (result.getSecond() != null) {
            return new ValidationInfo(result.getSecond(), installDirTextField);
        }
        return null;
    }

    public Pair<SdkItem, Path> selectSdkAndPath() {
        if (!showAndGet()) {
            return null;
        }

        SdkItem sdkItem = versionComboBox.getItem();
        String dest = FileUtil.expandUserHome(getSelectedDest());
        SdkInstaller.getInstance().validateInstallDir(dest);

        Pair<Path, String> result = SdkInstaller.getInstance().validateInstallDir(dest);
        if (result == null || result.getSecond() != null) {
            return null;
        }

        return Pair.pair(sdkItem, result.getFirst());
    }

    private String getSelectedDest() {
        return installDirTextField.getText();
    }

    private void onVersionSelectionChange(SdkItem item) {
        installDirTextField.setText("~/.concord/sdk/" + item.version());
        this.versionComboBox.setSelectedItem(item);
    }
}
