(ns snake.engine
  (:require [cljs.spec.alpha :as s]))


(s/def :execute/result (s/coll-of map? :min-count 1))
(s/def :execute/new-state map?)
(s/def :action/type keyword?)
(s/def :action/execute fn?)
(s/def :execute/action (s/keys :req-un [:action/type
                                        :action/execute]))
(s/def :execute/actions (s/coll-of :execute/action))


(defn- throw-invalid [action-type spec data]
  (when-not (s/valid? spec data)
    (throw (ex-info (str "Invalid Result for action " action-type " spec " spec)
                    (s/explain-data spec data)))))


(defmulti execute (fn [type game-state] type))


(defn game [game-state possible-actions]
  {:possible-actions possible-actions
   :game-state game-state})


(defn result [game-state & actions]
  (conj actions game-state))


(defn action
  [type game-state]
  {:type type
   :execute
   (fn []
     (let [result                (execute type game-state)
           _                     (throw-invalid type :execute/result result)
           [new-state & actions] result
           actions               (or actions [])]
       (throw-invalid type :execute/new-state new-state)
       (throw-invalid type :execute/actions actions)
       (game
        (-> game-state
            (merge new-state)
            (assoc :last-action type))
        actions)))})
