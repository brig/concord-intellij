package brig.concord.smart;

import brig.concord.completion.CompletionUtils;
import brig.concord.model.*;
import brig.concord.psi.ConcordYamlPsiUtils;
import com.intellij.codeInsight.editorActions.enter.EnterHandlerDelegateAdapter;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorModificationUtil;
import com.intellij.openapi.editor.actionSystem.EditorActionHandler;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ConcordYAMLEnterHandler extends EnterHandlerDelegateAdapter {

    @Override
    public Result preprocessEnter(@NotNull PsiFile file,
                                  @NotNull Editor editor,
                                  @NotNull Ref<Integer> caretOffset,
                                  @NotNull Ref<Integer> caretAdvance,
                                  @NotNull DataContext dataContext,
                                  EditorActionHandler originalHandler) {

        if (!ConcordYamlPsiUtils.isConcordFile(file)) {
            return Result.Continue;
        }

//        return Result.DefaultForceIndent;

        int offset = caretOffset.get();
        if (offset <= 0) {
            return Result.Continue;
        }

        PsiElement element = file.findElementAt(offset - 1);
        if (element == null) {
            return Result.Continue;
        }

        List<Schema> schemas = ConcordYamlPsiUtils.schemas(SchemaProvider.INSTANCE, element);
        if (schemas.isEmpty()) {
            return Result.DefaultForceIndent;
        }

        String lineContent = CompletionUtils.currentLineContent(editor);
        Schema currentSchema = schemas.get(schemas.size() - 1);
        IndentVisitor visitor = new IndentVisitor(schemas, lineContent);
        visitor.visit(currentSchema);
        String lineIndent = visitor.indent();


//        String lineContent = CompletionUtils.currentLineContent(editor);
//        String lineIndent = null;
//        Schema parentSchema = parentSchema(element);
//        if (parentSchema != null) {
//            IndentVisitor visitor = new IndentVisitor(lineContent);
//            visitor.visit(parentSchema);
//            lineIndent = visitor.indent();
//        }

        if (lineIndent == null) {
            lineIndent = CompletionUtils.contentIndent(lineContent);
        }

//        Logger.info("parent: {}, {}", element.getParent().getClass(), element.getParent().getText());
//        Logger.info("schema -> {}", ConcordYamlPsiUtils.schemas(SchemaProvider.INSTANCE, element));

        EditorModificationUtil.insertStringAtCaret(editor, "\n" + lineIndent);
        return Result.Stop;
    }

    @Override
    public Result postProcessEnter(@NotNull PsiFile file, @NotNull Editor editor, @NotNull DataContext dataContext) {
        if (!ConcordYamlPsiUtils.isConcordFile(file)) {
            return Result.Continue;
        }
        int caretOffset = editor.getCaretModel().getOffset();
        PsiElement element = file.findElementAt(caretOffset - 1);

        return Result.Stop;
    }

    static class IndentVisitor extends AbstractVisitor {

        private final List<Schema> schemas;
        private final String currentLineContent;

        private String indent;

        public IndentVisitor(List<Schema> schemas, String currentLineContent) {
            this.schemas = schemas;
            this.currentLineContent = currentLineContent;
        }

        private static Schema parentSchema(List<Schema> schemas) {
            if (schemas.size() > 1) {
                return schemas.get(schemas.size() - 2);
            }
            return null;
        }

        public String indent() {
            return indent;
        }

        @Override
        protected void visitArraySchema(ArraySchema schema) {
            this.indent = CompletionUtils.contentIndent(currentLineContent) + "- ";
        }

        @Override
        protected void visitStringSchema(StringSchema schema) {
            Schema parentSchema = parentSchema(schemas);
            if (parentSchema != null) {
                IndentVisitor visitor = new IndentVisitor(schemas, currentLineContent);
                visitor.visit(parentSchema);
                this.indent = visitor.indent();
            }
        }
    }
}
