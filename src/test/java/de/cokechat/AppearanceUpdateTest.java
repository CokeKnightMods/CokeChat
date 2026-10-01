package de.cokechat;
import de.cokechat.chat.ClipboardText;
import de.cokechat.animation.ChatOpening;
import de.cokechat.config.*;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;
class AppearanceUpdateTest {
    @Test void copyRemovesLegacyFormatsAndHexWithoutChangingReadableUnicode(){
        assertEquals("Hello World !",ClipboardText.plain("§aHello §bWorld §c!"));
        assertEquals("Bold italic under strike reset",ClipboardText.plain("§LBold §oitalic §nunder §mstrike §rreset"));
        assertEquals("Hex ❤ 😺",ClipboardText.plain("§x§a§B§0§1§2§3Hex §#FF00aa❤ 😺"));
        assertEquals("Literal § sign and :fire:",ClipboardText.plain("Literal § sign and :fire:"));
    }
    @Test void borderSwitchesAreIndependentAndPersist(@TempDir Path temp){
        for(boolean chat:new boolean[]{false,true})for(boolean input:new boolean[]{false,true}){
            var manager=new ConfigManager(temp.resolve("config.json"));manager.get().appearance.chatBorder=chat;manager.get().appearance.inputBorder=input;manager.get().appearance.scrollbar=false;manager.save();
            var restored=new ConfigManager(temp.resolve("config.json"));restored.load();assertEquals(chat,restored.get().appearance.chatBorder);assertEquals(input,restored.get().appearance.inputBorder);assertFalse(restored.get().appearance.scrollbar);
        }
        var old=ConfigManager.JSON.fromJson("{\"appearance\":{\"borderOpacity\":0.3}}",CokeChatConfig.class);old.validate();assertTrue(old.appearance.chatBorder);assertTrue(old.appearance.inputBorder);assertEquals(.3,old.appearance.borderOpacity);
    }
    @Test void peekAndOpenUseIdenticalTimelineForAllOptions(){
        var c=new CokeChatConfig().animations;var open=new ChatOpening();var peek=new ChatOpening();
        for(var easing:CokeChatConfig.Easing.values())for(boolean expand:new boolean[]{false,true})for(boolean fade:new boolean[]{false,true}){
            c.openEasing=easing;c.open=expand;c.openMessageFade=fade;c.openDistance=75;c.openDuration=750;c.fadeDelay=80;c.fadeStagger=25;c.fadeDuration=300;open.start(100);peek.start(100);
            for(int t:new int[]{100,200,400,900})for(int row:new int[]{0,1,10}){assertEquals(open.size(t,c),peek.size(t,c));assertEquals(open.fade(row,t,c),peek.fade(row,t,c));}
            peek.close(300,c);assertEquals(open.size(300,c),peek.size(300,c));assertFalse(peek.closing(2000,c));assertEquals(0,peek.opacity(2000,c));
        }
    }
}
