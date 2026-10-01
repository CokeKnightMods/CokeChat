package de.cokechat.mixin;
import de.cokechat.CokeChatClient;
import de.cokechat.emoji.EmojiAutocomplete;
import de.cokechat.gui.CokeChatSettingsScreen;
import de.cokechat.rendering.RoundedRenderer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.*;
import net.minecraft.client.input.*;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

/** Only decoration and explicit completion actions. handleChatInput is deliberately untouched. */
@Mixin(ChatScreen.class)
public abstract class ChatScreenMixin extends Screen {
    @Shadow protected EditBox input;
    @Shadow private CommandSuggestions commandSuggestions;
    @Unique private final EmojiAutocomplete cokechat$completion = new EmojiAutocomplete();
    @Unique private int cokechat$inputWidth=-1;
    @Unique private double cokechat$inputFont=-1;
    protected ChatScreenMixin(Component title) { super(title); }
    @Inject(method = "init", at = @At("TAIL"))
    private void cokechat$init(CallbackInfo ci) {
        input.setWidth(Math.max(40, width-70));
        if(CokeChatClient.config().enabled){de.cokechat.rendering.ChatPresentation.opening.start(de.cokechat.animation.Motion.now());cokechat$layout();}
        addRenderableWidget(Button.builder(Component.literal("CC"), b -> minecraft.setScreen(new CokeChatSettingsScreen(this))).bounds(width-38, height-22, 32, 20).build());
    }
    @Unique private void cokechat$layout(){
        var c=CokeChatClient.config();if(!c.enabled)return;
        var box=de.cokechat.rendering.ChatInputLayout.of(c,width,height,CokeChatClient.offsetX(),(int)de.cokechat.rendering.ChatPresentation.baseBottom());
        if(input.getValue().startsWith("/")||commandSuggestions!=null&&commandSuggestions.isVisible())box=de.cokechat.rendering.CommandPopupLayout.reserveAbove(box,height,c.input.fontSize);
        de.cokechat.rendering.ChatInputStyle.bounds=box;
        boolean newInput=de.cokechat.rendering.ChatInputStyle.target!=input;
        de.cokechat.rendering.ChatInputStyle.target=input;
        input.setX(box.x()+5);input.setY(box.y()+(box.height()-(int)Math.ceil(c.input.fontSize))/2);input.setWidth(box.width()-10);input.setHeight((int)Math.ceil(c.input.fontSize)+2);
        input.setTextColor(RoundedRenderer.color(c.input.textColor,c.input.textOpacity));input.setTextColorUneditable(RoundedRenderer.color(c.input.textColor,c.input.textOpacity));
        de.cokechat.rendering.ChatInputStyle.target=input;
        if(newInput||cokechat$inputWidth!=input.getWidth()||cokechat$inputFont!=c.input.fontSize){input.setCursorPosition(input.getCursorPosition());input.setCursorPosition(input.getCursorPosition());cokechat$inputWidth=input.getWidth();cokechat$inputFont=c.input.fontSize;}
        if(newInput&&commandSuggestions!=null)commandSuggestions.updateCommandInfo();
        if(commandSuggestions!=null)((de.cokechat.rendering.CommandPopupAccess)commandSuggestions).cokechat$preparePopup();
    }
    @Inject(method="removed",at=@At("TAIL"))
    private void cokechat$closed(CallbackInfo ci){de.cokechat.rendering.ChatPresentation.opening.stop();de.cokechat.rendering.ChatPresentation.commandInset=0;de.cokechat.rendering.ChatInputStyle.target=null;de.cokechat.rendering.ChatInputStyle.bounds=null;}
    @Inject(method="extractRenderState",at=@At("HEAD"))
    private void cokechat$layoutFrame(GuiGraphicsExtractor g,int mx,int my,float delta,CallbackInfo ci){cokechat$layout();}
    @Redirect(method = "extractRenderState", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;fill(IIIII)V"))
    private void cokechat$input(GuiGraphicsExtractor g, int x0, int y0, int x1, int y1, int color) {
        var c = CokeChatClient.config(); if (!c.enabled) { g.fill(x0,y0,x1,y1,color); return; }
    }
    @Inject(method="extractRenderState",at=@At(value="INVOKE",target="Lnet/minecraft/client/gui/screens/Screen;extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIF)V"))
    private void cokechat$inputLayer(GuiGraphicsExtractor g,int mx,int my,float delta,CallbackInfo ci){var c=CokeChatClient.config();if(c.enabled&&de.cokechat.rendering.ChatInputStyle.bounds!=null)de.cokechat.rendering.ChatInputStyle.bounds.draw(g,c);}
    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void cokechat$complete(GuiGraphicsExtractor g, int mx, int my, float delta, CallbackInfo ci) { cokechat$completion.draw(g, font, input); }
    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void cokechat$key(KeyEvent event, CallbackInfoReturnable<Boolean> cir) {
        if (event.key() == 297) { minecraft.setScreen(new CokeChatSettingsScreen(this)); cir.setReturnValue(true); }
        else if (cokechat$completion.key(event,input)) cir.setReturnValue(true);
    }
    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void cokechat$click(MouseButtonEvent event, boolean doubleClick, CallbackInfoReturnable<Boolean> cir) {
        if(de.cokechat.rendering.CommandPopup.enabled(input)&&commandSuggestions.mouseClicked(event))cir.setReturnValue(true);
        else if (cokechat$completion.click(event,input)) cir.setReturnValue(true);
        else if(event.button()==0&&de.cokechat.rendering.ChatActions.click((de.cokechat.chat.ChatAccess)minecraft.gui.getChat(),event.x(),event.y(),false))cir.setReturnValue(true);
    }
    @Redirect(method="mouseScrolled",at=@At(value="INVOKE",target="Lnet/minecraft/client/gui/components/ChatComponent;scrollChat(I)V"))
    private void cokechat$smoothScroll(ChatComponent chat,int amount){
        if(!CokeChatClient.config().enabled){chat.scrollChat(amount);return;}
        boolean shift=minecraft.hasShiftDown();((de.cokechat.chat.ChatAccess)chat).cokechat$wheel(amount/(shift?1.0:7.0),shift,false);
    }
    @Redirect(method="keyPressed",at=@At(value="INVOKE",target="Lnet/minecraft/client/gui/components/ChatComponent;scrollChat(I)V"))
    private void cokechat$smoothPage(ChatComponent chat,int amount){
        if(!CokeChatClient.config().enabled){chat.scrollChat(amount);return;}
        ((de.cokechat.chat.ChatAccess)chat).cokechat$wheel(amount/CokeChatClient.config().animations.scrollSpeed,minecraft.hasShiftDown(),false);
    }
}
