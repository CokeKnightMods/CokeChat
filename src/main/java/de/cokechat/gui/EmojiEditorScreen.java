package de.cokechat.gui;
import de.cokechat.CokeChatClient;
import de.cokechat.config.ConfigManager;
import de.cokechat.emoji.EmojiDefinition;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import java.nio.file.Files;
import java.util.*;
public final class EmojiEditorScreen extends Screen {
    private final Screen parent;private final String originalId;
    private final String[] labels={"ID / code (without colons)","Aliases (comma separated)","Unicode fallback", "Local font (optional namespace:name)","Font glyph (hex codepoint, e.g. E100)"};
    private final String[] values;private String status="";private String[] originalValues;private int tab,errorField=-1;private boolean saved;
    public EmojiEditorScreen(Screen parent,EmojiDefinition d){super(Component.literal("Emoji definition"));this.parent=parent;originalId=d==null?null:d.id();values=d==null?new String[]{"custom","","","","E100"}:new String[]{d.id(),d.aliases()==null?"":String.join(",",d.aliases()),d.unicode()==null?"":d.unicode(),d.font()==null?"":d.font(),d.glyph()==null||d.glyph().isEmpty()?"E100":Integer.toHexString(d.glyph().codePointAt(0)).toUpperCase(Locale.ROOT)};originalValues=values.clone();}
    @Override protected void init(){
        int left=Math.max(16,(width-520)/2),w=width-left*2;
        addRenderableWidget(new StyleButton("Identity",left,52,w/2-3,20,()->{tab=0;rebuildWidgets();}).selected(tab==0));
        addRenderableWidget(new StyleButton("Bitmap font",left+w/2+3,52,w/2-3,20,()->{tab=1;rebuildWidgets();}).selected(tab==1));
        int first=tab==0?0:3,last=tab==0?3:5;
        for(int i=first;i<last;i++){int row=i;var box=UiTheme.edit(left,96+(i-first)*34,w,labels[i]);box.setMaxLength(256);box.setValue(values[i]);box.setResponder(v->{values[row]=v;if(errorField==row){status="";errorField=-1;}});if(errorField==i)box.setTextColor(UiTheme.ERROR);addRenderableWidget(box);}
        int bw=(w-12)/3;
        addRenderableWidget(new StyleButton("Save",left,height-28,bw,20,()->save(false)).primary());
        var remove=addRenderableWidget(new StyleButton("Remove",left+bw+6,height-28,bw,20,()->minecraft.setScreen(new ConfirmActionScreen(this,"Remove emoji override?","The local definition will be removed. A built-in emoji, if present, will return to its default.",()->save(true)))).danger());UiTheme.hint(remove,"Remove this local emoji override; built-in artwork remains available");
        remove.active=originalId!=null;
        addRenderableWidget(new StyleButton("Back",left+2*(bw+6),height-28,bw,20,this::onClose));
    }
    private void invalid(int field,String message){errorField=field;throw new IllegalArgumentException(message);}
    private void save(boolean remove){
        try {
            var path=CokeChatClient.customEmojiFile();var list=new ArrayList<EmojiDefinition>();
            if(Files.exists(path))try(var reader=Files.newBufferedReader(path)){var saved=ConfigManager.JSON.fromJson(reader,EmojiDefinition[].class);if(saved!=null)list.addAll(Arrays.asList(saved));}
            if(!remove){
                if(!values[0].matches("[a-z0-9_+-]{1,64}"))invalid(0,"ID: lowercase letters, digits, underscores, + or -");
                if(values[2].isEmpty()&&values[3].isEmpty())invalid(2,"Enter Unicode or a local bitmap font");
                if(!values[3].isEmpty()){errorField=3;net.minecraft.resources.Identifier.parse(values[3]);}
                var aliases=Arrays.stream(values[1].split(",")).map(String::trim).filter(s->!s.isEmpty()).toList();
                if(aliases.stream().anyMatch(s->!s.matches("[a-z0-9_+-]{1,64}")))invalid(1,"Aliases: lowercase letters, digits, underscores, + or -");
                errorField=4;String glyph=values[3].isEmpty()?null:new String(Character.toChars(Integer.parseInt(values[4],16)));
                list.removeIf(d->d!=null&&(d.id().equals(originalId)||d.id().equals(values[0])));
                list.add(new EmojiDefinition(values[0],aliases,values[2],values[3].isEmpty()?null:values[3],glyph,16,16));
            }else list.removeIf(d->d!=null&&d.id().equals(originalId));
            ConfigManager.atomicWrite(path,ConfigManager.JSON.toJson(list));CokeChatClient.apply();saved=true;minecraft.setScreen(parent);
        }catch(Exception e){status=e.getMessage()==null?"Could not save local definitions":e.getMessage();if(errorField>=0)tab=errorField<3?0:1;rebuildWidgets();}
    }
    @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float delta){
        UiTheme.shell(g,width,height,"Emoji definition","Custom code, Unicode and optional local artwork");
        int left=Math.max(16,(width-520)/2),w=width-left*2,first=tab==0?0:3,last=tab==0?3:5;
        for(int i=first;i<last;i++)UiTheme.text(g,labels[i],left,84+(i-first)*34,w,UiTheme.MUTED);
        if(!status.isEmpty()){UiTheme.text(g,status,left,height-47,w,UiTheme.ERROR);if(my>=height-52&&my<height-35)g.setTooltipForNextFrame(Component.literal(status),mx,my);}
        else UiTheme.text(g,tab==0?"Changes are saved when you select Save.":"Artwork comes from installed local resource packs.",left,height-47,w,UiTheme.MUTED);
        super.extractRenderState(g,mx,my,delta);
    }
    @Override public void onClose(){if(!saved&&!Arrays.equals(values,originalValues))minecraft.setScreen(new ConfirmActionScreen(this,"Discard emoji changes?","Your unsaved changes to this definition will be discarded.",()->minecraft.setScreen(parent)));else minecraft.setScreen(parent);}
    @Override public boolean isPauseScreen(){return false;}
}
