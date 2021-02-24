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

import com.intellij.execution.filters.Filter;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConcordConsoleErrorFileLinkFilter implements Filter {

    private static final Pattern FILE_ERROR_PATH_REGEXP = Pattern.compile(".*\\((.*)\\): Error @ line: (\\d+), col: (\\d+).*");

    private final @NotNull Project myProject;
    private final String basePath;

    public ConcordConsoleErrorFileLinkFilter(@NotNull Project project, String basePath) {
        myProject = project;
        this.basePath = basePath;
    }

    @Override
    public @Nullable Result applyFilter(@NotNull String textLine, int endOffset) {
        if (StringUtil.isEmpty(textLine)) {
            return null;
        }

        Matcher matcher = FILE_ERROR_PATH_REGEXP.matcher(textLine);
        if (!matcher.find()) {
            return null;
        }

        int lineStartOffset = endOffset - textLine.length();
        int fileStartOffset = matcher.start(1);
        int lineNumberStartOffset = matcher.start(2);
        int lineNumberEndOffset = matcher.end(2);
        String filePath = basePath + "/" + matcher.group(1);
        int line;
        try {
            line = Integer.parseInt(matcher.group(2)) - 1;
        } catch (NumberFormatException e) {
            line = 0;
        }

        return new Result(
                lineStartOffset + /*fileStartOffset*/lineNumberStartOffset - "line: ".length(),
                lineStartOffset + lineNumberEndOffset,
                new ConcordFileHyperlinkInfo(myProject, line, filePath));
    }
}
