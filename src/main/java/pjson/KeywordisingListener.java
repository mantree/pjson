package pjson;

import clojure.lang.Keyword;

/**
 * Return the JSON data as: Objects == Map, Array == Vector, keys as Keywords and values as String.
 */
public class KeywordisingListener extends DefaultListener{

    @Override
    public void key(String val){
        super.current.append(Keyword.intern(val));
    }

    @Override
    public void lazyObject(char[] json, int from, int end) {
        LazyKeywordMap map = new LazyKeywordMap(json, from, end - from);
        if(current != null)
            current.append(map);
    }

    @Override
    public void lazyArr(char[] json, int from, int end) {
        LazyKeywordVector v = new LazyKeywordVector(json, from, end - from);
        if(current != null)
            current.append(v);
    }
}
