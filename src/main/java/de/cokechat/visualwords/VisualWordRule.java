package de.cokechat.visualwords;
public final class VisualWordRule {
    public boolean enabled, caseSensitive, wholeWord;
    public String searchText, replacementText;
    public VisualWordRule(boolean enabled, String search, String replacement, boolean sensitive, boolean whole) {
        this.enabled = enabled; searchText = search; replacementText = replacement; caseSensitive = sensitive; wholeWord = whole;
    }
}
