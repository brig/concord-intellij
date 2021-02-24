package brig.concord.run;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class ConcordConfigurationFactory extends ConfigurationFactory {

    private static final String FACTORY_NAME = "Concord configuration factory";

    protected ConcordConfigurationFactory(ConfigurationType type) {
        super(type);
    }

    @NotNull
    @Override
    public RunConfiguration createTemplateConfiguration(@NotNull Project project) {
        return new ConcordCliRunConfiguration(project, this, "Concord CLI");
    }

    @NotNull
    @Override
    public String getName() {
        return FACTORY_NAME;
    }

    @Override
    public @NotNull
    @NonNls
    String getId() {
        return FACTORY_NAME;
    }
}