(set-env!
  :resource-paths #{"src"}
  :dependencies '[[hoplon/hoplon                            "7.0.1"             :scope "test"]])

(require
  '[hoplon.boot-hoplon       :refer [hoplon]])


(task-options!
  push   {:repo "clojars-upload"}
  pom    {:project 'rowtr/form
          :version "0.2.5"
          :description "form handling for web projects"}
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
