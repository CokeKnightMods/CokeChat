package de.cokechat.mixin;
import de.cokechat.rendering.ChatPeek;
import net.minecraft.client.Minecraft;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(Gui.class)
public abstract class PeekHudMixin {
    @Inject(method="extractRenderState",at=@At("TAIL"))
    private void cokechat$peek(GuiGraphicsExtractor g,DeltaTracker delta,CallbackInfo ci){var c=Minecraft.getInstance();if(c.screen==null)ChatPeek.draw(g,(int)c.mouseHandler.getScaledXPos(c.getWindow()),(int)c.mouseHandler.getScaledYPos(c.getWindow()));}
}
