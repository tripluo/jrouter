/*
 * Copyright 2002-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.jrouter.util;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * PathMatcher implementation for Ant-style path patterns.
 *
 * <p>
 * Part of this mapping code has been kindly borrowed from
 * <a href="http://ant.apache.org">Apache Ant</a>.
 */
@SuppressWarnings("PMD")
public class AntPathMatcher {

    /**
     * The pattern that matches an arbitrary number of directories.
     *
     * @since Ant 1.8.0
     */
    public static final String DEEP_TREE_MATCH = "**";

    /**
     * Default path separator: "/"
     */
    public static final String DEFAULT_PATH_SEPARATOR = "/";

    private static final String[] EMPTY_STRING_ARRAY = {};

    @lombok.Getter
    @lombok.Setter
    private String pathSeparator = DEFAULT_PATH_SEPARATOR;

    @lombok.Getter
    @lombok.Setter
    private boolean caseSensitive = true;

    @lombok.Getter
    @lombok.Setter
    private boolean trimTokens = false;

    public AntPathMatcher() {
    }

    public AntPathMatcher(String pathSeparator) {
        this.pathSeparator = pathSeparator;
    }

    /**
     * Tests whether a given path matches a given pattern.
     * <p>
     * If you need to call this method multiple times with the same pattern you should
     * rather use TokenizedPath
     * @param pattern The pattern to match against. Must not be <code>null</code>.
     * @param path The path to match, as a String. Must not be <code>null</code>.
     * @return <code>true</code> if the pattern matches against the string, or
     * <code>false</code> otherwise.
     */
    public boolean match(String pattern, String path) {
        if (path == null || path.startsWith(this.pathSeparator) != pattern.startsWith(this.pathSeparator)) {
            return false;
        }

        String[] tokenizedPattern = tokenizePathAsArray(pattern);
        String[] strDirs = tokenizePathAsArray(path);

        int patIdxStart = 0;
        int patIdxEnd = tokenizedPattern.length - 1;
        int strIdxStart = 0;
        int strIdxEnd = strDirs.length - 1;

        // up to first '**'
        while (patIdxStart <= patIdxEnd && strIdxStart <= strIdxEnd) {
            String patDir = tokenizedPattern[patIdxStart];
            if (patDir.equals(DEEP_TREE_MATCH)) {
                break;
            }
            if (!matchString(patDir, strDirs[strIdxStart], this.caseSensitive)) {
                return false;
            }
            patIdxStart++;
            strIdxStart++;
        }
        if (strIdxStart > strIdxEnd) {
            // Path is exhausted, only match if rest of pattern is * or **'s
            if (patIdxStart > patIdxEnd) {
                return (pattern.endsWith(this.pathSeparator) == path.endsWith(this.pathSeparator));
            }
            if (patIdxStart == patIdxEnd && tokenizedPattern[patIdxStart].equals("*")
                    && path.endsWith(this.pathSeparator)) {
                return true;
            }
            // String is exhausted
            for (int i = patIdxStart; i <= patIdxEnd; i++) {
                if (!tokenizedPattern[i].equals(DEEP_TREE_MATCH)) {
                    return false;
                }
            }
            return true;
        }
        if (patIdxStart > patIdxEnd) {
            // String not exhausted, but pattern is. Failure.
            return false;
        }

        // up to last '**'
        while (patIdxStart <= patIdxEnd && strIdxStart <= strIdxEnd) {
            String patDir = tokenizedPattern[patIdxEnd];
            if (patDir.equals(DEEP_TREE_MATCH)) {
                break;
            }
            if (!matchString(patDir, strDirs[strIdxEnd], this.caseSensitive)) {
                return false;
            }
            if (patIdxEnd == (tokenizedPattern.length - 1)
                    && pattern.endsWith(this.pathSeparator) != path.endsWith(this.pathSeparator)) {
                return false;
            }
            patIdxEnd--;
            strIdxEnd--;
        }
        if (strIdxStart > strIdxEnd) {
            // String is exhausted
            for (int i = patIdxStart; i <= patIdxEnd; i++) {
                if (!tokenizedPattern[i].equals(DEEP_TREE_MATCH)) {
                    return false;
                }
            }
            return true;
        }

        while (patIdxStart != patIdxEnd && strIdxStart <= strIdxEnd) {
            int patIdxTmp = -1;
            for (int i = patIdxStart + 1; i <= patIdxEnd; i++) {
                if (tokenizedPattern[i].equals(DEEP_TREE_MATCH)) {
                    patIdxTmp = i;
                    break;
                }
            }
            if (patIdxTmp == patIdxStart + 1) {
                // '**/**' situation, so skip one
                patIdxStart++;
                continue;
            }
            // Find the pattern between padIdxStart & padIdxTmp in str between
            // strIdxStart & strIdxEnd
            int patLength = (patIdxTmp - patIdxStart - 1);
            int strLength = (strIdxEnd - strIdxStart + 1);
            int foundIdx = -1;
            strLoop: for (int i = 0; i <= strLength - patLength; i++) {
                for (int j = 0; j < patLength; j++) {
                    String subPat = tokenizedPattern[patIdxStart + j + 1];
                    String subStr = strDirs[strIdxStart + i + j];
                    if (!matchString(subPat, subStr, this.caseSensitive)) {
                        continue strLoop;
                    }
                }
                foundIdx = strIdxStart + i;
                break;
            }
            if (foundIdx == -1) {
                return false;
            }

            patIdxStart = patIdxTmp;
            strIdxStart = foundIdx + patLength;
        }

        for (int i = patIdxStart; i <= patIdxEnd; i++) {
            if (!DEEP_TREE_MATCH.equals(tokenizedPattern[i])) {
                return false;
            }
        }
        return true;
    }

    /**
     * Tests whether a string matches against a pattern. The pattern may contain two
     * special characters:<br>
     * '*' means zero or more characters<br>
     * '?' means one and only one character
     * @param pattern The pattern to match against. Must not be <code>null</code>.
     * @param str The string which must be matched against the pattern. Must not be
     * <code>null</code>.
     * @param caseSensitive Whether matching should be performed case sensitively.
     * @return <code>true</code> if the string matches against the pattern, or
     * <code>false</code> otherwise.
     */
    static boolean matchString(String pattern, String str, boolean caseSensitive) {
        char[] patArr = pattern.toCharArray();
        char[] strArr = str.toCharArray();
        int patIdxStart = 0;
        int patIdxEnd = patArr.length - 1;
        int strIdxStart = 0;
        int strIdxEnd = strArr.length - 1;

        boolean containsStar = false;
        for (char ch : patArr) {
            if (ch == '*') {
                containsStar = true;
                break;
            }
        }

        if (!containsStar) {
            // No '*'s, so we make a shortcut
            if (patIdxEnd != strIdxEnd) {
                return false; // Pattern and string do not have the same size
            }
            for (int i = 0; i <= patIdxEnd; i++) {
                char ch = patArr[i];
                if (ch != '?' && different(caseSensitive, ch, strArr[i])) {
                    return false; // Character mismatch
                }
            }
            return true; // String matches against pattern
        }

        if (patIdxEnd == 0) {
            return true; // Pattern contains only '*', which matches anything
        }

        // Process characters before first star
        while (true) {
            char ch = patArr[patIdxStart];
            if (ch == '*' || strIdxStart > strIdxEnd) {
                break;
            }
            if (ch != '?' && different(caseSensitive, ch, strArr[strIdxStart])) {
                return false; // Character mismatch
            }
            patIdxStart++;
            strIdxStart++;
        }
        if (strIdxStart > strIdxEnd) {
            // All characters in the string are used. Check if only '*'s are
            // left in the pattern. If so, we succeeded. Otherwise failure.
            return allStars(patArr, patIdxStart, patIdxEnd);
        }

        // Process characters after last star
        while (true) {
            char ch = patArr[patIdxEnd];
            if (ch == '*' || strIdxStart > strIdxEnd) {
                break;
            }
            if (ch != '?' && different(caseSensitive, ch, strArr[strIdxEnd])) {
                return false; // Character mismatch
            }
            patIdxEnd--;
            strIdxEnd--;
        }
        if (strIdxStart > strIdxEnd) {
            // All characters in the string are used. Check if only '*'s are
            // left in the pattern. If so, we succeeded. Otherwise failure.
            return allStars(patArr, patIdxStart, patIdxEnd);
        }

        // process pattern between stars. padIdxStart and patIdxEnd point
        // always to a '*'.
        while (patIdxStart != patIdxEnd && strIdxStart <= strIdxEnd) {
            int patIdxTmp = -1;
            for (int i = patIdxStart + 1; i <= patIdxEnd; i++) {
                if (patArr[i] == '*') {
                    patIdxTmp = i;
                    break;
                }
            }
            if (patIdxTmp == patIdxStart + 1) {
                // Two stars next to each other, skip the first one.
                patIdxStart++;
                continue;
            }
            // Find the pattern between padIdxStart & padIdxTmp in str between
            // strIdxStart & strIdxEnd
            int patLength = (patIdxTmp - patIdxStart - 1);
            int strLength = (strIdxEnd - strIdxStart + 1);
            int foundIdx = -1;
            strLoop: for (int i = 0; i <= strLength - patLength; i++) {
                for (int j = 0; j < patLength; j++) {
                    char ch = patArr[patIdxStart + j + 1];
                    if (ch != '?' && different(caseSensitive, ch, strArr[strIdxStart + i + j])) {
                        continue strLoop;
                    }
                }
                foundIdx = strIdxStart + i;
                break;
            }

            if (foundIdx == -1) {
                return false;
            }
            patIdxStart = patIdxTmp;
            strIdxStart = foundIdx + patLength;
        }

        // All characters in the string are used. Check if only '*'s are left
        // in the pattern. If so, we succeeded. Otherwise failure.
        return allStars(patArr, patIdxStart, patIdxEnd);
    }

    private static boolean allStars(char[] chars, int start, int end) {
        for (int i = start; i <= end; ++i) {
            if (chars[i] != '*') {
                return false;
            }
        }
        return true;
    }

    private static boolean different(boolean caseSensitive, char ch, char other) {
        return caseSensitive ? ch != other : Character.toUpperCase(ch) != Character.toUpperCase(other);
    }

    String[] tokenizePathAsArray(String str) {
        return tokenizeToStringArray(str, this.pathSeparator, this.trimTokens, true);
    }

    static String[] tokenizeToStringArray(String str, String delimiters, boolean trimTokens,
            boolean ignoreEmptyTokens) {

        if (str == null) {
            return EMPTY_STRING_ARRAY;
        }

        StringTokenizer st = new StringTokenizer(str, delimiters);
        List<String> tokens = new ArrayList<>();
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            if (trimTokens) {
                token = token.trim();
            }
            if (!ignoreEmptyTokens || !token.isEmpty()) {
                tokens.add(token);
            }
        }
        return tokens.isEmpty() ? EMPTY_STRING_ARRAY : tokens.toArray(new String[0]);
    }

}
