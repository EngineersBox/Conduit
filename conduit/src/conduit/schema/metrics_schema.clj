(ns conduit.schema.metrics-schema
  (:require
    [riemann.streams :refer]
    [riemann.folds :refer]
  )
)

(defn handler "test" [& _children]
  (riemann.folds/smap riemann.folds/maximum [])
)
