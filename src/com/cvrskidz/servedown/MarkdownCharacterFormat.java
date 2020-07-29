package com.cvrskidz.servedown;

public enum MarkdownCharacterFormat {
    ITALIC("*", "<em>", "</em>"),
    HIGHLIGHT("`", "<code>", "</code>"),
    BOLD("**", "<strong>", "</strong>");

    private String token, openTag, closeTag;

    private MarkdownCharacterFormat(String token, String openTag, String closeTag) {
        this.token = token;
        this.openTag = openTag;
        this.closeTag = closeTag;
    }

    public String getToken() {
        return token;
    }

    public String enclose(String s) {
        if(this.token.equals("`")) {
            s = escapedCodeCharacters(s);
        }
        return openTag + s + closeTag;
    }

    /**
     * Replaces all conflicting html or formatting tokens with entity codes e.g. \* -> &# 42;.
     *
     * @param s The string to format.
     * @return The formatted string
     */
    private String escapedCodeCharacters(String s) {
        s = s.replace("<", "&lt;");
        s = s.replace(">", "&gt;");
        s = s.replace("*", "&#42;");
        return s;
    }
}
