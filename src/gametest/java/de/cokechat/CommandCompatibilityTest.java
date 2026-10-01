package de.cokechat;

import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.suggestion.Suggestions;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import java.util.concurrent.CompletableFuture;

final class CommandCompatibilityTest {
    private static Object field(Object object,String name){try{var f=object.getClass().getDeclaredField(name);f.setAccessible(true);return f.get(object);}catch(Exception e){throw new AssertionError(e);}}
    private static CommandSuggestions commands(ChatScreen screen){return (CommandSuggestions)field(screen,"commandSuggestions");}
    private static EditBox input(ChatScreen screen){return (EditBox)field(screen,"input");}
    @SuppressWarnings("unchecked") private static CompletableFuture<Suggestions> pending(ChatScreen screen){return (CompletableFuture<Suggestions>)field(commands(screen),"pendingSuggestions");}
    private static void open(ClientGameTestContext context,String value){context.setScreen(()->new ChatScreen(value,false));context.waitTicks(2);}
    private static void valid(ClientGameTestContext context,String value){
        open(context,value);context.runOnClient(client->{
            var parse=(ParseResults<?>)field(commands((ChatScreen)client.screen),"currentParse");
            if(parse==null||parse.getReader().canRead()||parse.getContext().getCommand()==null&&parse.getContext().getChild()==null)throw new AssertionError("Valid command rejected: "+value+" parse="+(parse==null?"null":parse.getReader().getString()+" remaining="+parse.getReader().getRemaining()+" errors="+parse.getExceptions()));
            for(var line:(java.util.List<?>)field(commands((ChatScreen)client.screen),"commandUsage"))
                ((net.minecraft.util.FormattedCharSequence)line).accept((index,style,code)->{
                    if(style.getColor()!=null&&style.getColor().getValue()==0xFF5555)throw new AssertionError("Error hint for valid command: "+value);
                    return true;
                });
        });
    }
    private static void suggestion(ClientGameTestContext context,String value,String expected){
        open(context,value);
        context.waitFor(client->{var f=pending((ChatScreen)client.screen);return f!=null&&f.isDone();});
        context.runOnClient(client->{var screen=(ChatScreen)client.screen;
            if(pending(screen).join().getList().stream().noneMatch(s->s.getText().equals(expected)))throw new AssertionError("Missing provider result "+expected+" for "+value);
            commands(screen).showSuggestions(false);
        });
        context.waitTicks(2);
        context.runOnClient(client->{var cs=commands((ChatScreen)client.screen);de.cokechat.rendering.CommandPopup popup=null;
            for(var f:cs.getClass().getDeclaredFields())if(f.getType()==de.cokechat.rendering.CommandPopup.class)try{f.setAccessible(true);popup=(de.cokechat.rendering.CommandPopup)f.get(cs);}catch(Exception e){throw new AssertionError(e);}
            var p=popup==null?null:popup.layout();var box=de.cokechat.rendering.ChatInputStyle.bounds;
            if(p==null||p.rows()==0||p.y()+p.height()>box.y()-5||p.x()<0||p.x()+p.width()>client.getWindow().getGuiScaledWidth())throw new AssertionError("Provider popup misplaced");
        });
    }
    static void run(ClientGameTestContext context){
        context.runOnClient(client->{var c=CokeChatClient.config();c.input.positionMode=de.cokechat.config.CokeChatConfig.PositionMode.ABSOLUTE_SCREEN;c.input.x=95;c.input.y=170;c.input.width=240;c.input.fontSize=12;CokeChatClient.applySettings();});
        suggestion(context,"/c","cs");context.takeScreenshot("cokechat-mod-cs-completion");
        valid(context,"/cs");context.takeScreenshot("cokechat-mod-cs-valid");
        int serverBefore=CommandCompatibilityFixtures.serverRuns.get();
        context.getInput().pressKey(257);
        if(net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded("lotus_arsenal")){
            context.waitFor(client->client.screen!=null&&client.screen.getClass().getName().equals("lotus.arsenal.LoadoutScreen"));
            context.takeScreenshot("cokechat-mod-cs-opened");
        }else context.waitFor(client->CommandCompatibilityFixtures.localRuns.get()>0);
        if(CommandCompatibilityFixtures.serverRuns.get()!=serverBefore)throw new AssertionError("Local /cs reached server fixture");
        for(String value:new String[]{"/cc_client","/cc_client choose ClientValue_first","/cc_alias choose ClientValue_first","/warp hub","/play sb","/party invite TestPlayer"})valid(context,value);
        suggestion(context,"/cc_client ","choose");
        suggestion(context,"/cc_client ","remote");
        valid(context,"/cc_client remote");
        suggestion(context,"/cc_client choose ","ServerValue_first");
        context.runOnClient(client->{if(pending((ChatScreen)client.screen).join().getList().stream().noneMatch(s->s.getText().equals("ClientValue_first")))throw new AssertionError("Client/server providers were not merged");});
        context.takeScreenshot("cokechat-merged-client-server");
        suggestion(context,"/cc_alias choose ","ClientValue_first");
        suggestion(context,"/party ","invite");
        suggestion(context,"/party invite ","ServerPlayer_first");
        context.takeScreenshot("cokechat-server-dynamic-completion");
        CommandCompatibilityFixtures.dynamic="second";
        suggestion(context,"/cc_client choose ","ClientValue_second");
        suggestion(context,"/party invite ","ServerPlayer_second");
        context.getInput().pressKey(258);
        context.runOnClient(client->{if(!input((ChatScreen)client.screen).getValue().equals("/party invite ServerPlayer_second"))throw new AssertionError("Server completion insertion changed");});
        context.getInput().pressKey(257);
        context.waitFor(client->CommandCompatibilityFixtures.serverRuns.get()==serverBefore+1);
        if(!CommandCompatibilityFixtures.received.equals("party invite ServerPlayer_second")||CommandCompatibilityFixtures.providerRuns.get()<2)throw new AssertionError("Server provider/routing failed");
        int localBefore=CommandCompatibilityFixtures.localRuns.get();
        valid(context,"/cc_client");context.getInput().pressKey(257);context.waitFor(client->CommandCompatibilityFixtures.localRuns.get()==localBefore+1);
        if(CommandCompatibilityFixtures.serverRuns.get()!=serverBefore+1)throw new AssertionError("Local/server precedence changed");
        open(context,"/cc_opaque arbitrary :fire: payload");context.getInput().pressKey(257);
        context.waitFor(client->CommandCompatibilityFixtures.opaque.equals("cc_opaque arbitrary :fire: payload"));
        open(context,"/cc_delayed old");
        context.runOnClient(client->input((ChatScreen)client.screen).setValue("/cc_delayed new"));context.waitTicks(2);
        context.runOnClient(client->CommandCompatibilityFixtures.complete("new"));context.waitTicks(2);
        context.runOnClient(client->{if(pending((ChatScreen)client.screen).join().getList().stream().noneMatch(s->s.getText().equals("new_result")))throw new AssertionError("Delayed current suggestions missing");CommandCompatibilityFixtures.complete("old");});context.waitTicks(2);
        context.runOnClient(client->{var screen=(ChatScreen)client.screen;var list=(CommandSuggestions.SuggestionsList)field(commands(screen),"suggestions");if(list==null||((de.cokechat.mixin.SuggestionsListAccess)list).cokechat$options().stream().anyMatch(s->s.getText().equals("old_result")))throw new AssertionError("Stale async response replaced current popup");});
        if(net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded("athen")){
            context.runOnClient(client->{var dispatcher=net.fabricmc.fabric.api.client.command.v2.ClientCommands.getActiveDispatcher();var root=dispatcher.getRoot().getChild("athen");if(root==null)throw new AssertionError("Installed Athen did not register its root");System.out.println("Actual Athen root executable="+(root.getCommand()!=null)+" children="+root.getChildren().stream().map(n->n.getName()).toList());});
            suggestion(context,"/ath","athen");context.takeScreenshot("cokechat-athen-prefix");
            for(String text:new String[]{"/ath_","/athen"}){
                open(context,text);context.runOnClient(client->{if(!((java.util.List<?>)field(commands((ChatScreen)client.screen),"commandUsage")).isEmpty())throw new AssertionError("Large error hint while typing root: "+text);});
                context.takeScreenshot(text.equals("/athen")?"cokechat-athen-root":"cokechat-athen-typing");
            }
            suggestion(context,"/athen ","config");context.takeScreenshot("cokechat-athen-subcommands");
            valid(context,"/athen config");context.getInput().pressKey(257);
            context.waitFor(client->client.screen!=null&&!(client.screen instanceof ChatScreen));
            context.takeScreenshot("cokechat-athen-config-opened");
        }
        System.out.println("CokeChat command compatibility passed: cs="+(net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded("lotus_arsenal")?"installed CokeKnightAddons":"fixture")+", client alias/dynamic provider, server tree/remote provider, local/server routing, opaque input.");
    }
}
