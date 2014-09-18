package pjson;

import clojure.lang.*;

/**
 * When realised uses the KeywordisingListener.
 */
public class LazyKeywordMap extends LazyMap {

    public LazyKeywordMap(char[] json, int from, int len){
        super(json, from, len);
    }

    @Override
    protected final void realize(){
        if(!realized.getAndSet(true)){
            JSONListener listener = new KeywordisingListener();
            PJSON.performLazyParse(json, from, from+len, listener);
            map = (APersistentMap)listener.getValue();
        }
    }

}
