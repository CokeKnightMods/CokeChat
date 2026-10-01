package de.cokechat.gui;
import de.cokechat.CokeChatClient;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import java.util.function.*;
public final class ValueScreen extends Screen {
    private final Screen parent;private final Supplier<String> get;private final Consumer<String> set;private final boolean color;
    private String error="",draft;private Button done;
    public ValueScreen(Screen parent,String title,Supplier<String> get,Consumer<String> set,boolean color){super(Component.literal(title));this.parent=parent;this.get=get;this.set=set;this.color=color;draft=get.get();}
    private void change(String value){draft=value;try{set.accept(value);CokeChatClient.applySettings();error=CokeChatClient.manager().error();}catch(IllegalArgumentException e){error="Invalid value. Check the allowed range or format above.";}done.active=error.isEmpty();}
    @Override protected void init(){int w=Math.min(400,width-48),x=(width-w)/2;var edit=UiTheme.edit(x,78,w,title.getString());edit.setMaxLength(256);edit.setValue(draft);addRenderableWidget(edit);
        done=addRenderableWidget(new StyleButton("Done",width-92,height-28,80,20,this::onClose).primary());done.active=error.isEmpty();edit.setResponder(this::change);
        if(color)for(int i=0;i<3;i++){int shift=(2-i)*8,rgb=Integer.parseInt(get.get().substring(1),16);addRenderableWidget(new SettingSlider(x,119+i*25,w,(rgb>>shift)&255,0,255,1,v->{int existing=Integer.parseInt(get.get().substring(1),16);edit.setValue(String.format("#%06X",(existing&~(255<<shift))|((int)v<<shift)));}));}
        setInitialFocus(edit);
    }
    @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float delta){UiTheme.shell(g,width,height,title.getString(),"Exact value · Valid changes are saved immediately");int w=Math.min(400,width-48),x=(width-w)/2;UiTheme.text(g,"VALUE",x,62,w,UiTheme.MUTED);if(!error.isEmpty())UiTheme.lines(g,error,x,104,w,3,UiTheme.ERROR);if(height>300)CokeChatSettingsScreen.drawPreview(g,x,height-120,w,70);super.extractRenderState(g,mx,my,delta);}
    @Override public void onClose(){minecraft.setScreen(parent);}
    @Override public boolean isPauseScreen(){return false;}
}
