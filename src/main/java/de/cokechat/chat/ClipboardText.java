package de.cokechat.chat;
import java.util.regex.Pattern;
/** Clipboard-only cleanup. Component styles are already excluded by Component.getString(). */
public final class ClipboardText {
    private static final Pattern CODES=Pattern.compile("(?i)§(?:x(?:§[0-9a-f]){6}|#[0-9a-f]{6}|[0-9a-fk-or])");
    private ClipboardText() {}
    public static String plain(String text){return CODES.matcher(text).replaceAll("");}
}
