package brig.concord.run;

import brig.concord.ConcordBundle;
import brig.concord.sdk.ConcordSdkType;
import com.intellij.execution.util.ListTableWithButtons;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ui.configuration.SdkComboBox;
import com.intellij.openapi.roots.ui.configuration.SdkComboBoxModel;
import com.intellij.openapi.roots.ui.configuration.projectRoot.ProjectSdksModel;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.ListTableModel;

import javax.swing.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ConcordCliRunConfigurationForm {

    private final ProcessArgumentsTable processArguments;
    private final SdkComboBox sdk;
    private JPanel rootPanel;
    private JTextField processProfiles;
    private TextFieldWithBrowseButton workingDirectory;
    private JTextField processEntryPoint;

    public ConcordCliRunConfigurationForm(ConcordCliRunConfiguration runConfiguration) {
        this.processArguments = new ProcessArgumentsTable();
        GridConstraints argsConstraints = new GridConstraints();
        argsConstraints.setColumn(1);
        argsConstraints.setRow(1);
        argsConstraints.setFill(3);
        this.rootPanel.add(this.processArguments.getComponent(), argsConstraints);

        ProjectSdksModel sdksModel = new ProjectSdksModel();
        sdksModel.syncSdks();

        SdkComboBoxModel model = SdkComboBoxModel.createSdkComboBoxModel(runConfiguration.getProject(), sdksModel, sdkTypeId -> sdkTypeId == ConcordSdkType.getInstance(), sdkTypeId -> sdkTypeId == ConcordSdkType.getInstance(), sdk -> sdk.getSdkType() == ConcordSdkType.getInstance());
        this.sdk = new SdkComboBox(model);
        GridConstraints sdkConstraints = new GridConstraints();
        sdkConstraints.setColumn(1);
        sdkConstraints.setRow(4);
        sdkConstraints.setFill(3);
        this.rootPanel.add(this.sdk, sdkConstraints);
    }

    public JComponent getRootPanel() {
        return rootPanel;
    }

    public List<ProcessArgument> getProcessArguments() {
        return ContainerUtil.filter(processArguments.getElements(), property -> !processArguments.isEmpty(property));
    }

    public void setProcessArguments(List<ProcessArgument> args) {
        processArguments.setValues(args);
    }

    public List<String> getProcessProfiles() {
        String profiles = processProfiles.getText();
        if (StringUtil.isEmpty(profiles)) {
            return Collections.emptyList();
        }
        return Arrays.stream(profiles.split(","))
                .map(String::trim)
                .filter(p -> !p.isEmpty())
                .collect(Collectors.toList());
    }

    public void setProcessProfiles(List<String> profiles) {
        if (profiles == null || profiles.isEmpty()) {
            processProfiles.setText("");
        } else {
            processProfiles.setText(String.join(", ", profiles));
        }
    }

    public String getProcessWorkDir() {
        return workingDirectory.getText();
    }

    public void setProcessWorkDir(String dir) {
        workingDirectory.setText(dir);
    }

    public String getProcessEntryPoint() {
        return processEntryPoint.getText();
    }

    public void setProcessEntryPoint(String entryPoint) {
        processEntryPoint.setText(entryPoint);
    }

    public Sdk getProcessSdk() throws ConfigurationException {
        WriteAction.run(() -> sdk.getModel().getSdksModel().apply());
        return sdk.getSelectedSdk();
    }

    public void setProcessSdk(Sdk sdk) {
        if (sdk == null) {
            this.sdk.setSelectedItem(this.sdk.showNoneSdkItem());
        } else {
            this.sdk.setSelectedSdk(sdk);
        }
    }

    static class ProcessArgumentsTable extends ListTableWithButtons<ProcessArgument> {

        @Override
        @SuppressWarnings("rawtypes")
        protected ListTableModel createListModel() {
            ColumnInfo<ProcessArgument, String> nameColumn = new TableColumn(ConcordBundle.message("column.name.process.args.name")) {

                @Override
                public String valueOf(ProcessArgument property) {
                    return property.getName();
                }

                @Override
                public void setValue(ProcessArgument property, String value) {
                    property.setName(value);
                }
            };

            ColumnInfo<ProcessArgument, String> valueColumn = new TableColumn(ConcordBundle.message("column.name.process.args.value")) {
                @Override
                public String valueOf(ProcessArgument property) {
                    return property.getValue();
                }

                @Override
                public void setValue(ProcessArgument property, String value) {
                    property.setValue(value);
                }
            };
            return new ListTableModel(nameColumn, valueColumn);
        }

        @Override
        protected ProcessArgument createElement() {
            return new ProcessArgument();
        }

        @Override
        protected boolean isEmpty(ProcessArgument arg) {
            return StringUtil.isEmpty(arg.getName()) && StringUtil.isEmpty(arg.getValue());
        }

        @Override
        protected ProcessArgument cloneElement(ProcessArgument arg) {
            return arg.clone();
        }

        @Override
        protected boolean canDeleteElement(ProcessArgument selection) {
            return true;
        }

        @Override
        protected List<ProcessArgument> getElements() {
            return super.getElements();
        }

        private abstract static class TableColumn extends ElementsColumnInfoBase<ProcessArgument> {
            TableColumn(String name) {
                super(name);
            }

            @Override
            public boolean isCellEditable(ProcessArgument property) {
                return true;
            }

            @Override
            protected String getDescription(ProcessArgument element) {
                return null;
            }
        }
    }
}
