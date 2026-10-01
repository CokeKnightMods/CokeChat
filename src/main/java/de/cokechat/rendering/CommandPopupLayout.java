package de.cokechat.rendering;
/** All measurements are GUI pixels, independent of physical display scale. */
public record CommandPopupLayout(int x,int y,int width,int height,int rows,int rowHeight,double scale) {
    public static final int MARGIN=4,GAP=5,PADDING=5;
    public static ChatInputLayout reserveAbove(ChatInputLayout box,int screenHeight,double fontSize){
        int row=(int)Math.ceil(fontSize+4);int reserve=Math.min(3*row+2*PADDING+GAP+MARGIN,Math.max(MARGIN,screenHeight/3));
        int height=Math.min(box.height(),Math.max(14,screenHeight-reserve-MARGIN));
        return new ChatInputLayout(box.x(),Math.min(screenHeight-height-MARGIN,Math.max(box.y(),reserve)),box.width(),height);
    }
    public static CommandPopupLayout of(ChatInputLayout input,int sw,int sh,double fontSize,int preferredWidth,int count){
        return of(input,sw,sh,fontSize,preferredWidth,count,10);
    }
    public static CommandPopupLayout of(ChatInputLayout input,int sw,int sh,double fontSize,int preferredWidth,int count,int maxRows){
        double scale=Math.max(5.0/9,Math.min(24.0/9,fontSize/9));int row=Math.max(9,(int)Math.ceil(12*scale));
        int w=Math.min(Math.max(60,preferredWidth),Math.max(1,sw-2*MARGIN));int x=Math.max(MARGIN,Math.min(input.x(),sw-MARGIN-w));
        int bottom=Math.min(sh-MARGIN,input.y()-GAP),available=Math.max(0,bottom-MARGIN-2*PADDING);
        int rows=Math.min(Math.max(0,count),Math.min(maxRows,available/row));int h=rows==0?0:rows*row+2*PADDING;
        return new CommandPopupLayout(x,Math.max(MARGIN,bottom-h),w,h,rows,row,scale);
    }
    public int wrapWidth(){return Math.max(1,(int)Math.floor((width-2*PADDING)/scale));}
    public boolean contains(double mx,double my){return height>0&&mx>=x&&mx<x+width&&my>=y&&my<y+height;}
    public int rowAt(double my){return (int)Math.floor((my-y-PADDING)/rowHeight);}
}
