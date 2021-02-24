package brig.concord.module;

import com.intellij.ide.util.projectWizard.ModuleWizardStep;

import javax.swing.*;

public class ConcordModuleWizardStep extends ModuleWizardStep {

    @Override
    public JComponent getComponent() {
        return new JLabel("");
    }

    @Override
    public void updateDataModel() {
        //todo update model according to UI
    }

}