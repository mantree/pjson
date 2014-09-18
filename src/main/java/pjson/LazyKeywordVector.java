package pjson;

import clojure.lang.*;

/**
 * When realised uses the KeywordisingListener to ensure nested maps are keywordised.
 */
public class LazyKeywordVector extends LazyVector {

    public LazyKeywordVector(char[] json, int from, int len){
        super(json, from, len);
    }

    @Override
    protected void realized(){
        if(!realized.getAndSet(true)){
            JSONListener listener = new KeywordisingListener();
            PJSON.performLazyParse(json, from, from+len, listener);
            v = (APersistentVector)listener.getValue();
        }
    }
}
