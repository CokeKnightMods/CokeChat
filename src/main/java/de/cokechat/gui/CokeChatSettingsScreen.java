package de.cokechat.gui;

import de.cokechat.CokeChatClient;
import de.cokechat.compact.CompactChatManager;
import de.cokechat.config.CokeChatConfig;
import de.cokechat.rendering.RoundedRenderer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import java.util.*;
import java.util.function.*;

public final class CokeChatSettingsScreen extends Screen {
    private enum Kind { ACTION, TOGGLE, NUMBER, TEXT, COLOR, CHOICE }
    private record Field(int category,String name,String description,Kind kind,Supplier<String> value,Consumer<String> set,Runnable action,Runnable reset,double min,double max,double step,List<String> choices) { }
    private static final String[] CATEGORIES={"Chat","Appearance","Compact Chat","History","Visual Words","Emojis","Advanced","Chat Animation","Chat Peek","Message Actions","Chat Input"};
    private static final int[] NAVIGATION={0,1,10,7,2,3,4,5,8,9,6};
    private static int navigationIndex(int category){for(int i=0;i<NAVIGATION.length;i++)if(NAVIGATION[i]==category)return i;return 0;}
    private final Screen parent;
    private final List<Field> fields=new ArrayList<>(),visible=new ArrayList<>();
    private final List<AbstractWidget> controls=new ArrayList<>();
    private int category,page,rows,contentX=12,contentWidth,buildingCategory;
    private int categoryPage,categoryRows;
    private String query="",status="";
    private Button previous,next;
    private boolean sidebar,stacked;private int listTop=88,rowHeight,previewY;private String fieldError="";
    public CokeChatSettingsScreen(Screen parent){super(Component.literal("CokeChat"));this.parent=parent;}
    private Button button(String name,int x,int y,int w,Runnable action){return addRenderableWidget(new StyleButton(name,x,y,w,20,action));}
    private void changed(){CokeChatClient.applySettings();status=CokeChatClient.manager().error();}
    private void add(String name,String description,Kind kind,Supplier<String> value,Consumer<String> set,Runnable action,Runnable reset,double min,double max,double step,List<String> choices){fields.add(new Field(buildingCategory,name,description,kind,value,set,action,reset,min,max,step,choices));}
    private void toggle(String name,String description,BooleanSupplier get,Consumer<Boolean> set,boolean initial){add(name,description,Kind.TOGGLE,()->get.getAsBoolean()?"On":"Off",v->set.accept(v.equals("On")),null,()->set.accept(initial),0,0,0,List.of());}
    private void number(String name,String description,DoubleSupplier get,DoubleConsumer set,double min,double max,double step,double initial){
        add(name,description,Kind.NUMBER,()->SettingSlider.format(get.getAsDouble()),s->{double v=Double.parseDouble(s);if(!Double.isFinite(v)||v<min||v>max)throw new IllegalArgumentException();set.accept(v);},null,()->set.accept(initial),min,max,step,List.of());
    }
    private void action(String name,String description,String label,Runnable action){add(name,description,Kind.ACTION,()->label,null,action,null,0,0,0,List.of());}
    private void color(String name,String description,IntSupplier get,IntConsumer set,DoubleSupplier alpha,DoubleConsumer setAlpha,int initial,double initialAlpha){add(name,description,Kind.COLOR,()->String.format("#%06X",get.getAsInt()),null,()->minecraft.setScreen(new ColorPickerScreen(this,name,get,set,alpha,setAlpha,initial,initialAlpha)),()->{set.accept(initial);setAlpha.accept(initialAlpha);},0,0,0,List.of());}
    private void choice(String name,String description,Supplier<String> get,Consumer<String> set,String initial,List<String> choices){add(name,description,Kind.CHOICE,get,set,null,()->set.accept(initial),0,0,0,choices);}
    private void buildFields(){
        fields.clear();var c=CokeChatClient.config();var a=c.appearance;var d=new CokeChatConfig().appearance;
        buildingCategory=0;
        toggle("Enable CokeChat","Apply your local chat style",()->c.enabled,v->c.enabled=v,true);
        action("Settings shortcut","Choose your key in Minecraft controls","Key bindings",()->minecraft.setScreen(new net.minecraft.client.gui.screens.options.controls.KeyBindsScreen(this,minecraft.options)));
        action("Live preview","Fictional Clashbad / CokeKnight chat","Open preview",()->minecraft.setScreen(new PreviewScreen(this)));
        buildingCategory=1;
        toggle("Chat Border","Show only the main chat panel outline",()->a.chatBorder,v->a.chatBorder=v,true);
        toggle("Input Field Border","Independent outline around the input field",()->a.inputBorder,v->a.inputBorder=v,true);
        number("Chat width","Width of the chat panel",()->a.width,v->a.width=(int)v,80,1200,1,d.width);
        number("Chat height","Maximum visible chat height",()->a.height,v->a.height=(int)v,30,800,1,d.height);
        number("Position X","Distance from the left edge",()->a.x,v->a.x=(int)v,0,1200,1,d.x);
        number("Bottom offset","Distance above the chat input",()->a.y,v->a.y=(int)v,0,800,1,d.y);
        number("Font size","Chat text size",()->a.fontSize,v->a.fontSize=v,5,24,.5,d.fontSize);
        number("Corner radius","Rounded background corners",()->a.cornerRadius,v->a.cornerRadius=(int)v,0,32,1,d.cornerRadius);
        number("Message spacing","Space between chat lines",()->a.messageSpacing,v->a.messageSpacing=(int)v,0,12,1,d.messageSpacing);
        color("Background color","Independent color wheel / RGBA",()->a.backgroundColor,v->a.backgroundColor=v,()->a.backgroundOpacity,v->a.backgroundOpacity=v,d.backgroundColor,d.backgroundOpacity);
        color("Chat border color","Independent border wheel",()->a.borderColor,v->a.borderColor=v,()->a.borderOpacity,v->a.borderOpacity=v,d.borderColor,d.borderOpacity);
        color("Message text color","Default text; server formatting is retained",()->a.textColor,v->a.textColor=v,()->a.textOpacity,v->a.textOpacity=v,d.textColor,d.textOpacity);
        color("Scrollbar color","Independent scrollbar wheel",()->a.scrollbarColor,v->a.scrollbarColor=v,()->a.scrollbarOpacity,v->a.scrollbarOpacity=v,d.scrollbarColor,d.scrollbarOpacity);
        number("Background opacity","Transparency of the chat panel",()->a.backgroundOpacity,v->a.backgroundOpacity=v,0,1,.01,d.backgroundOpacity);
        toggle("Chat Side Bar / Scroll Bar","Show the side indicators and history scrollbar; scrolling remains available",()->a.scrollbar,v->a.scrollbar=v,true);
        number("Scrollbar width","Width in pixels",()->a.scrollbarWidth,v->a.scrollbarWidth=(int)v,1,8,1,d.scrollbarWidth);
        number("Scrollbar opacity","Scrollbar transparency",()->a.scrollbarOpacity,v->a.scrollbarOpacity=v,0,1,.01,d.scrollbarOpacity);
        buildingCategory=2;
        toggle("Enable Compact Chat","Group duplicate messages locally",()->c.compact.enabled,v->c.compact.enabled=v,false);
        action("Grouping mode","Duplicates merge across intervening messages; latest occurrence stays newest","Time Window",()->{});
        number("Grouping time","Seconds since the last matching message; each repeat restarts the timer",()->c.compact.groupingSeconds,v->c.compact.groupingSeconds=v,.5,300,.5,2);
        toggle("Show counter","Display the duplicate count",()->c.compact.showCounter,v->c.compact.showCounter=v,true);
        choice("Counter style","Choose a duplicate counter format",()->c.compact.counterFormat,v->c.compact.counterFormat=v,"×{count}",List.of("×{count}","[{count}]","({count}x)","x{count}"));
        add("Custom counter","Use {count}, e.g. [{count}x]",Kind.TEXT,()->c.compact.counterFormat,s->{if(!s.contains("{count}")||s.length()>32||s.contains("\n"))throw new IllegalArgumentException();c.compact.counterFormat=s;},null,()->c.compact.counterFormat="×{count}",0,0,0,List.of());
        buildingCategory=3;
        toggle("Extended history","Retain more received messages",()->c.history.enabled,v->c.history.enabled=v,true);
        choice("Maximum messages","Limit counts original messages",()->c.history.maxMessages<0?"Unlimited":Integer.toString(c.history.maxMessages),v->c.history.maxMessages=v.equals("Unlimited")?-1:Integer.parseInt(v),"1000",List.of("100","500","1000","5000","10000","Unlimited"));
        toggle("Save locally","Persist history per world or server",()->c.history.persistent,v->c.history.persistent=v,false);
        action("Clear history","Remove current local chat history","Clear",()->minecraft.setScreen(new ConfirmActionScreen(this,"Clear chat history?","This removes the current chat history from the display and local archive. This cannot be undone.",()->{CokeChatClient.clearHistory();status="Current history cleared";})));
        buildingCategory=4;
        toggle("Visual Words","Replace words in local display",()->c.visualWords.enabled,v->c.visualWords.enabled=v,true);
        action("Ordered rules","Add, edit, remove and reorder replacements","Manage rules",()->minecraft.setScreen(new VisualWordsScreen(this)));
        buildingCategory=5;
        toggle("Emojis","Apple artwork in standard colors",()->c.emoji.enabled,v->c.emoji.enabled=v,true);
        toggle("Autocomplete","Favorite emojis appear first",()->c.emoji.autocomplete,v->c.emoji.autocomplete=v,true);
        number("Emoji size","Bitmap glyph size",()->c.emoji.size,v->c.emoji.size=(int)v,6,16,1,9);
        action("Emoji favorites","Search emojis and toggle their stars","Manage emojis",()->minecraft.setScreen(new EmojiSettingsScreen(this)));
        buildingCategory=6;
        action("Reload files","Read local config and emoji definitions","Reload",()->{CokeChatClient.manager().load();CokeChatClient.apply();rebuildWidgets();});
        action("Connections","CokeChat initiates no network traffic","None",()->{});
        action("Runtime","CokeChat 1.5.0 / Fabric / Java 25","MC 26.1.2",()->{});
        buildingCategory=7;var animation=c.animations;
        toggle("Chat open animation","Expand the panel to your configured size",()->animation.open,v->animation.open=v,true);
        choice("Open distance preset","Choose a percentage; Custom uses the slider below",()->preset(animation.openDistance,10,25,50,75,100),v->{if(!v.equals("Custom"))animation.openDistance=Double.parseDouble(v);},"50",List.of("10","25","50","75","100","Custom"));
        number("Open animation distance","Percent of panel expansion",()->animation.openDistance,v->animation.openDistance=v,0,100,1,50);
        choice("Open duration preset","Milliseconds; Custom uses the slider below",()->preset(animation.openDuration,100,200,300,500,750,1000),v->{if(!v.equals("Custom"))animation.openDuration=Integer.parseInt(v);},"Custom",List.of("100","200","300","500","750","1000","Custom"));
        number("Chat open duration","Milliseconds; 0 means instant",()->animation.openDuration,v->animation.openDuration=(int)v,0,2000,10,250);
        choice("Chat open easing","Ease Out / Cubic / Quartic / Quintic",()->animation.openEasing.toString(),v->animation.openEasing=CokeChatConfig.Easing.valueOf(v),"CUBIC_OUT",List.of("EASE_OUT","CUBIC_OUT","QUARTIC_OUT","QUINTIC_OUT"));
        toggle("Chat open message fade","Fade existing messages when opening",()->animation.openMessageFade,v->animation.openMessageFade=v,true);
        number("Open fade duration","Milliseconds for each message",()->animation.fadeDuration,v->animation.fadeDuration=(int)v,0,2000,10,180);
        number("Open fade delay","Wait before fading the first message",()->animation.fadeDelay,v->animation.fadeDelay=(int)v,0,2000,5,25);
        number("Open fade stagger","Delay between rows; 0 fades together",()->animation.fadeStagger,v->animation.fadeStagger=(int)v,0,200,1,12);
        toggle("Message animation","Smooth new-message entry; groups keep their start time",()->animation.messages,v->animation.messages=v,true);
        number("Message duration","Milliseconds; 0 means instant",()->animation.messageDuration,v->animation.messageDuration=(int)v,0,2000,25,200);
        choice("Message easing","Shape of the entry animation",()->animation.messageEasing.toString(),v->animation.messageEasing=CokeChatConfig.Easing.valueOf(v),"CUBIC_OUT",List.of("CUBIC_OUT","QUARTIC_OUT","EASE_OUT"));
        toggle("Smooth scrolling","Animate toward a scroll target",()->animation.smoothScroll,v->animation.smoothScroll=v,true);
        number("Scroll speed","Lines per wheel step",()->animation.scrollSpeed,v->animation.scrollSpeed=v,.25,30,.25,7);
        number("Scroll duration","Milliseconds to the new scroll position",()->animation.scrollDuration,v->animation.scrollDuration=(int)v,0,2000,25,180);
        choice("Scroll easing","Shared by normal chat and Chat Peek",()->animation.scrollEasing.toString(),v->animation.scrollEasing=CokeChatConfig.Easing.valueOf(v),"CUBIC_OUT",List.of("CUBIC_OUT","QUARTIC_OUT","EASE_OUT"));
        toggle("Slow scroll","Hold Shift for precise scrolling",()->animation.slowScroll,v->animation.slowScroll=v,true);
        action("Slow scroll modifier","In Peek, its own hold key is excluded","Shift",()->{});
        number("Slow multiplier","Fraction of normal scroll distance",()->animation.slowMultiplier,v->animation.slowMultiplier=v,.01,.5,.01,.25);
        number("Slow duration","Milliseconds for precision scrolling",()->animation.slowDuration,v->animation.slowDuration=(int)v,0,2000,25,260);
        buildingCategory=8;var peek=c.peek;
        toggle("Enable Chat Peek","Hold its key to view chat without opening it",()->peek.enabled,v->peek.enabled=v,true);
        action("Chat Peek key","Default Right Shift; change in Minecraft controls","Key bindings",()->minecraft.setScreen(new net.minecraft.client.gui.screens.options.controls.KeyBindsScreen(this,minecraft.options)));
        number("Peek width","Independent overlay width",()->peek.width,v->peek.width=(int)v,100,1200,1,400);
        number("Peek height","Independent overlay height",()->peek.height,v->peek.height=(int)v,40,800,1,220);
        number("Peek position X","Distance from left edge",()->peek.x,v->peek.x=(int)v,0,1200,1,12);
        number("Peek bottom offset","Distance above the bottom chat margin",()->peek.y,v->peek.y=(int)v,0,800,1,24);
        number("Peek opacity","Transparent overlay background",()->peek.opacity,v->peek.opacity=v,0,1,.01,.85);
        action("Peek animation","Shares expansion, duration, easing and message fade with Open Chat","Animation settings",()->{category=7;categoryPage=navigationIndex(category)/categoryRows;page=0;rebuildWidgets();});
        buildingCategory=9;var actions=c.actions;
        toggle("Hover actions","Small icons on the hovered message only",()->actions.enabled,v->actions.enabled=v,true);
        toggle("Copy button","Copy one original message, without duplicate counter",()->actions.copy,v->actions.copy=v,true);
        toggle("Delete button","Hide the whole group locally; raw history remains",()->actions.delete,v->actions.delete=v,true);
        number("Button size","Icon size; capped to the rendered line height",()->actions.size,v->actions.size=(int)v,8,24,1,12);
        number("Button opacity","Transparency of hover icons",()->actions.opacity,v->actions.opacity=v,0,1,.01,.95);
        number("Button spacing","Gap between the icons",()->actions.spacing,v->actions.spacing=(int)v,0,12,1,3);
        toggle("Button animation","Short fade when entering or leaving a message",()->actions.animation,v->actions.animation=v,true);
        number("Tooltip delay","Milliseconds before showing action hints",()->actions.tooltipDelay,v->actions.tooltipDelay=(int)v,0,2000,25,400);
        color("Button background","Independent action background wheel",()->actions.backgroundColor,v->actions.backgroundColor=v,()->actions.backgroundOpacity,v->actions.backgroundOpacity=v,0x1B263B,1);
        color("Button hover color","Independent hover wheel",()->actions.hoverColor,v->actions.hoverColor=v,()->actions.hoverOpacity,v->actions.hoverOpacity=v,0x425879,1);
        color("Copy icon color","Independent copy wheel",()->actions.copyColor,v->actions.copyColor=v,()->actions.copyOpacity,v->actions.copyOpacity=v,0x9AEAF3,1);
        color("Delete icon color","Independent delete wheel",()->actions.deleteColor,v->actions.deleteColor=v,()->actions.deleteOpacity,v->actions.deleteOpacity=v,0xF2A7BF,1);
        buildingCategory=10;var input=c.input;var defaults=new CokeChatConfig().input;
        choice("Input position mode","Relative: from chat bottom; Absolute: from screen top left",()->input.positionMode.toString(),v->input.positionMode=CokeChatConfig.PositionMode.valueOf(v),"RELATIVE_TO_CHAT",List.of("RELATIVE_TO_CHAT","ABSOLUTE_SCREEN"));
        number("Input X","Horizontal offset; = for numeric entry",()->input.x,v->input.x=(int)v,-1200,1200,1,defaults.x);
        number("Input Y","Vertical offset; = for numeric entry",()->input.y,v->input.y=(int)v,-800,800,1,defaults.y);
        number("Input width","Independent input width in pixels",()->input.width,v->input.width=(int)v,60,1200,1,defaults.width);
        number("Input height","Independent input height in pixels",()->input.height,v->input.height=(int)v,14,200,1,defaults.height);
        number("Input font size","Size of typed text and caret",()->input.fontSize,v->input.fontSize=v,5,24,.5,defaults.fontSize);
        number("Input opacity","Input panel transparency",()->a.inputOpacity,v->a.inputOpacity=v,0,1,.01,d.inputOpacity);
        number("Input border radius","Input panel corner rounding",()->a.inputRadius,v->a.inputRadius=(int)v,0,32,1,d.inputRadius);
        color("Input background color","Independent input background wheel",()->a.inputColor,v->a.inputColor=v,()->a.inputOpacity,v->a.inputOpacity=v,d.inputColor,d.inputOpacity);
        color("Input border color","Independent input border wheel",()->input.borderColor,v->input.borderColor=v,()->input.borderOpacity,v->input.borderOpacity=v,defaults.borderColor,defaults.borderOpacity);
        color("Input text color","Independent input text wheel",()->input.textColor,v->input.textColor=v,()->input.textOpacity,v->input.textOpacity=v,defaults.textColor,defaults.textOpacity);
    }
    private static String preset(double value,int... values){for(int n:values)if(value==n)return Integer.toString(n);return "Custom";}
    private void selectCategory(int selected){category=selected;page=0;query="";categoryPage=navigationIndex(category)/Math.max(1,categoryRows);rebuildWidgets();}
    @Override protected void init(){
        controls.clear();buildFields();sidebar=width>=640;contentX=sidebar?166:12;contentWidth=width-contentX-12;
        stacked=contentWidth<410;rowHeight=stacked?44:48;previewY=height>=390?height-116:height-36;
        rows=Math.max(1,(previewY-listTop-28)/rowHeight);
        categoryRows=Math.max(1,(height-78)/24);if(categoryRows<CATEGORIES.length)categoryRows=Math.max(1,(height-104)/24);categoryPage=Math.min(categoryPage,(CATEGORIES.length-1)/categoryRows);
        if(sidebar){
            for(int i=categoryPage*categoryRows;i<Math.min(CATEGORIES.length,(categoryPage+1)*categoryRows);i++){
                int selected=NAVIGATION[i];addRenderableWidget(new StyleButton(CATEGORIES[selected],12,54+(i-categoryPage*categoryRows)*24,140,21,()->selectCategory(selected)).selected(selected==category));
            }
            if(CATEGORIES.length>categoryRows){button("< Cats",12,height-61,66,()->{categoryPage=Math.max(0,categoryPage-1);rebuildWidgets();});button("Cats >",84,height-61,68,()->{categoryPage=Math.min((CATEGORIES.length-1)/categoryRows,categoryPage+1);rebuildWidgets();});}
        }else{
            button(CATEGORIES[category]+" v",12,52,Math.min(140,width/3),()->minecraft.setScreen(new ChoiceScreen(this,"Categories",java.util.Arrays.stream(NAVIGATION).mapToObj(i->CATEGORIES[i]).toList(),v->{category=List.of(CATEGORIES).indexOf(v);page=0;query="";})));
        }
        int searchX=sidebar?contentX:20+Math.min(140,width/3);
        var search=UiTheme.edit(searchX,52,width-searchX-12,"Search all settings");search.setHint(Component.literal("Search settings..."));search.setMaxLength(80);search.setValue(query);
        search.setResponder(v->{query=v;page=0;refresh();});addRenderableWidget(search);
        previous=button("<",contentX,previewY-28,28,()->{page--;refresh();});next=button(">",contentX+34,previewY-28,28,()->{page++;refresh();});
        button("Full preview",width-190,height-28,106,()->minecraft.setScreen(new PreviewScreen(this)));
        addRenderableWidget(new StyleButton("Done",width-76,height-28,64,20,this::onClose).primary());refresh();
    }
    private void refresh(){
        var focused=getFocused() instanceof AbstractWidget widget?widget:null;boolean restore=controls.contains(focused);
        for(var widget:controls)removeWidget(widget);controls.clear();visible.clear();fieldError="";
        String needle=query.toLowerCase(Locale.ROOT).strip();
        for(var f:fields)if(needle.isEmpty()?f.category==category:(CATEGORIES[f.category]+" "+f.name+" "+f.description).toLowerCase(Locale.ROOT).contains(needle))visible.add(f);
        int pages=Math.max(1,(visible.size()+rows-1)/rows);page=Math.max(0,Math.min(page,pages-1));previous.active=page>0;next.active=page+1<pages;
        for(int i=page*rows;i<Math.min(visible.size(),(page+1)*rows);i++){
            var f=visible.get(i);int y=listTop+(i-page*rows)*rowHeight;
            if(!needle.isEmpty()){
                var b=button(CATEGORIES[f.category]+" / "+f.name,contentX+8,y+4,contentWidth-16,()->{category=f.category;categoryPage=navigationIndex(category)/categoryRows;query="";long index=fields.stream().filter(other->other.category==category).takeWhile(other->other!=f).count();page=(int)index/rows;rebuildWidgets();});
                UiTheme.hint(b,f.description);controls.add(b);continue;
            }
            int controlWidth=stacked?Math.min(240,contentWidth-16):Math.min(180,contentWidth/2);
            int x=stacked?contentX+8:contentX+contentWidth-controlWidth-8;int cy=y+(stacked?17:14);int w=controlWidth-(f.reset==null?0:28);
            AbstractWidget widget;
            if(f.kind==Kind.NUMBER){
                widget=new SettingSlider(x,cy,w-26,Double.parseDouble(f.value.get()),f.min,f.max,f.step,v->{f.set.accept(SettingSlider.format(v));changed();});
                var exact=button("=",x+w-22,cy,22,()->minecraft.setScreen(new ValueScreen(this,f.name+" ("+SettingSlider.format(f.min)+"–"+SettingSlider.format(f.max)+")",f.value,f.set,false)));
                UiTheme.hint(exact,"Enter an exact value for "+f.name);controls.add(exact);addRenderableWidget(widget);
            }else if(f.kind==Kind.TEXT){
                var box=UiTheme.edit(x,cy,w,f.name);box.setMaxLength(32);box.setValue(f.value.get());box.setResponder(v->{try{f.set.accept(v);changed();box.setTextColor(UiTheme.TEXT);fieldError="";}catch(IllegalArgumentException e){box.setTextColor(UiTheme.ERROR);fieldError=f.name+": include {count}; maximum 32 characters";UiTheme.hint(box,fieldError);}});widget=addRenderableWidget(box);
            }else{
                widget=button(f.value.get()+(f.kind==Kind.CHOICE?" v":""),x,cy,w,()->{
                    switch(f.kind){
                        case ACTION,COLOR -> f.action.run();
                        case TOGGLE -> {f.set.accept(f.value.get().equals("On")?"Off":"On");changed();buildFields();refresh();}
                        case CHOICE -> minecraft.setScreen(new ChoiceScreen(this,f.name,f.choices,v->{f.set.accept(v);changed();}));
                        default -> {}
                    }
                });
            }
            UiTheme.hint(widget,f.name+": "+f.description);controls.add(widget);
            if(f.reset!=null){var reset=button("R",x+controlWidth-22,cy,22,()->{f.reset.run();changed();buildFields();refresh();});UiTheme.hint(reset,"Reset "+f.name+" to default");controls.add(reset);}
        }
        if(restore)for(var widget:controls)if(widget.getX()==focused.getX()&&widget.getY()==focused.getY()){setFocused(widget);break;}
    }
    @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float delta){
        UiTheme.shell(g,width,height,"CokeChat","Make chat feel like yours");
        if(sidebar)UiTheme.card(g,8,49,148,height-89);
        UiTheme.text(g,query.isBlank()?CATEGORIES[category]:"Search results · "+visible.size(),contentX,77,contentWidth,UiTheme.ACCENT);
        for(int i=page*rows;i<Math.min(visible.size(),(page+1)*rows);i++){
            var f=visible.get(i);int y=listTop+(i-page*rows)*rowHeight;UiTheme.card(g,contentX,y,contentWidth,rowHeight-6);
            int labelWidth=stacked?contentWidth-16:contentWidth-Math.min(180,contentWidth/2)-24;
            if(query.isBlank()){
                UiTheme.text(g,f.name,contentX+8,y+(stacked?4:6),labelWidth,UiTheme.TEXT);
                if(!stacked)UiTheme.text(g,f.description,contentX+8,y+19,labelWidth,UiTheme.MUTED);
                if(mx>=contentX&&mx<contentX+labelWidth&&my>=y&&my<y+30)g.setTooltipForNextFrame(Component.literal(f.name+": "+f.description),mx,my);
            }else UiTheme.text(g,f.description,contentX+8,y+29,contentWidth-16,UiTheme.MUTED);
        }
        if(visible.isEmpty())UiTheme.lines(g,"No matching settings. Try a shorter name or another category.",contentX+8,listTop+12,contentWidth-16,3,UiTheme.MUTED);
        UiTheme.text(g,(page+1)+" / "+Math.max(1,(visible.size()+rows-1)/rows),contentX+72,previewY-22,contentWidth-80,UiTheme.MUTED);
        if(previewY<height-36){UiTheme.text(g,"LIVE PREVIEW",contentX,previewY+2,contentWidth,UiTheme.MUTED);drawPreview(g,contentX,previewY+17,contentWidth,54);}
        UiTheme.text(g,!fieldError.isEmpty()?fieldError:status.isEmpty()?"Saved locally":status,12,height-22,Math.max(70,width-214),!fieldError.isEmpty()||!status.isEmpty()?UiTheme.ERROR:UiTheme.MUTED);
        super.extractRenderState(g,mx,my,delta);
    }
    private static final PreviewChat SAMPLE=new PreviewChat();
    public static void drawPreview(GuiGraphicsExtractor g,int x,int y,int w,int h){
        var mc=net.minecraft.client.Minecraft.getInstance();var c=CokeChatClient.config();int sw=mc.getWindow().getGuiScaledWidth(),sh=mc.getWindow().getGuiScaledHeight();
        double sx=w/(double)sw,sy=h/(double)sh;int chatX=CokeChatClient.offsetX(),bottom=sh-40-CokeChatClient.offsetY();
        int cw=Math.min(c.appearance.width,sw-chatX-12),ch=Math.min(c.appearance.height,bottom);
        g.fill(x,y,x+w,y+h,0xFF263349);g.enableScissor(x,y,x+w,y+h);
        SAMPLE.draw(g,x+(int)(chatX*sx),y+(int)((bottom-ch)*sy),Math.max(20,(int)(cw*sx)),Math.max(10,(int)(ch*sy)),true);
        var box=de.cokechat.rendering.ChatInputLayout.of(c,sw,sh,chatX,bottom);
        new de.cokechat.rendering.ChatInputLayout(x+(int)(box.x()*sx),y+(int)(box.y()*sy),Math.max(5,(int)(box.width()*sx)),Math.max(3,(int)(box.height()*sy))).draw(g,c);g.disableScissor();
    }
    @Override public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent event,boolean doubleClick){if(event.button()==0&&SAMPLE.click(event.x(),event.y()))return true;return super.mouseClicked(event,doubleClick);}
    @Override public boolean mouseScrolled(double x,double y,double horizontal,double vertical){if(y>=listTop&&y<previewY-30&&x>=contentX){page+=vertical<0?1:-1;refresh();return true;}return SAMPLE.wheel(x,y,vertical)||super.mouseScrolled(x,y,horizontal,vertical);}
    @Override public void onClose(){changed();minecraft.setScreen(parent);}
    @Override public boolean isPauseScreen(){return false;}
}

