package de.cokechat.gui;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import de.cokechat.rendering.RoundedRenderer;
/** Vanilla editing, selection and narration with the same chrome as CokeChat buttons. */
public final class ThemedEditBox extends EditBox {
    public ThemedEditBox(int x,int y,int width,String label){super(Minecraft.getInstance().font,x,y,width,20,Component.literal(label));}
    @Override public void extractWidgetRenderState(GuiGraphicsExtractor g,int mx,int my,float delta){
        if(!visible)return;
        int x=getX(),y=getY(),w=getWidth(),h=getHeight();
        RoundedRenderer.fill(g,x,y,w,h,4,UiTheme.CONTROL);
        g.fill(x+4,y+h-2,x+w-4,y+h-1,isFocused()?UiTheme.ACCENT:0xFF647381);
        // Keep the exact vanilla four-pixel text inset and inner width; only omit its texture.
        setBordered(false);setX(x+4);setY(y+(h-8)/2);setWidth(w-8);
        try{super.extractWidgetRenderState(g,mx,my,delta);}finally{setWidth(w);setX(x);setY(y);setBordered(true);}
    }
}
