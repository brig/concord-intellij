/*
 * Copyright 2010 Jon S Akhtar (Sylvanaar)
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package brig.concord.sdk;

import brig.concord.language.ConcordIcons;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellij.openapi.projectRoots.*;
import com.intellij.openapi.roots.OrderRootType;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ConcordSdkType extends SdkType {

    private static final ObjectMapper om = new ObjectMapper();

    public ConcordSdkType() {
        super("Concord SDK");
    }

    public static boolean isConcordSdk(Sdk sdk) {
        return (sdk != null) && (sdk.getSdkType() instanceof ConcordSdkType);
    }

    @NotNull
    public static ConcordSdkType getInstance() {
        return SdkType.findInstance(ConcordSdkType.class);
    }

    private static Info readInfo(Path sdkHome) {
        Path info = sdkHome.resolve("info.json");
        if (!Files.exists(info)) {
            return null;
        }

        try {
            return om.readValue(info.toFile(), Info.class);
        } catch (Throwable t) {
            return null;
        }
    }

    @NotNull
    @Override
    public Icon getIcon() {
        return ConcordIcons.FILE;
    }

    @NotNull
    @Override
    public Icon getIconForAddAction() {
        return getIcon();
    }

    @Nullable
    @Override
    public String suggestHomePath() {
        return System.getenv().get("CONCORD_HOME");
    }

    @Override
    public boolean isValidSdkHome(@NotNull String path) {
        Path home = Paths.get(path);
        if (!Files.isDirectory(home)) {
            return false;
        }

        if (readInfo(home) == null) {
            return false;
        }

        if (!home.resolve("concord-cli.jar").toFile().canExecute()) {
            return false;
        }

        return true;
    }

    @NotNull
    @Override
    public String suggestSdkName(@Nullable String currentSdkName, @NotNull String sdkHome) {
        String version = getVersionString(sdkHome);
        return "Concord (" + version + ")";
    }

    @Nullable
    @Override
    public String getVersionString(@NotNull String sdkHome) {
        Path home = Paths.get(sdkHome);
        Info info = readInfo(home);
        if (info == null) {
            return null;
        }
        return info.version();
    }

    @Nullable
    @Override
    public AdditionalDataConfigurable createAdditionalDataConfigurable(@NotNull final SdkModel sdkModel,
                                                                       @NotNull final SdkModificator sdkModificator) {
        return null;
    }

    @Override
    public void saveAdditionalData(@NotNull final SdkAdditionalData additionalData, @NotNull final Element additional) {
    }

    @NotNull
    @Override
    public String getPresentableName() {
        return "Concord SDK";
    }

    @Override
    public void setupSdkPaths(@NotNull Sdk sdk) {
        SdkModificator sdkModificator = sdk.getSdkModificator();
//        sdkModificator.addRoot(StdLibrary.getStdFileLocation(), OrderRootType.CLASSES);
        sdkModificator.commitChanges();
    }

    @Override
    public boolean isRootTypeApplicable(OrderRootType type) {
//        return type == OrderRootType.CLASSES;
        return type.equals(OrderRootType.SOURCES) || type.equals(OrderRootType.CLASSES);
    }

    private static class Info {

        private final String version;

        @JsonCreator
        public Info(@JsonProperty("version") String version) {
            this.version = version;
        }

        public String version() {
            return version;
        }
    }
}