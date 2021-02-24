package brig.concord.codeinsight.action;

import brig.concord.ConcordBundle;
import brig.concord.psi.ConcordYamlPsiUtils;
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.psi.YAMLKeyValue;

public class DeletePropertyIntentionAction extends PsiElementBaseIntentionAction {

    private static void removeEmptyLine(Editor editor) {
        int offset = editor.getCaretModel().getOffset();
        int lineNumber = editor.getDocument().getLineNumber(offset);
        int lineStartOffset = editor.getDocument().getLineStartOffset(lineNumber);
        int lineEndOffset = editor.getDocument().getLineEndOffset(lineNumber);
        String lineContent = editor.getDocument().getText(TextRange.create(lineStartOffset, lineEndOffset));
        if ("".equals(lineContent.trim())) {
            int endIndex = editor.getDocument().getTextLength() > lineEndOffset ? lineEndOffset + 1 : lineEndOffset;
            editor.getDocument().deleteString(lineStartOffset, endIndex);
        }
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
        return ConcordBundle.message("intention.delete.property");
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element) throws IncorrectOperationException {
        PsiElement kv = ConcordYamlPsiUtils.getParentYamlOfType(element, YAMLKeyValue.class, false);
        if (kv == null) {
            return;
        }

        kv.delete();
        PsiDocumentManager.getInstance(project).doPostponedOperationsAndUnblockDocument(editor.getDocument());
        removeEmptyLine(editor);
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement psiElement) {
        return true;
    }
}
