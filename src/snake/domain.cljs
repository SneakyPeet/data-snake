(ns snake.domain)


(defn- new-game [board-size]
  {:board-size board-size
   :dead? false
   :win? false
   :snake #queue []})


(defn- position [x y] [x y])


(defn- start-position [game x y]
  (update game :snake conj (position x y)))


(defn- change-direction [game direction]
  (assoc game :direction direction))


(defn- all-spaces [{:keys [board-size]}]
  (for [x (range board-size)
        y (range board-size)]
    [x y]))


(defn- open-spaces [{:keys [snake] :as game}]
  (vec (clojure.set/difference (set (all-spaces game)) (set snake))))


(defn- add-food [game]
  (let [possible-spaces (open-spaces game)]
    (if (empty? possible-spaces)
      (assoc game :food nil)
      (let [i (rand-int (count possible-spaces))
            food-position (nth possible-spaces i)]
        (assoc game :food food-position)))))


(defn- head-position [game]
  (first (:snake game)))


(defmulti move-position (fn [direction position] direction))
(defmethod move-position :up [_ [x y]] [x (dec y)])
(defmethod move-position :down [_ [x y]] [x (inc y)])
(defmethod move-position :left [_ [x y]] [(dec x) y])
(defmethod move-position :right [_ [x y]] [(inc x) y])


(defn- out-of-bounds? [{:keys [board-size] :as game}]
  (let [[x y] (head-position game)]
    (or (< x 0) (< y 0) (>= x board-size) (>= y board-size))))


(defn- ate-self? [{:keys [snake] :as game}]
  (let [head (head-position game)
        positions (group-by identity snake)]
    (-> (get positions head)
        count
        (> 1))))


(defn move [game direction]
  (let [current-head-position (head-position game)
        next-head-position (move-position direction current-head-position)
        eat? (= next-head-position (:food game))
        next-game
        (cond-> game
          true (change-direction direction)
          true (update :snake #(conj % next-head-position))
          (not eat?) (update :snake #(pop %))
          eat? add-food)]
    (assoc next-game
           :dead? (or (out-of-bounds? next-game) (ate-self? next-game))
           :win? (empty? (open-spaces next-game)))))


(defn start-game [board-size start-x start-y direction]
  (-> (new-game board-size)
      (start-position start-x start-y)
      (change-direction direction)
      add-food))
