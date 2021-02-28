package brig.concord.module;

import brig.concord.ConcordBundle;
import brig.concord.language.ConcordIcons;
import brig.concord.sdk.ConcordSdkType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.module.ModuleTypeManager;
import com.intellij.openapi.projectRoots.Sdk;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class ConcordModuleType extends ModuleType<ConcordModuleBuilder> {

    private static final String ID = "CONCORD_MODULE";

    public ConcordModuleType() {
        super(ID);
    }

    public static ConcordModuleType getInstance() {
        return (ConcordModuleType) ModuleTypeManager.getInstance().findByID(ID);
    }

    @NotNull
    @Override
    public ConcordModuleBuilder createModuleBuilder() {
        return new ConcordModuleBuilder();
    }

    @NotNull
    @Override
    public String getName() {
        return ConcordBundle.message("module.name");
    }

    @NotNull
    @Override
    public String getDescription() {
        return ConcordBundle.message("module.description");
    }

    @NotNull
    @Override
    public Icon getNodeIcon(@Deprecated boolean isOpened) {
        return ConcordIcons.FILE;
    }

    @Override
    public boolean isValidSdk(@NotNull Module module, @Nullable Sdk projectSdk) {
        return projectSdk != null && projectSdk.getSdkType().equals(ConcordSdkType.getInstance());
    }
}
