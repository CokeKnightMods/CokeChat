package de.cokechat.chat;
import java.util.List;
import java.util.function.Function;
/** Keeps a wrapped line within its stable message/group at the same viewport offset. */
public record ViewportAnchor(Object key,int line,int index,boolean following) {
    public static <T> ViewportAnchor capture(List<T> lines,double value,double target,Function<T,Object> key){
        if(lines.isEmpty()||value<.01&&target<.01)return new ViewportAnchor(null,0,0,true);
        int index=Math.min(lines.size()-1,Math.max(0,(int)Math.floor(value))),first=index;
        Object id=key.apply(lines.get(index));while(first>0&&key.apply(lines.get(first-1))==id)first--;
        return new ViewportAnchor(id,index-first,index,false);
    }
    public <T> int delta(List<T> lines,Function<T,Object> key){
        for(int i=0;i<lines.size();i++)if(key.apply(lines.get(i))==this.key){int last=i;while(last+1<lines.size()&&key.apply(lines.get(last+1))==this.key)last++;return i+Math.min(line,last-i)-index;}
        return 0;
    }
}
