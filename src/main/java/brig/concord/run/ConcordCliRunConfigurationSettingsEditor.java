package brig.concord.run;

import brig.concord.sdk.ConcordSdkType;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Optional;

public class ConcordCliRunConfigurationSettingsEditor extends SettingsEditor<ConcordCliRunConfiguration> {

    private final Project project;
    private final ConcordCliRunConfigurationForm myForm;

    public ConcordCliRunConfigurationSettingsEditor(ConcordCliRunConfiguration concordCliRunConfiguration) {
        this.project = concordCliRunConfiguration.getProject();
        this.myForm = new ConcordCliRunConfigurationForm(concordCliRunConfiguration);
    }

    @Override
    protected void resetEditorFrom(@NotNull ConcordCliRunConfiguration runConfiguration) {
        myForm.setProcessEntryPoint(StringUtil.defaultIfEmpty(runConfiguration.getProcessEntryPoint(), "default"));
        myForm.setProcessArguments(runConfiguration.getProcessArguments());
        myForm.setProcessProfiles(runConfiguration.getProcessProfiles());
        myForm.setProcessWorkDir(runConfiguration.getProcessWorkDir());
        myForm.setProcessSdk(Optional.ofNullable(runConfiguration.getProcessSdk()).orElse(getProjectSdk()));
    }

    @Override
    protected void applyEditorTo(@NotNull ConcordCliRunConfiguration runConfiguration) throws ConfigurationException {
        runConfiguration.setProcessEntryPoint(myForm.getProcessEntryPoint());
        runConfiguration.setProcessArguments(myForm.getProcessArguments());
        runConfiguration.setProcessProfiles(myForm.getProcessProfiles());
        runConfiguration.setProcessWorkDir(myForm.getProcessWorkDir());
        runConfiguration.setProcessSdk(myForm.getProcessSdk());
    }

    @NotNull
    @Override
    protected JComponent createEditor() {
        return myForm.getRootPanel();
    }

    @Override
    protected void disposeEditor() {
    }

    private Sdk getProjectSdk() {
        Sdk sdk = ProjectRootManager.getInstance(project).getProjectSdk();
        if (sdk == null) {
            return null;
        }
        if (!ConcordSdkType.isConcordSdk(sdk)) {
            return null;
        }
        return sdk;
    }
}
