package brig.concord.module;

import brig.concord.sdk.ConcordSdkType;
import com.intellij.ide.util.projectWizard.*;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.projectRoots.SdkTypeId;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.util.Condition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ConcordModuleBuilder extends ModuleBuilder implements ModuleBuilderListener {

    @Override
    public void setupRootModel(@NotNull ModifiableRootModel rootModel) {
        doAddContentEntry(rootModel);
    }

    @Override
    public void moduleCreated(@NotNull Module module) {
    }

    @Override
    public ConcordModuleType getModuleType() {
        return ConcordModuleType.getInstance();
    }

    @Override
    public @Nullable String getContentEntryPath() {
        return super.getContentEntryPath();
    }

    @Override
    public boolean isSuitableSdkType(SdkTypeId sdkType) {
        return sdkType instanceof ConcordSdkType;
    }

    @Nullable
    @Override
    public ModuleWizardStep modifySettingsStep(@NotNull SettingsStep settingsStep) {
        return new SdkSettingsStep(settingsStep, this, new Condition<SdkTypeId>() {
            @Override
            public boolean value(SdkTypeId sdkType) {
                return ConcordModuleBuilder.this.isSuitableSdkType(sdkType);
            }
        });
    }
}
