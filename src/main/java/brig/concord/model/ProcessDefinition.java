package brig.concord.model;

import brig.concord.psi.ConcordYamlPsiUtils;
import brig.concord.psi.FileUtils;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.psi.*;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ProcessDefinition {

    private static final String DEFAULT_CONCORD_RESOURCES = "glob:concord/{**/,}{*.,}concord.yml";

    private final Path rootYamlPath;
    private final YAMLDocument rootDoc;
    private final YAMLDocument doc;

    public ProcessDefinition(Path rootYamlPath, YAMLDocument rootDoc, YAMLDocument doc) {
        this.rootYamlPath = rootYamlPath.getParent();
        this.rootDoc = rootDoc;
        this.doc = doc;
    }

    private static PsiElement flow(PsiElement root, String name) {
        return ConcordYamlPsiUtils.get(root, YAMLPsiElement.class, "flows", name);
    }

    private static Set<String> flowNames(PsiElement root) {
        YAMLMapping flows = ConcordYamlPsiUtils.get(root, YAMLMapping.class, "flows");
        if (flows == null) {
            return Collections.emptySet();
        }
        return ConcordYamlPsiUtils.properties(flows);
    }

    @Nullable
    public PsiElement flow(String name) {
        PsiElement root = flow(doc, name);
        if (root != null) {
            return root;
        }

        // TODO: find latest?
        return allDefinitions().stream()
                .map(d -> flow(d, name))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    @Nullable
    public YAMLSequence resources(String name) {
        return ConcordYamlPsiUtils.get(rootDoc, YAMLSequence.class, "resources", name);
    }

    public List<PathMatcher> resourcePatterns(String name) {
        YAMLSequence element = resources(name);
        if (element == null) {
            return Collections.singletonList(parsePattern(rootYamlPath, DEFAULT_CONCORD_RESOURCES));
        }

        return element.getItems().stream()
                .map(YAMLSequenceItem::getValue)
                .filter(v -> (v instanceof YAMLScalar))
                .map(v -> ((YAMLScalar) v).getTextValue())
                .map(v -> parsePattern(rootYamlPath, v))
                .collect(Collectors.toList());
    }

    public Set<String> flowNames() {
        Set<String> result = new HashSet<>();
        allDefinitions().forEach(c -> result.addAll(flowNames(c)));
        result.addAll(flowNames(doc));
        return result;
    }

    public List<YAMLDocument> allDefinitions() {
        List<PathMatcher> resources = resourcePatterns("concord");
        return Stream.concat(
                FileUtils.findFiles(doc.getProject(), resources).stream()
                        .map(e -> PsiTreeUtil.getChildOfType(e, YAMLDocument.class))
                        .filter(Objects::nonNull),
                Stream.of(doc))
                .collect(Collectors.toList());
    }

    private static PathMatcher parsePattern(Path baseDir, String pattern) {
        String normalizedPattern = null;

        pattern = pattern.trim();

        if (pattern.startsWith("glob:")) {
            normalizedPattern = "glob:" + concat(baseDir, pattern.substring("glob:".length()));
        } else if (pattern.startsWith("regex:")) {
            normalizedPattern = "regex:" + concat(baseDir, pattern.substring("regex:".length()));
        }

        if (normalizedPattern != null) {
            return FileSystems.getDefault().getPathMatcher(normalizedPattern);
        }

        return null;
    }

    private static String concat(Path path, String str) {
        String separator = "/";
        if (str.startsWith("/")) {
            separator = "";
        }
        return path.toAbsolutePath() + separator + str;
    }
}
