package de.cokechat.animation;
import de.cokechat.config.CokeChatConfig.Easing;
/** Allocation-free, monotonic-clock animation with continuous retargeting. */
public final class Motion {
    private double from,target;
    private long start;
    private int duration;
    private Easing easing=Easing.CUBIC_OUT;
    public static long now(){return System.nanoTime()/1_000_000;}
    public static double ease(double t,Easing easing){t=Math.max(0,Math.min(1,t));return switch(easing){case CUBIC_OUT->1-Math.pow(1-t,3);case QUARTIC_OUT->1-Math.pow(1-t,4);case QUINTIC_OUT->1-Math.pow(1-t,5);case EASE_OUT->1-(1-t)*(1-t);};}
    public static double progress(long born,long now,int duration,Easing easing){return duration<=0?1:ease((now-born)/(double)duration,easing);}
    public double value(long now){return from+(target-from)*progress(start,now,duration,easing);}
    public double target(){return target;}
    public void to(double next,long now,int duration,Easing easing){from=value(now);target=next;start=now;this.duration=duration;this.easing=easing;}
    public void snap(double value){from=target=value;duration=0;}
    public void shift(double delta){from+=delta;target+=delta;}
}
