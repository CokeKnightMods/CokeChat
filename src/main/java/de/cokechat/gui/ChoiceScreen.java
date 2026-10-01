package de.cokechat.gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import java.util.*;
import java.util.function.Consumer;
public final class ChoiceScreen extends Screen {
    private final Screen parent;private final List<String> options;private final Consumer<String> choose;
    private String query="";private int page,rows;private final List<Button> buttons=new ArrayList<>();private List<String> filtered=List.of();private Button previous,next;
    public ChoiceScreen(Screen parent,String title,List<String> options,Consumer<String> choose){super(Component.literal(title));this.parent=parent;this.options=options;this.choose=choose;}
    @Override protected void init(){
        buttons.clear();rows=Math.max(1,(height-136)/25);int w=Math.min(420,width-32),x=(width-w)/2;
        var search=UiTheme.edit(x,53,w,"Find an option");search.setMaxLength(128);search.setHint(Component.literal("Find an option..."));search.setValue(query);search.setResponder(v->{query=v;page=0;refresh();});addRenderableWidget(search);
        previous=addRenderableWidget(new StyleButton("<",x,height-57,28,20,()->{page--;refresh();}));next=addRenderableWidget(new StyleButton(">",x+34,height-57,28,20,()->{page++;refresh();}));
        addRenderableWidget(new StyleButton("Back",width-92,height-28,80,20,this::onClose));refresh();setInitialFocus(search);
    }
    private void refresh(){for(var b:buttons)removeWidget(b);buttons.clear();filtered=options.stream().filter(s->s.toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT))).toList();int pages=Math.max(1,(filtered.size()+rows-1)/rows);page=Math.clamp(page,0,pages-1);previous.active=page>0;next.active=page+1<pages;int w=Math.min(420,width-32);
        for(int i=page*rows;i<Math.min(filtered.size(),(page+1)*rows);i++){String option=filtered.get(i);buttons.add(addRenderableWidget(new StyleButton(option,(width-w)/2,81+(i-page*rows)*25,w,21,()->{choose.accept(option);onClose();})));}}
    @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float delta){UiTheme.shell(g,width,height,title.getString(),"Choose an option · Tab and Enter supported");if(filtered.isEmpty())UiTheme.text(g,"No matching options",(width-Math.min(420,width-32))/2,87,width-32,UiTheme.MUTED);UiTheme.text(g,(page+1)+" / "+Math.max(1,(filtered.size()+rows-1)/rows),Math.max(16,(width-420)/2)+72,height-51,100,UiTheme.MUTED);super.extractRenderState(g,mx,my,delta);}
    @Override public boolean mouseScrolled(double x,double y,double h,double v){page+=v<0?1:-1;refresh();return true;}
    @Override public void onClose(){minecraft.setScreen(parent);}
    @Override public boolean isPauseScreen(){return false;}
}
