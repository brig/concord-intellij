package brig.concord.psi;

import brig.concord.model.Schema;
import brig.concord.model.SchemaMatcherVisitor;
import brig.concord.model.SchemaProvider;
import com.intellij.codeInsight.completion.CompletionUtil;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ObjectUtils;
import com.intellij.util.SmartList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.YAMLFileType;
import org.jetbrains.yaml.psi.*;

import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class ConcordYamlPsiUtils {

    private static final String ROOT_FILE_NAME_PATTER = "^\\.?concord\\.yml$";

    private static final String DEFAULT_FILE_NAME_PATTERN = "^(.*\\.)?concord\\.yml$";

    private static final Set<Class<? extends YAMLPsiElement>> SCHEMA_YAML_ELEMENTS = createSchemaYamlElements();

    private static Set<Class<? extends YAMLPsiElement>> createSchemaYamlElements() {
        return new HashSet<>(Arrays.asList(
                YAMLDocument.class, YAMLMapping.class, YAMLKeyValue.class,
                YAMLSequence.class, YAMLSequenceItem.class, YAMLScalar.class
        ));
    }

    public static boolean isConcordFile(PsiElement element) {
        if (element == null) {
            return false;
        }

        PsiFile file = element.getContainingFile();
        if (file == null) {
            return false;
        }
        // TODO: check concord.yml override
        return file.getFileType() == YAMLFileType.YML && file.getName().matches(DEFAULT_FILE_NAME_PATTERN);
    }

    public static YAMLDocument getDocument(PsiElement element) {
        if (element == null) {
            return null;
        }

        YAMLFile file = ConcordYamlPsiUtils.getParentOfType(element, YAMLFile.class, true);
        if (file == null) {
            return null;
        }

        List<YAMLDocument> rootDocs = file.getDocuments();
        if (rootDocs.isEmpty()) {
            return null;
        }

        return rootDocs.get(0);
    }

    public static VirtualFile rootConcordYaml(PsiElement element) {
        if (element == null || element.getContainingFile() == null) {
            ;
            return null;
        }

//        if (isRootConcordFile(element)) {
//            return element.getContainingFile().getVirtualFile();
//        }

//        Module module = ModuleUtil.findModuleForPsiElement(element);
//        if (module == null) {
//            return null;
//        }
        VirtualFile elementFile = element.getContainingFile().getVirtualFile();
        if (elementFile == null) {
            return null;
        }

        VirtualFile rootDir = ProjectRootManager.getInstance(element.getProject()).getFileIndex().getContentRootForFile(elementFile);
        if (rootDir == null) {
            return null;
        }

        Path rootPath = rootDir.toNioPath();
        VirtualFile root = VfsUtil.findFile(rootPath.resolve("concord.yml"), false);
        if (root == null) {
            root = VfsUtil.findFile(rootPath.resolve(".concord.yml"), false);
        }

        return root;
    }

    public static List<YAMLPsiElement> path(PsiElement element) {
        List<YAMLPsiElement> path = new SmartList<>();
        while (element != null) {
            if (element instanceof PsiFile) {
                break;
            }
            if (isInstance(element, SCHEMA_YAML_ELEMENTS)) {
                path.add((YAMLPsiElement) element);
            }
            element = element.getParent();
        }

        Collections.reverse(path);

        return path;
    }

    public static Schema schema(SchemaProvider provider, PsiElement element) {
        return schema(provider, SchemaMatcherVisitor::new, element);
    }

    public static Schema schema(SchemaProvider provider,
                                Function<YAMLPsiElement, SchemaMatcherVisitor> matcherFactory,
                                PsiElement element) {

        List<YAMLPsiElement> path = path(element);

        Schema schema = provider.get();
        for (YAMLPsiElement p : path) {
            SchemaMatcherVisitor visitor = matcherFactory.apply(p);
            visitor.visit(schema);

            schema = visitor.schema();
            if (schema == null) {
                break;
            }
        }
        return schema;
    }

    public static List<Schema> schemas(SchemaProvider provider, PsiElement element) {
        List<YAMLPsiElement> path = path(element);

        Set<Schema> schemas = new HashSet<>();
        List<Schema> result = new ArrayList<>();

        Schema schema = provider.get();
        for (YAMLPsiElement p : path) {
            SchemaMatcherVisitor visitor = new SchemaMatcherVisitor(p);
            visitor.visit(schema);

            schema = visitor.schema();
            if (schema == null) {
                return Collections.emptyList();
            }
            boolean newSchema = schemas.add(schema);
            if (newSchema) {
                result.add(schema);
            }
        }
        return result;
    }

    public static <T extends YAMLPsiElement> T getParentOf(SchemaProvider provider, @Nullable PsiElement element, String schemaId, Class<T> clazz) {
        if (element == null) {
            return null;
        }

        T result = null;
        Schema schema = provider.get();
        List<YAMLPsiElement> path = path(element);
        for (YAMLPsiElement p : path) {
            SchemaMatcherVisitor visitor = new SchemaMatcherVisitor(p);
            visitor.visit(schema);

            schema = visitor.schema();
            if (schema == null) {
                return null;
            }

            if (schemaId.equals(schema.id()) && clazz.isAssignableFrom(p.getClass())) {
                result = clazz.cast(p);
            }
        }

        return result;
    }

    public static String currentFlowName(PsiElement element) {
        if (element == null) {
            return null;
        }

        List<String> keys = keys(element);
        if (keys.size() < 2) {
            return null;
        }

        if (!"flows".equals(keys.get(0))) {
            return null;
        }

        return keys.get(1);
    }

    public static List<String> keys(PsiElement target) {
        List<String> result = new ArrayList<>();
        PsiElement element = target;
        while (element != null) {
            PsiElement parent = PsiTreeUtil.getParentOfType(element, YAMLKeyValue.class);
            if (element instanceof YAMLKeyValue) {
                result.add(0, ((YAMLKeyValue) element).getKeyText());
            }
            element = parent;
        }
        return result;
    }

    public static boolean isValue(PsiElement element) {
        return element instanceof YAMLValue
                || (element instanceof YAMLSequenceItem && ((YAMLSequenceItem) element).getValue() == null)
                || (element instanceof YAMLKeyValue && ((YAMLKeyValue) element).getValue() == null);
    }

    public static boolean isNullValue(PsiElement element) {
        return (element instanceof YAMLSequenceItem && ((YAMLSequenceItem) element).getValue() == null)
                || (element instanceof YAMLKeyValue && ((YAMLKeyValue) element).getValue() == null);
    }

    public static YAMLValue asValue(PsiElement element) {
        if (element instanceof YAMLValue) {
            return (YAMLValue) element;
        }
        return null;
    }

    public static YAMLKeyValue asKv(PsiElement element) {
        return ObjectUtils.tryCast(element, YAMLKeyValue.class);
    }

    public static YAMLMapping asObject(PsiElement element) {
        return ObjectUtils.tryCast(element, YAMLMapping.class);
    }

    public static YAMLScalar asScalar(PsiElement element) {
        return ObjectUtils.tryCast(element, YAMLScalar.class);
    }

    public static <T extends PsiElement> T get(PsiElement root, Class<T> type, String... path) {
        if (root == null) {
            return null;
        }

        PsiElement current = root;
        for (String p : path) {
            YAMLMapping m = getChildOfType(current, YAMLMapping.class, true);
            if (m == null) {
                return null;
            }

            YAMLKeyValue kv = m.getKeyValueByKey(p);
            if (kv == null) {
                return null;
            }
            current = kv.getValue();
        }
        return getChildOfType(current, type, true);
    }

    public static Set<String> properties(YAMLMapping element) {
        if (element == null) {
            return Collections.emptySet();
        }

        return element.getKeyValues().stream()
                .map(k -> k.getKeyText().trim())
                .collect(Collectors.toSet());
    }

    public static Set<String> getPropertyNamesOfParentObject(@NotNull PsiElement originalPosition,
                                                             PsiElement computedPosition) {
        YAMLMapping object = PsiTreeUtil.getParentOfType(originalPosition, YAMLMapping.class);
        YAMLMapping otherObject = PsiTreeUtil.getParentOfType(computedPosition, YAMLMapping.class);
        if (object == null || otherObject != null
                && PsiTreeUtil.isAncestor(CompletionUtil.getOriginalOrSelf(object),
                CompletionUtil.getOriginalOrSelf(otherObject), true)) {
            object = otherObject;
        }
        return properties(object);
    }

    @Nullable
    public static <T extends PsiElement> T getParentOfType(@Nullable PsiElement element, @NotNull Class<T> type, boolean includeMySelf) {
        if (element == null) {
            return null;
        }

        if (includeMySelf && type.isInstance(element)) {
            return type.cast(element);
        }
        return PsiTreeUtil.getParentOfType(element, type);
    }

    @Nullable
    public static <T extends PsiElement> T getParentYamlOfType(@Nullable PsiElement element, @NotNull Class<T> type, boolean includeMySelf) {
        if (element == null) {
            return null;
        }

        if (includeMySelf && type.isInstance(element)) {
            return type.cast(element);
        }

        YAMLPsiElement parent = PsiTreeUtil.getParentOfType(element, YAMLPsiElement.class);
        if (type.isInstance(parent)) {
            return type.cast(parent);
        }

        return null;
    }

    @Nullable
    public static <T extends PsiElement> T getChildOfType(@Nullable PsiElement root, @NotNull Class<T> type, boolean includeMySelf) {
        if (root == null) {
            return null;
        }
        if (includeMySelf && type.isInstance(root)) {
            return type.cast(root);
        }
        return PsiTreeUtil.getChildOfType(root, type);
    }

    private static boolean isInstance(PsiElement element, Set<Class<? extends YAMLPsiElement>> types) {
        for (Class<? extends YAMLPsiElement> t : types) {
            if (t.isInstance(element)) {
                return true;
            }
        }
        return false;
    }

}
