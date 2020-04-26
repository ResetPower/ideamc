package org.imcl.lang;

public class LanguageMapFactory {
    private LanguageMapFactory() {
    }

    public static LanguageMapFactory newInstance() {
        return new LanguageMapFactory();
    }

    public LanguageMap newLanguageMap(Language language) {
        return new LanguageMap(LanguageResource.get(language));
    }
}
