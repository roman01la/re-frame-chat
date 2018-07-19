(ns chat.firebase)

(def fb js/firebase)

(defn create-provider []
  (let [p (.. fb -auth -GithubAuthProvider)]
    (p.)))

(defn sign-in-with-popup [provider]
  (-> (.auth fb)
      (.signInWithPopup provider)))

(defn sign-in-with-github []
  (sign-in-with-popup (create-provider)))

(defn connect-db []
  (.database fb))

(defn on-messages [room-ref cb]
  (.on room-ref "value" #(-> (.val %)
                             (js->clj :keywordize-keys true)
                             vals
                             cb)))

(defn subscribe-room [room cb]
  (let [db (connect-db)]
    (-> (.ref db (str "rooms/" room))
        (on-messages cb))))

(defn send-msg! [room msg]
  (let [db (connect-db)]
    (-> (.ref db (str "rooms/" room))
        (.push)
        (.set (clj->js msg)))))

