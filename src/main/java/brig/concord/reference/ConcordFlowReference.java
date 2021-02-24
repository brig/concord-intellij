package brig.concord.reference;

import brig.concord.model.ProcessDefinition;
import brig.concord.model.ProcessDefinitionProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReferenceBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.psi.YAMLScalar;

public class ConcordFlowReference extends PsiReferenceBase<YAMLScalar> {

    public ConcordFlowReference(@NotNull YAMLScalar flowNameElement) {
        super(flowNameElement, true);
    }

    @Override
    public @Nullable PsiElement resolve() {
        ProcessDefinition process = ProcessDefinitionProvider.getInstance().get(getElement());
        if (process == null) {
            return null;
        }
        return process.flow(getElement().getTextValue());
    }
}
