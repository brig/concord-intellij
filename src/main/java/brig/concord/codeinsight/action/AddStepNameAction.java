/*
 * Copyright 2000-2009 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package brig.concord.codeinsight.action;

import brig.concord.ConcordBundle;
import brig.concord.completion.CombinedSchemaCompletionMatcherVisitor;
import brig.concord.log.Logger;
import brig.concord.model.Schema;
import brig.concord.model.SchemaMatcherVisitor;
import brig.concord.model.SchemaProvider;
import brig.concord.psi.ConcordYamlPsiUtils;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.YAMLElementGenerator;
import org.jetbrains.yaml.psi.YAMLMapping;
import org.jetbrains.yaml.psi.impl.YAMLBlockMappingImpl;

public class AddStepNameAction implements IntentionAction {

    private static final String NAME_PLACEHOLDER = "step name";

    @Override
    @NotNull
    public String getText() {
        return ConcordBundle.message("intention.add.step.name.text");
    }

    @Override
    @NotNull
    public String getFamilyName() {
        return ConcordBundle.message("intention.family.name");
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
        if (!ConcordYamlPsiUtils.isConcordFile(file)) {
            return false;
        }

        if (editor == null) {
            return false;
        }

        int offset = editor.getCaretModel().getOffset();
        PsiElement element = file.findElementAt(offset);
        if (element == null) {
            return false;
        }

        element = element.getParent();
        if (element == null) {
            return false;
        }
        element = element.getParent();
        if (!(element instanceof YAMLMapping)) {
            return false;
        }

        boolean alreadyHasName = ((YAMLMapping)element).getKeyValues().stream()
                .anyMatch(kv -> "name".equals(kv.getKeyText()));
        if (alreadyHasName) {
            return false;
        }

        Schema schema = ConcordYamlPsiUtils.schema(SchemaProvider.INSTANCE,
                p -> new SchemaMatcherVisitor(p, new CombinedSchemaCompletionMatcherVisitor(p)),
                element);
        if (schema == null) {
            return false;
        }


        return SchemaProvider.TASK_DEF_ID.equals(schema.id());
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, PsiFile file) throws IncorrectOperationException {
        if (editor == null) {
            return;
        }

        int offset = editor.getCaretModel().getOffset();
        PsiElement element = file.findElementAt(offset);
        if (element == null) {
            return;
        }

        YAMLBlockMappingImpl mapping = ConcordYamlPsiUtils.getParentOfType(element, YAMLBlockMappingImpl.class, true);
        if (mapping == null) {
            return;
        }

        YAMLElementGenerator elementGenerator = YAMLElementGenerator.getInstance(project);
        mapping.insertKeyValueAtOffset(elementGenerator.createYamlKeyValue("name", "step name"), -1);

        editor.getCaretModel().moveToOffset(mapping.getTextOffset() + "name: ".length());
        int start = editor.getCaretModel().getCurrentCaret().getOffset();
        SelectionModel model = editor.getSelectionModel();
        model.setSelection(start, start + NAME_PLACEHOLDER.length());
    }


    @Override
    public boolean startInWriteAction() {
        return true;
    }
}
