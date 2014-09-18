package pjson;

import clojure.lang.PersistentArrayMap;
import clojure.lang.ITransientMap;
import clojure.lang.PersistentVector;
import clojure.lang.ITransientCollection;

import java.util.ArrayList;
import java.util.List;

/**
 * Object container that allows parsed values to be appended to the contains.
 * Two types of containers are: Vector/Array(s) and Object/Map(s).
 */
public abstract class ValueContainer {

    public abstract void append(Object val);
    public abstract Object getValue();
    public abstract void clear();

    public static final class ObjectContainer extends ValueContainer{

        private Object k = null;
        private ITransientMap v = PersistentArrayMap.EMPTY.asTransient();

        @Override
        public void clear(){
            v = PersistentArrayMap.EMPTY.asTransient();
        }

        @Override
        public final void append(Object val) {
            if(k != null) {
                v = v.assoc(k, val);
                k = null;
            }else
                k = val;
        }

        @Override
        public final Object getValue(){
            return v.persistent();
        }

    }

    public static final class AssocObjContainer extends ValueContainer{

        private Object k = null;
        private ITransientMap v = PersistentArrayMap.EMPTY.asTransient();

        @Override
        public void clear(){
            v = PersistentArrayMap.EMPTY.asTransient();
        }

        @Override
        public final void append(Object val) {
            if(k != null) {
                v = v.assoc(k, val);
                k = null;
            }else
                k = val;
        }

        @Override
        public final Object getValue(){
            return v.persistent();
        }

    }
    public static final class ArrayContainer extends ValueContainer{
        private ITransientCollection v = PersistentVector.EMPTY.asTransient();

        @Override
        public void clear(){
            v = PersistentVector.EMPTY.asTransient();
        }

        @Override
        public final void append(Object val) {
            v = v.conj(val);
        }

        @Override
        public final Object getValue() {
            return v.persistent();
        }
    }

}
