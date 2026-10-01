package de.cokechat.history;
import de.cokechat.config.ConfigManager;
import java.io.*;
import java.nio.file.*;
import java.security.*;
import java.util.*;
import org.slf4j.LoggerFactory;
public final class ChatHistoryManager {
    private final Path directory;
    private final Deque<String> messages = new ArrayDeque<>();
    private Path file;
    private int limit = 1000;
    private boolean persistent, dirty;
    public ChatHistoryManager(Path directory) { this.directory = directory; }
    public void configure(int limit, boolean persistent) { this.limit = limit; this.persistent = persistent; trim(); }
    public void open(String scope) {
        save(); messages.clear(); dirty = false;
        try { file = directory.resolve(HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(scope.getBytes(java.nio.charset.StandardCharsets.UTF_8))) + ".jsonl"); }
        catch (NoSuchAlgorithmException e) { throw new IllegalStateException(e); }
        if (persistent && Files.exists(file)) try (var reader = Files.newBufferedReader(file)) {
            String line; while ((line = reader.readLine()) != null) {
                try { String message = ConfigManager.JSON.fromJson(line, String.class); if (message != null) { messages.addLast(message); trim(); } }
                catch (RuntimeException ignored) { }
            }
        } catch (IOException e) { LoggerFactory.getLogger("CokeChat").warn("Local history could not be read", e); }
    }
    public void add(String message) { messages.addLast(message); trim(); dirty = true; }
    private void trim() { while (limit >= 0 && messages.size() > limit) messages.removeFirst(); }
    public List<String> snapshot() { return List.copyOf(messages); }
    public void save() {
        if (!persistent || !dirty || file == null) return;
        var json = new com.google.gson.Gson(); var out = new StringBuilder();
        for (var message : messages) out.append(json.toJson(message)).append('\n');
        try { ConfigManager.atomicWrite(file, out.toString()); dirty = false; }
        catch (IOException e) { LoggerFactory.getLogger("CokeChat").warn("Local history could not be saved", e); }
    }
    public void clear() { messages.clear(); dirty = true; if (file != null) try { ConfigManager.atomicWrite(file, ""); dirty = false; } catch (IOException e) { LoggerFactory.getLogger("CokeChat").warn("Local history could not be cleared", e); } }
    public void close() { save(); messages.clear(); file = null; dirty = false; }
}
