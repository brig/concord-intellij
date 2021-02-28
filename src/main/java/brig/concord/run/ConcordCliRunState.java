package brig.concord.run;

import brig.concord.ConcordBundle;
import com.intellij.execution.CantRunException;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.CommandLineState;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.process.CapturingProcessHandler;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.projectRoots.JavaSdk;
import com.intellij.openapi.projectRoots.JdkUtil;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.ObjectUtils;
import org.jetbrains.annotations.NotNull;

public class ConcordCliRunState extends CommandLineState {

    private final ConcordCliRunConfiguration runConfiguration;
    private final boolean debug;

    public ConcordCliRunState(ConcordCliRunConfiguration runConfiguration,
                              ExecutionEnvironment env,
                              boolean debug) {
        super(env);
        this.runConfiguration = runConfiguration;
        this.debug = debug;
    }

    @NotNull
    @Override
    protected ProcessHandler startProcess() throws ExecutionException {
        ConcordCliRunConfiguration cfg = (ConcordCliRunConfiguration) getEnvironment().getRunProfile();

        Sdk sdk = assertSdk(cfg);
        Sdk jre = assertJre(cfg);

        String executable = sdk.getHomePath() + "/" + "concord-cli.jar";
        GeneralCommandLine commandLine = new GeneralCommandLine()
                .withParentEnvironmentType(GeneralCommandLine.ParentEnvironmentType.CONSOLE)
                .withExePath(JavaSdk.getInstance().getVMExecutablePath(jre))
                .withParameters(cfg.getVmOptionsList().getParameters())
                .withParameters("-jar")
                .withParameters(executable)
                .withParameters("run")
                .withParameters(cfg.getSdkOptionsList().getParameters());

        for (String profile : cfg.getProcessProfiles()) {
            commandLine.addParameters("-p", profile);
        }

        for (ProcessArgument arg : cfg.getProcessArguments()) {
            commandLine.addParameters("-e", arg.getName() + "=" + arg.getValue());
        }

        if (cfg.getProcessEntryPoint() != null) {
            commandLine.addParameters("--entry-point", cfg.getProcessEntryPoint());
        }

        String basePath = assertWorkingDirectory(cfg);
        commandLine.setWorkDirectory(basePath);

        ProcessHandler handler = new CapturingProcessHandler(commandLine.createProcess(), commandLine.getCharset(), commandLine.getCommandLineString());
        setConsoleBuilder(TextConsoleBuilderFactory.getInstance().createBuilder(runConfiguration.getProject())
                .filters(new ConcordConsoleErrorFileLinkFilter(cfg.getProject(), basePath)));
        return handler;
    }

    private String assertWorkingDirectory(ConcordCliRunConfiguration cfg) throws ExecutionException {
        String workDir = cfg.getProcessWorkDir();
        if (StringUtil.isEmptyOrSpaces(workDir)) {
            throw new ExecutionException(ConcordBundle.message("dialog.message.workdir.not.specified"));
        }
        return workDir;
    }

    private static Sdk assertSdk(ConcordCliRunConfiguration cfg) throws ExecutionException {
        Sdk sdk = cfg.getProcessSdk();
        if (sdk == null) {
            throw new ExecutionException(ConcordBundle.message("dialog.message.sdk.not.specified"));
        }
        return sdk;
    }

    private static Sdk assertJre(ConcordCliRunConfiguration cfg) throws ExecutionException {
        String jreHomeOrName = cfg.getJrePathOrName();
        if (StringUtil.isEmptyOrSpaces(jreHomeOrName)) {
            throw new ExecutionException(ConcordBundle.message("dialog.message.jre.not.specified"));
        }

        Sdk jre = ProjectJdkTable.getInstance().findJdk(jreHomeOrName);
        if (jre != null) {
            return jre;
        }

        if (JdkUtil.checkForJre(jreHomeOrName)) {
            final JavaSdk javaSdk = JavaSdk.getInstance();
            return javaSdk.createJdk(ObjectUtils.notNull(javaSdk.getVersionString(jreHomeOrName), ""), jreHomeOrName);
        }

        throw new CantRunException(ConcordBundle.message("jre.path.is.not.valid.jre.home.error.message", jreHomeOrName));
    }
}
