package brig.concord.run;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionManager;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.configurations.RunnerSettings;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.runners.DefaultProgramRunnerKt;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import org.jetbrains.annotations.NotNull;

public class ConcordCliRunner implements ProgramRunner<RunnerSettings> {

    @NotNull
    public String getRunnerId() {
        return "Concord CLI Runner";
    }

    @Override
    public void execute(@NotNull ExecutionEnvironment environment) throws ExecutionException {
        FileDocumentManager.getInstance().saveAllDocuments();
        ExecutionManager.getInstance(environment.getProject()).startRunProfile(
                environment, state -> DefaultProgramRunnerKt.executeState(state, environment, this));
    }

    @Override
    public boolean canRun(@NotNull String executorId, @NotNull RunProfile profile) {
        return (profile instanceof ConcordCliRunConfiguration) && executorId.equals(DefaultRunExecutor.EXECUTOR_ID);
    }
}
