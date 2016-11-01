(export not-eq (define not-eq (lambda [] (not (apply eq #args)))))

(export car  (define car  (lambda [L]   (get L 1))))
(export cdr  (define cdr  (lambda [L]   (delete (copy L) 1))))
(export cons (define cons (lambda [o L] (insert (copy L) 1 o))))

(export kar  (define kar  (lambda [L]   (get L (length L)))))
(export kdr  (define kdr  (lambda [L]   (delete (copy L) (length L)))))
(export kons (define kons (lambda [L o] (insert (copy L) (inc (length L)) o))))

(export append kons)

(export cadr (define cadr (lambda [L] (car (cdr L)))))

(export 1st (define 1st (lambda [L] (get L 1))))
(export 2nd (define 2nd (lambda [L] (get L 2))))
(export 3rd (define 3rd (lambda [L] (get L 3))))
(export 4th (define 4rd (lambda [L] (get L 4))))
(export 5th (define 4rd (lambda [L] (get L 5))))
(export 6th (define 4rd (lambda [L] (get L 6))))
(export 7th (define 4rd (lambda [L] (get L 7))))
(export 8th (define 4rd (lambda [L] (get L 8))))
(export 9th (define 4rd (lambda [L] (get L 9))))

(export inc (define inc (lambda [n] (add n 1))))
(export dec (define dec (lambda [n] (sub n 1))))

(export default (lambda [value default-value] (if (eq value #none) default-value value)))

(export true  (lambda [] #true))
(export false (lambda [] #false))
(export const (define const (lambda [o] o)))

(export reverse (define reverse (lambda [L] (cond
    [(eq L []) L]
    [#true (cons (kar L) (#lambda (kdr L)))]
))))

(export $ (define $ (lambda [fn] (lambda [] ((kar #args) (apply fn (kdr #args)))))))

(export ~ (define ~ (lambda [] ((lambda [o chain-fn] (cond
    [(eq chain-fn []) o]
    [#true (#lambda ((car chain-fn) o) (cdr chain-fn))]
)) (car #args) (cdr #args)))))

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

(export in (define in (lambda [o L] (cond
    [(eq L []) #false]
    [(eq (car L) o) #true]
    [#true (#lambda o (cdr L))]
))))

(export not-in (define not-in (lambda [o L] (not (in o L)))))

(export max (define max (lambda [L]
    ((lambda [n L] (cond
        [(eq L []) n]
        [(> (car L) n) (#lambda (car L) (cdr L))]
        [#true (#lambda n (cdr L))]
    )) (car L) (cdr L))
)))

(export min (define min (lambda [L]
    ((lambda [n L] (cond
        [(eq L []) n]
        [(< (car L) n) (#lambda (car L) (cdr L))]
        [#true (#lambda n (cdr L))]
    )) (car L) (cdr L))
)))

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

(export slice (define slice (lambda [L start end] (cond
    [(> start end) []]
    [#true (cons (get L start) (#lambda L (inc start) end))]
))))

(export exclude (define exclude (lambda [e L]
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

(export take (define take (lambda [n fn] ((lambda [i] (cond
    [(> i n) []]
    [#true (cons (fn) (#lambda (inc i)))]
)) 1))))

(export all (define all (lambda [L] (apply and L))))
(export any (define any (lambda [L] (apply or  L))))

(export length-eq (define length-eq (lambda [L n] (eq (length L) n))))
(export type-eq (define type-eq   (lambda [o t] (eq (type o) t))))

(define quick-sort (lambda [L Fn] (cond
    [(<= (length L) 1) L]
    [#true (merge
        (#lambda (filter (lambda [i] (< (Fn i) (Fn (car L)))) (cdr L)) Fn)
        [(car L)]
        (#lambda (filter (lambda [i] (>= (Fn i) (Fn (car L)))) (cdr L)) Fn)
    )]
)))

(export qsort (define qsort (lambda [L] (quick-sort L const))))

(export println (define println (lambda [] (progn (apply output #args) (output "%n")))))