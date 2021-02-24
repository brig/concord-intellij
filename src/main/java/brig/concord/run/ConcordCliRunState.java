package brig.concord.run;

import brig.concord.ConcordBundle;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.CommandLineState;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.process.CapturingProcessHandler;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.util.text.StringUtil;
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
        GeneralCommandLine commandLine = new GeneralCommandLine();

        Sdk sdk = cfg.getProcessSdk();
        if (sdk == null) {
            throw new ExecutionException(ConcordBundle.message("dialog.message.sdk.not.specified"));
        }

        String executable = sdk.getHomePath() + "/" + "concord-cli.jar";

        commandLine.setExePath(executable);
        commandLine.addParameter("run");

        for (String profile : cfg.getProcessProfiles()) {
            commandLine.addParameters("-p", profile);
        }

        for (ProcessArgument arg : cfg.getProcessArguments()) {
            commandLine.addParameters("-e", arg.getName() + "=" + arg.getValue());
        }

        if (cfg.getProcessEntryPoint() != null) {
            commandLine.addParameters("--entry-point", cfg.getProcessEntryPoint());
        }

        String basePath = StringUtil.defaultIfEmpty(cfg.getProcessWorkDir(), cfg.getProject().getBasePath());
        commandLine.setWorkDirectory(basePath);

        ProcessHandler handler = new CapturingProcessHandler(commandLine.createProcess(), commandLine.getCharset(), commandLine.getCommandLineString());
        setConsoleBuilder(TextConsoleBuilderFactory.getInstance().createBuilder(runConfiguration.getProject())
                .filters(new ConcordConsoleErrorFileLinkFilter(cfg.getProject(), basePath)));
        return handler;
    }
}
