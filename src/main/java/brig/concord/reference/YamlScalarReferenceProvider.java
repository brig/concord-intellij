package brig.concord.reference;

import com.intellij.psi.PsiReference;
import org.jetbrains.yaml.psi.YAMLScalar;

public interface YamlScalarReferenceProvider {

    PsiReference get(YAMLScalar value);
}
