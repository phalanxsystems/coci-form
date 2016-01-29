(set-env!
  :resource-paths #{"src"}
  :dependencies '[
                  [hoplon/boot-hoplon                       "0.1.13"            :scope "test"]
                  [adzerk/boot-cljs                         "1.7.170-3"         :scope "test"]
                  [adzerk/env                               "0.2.0"]
                  [hoplon/castra                            "3.0.0-alpha3"] ])

(require
  '[adzerk.env               :as    env]
  '[adzerk.boot-cljs         :refer [cljs]]
  '[hoplon.boot-hoplon       :refer [hoplon prerender]])


(task-options!
  pom    {:project 'coci-form
          :version "0.1.0"
          :description "form handling for chabad on campus web projects"}
  hoplon {:manifest true})

(deftask build-jar
 "build the jar"
 []
 (comp
   (hoplon)
   (pom)
   (jar)))

(deftask install-jar
  "install the jar"
  []
  (comp
    (build-jar)
    (install)))
