package de.cokechat.emoji;
import java.util.List;
public record EmojiDefinition(String id, List<String> aliases, String unicode, String font, String glyph, int width, int height, List<String> unicodeAliases) {
    public EmojiDefinition(String id,List<String> aliases,String unicode,String font,String glyph,int width,int height) { this(id,aliases,unicode,font,glyph,width,height,List.of()); }
}
