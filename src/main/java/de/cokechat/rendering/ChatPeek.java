package de.cokechat.rendering;
import de.cokechat.CokeChatClient;
import de.cokechat.animation.Motion;
import de.cokechat.chat.ChatAccess;
import net.minecraft.client.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import com.mojang.blaze3d.platform.InputConstants;
/** Non-screen overlay: keeps the current screen, draft and normal scroll state untouched. */
public final class ChatPeek {
    public static KeyMapping key;
    public static final de.cokechat.animation.ChatOpening opening=new de.cokechat.animation.ChatOpening();
    private static boolean held,restoreMouse;
    private static Object level;
    public static boolean active(){return held||opening.closing(Motion.now(),CokeChatClient.config().animations);}
    public static float opacity(){return active()?(float)opening.opacity(Motion.now(),CokeChatClient.config().animations):0;}
    public static void tick(Minecraft client){
        if(level!=client.level){level=client.level;held=false;opening.stop();if(client.gui.getChat() instanceof ChatAccess access)access.cokechat$scroll(true).reset();}
        var c=CokeChatClient.config();boolean pressed=false;
        if(key!=null&&!key.isUnbound()&&c.enabled&&c.peek.enabled&&client.level!=null&&client.isWindowActive()){
            var binding=InputConstants.getKey(key.saveString());
            pressed=binding.getType()==InputConstants.Type.KEYSYM?InputConstants.isKeyDown(client.getWindow(),binding.getValue()):key.isDown();
        }
        if(pressed!=held){held=pressed;if(held)opening.start(Motion.now());else opening.close(Motion.now(),c.animations);}
        if(held&&client.screen==null&&client.mouseHandler.isMouseGrabbed()){restoreMouse=true;client.mouseHandler.releaseMouse();}
        if(!active()&&restoreMouse){restoreMouse=false;if(client.screen==null&&client.level!=null&&client.isWindowActive())client.mouseHandler.grabMouse();}
    }
    public static boolean slow(Minecraft client){
        int peekKey=key==null?-1:InputConstants.getKey(key.saveString()).getValue();
        return InputConstants.isKeyDown(client.getWindow(),340)&&(peekKey!=340||!held)
            ||InputConstants.isKeyDown(client.getWindow(),344)&&(peekKey!=344||!held);
    }
    public static boolean contains(double x,double y){
        boolean previous=ChatPresentation.peek;ChatPresentation.peek=true;
        try{return x>=ChatPresentation.x()-5&&x<=ChatPresentation.x()+ChatPresentation.width()+12&&y>=ChatPresentation.bottom()-ChatPresentation.height()&&y<=ChatPresentation.bottom();}
        finally{ChatPresentation.peek=previous;}
    }
    public static boolean scroll(double wheel){
        if(!active())return false;var client=Minecraft.getInstance();double x=client.mouseHandler.getScaledXPos(client.getWindow()),y=client.mouseHandler.getScaledYPos(client.getWindow());
        if(contains(x,y)){((ChatAccess)client.gui.getChat()).cokechat$wheel(wheel,slow(client),true);return true;}
        return client.screen==null;
    }
    public static boolean click(int button,int action){
        if(!active())return false;var client=Minecraft.getInstance();double x=client.mouseHandler.getScaledXPos(client.getWindow()),y=client.mouseHandler.getScaledYPos(client.getWindow());
        if(contains(x,y)){if(button==0&&action==1)ChatActions.click((ChatAccess)client.gui.getChat(),x,y,true);return true;}
        return client.screen==null;
    }
    public static void draw(GuiGraphicsExtractor g,int mx,int my){if(active()&&Minecraft.getInstance().level!=null)((ChatAccess)Minecraft.getInstance().gui.getChat()).cokechat$drawPeek(g,mx,my);}
}
