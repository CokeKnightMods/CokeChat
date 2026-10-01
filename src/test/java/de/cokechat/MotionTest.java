package de.cokechat;
import de.cokechat.animation.*;
import de.cokechat.chat.LocalMessageState;
import de.cokechat.config.CokeChatConfig;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
class MotionTest {
    @Test void easingDurationsAndDisabledAnimation(){
        for(var easing:CokeChatConfig.Easing.values())for(int duration:new int[]{0,50,200,500,2000}){
            assertEquals(1,Motion.progress(100,100+duration,duration,easing),1e-9);
            if(duration>0){assertEquals(0,Motion.progress(100,100,duration,easing),1e-9);assertTrue(Motion.progress(100,100+duration/2,duration,easing)>.5);}
        }
    }
    @Test void rapidRetargetIsContinuous(){
        var m=new Motion();m.snap(0);m.to(1,0,200,CokeChatConfig.Easing.CUBIC_OUT);
        double before=m.value(50);m.to(0,50,200,CokeChatConfig.Easing.CUBIC_OUT);assertEquals(before,m.value(50),1e-9);
        assertEquals(0,m.value(250),1e-9);
        for(int i=0;i<100;i++){double start=m.value(300+i);m.to(i%2,300+i,200,CokeChatConfig.Easing.CUBIC_OUT);assertEquals(start,m.value(300+i),1e-9);}
    }
    @Test void scrollPrecisionBoundsAndReleasingShift(){
        var c=new CokeChatConfig().animations;var normal=new SmoothScroll();var slow=new SmoothScroll();
        normal.scroll(1,false,100,0,c);slow.scroll(1,true,100,0,c);
        assertEquals(7,normal.target());assertEquals(1.75,slow.target());assertEquals(0,normal.value(0));assertEquals(7,normal.value(200));
        double value=slow.value(50);slow.scroll(1,false,100,50,c);assertEquals(value,slow.value(50));assertEquals(8.75,slow.target());
        normal.scroll(1000,false,100,500,c);assertEquals(100,normal.target());normal.scroll(-1000,false,100,1000,c);assertEquals(0,normal.target());
        c.smoothScroll=false;normal.scroll(1,false,100,1100,c);assertEquals(7,normal.value(1100));
        normal.clamp(2);assertEquals(2,normal.value(1100));normal.reset();assertEquals(0,normal.target());
    }
    @Test void stableGroupsDeletionAndSeparateRawObjects(){
        var state=new LocalMessageState<Object>();Object a=new Object(),b=new Object(),c=new Object();
        var first=state.attach(a,null,false,100);var second=state.attach(b,a,true,200);assertSame(first,second);assertSame(a,second.id);assertEquals(100,second.born);
        state.attach(c,null,false,300);state.hideGroup(b);assertTrue(state.hidden(a));assertTrue(state.hidden(b));assertFalse(state.hidden(c));
        state.rebuild();assertTrue(state.hidden(a));var rebuilt=state.attach(c,null,false,500);assertEquals(300,rebuilt.born);
        state.retain(List.of(c));assertFalse(state.hidden(a));state.clear();assertNull(state.group(c));
    }
    @Test void shrinkingHistoryWhileScrollingNeverLeavesTheViewportOutOfBounds(){
        var settings=new CokeChatConfig().animations;var scroll=new SmoothScroll();scroll.scroll(100,false,100,0,settings);scroll.scroll(-100,false,100,200,settings);
        assertTrue(scroll.value(210)>5);scroll.clamp(5,210);assertTrue(scroll.value(210)<=5);assertEquals(0,scroll.target());
        scroll.reset();scroll.scroll(1,false,100,300,settings);scroll.anchor(-7);scroll.clamp(100,301);assertTrue(scroll.value(301)>=0);
    }
    @Test void manyMessagesKeepIndependentStarts(){
        var state=new LocalMessageState<Object>();Object[] messages=new Object[2000];
        for(int i=0;i<messages.length;i++){messages[i]=new Object();state.attach(messages[i],null,false,i);}
        for(int i=0;i<messages.length;i++)assertEquals(i,state.group(messages[i]).born);
        state.retain(List.of(messages[1999]));assertNull(state.group(messages[0]));assertNotNull(state.group(messages[1999]));
    }
    @Test void oldConfigsAndInvalidNewSettingsAreSafe(){
        var c=new CokeChatConfig();c.animations.messageDuration=-1;c.animations.scrollSpeed=Double.NaN;c.peek.opacity=2;c.actions.size=200;c.validate();
        assertEquals(0,c.animations.messageDuration);assertEquals(7,c.animations.scrollSpeed);assertEquals(1,c.peek.opacity);assertEquals(24,c.actions.size);
        c.animations=null;c.peek=null;c.actions=null;c.validate();assertNotNull(c.peek);assertNotNull(c.actions);assertNotNull(c.animations);
    }
}
