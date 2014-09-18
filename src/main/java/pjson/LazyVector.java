package pjson;

import clojure.lang.*;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Takes a json string and only realized it when required, the toString methods do not require any json parsing.
 */
public class LazyVector extends APersistentVector implements ToJSONString{

    final char[] json;
    final int from, len;
    final AtomicBoolean realized = new AtomicBoolean(false);
    APersistentVector v;

    public LazyVector(char[] json, int from, int len){
        this.json = json;
        this.from = from;
        this.len = len;
    }

    public final char[] json(){
        return json;
    }

    public final int jsonFrom(){
        return from;
    }

    public final int jsonLen(){
        return len;
    }

    protected void realized(){
        if(!realized.getAndSet(true)){
            DefaultListener listener = new DefaultListener();
            PJSON.performLazyParse(json, from, from+len, listener);
            v = (APersistentVector)listener.getValue();
        }
    }
    @Override
    public IPersistentVector assocN(int i, Object val) {
        realized();
        return v.assocN(i, val);
    }

    @Override
    public int count() {
        realized();

        return (v == null) ? 0 : v.count();
    }

    @Override
    public IPersistentVector cons(Object o) {
        realized();
        return v.cons(o);
    }

    @Override
    public IPersistentCollection empty() {
        return PersistentVector.EMPTY;
    }

    @Override
    public IPersistentStack pop() {
        realized();
        return v.pop();
    }

    @Override
    public Object nth(int i) {
        realized();
        return v.nth(i);
    }

    @Override
    public String toString(){
        return StringUtil.fastToString(json, from, len);
    }

    @Override
    public void toString(JSONWriter buff) {
        buff.writeString(json, from, len);
    }
}
