package de.cokechat.gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
/** Destructive actions execute only after an explicit confirmation. Escape cancels. */
public final class ConfirmActionScreen extends Screen {
    private final Screen parent;private final String description;private final Runnable action;
    public ConfirmActionScreen(Screen parent,String title,String description,Runnable action){super(Component.literal(title));this.parent=parent;this.description=description;this.action=action;}
    @Override protected void init(){
        var cancel=addRenderableWidget(new StyleButton("Cancel",width/2-104,height-28,100,20,this::onClose));
        addRenderableWidget(new StyleButton("Confirm",width/2+4,height-28,100,20,()->{minecraft.setScreen(parent);action.run();}).danger());setInitialFocus(cancel);
    }
    @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float delta){UiTheme.shell(g,width,height,title.getString(),"Review this action before continuing");UiTheme.card(g,16,56,width-32,height-106);UiTheme.lines(g,description,28,70,width-56,Math.max(1,(height-126)/11),UiTheme.TEXT);super.extractRenderState(g,mx,my,delta);}
    @Override public void onClose(){minecraft.setScreen(parent);}
    @Override public boolean isPauseScreen(){return false;}
}
