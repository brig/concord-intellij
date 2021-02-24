package brig.concord.codeinsight.action;

import brig.concord.ConcordBundle;
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.YAMLElementGenerator;
import org.jetbrains.yaml.YAMLUtil;
import org.jetbrains.yaml.psi.YAMLKeyValue;

import java.util.Set;

public class CreateMissingPropertiesIntentionAction extends PsiElementBaseIntentionAction {

    private final PsiElement element;
    private final Set<String> missingKeys;

    public CreateMissingPropertiesIntentionAction(Set<String> missingKeys, PsiElement element) {
        this.missingKeys = missingKeys;
        this.element = element;
    }

    @Nls
    @NotNull
    @Override
    public String getFamilyName() {
        return ConcordBundle.message("intention.family.name");
    }

    @NotNull
    @Override
    public String getText() {
        return ConcordBundle.message("intention.create.missing.properties");
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement psiElement) throws IncorrectOperationException {
        YAMLElementGenerator elementGenerator = YAMLElementGenerator.getInstance(project);
        for (String missingKey : missingKeys) {
            YAMLKeyValue newKeyValue = elementGenerator.createYamlKeyValue(missingKey, "");
            element.add(elementGenerator.createEol());
            element.add(elementGenerator.createIndent(YAMLUtil.getIndentToThisElement(element)));
            element.add(newKeyValue);
        }
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement psiElement) {
        return true;
    }
}
