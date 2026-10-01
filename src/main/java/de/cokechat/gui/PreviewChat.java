package de.cokechat.gui;
import de.cokechat.CokeChatClient;
import de.cokechat.animation.*;
import de.cokechat.chat.ViewportAnchor;
import de.cokechat.compact.CompactChatManager;
import de.cokechat.rendering.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import java.util.*;
/** Local sample conversation: received messages never enter the real chat. */
public final class PreviewChat {
    private static final String[] TEXT={"Clashbad: your aim needs a tutorial :skull:","Clashbad: your aim needs a tutorial :skull:","Clashbad: your aim needs a tutorial :skull:","CokeKnight: says the guy fighting the wall :sob:","Clashbad: gg, you finally hit something","CokeKnight: yeah, the carry button :fire:","Clashbad: pog, another excuse :joy:","CokeKnight: your scoreboard is a horror story :heart:","Clashbad: bro, your strategy is respawning","CokeKnight: and you still need me to carry you"};
    private record Entry(String text,long born){}
    private record Row(FormattedCharSequence text,Entry key,List<Integer> members){}
    private final List<Entry> messages=new ArrayList<>();
    private final List<Row> rows=new ArrayList<>();
    private final Set<Integer> deleted=new HashSet<>();
    private final SmoothScroll scroll=new SmoothScroll();
    private final Motion hoverFade=new Motion();
    private final ChatOpening opening=new ChatOpening();
    private long hoverSince;
    private int revision=-1,lastWidth=-1,hover=-1,paintHover=-1,buttonHover=-1;
    private int x,y,width,height,iconX,iconY,iconSize;
    private int keyboardRow;
    private boolean keyboardSelection;
    public PreviewChat(){replay();}
    public void replay(){messages.clear();long now=Motion.now();for(int i=0;i<TEXT.length;i++)messages.add(new Entry(TEXT[i],now-5000+i*200));deleted.clear();revision=-1;scroll.reset();keyboardRow=0;keyboardSelection=false;opening.start(now);}
    public void reopen(){opening.start(Motion.now());}
    public void select(int direction){
        if(rows.isEmpty())return;keyboardSelection=true;keyboardRow=Math.clamp(keyboardRow,0,rows.size()-1);var key=rows.get(keyboardRow).key;
        int next=keyboardRow;do{next+=direction;}while(next>=0&&next<rows.size()&&rows.get(next).key==key);keyboardRow=Math.clamp(next,0,rows.size()-1);
        var c=CokeChatClient.config();double visible=height/(CompactChatManager.lineHeight(c)*CompactChatManager.scale(c));double target=scroll.target();
        if(keyboardRow<target)scroll.anchor(keyboardRow-target);else if(keyboardRow>=target+visible)scroll.anchor(keyboardRow-visible+1-target);
    }
    public void copySelection(){if(!rows.isEmpty())Minecraft.getInstance().keyboardHandler.setClipboard(de.cokechat.chat.ClipboardText.plain(rows.get(Math.clamp(keyboardRow,0,rows.size()-1)).key.text));}
    public void hideSelection(){if(!rows.isEmpty()){deleted.addAll(rows.get(Math.clamp(keyboardRow,0,rows.size()-1)).members);revision=-1;hover=paintHover=-1;hoverFade.snap(0);}}
    public void receive(boolean duplicate){long now=Motion.now();messages.add(new Entry(duplicate&&!messages.isEmpty()?messages.getLast().text:(messages.size()%2==0?"Clashbad: reading the old chat :eyes:":"CokeKnight: new message, same reading position :fire:"),now));revision=-1;}
    private void rebuild(int width){
        long now=Motion.now();var anchor=ViewportAnchor.capture(rows,scroll.value(now),scroll.target(),Row::key);
        rows.clear();var c=CokeChatClient.config();var font=Minecraft.getInstance().font;int wrap=Math.max(20,(int)((width-12)/CompactChatManager.scale(c)));
        var manager=new CompactChatManager();
        var groups=new java.util.LinkedHashMap<Integer,List<Integer>>();
        var latest=new java.util.HashMap<String,Integer>();
        for(int i=0;i<messages.size();i++){
            if(deleted.contains(i))continue;var entry=messages.get(i);
            int count=c.enabled&&c.compact.enabled?manager.accept(entry.text,entry.born,c.compact.groupingSeconds).count():1;
            List<Integer> members=count>1?groups.remove(latest.get(entry.text)):null;
            if(members==null)members=new ArrayList<>();
            members.add(i);groups.put(i,members);latest.put(entry.text,i);
        }
        for(var members:groups.values()){
            var entry=messages.get(members.getFirst());
            var text=CokeChatClient.text().process(Component.literal(entry.text)).copy().append(Component.literal(CompactChatManager.counter(c,members.size())).withStyle(net.minecraft.ChatFormatting.GRAY));
            for(var line:font.split(text,wrap))rows.add(new Row(line,entry,List.copyOf(members)));
        }
        Collections.reverse(rows);if(anchor.following())scroll.reset();else scroll.anchor(anchor.delta(rows,Row::key));
        revision=CokeChatClient.configRevision();lastWidth=width;
    }
    public void draw(GuiGraphicsExtractor g,int x,int y,int width,int height,boolean compactLayout){
        this.x=x;this.y=y;this.width=width;this.height=height;
        if(revision!=CokeChatClient.configRevision()||lastWidth!=width)rebuild(width);
        var client=Minecraft.getInstance();var c=CokeChatClient.config();double scale=CompactChatManager.scale(c),step=CompactChatManager.lineHeight(c)*scale;long now=Motion.now();
        double reveal=opening.size(now,c.animations);int rw=(int)Math.round(width*reveal),rh=(int)Math.round(height*reveal);
        ChatBackgroundRenderer.panel(g,x,y+height-rh,rw,rh,c.appearance.cornerRadius,c.appearance.backgroundColor,c.appearance.backgroundOpacity,c.appearance.borderColor,c.appearance.chatBorder?c.appearance.borderOpacity:0);
        scroll.clamp(Math.max(0,rows.size()-Math.floor(height/step)));double offset=scroll.value(now);
        double mx=client.mouseHandler.getScaledXPos(client.getWindow()),my=client.mouseHandler.getScaledYPos(client.getWindow());int nextHover=-1;
        g.enableScissor(x,y+height-rh,x+rw,y+height);
        for(int i=Math.max(0,(int)offset);i<rows.size()&&(i-offset)*step<height;i++){
            var row=rows.get(i);double progress=c.animations.messages?Motion.progress(row.key.born,now,c.animations.messageDuration,c.animations.messageEasing):1;
            double top=y+height-(i-offset+1)*step+Math.round((1-progress)*6)*scale;
            double opacity=progress*opening.fade(i-(int)offset,now,c.animations)*c.appearance.textOpacity;
            if(keyboardSelection&&i==keyboardRow)g.fill(x+2,(int)top,x+4,(int)(top+step),UiTheme.ACCENT);
            g.pose().pushMatrix();g.pose().translate(x+6,(float)top);g.pose().scale((float)scale,(float)scale);g.text(client.font,row.text,0,0,RoundedRenderer.color(c.appearance.textColor,opacity));g.pose().popMatrix();
            if(c.actions.enabled&&reveal>.99&&mx>=x&&mx<x+width&&my>=Math.max(y,top)&&my<Math.min(y+height,top+step)){
                nextHover=i;iconSize=Math.max(4,Math.min(c.actions.size,(int)step));int count=(c.actions.copy?1:0)+(c.actions.delete?1:0);
                iconX=x+width-4-count*iconSize-Math.max(0,count-1)*c.actions.spacing;iconY=(int)top;
            }
        }
        if(c.appearance.scrollbar&&rows.size()*step>height){int bh=Math.max(8,(int)(height*height/(rows.size()*step)));int by=y+height-bh-(int)(offset/(rows.size()-height/step)*(height-bh));RoundedRenderer.fill(g,x+width-c.appearance.scrollbarWidth,by,c.appearance.scrollbarWidth,bh,1,RoundedRenderer.color(c.appearance.scrollbarColor,c.appearance.scrollbarOpacity));}
        g.disableScissor();
        if(hover!=nextHover){hover=nextHover;hoverSince=now;hoverFade.to(hover<0?0:1,now,c.actions.animation?90:0,c.animations.messageEasing);}
        if(hover>=0)paintHover=hover;
        int button=button(mx,my);if(button!=buttonHover){buttonHover=button;hoverSince=now;}
        double alpha=hoverFade.value(now)*c.actions.opacity;
        if(paintHover>=0&&c.actions.enabled&&alpha>.01){int ix=iconX;if(c.actions.copy){ChatActions.icon(g,ix,iconY,iconSize,false,alpha,button==0);ix+=iconSize+c.actions.spacing;}if(c.actions.delete)ChatActions.icon(g,ix,iconY,iconSize,true,alpha,button==1);}
        if(button>=0&&now-hoverSince>=c.actions.tooltipDelay)g.setTooltipForNextFrame(Component.literal(button==0?"Copy preview message":"Hide preview group"),(int)mx,(int)my);
    }
    private int button(double mx,double my){if(hover<0||my<iconY||my>=iconY+iconSize)return -1;int ix=iconX;var a=CokeChatClient.config().actions;if(a.copy){if(mx>=ix&&mx<ix+iconSize)return 0;ix+=iconSize+a.spacing;}return a.delete&&mx>=ix&&mx<ix+iconSize?1:-1;}
    public boolean click(double mx,double my){int button=button(mx,my);if(button<0||hover>=rows.size())return false;var row=rows.get(hover);if(button==0)Minecraft.getInstance().keyboardHandler.setClipboard(de.cokechat.chat.ClipboardText.plain(row.key.text));else{deleted.addAll(row.members);revision=-1;hover=paintHover=-1;hoverFade.snap(0);}return true;}
    public boolean wheel(double mx,double my,double wheel){if(mx<x||mx>=x+width||my<y||my>=y+height)return false;var c=CokeChatClient.config();double step=CompactChatManager.lineHeight(c)*CompactChatManager.scale(c);scroll.scroll(wheel,Minecraft.getInstance().hasShiftDown(),Math.max(0,rows.size()-Math.floor(height/step)),Motion.now(),c.animations);return true;}
}
