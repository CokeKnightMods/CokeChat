package de.cokechat.config;

import de.cokechat.visualwords.VisualWordRule;
import java.util.ArrayList;
import java.util.List;

public final class CokeChatConfig {
    public boolean enabled = true;
    public Appearance appearance = new Appearance();
    public Compact compact = new Compact();
    public History history = new History();
    public VisualWords visualWords = new VisualWords();
    public Emoji emoji = new Emoji();
    public Animations animations = new Animations();
    public Peek peek = new Peek();
    public Actions actions = new Actions();
    public Input input = new Input();
    public enum Easing { CUBIC_OUT, QUARTIC_OUT, QUINTIC_OUT, EASE_OUT }
    public enum PositionMode { RELATIVE_TO_CHAT, ABSOLUTE_SCREEN }
    public static final class Input {
        public PositionMode positionMode=PositionMode.RELATIVE_TO_CHAT;
        public int x=0,y=12,width=380,height=22;
        public double fontSize=9;
        public int borderColor=0x657A9D,textColor=0xEAF0FF;
        public double borderOpacity=.45,textOpacity=1;
    }
    public static final class Animations {
        public boolean open=true,openMessageFade=true;
        public double openDistance=50;
        public int openDuration=250,fadeDuration=180,fadeDelay=25,fadeStagger=12;
        public Easing openEasing=Easing.CUBIC_OUT;
        public boolean messages=true, smoothScroll=true, slowScroll=true;
        public int messageDuration=200, scrollDuration=180, slowDuration=260;
        public double scrollSpeed=7, slowMultiplier=.25;
        public Easing messageEasing=Easing.CUBIC_OUT, scrollEasing=Easing.CUBIC_OUT;
    }
    public static final class Peek {
        public boolean enabled=true;
        public int width=400,height=220,x=12,y=24,duration=180;
        public double opacity=.85;
    }
    public static final class Actions {
        public int backgroundColor=0x1B263B,hoverColor=0x425879,copyColor=0x9AEAF3,deleteColor=0xF2A7BF;
        public double backgroundOpacity=1,hoverOpacity=1,copyOpacity=1,deleteOpacity=1;
        public boolean enabled=true,copy=true,delete=true,animation=true;
        public int size=12,spacing=3,tooltipDelay=400;
        public double opacity=.95;
    }
    public static final class Appearance {
        public boolean chatBorder=true,inputBorder=true;
        public int borderColor=0x657A9D,textColor=0xEAF0FF,scrollbarColor=0xB2BCD7;
        public double borderOpacity=.25,textOpacity=1;
        public int width = 340, height = 180, x = 8, y = 0, cornerRadius = 6, messageSpacing = 2;
        public double fontSize = 9, backgroundOpacity = .65, inputOpacity = .85, scrollbarOpacity = .8;
        public int backgroundColor = 0x171C29, inputColor = 0x171C29, inputRadius = 6, scrollbarWidth = 3;
        public boolean scrollbar = true;
    }
    public static final class Compact {
        public boolean enabled = false, showCounter = true;
        public double groupingSeconds = 2;
        public String counterFormat = "×{count}";
    }
    public static final class History { public boolean enabled = true, persistent = false; public int maxMessages = 1000; }
    public static final class VisualWords {
        public boolean enabled = true;
        public List<VisualWordRule> rules = new ArrayList<>(List.of(new VisualWordRule(true, "gg", "Good Game", false, true), new VisualWordRule(true, "pog", ":fire:", false, true)));
    }
    public static final class Emoji { public boolean enabled = true, autocomplete = true; public int size = 9; public List<String> favorites = new ArrayList<>(); }
    public void validate() {
        if (appearance == null) appearance = new Appearance();
        if (compact == null) compact = new Compact();
        if (history == null) history = new History();
        if (visualWords == null) visualWords = new VisualWords();
        if (emoji == null) emoji = new Emoji();
        if(animations==null)animations=new Animations();if(peek==null)peek=new Peek();if(actions==null)actions=new Actions();
        if(input==null)input=new Input();
        if(input.positionMode==null)input.positionMode=PositionMode.RELATIVE_TO_CHAT;
        input.x=clamp(input.x,-1200,1200);input.y=clamp(input.y,-800,800);
        input.width=clamp(input.width,60,1200);input.height=clamp(input.height,14,200);input.fontSize=finite(input.fontSize,5,24,9);
        input.borderColor&=0xFFFFFF;input.textColor&=0xFFFFFF;input.borderOpacity=finite(input.borderOpacity,0,1,.45);input.textOpacity=finite(input.textOpacity,0,1,1);
        animations.openDistance=finite(animations.openDistance,0,100,50);animations.openDuration=clamp(animations.openDuration,0,2000);
        animations.fadeDuration=clamp(animations.fadeDuration,0,2000);animations.fadeDelay=clamp(animations.fadeDelay,0,2000);animations.fadeStagger=clamp(animations.fadeStagger,0,200);
        if(animations.openEasing==null)animations.openEasing=Easing.CUBIC_OUT;
        actions.backgroundColor&=0xFFFFFF;actions.hoverColor&=0xFFFFFF;actions.copyColor&=0xFFFFFF;actions.deleteColor&=0xFFFFFF;
        actions.backgroundOpacity=finite(actions.backgroundOpacity,0,1,1);actions.hoverOpacity=finite(actions.hoverOpacity,0,1,1);actions.copyOpacity=finite(actions.copyOpacity,0,1,1);actions.deleteOpacity=finite(actions.deleteOpacity,0,1,1);
        animations.messageDuration=clamp(animations.messageDuration,0,2000);animations.scrollDuration=clamp(animations.scrollDuration,0,2000);animations.slowDuration=clamp(animations.slowDuration,0,2000);
        animations.scrollSpeed=finite(animations.scrollSpeed,.25,30,7);animations.slowMultiplier=finite(animations.slowMultiplier,.01,.5,.25);
        if(animations.messageEasing==null)animations.messageEasing=Easing.CUBIC_OUT;if(animations.scrollEasing==null)animations.scrollEasing=Easing.CUBIC_OUT;
        peek.width=clamp(peek.width,100,1200);peek.height=clamp(peek.height,40,800);peek.x=clamp(peek.x,0,1200);peek.y=clamp(peek.y,0,800);peek.duration=clamp(peek.duration,0,2000);peek.opacity=finite(peek.opacity,0,1,.85);
        actions.size=clamp(actions.size,8,24);actions.spacing=clamp(actions.spacing,0,12);actions.tooltipDelay=clamp(actions.tooltipDelay,0,2000);actions.opacity=finite(actions.opacity,0,1,.95);
        var a = appearance;
        a.borderColor&=0xFFFFFF;a.textColor&=0xFFFFFF;a.scrollbarColor&=0xFFFFFF;
        a.borderOpacity=finite(a.borderOpacity,0,1,.25);a.textOpacity=finite(a.textOpacity,0,1,1);
        a.width = clamp(a.width, 80, 1200); a.height = clamp(a.height, 30, 800);
        a.x = clamp(a.x, 0, 1200); a.y = clamp(a.y, 0, 800);
        a.fontSize = finite(a.fontSize, 5, 24, 9); a.cornerRadius = clamp(a.cornerRadius, 0, 32);
        a.messageSpacing = clamp(a.messageSpacing, 0, 12); a.inputRadius = clamp(a.inputRadius, 0, 32);
        a.backgroundOpacity = finite(a.backgroundOpacity, 0, 1, .65); a.inputOpacity = finite(a.inputOpacity, 0, 1, .85);
        a.scrollbarOpacity = finite(a.scrollbarOpacity, 0, 1, .8); a.scrollbarWidth = clamp(a.scrollbarWidth, 1, 8);
        a.backgroundColor &= 0xFFFFFF; a.inputColor &= 0xFFFFFF;
        compact.groupingSeconds = finite(compact.groupingSeconds, .5, 300, 2);
        if (compact.counterFormat == null || !compact.counterFormat.contains("{count}") || compact.counterFormat.length()>32 || compact.counterFormat.contains("\n")) compact.counterFormat = "×{count}";
        if (history.maxMessages != -1) history.maxMessages = clamp(history.maxMessages, 100, 100000);
        if (visualWords.rules == null) visualWords.rules = new ArrayList<>();
        visualWords.rules.removeIf(r -> r == null || r.searchText == null || r.searchText.isEmpty() || r.replacementText == null);
        if (visualWords.rules.size() > 256) visualWords.rules = new ArrayList<>(visualWords.rules.subList(0, 256));
        emoji.size = clamp(emoji.size, 6, 16);
        if (emoji.favorites == null) emoji.favorites = new ArrayList<>();
        emoji.favorites = new ArrayList<>(emoji.favorites.stream().filter(s -> s != null && s.matches("[a-z0-9_+-]{1,64}")).distinct().limit(2048).toList());
    }
    public static int clamp(int v, int min, int max) { return Math.max(min, Math.min(max, v)); }
    private static double finite(double v, double min, double max, double fallback) { return Double.isFinite(v) ? Math.max(min, Math.min(max, v)) : fallback; }
}
