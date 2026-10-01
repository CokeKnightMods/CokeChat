package de.cokechat.mixin;
import de.cokechat.rendering.ChatBackgroundRenderer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Changes only background and scrollbar fills. Vanilla text, tags and hover handling survive. */
@Mixin(targets = {"net.minecraft.client.gui.components.ChatComponent$DrawingBackgroundGraphicsAccess", "net.minecraft.client.gui.components.ChatComponent$DrawingFocusedGraphicsAccess"}, remap = false)
public abstract class ChatGraphicsMixin {
    @Shadow(remap = false) @Final private GuiGraphicsExtractor graphics;
    @Inject(method = "fill", at = @At("HEAD"), cancellable = true)
    private void cokechat$fill(int x0, int y0, int x1, int y1, int color, CallbackInfo ci) { ChatBackgroundRenderer.fill(graphics, x0, y0, x1, y1, color); ci.cancel(); }
    // Vanilla renders the left trust/system indicator through handleTag, not scrollbar fill.
    // Leave click capture and tag icons intact; this hook controls the visual strip only.
    @Inject(method="handleTag",at=@At("HEAD"),cancellable=true)
    private void cokechat$sideBar(int x0,int y0,int x1,int y1,float opacity,net.minecraft.client.multiplayer.chat.GuiMessageTag tag,CallbackInfo ci){
        var c=de.cokechat.CokeChatClient.config();if(c.enabled&&!c.appearance.scrollbar)ci.cancel();
    }
}
