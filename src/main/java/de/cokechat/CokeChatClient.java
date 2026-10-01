package de.cokechat;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import de.cokechat.config.*;
import de.cokechat.chat.ChatTextProcessor;
import de.cokechat.emoji.EmojiDatabase;
import de.cokechat.history.ChatHistoryManager;
import de.cokechat.gui.CokeChatSettingsScreen;
import java.nio.file.Path;

public final class CokeChatClient implements ClientModInitializer {
    private static final CokeChatConfig DEFAULTS = new CokeChatConfig();
    private static final ChatTextProcessor TEXT = new ChatTextProcessor();
    private static ConfigManager manager;
    private static EmojiDatabase emojis;
    private static ChatHistoryManager history;
    private static Path directory;
    private static String scope;
    private static boolean restoring;
    private int ticks;
    private static int configRevision;
    public static int configRevision(){return configRevision;}
    public static CokeChatConfig config() { return manager == null ? DEFAULTS : manager.get(); }
    public static ConfigManager manager() { return manager; }
    public static ChatTextProcessor text() { return TEXT; }
    public static EmojiDatabase emojis() { return emojis; }
    public static Path customEmojiFile() { return directory.resolve("emojis.json"); }
    public static int offsetX() { return Math.min(config().appearance.x, Math.max(0, Minecraft.getInstance().getWindow().getGuiScaledWidth()-Math.min(config().appearance.width, Minecraft.getInstance().getWindow().getGuiScaledWidth()-24)-12)); }
    public static int offsetY() { return Math.min(config().appearance.y, Math.max(0, Minecraft.getInstance().getWindow().getGuiScaledHeight()-80)); }
    @Override public void onInitializeClient() {
        directory = FabricLoader.getInstance().getConfigDir().resolve("cokechat");
        manager = new ConfigManager(directory.resolve("config.json")); manager.load();
        emojis = EmojiDatabase.load(directory.resolve("emojis.json")); TEXT.configure(config(), emojis);
        history = new ChatHistoryManager(directory.resolve("history"));
        history.configure(config().history.maxMessages, config().history.enabled && config().history.persistent);
        var key = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.cokechat.settings", 297, KeyMapping.Category.register(Identifier.fromNamespaceAndPath("cokechat", "settings"))));
        de.cokechat.rendering.ChatPeek.key=KeyMappingHelper.registerKeyMapping(new KeyMapping("key.cokechat.peek",344,key.getCategory()));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            syncScope(client);
            de.cokechat.rendering.ChatPeek.tick(client);
            while (key.consumeClick()) client.setScreen(new CokeChatSettingsScreen(client.screen));
            if (++ticks % 100 == 0) history.save();
        });
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> history.save());
    }
    private static void syncScope(Minecraft client) {
        if (history == null) return;
        String next = client.level == null ? null : client.getSingleplayerServer() != null
            ? "local:" + client.getSingleplayerServer().getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT).toAbsolutePath().normalize()
            : client.getCurrentServer() != null ? "server:" + client.getCurrentServer().ip : "session";
        if (java.util.Objects.equals(scope, next)) return;
        history.close(); scope = next;
        if (next != null) {
            history.open(next); restoring = true;
            try { for (var line : history.snapshot()) client.gui.getChat().addClientSystemMessage(Component.literal("[History] " + line).withStyle(net.minecraft.ChatFormatting.GRAY)); }
            finally { restoring = false; }
        }
    }
    public static void record(String raw) {
        if (history == null || restoring || !config().enabled || !config().history.enabled) return;
        syncScope(Minecraft.getInstance());
        if (scope != null) history.add(raw);
    }
    public static void prepareChat() { if (!restoring && history != null) syncScope(Minecraft.getInstance()); }
    public static void apply() {
        emojis = EmojiDatabase.load(directory.resolve("emojis.json")); applySettings();
    }
    public static void applySettings() {
        configRevision++;
        manager.save(); TEXT.configure(config(), emojis);
        history.configure(config().history.maxMessages, config().history.enabled && config().history.persistent);
        Minecraft.getInstance().gui.getChat().rescaleChat();
    }
    public static void clearHistory() { history.clear(); Minecraft.getInstance().gui.getChat().clearMessages(true); }
}
