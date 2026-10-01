package de.cokechat.rendering;
import de.cokechat.CokeChatClient;
import de.cokechat.mixin.SuggestionsListAccess;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.*;
import net.minecraft.util.FormattedCharSequence;
import java.util.*;
/** One popup layer; the original Brigadier suggestions still own selection and insertion. */
public final class CommandPopup {
    private record Row(FormattedCharSequence text,int option){}
    private final List<Row> rows=new ArrayList<>();
    private Object cachedSource;
    private int cachedHash,cachedWidth,offset,lastSelected=-1;
    private double lastX=Double.NaN,lastY=Double.NaN;
    private CommandPopupLayout layout;
    public CommandPopupLayout layout(){return layout;}
    public int lineCount(){return rows.size();}
    public int scrollOffset(){return offset;}
    public static boolean enabled(EditBox input){return CokeChatClient.config().enabled&&ChatInputStyle.target==input;}
    public void prepare(EditBox input,CommandSuggestions.SuggestionsList suggestions,List<FormattedCharSequence> usage){
        var mc=Minecraft.getInstance();var c=CokeChatClient.config();int sw=mc.getWindow().getGuiScaledWidth(),sh=mc.getWindow().getGuiScaledHeight();
        var box=ChatInputStyle.bounds; if(box==null)box=new ChatInputLayout(input.getX()-5,input.getY()-4,input.getWidth()+10,input.getHeight()+8);
        // Permit a useful hint width, but never let a long server-provided string grow past the screen.
        int preferred=Math.max(box.width(),Math.min(320,sw-8));
        if(suggestions==null)preferred=Math.min(box.width(),Math.max(80,(int)Math.ceil(usage.stream().mapToInt(mc.font::width).max().orElse(0)*c.input.fontSize/9)+2*CommandPopupLayout.PADDING));
        layout=CommandPopupLayout.of(box,sw,sh,c.input.fontSize,preferred,1);
        Object source=suggestions==null?usage:suggestions;int hash=suggestions==null?usage.hashCode():0;
        int wrap=layout.wrapWidth();
        if(source!=cachedSource||hash!=cachedHash||wrap!=cachedWidth){
            rows.clear();offset=0;lastSelected=-1;lastX=lastY=Double.NaN;
            if(suggestions!=null){var options=((SuggestionsListAccess)suggestions).cokechat$options();for(int i=0;i<options.size();i++)add(Component.literal(options.get(i).getText()),wrap,i);}
            else for(var line:usage){var component=Component.empty();line.accept((index,style,code)->{component.append(Component.literal(new String(Character.toChars(code))).setStyle(style));return true;});add(component,wrap,-1);}
            cachedSource=source;cachedHash=hash;cachedWidth=wrap;
        }
        layout=CommandPopupLayout.of(box,sw,sh,c.input.fontSize,preferred,rows.size(),suggestions==null?2:8);
        ChatPresentation.commandInset=0;
        if(suggestions==null&&layout.height()>0&&layout.x()<ChatPresentation.x()+ChatPresentation.width()+12&&layout.x()+layout.width()>ChatPresentation.x()-5
                &&layout.y()<ChatPresentation.baseBottom()&&layout.y()+layout.height()>ChatPresentation.baseBottom()-ChatPresentation.height())
            ChatPresentation.commandInset=(int)Math.min(Math.max(0,ChatPresentation.baseBottom()-32),Math.ceil(ChatPresentation.baseBottom()-layout.y()+CommandPopupLayout.GAP));
        if(suggestions!=null){int selected=((SuggestionsListAccess)suggestions).cokechat$selected();if(selected!=lastSelected){
            int first=0,last=0;for(int i=0;i<rows.size();i++)if(rows.get(i).option==selected){first=i;last=i;while(last+1<rows.size()&&rows.get(last+1).option==selected)last++;break;}
            if(first<offset||first>=offset+layout.rows())offset=first;
            else if(last>=offset+layout.rows()&&last-first+1<=layout.rows())offset=last-layout.rows()+1;
            lastSelected=selected;
        }}
        offset=Math.max(0,Math.min(offset,Math.max(0,rows.size()-layout.rows())));
    }
    private void add(Component text,int wrap,int option){for(var line:Minecraft.getInstance().font.split(text,wrap))rows.add(new Row(line,option));}
    public void draw(GuiGraphicsExtractor g,int mx,int my,EditBox input,CommandSuggestions.SuggestionsList suggestions,List<FormattedCharSequence> usage){
        prepare(input,suggestions,usage);if(layout.rows()==0)return;
        var c=CokeChatClient.config();var mc=Minecraft.getInstance();var p=layout;
        if(suggestions!=null&&(mx!=lastX||my!=lastY)){int option=optionAt(mx,my);if(option>=0){suggestions.select(option);lastSelected=option;}lastX=mx;lastY=my;}
        // Called after the input and after the history has popped its own scissor.
        ChatBackgroundRenderer.panel(g,p.x(),p.y(),p.width(),p.height(),c.appearance.inputRadius,c.appearance.inputColor,Math.max(.94,c.appearance.inputOpacity),c.input.borderColor,c.appearance.inputBorder?c.input.borderOpacity:0);
        int selected=suggestions==null?-1:((SuggestionsListAccess)suggestions).cokechat$selected();
        g.enableScissor(p.x()+2,p.y()+2,p.x()+p.width()-2,p.y()+p.height()-2);
        try{for(int i=0;i<p.rows();i++){
            var row=rows.get(offset+i);int y=p.y()+CommandPopupLayout.PADDING+i*p.rowHeight();
            if(row.option>=0&&row.option==selected)RoundedRenderer.fill(g,p.x()+3,y-1,p.width()-6,p.rowHeight(),3,0xFF344665);
            g.pose().pushMatrix();g.pose().translate(p.x()+CommandPopupLayout.PADDING,y+Math.max(0,(p.rowHeight()-(float)c.input.fontSize)/2));g.pose().scale((float)p.scale(),(float)p.scale());
            g.text(mc.font,row.text,0,0,RoundedRenderer.color(row.option==selected&&selected>=0?0x9AEAF3:c.input.textColor,1));g.pose().popMatrix();
        }}finally{g.disableScissor();}
        if(offset>0)g.fill(p.x()+6,p.y()+2,p.x()+p.width()-6,p.y()+3,0xFF72D9E8);
        if(offset+p.rows()<rows.size())g.fill(p.x()+6,p.y()+p.height()-3,p.x()+p.width()-6,p.y()+p.height()-2,0xFF72D9E8);
        if(suggestions!=null&&optionAt(mx,my)>=0){var tip=((SuggestionsListAccess)suggestions).cokechat$options().get(selected).getTooltip();if(tip!=null)g.setTooltipForNextFrame(mc.font,ComponentUtils.fromMessage(tip),mx,p.y());}
    }
    private int optionAt(double mx,double my){if(layout==null||!layout.contains(mx,my))return -1;int row=layout.rowAt(my);return row>=0&&row<layout.rows()&&offset+row<rows.size()?rows.get(offset+row).option:-1;}
    public boolean click(double mx,double my,EditBox input,CommandSuggestions.SuggestionsList suggestions,List<FormattedCharSequence> usage){prepare(input,suggestions,usage);if(!layout.contains(mx,my))return false;int option=optionAt(mx,my);if(option>=0&&suggestions!=null){suggestions.select(option);suggestions.useSuggestion();}return true;}
    public boolean scroll(double amount,EditBox input,CommandSuggestions.SuggestionsList suggestions,List<FormattedCharSequence> usage){prepare(input,suggestions,usage);var mc=Minecraft.getInstance();if(!layout.contains(mc.mouseHandler.getScaledXPos(mc.getWindow()),mc.mouseHandler.getScaledYPos(mc.getWindow())))return false;offset=Math.max(0,Math.min(Math.max(0,rows.size()-layout.rows()),offset-(int)Math.signum(amount)));return true;}
}
