package de.cokechat;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.minecraft.commands.Commands;
import java.util.concurrent.atomic.AtomicInteger;

/** Only in the test mod: real registrations on both sides of an integrated connection. */
public final class CommandCompatibilityFixtures implements ModInitializer, ClientModInitializer {
    static final AtomicInteger localRuns=new AtomicInteger(), serverRuns=new AtomicInteger(), providerRuns=new AtomicInteger();
    static volatile String dynamic="first", received="", opaque="";
    private record Pending(com.mojang.brigadier.suggestion.SuggestionsBuilder builder,java.util.concurrent.CompletableFuture<com.mojang.brigadier.suggestion.Suggestions> future) {}
    private static final java.util.List<Pending> delayed=new java.util.concurrent.CopyOnWriteArrayList<>();
    static void complete(String prefix){for(var item:delayed)if(item.builder().getRemaining().equals(prefix)){item.future().complete(item.builder().suggest(prefix+"_result").build());delayed.remove(item);}}
    @Override public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher,access,selection)->{
            // Hypixel-shaped fixtures, not a claim to reproduce Hypixel's live command tree.
            dispatcher.register(Commands.literal("party").then(Commands.literal("invite")
                .then(Commands.argument("player",StringArgumentType.word()).suggests((ctx,builder)->{
                    providerRuns.incrementAndGet();return builder.suggest("ServerPlayer_"+dynamic).buildFuture();
                }).executes(ctx->{received=ctx.getInput();serverRuns.incrementAndGet();return 1;}))));
            dispatcher.register(Commands.literal("warp").then(Commands.literal("hub").executes(ctx->{received=ctx.getInput();serverRuns.incrementAndGet();return 1;})));
            dispatcher.register(Commands.literal("play").then(Commands.literal("sb").executes(ctx->{received=ctx.getInput();serverRuns.incrementAndGet();return 1;})));
            // Same name on both sides: Fabric must still choose the local implementation.
            dispatcher.register(Commands.literal("cc_client").executes(ctx->{serverRuns.incrementAndGet();return 1;})
                .then(Commands.literal("remote").executes(ctx->1))
                .then(Commands.literal("choose").then(Commands.argument("value",StringArgumentType.word())
                    .suggests((ctx,builder)->builder.suggest("ServerValue_"+dynamic).buildFuture()).executes(ctx->1))));
        });
    }
    @Override public void onInitializeClient() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher,access)->{
            dispatcher.register(ClientCommands.literal("cc_delayed").then(ClientCommands.argument("value",StringArgumentType.word()).suggests((ctx,builder)->{
                var future=new java.util.concurrent.CompletableFuture<com.mojang.brigadier.suggestion.Suggestions>();delayed.add(new Pending(builder,future));return future;
            }).executes(ctx->1)));
            dispatcher.register(ClientCommands.literal("cc_client").executes(ctx->{localRuns.incrementAndGet();return 1;})
                .then(ClientCommands.literal("choose").then(ClientCommands.argument("value",StringArgumentType.word())
                    .suggests((ctx,builder)->builder.suggest("ClientValue_"+dynamic).buildFuture())
                    .executes(ctx->{localRuns.incrementAndGet();return 1;}))));
            dispatcher.register(ClientCommands.literal("cc_alias").redirect(dispatcher.getRoot().getChild("cc_client")));
            if(!net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded("lotus_arsenal"))
                dispatcher.register(ClientCommands.literal("cs").executes(ctx->{localRuns.incrementAndGet();return 1;}));
        });
        // Model an opaque mod that intercepts input but publishes no Brigadier metadata.
        ClientSendMessageEvents.ALLOW_COMMAND.register(command->{
            if(command.startsWith("cc_opaque ")){opaque=command;return false;}return true;
        });
    }
}
