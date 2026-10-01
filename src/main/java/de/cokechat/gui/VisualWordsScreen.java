package de.cokechat.gui;
import de.cokechat.CokeChatClient;
import de.cokechat.visualwords.VisualWordRule;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import java.util.Collections;
public final class VisualWordsScreen extends Screen {
    private final Screen parent;private int selected;private String error="";
    public VisualWordsScreen(Screen parent){super(Component.literal("Visual Words"));this.parent=parent;}
    private StyleButton button(String text,int x,int y,int w,Runnable action){return addRenderableWidget(new StyleButton(text,x,y,w,20,action));}
    private void update(){CokeChatClient.apply();rebuildWidgets();}
    @Override protected void init(){
        var rules=CokeChatClient.config().visualWords.rules;selected=Math.max(0,Math.min(selected,rules.size()-1));int x=Math.max(12,(width-560)/2),w=width-2*x;
        button("<",x,52,26,()->{selected--;error="";rebuildWidgets();}).active=selected>0;
        button(">",x+30,52,26,()->{selected++;error="";rebuildWidgets();}).active=selected+1<rules.size();
        button("New",x+62,52,46,()->{rules.add(new VisualWordRule(true,"word","replacement",false,true));selected=rules.size()-1;update();}).active=rules.size()<256;
        button("Delete",x+114,52,54,()->minecraft.setScreen(new ConfirmActionScreen(this,"Delete word rule?","This removes rule "+(selected+1)+". The other rules will retain their order.",()->{rules.remove(selected);update();}))).danger().active=!rules.isEmpty();
        var up=button("Up",x+174,52,(w-180)/2,()->{Collections.swap(rules,selected,selected-1);selected--;update();});up.active=selected>0;UiTheme.hint(up,"Move this rule earlier; earlier matching rules have priority");
        var down=button("Down",x+180+(w-180)/2,52,(w-180)/2,()->{Collections.swap(rules,selected,selected+1);selected++;update();});down.active=selected+1<rules.size();UiTheme.hint(down,"Move this rule later");
        if(!rules.isEmpty()){
            var r=rules.get(selected);
            var search=UiTheme.edit(x,96,w,"Search text");search.setMaxLength(256);search.setValue(r.searchText);
            search.setResponder(v->{if(!v.isEmpty()){r.searchText=v;CokeChatClient.apply();error="";}else error="Search text cannot be empty.";search.setTextColor(v.isEmpty()?UiTheme.ERROR:UiTheme.TEXT);});addRenderableWidget(search);
            var replacement=UiTheme.edit(x,133,w,"Local replacement — emoji codes supported");replacement.setMaxLength(1024);replacement.setValue(r.replacementText);replacement.setResponder(v->{r.replacementText=v;CokeChatClient.apply();});addRenderableWidget(replacement);
            int bw=(w-12)/3;
            button("Enabled: "+(r.enabled?"On":"Off"),x,163,bw,()->{r.enabled=!r.enabled;update();}).selected(r.enabled);
            button("Match case: "+(r.caseSensitive?"On":"Off"),x+bw+6,163,bw,()->{r.caseSensitive=!r.caseSensitive;update();}).selected(r.caseSensitive);
            button("Whole word: "+(r.wholeWord?"On":"Off"),x+2*(bw+6),163,bw,()->{r.wholeWord=!r.wholeWord;update();}).selected(r.wholeWord);
        }
        button("Done",width-92,height-28,80,this::onClose).primary();
    }
    @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float delta){
        var rules=CokeChatClient.config().visualWords.rules;UiTheme.shell(g,width,height,"Visual Words",rules.isEmpty()?"Create local word replacements":"Rule "+(selected+1)+" of "+rules.size()+" · First matching rule wins");int x=Math.max(12,(width-560)/2),w=width-2*x;
        if(rules.isEmpty()){UiTheme.card(g,x,84,w,96);UiTheme.lines(g,"No word rules yet. Select New to create a local replacement. Emoji codes are supported.",x+12,101,w-24,4,UiTheme.MUTED);}
        else{UiTheme.text(g,"SEARCH TEXT",x,84,w,UiTheme.MUTED);UiTheme.text(g,"REPLACEMENT",x,121,w,UiTheme.MUTED);}
        if(!error.isEmpty())UiTheme.text(g,error,x,187,w,UiTheme.ERROR);
        UiTheme.text(g,"Saved locally · Rules only affect the display",12,height-22,width-116,UiTheme.MUTED);super.extractRenderState(g,mx,my,delta);
    }
    @Override public void onClose(){CokeChatClient.apply();minecraft.setScreen(parent);}
    @Override public boolean isPauseScreen(){return false;}
}
