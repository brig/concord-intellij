package brig.concord.run;

import brig.concord.language.ConcordIcons;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.configurations.ConfigurationTypeUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class ConcordCliRunConfigurationType implements ConfigurationType {

    public static ConcordCliRunConfigurationType getInstance() {
        return ConfigurationTypeUtil.findConfigurationType(ConcordCliRunConfigurationType.class);
    }

    @Override
    public @NotNull @Nls(capitalization = Nls.Capitalization.Title) String getDisplayName() {
        return "Concord Executable";
    }

    @Override
    public @Nls(capitalization = Nls.Capitalization.Sentence) String getConfigurationTypeDescription() {
        return "Concord CLI run configuration";
    }

    @Override
    public Icon getIcon() {
        return ConcordIcons.FILE;
    }

    @Override
    public @NotNull
    @NonNls
    String getId() {
        return "ConcordCliRunConfiguration";
    }

    @Override
    public ConfigurationFactory[] getConfigurationFactories() {
        return new ConfigurationFactory[]{new ConcordConfigurationFactory(this)};
    }
}
