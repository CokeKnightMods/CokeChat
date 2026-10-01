package de.cokechat.gui;
/** Each picker owns one draft. No global selected color or shared HSV state. */
public final class ColorValue {
    public float hue,saturation,value;
    public double alpha;
    public ColorValue(int rgb,double alpha){set(rgb,alpha);}
    public void set(int rgb,double alpha){float[] hsv=java.awt.Color.RGBtoHSB((rgb>>16)&255,(rgb>>8)&255,rgb&255,null);hue=hsv[0];saturation=hsv[1];value=hsv[2];this.alpha=Math.max(0,Math.min(1,alpha));}
    public int rgb(){return java.awt.Color.HSBtoRGB(hue,saturation,value)&0xFFFFFF;}
    public void hex(String text){String digits=text.strip().replaceFirst("^#","");if(!digits.matches("[0-9a-fA-F]{6}([0-9a-fA-F]{2})?"))throw new IllegalArgumentException();long packed=Long.parseLong(digits,16);set((int)(digits.length()==8?packed>>>8:packed),digits.length()==8?(packed&255)/255.0:alpha);}
    public String hex(){return String.format("#%06X%02X",rgb(),Math.round(alpha*255));}
}
