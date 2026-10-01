package de.cokechat.rendering;
import de.cokechat.config.CokeChatConfig;
/** Screen-space bounds shared by the real input and local preview. */
public record ChatInputLayout(int x,int y,int width,int height) {
    public static ChatInputLayout of(CokeChatConfig c,int screenWidth,int screenHeight,int chatX,int chatBottom){
        var i=c.input;boolean relative=i.positionMode==CokeChatConfig.PositionMode.RELATIVE_TO_CHAT;
        int w=Math.min(i.width,Math.max(40,screenWidth-8)),h=Math.min(Math.max(i.height,(int)Math.ceil(i.fontSize)+8),Math.max(14,screenHeight-8));
        int x=(relative?chatX:0)+i.x,y=(relative?chatBottom:0)+i.y;
        return new ChatInputLayout(Math.max(2,Math.min(screenWidth-w-2,x)),Math.max(2,Math.min(screenHeight-h-2,y)),w,h);
    }
    public void draw(net.minecraft.client.gui.GuiGraphicsExtractor g,CokeChatConfig c){ChatBackgroundRenderer.panel(g,x,y,width,height,c.appearance.inputRadius,c.appearance.inputColor,c.appearance.inputOpacity,c.input.borderColor,c.appearance.inputBorder?c.input.borderOpacity:0);}
}
