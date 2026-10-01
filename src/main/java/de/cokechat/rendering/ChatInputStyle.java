package de.cokechat.rendering;
import de.cokechat.CokeChatClient;
import net.minecraft.client.gui.components.EditBox;
public final class ChatInputStyle {
    public static EditBox target;
    public static ChatInputLayout bounds;
    public static double scale(Object box){return box==target&&CokeChatClient.config().enabled?CokeChatClient.config().input.fontSize/9:1;}
}
