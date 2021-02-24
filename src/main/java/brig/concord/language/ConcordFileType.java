package brig.concord.language;

import brig.concord.ConcordBundle;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.fileTypes.OSFileIdeAssociation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.YAMLLanguage;

import javax.swing.*;

@SuppressWarnings("unused")
public class ConcordFileType extends LanguageFileType implements OSFileIdeAssociation {

    public static final ConcordFileType INSTANCE = new ConcordFileType();

    private ConcordFileType() {
//        super(ConcordLanguage.INSTANCE);
        super(YAMLLanguage.INSTANCE);
    }

    @NotNull
    @Override
    public String getName() {
        return "Concord";
    }

    @NotNull
    @Override
    public String getDescription() {
        return ConcordBundle.message("filetype.description.yaml");
    }

    @NotNull
    @Override
    public String getDefaultExtension() {
        return "concord.yml";
    }

    @Nullable
    @Override
    public Icon getIcon() {
        return ConcordIcons.FILE;
    }
}