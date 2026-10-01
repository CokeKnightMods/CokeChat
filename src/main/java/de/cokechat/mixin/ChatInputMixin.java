package de.cokechat.mixin;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import de.cokechat.rendering.ChatInputStyle;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.MouseButtonEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(EditBox.class)
public abstract class ChatInputMixin {
    @WrapMethod(method="extractWidgetRenderState")
    private void cokechat$font(GuiGraphicsExtractor g,int mx,int my,float delta,Operation<Void> original){
        double s=ChatInputStyle.scale(this);if(ChatInputStyle.target!=(Object)this||!de.cokechat.CokeChatClient.config().enabled){original.call(g,mx,my,delta);return;}
        var box=(EditBox)(Object)this;var bounds=ChatInputStyle.bounds;boolean clip=bounds!=null;
        if(clip)g.enableScissor(bounds.x()+4,bounds.y(),bounds.x()+bounds.width()-4,bounds.y()+bounds.height());
        g.pose().pushMatrix();g.pose().translate(box.getX(),box.getY());g.pose().scale((float)s,(float)s);g.pose().translate(-box.getX(),-box.getY());
        try{original.call(g,mx,my,delta);}finally{g.pose().popMatrix();if(clip)g.disableScissor();}
    }
    @Inject(method="getInnerWidth",at=@At("RETURN"),cancellable=true)
    private void cokechat$width(CallbackInfoReturnable<Integer> cir){double s=ChatInputStyle.scale(this);if(s!=1)cir.setReturnValue((int)(cir.getReturnValue()/s));}
    @ModifyVariable(method="findClickedPositionInText",at=@At("HEAD"),argsOnly=true)
    private MouseButtonEvent cokechat$mouse(MouseButtonEvent event){double s=ChatInputStyle.scale(this);var box=(EditBox)(Object)this;return s==1?event:new MouseButtonEvent(box.getX()+(event.x()-box.getX())/s,event.y(),event.buttonInfo());}
    @Inject(method="getScreenX",at=@At("RETURN"),cancellable=true)
    private void cokechat$cursor(CallbackInfoReturnable<Integer> cir){double s=ChatInputStyle.scale(this);var box=(EditBox)(Object)this;if(s!=1)cir.setReturnValue(box.getX()+(int)((cir.getReturnValue()-box.getX())*s));}
}
