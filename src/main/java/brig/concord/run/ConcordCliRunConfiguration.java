package brig.concord.run;

import brig.concord.ConcordBundle;
import brig.concord.sdk.ConcordSdkType;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.*;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.util.JavaParametersUtil;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.JavaSdk;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.impl.JavaSdkImpl;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.JDOMExternalizable;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Predicate;

@SuppressWarnings("rawtypes")
public class ConcordCliRunConfiguration extends LocatableConfigurationBase {

    private CliRunSettings settings = new CliRunSettings();

    protected ConcordCliRunConfiguration(Project project, ConfigurationFactory factory, String name) {
        super(project, factory, name);

        setAllowRunningInParallel(false);

        settings.setProcessEntryPoint("default");
        settings.setVmOptions("-Xmx128m -client");
    }

    @Override
    public RunConfiguration clone() {
        ConcordCliRunConfiguration configuration = (ConcordCliRunConfiguration) super.clone();
        configuration.settings = settings.copy();
        return configuration;
    }

    @Override
    public void onNewConfigurationCreated() {
        super.onNewConfigurationCreated();

        if (StringUtil.isEmptyOrSpaces(getProcessWorkDir())) {
            settings.setProcessWorkdir(FileUtil.toSystemIndependentName(StringUtil.notNullize(getProject().getBasePath())));
        }

        if (StringUtil.isEmptyOrSpaces(settings.getProcessSdk())) {
            settings.setProcessSdk(defaultSdk(getProject()).map(Sdk::getName).orElse(null));
        }

        if (StringUtil.isEmptyOrSpaces(getJrePathOrName())) {
            settings.setJrePathOrName(defaultJre(getProject()).map(Sdk::getName).orElse(null));
        }
    }

    @NotNull
    @Override
    public SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
        return new ConcordCliRunConfigurationSettingsEditor(this);
    }

    @Override
    public void checkConfiguration() throws RuntimeConfigurationException {
        checkEntryPoint();
        checkWorkingDirectory();
        checkSdk();
        JavaParametersUtil.checkAlternativeJRE(getJrePathOrName());
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

    public String getSdkOptions() {
        return settings.getSdkOptions();
    }

    public void setSdkOptions(String options) {
        settings.setSdkOptions(options);
    }

    public ParametersList getSdkOptionsList() {
        ParametersList result = new ParametersList();
        result.addParametersString(settings.getSdkOptions());
        return result;
    }

    public String getJrePathOrName() {
        return settings.getJrePathOrName();
    }

    public void setJrePath(String path) {
        this.settings.setJrePathOrName(path);
    }

    public String getVmOptions() {
        return settings.getVmOptions();
    }

    public void setVmOptions(String parameters) {
        settings.setVmOptions(parameters);
    }

    public ParametersList getVmOptionsList() {
        ParametersList result = new ParametersList();
        result.addParametersString(settings.getVmOptions());
        return result;
    }

    public static Optional<Sdk> defaultSdk(Project project) {
        return findSdk(project, ConcordSdkType::isConcordSdk);
    }

    public static Optional<Sdk> defaultJre(Project project) {
        JavaSdk javaSdkType = JavaSdkImpl.getInstance();
        return findSdk(project, sdk -> sdk.getSdkType() == javaSdkType);
    }

    public static Optional<Sdk> findSdk(Project project, Predicate<Sdk> sdkFilter) {
        Sdk sdk = ProjectRootManager.getInstance(project).getProjectSdk();
        if (sdk != null && sdkFilter.test(sdk)) {
            return Optional.of(sdk);
        }

        return Arrays.stream(ProjectJdkTable.getInstance().getAllJdks())
                .filter(sdkFilter)
                .findFirst();
    }

    private void checkEntryPoint() throws RuntimeConfigurationWarning {
        String entryPoint = getProcessEntryPoint();
        if (StringUtil.isEmptyOrSpaces(entryPoint)) {
            throw new RuntimeConfigurationWarning(ConcordBundle.message("dialog.message.process.entry.point.not.specified"));
        }
    }

    private void checkWorkingDirectory() throws RuntimeConfigurationException {
        String workingDir = getProcessWorkDir();
        if (StringUtil.isEmptyOrSpaces(workingDir)) {
            throw new RuntimeConfigurationWarning(ConcordBundle.message("dialog.message.workdir.not.specified"));
        }

        boolean exists;
        try {
            exists = Files.exists(Paths.get(workingDir));
        }
        catch (InvalidPathException e) {
            exists = false;
        }

        if (!exists) {
            throw new RuntimeConfigurationWarning(ConcordBundle.message("dialog.message.working.directory.doesn.t.exist", workingDir));
        }
    }

    private void checkSdk() throws RuntimeConfigurationException {
        if (getProcessSdk() == null) {
            throw new RuntimeConfigurationWarning(ConcordBundle.message("dialog.message.sdk.not.specified"));
        }
    }

    public static class CliRunSettings implements JDOMExternalizable {
        private static final String SETTINGS_KEY = "concord.settings";
        private static final String PROCESS_ENTRY_POINT_KEY = "concord.process.entry.point";
        private static final String PROCESS_ARGUMENTS_KEY = "concord.process.arguments";
        private static final String PROCESS_PROFILES_KEY = "concord.process.profiles";
        private static final String PROCESS_WORKDIR_KEY = "concord.process.workdir";
        private static final String PROCESS_SDK_KEY = "concord.process.sdk";
        private static final String PROCESS_SDK_OPTIONS_KEY = "concord.sdk.options";
        private static final String PROCESS_JRE_PATH_KEY = "concord.sdk.jre";
        private static final String VM_OPTION_KEY = "concord.vm.options";
        private final List<ProcessArgument> processArguments = new ArrayList<>();
        private final List<String> processProfiles = new ArrayList<>();
        private String processEntryPoint;
        private String processWorkdir;
        private String sdk;
        private String sdkOptions;
        private String jrePathOrName;
        private String vmOptions;

        public CliRunSettings() {
        }

        public CliRunSettings(CliRunSettings other) {
            this.processEntryPoint = other.processEntryPoint;
            this.processArguments.addAll(new ArrayList<>(other.processArguments));
            this.processProfiles.addAll(new ArrayList<>(other.processProfiles));
            this.processWorkdir = other.processWorkdir;
            this.sdk = other.sdk;
            this.sdkOptions = other.sdkOptions;
            this.jrePathOrName = other.jrePathOrName;
            this.vmOptions = other.vmOptions;
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
            sdkOptions = element.getAttributeValue(PROCESS_SDK_OPTIONS_KEY);

            jrePathOrName = element.getAttributeValue(PROCESS_JRE_PATH_KEY);
            vmOptions = element.getAttributeValue(VM_OPTION_KEY);
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

            if (sdkOptions != null) {
                settingsElement.setAttribute(PROCESS_SDK_OPTIONS_KEY, sdkOptions);
            }

            if (jrePathOrName != null) {
                settingsElement.setAttribute(PROCESS_JRE_PATH_KEY, jrePathOrName);
            }

            if (vmOptions != null) {
                settingsElement.setAttribute(VM_OPTION_KEY, vmOptions);
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

        public String getSdkOptions() {
            return this.sdkOptions;
        }

        public void setSdkOptions(String options) {
            this.sdkOptions = options;
        }

        public String getJrePathOrName() {
            return jrePathOrName;
        }

        public void setJrePathOrName(String pathOrName) {
            this.jrePathOrName = pathOrName;
        }

        public String getVmOptions() {
            return vmOptions;
        }

        public void setVmOptions(String vmOptions) {
            this.vmOptions = vmOptions;
        }

        public CliRunSettings copy() {
            return new CliRunSettings(this);
        }
    }
}