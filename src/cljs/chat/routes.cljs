(ns chat.routes
  (:require-macros [secretary.core :refer [defroute]])
  (:import goog.History)
  (:require
    [secretary.core :as secretary]
    [goog.events :as gevents]
    [goog.history.EventType :as EventType]
    [re-frame.core :as re-frame]
    [chat.events :as events]))


(defn hook-browser-navigation! []
  (doto (History.)
    (gevents/listen
      EventType/NAVIGATE
      (fn [event]
        (secretary/dispatch! (.-token event))))
    (.setEnabled true)))

(defn app-routes []
  (secretary/set-config! :prefix "#")
  ;; --------------------
  ;; define routes here
  (defroute "/" []
            (re-frame/dispatch [::events/set-route :sign-in]))

  (defroute "/chat/:id" {id :id}
            (re-frame/dispatch [::events/set-route :chat {:id id}]))

  (defroute "/me" []
            (re-frame/dispatch [::events/set-route :me]))

  (defroute "/users/:id" {id :id}
            (re-frame/dispatch [::events/set-route :other {:id id}]))


  ;; --------------------
  (hook-browser-navigation!))
