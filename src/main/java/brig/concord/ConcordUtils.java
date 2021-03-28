package brig.concord;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.Function;

public final class ConcordUtils {

    public static final String NOTIFICATION_GROUP = "Concord";

    public static <T> T computeInBackground(Project project, String title, Function<? super ProgressIndicator, ? extends T> task) {
        return ProgressManager.getInstance().run(new Task.WithResult<>(project, title, true) {

            @Override
            protected T compute(@NotNull ProgressIndicator indicator) {
                return task.apply(indicator);
            }
        });
    }

    public static void runInBackground(Project project, String title, Consumer<ProgressIndicator> task) throws Exception {
        ProgressManager.getInstance().run(new Task.Backgroundable(project, title, true) {

            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                task.accept(indicator);
            }
        });
    }

    private ConcordUtils() {
    }
}
