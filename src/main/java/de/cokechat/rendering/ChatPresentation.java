package de.cokechat.rendering;
import de.cokechat.CokeChatClient;
import de.cokechat.animation.Motion;
import de.cokechat.chat.ChatAccess;
import de.cokechat.compact.CompactChatManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.chat.GuiMessage;
/** Render-thread context, also used by vanilla clickable-text capture. */
public final class ChatPresentation {
    public static boolean peek,active,line;
    public static double fraction;
    public static float entry=1;
    public static int offset;
    public static int commandInset;
    public static long frame;
    public static final de.cokechat.animation.ChatOpening opening=new de.cokechat.animation.ChatOpening();
    public static de.cokechat.animation.ChatOpening opening(){return peek?ChatPeek.opening:opening;}
    public static double reveal(){return opening().size(Motion.now(),CokeChatClient.config().animations);}
    public static int width(){var mc=Minecraft.getInstance();return Math.min(peek?CokeChatClient.config().peek.width:CokeChatClient.config().appearance.width,Math.max(80,mc.getWindow().getGuiScaledWidth()-24));}
    public static int x(){return peek?Math.min(CokeChatClient.config().peek.x,Math.max(0,Minecraft.getInstance().getWindow().getGuiScaledWidth()-width()-12)):CokeChatClient.offsetX();}
    public static int y(){return peek?Math.min(CokeChatClient.config().peek.y,Math.max(0,Minecraft.getInstance().getWindow().getGuiScaledHeight()-80)):CokeChatClient.offsetY()+commandInset;}
    public static int height(){return Math.min(peek?CokeChatClient.config().peek.height:CokeChatClient.config().appearance.height,Math.max(30,Minecraft.getInstance().getWindow().getGuiScaledHeight()-65-y()));}
    public static double scale(){return CompactChatManager.scale(CokeChatClient.config());}
    public static double baseBottom(){return Math.floor((Minecraft.getInstance().getWindow().getGuiScaledHeight()-40)/scale())*scale()-(peek?y():CokeChatClient.offsetY());}
    public static double bottom(){return baseBottom()-(peek?0:commandInset);}
    public static void beginLine(GuiMessage raw,ChatAccess access,int index){
        line=true;var c=CokeChatClient.config();var group=access.cokechat$state().group(raw);
        entry=c.animations.messages&&group!=null?(float)Motion.progress(group.born,frame,c.animations.messageDuration,c.animations.messageEasing):1;
        offset=(int)Math.round(fraction*CompactChatManager.lineHeight(c)+(1-entry)*6);
        entry*=opening().fade(index,frame,c.animations);
    }
    public static int offset(){return line?offset:0;}
    public static float opacity(){return (line?entry:1)*(peek?ChatPeek.opacity():1);}
}
