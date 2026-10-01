package de.cokechat;
import de.cokechat.chat.CommandAnalysis;
import com.mojang.brigadier.*;
import com.mojang.brigadier.arguments.*;
import com.mojang.brigadier.builder.*;
import com.mojang.brigadier.suggestion.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class CommandAnalysisTest {
    private static LiteralArgumentBuilder<Object> literal(String name){return LiteralArgumentBuilder.literal(name);}
    @Test void parsesBothSourcesWithoutLosingValidServerOrClientBranch(){
        var local=new CommandDispatcher<Object>();var server=new CommandDispatcher<Object>();
        local.register(literal("shared").then(literal("local").executes(c->1)));
        server.register(literal("shared").then(literal("remote").executes(c->1)));
        for(String suffix:List.of("local","remote"))assertEquals(CommandAnalysis.State.VALID,new CommandAnalysis<>("/shared "+suffix,new Object(),List.of(local,server)).state());
        var both=new CommandAnalysis<>("/shared ",new Object(),List.of(local,server));
        assertEquals(List.of("local","remote"),both.ready(both.request(8)).getList().stream().map(Suggestion::getText).toList());
        assertEquals(2,both.sources().size());
    }
    @Test void distinguishesUnknownIncompleteAndActualArgumentError(){
        var d=new CommandDispatcher<Object>();d.register(literal("mod").then(RequiredArgumentBuilder.<Object,Integer>argument("count",IntegerArgumentType.integer()).executes(c->1)));
        assertEquals(CommandAnalysis.State.UNKNOWN_ROOT,new CommandAnalysis<>("/mo",new Object(),List.of(d)).state());
        assertTrue(new CommandAnalysis<>("/mo_",new Object(),List.of(d)).typingRoot());
        assertEquals(CommandAnalysis.State.INCOMPLETE,new CommandAnalysis<>("/mod ",new Object(),List.of(d)).state());
        assertEquals(CommandAnalysis.State.ARGUMENT_ERROR,new CommandAnalysis<>("/mod nope",new Object(),List.of(d)).state());
        assertEquals(CommandAnalysis.State.VALID,new CommandAnalysis<>("/mod 4",new Object(),List.of(d)).state());
        assertEquals(CommandAnalysis.State.UNAVAILABLE,new CommandAnalysis<>("/mod",new Object(),List.<CommandDispatcher<Object>>of()).state());
    }
    @Test void slashAndMiddleCursorRangesRemainOriginal(){
        var d=new CommandDispatcher<Object>();d.register(literal("example").executes(c->1));
        var a=new CommandAnalysis<>("/exa tail",new Object(),List.of(d));var s=a.ready(a.request(4)).getList().getFirst();
        assertEquals(1,s.getRange().getStart());assertEquals("/example tail",s.apply("/exa tail"));
        assertEquals(CommandAnalysis.State.UNKNOWN_ROOT,new CommandAnalysis<>("//example",new Object(),List.of(d)).state());
    }
    @Test void mergesDuplicatesAndKeepsTooltipAndReplacementRanges(){
        var plain=new SuggestionsBuilder("/he",1).suggest("hello").build();
        var tip=new SuggestionsBuilder("/he",1).suggest("hello",new LiteralMessage("provider tip")).build();
        var result=CommandAnalysis.merge("/he",List.of(plain,tip));assertEquals(1,result.getList().size());assertNotNull(result.getList().getFirst().getTooltip());assertEquals("/hello",result.getList().getFirst().apply("/he"));
    }
    @Test void delayedAndFailedProvidersDoNotHideAvailableLocalResults(){
        var local=new CommandDispatcher<Object>();var remote=new CommandDispatcher<Object>();var delayed=new CompletableFuture<Suggestions>();
        local.register(literal("mod").then(RequiredArgumentBuilder.<Object,String>argument("arg",StringArgumentType.word()).suggests((c,b)->b.suggest("local").buildFuture())));
        remote.register(literal("mod").then(RequiredArgumentBuilder.<Object,String>argument("arg",StringArgumentType.word()).suggests((c,b)->delayed)));
        var a=new CommandAnalysis<>("/mod ",new Object(),List.of(local,remote));var requests=a.request(5);
        assertEquals("local",a.ready(requests).getList().getFirst().getText());
        delayed.completeExceptionally(new IllegalStateException("provider unavailable"));
        assertEquals("local",a.ready(requests).getList().getFirst().getText());
    }
}
