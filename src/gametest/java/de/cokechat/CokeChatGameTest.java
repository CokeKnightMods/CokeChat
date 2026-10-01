package de.cokechat;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import de.cokechat.gui.*;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.multiplayer.chat.GuiMessage;
import java.util.List;
public final class CokeChatGameTest implements FabricClientGameTest {
    @SuppressWarnings("unchecked") private static List<GuiMessage> messages(ChatComponent chat) {
        try {var field=ChatComponent.class.getDeclaredField("allMessages");field.setAccessible(true);return (List<GuiMessage>)field.get(chat);}catch(Exception e){throw new AssertionError(e);}
    }
    private static EditBox input(ChatScreen screen) {
        try {var field=ChatScreen.class.getDeclaredField("input");field.setAccessible(true);return (EditBox)field.get(screen);}catch(Exception e){throw new AssertionError(e);}
    }
    @SuppressWarnings("unchecked") private static List<GuiMessage.Line> displayed(ChatComponent chat) {
        try {var field=ChatComponent.class.getDeclaredField("trimmedMessages");field.setAccessible(true);return (List<GuiMessage.Line>)field.get(chat);}catch(Exception e){throw new AssertionError(e);}
    }
    private static String visibleText(ChatComponent chat) {
        var text=new StringBuilder();for(var line:displayed(chat))line.content().accept((i,s,cp)->{text.appendCodePoint(cp);return true;});return text.toString();
    }
    @Override public void runTest(ClientGameTestContext context) {
        context.runOnClient(client->{
            var c=CokeChatClient.config();var defaults=new de.cokechat.config.CokeChatConfig();
            c.enabled=true;c.appearance=defaults.appearance;c.compact=defaults.compact;c.history=defaults.history;c.visualWords=defaults.visualWords;c.emoji=defaults.emoji;c.animations=defaults.animations;c.peek=defaults.peek;c.actions=defaults.actions;c.input=defaults.input;CokeChatClient.applySettings();
        });
        var reload=context.computeOnClient(client->{client.getResourcePackRepository().reload();client.getResourcePackRepository().setSelected(java.util.List.of("vanilla","file/CokeChat-Custom"));return client.reloadResourcePacks();});
        context.waitFor(client->reload.isDone()&&client.getOverlay()==null);
        context.runOnClient(client->{
            try{de.cokechat.config.ConfigManager.atomicWrite(CokeChatClient.customEmojiFile(),"[{\"id\":\"badge\",\"aliases\":[],\"unicode\":\"heart\",\"font\":\"cokechat_custom:emoji\",\"glyph\":\"\\ue100\",\"width\":16,\"height\":16}]");}catch(Exception e){throw new AssertionError(e);}
            CokeChatClient.apply();
            if(!CokeChatClient.text().process(Component.literal(":badge:")).getString().equals("\ue100"))throw new AssertionError("Custom local bitmap font did not load");
            var missing=new de.cokechat.emoji.EmojiDefinition("missing",List.of(),"fallback","absent:font","\ue101",16,16);
            if(!CokeChatClient.text().renderEmoji(missing,":missing:",net.minecraft.network.chat.Style.EMPTY).getString().equals("fallback"))throw new AssertionError("Missing texture fallback failed");
        });
        GuiRedesignTest.run(context);
        context.setScreen(()->new EmojiEditorScreen(null,CokeChatClient.emojis().find("badge")));context.waitTicks(2);context.takeScreenshot("cokechat-custom-editor");
        context.setScreen(()->new EmojiSettingsScreen(null));context.waitTicks(2);context.takeScreenshot("cokechat-full-library");
        context.runOnClient(client->{
            client.screen.children().stream().filter(EditBox.class::isInstance).map(EditBox.class::cast).findFirst().orElseThrow().setValue("orca");
            for(var d:CokeChatClient.emojis().definitions())if(d.font()!=null) {
                var display=CokeChatClient.text().renderEmoji(d,":"+d.id()+":",net.minecraft.network.chat.Style.EMPTY);
                if(!display.getString().equals(d.glyph()))throw new AssertionError("Missing bitmap font: "+d.id());
                if(client.font.width(display)<=0)throw new AssertionError("Invalid glyph width: "+d.id());
            }
            if(!CokeChatClient.text().process(Component.literal("👍🏻👍🏿")).getString().equals(CokeChatClient.text().process(Component.literal("👍👍")).getString()))throw new AssertionError("Skin modifiers did not resolve to standard colors");
        });
        context.waitTicks(2);context.takeScreenshot("cokechat-emoji-search");
        context.clickScreenButton("☆");context.waitTicks(1);
        context.runOnClient(client->{if(!CokeChatClient.config().emoji.favorites.contains("orca"))throw new AssertionError("Favorite star did not add emoji");});
        context.clickScreenButton("All emojis");context.waitTicks(2);context.takeScreenshot("cokechat-favorites");
        context.clickScreenButton("★");context.waitTicks(1);
        context.runOnClient(client->{if(CokeChatClient.config().emoji.favorites.contains("orca"))throw new AssertionError("Favorite star did not remove emoji");});
        context.setScreen(()->new CokeChatSettingsScreen(null));context.waitTicks(3);context.takeScreenshot("cokechat-settings");
        GuiRedesignTest.category(context,"Appearance");context.waitTicks(2);context.takeScreenshot("cokechat-appearance");
        GuiRedesignTest.category(context,"Compact Chat");context.waitTicks(2);context.takeScreenshot("cokechat-compact-settings");
        context.runOnClient(client->{client.screen.children().stream().filter(EditBox.class::isInstance).map(EditBox.class::cast).findFirst().orElseThrow().setValue("counter style");});
        context.waitTicks(2);context.takeScreenshot("cokechat-settings-search");
        context.clickScreenButton("Compact Chat / Counter style");context.waitTicks(2);context.takeScreenshot("cokechat-counter-settings");
        context.clickScreenButton("×{count} v");context.waitTicks(1);context.takeScreenshot("cokechat-counter-dropdown");context.clickScreenButton("[{count}]");
        context.runOnClient(client->{CokeChatClient.config().compact.counterFormat="×{count}";CokeChatClient.applySettings();});
        context.setScreen(()->new CokeChatSettingsScreen(null));
        context.runOnClient(client->{client.screen.children().stream().filter(EditBox.class::isInstance).map(EditBox.class::cast).findFirst().orElseThrow().setValue("background color");});
        context.clickScreenButton("Appearance / Background color");context.clickScreenButton("#171C29");context.waitTicks(2);context.takeScreenshot("cokechat-color-picker");
        context.runOnClient(client->{var hex=client.screen.children().stream().filter(EditBox.class::isInstance).map(EditBox.class::cast).findFirst().orElseThrow();hex.setValue("#FF0000");hex.insertText("80");if(!hex.getValue().equals("#FF000080")||Math.abs(CokeChatClient.config().appearance.backgroundOpacity-128/255.0)>.001)throw new AssertionError("Typing RGBA was interrupted by live synchronization");if(CokeChatClient.config().appearance.backgroundColor!=0xFF0000||CokeChatClient.config().appearance.inputColor!=0x171C29)throw new AssertionError("Color picker changed wrong target");});
        context.clickScreenButton("Cancel");context.runOnClient(client->{if(CokeChatClient.config().appearance.backgroundColor!=0x171C29)throw new AssertionError("Color cancel did not restore snapshot");});
        context.setScreen(()->{var c=CokeChatClient.config();return new ColorPickerScreen(null,"Input background color",()->c.appearance.inputColor,v->c.appearance.inputColor=v,()->c.appearance.inputOpacity,v->c.appearance.inputOpacity=v,0x171C29,.85);});
        context.runOnClient(client->{client.screen.children().stream().filter(EditBox.class::isInstance).map(EditBox.class::cast).findFirst().orElseThrow().setValue("#335577CC");});context.clickScreenButton("Apply");
        context.runOnClient(client->{var c=CokeChatClient.config();if(c.appearance.inputColor!=0x335577||c.appearance.backgroundColor!=0x171C29)throw new AssertionError("Independent input color apply failed");c.appearance.inputColor=0x171C29;c.appearance.inputOpacity=.85;CokeChatClient.applySettings();});
        context.setScreen(()->new VisualWordsScreen(null));context.waitTicks(2);context.takeScreenshot("cokechat-rules");
        context.setScreen(()->new PreviewScreen(null));context.waitTicks(3);context.takeScreenshot("cokechat-preview");context.setScreen(()->null);
        try(var world=context.worldBuilder().create()){
            world.getClientLevel().waitForChunksRender();
            context.runOnClient(client->{
                var chat=client.gui.getChat();var c=CokeChatClient.config();CokeChatClient.clearHistory();c.compact.enabled=true;CokeChatClient.applySettings();
                for(int i=0;i<3;i++)chat.addClientSystemMessage(Component.literal("CokeKnight: duplicate"));
                if(messages(chat).size()!=3||!visibleText(chat).contains("×3"))throw new AssertionError("Three-message group failed: "+visibleText(chat));
                for(int i=3;i<10;i++)chat.addClientSystemMessage(Component.literal("CokeKnight: duplicate"));
                if(messages(chat).size()!=10||!visibleText(chat).contains("×10"))throw new AssertionError("Ten-message group failed");
                for(var raw:messages(chat))if(!raw.content().getString().equals("CokeKnight: duplicate"))throw new AssertionError("Raw duplicate changed");
                chat.addClientSystemMessage(Component.literal("Clashbad: interruption"));chat.addClientSystemMessage(Component.literal("CokeKnight: duplicate"));
                if(displayed(chat).size()!=2||!visibleText(chat).contains("×11")||!displayed(chat).getFirst().parent().content().getString().equals("CokeKnight: duplicate"))throw new AssertionError("Intervening duplicate did not move to latest position");
                c.compact.counterFormat="[{count}]";CokeChatClient.applySettings();if(!visibleText(chat).contains("[11]")||displayed(chat).size()!=2)throw new AssertionError("Rebuilt nonconsecutive group changed");
                client.setScreen(new ChatScreen("",false));
            });
            context.waitTicks(2);context.takeScreenshot("cokechat-duplicate-groups");
            context.runOnClient(client->{
                var chat=client.gui.getChat();var c=CokeChatClient.config();c.compact.enabled=false;CokeChatClient.applySettings();
                if(displayed(chat).size()!=12)throw new AssertionError("Disabling grouping did not restore originals");
                CokeChatClient.clearHistory();c.compact.enabled=true;c.history.maxMessages=100;CokeChatClient.applySettings();
                for(int i=0;i<105;i++)chat.addClientSystemMessage(Component.literal("same"));
                if(messages(chat).size()!=100||!visibleText(chat).contains("[100]"))throw new AssertionError("Duplicate count ignored raw history eviction: "+visibleText(chat));
                c.compact.enabled=false;c.history.maxMessages=1000;
                CokeChatClient.clearHistory();CokeChatClient.applySettings();
                chat.addClientSystemMessage(Component.literal("Unrelated lobby chat"));chat.addClientSystemMessage(Component.literal("Party > Clashbad: focus :skull:"));
                chat.addClientSystemMessage(Component.literal("[BOSS] Necron: You went further than any human before"));
                chat.addClientSystemMessage(Component.literal("[NPC] Elle: Cover me!"));
                if(messages(chat).size()!=4||!visibleText(chat).contains("Unrelated")||!visibleText(chat).contains("Elle")||!visibleText(chat).contains("Necron")||!visibleText(chat).contains("Party >"))throw new AssertionError("Messages were unexpectedly filtered");
            });
            context.waitTicks(2);context.takeScreenshot("cokechat-unfiltered-chat");
            context.runOnClient(client->{
                var c=CokeChatClient.config();
                c.compact.counterFormat="×{count}";CokeChatClient.clearHistory();
            });
            context.runOnClient(client->{
                var original=Component.literal("Alex: gg :sob: :wilted_rose: :fire: :skull:");
                String before=original.getString();var transformed=CokeChatClient.text().process(original);
                if(!original.getString().equals(before)||!transformed.getString().contains("Good Game"))throw new AssertionError("Display processing changed raw content or failed");
                for(int i=0;i<1200;i++)client.gui.getChat().addClientSystemMessage(Component.literal("Message "+i+" gg :sob: :fire:"));
                client.gui.getChat().addClientSystemMessage(original);
                client.gui.getChat().addClientSystemMessage(Component.literal("Custom local texture :badge:"));
                client.setScreen(new ChatScreen("gg :sob:",false));
            });
            context.waitTicks(5);context.takeScreenshot("cokechat-chat");
            context.runOnClient(client->{
                var history=messages(client.gui.getChat());
                if(history.size()!=1000)throw new AssertionError("Expected 1000 retained messages, got "+history.size());
                if(!history.get(1).content().getString().equals("Alex: gg :sob: :wilted_rose: :fire: :skull:"))throw new AssertionError("Raw received chat was changed");
                if(!input((ChatScreen)client.screen).getValue().equals("gg :sob:"))throw new AssertionError("Input was transformed");
            });
            context.getInput().pressKey(257);
            context.waitFor(client->client.gui.getChat().getRecentChat().contains("gg :sob:"));
            context.waitFor(client->messages(client.gui.getChat()).getFirst().content().getString().contains("gg :sob:"));
            context.setScreen(()->new ChatScreen("",false));
            context.runOnClient(client->client.gui.getChat().scrollChat(900));context.waitTicks(3);context.takeScreenshot("cokechat-history");
            context.setScreen(()->new ChatScreen(":fi",false));context.waitTicks(3);context.takeScreenshot("cokechat-autocomplete");
            context.getInput().pressKey(257);context.waitTicks(2);
            context.runOnClient(client->{if(!(client.screen instanceof ChatScreen))throw new AssertionError("Completion submitted a message instead of staying in chat");});
            context.runOnClient(client->{if(!input((ChatScreen)client.screen).getValue().equals(":fire:"))throw new AssertionError("Completion inserted a transformed message");});
            context.getInput().pressKey(256);
            context.runOnClient(client->{var c=CokeChatClient.config();c.appearance.x=30;c.appearance.y=25;c.appearance.fontSize=11;c.appearance.cornerRadius=18;c.compact.enabled=true;c.emoji.size=16;CokeChatClient.apply();client.setScreen(new ChatScreen("",false));});
            context.waitTicks(3);context.takeScreenshot("cokechat-scaled");
            context.runOnClient(client->{client.gui.getChat().addClientSystemMessage(Component.literal("Catalog: :orca: :pizza: :rocket: :thumbsup: :de:"));client.gui.getChat().addClientSystemMessage(Component.literal("Unicode: 👩🏽‍💻 🇩🇪 👍🏻 👍🏿 🐱 🦄"));});
            context.waitTicks(3);context.takeScreenshot("cokechat-standard-colors");
            motionTests(context);
            updateThreeTests(context);
            commandPopupTests(context);
            CommandCompatibilityTest.run(context);
            appearanceAndCompactTests(context);
            context.runOnClient(client->{CokeChatClient.clearHistory();if(!messages(client.gui.getChat()).isEmpty())throw new AssertionError("Clear history failed");});
        }
    }
    private static void appearanceAndCompactTests(ClientGameTestContext context){
        String clipboard=context.computeOnClient(client->client.keyboardHandler.getClipboard());
        try{
            context.runOnClient(client->{var c=CokeChatClient.config();c.compact.enabled=true;c.compact.groupingSeconds=2;c.compact.counterFormat="×{count}";c.animations.open=false;c.animations.openMessageFade=false;c.input=new de.cokechat.config.CokeChatConfig.Input();CokeChatClient.clearHistory();CokeChatClient.applySettings();client.setScreen(new ChatScreen("",false));client.gui.getChat().addClientSystemMessage(Component.literal("Rolling duplicate"));});
            long birth=context.computeOnClient(client->{var raw=messages(client.gui.getChat()).getFirst();return ((de.cokechat.chat.ChatAccess)client.gui.getChat()).cokechat$state().group(raw).born;});
            for(int i=0;i<2;i++){context.waitTicks(30);context.runOnClient(client->client.gui.getChat().addClientSystemMessage(Component.literal("Rolling duplicate")));}
            context.runOnClient(client->{var chat=client.gui.getChat();if(displayed(chat).size()!=1||!visibleText(chat).contains("×3"))throw new AssertionError("Rolling duplicate timer did not restart");if(((de.cokechat.chat.ChatAccess)chat).cokechat$state().group(messages(chat).getFirst()).born!=birth)throw new AssertionError("Duplicate restarted incoming animation");CokeChatClient.applySettings();if(displayed(chat).size()!=1)throw new AssertionError("History rebuild changed rolling groups");});
            context.waitTicks(41);context.runOnClient(client->{var chat=client.gui.getChat();chat.addClientSystemMessage(Component.literal("Rolling duplicate"));chat.addClientSystemMessage(Component.literal("Hello").withStyle(net.minecraft.ChatFormatting.GREEN));chat.addClientSystemMessage(Component.literal("Hello").withStyle(net.minecraft.ChatFormatting.AQUA));if(displayed(chat).size()!=4)throw new AssertionError("Expiry or formatting-aware grouping failed");});
            context.takeScreenshot("cokechat-rolling-compact");
            context.runOnClient(client->client.gui.getChat().addClientSystemMessage(Component.literal("§aHello §bWorld §c!")));context.waitTicks(8);
            actionCursor(context,false,false);context.waitTicks(3);context.getInput().pressMouse(0);
            context.runOnClient(client->{if(!client.keyboardHandler.getClipboard().equals("Hello World !"))throw new AssertionError("Real Copy Chat retained formatting codes");if(!messages(client.gui.getChat()).getFirst().content().getString().contains("§a"))throw new AssertionError("Copy modified original message");});
            for(boolean border:new boolean[]{false,true})for(boolean inputBorder:new boolean[]{false,true}){
                context.runOnClient(client->{var c=CokeChatClient.config();c.appearance.chatBorder=border;c.appearance.inputBorder=inputBorder;c.appearance.scrollbar=false;c.appearance.borderOpacity=1;c.input.borderOpacity=1;CokeChatClient.applySettings();});context.waitTicks(2);context.takeScreenshot("cokechat-borders-"+border+"-"+inputBorder);
            }
            context.runOnClient(client->{var c=CokeChatClient.config();c.appearance.chatBorder=true;c.appearance.inputBorder=true;c.appearance.scrollbar=true;c.animations.open=true;c.animations.openMessageFade=true;c.animations.openDuration=1000;c.animations.openDistance=100;c.animations.fadeDuration=900;c.animations.fadeDelay=100;c.animations.fadeStagger=50;CokeChatClient.applySettings();client.setScreen(null);});
            context.getInput().holdKey(344);context.waitTicks(2);context.takeScreenshot("cokechat-peek-shared-expansion");context.waitFor(client->de.cokechat.rendering.ChatPeek.opening.size(de.cokechat.animation.Motion.now(),CokeChatClient.config().animations)>.999);context.takeScreenshot("cokechat-peek-shared-open");context.getInput().releaseKey(344);context.waitFor(client->!de.cokechat.rendering.ChatPeek.active());
            context.runOnClient(client->{var c=CokeChatClient.config();c.animations=new de.cokechat.config.CokeChatConfig.Animations();CokeChatClient.applySettings();});
        }finally{context.getInput().releaseKey(344);context.runOnClient(client->client.keyboardHandler.setClipboard(clipboard));}
    }
    private static void cursor(ClientGameTestContext context,double x,double y){
        double[] point=context.computeOnClient(client->new double[]{x*client.getWindow().getScreenWidth()/client.getWindow().getGuiScaledWidth(),y*client.getWindow().getScreenHeight()/client.getWindow().getGuiScaledHeight()});
        context.getInput().setCursorPos(point[0],point[1]);
    }
    private static void actionCursor(ClientGameTestContext context,boolean peek,boolean delete){
        double[] point=context.computeOnClient(client->{
            de.cokechat.rendering.ChatPresentation.peek=peek;
            try{var c=CokeChatClient.config();double step=de.cokechat.compact.CompactChatManager.lineHeight(c)*de.cokechat.rendering.ChatPresentation.scale();int size=Math.min(c.actions.size,(int)step);
                return new double[]{de.cokechat.rendering.ChatPresentation.x()+de.cokechat.rendering.ChatPresentation.width()-2*size-c.actions.spacing+(delete?size+c.actions.spacing:0)+size/2.0,de.cokechat.rendering.ChatPresentation.bottom()-step/2};
            }finally{de.cokechat.rendering.ChatPresentation.peek=false;}
        });cursor(context,point[0],point[1]);
    }
    private static void motionTests(ClientGameTestContext context){
        String clipboard=context.computeOnClient(client->client.keyboardHandler.getClipboard());
        try{
            context.runOnClient(client->{
                var c=CokeChatClient.config();c.appearance=new de.cokechat.config.CokeChatConfig.Appearance();c.emoji.size=9;c.compact.enabled=true;c.animations.messageDuration=2000;c.peek.width=210;c.peek.height=100;c.peek.x=120;c.peek.y=30;CokeChatClient.applySettings();CokeChatClient.clearHistory();
                for(int i=0;i<60;i++)client.gui.getChat().addClientSystemMessage(Component.literal("History row "+i));
                for(int i=0;i<5;i++)client.gui.getChat().addClientSystemMessage(Component.literal("Clashbad: raw :skull:"));
                var access=(de.cokechat.chat.ChatAccess)client.gui.getChat();var newest=messages(client.gui.getChat()).getFirst();var group=access.cokechat$state().group(newest);
                if(group.members.size()!=5||group.id==newest)throw new AssertionError("Duplicate animation identity was restarted");
                client.setScreen(new ChatScreen("unsent draft",false));
            });
            context.waitTicks(2);context.takeScreenshot("cokechat-message-animation");
            context.waitFor(client->{var access=(de.cokechat.chat.ChatAccess)client.gui.getChat();return de.cokechat.animation.Motion.now()-access.cokechat$state().group(messages(client.gui.getChat()).getFirst()).born>=2000;});
            actionCursor(context,false,false);context.waitTicks(10);context.takeScreenshot("cokechat-hover-copy");context.getInput().pressMouse(0);
            context.runOnClient(client->{if(!client.keyboardHandler.getClipboard().equals("Clashbad: raw :skull:"))throw new AssertionError("Copy did not use raw single-message text: "+client.keyboardHandler.getClipboard());});
            actionCursor(context,false,true);context.getInput().pressMouse(0);
            context.runOnClient(client->{if(messages(client.gui.getChat()).size()!=65||visibleText(client.gui.getChat()).contains("Clashbad: raw"))throw new AssertionError("Delete changed raw history or left the duplicate group visible");});
            context.getInput().scroll(1);context.waitTicks(1);
            context.runOnClient(client->{var scroll=((de.cokechat.chat.ChatAccess)client.gui.getChat()).cokechat$scroll(false);if(scroll.target()!=7)throw new AssertionError("Normal scroll distance: "+scroll.target());});
            context.getInput().holdShift();context.getInput().scroll(1);context.getInput().releaseShift();
            context.runOnClient(client->{var scroll=((de.cokechat.chat.ChatAccess)client.gui.getChat()).cokechat$scroll(false);if(scroll.target()!=8.75)throw new AssertionError("Shift precision distance: "+scroll.target());});
            context.waitTicks(6);context.takeScreenshot("cokechat-smooth-scroll");
            context.getInput().holdKey(344);context.waitFor(client->de.cokechat.rendering.ChatPeek.opacity()>.99);
            actionCursor(context,true,false);context.waitTicks(10);context.takeScreenshot("cokechat-peek-hover");context.getInput().pressMouse(0);
            context.runOnClient(client->{if(!client.keyboardHandler.getClipboard().equals("History row 59"))throw new AssertionError("Peek copy hitbox did not match the displayed row");});
            context.getInput().scroll(1);
            context.runOnClient(client->{var access=(de.cokechat.chat.ChatAccess)client.gui.getChat();if(access.cokechat$scroll(true).target()!=7)throw new AssertionError("Peek scroll failed or hold key incorrectly enabled slow mode");if(access.cokechat$scroll(false).target()!=8.75)throw new AssertionError("Peek changed normal scroll");});
            context.getInput().holdShift();context.getInput().scroll(1);context.getInput().releaseShift();
            context.runOnClient(client->{var access=(de.cokechat.chat.ChatAccess)client.gui.getChat();if(access.cokechat$scroll(true).target()!=8.75)throw new AssertionError("Peek Shift precision failed");});
            context.getInput().releaseKey(344);context.waitFor(client->!de.cokechat.rendering.ChatPeek.active());
            context.runOnClient(client->{if(!(client.screen instanceof ChatScreen)||!input((ChatScreen)client.screen).getValue().equals("unsent draft"))throw new AssertionError("Peek changed chat screen or draft");if(((de.cokechat.chat.ChatAccess)client.gui.getChat()).cokechat$scroll(false).target()!=8.75)throw new AssertionError("Peek close changed normal scroll");});
            context.runOnClient(client->{
                var c=CokeChatClient.config();c.animations.messages=false;c.appearance.fontSize=11;c.appearance.messageSpacing=5;c.appearance.x=35;c.emoji.size=16;CokeChatClient.applySettings();
                for(int i=0;i<3;i++)client.gui.getChat().addClientSystemMessage(Component.literal("CokeKnight: a wrapped original message :fire: "+"precision hitbox check ".repeat(5)));
            });
            actionCursor(context,false,false);context.waitTicks(3);context.takeScreenshot("cokechat-scaled-hover");context.getInput().pressMouse(0);
            context.runOnClient(client->{if(!client.keyboardHandler.getClipboard().equals("CokeKnight: a wrapped original message :fire: "+"precision hitbox check ".repeat(5)))throw new AssertionError("Scaled/wrapped copy used wrong geometry or transformed text");});
            context.getInput().holdKey(344);context.waitFor(client->de.cokechat.rendering.ChatPeek.opacity()>.99);
            context.runOnClient(client->((de.cokechat.chat.ChatAccess)client.gui.getChat()).cokechat$scroll(true).reset());
            actionCursor(context,true,true);context.waitTicks(3);context.takeScreenshot("cokechat-peek-group-delete");context.getInput().pressMouse(0);
            context.runOnClient(client->{if(messages(client.gui.getChat()).size()!=68||visibleText(client.gui.getChat()).contains("precision hitbox"))throw new AssertionError("Peek delete did not hide whole wrapped compact group");});
            context.getInput().releaseKey(344);context.waitFor(client->!de.cokechat.rendering.ChatPeek.active());
            for(int i=0;i<3;i++){context.getInput().holdKey(344);context.waitTicks(1);context.getInput().releaseKey(344);context.waitTicks(1);}
            context.waitFor(client->!de.cokechat.rendering.ChatPeek.active());
            context.setScreen(()->null);context.getInput().holdKey(344);context.waitFor(client->de.cokechat.rendering.ChatPeek.opacity()>.99);context.takeScreenshot("cokechat-peek-in-game");context.getInput().releaseKey(344);context.waitFor(client->!de.cokechat.rendering.ChatPeek.active());
            context.setScreen(()->new CokeChatSettingsScreen(null));GuiRedesignTest.category(context,"Chat Animation");context.waitTicks(2);context.takeScreenshot("cokechat-animation-settings");
            GuiRedesignTest.category(context,"Chat Peek");context.waitTicks(2);context.takeScreenshot("cokechat-peek-settings");GuiRedesignTest.category(context,"Message Actions");context.waitTicks(2);context.takeScreenshot("cokechat-action-settings");
            context.runOnClient(client->{var c=CokeChatClient.config();c.appearance=new de.cokechat.config.CokeChatConfig.Appearance();c.emoji.size=9;CokeChatClient.applySettings();});
            context.setScreen(()->new PreviewScreen(null));double previewBottom=context.computeOnClient(client->client.getWindow().getGuiScaledHeight()-68.0);cursor(context,321,previewBottom-6);context.waitTicks(10);context.takeScreenshot("cokechat-interactive-preview");context.getInput().pressMouse(0);
            context.runOnClient(client->{if(!client.keyboardHandler.getClipboard().equals("CokeKnight: and you still need me to carry you"))throw new AssertionError("Preview copy did not use local sample original");});
            cursor(context,336,previewBottom-6);context.getInput().pressMouse(0);context.waitTicks(2);context.takeScreenshot("cokechat-preview-delete");
            context.runOnClient(client->{if(messages(client.gui.getChat()).size()!=68)throw new AssertionError("Preview action affected actual chat");});
        }finally{context.getInput().releaseKey(344);context.getInput().releaseShift();context.runOnClient(client->client.keyboardHandler.setClipboard(clipboard));}
    }
    private static void updateThreeTests(ClientGameTestContext context){
        context.runOnClient(client->{
            var c=CokeChatClient.config();c.animations.smoothScroll=false;c.animations.messageDuration=250;c.compact.enabled=true;c.appearance.width=200;c.input.fontSize=14;c.input.width=250;c.input.positionMode=de.cokechat.config.CokeChatConfig.PositionMode.ABSOLUTE_SCREEN;c.input.x=90;c.input.y=70;CokeChatClient.applySettings();CokeChatClient.clearHistory();
            for(int i=0;i<80;i++)client.gui.getChat().addClientSystemMessage(Component.literal("Reading anchor "+i));
            client.setScreen(new ChatScreen("Unsent local draft :fire:",false));
            var access=(de.cokechat.chat.ChatAccess)client.gui.getChat();access.cokechat$wheel(4,false,false);access.cokechat$wheel(3,true,true);
            var normal=access.cokechat$lines(false).get((int)access.cokechat$scroll(false).value(de.cokechat.animation.Motion.now())).parent();
            de.cokechat.rendering.ChatPresentation.peek=true;var overlay=access.cokechat$lines(true).get((int)access.cokechat$scroll(true).value(de.cokechat.animation.Motion.now())).parent();de.cokechat.rendering.ChatPresentation.peek=false;
            for(int i=0;i<15;i++)client.gui.getChat().addClientSystemMessage(Component.literal("Burst message "+i+" wrapped "+"text ".repeat(10)));
            for(int i=0;i<12;i++)client.gui.getChat().addClientSystemMessage(Component.literal("Compact duplicate counter"));
            if(access.cokechat$lines(false).get((int)access.cokechat$scroll(false).value(de.cokechat.animation.Motion.now())).parent()!=normal)throw new AssertionError("New messages moved reading viewport");
            de.cokechat.rendering.ChatPresentation.peek=true;
            try{if(access.cokechat$lines(true).get((int)access.cokechat$scroll(true).value(de.cokechat.animation.Motion.now())).parent()!=overlay)throw new AssertionError("Peek reading position changed");}finally{de.cokechat.rendering.ChatPresentation.peek=false;}
            access.cokechat$wheel(-1000,false,false);client.gui.getChat().addClientSystemMessage(Component.literal("Following again"));if(access.cokechat$scroll(false).target()!=0)throw new AssertionError("Bottom follow not resumed");
            c.animations.openDuration=1000;c.animations.openDistance=100;client.setScreen(new ChatScreen("Unsent local draft :fire:",false));
        });
        context.waitTicks(3);context.takeScreenshot("cokechat-opening-third-update");context.waitTicks(22);context.takeScreenshot("cokechat-input-third-update");
        context.runOnClient(client->{var box=input((ChatScreen)client.screen);if(box.getX()!=95||box.getWidth()!=240||!box.getValue().equals("Unsent local draft :fire:"))throw new AssertionError("Input position/width/draft changed incorrectly");
            var c=CokeChatClient.config();c.animations.smoothScroll=true;c.animations.openDuration=250;c.animations.openDistance=50;c.input=new de.cokechat.config.CokeChatConfig.Input();CokeChatClient.applySettings();});
    }
    private static net.minecraft.client.gui.components.CommandSuggestions commands(ChatScreen screen){try{var field=ChatScreen.class.getDeclaredField("commandSuggestions");field.setAccessible(true);return (net.minecraft.client.gui.components.CommandSuggestions)field.get(screen);}catch(Exception e){throw new AssertionError(e);}}
    private static de.cokechat.rendering.CommandPopup popup(ChatScreen screen){try{var commands=commands(screen);for(var field:commands.getClass().getDeclaredFields())if(field.getType()==de.cokechat.rendering.CommandPopup.class){field.setAccessible(true);return (de.cokechat.rendering.CommandPopup)field.get(commands);}throw new AssertionError("Popup field missing");}catch(Exception e){throw new AssertionError(e);}}
    private static void assertPopup(net.minecraft.client.Minecraft client){var p=popup((ChatScreen)client.screen).layout();var input=de.cokechat.rendering.ChatInputStyle.bounds;if(p==null||p.rows()==0||p.x()<0||p.y()<0||p.x()+p.width()>client.getWindow().getGuiScaledWidth()||p.y()+p.height()>input.y()-5)throw new AssertionError("Popup clipped or overlapped input: "+p+" input="+input);}
    private static void commandPopupTests(ClientGameTestContext context){
        int originalScale=context.computeOnClient(client->client.options.guiScale().get());
        int[] originalSize=context.computeOnClient(client->new int[]{client.getWindow().getScreenWidth(),client.getWindow().getScreenHeight()});
        try{
            context.runOnClient(client->{
                var dispatcher=client.player.connection.getCommands();
                dispatcher.register(com.mojang.brigadier.builder.LiteralArgumentBuilder.<net.minecraft.client.multiplayer.ClientSuggestionProvider>literal("cokechat_local_test").then(com.mojang.brigadier.builder.RequiredArgumentBuilder.<net.minecraft.client.multiplayer.ClientSuggestionProvider,String>argument("value",com.mojang.brigadier.arguments.StringArgumentType.word()).suggests((ctx,builder)->{for(int i=0;i<20;i++)builder.suggest("option_"+i+"_"+"long_suggestion_".repeat(9));return builder.buildFuture();}).executes(ctx->0)));
                dispatcher.register(com.mojang.brigadier.builder.LiteralArgumentBuilder.<net.minecraft.client.multiplayer.ClientSuggestionProvider>literal("cokechat_local_number").then(com.mojang.brigadier.builder.RequiredArgumentBuilder.<net.minecraft.client.multiplayer.ClientSuggestionProvider,Integer>argument("number",com.mojang.brigadier.arguments.IntegerArgumentType.integer(1,10)).executes(ctx->0)));
                var c=CokeChatClient.config();c.input=new de.cokechat.config.CokeChatConfig.Input();c.input.positionMode=de.cokechat.config.CokeChatConfig.PositionMode.ABSOLUTE_SCREEN;c.input.x=95;c.input.y=170;c.input.width=240;c.input.fontSize=12;CokeChatClient.applySettings();
                client.setScreen(new ChatScreen("/",false));commands((ChatScreen)client.screen).showSuggestions(false);
            });
            context.waitTicks(3);context.runOnClient(CokeChatGameTest::assertPopup);context.takeScreenshot("cokechat-command-slash");
            context.runOnClient(client->{input((ChatScreen)client.screen).setValue("/cokechat_local_test ");commands((ChatScreen)client.screen).showSuggestions(false);});
            context.waitTicks(3);context.runOnClient(CokeChatGameTest::assertPopup);context.takeScreenshot("cokechat-command-long-suggestions");
            context.getInput().pressKey(264);context.getInput().pressKey(265);context.getInput().pressKey(264);context.getInput().pressKey(258);
            context.runOnClient(client->{if(!input((ChatScreen)client.screen).getValue().contains("option_10_"))throw new AssertionError("Arrow/Tab completion failed: "+input((ChatScreen)client.screen).getValue());input((ChatScreen)client.screen).setValue("/cokechat_local_test ");commands((ChatScreen)client.screen).showSuggestions(false);});
            context.waitTicks(2);double[] point=context.computeOnClient(client->{var p=popup((ChatScreen)client.screen).layout();return new double[]{p.x()+15,p.y()+10};});cursor(context,point[0],point[1]);context.getInput().pressMouse(0);
            context.runOnClient(client->{if(!input((ChatScreen)client.screen).getValue().contains("option_0_"))throw new AssertionError("Relocated popup mouse insertion failed");});
            context.runOnClient(client->{input((ChatScreen)client.screen).setValue("/cokechat_local_test ");commands((ChatScreen)client.screen).showSuggestions(false);});context.waitTicks(2);
            double previousChatScroll=context.computeOnClient(client->((de.cokechat.chat.ChatAccess)client.gui.getChat()).cokechat$scroll(false).target());
            context.getInput().scroll(-1);context.waitTicks(2);context.runOnClient(client->{if(popup((ChatScreen)client.screen).scrollOffset()<1)throw new AssertionError("Wrapped suggestions did not scroll");if(((de.cokechat.chat.ChatAccess)client.gui.getChat()).cokechat$scroll(false).target()!=previousChatScroll)throw new AssertionError("Popup wheel leaked into chat history");});
            for(String command:new String[]{"/cokechat_local_number ","/cokechat_local_number nope","/this_command_does_not_exist_"+"long_error_context_".repeat(10)+" argument"}){
                context.runOnClient(client->{input((ChatScreen)client.screen).setValue(command);commands((ChatScreen)client.screen).hide();});context.waitTicks(2);context.runOnClient(CokeChatGameTest::assertPopup);
                context.takeScreenshot(command.endsWith("nope")?"cokechat-command-error":command.endsWith(" ")?"cokechat-command-usage":"cokechat-command-long-error");
            }
            int[][] cases={{854,480,1,9,2,2},{854,480,2,14,300,190},{1280,720,3,24,1200,2},{640,360,2,12,2,330}};
            for(int n=0;n<cases.length;n++){int[] scenario=cases[n];context.runOnClient(client->{client.getWindow().setWindowed(scenario[0],scenario[1]);client.options.guiScale().set(scenario[2]);client.resizeGui();var c=CokeChatClient.config();c.input.fontSize=scenario[3];c.input.x=scenario[4];c.input.y=scenario[5];c.input.width=180;CokeChatClient.applySettings();client.setScreen(new ChatScreen("/cokechat_local_number nope",false));commands((ChatScreen)client.screen).hide();});context.waitTicks(4);context.runOnClient(CokeChatGameTest::assertPopup);context.takeScreenshot("cokechat-command-layout-"+n);}
            context.runOnClient(client->{var c=CokeChatClient.config();c.input.positionMode=de.cokechat.config.CokeChatConfig.PositionMode.RELATIVE_TO_CHAT;c.input.x=15;c.input.y=12;c.appearance.x=40;c.appearance.y=50;CokeChatClient.applySettings();client.setScreen(new ChatScreen("/cokechat_local_number nope",false));commands((ChatScreen)client.screen).hide();});context.waitTicks(3);context.runOnClient(CokeChatGameTest::assertPopup);context.takeScreenshot("cokechat-command-relative");
            context.runOnClient(client->{CokeChatClient.config().enabled=false;CokeChatClient.applySettings();client.setScreen(new ChatScreen("/",false));if(de.cokechat.rendering.CommandPopup.enabled(input((ChatScreen)client.screen)))throw new AssertionError("Custom popup remained enabled in vanilla mode");CokeChatClient.config().enabled=true;CokeChatClient.applySettings();});
        }finally{context.runOnClient(client->{client.options.guiScale().set(originalScale);client.getWindow().setWindowed(originalSize[0],originalSize[1]);client.resizeGui();});}
    }
}
