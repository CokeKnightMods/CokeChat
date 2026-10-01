package de.cokechat.emoji;
import de.cokechat.config.ConfigManager;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import org.slf4j.LoggerFactory;
public final class EmojiDatabase {
    private final Map<String, EmojiDefinition> byId = new LinkedHashMap<>();
    private final Map<String, EmojiDefinition> aliases = new LinkedHashMap<>();
    private List<EmojiDefinition> definitions = List.of();
    private final Node unicodeRoot = new Node();
    private static final class Node { final Map<Integer,Node> children = new HashMap<>(); EmojiDefinition emoji; }
    public record UnicodeMatch(int end, EmojiDefinition emoji) { }
    public EmojiDatabase(Collection<EmojiDefinition> definitions) { for (var d : definitions) add(d); index(); }
    public static EmojiDatabase load(Path custom) {
        var database = new EmojiDatabase(List.of());
        try (var in = EmojiDatabase.class.getResourceAsStream("/assets/cokechat/emojis/emojis.json")) {
            if (in != null) database.read(new InputStreamReader(in, java.nio.charset.StandardCharsets.UTF_8));
        } catch (Exception e) { LoggerFactory.getLogger("CokeChat").warn("Cannot load bundled emojis", e); }
        if (Files.exists(custom)) try (var reader = Files.newBufferedReader(custom)) { database.read(reader); }
        catch (Exception e) { LoggerFactory.getLogger("CokeChat").warn("Invalid local emoji definitions; keeping bundled emojis", e); }
        database.index();
        return database;
    }
    private void read(Reader reader) {
        var defs = ConfigManager.JSON.fromJson(reader, EmojiDefinition[].class);
        if (defs != null) for (var d : defs) add(d);
    }
    private void add(EmojiDefinition d) {
        if (d == null || d.id() == null || !d.id().matches("[a-z0-9_+-]{1,64}")) return;
        if ((d.unicode() == null || d.unicode().isEmpty()) && (d.font() == null || d.glyph() == null || d.glyph().isEmpty())) return;
        byId.put(d.id(), d);
    }
    private void index() {
        aliases.clear(); unicodeRoot.children.clear(); definitions = List.copyOf(byId.values());
        for (var d : definitions) {
            aliases.put(d.id(), d);
            if (d.aliases() != null) for (var alias : d.aliases()) if (alias != null && alias.matches("[a-z0-9_+-]{1,64}")) aliases.put(alias,d);
            indexUnicode(d.unicode(),d);
            if (d.unicodeAliases() != null) for (var text : d.unicodeAliases()) indexUnicode(text,d);
        }
    }
    private static boolean ignoredModifier(int cp) { return cp == 0xFE0F || cp >= 0x1F3FB && cp <= 0x1F3FF; }
    private void indexUnicode(String text, EmojiDefinition definition) {
        if (text == null || text.isEmpty() || text.codePoints().noneMatch(cp -> cp > 127 && !ignoredModifier(cp))) return;
        Node node = unicodeRoot;
        for (int cp : text.codePoints().toArray()) if (!ignoredModifier(cp)) node = node.children.computeIfAbsent(cp,key -> new Node());
        node.emoji = definition;
    }
    /** Longest sequence wins. Skin modifiers affect neither stored input nor the chosen standard glyph. */
    public UnicodeMatch matchUnicode(String text, int start) {
        Node node = unicodeRoot; UnicodeMatch match = null; int offset = start;
        while (offset < text.length()) {
            int cp = text.codePointAt(offset); Node next = node.children.get(cp); if (next == null) break;
            node = next; offset += Character.charCount(cp);
            if (offset < text.length() && text.codePointAt(offset) == 0xFE0E) return null;
            while (offset < text.length() && ignoredModifier(text.codePointAt(offset))) offset += Character.charCount(text.codePointAt(offset));
            if (node.emoji != null) match = new UnicodeMatch(offset,node.emoji);
        }
        return match;
    }
    public EmojiDefinition find(String alias) { return aliases.get(alias); }
    public List<String> suggest(String prefix) { return aliases.keySet().stream().filter(s -> s.startsWith(prefix)).sorted(Comparator.comparingInt(String::length).thenComparing(Comparator.naturalOrder())).limit(6).toList(); }
    public List<String> suggest(String prefix, Collection<String> favorites) {
        var favoriteIds=new HashSet<>(favorites);
        return aliases.keySet().stream().filter(s -> s.startsWith(prefix))
            .sorted(Comparator.<String>comparingInt(s -> favoriteIds.contains(aliases.get(s).id()) ? 0 : 1)
                .thenComparingInt(String::length).thenComparing(Comparator.naturalOrder())).limit(6).toList();
    }
    public List<EmojiDefinition> definitions() { return definitions; }
    public List<EmojiDefinition> search(String query) {
        String needle = query.toLowerCase(Locale.ROOT).replace(":", "").trim();
        if (needle.isEmpty()) return definitions;
        return definitions.stream().filter(d -> d.id().contains(needle) || needle.equals(d.unicode()) || d.aliases() != null && d.aliases().stream().anyMatch(a -> a != null && a.contains(needle))).toList();
    }
}
