package de.cokechat;
import de.cokechat.compact.*;
import de.cokechat.config.CokeChatConfig;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class CompactTest {
    @Test void threeAndTenDuplicatesKeepRawKeys() {
        var m=new CompactChatManager();String raw="Player: hello";
        for(int i=1;i<=10;i++)assertEquals(i,m.accept(raw,i*50,2).count());
        assertEquals("Player: hello",raw);
    }
    @Test void timeWindowIncludesInterveningMessages() {
        var m=new CompactChatManager();assertEquals(1,m.accept("A",0,2).count());
        assertEquals(2,m.accept("A",2000,2).count());assertEquals(3,m.accept("A",2001,2).count());
        assertEquals(1,m.accept("A",4002,2).count());
        assertEquals(1,m.accept("B",4100,2).count());assertEquals(2,m.accept("A",4200,2).count());
        assertEquals(2,m.accept("B",4300,2).count());assertEquals(3,m.accept("A",6000,2).count());
        assertEquals(1,m.accept("B",6401,2).count());
        m.reset();assertEquals(1,m.accept("A",2300,2).count());
    }
    @Test void customGroupingTimesAndCounterFormats() {
        for(double seconds:new double[]{.5,1,2,3,5,10,30,60,300}) {
            var m=new CompactChatManager();m.accept("A",0,seconds);
            assertEquals(2,m.accept("A",(long)(seconds*1000),seconds).count());
            assertEquals(1,m.accept("A",2*(long)(seconds*1000)+1,seconds).count());
        }
        var c=new CokeChatConfig();
        for(String pattern:new String[]{"×{count}","[{count}]","({count}x)","x{count}"}) {
            c.compact.counterFormat=pattern;assertEquals(" "+pattern.replace("{count}","3"),CompactChatManager.counter(c,3));
        }
        c.compact.showCounter=false;assertEquals("",CompactChatManager.counter(c,10));
    }
    @Test void everyDuplicateRestartsWindowWithoutRestartingBirth(){
        var m=new CompactChatManager();
        for(int i=0;i<10;i++){var group=m.accept("same",1000L*i,2);assertEquals(i+1,group.count());assertEquals(0,group.startedAt());assertEquals(1000L*i,group.lastSeenAt());}
        assertEquals(1,m.accept("same",11001,2).count());
        assertEquals(1,m.accept("same",0,2).count());
    }
}
