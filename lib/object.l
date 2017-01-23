(import "/utils.l")

(export create (lambda [name bb] (let [[self [name bb]]] (lambda [] (apply (1st #args) (insert (cdr #args) 1 self))))))

(export f-set (lambda [o foo] (set o 1 foo)))
(export f-set2 (lambda [o foo] (set o 2 foo)))

(export f-get (lambda [o] (get o 1)))

; (define foo (create "foo"))
; (foo f-get)
; (foo f-set 10)
; (foo f-get)
; (super-each foobar [[f-set "10"] [f-set2 "20"]])
