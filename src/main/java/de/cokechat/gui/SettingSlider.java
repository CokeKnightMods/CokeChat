package de.cokechat.gui;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;
import java.util.function.DoubleConsumer;
public final class SettingSlider extends AbstractSliderButton {
    private final double min,max,step;
    private final DoubleConsumer change;
    public SettingSlider(int x,int y,int width,double value,double min,double max,double step,DoubleConsumer change) {
        super(x,y,width,20,Component.empty(),(value-min)/(max-min));this.min=min;this.max=max;this.step=step;this.change=change;updateMessage();
    }
    private double current() { return Math.max(min,Math.min(max,Math.round((min+value*(max-min))/step)*step)); }
    public void setCurrent(double number){value=Math.max(0,Math.min(1,(number-min)/(max-min)));updateMessage();}
    public static String format(double value) { return value==Math.rint(value)?Long.toString((long)value):String.format(java.util.Locale.ROOT,"%.2f",value).replaceAll("0+$","").replaceAll("\\.$",""); }
    @Override protected void updateMessage() { setMessage(Component.literal(format(current()))); }
    @Override protected void applyValue() { change.accept(current()); }
    @Override public void extractWidgetRenderState(net.minecraft.client.gui.GuiGraphicsExtractor g,int mx,int my,float delta) {
        de.cokechat.rendering.RoundedRenderer.fill(g,getX(),getY(),getWidth(),getHeight(),4,active?UiTheme.CONTROL:UiTheme.SURFACE);
        int fill=(int)((getWidth()-4)*value);
        de.cokechat.rendering.RoundedRenderer.fill(g,getX()+2,getY()+2,Math.max(3,fill),getHeight()-4,3,0xFF35505B);
        g.fill(getX()+Math.max(2,fill),getY()+4,getX()+Math.max(2,fill)+2,getBottom()-4,0xFF8BE7F5);
        g.centeredText(net.minecraft.client.Minecraft.getInstance().font,getMessage(),getX()+getWidth()/2,getY()+6,0xFFF1F6FF);
        if(isFocused())g.outline(getX(),getY(),getWidth(),getHeight(),UiTheme.ACCENT);
    }
}
