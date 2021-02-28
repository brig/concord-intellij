package brig.concord.run;

import brig.concord.run.ui.ConcordCliRunConfigurationForm;
import com.intellij.execution.impl.CheckableRunConfigurationEditor;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class ConcordCliRunConfigurationSettingsEditor extends SettingsEditor<ConcordCliRunConfiguration>
        implements CheckableRunConfigurationEditor<ConcordCliRunConfiguration> {

    private final ConcordCliRunConfigurationForm myForm;

    public ConcordCliRunConfigurationSettingsEditor(ConcordCliRunConfiguration concordCliRunConfiguration) {
        this.myForm = new ConcordCliRunConfigurationForm(concordCliRunConfiguration);
    }

    @Override
    public void checkEditorData(ConcordCliRunConfiguration s) {
        s.setProcessEntryPoint(myForm.getProcessEntryPoint());
        s.setProcessArguments(myForm.getProcessArguments());
        s.setProcessProfiles(myForm.getProcessProfiles());
        s.setProcessWorkDir(myForm.getProcessWorkDir());
//        s.setProcessSdk(myForm.getProcessSdk());
        s.setSdkOptions(myForm.getSdkOptions());
        s.setJrePath(myForm.getJrePath());
        s.setVmOptions(myForm.getVmOptions());
//         this prevents applyTo() call with unneeded sdk model update
    }

    @Override
    protected void resetEditorFrom(@NotNull ConcordCliRunConfiguration runConfiguration) {
        myForm.setProcessEntryPoint(runConfiguration.getProcessEntryPoint());
        myForm.setProcessArguments(runConfiguration.getProcessArguments());
        myForm.setProcessProfiles(runConfiguration.getProcessProfiles());
        myForm.setProcessWorkDir(runConfiguration.getProcessWorkDir());
        myForm.setProcessSdk(runConfiguration.getProcessSdk());
        myForm.setSdkOptions(runConfiguration.getSdkOptions());
        myForm.setJrePath(runConfiguration.getJrePathOrName());
        myForm.setVmOptions(runConfiguration.getVmOptions());
    }

    @Override
    protected void applyEditorTo(@NotNull ConcordCliRunConfiguration runConfiguration) throws ConfigurationException {
        runConfiguration.setProcessEntryPoint(myForm.getProcessEntryPoint());
        runConfiguration.setProcessArguments(myForm.getProcessArguments());
        runConfiguration.setProcessProfiles(myForm.getProcessProfiles());
        runConfiguration.setProcessWorkDir(myForm.getProcessWorkDir());
        runConfiguration.setProcessSdk(myForm.getProcessSdk());
        runConfiguration.setSdkOptions(myForm.getSdkOptions());
        runConfiguration.setJrePath(myForm.getJrePath());
        runConfiguration.setVmOptions(myForm.getVmOptions());
    }

    @NotNull
    @Override
    protected JComponent createEditor() {
        return myForm.getRootPanel();
    }

    @Override
    protected void disposeEditor() {
    }
}
