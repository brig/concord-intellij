package brig.concord.run;

import brig.concord.psi.ConcordYamlPsiUtils;
import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.actions.ConfigurationFromContext;
import com.intellij.execution.actions.LazyRunConfigurationProducer;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ConcordCliRunContextConfigurationProducer extends LazyRunConfigurationProducer<ConcordCliRunConfiguration> implements Cloneable {

    private static RunParams getParams(ConfigurationContext context) {
        PsiElement element = context.getPsiLocation();

        String flowName = ConcordYamlPsiUtils.currentFlowName(element);
        String workingDir = getWorkingDirectory(element);

        if (flowName == null || workingDir == null) {
            return null;
        }

        return new RunParams(flowName, workingDir);
    }

    private static String getWorkingDirectory(PsiElement element) {
        VirtualFile root = ConcordYamlPsiUtils.rootConcordYaml(element);
        if (root == null) {
            return null;
        }

        return root.getParent().getPath();
    }

    @NotNull
    @Override
    public ConfigurationFactory getConfigurationFactory() {
        return ConcordCliRunConfigurationType.getInstance().getConfigurationFactories()[0];
    }

    @Override
    protected boolean setupConfigurationFromContext(@NotNull ConcordCliRunConfiguration configuration, @NotNull ConfigurationContext context, @NotNull Ref<PsiElement> sourceElement) {
        RunParams params = getParams(context);
        if (params == null) {
            return false;
        }

        setupConf(configuration, params);
        return true;
    }

    @Override
    public boolean isConfigurationFromContext(@NotNull ConcordCliRunConfiguration configuration, ConfigurationContext context) {
        String flowName = ConcordYamlPsiUtils.currentFlowName(context.getPsiLocation());
        if (flowName == null) {
            return false;
        }
        return flowName.equals(configuration.getProcessEntryPoint());
    }

    private void setupConf(@NotNull ConcordCliRunConfiguration configuration, RunParams params) {
        configuration.setName(params.flowName());
        configuration.setProcessEntryPoint(params.flowName());
        configuration.setProcessWorkDir(params.workingDir());
    }

    @Nullable
    @Override
    public ConfigurationFromContext createConfigurationFromContext(@NotNull ConfigurationContext context) {
        return getParams(context) != null ? super.createConfigurationFromContext(context) : null;
    }

    private static class RunParams {

        private final String flowName;
        private final String workingDir;

        private RunParams(String flowName, String workingDir) {
            this.flowName = flowName;
            this.workingDir = workingDir;
        }

        public String flowName() {
            return flowName;
        }

        public String workingDir() {
            return workingDir;
        }
    }
}