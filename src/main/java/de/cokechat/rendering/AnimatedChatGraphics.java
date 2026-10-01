package de.cokechat.rendering;
import net.minecraft.client.gui.components.ChatComponent.ChatGraphicsAccess;
import net.minecraft.client.multiplayer.chat.GuiMessageTag;
import net.minecraft.util.FormattedCharSequence;
import org.joml.Matrix3x2f;
import java.util.function.Consumer;
/** Decorates vanilla rendering AND click capture with identical pixel coordinates. */
public final class AnimatedChatGraphics implements ChatGraphicsAccess {
    public ChatGraphicsAccess delegate;
    @Override public void updatePose(Consumer<Matrix3x2f> updater){delegate.updatePose(updater);}
    @Override public void fill(int x0,int y0,int x1,int y1,int color){delegate.fill(x0,y0,x1,y1,color);}
    @Override public boolean handleMessage(int top,float opacity,FormattedCharSequence text){var a=de.cokechat.CokeChatClient.config().appearance;FormattedCharSequence tinted=sink->text.accept((index,style,code)->sink.accept(index,style.getColor()==null?style.withColor(a.textColor):style,code));return delegate.handleMessage(top+ChatPresentation.offset(),opacity*ChatPresentation.opacity()*(float)a.textOpacity,tinted);}
    @Override public void handleTag(int x0,int y0,int x1,int y1,float opacity,GuiMessageTag tag){int offset=ChatPresentation.offset();delegate.handleTag(x0,y0+offset,x1,y1+offset,opacity*ChatPresentation.opacity(),tag);}
    @Override public void handleTagIcon(int left,int bottom,boolean force,GuiMessageTag tag,GuiMessageTag.Icon icon){delegate.handleTagIcon(left,bottom+ChatPresentation.offset(),force,tag,icon);}
}
