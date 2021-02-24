package brig.concord.sdk;

import brig.concord.ConcordBundle;
import brig.concord.language.ConcordIcons;
import brig.concord.log.Logger;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkModel;
import com.intellij.openapi.projectRoots.SdkTypeId;
import com.intellij.openapi.roots.ui.configuration.projectRoot.SdkDownload;
import com.intellij.openapi.roots.ui.configuration.projectRoot.SdkDownloadTask;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class SdkDownloader implements SdkDownload {

    private static final Logger log = Logger.getInstance(SdkDownloader.class);

    private static <T> T computeInBackground(Project project, String title, Function<? super ProgressIndicator, ? extends T> task) throws Exception {
        return ProgressManager.getInstance().run(new Task.WithResult<T, Exception>(project, title, true) {
            @Override
            protected T compute(@NotNull ProgressIndicator indicator) {
                return task.apply(indicator);
            }
        });
    }

    @Override
    public boolean supportsDownload(@NotNull SdkTypeId sdkTypeId) {
        return sdkTypeId == ConcordSdkType.getInstance();
    }

    @Override
    public @NotNull Icon getIconForDownloadAction(@NotNull SdkTypeId sdkTypeId) {
        return ConcordIcons.FILE;
    }

    @Override
    public void showDownloadUI(@NotNull SdkTypeId sdkTypeId, @NotNull SdkModel sdkModel, @NotNull JComponent parentComponent, @Nullable Sdk selectedSdk, @NotNull Consumer<SdkDownloadTask> sdkCreatedCallback) {
        Project project = CommonDataKeys.PROJECT.getData(DataManager.getInstance().getDataContext(parentComponent));
        if (project == null || project.isDisposed()) {
            return;
        }

        List<SdkItem> items = Collections.emptyList();
        try {
            items = computeInBackground(project, ConcordBundle.message("progress.title.downloading.sdk.list"), progressIndicator -> SdkListDownloader.getInstance().downloadForUI(progressIndicator));
        } catch (Throwable e) {
            log.warn("Failed to download the list of SDKs. {}", e);
        }

        if (project.isDisposed()) {
            return;
        }

        if (items.isEmpty()) {
            Messages.showErrorDialog(project,
                    ConcordBundle.message("error.message.no.sdk.for.download"),
                    ConcordBundle.message("error.message.title.download.sdk")
            );
            return;
        }

        Pair<SdkItem, Path> result = new SdkDownloadDialog(project, parentComponent, items)
                .selectSdkAndPath();
        if (result == null) {
            return;
        }

        SdkInstaller.PendingSdkRequest request;
        try {
            request = computeInBackground(project, ConcordBundle.message("progress.title.preparing.sdk"),
                    progressIndicator -> SdkInstaller.getInstance().prepareSdkInstallation(result.getFirst(), result.getSecond()));
        } catch (Throwable t) {
            log.warn("Failed to prepare SDK installation to {}", result.getSecond(), t);
            Messages.showErrorDialog(project,
                    ConcordBundle.message("error.message.text.sdk.install.failed", result.getSecond()),
                    ConcordBundle.message("error.message.title.download.sdk")
            );
            return;
        }

        sdkCreatedCallback.accept(
                new SdkDownloadTask() {
                    @Override
                    public @NotNull String getSuggestedSdkName() {
                        return "Concord (" + request.item().version() + ")";
                    }

                    @Override
                    public @NotNull String getPlannedHomeDir() {
                        return request.installDir().toString();
                    }

                    @Override
                    public @NotNull String getPlannedVersion() {
                        return request.item().version();
                    }

                    @Override
                    public void doDownload(@NotNull ProgressIndicator indicator) {
                        SdkInstaller.getInstance().installJdk(request, indicator);
                    }
                });
    }
}
