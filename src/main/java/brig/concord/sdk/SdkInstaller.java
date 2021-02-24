package brig.concord.sdk;

import brig.concord.ConcordBundle;
import brig.concord.IOUtils;
import brig.concord.log.Logger;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.util.io.Decompressor;
import com.intellij.util.io.HttpRequests;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SdkInstaller {

    private static final Logger log = Logger.getInstance(SdkInstaller.class);

    public static SdkInstaller getInstance() {
        return ApplicationManager.getApplication().getService(SdkInstaller.class);
    }

    private static boolean isDirEmpty(Path directory) throws IOException {
        try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(directory)) {
            return !dirStream.iterator().hasNext();
        }
    }

    public Pair<Path, String> validateInstallDir(String dest) {
        if (dest == null || dest.trim().isEmpty()) {
            return Pair.pair(null, ConcordBundle.message("dialog.message.error.target.sdk.path.empty"));
        }

        try {
            Path targetDir = Paths.get(FileUtil.expandUserHome(dest));

            if (Files.isRegularFile(targetDir)) {
                return Pair.pair(null, ConcordBundle.message("dialog.message.error.target.sdk.path.exists.file"));
            }

            if (Files.isDirectory(targetDir) && !isDirEmpty(targetDir)) {
                return Pair.pair(null, ConcordBundle.message("dialog.message.error.target.sdk.path.exists.nonEmpty.dir"));
            }

            return Pair.pair(targetDir, null);
        } catch (Throwable t) {
            log.warn("Failed to resolve user path: {}", dest, t);
            return Pair.pair(null, ConcordBundle.message("dialog.message.error.resolving.sdk.path"));
        }
    }

    public PendingSdkRequest prepareSdkInstallation(SdkItem sdkItem, Path installDir) {
        // TODO: check already installed or loading SDK
        return prepareJdkInstallation(sdkItem, installDir);
    }

    public void installJdk(PendingSdkRequest request, ProgressIndicator indicator) {
        SdkItem item = request.item();
        if (indicator != null) {
            indicator.setText(ConcordBundle.message("progress.text.installing.sdk.1", item.version()));
        }

        Path targetDir = request.installDir;
        if (indicator != null) {
            indicator.setText2(ConcordBundle.message("progress.text2.downloading.sdk"));
        }

        Path downloadFile = Paths.get(PathManager.getTempPath(), "sdk-" + System.nanoTime());
        try {
            try {
                HttpRequests.request(item.url())
                        .productNameAsUserAgent()
                        .saveToFile(downloadFile.toFile(), indicator);

                if (!Files.isRegularFile(downloadFile)) {
                    throw new RuntimeException("Downloaded file does not exist: " + downloadFile);
                }
            } catch (Throwable t) {
                throw new RuntimeException("Failed to download SDK " + item.version() + " from " + item.url(), t);
            }

            // TODO: validate size/checksum

            if (indicator != null) {
                indicator.setIndeterminate(true);
                indicator.setText2(ConcordBundle.message("progress.text2.unpacking.sdk"));
            }

            try {
                Decompressor decompressor = openDecompressor(downloadFile);

                decompressor.postProcessor(path -> {
                    if (indicator != null) {
                        indicator.checkCanceled();
                    }
                });

                decompressor.extract(targetDir);
            } catch (Throwable t) {
                throw new RuntimeException("Failed to extract SDK " + item.version() + ": " + t.getMessage());
            }
        } catch (Throwable t) {
            try {
                IOUtils.deleteRecursively(targetDir);
            } catch (IOException e) {
                // ignore
            }
            throw t;
        } finally {
            try {
                IOUtils.deleteRecursively(downloadFile);
            } catch (IOException e) {
                // ignore
            }
        }
    }

    private Decompressor openDecompressor(Path archiveFile) {
        Decompressor.Zip decompressor = new Decompressor.Zip(archiveFile);
        if (!SystemInfo.isWindows) {
            return decompressor.withZipExtensions();
        }
        return decompressor;
    }

    private PendingSdkRequest prepareJdkInstallation(SdkItem sdkItem, Path installDir) {
        Pair<Path, String> result = validateInstallDir(installDir.toString());
        if (result.getFirst() == null || result.getSecond() != null) {
            throw new RuntimeException(result.getSecond() != null ? result.getSecond() : "Invalid Target Directory");
        }
        return new PendingSdkRequest(sdkItem, installDir);
    }

    static class PendingSdkRequest {
        private final SdkItem item;
        private final Path installDir;

        PendingSdkRequest(SdkItem item, Path installDir) {
            this.item = item;
            this.installDir = installDir;
        }

        public SdkItem item() {
            return item;
        }

        public Path installDir() {
            return installDir;
        }
    }
}
