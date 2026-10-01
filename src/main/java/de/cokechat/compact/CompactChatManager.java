package de.cokechat.compact;
import de.cokechat.config.CokeChatConfig;
import java.util.*;
/** Each formatted message has its own rolling duplicate window. */
public final class CompactChatManager {
    private final Map<Object,DuplicateMessageGroup> groups=new HashMap<>();
    public DuplicateMessageGroup accept(Object key, long timeMillis, double windowSeconds) {
        groups.values().removeIf(g->timeMillis<g.lastSeenAt()||timeMillis-g.lastSeenAt()>Math.round(windowSeconds*1000));
        var group=groups.get(key);
        if (group != null && timeMillis >= group.lastSeenAt()
                && timeMillis - group.lastSeenAt() <= Math.round(windowSeconds * 1000))
            group = new DuplicateMessageGroup(key, group.startedAt(), timeMillis, group.count()+1);
        else group = new DuplicateMessageGroup(key, timeMillis, timeMillis, 1);
        groups.put(key,group);return group;
    }
    public void reset() { groups.clear(); }
    public static String counter(CokeChatConfig c, int count) {
        return c.compact.showCounter && count > 1 ? " " + c.compact.counterFormat.replace("{count}", Integer.toString(count)) : "";
    }
    public static int spacing(CokeChatConfig c) { return c.appearance.messageSpacing; }
    public static double scale(CokeChatConfig c) { return c.appearance.fontSize / 9.0; }
    public static int lineHeight(CokeChatConfig c) { return Math.max(9, c.emoji.enabled ? c.emoji.size : 9) + spacing(c); }
}
