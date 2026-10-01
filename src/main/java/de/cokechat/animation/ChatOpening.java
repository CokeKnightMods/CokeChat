package de.cokechat.animation;
import de.cokechat.config.CokeChatConfig.Animations;
/** Independent panel reveal. It never changes message layout or wrap width. */
public final class ChatOpening {
    private long start;
    private boolean active;
    private boolean closing;
    private long closedAt;
    private double closeSize;
    public void start(long now){start=now;active=true;closing=false;}
    public void stop(){active=false;closing=false;}
    public void close(long now,Animations c){closeSize=size(now,c);closedAt=now;closing=true;}
    public boolean closing(long now,Animations c){return closing&&now-closedAt<Math.max(c.open?c.openDuration:0,c.openMessageFade?c.fadeDuration:0);}
    public double opacity(long now,Animations c){return !closing?1:1-Motion.progress(closedAt,now,c.openMessageFade?c.fadeDuration:c.open?c.openDuration:0,c.openEasing);}
    public double size(long now,Animations c){
        if(!active||!c.open)return 1;
        if(closing)return closeSize*(1-c.openDistance/100*Motion.progress(closedAt,now,c.openDuration,c.openEasing));
        return 1-c.openDistance/100*(1-Motion.progress(start,now,c.openDuration,c.openEasing));
    }
    public double fade(int row,long now,Animations c){return !active||!c.openMessageFade?1:Motion.progress(start+c.fadeDelay+(long)Math.min(100,Math.max(0,row))*c.fadeStagger,closing?closedAt:now,c.fadeDuration,c.openEasing);}
}
