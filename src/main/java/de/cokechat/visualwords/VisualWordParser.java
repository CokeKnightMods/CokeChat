package de.cokechat.visualwords;
import java.util.*;
import java.util.regex.Pattern;
public final class VisualWordParser {
    private record Compiled(Pattern pattern, String replacement) { }
    public record Span(int start, int end, String replacement) { }
    private final List<Compiled> rules;
    public VisualWordParser(List<VisualWordRule> source) {
        var compiled = new ArrayList<Compiled>();
        for (var r : source) {
            if (r == null || !r.enabled || r.searchText == null || r.searchText.isEmpty() || r.replacementText == null || r.searchText.length() > 256 || r.replacementText.length() > 1024) continue;
            String expression = Pattern.quote(r.searchText);
            if (r.wholeWord) expression = "(?<![\\p{L}\\p{N}_])" + expression + "(?![\\p{L}\\p{N}_])";
            compiled.add(new Compiled(Pattern.compile(expression, r.caseSensitive ? 0 : Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE), r.replacementText));
        }
        rules = List.copyOf(compiled);
    }
    // First rule wins overlaps. Replacements are never fed through the rule set again.
    public List<Span> spans(String text) {
        var result = new ArrayList<Span>();
        BitSet used = new BitSet(text.length());
        for (var rule : rules) {
            var matcher = rule.pattern.matcher(text);
            while (matcher.find()) {
                int overlap = used.nextSetBit(matcher.start());
                if (overlap >= 0 && overlap < matcher.end()) continue;
                result.add(new Span(matcher.start(), matcher.end(), rule.replacement)); used.set(matcher.start(), matcher.end());
            }
        }
        result.sort(Comparator.comparingInt(Span::start)); return result;
    }
    public String parse(String text) {
        var out = new StringBuilder(); int offset = 0;
        for (var span : spans(text)) { out.append(text, offset, span.start).append(span.replacement); offset = span.end; }
        return out.append(text, offset, text.length()).toString();
    }
}
