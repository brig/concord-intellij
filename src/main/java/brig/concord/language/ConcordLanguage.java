package brig.concord.language;

import com.intellij.lang.Language;

public class ConcordLanguage extends Language {

    public static final ConcordLanguage INSTANCE = new ConcordLanguage();

    private ConcordLanguage() {
        super("Concord");
    }

}