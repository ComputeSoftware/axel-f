(ns axel-f.excel.utils)

(defmacro for-map
  "Like 'for' for building maps. Same bindings except the body should have a
  key-expression and value-expression. If a key is repeated, the last
  value (according to \"for\" semantics) will be retained.
  (= (for-map [i (range 2) j (range 2)] [i j] (even? (+ i j)))
     {[0 0] true, [0 1] false, [1 0] false, [1 1] true})
  An optional symbol can be passed as a first argument, which will be
  bound to the transient map containing the entries produced so far."
  ([seq-exprs key-expr val-expr]
   `(for-map ~(gensym "m") ~seq-exprs ~key-expr ~val-expr))
  ([m-sym seq-exprs key-expr val-expr]
   `(let [m-atom# (atom (transient {}))]
      (doseq ~seq-exprs
        (let [~m-sym @m-atom#]
          (reset! m-atom# (assoc! ~m-sym ~key-expr ~val-expr))))
      (persistent! @m-atom#))))

(defn map-vals
  "Build map k -> (f v) for [k v] in map, preserving the initial type"
  [f m]
  (cond
    (sorted? m)
    (reduce-kv (fn [out-m k v] (assoc out-m k (f v))) (sorted-map) m)
    (map? m)
    (persistent! (reduce-kv (fn [out-m k v] (assoc! out-m k (f v))) (transient {}) m))
    :else
    (for-map [[k v] m] k (f v))))
