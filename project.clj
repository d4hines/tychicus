(defproject tychicus "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [io.forward/clojure-mail "1.0.7"]
                 [javax.mail/mail "1.4.3"]
                 ;; May not need postal... don't like the docs.
                 [com.draines/postal "2.0.2"]
                 [com.taoensso/timbre "4.10.0"]
                 [environ "1.1.0"]]
  :main ^:skip-aot tychicus.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
           
