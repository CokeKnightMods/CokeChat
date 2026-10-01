package de.cokechat.rendering;
import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import java.util.*;

/** Uses only public APIs; no copied command trees or mod-name allowlists. */
public final class CommandDispatchers {
    private CommandDispatchers() {}
    public static List<CommandDispatcher<ClientSuggestionProvider>> active(ClientSuggestionProvider source,CommandDispatcher<ClientSuggestionProvider> connection){
        var result=new ArrayList<CommandDispatcher<ClientSuggestionProvider>>();
        if(FabricLoader.getInstance().isModLoaded("fabric-command-api-v2")){
            var client=FabricCommands.active(source);if(client!=null)result.add(client);
        }
        if(connection!=null)result.add(connection);
        return result;
    }
    private static final class FabricCommands {
        @SuppressWarnings({"unchecked","rawtypes"})
        static CommandDispatcher<ClientSuggestionProvider> active(ClientSuggestionProvider source){
            if(!((Object)source instanceof net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource))return null;
            return (CommandDispatcher)net.fabricmc.fabric.api.client.command.v2.ClientCommands.getActiveDispatcher();
        }
    }
}
