(define fibonacci (lambda [n] 
    ((lambda [a b i] (cond
        [(eq i n) []]
        [#true ((lambda [atom L] (insert L 1 atom)) 
            a 
            (#lambda b (add a b) (add i 1))
        )]
    )) 0 1 0)
))

(export main (lambda [] (progn
    (output "Evaluate Fibonacci%n")
    (fibonacci 10)
)))

==============================================

Evaluate Fibonacci

System finished with [0 1 1 2 3 5 8 13 21 34]