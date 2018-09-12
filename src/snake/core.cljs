(ns snake.core
  (:require [play-cljs.core :as p]
            [snake.game :as snake]
            [clojure.string :as string]))

(enable-console-print!)

;;;;

(defn scale
  ([] (scale 1))
  ([x] (* x 15)))


(defn make-game [state]
  (let [s (scale (get-in state [:domain :game-state :board-size]))]
    (p/create-game (* 2 s) s)))


(def speed 1000)

(defonce *state (atom {:domain (snake/new-game)}))


(defonce game (make-game @*state))


(defn draw-board [domain]
  (let [game-state (:game-state domain)
        s (scale (:board-size game-state))]
    [:rect {:x 0 :y 0 :width (* 2 s) :height s}
     [:fill {:color "black"}
      [:rect {:x 0 :y 0 :width s :height s}]]
     [:fill {:color "black"}
      [:text {:x (+ 5 s) :y 12
              :size 12
              :value "Arrows to move, space to reset"}]]
     [:fill {:color "black"}
      [:text {:x (+ 5 s) :y 24
              :size 10
              :value "Game state"}]]
     [:fill {:color "grey"}
      (let [lines
            (-> (pr-str game-state)
                (string/split #", "))]
        (->> lines
             (map-indexed
              (fn [i l]
                [:text {:x (+ 5 s) :y (+ 5 (* (+ 2 (inc i)) 10))
                        :size 8
                        :value l}]))))]
     [:fill {:color "black"}
      [:text {:x (+ 5 s (/ s 2)) :y 24
              :size 10
              :value "Possible Moves"}]]
     [:fill {:color "grey"}
      (let [lines
            (-> (pr-str (map :type (:possible-actions domain)))
                (string/split #" "))]
        (->> lines
             (map-indexed
              (fn [i l]
                [:text {:x (+ 5 s (/ s 2)) :y (+ 5 (* (+ 2 (inc i)) 10))
                        :size 8
                        :value l}]))))]]))


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
    [(->> snake
          (map
           (fn [[x y]]
             [:fill {:color "white"}
              [:rect {:x (* x size) :y (* y size) :width size :height size}]])))
     (let [head (last snake)
           [x y] head]
       [:fill {:color "green"}
        [:rect {:x (* x size) :y (* y size) :width size :height size}]])]))


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
  (swap! *state assoc :domain (snake/new-game))
  (swap! *state update :timeout-id
         (fn [_] (js/setInterval
                  (fn []
                    (swap! *state update :domain
                           (fn [{:keys [possible-actions game-state] :as domain}]
                             (when (or (:win? game-state) (:dead? game-state))
                               (stop!))
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
                  speed))))

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
      (let [{:keys [game-state possible-actions] :as domain} (get @*state :domain)
            {:keys [win? dead? snake]} game-state]
        (p/render game
                  [(draw-board domain)
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
