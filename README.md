# Snake Clone in Clojurescript using play-cljs

see here https://sneakypeet.github.io/data-snake/

### Goals

* Entire Game should be self contained in a map.
* Possible actions should be provided to the consumer (ui or whatever). 
* The ui should only have to call the provided action's execute function.

### Game state example
```
{
 ;; the only way progress is by executing one of these actions
 :possible-actions 
 [{:type :move-down, :execute #object[Function]}
  {:type :move-left, :execute #object[Function]}
  {:type :move-right, :execute #object[Function]}],
 
 :game-state
 {:board-size 20,
  :dead? false,
  :win? false, 
  :snake #queue [[10 0]], 
  :direction :down, 
  :food [5 3]}}
```
