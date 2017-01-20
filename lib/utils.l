(export not-eq (define not-eq (lambda [] (not (apply eq #args)))))

(export car  (define car  (lambda [L]   (get L 1))))
(export cdr  (define cdr  (lambda [L]   (delete (copy L) 1))))
(export cons (define cons (lambda [o L] (insert (copy L) 1 o))))

(export kar  (define kar  (lambda [L]   (get L (length L)))))
(export kdr  (define kdr  (lambda [L]   (delete (copy L) (length L)))))
(export kons (define kons (lambda [L o] (insert (copy L) (inc (length L)) o))))

(export append (define append kons))
(export append-into (define append-into (lambda [L object] (insert L (inc (length L)) object))))

(export cadr (define cadr (lambda [L] (car (cdr L)))))
(export cddr (define cddr (lambda [L] (cdr (cdr L)))))
(export caar (define caar (lambda [L] (car (car L)))))

(export cadar (define cadar (lambda [L] (car (cdr (car L))))))

(export 1st (define 1st (lambda [L] (get L 1))))
(export 2nd (define 2nd (lambda [L] (get L 2))))
(export 3rd (define 3rd (lambda [L] (get L 3))))
(export 4th (define 4th (lambda [L] (get L 4))))
(export 5th (define 5th (lambda [L] (get L 5))))
(export 6th (define 6th (lambda [L] (get L 6))))
(export 7th (define 7th (lambda [L] (get L 7))))
(export 8th (define 8th (lambda [L] (get L 8))))
(export 9th (define 9th (lambda [L] (get L 9))))

(export inc (define inc (lambda [n] (add n 1))))
(export dec (define dec (lambda [n] (sub n 1))))

(export end-of (lambda [L] (inc (length L))))

(export part (define part (lambda [n L] (cond
    [(eq L []) []]
    [#true (cons (car-n n L) (#lambda n (cdr-n n L)))]
))))

(export car-n (define car-n (lambda [n L] (cond
    [(or (eq n 0) (eq L [])) []]
    [#true (cons (car L) (#lambda (dec n) (cdr L)))]
))))

(export cdr-n (define cdr-n (lambda [n L] (cond
    [(or (eq n 0) (eq L [])) L]
    [#true (#lambda (dec n) (cdr L))]
))))

(export rep (define rep (lambda [o i] (cond
    [(eq i 0) []]
    [#true (cons o (#lambda o (dec i)))]
))))

(export be-empty (define be-empty (lambda [L] (eq L []))))

(export exp  (lambda [x e] (apply * (rep x e))))
(export exp2 (lambda [x] (* x x)))
(export exp3 (lambda [x] (* x x x)))

(export default (lambda [value default-value] (if (eq value #none) default-value value)))

(export true  (define true  (lambda [] #true)))
(export false (define false (lambda [] #false)))
(export const (define const (lambda [o] o)))

(export complement (define complement (lambda [] (reduce 
    (lambda [_L __L] (unique (filter (lambda [atom] (not-in atom __L)) _L)))
    #args))))
    
(export union (define union (lambda [] (unique (apply merge #args)))))

(export intersection (define intersection (lambda [] (reduce 
    (lambda [_L __L] (unique (filter (lambda [atom] (in atom __L)) _L)))
    #args))))

(export reverse (define reverse (lambda [L] (cond
    [(eq L []) L]
    [#true (cons (kar L) (#lambda (kdr L)))]
))))

(export $ (define $ (lambda [fn] (lambda [] ((kar #args) (apply fn (kdr #args)))))))

;(export ~ (lambda [o chain-fn] (cond
;    [(eq chain-fn []) o]
;    [#true (#lambda ((car chain-fn) o) (cdr chain-fn))]
;)))

(export ~ (lambda [o L] (reduce (lambda [o Fn] (Fn o)) (merge [o] L))))

(export each (define each (lambda [Fn L] (cond
    [(eq L []) #none]
    [#true (progn (Fn (car L)) (#lambda Fn (cdr L)))]
))))

(export map (define map (lambda [Fn L] (cond
    [(eq L []) []]
    [#true (cons (Fn (car L)) (#lambda Fn (cdr L)))]
))))

(export reduce (define reduce (lambda [Fn L]
    ((lambda [n Fn L] (cond
        [(eq L []) n]
        [#true (#lambda (Fn n (car L)) Fn (cdr L))]
    )) (car L) Fn (cdr L))
)))

(export trav (define trav (lambda [Fn L] (cond
    [(eq L []) []]
    [(type-eq (car L) :list) (cons (#lambda Fn (car L)) (#lambda Fn (cdr L)))]
    [#true (cons (Fn (car L)) (#lambda Fn (cdr L)))]
))))

(export filter (define filter (lambda [Fn L] (cond
    [(eq L []) []]
    [(eq (Fn (car L)) #true) (cons (car L) (#lambda Fn (cdr L)))]
    [#true (#lambda Fn (cdr L))]
))))

(export validate (define validate (lambda [L Format] (progn
    (define atomic [:number :string :bool :none :exception :blob :handle])

    ((lambda [L Format] (cond
        [(in (type Format) atomic) (eq Format L)]
        [(type-eq Format :type) (eq (type L) Format)]
        [(type-eq Format :lambda) (Format L)]
        [(eq (type L) (type Format) :list) (cond
            [(eq (length L) (length Format) 0) #true]
            [(eq (length L) (length Format)) (and
                (#lambda (car L) (car Format))
                (#lambda (cdr L) (cdr Format))
            )]
            [#true #false]
        )]
        [#true #false]
    )) L Format)
))))

(export in (define in (lambda [o L] (cond
    [(eq L []) #false]
    [(eq (car L) o) #true]
    [#true (#lambda o (cdr L))]
))))

(export not-in (define not-in (lambda [o L] (not (in o L)))))

(export max (define max (lambda [L] (reduce (lambda [a b] (if (> a b) a b)) L))))

(export min (define min (lambda [L] (reduce (lambda [a b] (if (< a b) a b)) L))))

(export at (define at (lambda [e L] ((lambda [e L n] (cond
    [(eq L []) &NOT-FOUND]
    [(eq (car L) e) n]
    [#true (#lambda e (cdr L) (inc n))]
)) e L 1))))

(export unique (define unique (lambda [L] ((lambda [L C] (cond
    [(eq L []) C]
    [(not-in (car L) C) (#lambda (cdr L) (kons C (car L)))]
    [#true (#lambda (cdr L) C)]
)) L []))))

(export enum (lambda [L] ((lambda [i L] (cond
    [(eq L []) []]
    [#true (cons [i (car L)] (#lambda (inc i) (cdr L)))]
)) 1 L)))

(export slice (define slice (lambda [L start end] (cond
    [(> start end) []]
    [#true (cons (get L start) (#lambda L (inc start) end))]
))))

(export exclude (define exclude (lambda [L e]
    ((lambda [L] (cond
        [(eq L []) []]
        [(eq (car L) e) (#lambda (cdr L))]
        [#true (cons (car L) (#lambda (cdr L)))]
    )) L)
)))

(export merge (define merge (lambda [] (reduce (lambda [L1 L2] (cond
    [(eq [] L1) L2]
    [#true (cons (car L1) (#lambda (cdr L1) L2))]
)) #args))))

(export zip (define zip (lambda [] (reduce (lambda [L1 L2] (cond
    [(eq [] L2) []]
    [#true (cons (kons (car L1) (car L2)) (#lambda (cdr L1) (cdr L2)))]
)) (set #args 1 ((lambda [L] (if (eq L []) [] (cons [(car L)] (#lambda (cdr L))))) (car #args)))))))

(export flat (define flat (lambda [L] (cond
    [(eq L []) []]
    [(type-eq (car L) :list) (merge (#lambda (car L)) (#lambda (cdr L)))]
    [#true (cons (car L) (#lambda (cdr L)))]
))))

(export range (define range (lambda [] ((lambda [s e i] (cond
    [(if (> i 0) (> s e) (< s e)) []]
    [#true (cons s (#lambda (+ s i) e i))]
)) (get #args 1) (get #args 2) (if (length-eq #args 3) (get #args 3) 1)))))

(export take (define take (lambda [n Fn] ((lambda [i] (cond
    [(> i n) []]
    [#true (let [[_ (Fn)]] (if (eq _ &END) [] (cons _ (#lambda (inc i)))))]
)) 1))))

(export all (define all (lambda [L] (progn

    (assert (not-eq L []))

    ((lambda [L] (cond
        [(eq L []) #true]
        [(eq (car L) #false) #false]
        [#true (#lambda (cdr L))]
    )) L)
))))

(export any (define any (lambda [L] (progn

    (assert (not-eq L []))

    ((lambda [L] (cond
        [(eq L []) #false]
        [(eq (car L) #true) #true]
        [#true (#lambda (cdr L))]
    )) L)
))))

(export length-eq (define length-eq (lambda [L n] (eq (length L) n))))
(export type-eq (define type-eq   (lambda [o t] (eq (type o) t))))

(export println (define println (lambda [] (progn (apply output #args) (output "%n")))))

(export quick-sort (define quick-sort (lambda [L Fn] (cond
    [(<= (length L) 1) L]
    [#true (merge
        (#lambda (filter (lambda [i] (< (Fn i) (Fn (car L)))) (cdr L)) Fn)
        [(car L)]
        (#lambda (filter (lambda [i] (>= (Fn i) (Fn (car L)))) (cdr L)) Fn)
    )]
))))

(export qsort (define qsort (lambda [L] (quick-sort L const))))

(export dict-validate (define dict-validate (lambda [dict] (and
    (type-eq dict :list)
    (or
        (be-empty dict)
        (all (map (lambda [item] (validate item [:string true])) dict))
    )
))))

(export product (lambda [] ((lambda [L] (reduce (lambda [M N] (apply merge (map (lambda [m] (map (lambda [n] (append m n)) N)) M))) L)) (merge [(map (lambda [n] [n]) (car #args))] (cdr #args)))))
; (export product(lambda[]((lambda[L](reduce(lambda[M N](apply merge(map(lambda[m](map(lambda[n](append m n))N))M)))L))(merge[(map(lambda[n][n])(car #args))](cdr #args)))))
; product = ->(*args){ ->(_L){ _L.reduce(&->(_M, _N){ _M.map(&->(m){ _N.map(&->(n){ m.dup.push(n) }) }).reduce(&:+) }) }[[args[0].map(&->(n){[n]})] + args[1..-1]] }
; product=->(*args){->(_L){_L.reduce(&->(_M,_N){_M.map(&->(m){_N.map(&->(n){m.dup.push(n)})}).reduce(&:+)})}[[args[0].map(&->(n){[n]})]+args[1..-1]]}

(export assoc (export dict-get (define dict-get (lambda [L name]
    ((lambda [i] (cond
        [(> i (length L)) &NOT-FOUND]
        [(eq (car (get L i)) name) (get (get L i) 2)]
        [#true (#lambda (inc i))]
    )) 1)))))

(export dict-get-item (define dict-get-item (lambda [L name]
    ((lambda [i] (cond
        [(> i (length L)) &NOT-FOUND]
        [(eq (car (get L i)) name) (get L i)]
        [#true (#lambda (inc i))]
    )) 1))))

(export dict-set (define dict-set
    (lambda [L name value]
        ((lambda [i] (cond
            [(> i (length L)) &NOT-FOUND]
            [(eq (car (get L i)) name) (set (get L i) 2 value)]
            [#true (#lambda (inc i))]
        )) 1))))

(export dict-insert (define dict-insert
    (lambda [L name value]
        (insert L (inc (length L)) [name value]))))

(export dict-delete (define dict-delete (lambda [L name]
    ((lambda [i] (cond
        [(> i (length L)) &NOT-FOUND]
        [(eq (car (get L i)) name) (delete L i)]
        [#true (#lambda (inc i))]
    )) 1))))

(export dict-keys (define dict-keys (lambda [L]
    ((lambda [i] (cond
        [(> i (length L)) []]
        [#true (cons (car (get L i)) (#lambda (inc i)))]
    )) 1))))

(export super-map (define super-map (lambda [Fn L] (cond
    [(eq L []) []]
    [#true (cons (apply Fn (car L)) (#lambda Fn (cdr L)))]
))))

(export dict-values (define dict-values (lambda [L] ((lambda [i] (cond
    [(> i (length L)) []]
    [#true (cons (cadr (get L i)) (#lambda (inc i)))]
)) 1))))

; (export string-start-with (lambda [s prefix] (cond
    ; [(> (length prefix) (length s)) #false]
    ; [(= (length prefix) (length s)) (eq s prefix)]
    ; [(< (length prefix) (length s)) (eq (substr s 1 (length prefix)) prefix)]
; )))

; (export string-end-with (lambda [s suffix] ()))
; (export string-insert (lambda [] ()))
; (export string-cutout (lambda [] ()))
; (export string-split-by (lambda [] ()))
; (export string-join-with (lambda [] ()))
; (export string-capitalize (lambda [] ()))
; (export string-search (lambda [] ()))

(export to-chars (define to-chars (lambda [str] (cond
    [(length-eq str 0) []]
    [#true (cons (substr str 1 1) (#lambda (substr str 2 (length str))))]
))))

(export options (export dict-merge (lambda [permitted-options options] (progn

   ; (assert (and (dict-validate permitted-options) (dict-validate options)))
   ; (assert (not (be-empty permitted-options)))
   ; (assert (or (be-empty options) (all (map (let [[permitted-keys (dict-keys permitted-options)]] (lambda [key] (in key permitted-keys))) (dict-keys options)))))

    (map (let [[options-keys (dict-keys options)]] (lambda [option] (let [[key (car option)]] (if (in key options-keys) (dict-get-item options key) option)))) permitted-options)
))))

(define options (lambda [M N] (map (let [[NK (dict-keys N)]] (lambda [m] (let [k (car m)] (if (in k NK) (dict-get-item N k) m)))) M)))
; (定义 合并选项 (算法 [元选项 变选项] (迭代 (使 [[诸键 (取诸键 变选项)]] (算法 [选项] (使 [键 (取首项 选项)] (若 (有 键 诸键) (取项 变选项 键) 选项)))) 元选项)))

(export format (define format (lambda [] (apply concat ((lambda [S L] (cond
  [(eq S []) []]
  [#true (progn
    ;(println S)
    (if (and (eq (car S) "#") (if (length-eq S 1) #false (eq (cadr S) "s"))) (cons (car L) (#lambda (cddr S) (cdr L))) (cons (car S) (#lambda (cdr S) L)))
  )]
)) (to-chars (car #args)) (cdr #args))))))

(export wrap (define wrap (lambda [n] [n])))

(export average (define average (lambda [L] (let [[length (length L)]] (/ (apply + L) length)))))

(export ~filter (define ~filter (lambda [op] (lambda [L] (filter op L)))))
(export ~map (define ~map (lambda [op] (lambda [L] (map op L)))))
(export +: (define +: (lambda [n] (lambda [m] (+ n m)))))
(export *: (define *: (lambda [m] (lambda [n] (* m n)))))
(export even? (define even? (lambda [n] (= (% n 2) 0))))
(export odd? (define odd? (lambda [n] (not (even? n)))))
; (~ (range 1 10) [(~filter even?) (~map (*: 3)) (~map quote)])
