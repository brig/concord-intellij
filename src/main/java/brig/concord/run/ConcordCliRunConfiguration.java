package brig.concord.run;

import brig.concord.ConcordBundle;
import brig.concord.sdk.ConcordSdkType;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.*;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.JDOMExternalizable;
import com.intellij.openapi.util.WriteExternalException;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("rawtypes")
public class ConcordCliRunConfiguration extends LocatableConfigurationBase {

    private CliRunSettings settings = new CliRunSettings();

    protected ConcordCliRunConfiguration(Project project, ConfigurationFactory factory, String name) {
        super(project, factory, name);

        setAllowRunningInParallel(false);

        settings.setProcessEntryPoint("default");
        settings.setProcessSdk(getProjectSdkName(project));
    }

    @Override
    public RunConfiguration clone() {
        ConcordCliRunConfiguration configuration = (ConcordCliRunConfiguration) super.clone();
        configuration.settings = settings.copy();
        return configuration;
    }

    @NotNull
    @Override
    public SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
        return new ConcordCliRunConfigurationSettingsEditor(this);
    }

    @Override
    public void checkConfiguration() throws RuntimeConfigurationException {
        if (getProcessSdk() == null) {
            throw new RuntimeConfigurationException(ConcordBundle.message("dialog.message.sdk.not.specified"),
                    ConcordBundle.message("dialog.title.run.configuration.missing.parameters"));
        }
    }

    @Nullable
    @Override
    public RunProfileState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment environment) {
        return new ConcordCliRunState(this, environment, executor instanceof DefaultDebugExecutor);
    }

    @Override
    public void readExternal(@NotNull Element element) throws InvalidDataException {
        super.readExternal(element);
        settings.readExternal(element);
    }

    @Override
    public void writeExternal(@NotNull Element element) {
        super.writeExternal(element);
        settings.writeExternal(element);
    }

    public String getProcessEntryPoint() {
        return settings.getProcessEntryPoint();
    }

    public void setProcessEntryPoint(String entryPoint) {
        settings.setProcessEntryPoint(entryPoint);
    }

    public List<ProcessArgument> getProcessArguments() {
        return Collections.unmodifiableList(settings.getProcessArguments());
    }

    public void setProcessArguments(List<ProcessArgument> arguments) {
        settings.setProcessArguments(arguments);
    }

    public List<String> getProcessProfiles() {
        return Collections.unmodifiableList(settings.getProcessProfiles());
    }

    public void setProcessProfiles(List<String> profiles) {
        settings.setProcessProfiles(profiles);
    }

    public String getProcessWorkDir() {
        return settings.getProcessWorkdir();
    }

    public void setProcessWorkDir(String dir) {
        settings.setProcessWorkdir(dir);
    }

    public Sdk getProcessSdk() {
        if (settings.getProcessSdk() == null) {
            return null;
        }
        return ProjectJdkTable.getInstance().findJdk(settings.getProcessSdk());
    }

    public void setProcessSdk(Sdk sdk) {
        if (sdk == null) {
            settings.setProcessSdk(null);
        } else {
            settings.setProcessSdk(sdk.getName());
        }
    }

    private static String getProjectSdkName(Project project) {
        Sdk sdk = ProjectRootManager.getInstance(project).getProjectSdk();
        if (ConcordSdkType.isConcordSdk(sdk)) {
            return sdk.getName();
        }
        return null;
    }

    public static class CliRunSettings implements JDOMExternalizable {
        private static final String SETTINGS_KEY = "concord.settings";
        private static final String PROCESS_ARGUMENTS_KEY = "concord.process.arguments";
        private static final String PROCESS_PROFILES_KEY = "concord.process.profiles";
        private static final String PROCESS_WORKDIR_KEY = "concord.process.workdir";
        private static final String PROCESS_SDK_KEY = "concord.process.sdk";
        private static final String PROCESS_ENTRY_POINT_KEY = "concord.process.entry.point";
        private final List<ProcessArgument> processArguments = new ArrayList<>();
        private final List<String> processProfiles = new ArrayList<>();
        private String processEntryPoint;
        private String processWorkdir;
        private String sdk;

        public CliRunSettings() {
        }

        public CliRunSettings(CliRunSettings other) {
            this.processEntryPoint = other.processEntryPoint;
            this.processArguments.addAll(new ArrayList<>(other.processArguments));
            this.processProfiles.addAll(new ArrayList<>(other.processProfiles));
            this.processWorkdir = other.processWorkdir;
            this.sdk = other.sdk;
        }

        @Override
        public void readExternal(Element element) throws InvalidDataException {
            element = element.getChild(SETTINGS_KEY);
            if (element == null) {
                return;
            }

            processEntryPoint = element.getAttributeValue(PROCESS_ENTRY_POINT_KEY);

            processArguments.clear();
            for (Element ae : element.getChildren(PROCESS_ARGUMENTS_KEY)) {
                ProcessArgument arg = new ProcessArgument();
                arg.readExternal(ae);
                processArguments.add(arg);
            }

            processProfiles.clear();
            for (Element pe : element.getChildren(PROCESS_PROFILES_KEY)) {
                processProfiles.add(pe.getAttributeValue("value"));
            }

            processWorkdir = element.getAttributeValue(PROCESS_WORKDIR_KEY);

            sdk = element.getAttributeValue(PROCESS_SDK_KEY);
        }

        @Override
        public void writeExternal(Element element) throws WriteExternalException {
            Element settingsElement = new Element(SETTINGS_KEY);

            if (processEntryPoint != null) {
                settingsElement.setAttribute(PROCESS_ENTRY_POINT_KEY, processEntryPoint);
            }

            for (ProcessArgument arg : processArguments) {
                Element ae = new Element(PROCESS_ARGUMENTS_KEY);
                arg.writeExternal(ae);
                settingsElement.addContent(ae);
            }

            for (String p : processProfiles) {
                Element pe = new Element(PROCESS_PROFILES_KEY);
                pe.setAttribute("value", p);
                settingsElement.addContent(pe);
            }

            if (processWorkdir != null) {
                settingsElement.setAttribute(PROCESS_WORKDIR_KEY, processWorkdir);
            }

            if (sdk != null) {
                settingsElement.setAttribute(PROCESS_SDK_KEY, sdk);
            }

            element.addContent(settingsElement);
        }

        public String getProcessEntryPoint() {
            return processEntryPoint;
        }

        public void setProcessEntryPoint(String processEntryPoint) {
            this.processEntryPoint = processEntryPoint;
        }

        public List<ProcessArgument> getProcessArguments() {
            return processArguments;
        }

        public void setProcessArguments(List<ProcessArgument> arguments) {
            this.processArguments.clear();
            this.processArguments.addAll(arguments);
        }

        public List<String> getProcessProfiles() {
            return processProfiles;
        }

        public void setProcessProfiles(List<String> profiles) {
            this.processProfiles.clear();
            this.processProfiles.addAll(profiles);
        }

        public String getProcessWorkdir() {
            return processWorkdir;
        }

        public void setProcessWorkdir(String processWorkdir) {
            this.processWorkdir = processWorkdir;
        }

        public String getProcessSdk() {
            return sdk;
        }

        public void setProcessSdk(String sdk) {
            this.sdk = sdk;
        }

        public CliRunSettings copy() {
            return new CliRunSettings(this);
        }
    }
}