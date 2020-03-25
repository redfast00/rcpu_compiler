(ns rcpu-compiler.brainfuck
   (:require [instaparse.core :as insta])
)

(def brainfuck-parser
  (insta/parser
    (clojure.java.io/resource "brainfuck.bnf")
    :output-format :enlive))

(defn long-str [& strings] (clojure.string/join "\n" strings))

(def prelude (long-str
  ".data"
  ".sys_printf 0"
  ".sys_getc 2"
  ".stdin 0"
  ".format_str string '%c'"
  ".text"
  ".global main:"
  "main:"
  "LDV B, 0"
  "LDV16 C, end_of_program:"
  "LDV A, 16"
  "ADD C, A"
  "LDV A, 0"
))

(def end-of-program (long-str
  "HLT"
  "end_of_program:"
))

(defn rand-str [len]
  (apply str (take len (repeatedly #(char (+ (rand 26) 65))))))

(defn rand-label [type]
  (str type "_" (rand-str 20) ":")
)

(defn compile-main
  [node]
  (case (node :tag)
    :single (case (first (node :content))
      "+" "INC B"
      "-" "DEC B"
      ">" (long-str
        "LDP C, B"
        "INC C"
        "LDR B, C"
        )
      "<" (long-str
        "LDP C, B"
        "DEC C"
        "LDR B, C"
        )
      "," (long-str
        "LDV D, .stdin"
        "PSH D"
        "LDV D, .sys_getc"
        "PSH D"
        "SYS"
        "POP B"
        )
      "." (long-str
        "PSH B"
        "LDV16 D, .format_str"
        "PSH D"
        "LDV D, .sys_printf"
        "PSH D"
        "SYS"
      )
    )
    :jumppair (let [
      begin-label (rand-label "begin")
      end-label (rand-label "end")
      ]
      (long-str
        (str "LDV16 D, " begin-label)
        "LDV A, 255"
        "AND B, A"
        "LDV A, 0"
        "JLT B, D"
        (str "LDV16 D, " end-label)
        "JMR D"
        begin-label
        (clojure.string/join "\n" (map compile-main (node :content)))
        (str "LDV16 D, " begin-label)
        "LDV A, 255"
        "AND B, A"
        "LDV A, 0"
        "JLT B, D" ; jump if A < B (so 0 < B, B != 0)
        end-label
      )
    )
    (println node)
  )
)

(defn compile-brainfuck
  [input]
  (let [ast (brainfuck-parser input)]
    (long-str
      prelude
      (clojure.string/join "\n" (map compile-main ast))
      end-of-program
    )
  )
)

(defn remove-non-brainfuck
  [s]
  (clojure.string/replace s #"[^\[\]+\-,.<>]" "")
)

(defn -main
  [& args]
  (when (empty? args)
    (println "Needs a file to compile")
    (System/exit 1)
  )
  (print (compile-brainfuck (remove-non-brainfuck (slurp (first args)))))
)
