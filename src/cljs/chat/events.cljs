(ns chat.events
  (:require
    [re-frame.core :as re-frame]
    [chat.db :as db]
    [day8.re-frame.tracing :refer-macros [fn-traced defn-traced]]
    [chat.firebase :as fb]
    [goog.object :as gobj]
    [cljs.reader :as reader]
    [secretary.core :as secretary]))


(re-frame/reg-event-fx
  ::initialize-db
  (fn-traced [_ _]
    {:db db/default-db
     :ls {:op       :get
          :key      "chat/user"
          :dispatch ::user-read-ok}}))

(re-frame/reg-event-fx
  ::user-read-ok
  (fn-traced [db [_ user]]
    {:db       (assoc db :user user)
     :navigate "#/chat/clj-group"}))

(re-frame/reg-event-db
  ::set-route
  (fn-traced [db [_ id params]]
    (assoc db :route {:id id :params params})))

(re-frame/reg-event-fx
  ::auth-ok
  (fn-traced [db [_ resp]]
    (let [p (gobj/getValueByKeys resp "additionalUserInfo" "profile")
          user {:username  (gobj/get p "login")
                :bio       (gobj/get p "bio")
                :full-name (gobj/get p "full-name")
                :avatar    (gobj/get p "avatar_url")}]
      {:ls       {:data user
                  :key  "chat/user"
                  :op   :set}
       :db       (assoc db :user user)
       :navigate "#/chat/clj-group"})))

(re-frame/reg-fx
  :navigate
  (fn [path _]
    (set! (.. js/location -hash) path)))

(re-frame/reg-fx
  :ls
  (fn [{:keys [data key op dispatch]}]
    (case op
      :set (.setItem js/localStorage key (pr-str data))
      :get (->> (.getItem js/localStorage key)
                reader/read-string
                (vector dispatch)
                (re-frame/dispatch))
      nil)))

(re-frame/reg-event-db
  ::auth-failed
  (fn-traced [db [_ err]]
    db))

(re-frame/reg-event-fx
  ::sign-in
  (fn [_ _]
    {:firebase {:op         :auth
                :on-success ::auth-ok
                :on-fail    ::auth-failed}}))

(re-frame/reg-event-fx
  ::send-message
  (fn [_ [_ id msg user]]
    {:firebase {:op   :send-msg
                :room id
                :payload
                      {:user user
                       :time (-> (js/Date.) .toUTCString)
                       :body msg
                       :uid  ""}}}))


(defmulti firebase (fn [m] (:op m)))

(defmethod firebase :auth [{:keys [op on-success on-fail]}]
  (-> (fb/sign-in-with-github)
      (.then #(re-frame/dispatch [on-success %]))
      (.catch #(re-frame/dispatch [on-fail %]))))

(defmethod firebase :send-msg [{:keys [room payload]}]
  (fb/send-msg! room payload))

(re-frame/reg-fx
  :firebase
  (fn [effect]
    (firebase effect)))

(re-frame/reg-event-db
  ::on-messages
  (fn-traced [db [_ id messages]]
    (assoc-in db [:messages id] messages)))

(comment
  (re-frame/dispatch [::sign-in]))

