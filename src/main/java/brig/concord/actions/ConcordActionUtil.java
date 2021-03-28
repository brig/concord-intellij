package brig.concord.actions;

import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.project.Project;

public final class ConcordActionUtil {

    public static Project getProject(DataContext context) {
        return CommonDataKeys.PROJECT.getData(context);
    }

    private ConcordActionUtil() {
    }
}
