(ns chat.subs
  (:require
    [re-frame.core :as re-frame]
    [day8.re-frame.tracing :refer-macros [fn-traced defn-traced]]))

(re-frame/reg-sub
  ::name
  (fn [db]
    (:name db)))

(re-frame/reg-sub
  ::route
  (fn-traced [db _]
    (:route db)))

(re-frame/reg-sub
  ::messages
  (fn-traced [db [_ id]]
    (-> db :messages (get id))))

(re-frame/reg-sub
  ::me
  (fn-traced [db [_ id]]
    (-> db :user)))
