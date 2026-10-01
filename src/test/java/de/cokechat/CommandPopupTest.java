package de.cokechat;
import de.cokechat.rendering.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class CommandPopupTest {
    @Test void popupAndReservedInputFitAtEveryEdgeAndScale(){
        for(int w:new int[]{320,427,640,960,1920})for(int h:new int[]{180,240,360,540,1080})for(int font:new int[]{5,9,14,24})for(int x:new int[]{2,w/2,w-70})for(int y:new int[]{2,h/2,h-26}){
            var box=CommandPopupLayout.reserveAbove(new ChatInputLayout(x,y,60,22),h,font);
            var popup=CommandPopupLayout.of(box,w,h,font,Math.max(320,box.width()),100);
            assertTrue(popup.rows()>0,"No visible popup row at "+w+"x"+h+" font "+font);
            assertTrue(popup.x()>=0&&popup.x()+popup.width()<=w);
            assertTrue(popup.y()>=0&&popup.y()+popup.height()<=box.y()-CommandPopupLayout.GAP);
            assertTrue(box.y()+box.height()<=h);
            assertTrue(popup.wrapWidth()*popup.scale()<=popup.width()-2*CommandPopupLayout.PADDING);
            assertTrue(popup.contains(popup.x()+6,popup.y()+6));assertFalse(popup.contains(popup.x()+6,box.y()));
            assertEquals(0,popup.rowAt(popup.y()+CommandPopupLayout.PADDING+1));
        }
    }
    @Test void veryTallInputStillLeavesSpaceForCommandHints(){var box=CommandPopupLayout.reserveAbove(new ChatInputLayout(2,2,150,172),180,24);var p=CommandPopupLayout.of(box,320,180,24,310,100);assertTrue(p.rows()>0);assertTrue(p.y()+p.height()<box.y());assertTrue(box.y()+box.height()<=180);}
    @Test void emptyHintsHaveNoClickableOverlay(){var p=CommandPopupLayout.of(new ChatInputLayout(40,200,200,20),427,240,9,320,0);assertEquals(0,p.height());assertFalse(p.contains(50,190));}
}
