package de.cokechat;
import de.cokechat.emoji.*;
import de.cokechat.visualwords.*;
import de.cokechat.history.*;
import de.cokechat.config.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.*;
import java.util.*;
public class CoreTest {
    @Test void fullStandardCatalogHasNoSkinVariants() {
        var db=EmojiDatabase.load(Path.of("missing-test-emojis.json"));
        assertEquals(1914,db.definitions().size());
        assertEquals(1911,db.definitions().stream().filter(d->d.font()!=null).count());
        for(var d:db.definitions()) {
            assertFalse(d.id().contains("skin-tone"));
            assertFalse(d.unicode().codePoints().anyMatch(cp->cp>=0x1F3FB&&cp<=0x1F3FF));
        }
        assertNotNull(db.find("orca"));assertNotNull(db.find("thumbsup"));assertNull(db.find("skin-tone-3"));
        assertFalse(db.search("cat").isEmpty());assertEquals("fire",db.suggest("fi").getFirst());
    }
    @Test void unicodeSequencesUseStandardColorAndPreserveRawText() {
        var db=EmojiDatabase.load(Path.of("missing-test-emojis.json"));var parser=new EmojiParser(db);
        for(String text:List.of("👍","👍🏽","🇩🇪","👩‍💻","👩🏽‍💻","🫱🏽‍🫲🏻","1️⃣","👨‍👩‍👧‍👦")) {
            var tokens=parser.parse(text);assertEquals(1,tokens.size(),text);assertNotNull(tokens.getFirst().emoji(),text);assertEquals(text,tokens.getFirst().text());
        }
        assertEquals(parser.parse("👍").getFirst().emoji(),parser.parse("👍🏽").getFirst().emoji());
        assertEquals("Unknown :skin-tone-3:",parser.parse("Unknown :skin-tone-3:").getFirst().text());
    }
    @Test void everyCatalogGlyphHasARealLocalFontAndTexture() throws Exception {
        var db=EmojiDatabase.load(Path.of("missing-test-emojis.json"));var root=Path.of("src/main/resources/assets/cokechat");
        for(var d:db.definitions())if(d.font()!=null) {
            String page=d.font().substring("cokechat:emoji/".length());
            for(int size:List.of(6,9,16)) {
                var json=com.google.gson.JsonParser.parseString(Files.readString(root.resolve("font/emoji_"+size+"/"+page+".json"))).getAsJsonObject();
                boolean found=false;
                for(var p:json.getAsJsonArray("providers")) {var provider=p.getAsJsonObject();if(provider.getAsJsonArray("chars").get(0).getAsString().equals(d.glyph())) {found=true;assertTrue(Files.exists(root.resolve("textures/"+provider.get("file").getAsString().substring("cokechat:".length()))));}}
                assertTrue(found,d.id());
            }
        }
    }
    @TempDir Path directory;
    private EmojiParser parser() { return new EmojiParser(EmojiDatabase.load(directory.resolve("emojis.json"))); }
    @Test void requiredEmojiSequences() {
        for(String text: List.of(":sob:","Hello :sob:",":sob: :fire:","Unknown :something:","Hello :sob: world :fire:")) {
            var tokens=parser().parse(text);assertEquals(text,tokens.stream().map(EmojiParser.Token::text).reduce("",String::concat));
            assertEquals(text.contains(":fire:")?2:text.contains(":sob:")?1:0,tokens.stream().filter(t->t.emoji()!=null).count());
        }
        assertNotNull(parser().parse(":wilted_rose:").getFirst().emoji());
    }
    @Test void invalidEmojiDefinitionsAndUnknownCodesStayText() throws Exception {
        Files.writeString(directory.resolve("emojis.json"),"invalid json");
        assertEquals(":unknown:",parser().parse(":unknown:").getFirst().text());assertNotNull(parser().parse(":sob:").getFirst().emoji());
    }
    @Test void literalRulesCaseAndUnicodeBoundaries() {
        var rule=new VisualWordRule(true,"pog","fire",false,true);
        var parser=new VisualWordParser(List.of(rule));assertEquals("fire fire pogg fire moment épog",parser.parse("pog POG pogg pog moment épog"));
        rule.caseSensitive=true;assertEquals("fire POG",new VisualWordParser(List.of(rule)).parse("pog POG"));
        rule.wholeWord=false;assertEquals("fireg",new VisualWordParser(List.of(rule)).parse("pogg"));
        assertEquals("$1",new VisualWordParser(List.of(new VisualWordRule(true,".*","$1",false,false))).parse(".*"));
    }
    @Test void priorityNoRecursiveReplacementAndInvalidRules() {
        var parser=new VisualWordParser(Arrays.asList(null,new VisualWordRule(true,"","x",false,true),new VisualWordRule(true,"pog","gg",false,true),new VisualWordRule(true,"gg","Good Game",false,true)));
        assertEquals("gg Good Game",parser.parse("pog gg"));
    }
    @Test void boundedHistoryPersistenceIsolationAndClear() {
        var history=new ChatHistoryManager(directory);history.configure(2,true);history.open("world-A");history.add("one");history.add("two\nlines");history.add("three");history.save();
        assertEquals(List.of("two\nlines","three"),history.snapshot());history.close();history.open("world-B");assertTrue(history.snapshot().isEmpty());history.open("world-A");assertEquals(2,history.snapshot().size());history.clear();history.close();history.open("world-A");assertTrue(history.snapshot().isEmpty());
    }
    @Test void unlimitedHistoryAndDisabledPersistence() {
        var history=new ChatHistoryManager(directory);history.configure(-1,false);history.open("world");for(int i=0;i<10001;i++)history.add(""+i);assertEquals(10001,history.snapshot().size());history.save();history.close();history.open("world");assertTrue(history.snapshot().isEmpty());
    }
    @Test void configSurvivesRestartAndCorruption() throws Exception {
        var path=directory.resolve("config.json");var manager=new ConfigManager(path);manager.load();manager.get().appearance.width=512;manager.save();var second=new ConfigManager(path);second.load();assertEquals(512,second.get().appearance.width);
        Files.writeString(path,"{broken");second.load();assertEquals(340,second.get().appearance.width);assertFalse(second.error().isEmpty());
    }
    @Test void partialConfigAndInvalidRanges() throws Exception {
        var path=directory.resolve("config.json");Files.writeString(path,"{\"appearance\":null,\"compact\":{\"mode\":null},\"history\":{\"maxMessages\":-9},\"visualWords\":null}");var manager=new ConfigManager(path);manager.load();assertNotNull(manager.get().appearance);assertEquals(100,manager.get().history.maxMessages);assertEquals(2,manager.get().compact.groupingSeconds);
    }
    @Test void historyDequeMatchesArrayListForMixedOperations() {
        var actual=new HistoryList<Integer>();var expected=new ArrayList<Integer>();var random=new Random(42);
        for(int i=0;i<20000;i++){int action=random.nextInt(4);int index=expected.isEmpty()?0:random.nextInt(expected.size());if(action<2||expected.isEmpty()){int at=action==0?0:expected.size();actual.add(at,i);expected.add(at,i);}else if(action==2){assertEquals(expected.remove(index),actual.remove(index));}else{assertEquals(expected.set(index,i),actual.set(index,i));}assertEquals(expected,actual);}
        actual.clear();assertTrue(actual.isEmpty());
    }
    @Test void productionSourcesContainNoNetworkOrSendHooks() throws Exception {
        var banned=java.util.regex.Pattern.compile("java\\.net\\.|java\\.net\\.http|javax\\.net|io\\.netty|ClientPlayNetworking|ServerPlayNetworking|CustomPayload|sendPacket|sendChat\\(|sendCommand\\(|handleChatInput|normalizeChatMessage|org\\.apache\\.http|okhttp|WebSocket|DatagramSocket");
        try(var files=Files.walk(Path.of("src/main/java"))){for(var file:files.filter(p->p.toString().endsWith(".java")).toList()){String code=Files.readString(file).replaceAll("(?s)/\\*.*?\\*/", "").replaceAll("(?m)//.*$", "");assertFalse(banned.matcher(code).find(),file.toString());}}
    }
}

