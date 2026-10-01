package de.cokechat;
import de.cokechat.animation.*;
import de.cokechat.chat.ViewportAnchor;
import de.cokechat.config.*;
import de.cokechat.gui.ColorValue;
import de.cokechat.rendering.ChatInputLayout;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class UpdateThreeTest {
    @Test void openingReachesExactTargetForEveryDistanceAndEasing(){
        var c=new CokeChatConfig().animations;var opening=new ChatOpening();
        for(var ease:CokeChatConfig.Easing.values())for(int distance:new int[]{0,10,25,50,75,100})for(int duration:new int[]{0,100,200,300,500,750,1000}){
            c.openDistance=distance;c.openDuration=duration;c.openEasing=ease;opening.start(100);
            assertEquals(duration==0?1:1-distance/100.0,opening.size(100,c),1e-8);
            assertEquals(1,opening.size(100+duration,c),1e-8);
        }
        c.open=false;opening.start(0);assertEquals(1,opening.size(0,c));
    }
    @Test void openingFadeDelayAndStaggerDoNotMoveLayout(){var a=new CokeChatConfig().animations;var open=new ChatOpening();open.start(100);a.fadeDelay=50;a.fadeStagger=20;a.fadeDuration=100;
        assertEquals(0,open.fade(0,149,a));assertEquals(0,open.fade(2,189,a));assertEquals(1,open.fade(2,290,a));a.openMessageFade=false;assertEquals(1,open.fade(2,100,a));}
    @Test void readingAnchorSurvivesBatchesAndWrappedCompactCounters(){
        Object newest=new Object(),reading=new Object(),older=new Object(),incoming=new Object();var before=List.of(newest,reading,reading,older);
        var anchor=ViewportAnchor.capture(before,2.4,3,x->x);assertFalse(anchor.following());
        assertEquals(3,anchor.delta(List.of(incoming,incoming,incoming,newest,reading,reading,older),x->x));
        assertEquals(1,anchor.delta(List.of(newest,newest,reading,reading,older),x->x));
        var scroll=new SmoothScroll();var c=new CokeChatConfig().animations;scroll.scroll(1,false,100,0,c);double at=scroll.value(50);scroll.anchor(3);assertEquals(at+3,scroll.value(50));assertEquals(10,scroll.target());
        assertTrue(ViewportAnchor.capture(before,0,0,x->x).following());assertFalse(ViewportAnchor.capture(before,1.5,0,x->x).following());
    }
    @Test void inputRelativeTracksChatAndAbsoluteStaysOnScreen(){
        var c=new CokeChatConfig();c.input.width=160;c.input.x=10;c.input.y=12;
        var a=ChatInputLayout.of(c,800,600,20,500);var b=ChatInputLayout.of(c,800,600,120,400);assertEquals(100,b.x()-a.x());assertEquals(-100,b.y()-a.y());
        c.input.positionMode=CokeChatConfig.PositionMode.ABSOLUTE_SCREEN;assertEquals(ChatInputLayout.of(c,800,600,20,500),ChatInputLayout.of(c,800,600,120,400));
        c.input.x=1200;c.input.y=800;var edge=ChatInputLayout.of(c,320,240,0,0);assertTrue(edge.x()+edge.width()<=320);assertTrue(edge.y()+edge.height()<=240);
    }
    @Test void colorDraftsAreIndependentAndRgbaRoundTrips(){var a=new ColorValue(0x171C29,.5);var b=new ColorValue(0xAABBCC,1);a.hex("#FF000080");assertEquals(0xFF0000,a.rgb());assertEquals(128/255.0,a.alpha);assertEquals(0xAABBCC,b.rgb());assertEquals(1,b.alpha);assertEquals("#FF000080",a.hex());assertThrows(IllegalArgumentException.class,()->a.hex("#bad"));}
    @Test void oldConfigKeepsInputAppearanceAndGetsSafeNewDefaults(){var c=ConfigManager.JSON.fromJson("{\"appearance\":{\"inputColor\":123,\"inputOpacity\":0.4}}",CokeChatConfig.class);c.validate();assertEquals(123,c.appearance.inputColor);assertEquals(.4,c.appearance.inputOpacity);assertNotNull(c.input);assertEquals(50,c.animations.openDistance);c.input.fontSize=Double.NaN;c.animations.openDistance=999;c.validate();assertEquals(9,c.input.fontSize);assertEquals(100,c.animations.openDistance);}
}
