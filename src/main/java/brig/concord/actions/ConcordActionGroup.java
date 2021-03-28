package brig.concord.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.DumbAware;
import org.jetbrains.annotations.NotNull;

public class ConcordActionGroup extends DefaultActionGroup implements DumbAware {

    @Override
    public void update(@NotNull AnActionEvent event) {
        super.update(event);
        boolean available = isAvailable(event);
        event.getPresentation().setEnabledAndVisible(available);
    }

    protected boolean isAvailable(@NotNull AnActionEvent event) {
        return ConcordActionUtil.getProject(event.getDataContext()) != null;
    }
}
