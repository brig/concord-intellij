/*
 * Copyright 2015-2020 Alexandr Evstigneev
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

package brig.concord.run;

import com.intellij.execution.filters.FileHyperlinkInfoBase;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

class ConcordFileHyperlinkInfo extends FileHyperlinkInfoBase {

    private final @NotNull String myFilePath;

    private final int myLine;

    public ConcordFileHyperlinkInfo(Project project, int documentLine, @NotNull String filePath) {
        super(project, documentLine, 0);
        myFilePath = filePath;
        myLine = documentLine;
    }

    @Override
    protected @Nullable VirtualFile getVirtualFile() {
        return VfsUtil.findFileByIoFile(new File(myFilePath), false);
    }

    @Override
    public String toString() {
        return "line " + myLine + " in " + myFilePath;
    }
}
