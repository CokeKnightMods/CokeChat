package de.cokechat.mixin;
import de.cokechat.rendering.ChatPeek;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(Screen.class)
public abstract class PeekScreenMixin {
    @Inject(method="extractRenderStateWithTooltipAndSubtitles",at=@At("TAIL"))
    private void cokechat$peek(GuiGraphicsExtractor g,int mx,int my,float delta,CallbackInfo ci){ChatPeek.draw(g,mx,my);}
}
