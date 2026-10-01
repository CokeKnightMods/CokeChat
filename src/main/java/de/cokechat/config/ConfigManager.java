package de.cokechat.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.nio.file.*;
import org.slf4j.LoggerFactory;

public final class ConfigManager {
    public static final Gson JSON = new GsonBuilder().setPrettyPrinting().create();
    private final Path file;
    private CokeChatConfig config = new CokeChatConfig();
    private String error = "";
    public ConfigManager(Path file) { this.file = file; }
    public CokeChatConfig get() { return config; }
    public String error() { return error; }
    public void load() {
        if (!Files.exists(file)) { save(); return; }
        try (var reader = Files.newBufferedReader(file)) {
            config = JSON.fromJson(reader, CokeChatConfig.class);
            if (config == null) throw new IOException("Empty configuration");
            config.validate();
        } catch (Exception e) {
            config = new CokeChatConfig();
            try { Files.copy(file, file.resolveSibling(file.getFileName() + ".broken-" + System.currentTimeMillis())); } catch (IOException ignored) { }
            failure("Configuration could not be read; using defaults", e);
        }
    }
    public void save() {
        config.validate();
        try { atomicWrite(file, JSON.toJson(config)); error = ""; }
        catch (IOException e) { failure("Configuration could not be saved", e); }
    }
    public static void atomicWrite(Path path, String data) throws IOException {
        Files.createDirectories(path.getParent());
        Path temporary = path.resolveSibling(path.getFileName() + ".tmp");
        Files.writeString(temporary, data);
        try { Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE); }
        catch (AtomicMoveNotSupportedException e) { Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING); }
    }
    private void failure(String message, Exception e) { error = message; LoggerFactory.getLogger("CokeChat").warn(message, e); }
}
