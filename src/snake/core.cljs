(ns snake.core
  (:require [play-cljs.core :as p]
            [snake.game :as snake]))

(enable-console-print!)

;;;;

(defn scale
  ([] (scale 1))
  ([x] (* x 10)))

(defn make-game [state]
  (let [s (scale (get-in state [:domain :game-state :board-size]))]
    (p/create-game s s)))


(defonce *state (atom {:domain snake/new-game}))


(defonce game (make-game @*state))


(defn draw-board [game-state]
  (let [s (scale (:board-size game-state))]
    [:fill {:color "black"}
     [:rect {:x 0 :y 0 :width s :height s}]]))


(defn draw-food [game-state]
  (when-let [food (:food game-state)]
    (let [[x y] food
          size (scale)
          x (* x size)
          y (* y size)]
      [:fill {:color "red"}
       [:rect {:x x :y y :width size :height size}]])))


(defn draw-snake [game-state]
  (let [snake (:snake game-state)
        size (scale)]
    (->> snake
         (map
          (fn [[x y]]
            [:fill {:color "white"}
             [:rect {:x (* x size) :y (* y size) :width size :height size}]])))))


(defn draw-win-state [game-state]
  [:fill {:color "orange"}
   [:text
    {:value (str "Score: " (dec (count (:snake game-state)))),
     :x 0,
     :y 18,
     :size 16,
     ;:font "Georgia",
     :color "orange"}]])


(defn stop! []
  (when-let [timeout-id (get @*state :timeout-id)]
    (prn "*** STOP")
    (js/clearInterval timeout-id)))


(defn start! []
  (prn "*** START")
  (swap! *state assoc :domain snake/new-game)
  (swap! *state update :timeout-id
         (fn [_] (js/setInterval
                  (fn []
                    (swap! *state update :domain
                           (fn [{:keys [possible-actions game-state] :as domain}]
                             (when (or (:win? game-state) (:dead? game-state))
                               (stop!))
                             (prn domain)
                             (let [auto-move (->> (:direction game-state)
                                                  name
                                                  (str "move-")
                                                  keyword)
                                   moves (->> possible-actions
                                              (map (juxt :type :execute))
                                              (into {}))
                                   auto-move-fn (get moves auto-move)
                                   next-state (if auto-move-fn (auto-move-fn) domain)]
                               next-state
                               ))))
                  200))))

(defn move! [direction]
  (let [moves (->> (get-in @*state [:domain :possible-actions])
                   (map (juxt :type :execute))
                   (into {}))
        move (get moves direction)]
    (when move
      (swap! *state assoc :domain (move)))))


(defn key-down [e]
  (let [key-code (.-keyCode e)
        space 32
        down 40
        left 37
        right 39
        up 38]
    (when (= key-code space) (stop!) (start!))
    (when (= key-code left) (move! :move-left))
    (when (= key-code right) (move! :move-right))
    (when (= key-code up) (move! :move-up))
    (when (= key-code down) (move! :move-down))))

(def main-screen
  (reify p/Screen

    (on-show [this]
      (start!))

    (on-hide [this]
      (stop!))

    (on-render [this]
      (let [{:keys [win? dead? snake] :as game-state} (get-in @*state [:domain :game-state])]
        (p/render game
                  [(draw-board game-state)
                   (draw-food game-state)
                   (draw-snake game-state)
                   (when (or win? dead?)
                     (draw-win-state game-state))])))))

(doto game
  (p/start)
  (p/listen "keydown" key-down)
  (p/set-screen main-screen)
  )

;;;;


(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
  (stop!)
  (start!)
)
