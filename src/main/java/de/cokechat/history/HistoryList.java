package de.cokechat.history;
import java.util.*;
/** Random-access deque: O(1) insertion/removal at either end and O(1) scrolled-line lookup. */
public final class HistoryList<E> extends AbstractList<E> implements RandomAccess {
    private Object[] items = new Object[128]; private int head, length;
    @Override public int size() { return length; }
    @SuppressWarnings("unchecked") @Override public E get(int index) { Objects.checkIndex(index,length); return (E)items[(head+index)%items.length]; }
    @Override public E set(int index,E value) { E old=get(index);items[(head+index)%items.length]=value;return old; }
    private void grow() { if(length<items.length)return;Object[] next=new Object[items.length*2];for(int i=0;i<length;i++)next[i]=get(i);items=next;head=0; }
    @Override public void add(int index,E value) {
        if(index<0||index>length)throw new IndexOutOfBoundsException(index);grow();
        if(index==0){head=Math.floorMod(head-1,items.length);items[head]=value;}
        else if(index==length)items[(head+length)%items.length]=value;
        else{for(int i=length;i>index;i--)items[(head+i)%items.length]=items[(head+i-1)%items.length];items[(head+index)%items.length]=value;}
        length++;modCount++;
    }
    @Override public E remove(int index) {
        E old=get(index);
        if(index==0){items[head]=null;head=(head+1)%items.length;}
        else{for(int i=index;i<length-1;i++)items[(head+i)%items.length]=items[(head+i+1)%items.length];items[(head+length-1)%items.length]=null;}
        length--;modCount++;return old;
    }
    @Override public void clear(){Arrays.fill(items,null);head=0;length=0;modCount++;}
}
