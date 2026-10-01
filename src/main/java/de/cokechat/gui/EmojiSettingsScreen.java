package de.cokechat.gui;

import de.cokechat.CokeChatClient;
import de.cokechat.emoji.EmojiDefinition;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import java.util.*;

public final class EmojiSettingsScreen extends Screen {
    private final Screen parent;
    private int page, rows;
    private String query = "";
    private boolean favoritesOnly;private String status="Local artwork · Standard colors";
    private List<EmojiDefinition> filtered = List.of();
    private final List<Button> rowButtons = new ArrayList<>();
    private Button previous, next;
    public EmojiSettingsScreen(Screen parent) { super(Component.literal("Local emojis")); this.parent = parent; }
    private Button button(String label,int x,int y,int w,Runnable action) {
        return addRenderableWidget(new StyleButton(label,x,y,w,20,action));
    }
    @Override protected void init() {
        rowButtons.clear(); rows = Math.max(1,(height-148)/28);
        var search = UiTheme.edit(12,52,width-200,"Search emojis");
        search.setMaxLength(128); search.setHint(Component.literal("Search names or paste an emoji")); search.setValue(query);
        search.setResponder(value -> { query=value; page=0; filter(); }); addRenderableWidget(search);
        button("Add",width-83,52,71,() -> minecraft.setScreen(new EmojiEditorScreen(this,null)));
        button(favoritesOnly?"Favorites":"All emojis",width-182,52,94,() -> {favoritesOnly=!favoritesOnly;page=0;rebuildWidgets();});
        previous=button("<",12,height-70,32,() -> {page--;refreshRows();});
        next=button(">",49,height-70,32,() -> {page++;refreshRows();});
        button("Reload local definitions",12,height-28,180,() -> {CokeChatClient.apply();status=CokeChatClient.manager().error().isEmpty()?"Local definitions reloaded":CokeChatClient.manager().error();filter();});
        button("Done",width-83,height-28,71,this::onClose);
        filter(); setInitialFocus(search);
    }
    private void filter() { filtered=CokeChatClient.emojis().search(query).stream().filter(d -> !favoritesOnly || CokeChatClient.config().emoji.favorites.contains(d.id())).toList();refreshRows(); }
    private void refreshRows() {
        for(var button:rowButtons)removeWidget(button); rowButtons.clear();
        int pages=Math.max(1,(filtered.size()+rows-1)/rows);page=Math.max(0,Math.min(page,pages-1));
        previous.active=page>0;next.active=page+1<pages;
        for(int i=page*rows;i<Math.min(filtered.size(),(page+1)*rows);i++){
            var definition=filtered.get(i);int y=87+(i-page*rows)*28;
            rowButtons.add(button("Edit",width-68,y-1,56,() -> minecraft.setScreen(new EmojiEditorScreen(this,definition))));
            boolean favorite=CokeChatClient.config().emoji.favorites.contains(definition.id());
            var star=button(favorite?"★":"☆",width-96,y-1,24,() -> {
                var favorites=CokeChatClient.config().emoji.favorites;
                if(!favorites.remove(definition.id()))favorites.add(definition.id());
                CokeChatClient.manager().save();filter();
            });
            star.setTooltip(Tooltip.create(Component.literal(favorite?"Remove favorite":"Add favorite")));rowButtons.add(star);
        }
    }
    @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float delta) {
        UiTheme.shell(g,width,height,"Emoji library","Search, favorite and customize your local emojis");
        for(int i=page*rows;i<Math.min(filtered.size(),(page+1)*rows);i++){
            var d=filtered.get(i);int y=87+(i-page*rows)*28;UiTheme.card(g,12,y-4,width-24,26);String code=":"+d.id()+":";
            g.text(font,CokeChatClient.text().process(Component.literal(code)),14,y,0xFFFFFFFF);
            g.text(font,UiTheme.fit(code,width-146),42,y,0xFFBBD1EE);
            if(mx>=42&&mx<width-72&&my>=y&&my<y+20)g.setTooltipForNextFrame(Component.literal(code),mx,my);
        }
        if(filtered.isEmpty())UiTheme.lines(g,"No matching emojis. Clear the search or switch to All emojis.",20,91,width-40,3,UiTheme.MUTED);
        g.text(font,(page+1)+" / "+Math.max(1,(filtered.size()+rows-1)/rows)+"  |  "+filtered.size()+" emojis",92,height-64,0xFF91ACCE);
        UiTheme.text(g,status,12,height-43,width-24,UiTheme.MUTED);
        super.extractRenderState(g,mx,my,delta);
    }
    @Override public void onClose(){minecraft.setScreen(parent);}
    @Override public boolean isPauseScreen(){return false;}
}
