package de.cokechat.gui;

import de.cokechat.rendering.RoundedRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/** Shared visual tokens and text/layout rules for every CokeChat settings screen. */
public final class UiTheme {
    public static final int BACKGROUND=0xFF10151D,SURFACE=0xFF19212C,CONTROL=0xFF263240,HOVER=0xFF334353;
    public static final int TEXT=0xFFEAF1F5,MUTED=0xFFACBAC7,ACCENT=0xFF83DCE5,ERROR=0xFFFFA5A5,DISABLED=0xFF7E8B98;
    private UiTheme(){}
    public static String fit(String text,int width){var font=Minecraft.getInstance().font;if(font.width(text)<=width)return text;return font.plainSubstrByWidth(text,Math.max(0,width-font.width("…")))+"…";}
    public static void text(GuiGraphicsExtractor g,String text,int x,int y,int width,int color){g.text(Minecraft.getInstance().font,fit(text,width),x,y,color);}
    public static void card(GuiGraphicsExtractor g,int x,int y,int width,int height){RoundedRenderer.fill(g,x,y,width,height,6,SURFACE);}
    public static void shell(GuiGraphicsExtractor g,int width,int height,String title,String subtitle){
        g.fill(0,0,width,height,BACKGROUND);
        g.blit(Identifier.fromNamespaceAndPath("cokechat","icon.png"),12,10,38,36,0,1,0,1);
        g.pose().pushMatrix();g.pose().translate(46,10);g.pose().scale(1.2f,1.2f);text(g,title,0,0,(int)((width-60)/1.2),TEXT);g.pose().popMatrix();text(g,subtitle,46,27,width-60,MUTED);
        var client=Minecraft.getInstance();int mx=(int)client.mouseHandler.getScaledXPos(client.getWindow()),my=(int)client.mouseHandler.getScaledYPos(client.getWindow());
        if(mx>=46&&mx<width-12&&my>=8&&my<25)g.setTooltipForNextFrame(Component.literal(title),mx,my);
        g.fill(12,43,width-12,44,0xFF2B3743);g.fill(12,height-36,width-12,height-35,0xFF2B3743);
    }
    public static EditBox edit(int x,int y,int width,String label){
        var box=new ThemedEditBox(x,y,Math.max(24,width),label);
        box.setTextColor(TEXT);box.setTextColorUneditable(DISABLED);box.setTooltip(Tooltip.create(Component.literal(label)));return box;
    }
    public static void hint(AbstractWidget widget,String text){widget.setTooltip(Tooltip.create(Component.literal(text)));}
    public static int blend(int a,int b,double t){int result=0xFF000000;for(int shift:new int[]{0,8,16})result|=(int)Math.round(((a>>shift)&255)*(1-t)+((b>>shift)&255)*t)<<shift;return result;}
    public static void lines(GuiGraphicsExtractor g,String text,int x,int y,int width,int maxLines,int color){
        var font=Minecraft.getInstance().font;var lines=font.split(Component.literal(text),Math.max(20,width));
        for(int i=0;i<Math.min(maxLines,lines.size());i++)g.text(font,lines.get(i),x,y+i*11,color);
    }
}
