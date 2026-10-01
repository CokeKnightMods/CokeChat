package de.cokechat.gui;
import de.cokechat.rendering.RoundedRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
/** Original flat controls, using vanilla input, focus, sound and narration behavior. */
public final class StyleButton extends Button {
    private boolean selected,primary,danger;
    private final de.cokechat.animation.Motion hover=new de.cokechat.animation.Motion();
    public StyleButton selected(boolean value){selected=value;return this;}
    public StyleButton primary(){primary=true;return this;}
    public StyleButton danger(){danger=true;return this;}
    public StyleButton(String label,int x,int y,int width,int height,Runnable action) {
        super(x,y,width,height,Component.literal(label),b->action.run(),DEFAULT_NARRATION);
        UiTheme.hint(this,label);
    }
    @Override protected void extractContents(GuiGraphicsExtractor g,int mx,int my,float delta) {
        long now=de.cokechat.animation.Motion.now();double target=active&&isHoveredOrFocused()?1:0;if(hover.target()!=target)hover.to(target,now,80,de.cokechat.config.CokeChatConfig.Easing.CUBIC_OUT);
        int bg=!active?0xFF1B232D:UiTheme.blend(selected||primary?0xFF294751:UiTheme.CONTROL,UiTheme.HOVER,hover.value(now));
        RoundedRenderer.fill(g,getX(),getY(),getWidth(),getHeight(),4,bg);
        if(isFocused()&&active)g.outline(getX(),getY(),getWidth(),getHeight(),UiTheme.ACCENT);
        if(selected)g.fill(getX()+3,getY()+5,getX()+5,getBottom()-5,UiTheme.ACCENT);
        var font=Minecraft.getInstance().font;String text=UiTheme.fit(getMessage().getString(),getWidth()-12);
        int color=!active?UiTheme.DISABLED:danger?UiTheme.ERROR:selected||primary||getMessage().getString().equals("On")?UiTheme.ACCENT:UiTheme.TEXT;
        g.centeredText(font,text,getX()+getWidth()/2,getY()+(getHeight()-8)/2,color);
    }
}
