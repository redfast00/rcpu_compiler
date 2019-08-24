(defproject rcpu_compiler "0.1.0-SNAPSHOT"
  :description "A compiler for RCPU"
  :url "http://example.com/FIXME"
  :license {:name "MIT"
            :url "https://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.10.0"] [instaparse "1.4.10"]]
  :main ^:skip-aot rcpu-compiler.brainfuck
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
