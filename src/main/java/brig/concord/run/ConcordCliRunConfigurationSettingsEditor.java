package brig.concord.run;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class ConcordCliRunConfigurationSettingsEditor extends SettingsEditor<ConcordCliRunConfiguration> {

    private final ConcordCliRunConfigurationForm myForm;

    public ConcordCliRunConfigurationSettingsEditor(ConcordCliRunConfiguration concordCliRunConfiguration) {
        this.myForm = new ConcordCliRunConfigurationForm(concordCliRunConfiguration);
    }

    @Override
    protected void resetEditorFrom(@NotNull ConcordCliRunConfiguration runConfiguration) {
        myForm.setProcessEntryPoint(runConfiguration.getProcessEntryPoint());
        myForm.setProcessArguments(runConfiguration.getProcessArguments());
        myForm.setProcessProfiles(runConfiguration.getProcessProfiles());
        myForm.setProcessWorkDir(runConfiguration.getProcessWorkDir());
        myForm.setProcessSdk(runConfiguration.getProcessSdk());
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
}
