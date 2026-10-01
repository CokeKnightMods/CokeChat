package de.cokechat.gui;
import de.cokechat.CokeChatClient;
import de.cokechat.rendering.RoundedRenderer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
public final class PreviewScreen extends Screen {
    private final Screen parent;
    private final PreviewChat sample=new PreviewChat();
    public PreviewScreen(Screen parent){super(Component.literal("CokeChat preview"));this.parent=parent;}
    @Override protected void init(){
        addRenderableWidget(new StyleButton("Back",width-76,height-28,64,20,this::onClose));addRenderableWidget(new StyleButton("Replay",width-148,height-28,64,20,sample::reopen));
        addRenderableWidget(new StyleButton("Reset sample",12,height-28,96,20,sample::replay));
        int w=(width-42)/4;
        var add=addRenderableWidget(new StyleButton("New",12,52,w,20,()->sample.receive(false)));UiTheme.hint(add,"Add a new sample message");addRenderableWidget(new StyleButton("Duplicate",18+w,52,w,20,()->sample.receive(true)));
        var copy=addRenderableWidget(new StyleButton("Copy",24+w*2,52,w,20,sample::copySelection));var hide=addRenderableWidget(new StyleButton("Hide",30+w*3,52,w,20,sample::hideSelection));
        var actions=CokeChatClient.config().actions;copy.active=actions.enabled&&actions.copy;hide.active=actions.enabled&&actions.delete;UiTheme.hint(copy,"Copy selected sample; Page Up / Page Down selects a message");UiTheme.hint(hide,"Hide selected sample; Reset sample restores it");
    }
    @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float delta){
        UiTheme.shell(g,width,height,"Chat preview","Local sample · Page Up / Down selects a message");
        var a=CokeChatClient.config().appearance;int x=CokeChatClient.offsetX();int bottom=Math.max(116,height-68-CokeChatClient.offsetY());int h=Math.min(a.height,Math.max(30,bottom-82));
        sample.draw(g,x,bottom-h,Math.min(a.width,width-x-12),h,false);
        var c=CokeChatClient.config();var box=de.cokechat.rendering.ChatInputLayout.of(c,width,height,x,bottom);box=new de.cokechat.rendering.ChatInputLayout(box.x(),Math.clamp(box.y(),82,height-40-box.height()),box.width(),box.height());box.draw(g,c);
        g.enableScissor(box.x(),box.y(),box.x()+box.width(),box.y()+box.height());g.pose().pushMatrix();g.pose().translate(box.x()+5,box.y()+(box.height()-(float)c.input.fontSize)/2);g.pose().scale((float)c.input.fontSize/9,(float)c.input.fontSize/9);g.text(font,"gg :sob:  (local input preview)",0,0,RoundedRenderer.color(c.input.textColor,c.input.textOpacity));g.pose().popMatrix();g.disableScissor();super.extractRenderState(g,mx,my,delta);
    }
    @Override public void onClose(){minecraft.setScreen(parent);}
    @Override public boolean keyPressed(net.minecraft.client.input.KeyEvent event){if(event.key()==266||event.key()==267){sample.select(event.key()==266?1:-1);return true;}return super.keyPressed(event);}
    @Override public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent event,boolean doubleClick){if(event.button()==0&&sample.click(event.x(),event.y()))return true;return super.mouseClicked(event,doubleClick);}
    @Override public boolean mouseScrolled(double x,double y,double horizontal,double vertical){return sample.wheel(x,y,vertical)||super.mouseScrolled(x,y,horizontal,vertical);}
    @Override public boolean isPauseScreen(){return false;}
}
