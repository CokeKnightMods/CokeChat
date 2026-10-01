package de.cokechat.chat;
import java.util.*;
/** Identity-based display state, separate from immutable vanilla message contents/signatures. */
public final class LocalMessageState<T> {
    public static final class Group<T> {
        public final T id; public final long born;
        public final List<T> members=new ArrayList<>();
        Group(T id,long born){this.id=id;this.born=born;}
    }
    private final IdentityHashMap<T,Long> births=new IdentityHashMap<>();
    private final IdentityHashMap<T,Group<T>> groups=new IdentityHashMap<>();
    private final Set<T> hidden=Collections.newSetFromMap(new IdentityHashMap<>());
    public void register(T raw,long now){births.putIfAbsent(raw,now);}
    public Group<T> attach(T raw,T previous,boolean merge,long now){
        register(raw,now);Group<T> group=merge?groups.get(previous):null;
        if(group==null)group=new Group<>(raw,births.get(raw));
        group.members.add(raw);groups.put(raw,group);return group;
    }
    public Group<T> group(T raw){return groups.get(raw);}
    public boolean hidden(T raw){return hidden.contains(raw);}
    public void hideGroup(T raw){var group=groups.get(raw);if(group==null)hidden.add(raw);else hidden.addAll(group.members);}
    public void rebuild(){groups.clear();}
    public void retain(Collection<T> raw){var keep=Collections.newSetFromMap(new IdentityHashMap<T,Boolean>());keep.addAll(raw);births.keySet().retainAll(keep);groups.keySet().retainAll(keep);hidden.retainAll(keep);}
    public void clear(){births.clear();groups.clear();hidden.clear();}
}
