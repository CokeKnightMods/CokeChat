package de.cokechat.chat;

import de.cokechat.config.CokeChatConfig;
import de.cokechat.emoji.*;
import de.cokechat.visualwords.VisualWordParser;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.*;
import net.minecraft.resources.Identifier;

/** Only called when received GUI messages are split into display lines. Never used by input/sending. */
public final class ChatTextProcessor {
    private record Run(int start, int end, Style style) { }
    private record Styled(String text, List<Run> runs) { }
    private final Map<Component, Component> cache = new LinkedHashMap<>(256, .75f, true) {
        @Override protected boolean removeEldestEntry(Map.Entry<Component, Component> e) { return size() > 2048; }
    };
    private CokeChatConfig config;
    private VisualWordParser words;
    private EmojiParser emojis;
    private final Map<String,Boolean> fontAvailable = new HashMap<>();
    public void configure(CokeChatConfig c, EmojiDatabase database) { config = c; words = new VisualWordParser(c.visualWords.rules); emojis = new EmojiParser(database); cache.clear(); fontAvailable.clear(); }
    public Component process(Component original) {
        if (config == null || !config.enabled) return original;
        return cache.computeIfAbsent(original, this::transform);
    }
    private Component transform(Component original) {
        Styled source = flatten(original);
        MutableComponent result = Component.empty();
        int last = 0;
        if (config.visualWords.enabled) for (var span : words.spans(source.text)) {
            appendRange(result, source, last, span.start());
            result.append(Component.literal(span.replacement()).setStyle(styleAt(source, span.start()))); last = span.end();
        }
        appendRange(result, source, last, source.text.length());
        if (!config.emoji.enabled) return result;
        Styled displayed = flatten(result); var output = Component.empty();
        for (var token : emojis.parse(displayed.text)) {
            if (token.emoji() == null) appendRange(output, displayed, token.start(), token.end());
            else output.append(renderEmoji(token.emoji(), token.text(), styleAt(displayed, token.start())));
        }
        return output;
    }
    public Component renderEmoji(EmojiDefinition d, String fallback, Style style) {
        if (d.font() != null && d.glyph() != null) {
            String font = d.font().equals("cokechat:emoji") ? "cokechat:emoji_" + config.emoji.size : d.font();
            if (font.startsWith("cokechat:emoji/")) font = "cokechat:emoji_" + config.emoji.size + font.substring("cokechat:emoji".length());
            try {
                var id = Identifier.parse(font);
                if (fontAvailable.computeIfAbsent(font, this::validLocalFont))
                    return Component.literal(d.glyph()).setStyle(style.withFont(new FontDescription.Resource(id)).withColor(0xFFFFFF).withBold(false).withItalic(false));
            } catch (RuntimeException ignored) { }
        }
        return Component.literal(d.unicode() == null || d.unicode().isEmpty() ? fallback : d.unicode()).setStyle(style);
    }
    private boolean validLocalFont(String font) {
        try {
            var id=Identifier.parse(font);var resources=Minecraft.getInstance().getResourceManager();
            var resource=resources.getResource(Identifier.fromNamespaceAndPath(id.getNamespace(),"font/"+id.getPath()+".json"));
            if(resource.isEmpty())return false;
            try(var reader=resource.get().openAsReader()) {
                var root=com.google.gson.JsonParser.parseReader(reader).getAsJsonObject();
                for(var provider:root.getAsJsonArray("providers")) {
                    var object=provider.getAsJsonObject();
                    if(object.has("file")) {var asset=Identifier.parse(object.get("file").getAsString());if(resources.getResource(Identifier.fromNamespaceAndPath(asset.getNamespace(),"textures/"+asset.getPath())).isEmpty())return false;}
                }
                return true;
            }
        } catch(Exception ignored){return false;}
    }
    private static Styled flatten(Component component) {
        var text = new StringBuilder(); var runs = new ArrayList<Run>();
        component.visit((Style style, String value) -> { int start = text.length(); text.append(value); runs.add(new Run(start, text.length(), style)); return Optional.empty(); }, Style.EMPTY);
        return new Styled(text.toString(), runs);
    }
    private static Style styleAt(Styled s, int offset) { for (var run : s.runs) if (offset >= run.start && offset < run.end) return run.style; return Style.EMPTY; }
    private static void appendRange(MutableComponent out, Styled s, int start, int end) {
        for (var run : s.runs) { int a = Math.max(start, run.start), b = Math.min(end, run.end); if (a < b) out.append(Component.literal(s.text.substring(a, b)).setStyle(run.style)); }
    }
}
