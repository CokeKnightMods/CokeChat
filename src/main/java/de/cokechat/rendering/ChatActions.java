package de.cokechat.rendering;
import de.cokechat.CokeChatClient;
import de.cokechat.animation.Motion;
import de.cokechat.chat.*;
import de.cokechat.compact.CompactChatManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.chat.GuiMessage;
import net.minecraft.network.chat.Component;
/** Hover geometry is computed from the same displayed lines, scroll and animation as vanilla text. */
public final class ChatActions {
    private static final Hover normal=new Hover(),peek=new Hover();
    private static final Component COPY=Component.literal("Copy message"),DELETE=Component.literal("Delete local group");
    private static final class Hover {
        GuiMessage raw,last;Object groupId;
        int x,y,size,gap;long since;int over=-1;
        final Motion fade=new Motion();
    }
    private static Hover update(ChatAccess access,double mx,double my,boolean peeking){
        var h=peeking?peek:normal;var c=CokeChatClient.config();long now=Motion.now();GuiMessage raw=null;
        boolean previous=ChatPresentation.peek;ChatPresentation.peek=peeking;
        try{
            double scale=ChatPresentation.scale();double step=CompactChatManager.lineHeight(c)*scale;
            double scroll=access.cokechat$scroll(peeking).value(now);var lines=access.cokechat$lines(peeking);
            if(c.enabled&&c.actions.enabled&&ChatPresentation.reveal()>.99&&mx>=ChatPresentation.x()-4&&mx<=ChatPresentation.x()+ChatPresentation.width()+8&&my>=ChatPresentation.bottom()-ChatPresentation.height()&&my<ChatPresentation.bottom()){
                int first=(int)Math.floor(scroll),last=Math.min(lines.size(),first+(int)(ChatPresentation.height()/step)+2);
                for(int i=first;i<last;i++){
                    var candidate=lines.get(i).parent();var group=access.cokechat$state().group(candidate);
                    double entry=c.animations.messages&&group!=null?Motion.progress(group.born,now,c.animations.messageDuration,c.animations.messageEasing):1;
                    double bottom=ChatPresentation.bottom()-(i-first)*step+Math.round((scroll-first)*CompactChatManager.lineHeight(c)+(1-entry)*6)*scale;
                    if(my>=bottom-step&&my<bottom&&entry>.05){raw=candidate;h.size=Math.max(4,Math.min(c.actions.size,(int)step));h.gap=c.actions.spacing;
                        int count=(c.actions.copy?1:0)+(c.actions.delete?1:0);h.x=ChatPresentation.x()+ChatPresentation.width()-count*h.size-Math.max(0,count-1)*h.gap;h.y=(int)(bottom-step+(step-h.size)/2);break;}
                }
            }
        }finally{ChatPresentation.peek=previous;}
        var group=raw==null?null:access.cokechat$state().group(raw);Object id=group==null?raw:group.id;
        if(id!=h.groupId){h.groupId=id;h.since=now;h.fade.to(raw==null?0:1,now,c.actions.animation?90:0,c.animations.messageEasing);}
        h.raw=raw;if(raw!=null)h.last=raw;
        int button=buttonAt(h,mx,my);
        if(button!=h.over){h.over=button;h.since=now;}
        return h;
    }
    private static int buttonAt(Hover h,double mx,double my){
        if(h.raw==null||my<h.y||my>=h.y+h.size)return -1;
        var c=CokeChatClient.config().actions;int x=h.x;
        if(c.copy){if(mx>=x&&mx<x+h.size)return 0;x+=h.size+h.gap;}
        if(c.delete&&mx>=x&&mx<x+h.size)return 1;return -1;
    }
    public static void draw(GuiGraphicsExtractor g,ChatAccess access,int mx,int my,boolean peeking){
        var h=update(access,mx,my,peeking);var a=CokeChatClient.config().actions;if(!a.enabled)return;
        double alpha=h.fade.value(Motion.now())*a.opacity*(peeking?ChatPeek.opacity():1);if(alpha<.01||h.last==null)return;
        int x=h.x;if(a.copy){icon(g,x,h.y,h.size,false,alpha,h.over==0);x+=h.size+h.gap;}if(a.delete)icon(g,x,h.y,h.size,true,alpha,h.over==1);
        if(h.over>=0&&Motion.now()-h.since>=a.tooltipDelay)g.setTooltipForNextFrame(h.over==0?COPY:DELETE,mx,my);
    }
    public static void icon(GuiGraphicsExtractor g,int x,int y,int size,boolean delete,double opacity,boolean hovered){
        var a=CokeChatClient.config().actions;
        RoundedRenderer.fill(g,x,y,size,size,3,RoundedRenderer.color(hovered?a.hoverColor:a.backgroundColor,opacity*(hovered?a.hoverOpacity:a.backgroundOpacity)));
        int color=RoundedRenderer.color(delete?a.deleteColor:a.copyColor,opacity*(delete?a.deleteOpacity:a.copyOpacity));int inset=Math.max(2,size/4),w=Math.max(2,size-2*inset);
        if(delete){g.fill(x+inset,y+inset,x+inset+w,y+inset+1,color);g.fill(x+inset+1,y+inset-1,x+inset+w-1,y+inset,color);g.outline(x+inset+1,y+inset+2,Math.max(2,w-2),Math.max(2,size-2*inset-1),color);}
        else{g.outline(x+inset,y+inset+1,w,Math.max(3,size-2*inset),color);g.fill(x+inset+1,y+inset-1,x+inset+w-1,y+inset+1,color);}
    }
    public static boolean click(ChatAccess access,double mx,double my,boolean peeking){
        var h=update(access,mx,my,peeking);int button=buttonAt(h,mx,my);if(button<0)return false;
        if(button==0)Minecraft.getInstance().keyboardHandler.setClipboard(ClipboardText.plain(h.raw.content().getString()));
        else{access.cokechat$hide(h.raw);h.raw=h.last=null;h.groupId=null;h.fade.snap(0);}
        return true;
    }
}
