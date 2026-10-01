package de.cokechat.gui;
import de.cokechat.CokeChatClient;
import de.cokechat.rendering.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.client.renderer.texture.DynamicTexture;
import com.mojang.blaze3d.platform.NativeImage;
import java.util.function.*;
/** Live target-specific editing with transactional Apply/Cancel and independent defaults. */
public final class ColorPickerScreen extends Screen {
    private final Screen parent;
    private final IntConsumer setRgb;
    private final DoubleConsumer setAlpha;
    private final int original,initial;
    private final double originalAlpha,initialAlpha;
    private final ColorValue draft;
    private final Identifier wheelId=Identifier.fromNamespaceAndPath("cokechat","picker/"+java.util.UUID.randomUUID());
    private final PreviewChat preview=new PreviewChat();
    private EditBox hex;private final EditBox[] rgb=new EditBox[3];
    private SettingSlider brightness,alpha;private StyleButton apply;
    private int cx,cy,radius;
    private boolean syncing,dragging,accepted,textureRegistered;
    private String error="";
    public ColorPickerScreen(Screen parent,String name,IntSupplier get,IntConsumer set,DoubleSupplier opacity,DoubleConsumer setOpacity,int initial,double initialAlpha){
        super(Component.literal(name));this.parent=parent;this.setRgb=set;this.setAlpha=setOpacity;original=get.getAsInt();originalAlpha=opacity.getAsDouble();this.initial=initial;this.initialAlpha=initialAlpha;draft=new ColorValue(original,originalAlpha);
    }
    @Override protected void init(){
        radius=Math.max(25,Math.min(64,Math.min((height-132)/2,(width-198)/2)));cx=Math.max(radius+16,width/2-105);cy=62+radius;
        int right=Math.min(width-166,cx+radius+18);
        hex=addRenderableWidget(UiTheme.edit(right,66,150,"HEX RGBA: #RRGGBB or #RRGGBBAA"));hex.setMaxLength(9);hex.setResponder(v->{if(syncing)return;try{draft.hex(v);publish(hex);}catch(IllegalArgumentException e){error="HEX: #RRGGBB or #RRGGBBAA";hex.setTextColor(UiTheme.ERROR);apply.active=false;}});
        for(int n=0;n<3;n++){int channel=n;rgb[n]=addRenderableWidget(UiTheme.edit(right+n*51,99,47,new String[]{"Red","Green","Blue"}[n]+" (0–255)"));rgb[n].setMaxLength(3);rgb[n].setResponder(v->{if(syncing)return;try{int number=Integer.parseInt(v);if(number<0||number>255)throw new IllegalArgumentException();int shift=(2-channel)*8;draft.set((draft.rgb()&~(255<<shift))|(number<<shift),draft.alpha);publish(rgb[channel]);}catch(IllegalArgumentException e){error="RGB channels: 0–255";rgb[channel].setTextColor(UiTheme.ERROR);apply.active=false;}});}
        brightness=addRenderableWidget(new SettingSlider(right,132,150,draft.value,0,1,.01,v->{draft.value=(float)v;publish();}));
        alpha=addRenderableWidget(new SettingSlider(right,165,150,draft.alpha,0,1,.01,v->{draft.alpha=v;publish();}));
        apply=addRenderableWidget(new StyleButton("Apply",width/2-112,height-28,70,20,()->{accepted=true;onClose();}).primary());
        addRenderableWidget(new StyleButton("Cancel",width/2-35,height-27,70,20,this::onClose));
        addRenderableWidget(new StyleButton("Reset",width/2+42,height-27,70,20,()->{draft.set(initial,initialAlpha);publish();}));
        if(!textureRegistered){var image=new NativeImage(128,128,true);for(int y=0;y<128;y++)for(int x=0;x<128;x++){double dx=(x-63.5)/63.5,dy=(y-63.5)/63.5,d=Math.hypot(dx,dy);int color=d>1?0:0xFF000000|(java.awt.Color.HSBtoRGB((float)((Math.atan2(dy,dx)/(Math.PI*2)+1)%1),(float)d,1)&0xFFFFFF);image.setPixel(x,y,color);}minecraft.getTextureManager().register(wheelId,new DynamicTexture(()->"CokeChat color wheel",image));textureRegistered=true;}
        sync();
    }
    private void sync(){sync(null);}
    private void sync(EditBox editing){syncing=true;if(hex!=editing)hex.setValue(draft.hex());for(int i=0;i<3;i++)if(rgb[i]!=editing)rgb[i].setValue(Integer.toString((draft.rgb()>>((2-i)*8))&255));brightness.setCurrent(draft.value);alpha.setCurrent(draft.alpha);syncing=false;}
    private void publish(){publish(null);}
    private void publish(EditBox editing){setRgb.accept(draft.rgb());setAlpha.accept(draft.alpha);CokeChatClient.applySettings();error="";hex.setTextColor(UiTheme.TEXT);for(var box:rgb)box.setTextColor(UiTheme.TEXT);apply.active=true;sync(editing);}
    private void pick(double x,double y){double dx=(x-cx)/radius,dy=(y-cy)/radius;draft.hue=(float)((Math.atan2(dy,dx)/(2*Math.PI)+1)%1);draft.saturation=(float)Math.min(1,Math.hypot(dx,dy));publish();}
    @Override public boolean mouseClicked(MouseButtonEvent event,boolean twice){if(event.button()==0&&Math.hypot(event.x()-cx,event.y()-cy)<=radius){dragging=true;pick(event.x(),event.y());return true;}return super.mouseClicked(event,twice);}
    @Override public boolean mouseDragged(MouseButtonEvent event,double dx,double dy){if(dragging){pick(event.x(),event.y());return true;}return super.mouseDragged(event,dx,dy);}
    @Override public boolean mouseReleased(MouseButtonEvent event){dragging=false;return super.mouseReleased(event);}
    @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float delta){
        UiTheme.shell(g,width,height,title.getString(),"Live preview · Apply to keep, Cancel to restore");
        g.blit(wheelId,cx-radius,cy-radius,cx+radius,cy+radius,0,1,0,1);
        double angle=draft.hue*Math.PI*2;int px=cx+(int)(Math.cos(angle)*draft.saturation*radius),py=cy+(int)(Math.sin(angle)*draft.saturation*radius);g.outline(px-3,py-3,7,7,0xFF000000);g.outline(px-2,py-2,5,5,0xFFFFFFFF);
        int right=hex.getX();g.text(font,"HEX / RGBA",right,54,0xFF9DADC8);g.text(font,"R       G       B",right,87,0xFF9DADC8);g.text(font,"Brightness / Value",right,120,0xFF9DADC8);g.text(font,"Alpha / Opacity",right,153,0xFF9DADC8);
        for(int y=cy+radius+8;y<cy+radius+16;y+=8)for(int x=cx-radius;x<cx+radius;x+=8)g.fill(x,y,Math.min(x+8,cx+radius),y+8,((x/8+y/8)&1)==0?0xFF637087:0xFF303B4E);
        g.fill(cx-radius,cy+radius+8,cx+radius,cy+radius+24,RoundedRenderer.color(draft.rgb(),draft.alpha));
        int previewY=Math.max(210,height-130);if(height-previewY>=95){preview.draw(g,12,previewY,width-24,height-previewY-56,true);var c=CokeChatClient.config();var box=new ChatInputLayout(12,height-52,width-24,18);box.draw(g,c);g.text(font,"CokeKnight: :heart: — input preview",18,height-47,RoundedRenderer.color(c.input.textColor,c.input.textOpacity));}
        if(!error.isEmpty())UiTheme.text(g,error,12,height-47,width-24,UiTheme.ERROR);super.extractRenderState(g,mx,my,delta);
    }
    @Override public void onClose(){minecraft.setScreen(parent);}
    @Override public void removed(){if(!accepted){setRgb.accept(original);setAlpha.accept(originalAlpha);CokeChatClient.applySettings();}if(textureRegistered){minecraft.getTextureManager().release(wheelId);textureRegistered=false;}}
    @Override public boolean isPauseScreen(){return false;}
}
