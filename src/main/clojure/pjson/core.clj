(ns pjson.core
  (:import [pjson PJSON StringUtil ToJSONString JSONGenerator KeywordisingListener JSONListener]
           (java.nio.charset Charset)
           (java.util Map Collection)
           (clojure.lang Seqable IPersistentMap))
  (:gen-class))

(defonce ^Charset DEFAULT_CHARSET (StringUtil/DEFAULT_CHAR_SET))

(defprotocol JSONToString
  "Convert an ordinary java/clojure object to a JSON compcatible String"
  (write-str [obj]))

(defprotocol JSONParser
  "Convert a json string to clojure objects lazily"
  (read-str [obj] [obj ^Charset charset] [obj ^Charset charset ^Long from ^Long len])
  (read-str-keywordise [obj] [obj ^Charset charset] [obj ^Charset charset ^Long from ^Long len]))

;compatibility functions for easy swap between other libraries
;note that only arity one support is added, use read-str for multi arity support
(defn asObj [obj] (read-str obj))
(defn parse-string [obj] (read-str obj))
(defn asString [obj] (write-str obj))
(defn generate-string [obj] (write-str obj))

(defn get-charset [n]
  (Charset/forName (str n)))

(defn- bts->json
  ([^"[B" bts]
   (bts->json DEFAULT_CHARSET bts))
  ([^Charset charset ^"[B" bts]
   (PJSON/defaultParse charset bts))
  ([^Charset charset ^"[B" bts ^Long from ^Long len]
   ;defaultParse(final Charset charset, final byte[] bts, final int start, final int len)
   (PJSON/defaultParse charset bts (int from) (int len)))
  ([^Charset charset ^"[B" bts ^Long from ^Long len ^JSONListener list]
   ;defaultParse(final Charset charset, final byte[] bts, final int start, final int len)
   (PJSON/parse charset bts (int from) (int len) list)))

(defn- bts->lazy-json
  ([^"[B" bts]
   (bts->lazy-json DEFAULT_CHARSET bts))
  ([^Charset charset ^"[B" bts]
   (PJSON/defaultLazyParse charset bts))
  ([^Charset charset ^"[B" bts ^Long from ^Long len]
   (PJSON/defaultLazyParse charset bts (int from) (int len)))
  ([^Charset charset ^"[B" bts ^Long from ^Long len ^JSONListener list]
   (PJSON/lazyParse charset bts (int from) (int len) list)))


(extend-protocol JSONParser
  String
  (read-str
    ([obj] (read-str (StringUtil/toCharArray ^String obj)))
    ([obj charset] (read-str (StringUtil/toCharArray ^String obj) ^Charset charset))
    ([obj charset from len] (read-str (StringUtil/toCharArray ^String obj) ^Charset charset ^Long from ^Long len)))
  (read-str-keywordise
    ([obj] (read-str-keywordise (StringUtil/toCharArray ^String obj)))
    ([obj charset] (read-str-keywordise (StringUtil/toCharArray ^String obj) ^Charset charset))
    ([obj charset from len] (read-str-keywordise (StringUtil/toCharArray ^String obj) ^Charset charset ^Long from ^Long len))))

(extend-protocol JSONParser
  (Class/forName "[C")
  (read-str
    ([^"[C" obj]
       (PJSON/defaultLazyParse DEFAULT_CHARSET ^"[C" obj))
    ([^"[C" obj charset]
       (PJSON/defaultLazyParse ^Charset charset ^"[C" obj))
    ([^"[C" obj charset ^Long from ^Long len]
       (PJSON/defaultLazyParse ^"[C" obj (int from) (int len))))
  (read-str-keywordise
    ([^"[C" obj]
       (read-str-keywordise ^"[C" obj DEFAULT_CHARSET))
    ([^"[C" obj charset]
       (read-str-keywordise ^"[C" obj ^Charset charset 0 (alength ^"[C" obj)))
    ([^"[C" obj charset ^Long from ^Long len]
       (PJSON/lazyParse ^"[C" obj (int from) (int len) (new KeywordisingListener)))))

(extend-protocol JSONParser
  (Class/forName "[B")
  (read-str
    ([^"[B" obj]
     (bts->lazy-json obj))
    ([^"[B" obj charset]
     (bts->lazy-json charset obj))
    ([^"[B" obj charset from len]
     (bts->lazy-json charset obj from len)))
  (read-str-keywordise
    ([^"[B" obj]
     (read-str-keywordise obj DEFAULT_CHARSET))
    ([^"[B" obj charset]
     (read-str-keywordise obj charset 0 (alength ^"[B" obj)))
    ([^"[B" obj charset from len]
     (bts->lazy-json charset obj from len (new KeywordisingListener)))))


(extend-protocol JSONToString
  ToJSONString
  (write-str [^ToJSONString this]
    (.toString this))
  IPersistentMap
  (write-str [^IPersistentMap this]
    (JSONGenerator/forObj this))
  Map
  (write-str [^Map this]
    (JSONGenerator/forObj this))
  Collection
  (write-str [^Collection this]
    (JSONGenerator/forObj this))
  Seqable
  (write-str [^Seqable this]
    (JSONGenerator/forObj this))
  Object
  (write-str [^Object this]
    (JSONGenerator/forObj this)))
