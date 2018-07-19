(ns chat.views
  (:require-macros [chat.ui :refer [defui]])
  (:require
    [re-frame.core :as re-frame]
    [chat.subs :as subs]
    [chat.events :as events]
    [reagent.core :as r]
    [chat.firebase :as fb]))


(defn button [attrs text]
  [:button.button attrs text])

(defn avatar [{:keys [src size]}]
  [:img.avatar
   {:src (or src "https://user-images.githubusercontent.com/6009640/31679076-dc7581c6-b391-11e7-87fe-a8fa89793c63.png")}])


(defn header [{:keys [left right title]}]
  [:div.header
   [:div.header-left left]
   [:div.header-title title]
   [:div.header-right right]])

(defn sign-in []
  [:div.screen.sign-in
   [button {:on-click #(re-frame/dispatch [::events/sign-in])}
    "Sign In with GitHub"]])

(defn message [{:keys [user uid time body]} me?]
  [:div.message {:class (if me? "me" "other")}
   [avatar {:src nil}]
   [:div.message-buble
    [:div.message-meta
     [:div.message-user (str "@" user)]
     [:div.message-time (-> (js/Date. time) .toLocaleString)]]
    [:div.message-text body]]])

(def msg (r/atom ""))

(defui chat
  {:component-did-mount
   (fn [] (fb/subscribe-room id #(re-frame/dispatch [::events/on-messages id %])))}
  [{:keys [id]}]
  (let [msgs @(re-frame/subscribe [::subs/messages id])
        {:keys [username]} @(re-frame/subscribe [::subs/me])]
    (println @msg)
    [:div.screen
     [header {:title "Clojure Learning Group"
              :right [avatar {}]}]
     [:div.content
      (for [msg msgs]
        [message msg (= username (:user msg))])]
     [:div.footer
      [:textarea.input
       {:value     @msg
        :on-change #(reset! msg (.. % -target -value))}]
      [button {:on-click #(re-frame/dispatch [::events/send-message id @msg username])}
       "Send"]]]))


(defn main-panel []
  (let [{:keys [id params]} @(re-frame/subscribe [::subs/route])]
    [:div
     (case id
       :sign-in [sign-in]
       :chat [chat params]
       :me "me"
       :other "other"
       "404")]))



