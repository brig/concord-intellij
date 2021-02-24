package brig.concord.run;

import brig.concord.ConcordBundle;
import brig.concord.psi.ConcordYamlPsiUtils;
import com.intellij.execution.lineMarker.ExecutorAction;
import com.intellij.execution.lineMarker.RunLineMarkerContributor;
import com.intellij.icons.AllIcons;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiUtilCore;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.YAMLTokenTypes;
import org.jetbrains.yaml.psi.YAMLKeyValue;

import java.util.List;

public class ConcordCliRunLineMarkerContributor extends RunLineMarkerContributor {

    @Nullable
    @Override
    public Info getInfo(@NotNull PsiElement element) {
        if (!ConcordYamlPsiUtils.isConcordFile(element)) {
            return null;
        }

        if (!(PsiUtilCore.getElementType(element.getNode()) == YAMLTokenTypes.SCALAR_KEY)) {
            return null;
        }

        if (!(element.getParent() instanceof YAMLKeyValue)) {
            return null;
        }

        List<String> keys = ConcordYamlPsiUtils.keys(element.getParent());
        if (keys.size() != 2 || !"flows".equals(keys.get(0))) {
            return null;
        }

        String flowName = keys.get(1);
        if (flowName == null) {
            return null;
        }

        return new Info(AllIcons.RunConfigurations.TestState.Run,
                element1 -> ConcordBundle.message("run.flow.tooltip", flowName),
                ExecutorAction.getActions(0));
    }

}
