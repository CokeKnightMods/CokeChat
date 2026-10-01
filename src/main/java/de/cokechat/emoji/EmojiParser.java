package de.cokechat.emoji;
import java.util.*;
import java.util.regex.Pattern;
public final class EmojiParser {
    private static final Pattern ALIAS = Pattern.compile("[a-z0-9_+\\-]{1,64}");
    public record Token(int start, int end, String text, EmojiDefinition emoji) { }
    private final EmojiDatabase database;
    public EmojiParser(EmojiDatabase database) { this.database = database; }
    public List<Token> parse(String text) {
        var tokens = new ArrayList<Token>(); int last = 0;
        for (int offset = 0; offset < text.length();) {
            EmojiDefinition emoji = null; int end = offset;
            if (text.charAt(offset) == ':') {
                int close = text.indexOf(':',offset+1);
                if (close > offset+1 && close-offset <= 65) {
                    String alias = text.substring(offset+1,close);
                    if (ALIAS.matcher(alias).matches()) { emoji = database.find(alias); if (emoji != null) end = close+1; }
                }
            }
            if (emoji == null) { var match = database.matchUnicode(text,offset); if (match != null) { emoji = match.emoji(); end = match.end(); } }
            if (emoji == null) { offset += Character.charCount(text.codePointAt(offset)); continue; }
            if (offset > last) tokens.add(new Token(last,offset,text.substring(last,offset),null));
            tokens.add(new Token(offset,end,text.substring(offset,end),emoji)); last = end; offset = end;
        }
        if (last < text.length()) tokens.add(new Token(last, text.length(), text.substring(last), null));
        return List.copyOf(tokens);
    }
}
