package brig.concord.psi;

import brig.concord.language.ConcordFileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.indexing.FileBasedIndex;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class FileUtils {

    private FileUtils() {
    }

    @Nullable
    public static VirtualFile getRootYamlDir(Project project, VirtualFile file) {
        return ProjectRootManager.getInstance(project).getFileIndex().getContentRootForFile(file);
    }

    public static List<PsiFile> findFiles(Project project, List<PathMatcher> patterns) {
        List<PsiFile> result = new ArrayList<>();

        Collection<VirtualFile> virtualFiles = FileBasedIndex.getInstance().getContainingFiles(FileTypeIndex.NAME, ConcordFileType.INSTANCE,
                GlobalSearchScope.projectScope(project));

        for (VirtualFile virtualFile : virtualFiles) {
            Path path = virtualFile.getFileSystem().getNioPath(virtualFile);
            if (path == null || path.toString().contains("/target/")) {
                continue;
            }

            if (patterns.stream().anyMatch(m -> m.matches(path))) {
                PsiFile psiFile = PsiManager.getInstance(project).findFile(virtualFile);
                if (psiFile != null) {
                    result.add(psiFile);
                }
            }
        }
        return result;
    }

    public static String fileName(PsiElement element) {
        if (element == null) {
            return null;
        }

        PsiFile fileElement = element.getContainingFile();
        if (fileElement == null) {
            return null;
        }

        return fileElement.getName();
    }
}
