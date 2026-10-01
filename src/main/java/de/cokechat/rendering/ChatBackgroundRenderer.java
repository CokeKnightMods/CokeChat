package de.cokechat.rendering;
import de.cokechat.CokeChatClient;
import net.minecraft.client.gui.GuiGraphicsExtractor;
public final class ChatBackgroundRenderer {
    public static int top, bottom;
    public static boolean panelDrawn;
    public static void fill(GuiGraphicsExtractor g, int x0, int y0, int x1, int y1, int color) {
        var c = CokeChatClient.config(); var a = c.appearance;
        if (!c.enabled) { g.fill(x0, y0, x1, y1, color); return; }
        if (x0 >= 0 && x1 - x0 <= 2) {
            if (a.scrollbar && x1 > x0 + 1) {
                int height=Math.max(8,Math.abs(y1-y0)), y=Math.max(top,Math.min(bottom-height,Math.min(y0,y1)));
                RoundedRenderer.fill(g, x0, y, a.scrollbarWidth, height, a.scrollbarWidth / 2, RoundedRenderer.color(a.scrollbarColor, a.scrollbarOpacity*(ChatPresentation.peek?ChatPeek.opacity():1)));
            }
            return;
        }
        if(ChatPresentation.active&&ChatPresentation.line){
            if(!panelDrawn){
                panelDrawn=true;
                double reveal=ChatPresentation.reveal();int w=(int)Math.round((x1-x0)*reveal),h=(int)Math.round((bottom-top)*reveal);
                double alpha=(ChatPresentation.peek?c.peek.opacity:a.backgroundOpacity)*(ChatPresentation.peek?ChatPeek.opacity():1);
                panel(g,x0,bottom-h,w,h,a.cornerRadius,a.backgroundColor,alpha,a.borderColor,a.chatBorder?a.borderOpacity*(ChatPresentation.peek?ChatPeek.opacity():1):0);
            }
            return;
        }
        int tint = (color & 0xFF000000) | a.backgroundColor;
        if ((ChatPresentation.active&&ChatPresentation.line || y0 >= top && y1 <= bottom) && bottom > top) RoundedRenderer.band(g, x0, top, x1-x0, bottom-top, y0, y1, a.cornerRadius, tint);
        else RoundedRenderer.fill(g, x0, y0, x1-x0, y1-y0, a.cornerRadius, tint);
    }
    public static void panel(GuiGraphicsExtractor g,int x,int y,int w,int h,int radius,int rgb,double alpha,int border,double borderAlpha){
        RoundedRenderer.fill(g,x,y,w,h,radius,RoundedRenderer.color(rgb,alpha));
        if(borderAlpha>0&&w>2&&h>2){
            int color=RoundedRenderer.color(border,borderAlpha);int r=Math.min(radius,Math.min(w,h)/2);
            g.fill(x+r,y,x+w-r,y+1,color);g.fill(x+r,y+h-1,x+w-r,y+h,color);
            g.fill(x,y+r,x+1,y+h-r,color);g.fill(x+w-1,y+r,x+w,y+h-r,color);
        }
    }
}
