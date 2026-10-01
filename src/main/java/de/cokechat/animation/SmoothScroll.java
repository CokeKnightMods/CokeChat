package de.cokechat.animation;
import de.cokechat.config.CokeChatConfig;
public final class SmoothScroll {
    private final Motion motion=new Motion();
    public double target(){return motion.target();}
    public double value(long now){return motion.value(now);}
    public void reset(){motion.snap(0);}
    public void anchor(double delta){motion.shift(delta);}
    public void clamp(double max){clamp(max,Motion.now());}
    public void clamp(double max,long now){max=Math.max(0,max);double value=motion.value(now);if(motion.target()>max||value>max||value<0||motion.target()<0)motion.snap(Math.max(0,Math.min(max,motion.target())));}
    public void scroll(double wheel,boolean shift,double max,long now,CokeChatConfig.Animations settings){
        boolean slow=shift&&settings.slowScroll;
        double target=Math.max(0,Math.min(max,motion.target()+wheel*settings.scrollSpeed*(slow?settings.slowMultiplier:1)));
        motion.to(target,now,settings.smoothScroll?(slow?settings.slowDuration:settings.scrollDuration):0,settings.scrollEasing);
    }
}
