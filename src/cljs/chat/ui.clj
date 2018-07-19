(ns chat.ui)

(defmacro defui [sym m args & bodies]
  `(defn ~sym ~args
     (reagent.core/create-class
       (assoc ~m :reagent-render (fn ~args ~@bodies)))))
