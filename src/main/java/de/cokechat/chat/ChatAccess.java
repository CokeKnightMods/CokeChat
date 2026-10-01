package de.cokechat.chat;
import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.chat.GuiMessage;
import de.cokechat.animation.SmoothScroll;
public interface ChatAccess {
    LocalMessageState<GuiMessage> cokechat$state();
    List<GuiMessage.Line> cokechat$lines(boolean peek);
    SmoothScroll cokechat$scroll(boolean peek);
    void cokechat$wheel(double wheel,boolean slow,boolean peek);
    void cokechat$hide(GuiMessage message);
    void cokechat$drawPeek(GuiGraphicsExtractor g,int mx,int my);
}
