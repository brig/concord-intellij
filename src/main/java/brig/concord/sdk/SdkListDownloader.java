package brig.concord.sdk;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;

import java.util.Collections;
import java.util.List;

public class SdkListDownloader {

    public static SdkListDownloader getInstance() {
        return ApplicationManager.getApplication().getService(SdkListDownloader.class);
    }

    public List<SdkItem> downloadForUI(ProgressIndicator progressIndicator) {
//        try {
//            Thread.sleep(20_000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        throw new RuntimeException("BOOM");

        // TODO :)
        return Collections.singletonList(new SdkItem("1.79.0", "https://github.com/brig/concord-sdk/releases/latest/download/concord-sdk.zip"));
    }
}
