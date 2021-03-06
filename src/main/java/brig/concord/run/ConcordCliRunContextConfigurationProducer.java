package brig.concord.run;

import brig.concord.psi.ConcordYamlPsiUtils;
import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.actions.ConfigurationFromContext;
import com.intellij.execution.actions.LazyRunConfigurationProducer;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.openapi.projectRoots.Sdk;
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

        Sdk sdk = ConcordCliRunConfiguration.defaultSdk(context.getProject()).orElse(null);
        String jre = ConcordCliRunConfiguration.defaultJre(context.getProject()).map(Sdk::getName).orElse(null);
        return new RunParams(flowName, workingDir, sdk, jre);
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
        configuration.setProcessSdk(params.sdk());
        configuration.setJrePath(params.jre());
    }

    @Nullable
    @Override
    public ConfigurationFromContext createConfigurationFromContext(@NotNull ConfigurationContext context) {
        return getParams(context) != null ? super.createConfigurationFromContext(context) : null;
    }

    private static class RunParams {

        private final String flowName;
        private final String workingDir;
        private final Sdk sdk;
        private final String jre;

        private RunParams(String flowName, String workingDir, Sdk sdk, String jre) {
            this.flowName = flowName;
            this.workingDir = workingDir;
            this.sdk = sdk;
            this.jre = jre;
        }

        public String flowName() {
            return flowName;
        }

        public String workingDir() {
            return workingDir;
        }

        public Sdk sdk() {
            return sdk;
        }

        public String jre() {
            return jre;
        }
    }
}