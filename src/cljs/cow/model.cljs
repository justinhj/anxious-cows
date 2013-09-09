(ns cow.model
  (:require [cow.math :as math]))

(def cow-count 10)
(def max-starting-velocity 0.01)
(def max-turn-degrees 180)
(def cow-id (atom 0))

(defn random-cow []
  (let [random-velocity (fn [] (- max-starting-velocity (rand (* 2 max-starting-velocity))))
        random-position (fn [] (math/rand-normal 0 1))
        cow (atom {
                   :id (swap! cow-id inc)
                   :anxiety (rand)
                   :velocity [(random-velocity) (random-velocity)]
                   :pos [(random-position) (random-position)]
                   :self-differentiation (rand)
                   })]
    cow))

(def cows (repeatedly cow-count random-cow))

(defn hit-fence? 
  ([cow] (hit-fence? ((:pos cow) 0) ((:pos cow) 1)))
  ([x y] (every? #(> (Math/abs %) 1) (vector x y))))


(defn perturb-vector [velocity heading]
  (let [polar (math/rect-to-polar velocity)
        perturbed-polar (assoc polar 0 (+ (polar 0) heading))]
    ; (.log js/console (str (polar-to-rect polar) " | " velocity))
    ; Disable random walking for now
    ; (polar-to-rect perturbed-polar)))
    velocity))


(defn random-walk [cow]
  (let [velocity (:velocity cow)
        vx (velocity 0)
        vy (velocity 1)
        random-x (math/rand-normal vx (* 2 max-starting-velocity))
        random-y (math/rand-normal vy (* 2 max-starting-velocity))
        heading (Math/atan (/ vy vx))
        wander (* (math/rand-normal 0 (math/degrees-to-radians max-turn-degrees)))]
    (perturb-vector (:velocity cow) wander)))

(defn new-cow-velocity [cow]
  (let [velocity (:velocity cow)
        pos (:pos cow)
        negate (fn [vec n] (assoc vec n (- (vec n))))]
    (cond 
      (> (Math/abs (pos 0)) 0.97) (negate velocity 0)
      (> (Math/abs (pos 1)) 0.97) (negate velocity 1)
      :else (random-walk cow))))

(defn new-cow-anxiety [cows cow] 
  (* (:anxiety cow) 0.99))

(defn sim-cows [cows]
  (doseq [cow-atom cows]
    (let [cow @cow-atom
          new-velocity (new-cow-velocity cow)
          new-pos (vec (map + new-velocity (:pos cow)))
          new-anxiety (new-cow-anxiety cows cow)]
      (swap! cow-atom assoc 
             :anxiety new-anxiety
             :pos new-pos 
             :velocity new-velocity))))



              
