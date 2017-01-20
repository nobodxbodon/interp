(import "/utils.l")

(export create (lambda [name] (let [[self [name]]] (lambda [] (apply (1st #args) (insert (cdr #args) 1 self))))))

(export f-set (lambda [o foo] (set o 1 foo)))

(export f-get (lambda [o] (get o 1)))

; (define foo (create "foo"))
; (foo f-get)
; (foo f-set 10)
; (foo f-get)