package de.cokechat.chat;

import com.mojang.brigadier.*;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/** Read-only metadata from independent live dispatchers. Never executes a command. */
public final class CommandAnalysis<S> {
    public enum State { VALID, INCOMPLETE, UNKNOWN_ROOT, ARGUMENT_ERROR, UNAVAILABLE }
    public record Source<S>(CommandDispatcher<S> dispatcher,ParseResults<S> parse,boolean known,State state) {}
    private final String input;
    private final List<Source<S>> sources;
    private final Source<S> best;
    public CommandAnalysis(String input,S source,List<CommandDispatcher<S>> dispatchers){
        this.input=input;var results=new ArrayList<Source<S>>();
        int start=input.startsWith("/")?1:0,end=input.indexOf(' ',start);
        String root=input.substring(start,end<0?input.length():end);
        Set<CommandDispatcher<S>> seen=Collections.newSetFromMap(new IdentityHashMap<>());
        for(var dispatcher:dispatchers){
            if(dispatcher==null||!seen.add(dispatcher))continue;
            try{
                var reader=new StringReader(input);reader.setCursor(start);
                var parse=dispatcher.parse(reader,source);
                var node=dispatcher.getRoot().getChild(root);
                boolean known=node!=null&&node.canUse(source);
                boolean complete=!parse.getReader().canRead()&&parse.getContext().getLastChild().getCommand()!=null;
                State state=!known?State.UNKNOWN_ROOT:complete?State.VALID:
                    parse.getReader().getRemaining().isBlank()?State.INCOMPLETE:State.ARGUMENT_ERROR;
                results.add(new Source<>(dispatcher,parse,known,state));
            }catch(RuntimeException ignored){/* A broken third-party provider must not disable chat. */}
        }
        sources=List.copyOf(results);
        best=sources.stream().max(Comparator.<Source<S>>comparingInt(s->rank(s.state()))
            .thenComparingInt(s->s.parse().getReader().getCursor())).orElse(null);
    }
    private static int rank(State s){return switch(s){case VALID->4;case INCOMPLETE->3;case ARGUMENT_ERROR->2;case UNKNOWN_ROOT->1;case UNAVAILABLE->0;};}
    public State state(){return best==null?State.UNAVAILABLE:best.state();}
    public boolean typingRoot(){return input.indexOf(' ',input.startsWith("/")?1:0)<0;}
    public ParseResults<S> parse(){return best==null?null:best.parse();}
    public List<Source<S>> sources(){return sources;}
    public Optional<CommandSyntaxException> error(){
        if(best==null||state()!=State.ARGUMENT_ERROR)return Optional.empty();
        return best.parse().getExceptions().values().stream().max(Comparator.comparingInt(CommandSyntaxException::getCursor));
    }
    public List<String> usage(int cursor){
        var usage=new LinkedHashSet<String>();
        if(state()==State.VALID||typingRoot())return List.of();
        for(var s:sources)if(s.known())try{
            var context=s.parse().getContext().findSuggestionContext(Math.max(1,Math.min(cursor,input.length())));
            usage.addAll(s.dispatcher().getSmartUsage(context.parent,s.parse().getContext().getSource()).values());
        }catch(RuntimeException ignored){}
        return usage.stream().limit(3).toList();
    }
    public List<CompletableFuture<Suggestions>> request(int cursor){
        var futures=new ArrayList<CompletableFuture<Suggestions>>();
        for(var source:sources)try{
            futures.add(source.dispatcher().getCompletionSuggestions(source.parse(),Math.max(0,Math.min(cursor,input.length())))
                .exceptionally(error->empty()));
        }catch(RuntimeException ignored){futures.add(CompletableFuture.completedFuture(empty()));}
        return futures;
    }
    public static Suggestions empty(){return Suggestions.empty().join();}
    public static Suggestions merge(String input,Collection<Suggestions> results){
        var merged=Suggestions.merge(input,results);
        var unique=new LinkedHashMap<String,Suggestion>();
        for(var suggestion:merged.getList()){
            String replacement=suggestion.apply(input);var old=unique.get(replacement);
            if(old==null||old.getTooltip()==null&&suggestion.getTooltip()!=null)unique.put(replacement,suggestion);
        }
        return new Suggestions(merged.getRange(),List.copyOf(unique.values()));
    }
    public Suggestions ready(List<CompletableFuture<Suggestions>> futures){return merge(input,futures.stream().map(f->f.getNow(empty())).toList());}
}
