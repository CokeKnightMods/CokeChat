package de.cokechat.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.suggestion.Suggestions;
import de.cokechat.chat.CommandAnalysis;
import de.cokechat.rendering.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.network.chat.*;
import net.minecraft.util.FormattedCharSequence;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

@Mixin(CommandSuggestions.class)
public abstract class CommandSuggestionsMixin implements CommandPopupAccess {
    @Shadow @Final private Minecraft minecraft;
    @Shadow @Final private EditBox input;
    @Shadow @Final private List<FormattedCharSequence> commandUsage;
    @Shadow private ParseResults<ClientSuggestionProvider> currentParse;
    @Shadow private CompletableFuture<Suggestions> pendingSuggestions;
    @Shadow private CommandSuggestions.SuggestionsList suggestions;
    @Shadow private boolean keepSuggestions,allowSuggestions,currentParseIsCommand,currentParseIsMessage,commandsAllowed,messagesAllowed;
    @Shadow public abstract void showSuggestions(boolean narrate);
    @Shadow private static boolean hasMessageArguments(ParseResults<ClientSuggestionProvider> parse){throw new AssertionError();}
    @Unique private final CommandPopup cokechat$popup=new CommandPopup();
    @Unique private CommandAnalysis<ClientSuggestionProvider> cokechat$analysis;
    @Unique private Suggestions cokechat$available=CommandAnalysis.empty();
    @Unique private long cokechat$request;

    @Inject(method="updateCommandInfo",at=@At("HEAD"),cancellable=true)
    private void cokechat$commands(CallbackInfo ci){
        long request=++cokechat$request;
        if(!CommandPopup.enabled(input)||!input.getValue().startsWith("/")){cokechat$analysis=null;return;}
        ci.cancel();
        String text=input.getValue();int cursor=input.getCursorPosition();
        var connection=minecraft.getConnection();
        commandUsage.clear();
        if(connection==null){currentParse=null;pendingSuggestions=null;suggestions=null;return;}
        var analysis=new CommandAnalysis<>(text,connection.getSuggestionsProvider(),CommandDispatchers.active(connection.getSuggestionsProvider(),connection.getCommands()));
        cokechat$analysis=analysis;currentParse=analysis.parse();currentParseIsCommand=true;
        currentParseIsMessage=currentParse!=null&&hasMessageArguments(currentParse);
        if(keepSuggestions&&suggestions!=null)return;
        input.setSuggestion(null);suggestions=null;cokechat$available=CommandAnalysis.empty();
        var futures=analysis.request(cursor);
        var all=CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
        pendingSuggestions=all.thenApply(done->analysis.ready(futures));
        Runnable publish=()->minecraft.execute(()->{
            if(request!=cokechat$request||!CommandPopup.enabled(input)||!input.getValue().equals(text)||input.getCursorPosition()!=cursor)return;
            cokechat$available=analysis.ready(futures);
            cokechat$hints(analysis,cursor);
            suggestions=null;input.setSuggestion(null);
            if(allowSuggestions&&minecraft.options.autoSuggestions().get())showSuggestions(false);
        });
        // Publish local completions immediately; a slow server may add more later.
        publish.run();for(var future:futures)future.thenRun(publish);
    }
    @Inject(method="updateUsageInfo",at=@At("HEAD"),cancellable=true)
    private void cokechat$oldResult(ParseResults<ClientSuggestionProvider> parse,Suggestions result,CallbackInfo ci){
        if(cokechat$analysis!=null&&CommandPopup.enabled(input))ci.cancel();
    }
    @Unique private void cokechat$hints(CommandAnalysis<ClientSuggestionProvider> analysis,int cursor){
        commandUsage.clear();
        if(cokechat$available.isEmpty()&&!analysis.typingRoot()){
            Component hint=null;
            switch(analysis.state()){
                case UNKNOWN_ROOT -> hint=Component.translatable("command.unknown.command").withStyle(net.minecraft.ChatFormatting.GRAY);
                case ARGUMENT_ERROR -> hint=analysis.error().map(e->ComponentUtils.fromMessage(e.getRawMessage()))
                    .orElseGet(()->Component.translatable("command.unknown.argument")).copy().withStyle(net.minecraft.ChatFormatting.RED);
                case INCOMPLETE -> {var usage=analysis.usage(cursor);if(!usage.isEmpty())hint=Component.literal(String.join(" | ",usage)).withStyle(net.minecraft.ChatFormatting.GRAY);}
                default -> {}
            }
            if(hint!=null)commandUsage.add(hint.getVisualOrderText());
        }
        if(!commandsAllowed)commandUsage.add(Component.translatable("chat_screen.commands_not_allowed").getVisualOrderText());
        if(currentParseIsMessage&&!messagesAllowed)commandUsage.add(Component.translatable("chat_screen.messages_not_allowed").getVisualOrderText());
    }
    @WrapMethod(method="showSuggestions")
    private void cokechat$show(boolean narrate,Operation<Void> original){
        if(cokechat$analysis==null||!CommandPopup.enabled(input)){original.call(narrate);return;}
        var pending=pendingSuggestions;
        try{pendingSuggestions=CompletableFuture.completedFuture(cokechat$available);original.call(narrate);}
        finally{pendingSuggestions=pending;}
    }
    @Inject(method="formatChat",at=@At("HEAD"),cancellable=true)
    private void cokechat$format(String text,int offset,CallbackInfoReturnable<FormattedCharSequence> cir){
        if(cokechat$analysis!=null&&CommandPopup.enabled(input)&&cokechat$analysis.state()!=CommandAnalysis.State.VALID
                &&(cokechat$analysis.typingRoot()||cokechat$analysis.state()!=CommandAnalysis.State.ARGUMENT_ERROR||!cokechat$available.isEmpty()))
            cir.setReturnValue(FormattedCharSequence.forward(text,Style.EMPTY.withColor(net.minecraft.ChatFormatting.GRAY)));
    }
    @Override public void cokechat$preparePopup(){if(CommandPopup.enabled(input))cokechat$popup.prepare(input,suggestions,commandUsage);}
    @Inject(method="extractRenderState",at=@At("HEAD"),cancellable=true)
    private void cokechat$draw(GuiGraphicsExtractor g,int mx,int my,CallbackInfo ci){if(CommandPopup.enabled(input)){cokechat$popup.draw(g,mx,my,input,suggestions,commandUsage);ci.cancel();}}
    @Inject(method="mouseClicked",at=@At("HEAD"),cancellable=true)
    private void cokechat$click(MouseButtonEvent event,CallbackInfoReturnable<Boolean> cir){if(CommandPopup.enabled(input))cir.setReturnValue(event.button()==0&&cokechat$popup.click(event.x(),event.y(),input,suggestions,commandUsage));}
    @Inject(method="mouseScrolled",at=@At("HEAD"),cancellable=true)
    private void cokechat$scroll(double scroll,CallbackInfoReturnable<Boolean> cir){if(CommandPopup.enabled(input))cir.setReturnValue(cokechat$popup.scroll(scroll,input,suggestions,commandUsage));}
}
