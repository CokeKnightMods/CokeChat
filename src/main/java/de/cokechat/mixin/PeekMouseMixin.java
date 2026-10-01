package de.cokechat.mixin;
import de.cokechat.rendering.ChatPeek;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.MouseButtonInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
/** Local UI event consumption only; never enters a chat-send or network path. */
@Mixin(MouseHandler.class)
public abstract class PeekMouseMixin {
    @Inject(method="onScroll",at=@At("HEAD"),cancellable=true)
    private void cokechat$scroll(long window,double x,double y,CallbackInfo ci){if(ChatPeek.scroll(y))ci.cancel();}
    @Inject(method="onButton",at=@At("HEAD"),cancellable=true)
    private void cokechat$click(long window,MouseButtonInfo button,int action,CallbackInfo ci){if(ChatPeek.click(button.button(),action))ci.cancel();}
}
