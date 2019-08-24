(ns rcpu-compiler.core
  (:require [clojure.tools.reader.edn :as edn])
)

(defn read-all
  [input]
  ; TODO check that read is safe
  (take-while #(not= % :eof) (repeatedly #(read input false :eof))))

(defn data-name
  [name]
  (format ".%s" name)
)

(defn label-name
  [name]
  (format "%s:" name)
)

(def registers [:A :B :C :D])

(defn register-name
  [reg]
  (name reg)
)

(defn generate-data-record
  [data-record]
  (let [name (label-name (data-record :name))]
    (cond
      (= (data-record :type) :string) (format "%s string '%s'" name (data-record :value))
      (= (data-record :type) :number) (format "%s %d" name (data-record :value))
      (= (data-record :type) :allocate) (format "%s allocate %d" name (data-record :size))
      :else (throw (ex-info "Unknown data record for .data section"))

    )
  )
)

(defn prelude
  [main-label data-collection used-functions]
  (let [data-section (clojure.string/join "\n" (map generate-data-record data-collection))]
    (format ".data\n%s%\n.main\n.text\n.global %s\n" data-section (label-name main-label))
  )
)

(defn long-str [& strings] (clojure.string/join "\n" strings))

(def add-builtin (long-str
  (label-name "add-builtin")
  "ADD A, B"
  "RET"
))

(def mul-builtin (long-str
  (label-name "mul-builtin")
  "MUL A, B"
  "RET"
))

(defn compile-statement
  [statement destination-register]
  (cond
    (?number statement) (format "LDV16 %s, %d\n" (register-name destination-register) statement)
    (?list statement) (do
      ; TODO save this somewhere
      (map compile-statement arguments registers)
      ; add call to function here
      (long-str
        ; TODO find label to call
        "CAL %s"
        ; move result to destination register
        (unless (= destination-register :A) (format "MOV %s, A" (register-name destination-register)))
      )
    )
  )
  (when (> (count arguments) 3) (throw (ex-info "Function has more than 3 arguments, unsupported for now")))
  (map compile-statement arguments registers)
  (println "yeet")
)

(defn -main
  [filename & args]
  (with-open [in (java.io.PushbackReader. (clojure.java.io/reader filename))]
    (let [sourcecode (read-all in)]
      (println sourcecode)
      (println (map compile-statement sourcecode))
    )
  )
)
