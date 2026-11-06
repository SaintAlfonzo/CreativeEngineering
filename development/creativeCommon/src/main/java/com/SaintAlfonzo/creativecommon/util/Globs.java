package com.SaintAlfonzo.creativecommon.util;

import java.util.regex.Pattern;

/** Id globs for the pack's rule files (creative_split.md 7.2, Common API): read by Common's state lines and by content jars. */
public final class Globs {
    private Globs() {}

    /** Glob over a full id: {@code *} matches anything, the rest is literal. */
    public static Pattern glob(String text) {
        StringBuilder re = new StringBuilder();
        for (String part : text.split("\\*", -1)) {
            if (re.length() > 0) {
                re.append(".*");
            }
            re.append(Pattern.quote(part));
        }
        return Pattern.compile(re.toString());
    }
}
