package brig.concord.completion;

import com.intellij.application.options.CodeStyle;
import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CodeStyleSettingsManager;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import org.jetbrains.annotations.NotNull;

public final class CompletionUtils {

    private CompletionUtils() {
    }

    public static void formatInsertedString(@NotNull InsertionContext context, int offset) {
        Project project = context.getProject();
        PsiDocumentManager.getInstance(project).commitDocument(context.getDocument());
        CodeStyleManager codeStyleManager = CodeStyleManager.getInstance(project);
        codeStyleManager.reformatText(context.getFile(), context.getStartOffset(), context.getTailOffset() + offset);
    }

    public static String indent(PsiFile file) {
        CodeStyleSettings currentSettings = CodeStyle.getSettings(file);
        CommonCodeStyleSettings.IndentOptions indentOptions = currentSettings.getIndentOptionsByFile(file);
        return indentOptions.USE_TAB_CHARACTER ? "\t" : StringUtil.repeatSymbol(' ', indentOptions.INDENT_SIZE);
    }

    public static String indent(InsertionContext context) {
        CodeStyleSettings currentSettings = CodeStyleSettingsManager.getSettings(context.getProject());
        CommonCodeStyleSettings.IndentOptions indentOptions = currentSettings.getIndentOptions(context.getFile().getFileType());
        return indentOptions.USE_TAB_CHARACTER ? "\t" : StringUtil.repeatSymbol(' ', indentOptions.INDENT_SIZE);
    }

    public static String currentLineContent(Editor editor) {
        int offset = editor.getCaretModel().getOffset();
        int lineNumber = editor.getDocument().getLineNumber(offset);
        int lineEndOffset = editor.getDocument().getLineEndOffset(lineNumber);
        if (lineEndOffset == offset) {
            int lineStartOffset = editor.getDocument().getLineStartOffset(lineNumber);
            return editor.getDocument().getText(TextRange.create(lineStartOffset, lineEndOffset));
        }

        return null;
    }

    public static String contentIndent(String content) {
        if (content == null) {
            return "";
        }
        int offsetOfContent = content.length() - StringUtil.trimLeading(content).length();
        return content.substring(0, offsetOfContent);
    }
}
