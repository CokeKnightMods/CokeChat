package de.cokechat.emoji;
import de.cokechat.CokeChatClient;
import de.cokechat.rendering.RoundedRenderer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import java.util.List;
public final class EmojiAutocomplete {
    private String last = "";
    private int cursor = -1, start, selected, popupY,popupX;
    private List<String> choices = List.of();
    private boolean hidden;
    private List<String> previousFavorites = List.of();
    private EmojiDatabase previousDatabase;
    public void update(EditBox input) {
        var c = CokeChatClient.config(); String value = input.getValue(); int pos = input.getCursorPosition();
        if (!c.enabled || !c.emoji.enabled || !c.emoji.autocomplete || value.startsWith("/")) { choices = List.of(); return; }
        if (value.equals(last) && pos == cursor && previousFavorites.equals(c.emoji.favorites) && previousDatabase==CokeChatClient.emojis()) return;
        previousFavorites=List.copyOf(c.emoji.favorites);previousDatabase=CokeChatClient.emojis();
        last = value; cursor = pos; hidden = false; selected = 0;
        start = value.lastIndexOf(':', Math.max(0, pos-1));
        if (start < 0 || pos <= start || start > 0 && !Character.isWhitespace(value.charAt(start-1))) { choices = List.of(); return; }
        String prefix = value.substring(start+1, pos);
        choices = prefix.matches("[a-z0-9_+\\-]{0,64}") ? CokeChatClient.emojis().suggest(prefix,c.emoji.favorites) : List.of();
    }
    public void draw(GuiGraphicsExtractor g, Font font, EditBox input) {
        update(input); if (hidden || choices.isEmpty()) return;
        popupX=Math.max(2,Math.min(g.guiWidth()-192,input.getX()));
        popupY = input.getY()-choices.size()*15-9;if(popupY<5)popupY=Math.min(g.guiHeight()-choices.size()*15-5,input.getY()+input.getHeight()+9);
        RoundedRenderer.fill(g, popupX, popupY-4, 190, choices.size()*15+8, 6, 0xF51A2233);
        for (int i = 0; i < choices.size(); i++) {
            if (i == selected) RoundedRenderer.fill(g, popupX+3, popupY+i*15-1, 184, 14, 3, 0xFF344665);
            g.text(font, CokeChatClient.text().process(net.minecraft.network.chat.Component.literal(":"+choices.get(i)+": ")), popupX+7, popupY+i*15+2, 0xFFFFFFFF);
            g.text(font, ":"+choices.get(i)+":", popupX+32, popupY+i*15+2, 0xFFDCE7FF);
        }
    }
    public boolean key(KeyEvent event, EditBox input) {
        update(input); if (hidden || choices.isEmpty()) return false;
        switch (event.key()) {
            case 265 -> selected = Math.floorMod(selected-1, choices.size());
            case 264 -> selected = (selected+1)%choices.size();
            case 257, 335, 258 -> accept(input);
            case 256 -> hidden = true;
            default -> { return false; }
        }
        return true;
    }
    public boolean click(MouseButtonEvent event, EditBox input) {
        update(input); if (hidden || choices.isEmpty() || event.button() != 0 || event.x() < popupX || event.x() > popupX+190) return false;
        int row = (int)Math.floor((event.y()-popupY)/15); if (row < 0 || row >= choices.size()) return false;
        selected = row; accept(input); return true;
    }
    private void accept(EditBox input) {
        String code = ":" + choices.get(selected) + ":";
        String value = input.getValue(); input.setValue(value.substring(0,start)+code+value.substring(cursor));
        input.setCursorPosition(start+code.length()); input.setHighlightPos(start+code.length());
        choices = List.of(); hidden = true;
    }
}
