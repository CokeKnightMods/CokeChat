package de.cokechat.mixin;

import de.cokechat.CokeChatClient;
import de.cokechat.compact.CompactChatManager;
import de.cokechat.rendering.ChatBackgroundRenderer;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.multiplayer.chat.GuiMessage;
import net.minecraft.util.FormattedCharSequence;
import org.joml.Matrix3x2f;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import de.cokechat.chat.*;
import de.cokechat.animation.*;
import de.cokechat.rendering.*;

/** Retains raw messages, signatures, deletion markers, filtering and all vanilla input/networking. */
@Mixin(ChatComponent.class)
public abstract class ChatComponentMixin implements ChatAccess {
    @Shadow @Final @Mutable private List<GuiMessage.Line> trimmedMessages;
    @Shadow @Final @Mutable private List<GuiMessage> allMessages;
    @Shadow private int chatScrollbarPos;
    @Shadow private boolean newMessageSinceScroll;
    @Shadow private void refreshTrimmedMessages() { throw new AssertionError(); }
    @Unique private final CompactChatManager cokechat$groups = new CompactChatManager();
    @Unique private final java.util.Map<Object,GuiMessage> cokechat$previous=new java.util.HashMap<>();
    @Unique private int cokechat$count = 1;
    @Unique private boolean cokechat$evicted;
    @Unique private final LocalMessageState<GuiMessage> cokechat$state=new LocalMessageState<>();
    @Unique private final SmoothScroll cokechat$normalScroll=new SmoothScroll(),cokechat$peekScroll=new SmoothScroll();
    @Unique private final AnimatedChatGraphics cokechat$animated=new AnimatedChatGraphics();
    @Unique private List<GuiMessage.Line> cokechat$peekLines=List.of();
    @Unique private int cokechat$revision,cokechat$peekRevision=-1,cokechat$peekWidth;
    @Unique private final java.util.IdentityHashMap<GuiMessage,net.minecraft.network.chat.Component> cokechat$display=new java.util.IdentityHashMap<>();

    @Override public LocalMessageState<GuiMessage> cokechat$state(){return cokechat$state;}
    @Unique private Object cokechat$key(GuiMessage.Line line){var group=cokechat$state.group(line.parent());return group==null?line.parent():group.id;}
    @WrapMethod(method="addMessage")
    private void cokechat$preserveReading(net.minecraft.network.chat.Component content,net.minecraft.network.chat.MessageSignature signature,net.minecraft.client.multiplayer.chat.GuiMessageSource source,net.minecraft.client.multiplayer.chat.GuiMessageTag tag,Operation<Void> original){
        if(!CokeChatClient.config().enabled){original.call(content,signature,source,tag);return;}
        if(chatScrollbarPos!=(int)Math.round(cokechat$normalScroll.target()))cokechat$normalScroll.anchor(chatScrollbarPos-Math.round(cokechat$normalScroll.target()));
        long now=Motion.now();var normal=ViewportAnchor.capture(trimmedMessages,cokechat$normalScroll.value(now),cokechat$normalScroll.target(),this::cokechat$key);
        boolean wasPeek=ChatPresentation.peek;ChatPresentation.peek=true;
        var overlay=cokechat$peekScroll.target()<.01&&cokechat$peekScroll.value(now)<.01?new ViewportAnchor(null,0,0,true):ViewportAnchor.capture(cokechat$lines(true),cokechat$peekScroll.value(now),cokechat$peekScroll.target(),this::cokechat$key);ChatPresentation.peek=wasPeek;
        original.call(content,signature,source,tag);
        if(normal.following())cokechat$normalScroll.reset();else cokechat$normalScroll.anchor(normal.delta(trimmedMessages,this::cokechat$key));
        cokechat$normalScroll.clamp(Math.max(0,trimmedMessages.size()-getLinesPerPage()));
        chatScrollbarPos=(int)Math.round(cokechat$normalScroll.target());newMessageSinceScroll=!normal.following();
        ChatPresentation.peek=true;
        try{if(overlay.following())cokechat$peekScroll.reset();else{var lines=cokechat$lines(true);cokechat$peekScroll.anchor(overlay.delta(lines,this::cokechat$key));cokechat$peekScroll.clamp(Math.max(0,lines.size()-getLinesPerPage()));}}
        finally{ChatPresentation.peek=wasPeek;}
    }
    @Override public SmoothScroll cokechat$scroll(boolean peek){return peek?cokechat$peekScroll:cokechat$normalScroll;}
    @Override public List<GuiMessage.Line> cokechat$lines(boolean peek){
        if(!peek)return trimmedMessages;
        int width=(int)(ChatPresentation.width()/getScale());
        if(cokechat$peekRevision!=cokechat$revision||cokechat$peekWidth!=width){
            var result=new java.util.ArrayList<GuiMessage.Line>();GuiMessage previous=null;var font=Minecraft.getInstance().font;
            for(var line:trimmedMessages){var raw=line.parent();if(raw==previous)continue;previous=raw;
                var content=cokechat$display.getOrDefault(raw,raw.content());var split=new GuiMessage(raw.addedTime(),content,raw.signature(),raw.source(),raw.tag()).splitLines(font,width);
                for(int i=split.size()-1;i>=0;i--)result.add(new GuiMessage.Line(raw,split.get(i),i==split.size()-1));
            }
            cokechat$peekLines=result;cokechat$peekRevision=cokechat$revision;cokechat$peekWidth=width;
        }
        return cokechat$peekLines;
    }
    @Override public void cokechat$wheel(double wheel,boolean slow,boolean peek){
        boolean previous=ChatPresentation.peek;ChatPresentation.peek=peek;
        try{var scroll=cokechat$scroll(peek);scroll.scroll(wheel,slow,Math.max(0,cokechat$lines(peek).size()-getLinesPerPage()),Motion.now(),CokeChatClient.config().animations);if(!peek)chatScrollbarPos=(int)Math.round(scroll.target());}
        finally{ChatPresentation.peek=previous;}
    }
    @Override public void cokechat$hide(GuiMessage raw){cokechat$state.hideGroup(raw);int position=chatScrollbarPos;refreshTrimmedMessages();chatScrollbarPos=Math.max(0,Math.min(position,trimmedMessages.size()-getLinesPerPage()));}
    @Override public void cokechat$drawPeek(GuiGraphicsExtractor g,int mx,int my){
        var originalLines=trimmedMessages;int originalPosition=chatScrollbarPos;ChatPresentation.peek=true;
        try{var lines=cokechat$lines(true);trimmedMessages=lines;chatScrollbarPos=(int)Math.round(cokechat$peekScroll.target());
            ((ChatComponent)(Object)this).extractRenderState(g,Minecraft.getInstance().font,Minecraft.getInstance().gui.getGuiTicks(),mx,my,ChatComponent.DisplayMode.FOREGROUND,false);
        }finally{trimmedMessages=originalLines;chatScrollbarPos=originalPosition;ChatPresentation.peek=false;}
    }

    @Inject(method = {"refreshTrimmedMessages", "clearMessages"}, at = @At("HEAD"))
    private void cokechat$resetGroups(CallbackInfo ci) { cokechat$groups.reset(); cokechat$previous.clear();cokechat$state.rebuild();cokechat$display.clear();cokechat$revision++; }
    @Inject(method="clearMessages",at=@At("TAIL"))
    private void cokechat$clearVisuals(CallbackInfo ci){cokechat$state.clear();cokechat$normalScroll.reset();cokechat$peekScroll.reset();}
    @Inject(method="resetChatScroll",at=@At("TAIL"))
    private void cokechat$resetScroll(CallbackInfo ci){if(!ChatPresentation.peek)cokechat$normalScroll.reset();}
    @Inject(method = "addMessageToDisplayQueue", at = @At("HEAD"), cancellable = true)
    private void cokechat$group(GuiMessage raw, CallbackInfo ci) {
        var c=CokeChatClient.config(); cokechat$count=1;
        cokechat$state.register(raw,Motion.now());cokechat$revision++;
        if (c.enabled&&cokechat$state.hidden(raw)) {
            ci.cancel();return;
        }
        if (!c.enabled || !c.compact.enabled) { cokechat$groups.reset(); cokechat$previous.clear();cokechat$state.attach(raw,null,false,Motion.now());return; }
        var key=java.util.Arrays.asList(raw.content(),raw.source(),raw.tag());
        long cutoff=raw.addedTime()*50L-Math.round(c.compact.groupingSeconds*1000);
        cokechat$previous.values().removeIf(message->message.addedTime()*50L<cutoff||message.addedTime()>raw.addedTime());
        var previous=cokechat$previous.get(key);
        cokechat$count=cokechat$groups.accept(key,raw.addedTime()*50L,c.compact.groupingSeconds).count();
        if (cokechat$count>1) {
            int removed=0;
            for(var iterator=trimmedMessages.iterator();iterator.hasNext();)if(iterator.next().parent()==previous){iterator.remove();removed++;}
            cokechat$display.remove(previous);
            if(chatScrollbarPos>0)chatScrollbarPos=Math.max(0,chatScrollbarPos-removed);
        }
        cokechat$state.attach(raw,previous,cokechat$count>1,Motion.now());cokechat$previous.put(key,raw);
    }
    @Shadow public abstract int getLinesPerPage();
    @Shadow private double getScale() { throw new AssertionError(); }
    @Shadow private int getLineHeight() { throw new AssertionError(); }
    @Unique private static final String COKECHAT_RENDER = "extractRenderState(Lnet/minecraft/client/gui/components/ChatComponent$ChatGraphicsAccess;IILnet/minecraft/client/gui/components/ChatComponent$DisplayMode;)V";

    @Inject(method = "<init>", at = @At("TAIL"))
    private void cokechat$lists(Minecraft minecraft, CallbackInfo ci) { trimmedMessages = new de.cokechat.history.HistoryList<>(); allMessages = new de.cokechat.history.HistoryList<>(); }
    @Inject(method = "addMessage", at = @At("HEAD"))
    private void cokechat$prepareHistory(CallbackInfo ci) { CokeChatClient.prepareChat(); }

    @Inject(method = "getWidth()I", at = @At("HEAD"), cancellable = true)
    private void cokechat$width(CallbackInfoReturnable<Integer> cir) { if (CokeChatClient.config().enabled) cir.setReturnValue(ChatPresentation.width()); }
    @Inject(method = "getHeight()I", at = @At("HEAD"), cancellable = true)
    private void cokechat$height(CallbackInfoReturnable<Integer> cir) { if (CokeChatClient.config().enabled) cir.setReturnValue((int)(ChatPresentation.height()/getScale())); }
    @Inject(method = "getScale", at = @At("HEAD"), cancellable = true)
    private void cokechat$scale(CallbackInfoReturnable<Double> cir) { if (CokeChatClient.config().enabled) cir.setReturnValue(CompactChatManager.scale(CokeChatClient.config())); }
    @Inject(method = "getLineHeight", at = @At("HEAD"), cancellable = true)
    private void cokechat$lineHeight(CallbackInfoReturnable<Integer> cir) { if (CokeChatClient.config().enabled) cir.setReturnValue(CompactChatManager.lineHeight(CokeChatClient.config())); }
    @ModifyVariable(method = COKECHAT_RENDER, at = @At("STORE"), ordinal = 0)
    private double cokechat$spacing(double original) { return CokeChatClient.config().enabled ? (CompactChatManager.lineHeight(CokeChatClient.config()) - 9) / 9.0 : original; }
    @ModifyExpressionValue(method = COKECHAT_RENDER, at = @At(value = "INVOKE", target = "Ljava/lang/Double;floatValue()F", ordinal = 1))
    private float cokechat$opacity(float original) { return CokeChatClient.config().enabled ? (float)(ChatPresentation.peek?CokeChatClient.config().peek.opacity:CokeChatClient.config().appearance.backgroundOpacity) : original; }
    @ModifyExpressionValue(method = COKECHAT_RENDER, at = @At(value = "INVOKE", target = "Ljava/lang/Math;round(D)J"))
    private long cokechat$baseline(long original) { var c=CokeChatClient.config(); return c.enabled ? CompactChatManager.lineHeight(c)-1-CompactChatManager.spacing(c)/2 : original; }
    @Inject(method = "lambda$extractRenderState$0", at = @At("TAIL"))
    private static void cokechat$position(float scale, Matrix3x2f pose, CallbackInfo ci) {
        if (CokeChatClient.config().enabled) pose.translate(ChatPresentation.x() / scale - 4, -ChatPresentation.y() / scale);
    }
    @Inject(method = "extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/gui/Font;IIILnet/minecraft/client/gui/components/ChatComponent$DisplayMode;Z)V", at = @At("HEAD"))
    private void cokechat$bounds(GuiGraphicsExtractor g, Font font, int ticks, int mx, int my, ChatComponent.DisplayMode mode, boolean insert, CallbackInfo ci) {
        int count = 0, end = Math.min(trimmedMessages.size()-chatScrollbarPos, getLinesPerPage());
        for (int i = 0; i < end; i++) if (mode.foreground || ticks - trimmedMessages.get(i + chatScrollbarPos).addedTime() < 200) count = i + 1;
        ChatBackgroundRenderer.bottom = (int)Math.floor((g.guiHeight()-40) / getScale());
        ChatBackgroundRenderer.top = ChatBackgroundRenderer.bottom - (mode.foreground?(int)(ChatPresentation.height()/getScale()):count * getLineHeight());
        ChatBackgroundRenderer.panelDrawn=false;
    }
    @ModifyConstant(method = "addMessageToQueue", constant = @Constant(intValue = 100))
    private int cokechat$historyLimit(int original) { var c = CokeChatClient.config(); return c.enabled && c.history.enabled ? c.history.maxMessages < 0 ? Integer.MAX_VALUE : c.history.maxMessages : original; }
    @ModifyConstant(method = "addMessageToDisplayQueue", constant = @Constant(intValue = 100))
    private int cokechat$lineLimit(int original) { return CokeChatClient.config().enabled && CokeChatClient.config().history.enabled ? Integer.MAX_VALUE : original; }
    @Inject(method = "addMessageToQueue", at = @At("HEAD"))
    private void cokechat$evictLines(GuiMessage message, CallbackInfo ci) {
        int limit = cokechat$historyLimit(100);
        while (allMessages.size() >= limit) {
            cokechat$evicted=true;
            var oldest = allMessages.removeLast();
            while (!trimmedMessages.isEmpty() && trimmedMessages.getLast().parent() == oldest) trimmedMessages.removeLast();
        }
    }
    @Inject(method = "rescaleChat", at = @At("HEAD"))
    private void cokechat$trimOnSettings(CallbackInfo ci) { int limit=cokechat$historyLimit(100); while(allMessages.size()>limit)allMessages.removeLast(); }
    @Redirect(method = "addMessageToDisplayQueue", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/chat/GuiMessage;splitLines(Lnet/minecraft/client/gui/Font;I)Ljava/util/List;"))
    private List<FormattedCharSequence> cokechat$displayOnly(GuiMessage raw, Font font, int width) {
        var display = CokeChatClient.text().process(raw.content());
        if (CokeChatClient.config().enabled && CokeChatClient.config().compact.enabled && cokechat$count>1)
            display=display.copy().append(net.minecraft.network.chat.Component.literal(CompactChatManager.counter(CokeChatClient.config(),cokechat$count)).withStyle(net.minecraft.ChatFormatting.GRAY));
        cokechat$display.put(raw,display);
        return new GuiMessage(raw.addedTime(), display, raw.signature(), raw.source(), raw.tag()).splitLines(font, width);
    }
    @Inject(method = "addMessageToQueue", at = @At("TAIL"))
    private void cokechat$record(GuiMessage message, CallbackInfo ci) {
        CokeChatClient.record(message.content().getString());
        if (cokechat$evicted && CokeChatClient.config().enabled && CokeChatClient.config().compact.enabled) refreshTrimmedMessages();
        if(cokechat$evicted){cokechat$state.retain(allMessages);cokechat$display.keySet().removeIf(raw->cokechat$state.group(raw)==null);}
        cokechat$evicted=false;
    }
    @WrapMethod(method=COKECHAT_RENDER)
    private void cokechat$animate(ChatComponent.ChatGraphicsAccess graphics,int height,int ticks,ChatComponent.DisplayMode mode,Operation<Void> original){
        if(!ChatPresentation.peek&&ChatPeek.active())return;
        if(!CokeChatClient.config().enabled){original.call(graphics,height,ticks,mode);return;}
        int stored=chatScrollbarPos;var scroll=cokechat$scroll(ChatPresentation.peek);
        if(!ChatPresentation.peek && stored!=(int)Math.round(scroll.target()))scroll.anchor(stored-Math.round(scroll.target()));
        scroll.clamp(Math.max(0,trimmedMessages.size()-getLinesPerPage()));
        long now=Motion.now();double value=scroll.value(now);chatScrollbarPos=(int)Math.floor(value);
        ChatPresentation.fraction=value-chatScrollbarPos;ChatPresentation.frame=now;ChatPresentation.active=true;
        cokechat$animated.delegate=graphics;
        try{original.call(cokechat$animated,height,ticks,mode);}finally{chatScrollbarPos=stored;ChatPresentation.active=false;ChatPresentation.line=false;cokechat$animated.delegate=null;}
    }
    @WrapOperation(method="forEachLine",at=@At(value="INVOKE",target="Lnet/minecraft/client/gui/components/ChatComponent$LineConsumer;accept(Lnet/minecraft/client/multiplayer/chat/GuiMessage$Line;IF)V"))
    private void cokechat$line(@Coerce Object consumer,GuiMessage.Line line,int index,float alpha,Operation<Void> original){
        if(ChatPresentation.active)ChatPresentation.beginLine(line.parent(),this,index);
        try{original.call(consumer,line,index,alpha);}finally{ChatPresentation.line=false;}
    }
    @ModifyExpressionValue(method="forEachLine",at=@At(value="INVOKE",target="Lnet/minecraft/client/gui/components/ChatComponent;getLinesPerPage()I"))
    private int cokechat$extraLine(int original){return ChatPresentation.active && ChatPresentation.fraction>0?original+1:original;}
    @WrapMethod(method="extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/gui/Font;IIILnet/minecraft/client/gui/components/ChatComponent$DisplayMode;Z)V")
    private void cokechat$viewport(GuiGraphicsExtractor g,Font font,int ticks,int mx,int my,ChatComponent.DisplayMode mode,boolean insert,Operation<Void> original){
        if(!ChatPresentation.peek&&ChatPeek.active())return;
        boolean clip=CokeChatClient.config().enabled&&!mode.showRestrictedPrompt&&Minecraft.getInstance().getChatListener().queueSize()==0;
        double reveal=ChatPresentation.reveal();
        if(clip)g.enableScissor(ChatPresentation.x()-5,(int)(ChatPresentation.bottom()-ChatPresentation.height()*reveal),ChatPresentation.x()-5+(int)((ChatPresentation.width()+17)*reveal),(int)ChatPresentation.bottom());
        if(clip&&trimmedMessages.isEmpty()&&mode.foreground){var c=CokeChatClient.config();var a=c.appearance;float fade=ChatPresentation.peek?ChatPeek.opacity():1;ChatBackgroundRenderer.panel(g,ChatPresentation.x()-4,(int)(ChatPresentation.bottom()-ChatPresentation.height()*reveal),(int)((ChatPresentation.width()+12)*reveal),(int)(ChatPresentation.height()*reveal),a.cornerRadius,a.backgroundColor,(ChatPresentation.peek?c.peek.opacity:a.backgroundOpacity)*fade,a.borderColor,a.chatBorder?a.borderOpacity*fade:0);}
        try{original.call(g,font,ticks,mx,my,mode,insert);}finally{if(clip)g.disableScissor();}
        if(CokeChatClient.config().enabled&&mode.foreground)ChatActions.draw(g,this,mx,my,ChatPresentation.peek);
    }
}
