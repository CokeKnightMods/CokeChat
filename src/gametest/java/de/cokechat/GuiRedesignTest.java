package de.cokechat;
import de.cokechat.gui.*;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.client.gui.components.*;
import net.minecraft.network.chat.Component;
import java.util.*;
final class GuiRedesignTest {
    static void category(ClientGameTestContext c,String name){
        String button=c.computeOnClient(client->client.screen.children().stream().filter(Button.class::isInstance).map(Button.class::cast).filter(b->b.getY()==52&&b.getMessage().getString().endsWith(" v")).map(b->b.getMessage().getString()).findFirst().orElse(null));
        if(button!=null){c.clickScreenButton(button);c.runOnClient(client->client.screen.children().stream().filter(EditBox.class::isInstance).map(EditBox.class::cast).findFirst().orElseThrow().setValue(name));}
        else {boolean paged=c.computeOnClient(client->client.screen.children().stream().filter(Button.class::isInstance).map(Button.class::cast).anyMatch(b->b.getMessage().getString().equals("< Cats")));if(paged)for(int i=0;i<3;i++)c.clickScreenButton("< Cats");for(int i=0;i<4;i++){boolean found=c.computeOnClient(client->client.screen.children().stream().filter(Button.class::isInstance).map(Button.class::cast).anyMatch(b->b.getMessage().getString().equals(name)));if(found)break;c.clickScreenButton("Cats >");}}
        c.clickScreenButton(name);
    }
    private static void bounds(ClientGameTestContext c){c.runOnClient(client->{
        var screen=client.screen;var widgets=screen.children().stream().filter(AbstractWidget.class::isInstance).map(AbstractWidget.class::cast).filter(w->w.visible).toList();
        for(var w:widgets){if(w.getX()<0||w.getY()<0||w.getRight()>screen.width||w.getBottom()>screen.height||w.getWidth()<16)throw new AssertionError("Out of bounds "+screen.getClass()+" "+w.getMessage().getString()+" at "+w.getX()+","+w.getY()+" "+w.getWidth()+"x"+w.getHeight()+" screen="+screen.width+"x"+screen.height);}
        for(int i=0;i<widgets.size();i++)for(int j=i+1;j<widgets.size();j++){var a=widgets.get(i);var b=widgets.get(j);if(a.getX()<b.getRight()&&a.getRight()>b.getX()&&a.getY()<b.getBottom()&&a.getBottom()>b.getY())throw new AssertionError("Overlapping controls "+a.getMessage().getString()+" / "+b.getMessage().getString()+" in "+screen.getClass());}
    });}
    static void run(ClientGameTestContext c){
        int[] original=c.computeOnClient(client->new int[]{client.getWindow().getWidth(),client.getWindow().getHeight(),client.options.guiScale().get()});
        try{
            int[][] cases={{854,480,2},{640,480,2},{1280,720,2},{1920,1080,3},{1024,768,1},{1280,480,2}};
            for(int i=0;i<cases.length;i++){var size=cases[i];c.runOnClient(client->{client.getWindow().setWindowed(size[0],size[1]);client.options.guiScale().set(size[2]);client.resizeGui();});c.waitTicks(3);
                c.setScreen(()->new CokeChatSettingsScreen(null));bounds(c);c.takeScreenshot("gui-settings-"+i);
                for(String name:List.of("Appearance","Compact Chat","History","Visual Words","Emojis","Advanced","Chat Animation","Chat Peek","Message Actions","Chat Input")){category(c,name);bounds(c);for(int page=0;page<25;page++){boolean next=c.computeOnClient(client->client.screen.children().stream().filter(Button.class::isInstance).map(Button.class::cast).anyMatch(b->b.active&&b.getMessage().getString().equals(">")));if(!next)break;c.clickScreenButton(">");bounds(c);}}
                c.setScreen(()->new VisualWordsScreen(null));bounds(c);c.takeScreenshot("gui-words-"+i);
                c.setScreen(()->new EmojiEditorScreen(null,null));bounds(c);c.clickScreenButton("Bitmap font");bounds(c);c.clickScreenButton("Identity");c.clickScreenButton("Save");bounds(c);c.takeScreenshot("gui-emoji-error-"+i);
                c.setScreen(()->new EmojiSettingsScreen(null));bounds(c);c.runOnClient(client->client.screen.children().stream().filter(EditBox.class::isInstance).map(EditBox.class::cast).findFirst().orElseThrow().setValue("no_such_emoji_12345"));bounds(c);c.takeScreenshot("gui-empty-library-"+i);
                c.setScreen(()->{var a=CokeChatClient.config().appearance;return new ColorPickerScreen(null,"A deliberately long translated color setting name",()->a.backgroundColor,v->a.backgroundColor=v,()->a.backgroundOpacity,v->a.backgroundOpacity=v,0x171C29,.85);});bounds(c);c.takeScreenshot("gui-color-"+i);c.clickScreenButton("Cancel");
                c.setScreen(()->new ChoiceScreen(null,"A long list of options",java.util.stream.IntStream.range(0,30).mapToObj(n->"Option "+n+" with an intentionally long translated description").toList(),v->{}));bounds(c);c.takeScreenshot("gui-choice-"+i);
                c.setScreen(()->new ValueScreen(null,"Width (80–1200)",()->"400",v->{int number=Integer.parseInt(v);if(number<80||number>1200)throw new IllegalArgumentException();},false));bounds(c);
                c.setScreen(()->new ConfirmActionScreen(null,"Clear history?","This permanently deletes the current local history. Cancel keeps it unchanged.",()->{throw new AssertionError("Confirmation executed without approval");}));bounds(c);c.getInput().pressKey(256);
                c.setScreen(()->new PreviewScreen(null));c.waitTicks(10);bounds(c);c.takeScreenshot("gui-preview-"+i);
            }
            c.setScreen(()->new VisualWordsScreen(null));int count=CokeChatClient.config().visualWords.rules.size();c.clickScreenButton("Delete");c.clickScreenButton("Cancel");c.runOnClient(client->{if(CokeChatClient.config().visualWords.rules.size()!=count)throw new AssertionError("Cancelled deletion changed rules");});
            c.clickScreenButton("New");c.clickScreenButton("Up");c.clickScreenButton("Down");c.clickScreenButton("Delete");c.clickScreenButton("Confirm");
            c.runOnClient(client->{if(CokeChatClient.config().visualWords.rules.size()!=count)throw new AssertionError("Rule removal failed");});
            c.setScreen(()->new EmojiEditorScreen(null,null));
            c.runOnClient(client->{var boxes=client.screen.children().stream().filter(EditBox.class::isInstance).map(EditBox.class::cast).toList();boxes.get(0).setValue("gui_test_emoji");boxes.get(2).setValue("★");});
            c.clickScreenButton("Save");c.runOnClient(client->{if(CokeChatClient.emojis().find("gui_test_emoji")==null)throw new AssertionError("Emoji save failed");});
            c.setScreen(()->new EmojiEditorScreen(null,CokeChatClient.emojis().find("gui_test_emoji")));c.clickScreenButton("Remove");c.clickScreenButton("Cancel");
            c.runOnClient(client->{if(CokeChatClient.emojis().find("gui_test_emoji")==null)throw new AssertionError("Cancelled removal lost emoji");});c.clickScreenButton("Remove");c.clickScreenButton("Confirm");
            c.runOnClient(client->{if(CokeChatClient.emojis().find("gui_test_emoji")!=null)throw new AssertionError("Emoji removal failed");});
            var chosen=new java.util.concurrent.atomic.AtomicBoolean();
            c.setScreen(()->new ChoiceScreen(null,"Keyboard choice",List.of("Keyboard result"),v->chosen.set(v.equals("Keyboard result"))));c.getInput().pressKey(258);c.getInput().pressKey(258);c.getInput().pressKey(257);
            if(!chosen.get())throw new AssertionError("Keyboard navigation failed");
            c.setScreen(()->new PreviewScreen(null));c.waitTicks(3);c.getInput().pressKey(266);c.clickScreenButton("Copy");c.clickScreenButton("Hide");c.clickScreenButton("Reset sample");
        }finally{c.runOnClient(client->{client.setScreen(null);client.getWindow().setWindowed(original[0],original[1]);client.options.guiScale().set(original[2]);client.resizeGui();});}
    }
}
