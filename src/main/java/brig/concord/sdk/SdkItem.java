package brig.concord.sdk;

public class SdkItem {

    private final String version;
    private final String url;

    public SdkItem(String version, String url) {
        this.version = version;
        this.url = url;
    }

    public String version() {
        return version;
    }

    public String url() {
        return url;
    }

    @Override
    public String toString() {
        return version;
    }
}
