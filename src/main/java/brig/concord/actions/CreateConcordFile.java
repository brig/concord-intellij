package brig.concord.actions;

import brig.concord.ConcordBundle;
import brig.concord.language.ConcordIcons;
import com.intellij.ide.actions.CreateFileFromTemplateAction;
import com.intellij.ide.actions.CreateFileFromTemplateDialog;
import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class CreateConcordFile extends CreateFileFromTemplateAction implements DumbAware {

    public CreateConcordFile() {
        super(
                ConcordBundle.messagePointer("action.new.file.name"),
                ConcordBundle.messagePointer("action.new.file.description"),
                ConcordIcons.FILE);
    }

    @Override
    protected void buildDialog(@NotNull Project project, @NotNull PsiDirectory directory, CreateFileFromTemplateDialog.@NotNull Builder builder) {
        builder
                .setTitle(ConcordBundle.message("action.new.file.dialog.title"))
                .addKind("Concord file", ConcordIcons.FILE, "concord.yml");
    }

    @Override
    protected String getActionName(PsiDirectory directory, @NonNls @NotNull String newName, @NonNls String templateName) {
        return "Create Concord flow " + newName;
    }

    @Override
    protected PsiFile createFileFromTemplate(String name, FileTemplate template, PsiDirectory dir) {
//        if (ProjectRootsUtil.isModuleContentRoot(dir)) {
//             filename concord.yml or .concord.yml
//        } else {
//             filename ends with .concord.yml
//        }
        return super.createFileFromTemplate(name, template, dir);
    }
}
