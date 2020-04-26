package org.imcl.lang;

import java.util.Map;

public class LanguageMap {
    private Map<String, String> map;
    LanguageMap(Map<String, String> map) {
        this.map = map;
    }
    String get(String key) {
        return map.get(key);
    }
}
