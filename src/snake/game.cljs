(ns snake.game
  (:require [snake.domain :as d]
            [snake.engine :as e]))


(defn- move-action [direction game-state]
  (e/action direction game-state))


(defn- execute-move [game-state direction & possible-directions]
  (let [next-state (d/move game-state direction)]
    (if (or (:dead? next-state) (:win? next-state))
      (e/result next-state)
      (apply e/result
             (into [next-state]
                   (->> possible-directions
                        (map #(move-action % next-state))))))))


(defmethod e/execute :move-up [_ game-state]
  (execute-move game-state :up :move-up :move-left :move-right))


(defmethod e/execute :move-down [_ game-state]
  (execute-move game-state :down :move-down :move-left :move-right))


(defmethod e/execute :move-left [_ game-state]
  (execute-move game-state :left :move-left :move-up :move-down))


(defmethod e/execute :move-right [_ game-state]
  (execute-move game-state :right :move-right :move-up :move-down))


(defn new-game []
  (let [state (d/start-game 20 10 0 :down)
        possible-actions [(move-action :move-down state)
                          (move-action :move-left state)
                          (move-action :move-right state)]]
    (e/game state possible-actions)))
