(ns  ui.form.validation
  (:refer-clojure :exclude [when not-empty])
  (:require
    [clojure.string    :as string])
  (:import
    [java.net URL]))

(def ^:private url-regex
  "Source: https://gist.github.com/dperini/729294"
  (str "(?i)"
       ; protocol identifier
       "(?:(?:https?|ftp)://)"
       ; user:pass authentication
       "(?:\\S+(?::\\S*)?@)?"
       "(?:"
       ; IP address exclusion
       ; private & local networks
       "(?!(?:10|127)(?:\\.\\d{1,3}){3})"
       "(?!(?:169\\.254|192\\.168)(?:\\.\\d{1,3}){2})"
       "(?!172\\.(?:1[6-9]|2\\d|3[0-1])(?:\\.\\d{1,3}){2})"
       ; IP address dotted notation octets
       ; excludes loopback network 0.0.0.0
       ; excludes reserved space >= 224.0.0.0
       ; excludes network & broacast addresses
       ; (first & last IP address of each class)
       "(?:[1-9]\\d?|1\\d\\d|2[01]\\d|22[0-3])"
       "(?:\\.(?:1?\\d{1,2}|2[0-4]\\d|25[0-5])){2}"
       "(?:\\.(?:[1-9]\\d?|1\\d\\d|2[0-4]\\d|25[0-4]))"
       "|"
       ; host name
       "(?:(?:[a-z\\u00a1-\\uffff0-9]-*)*[a-z\\u00a1-\\uffff0-9]+)"
       ; domain name
       "(?:\\.(?:[a-z\\u00a1-\\uffff0-9]-*)*[a-z\\u00a1-\\uffff0-9]+)*"
       ; TLD identifier
       "(?:\\.(?:[a-z\\u00a1-\\uffff]{2,}))"
       ")"
       ; port number
       "(?::\\d{2,5})?"
       ; resource path
       "(?:/\\S*)?"))

(def ^:private email-regex
       (str "(?i)"
            "[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?"))

(defmacro guard [& body]
  `(try ~@body (catch Throwable _#)))

(defmacro validator
  [binding expr error]
  `(fn [~@binding]
     (when-not (guard ~expr) ~error)))

(defmacro defvalidator [name & forms]
  `(def ~name (validator ~@forms)))

(defn keep-vals [keep-fn kv-pairs]
  (into {} (filter (comp val keep-fn) kv-pairs)))

(defn luhn?  [cc]
  (let [factors  (cycle  [1 2])
        numbers  (map #(Character/digit % 10) cc)
        sum  (reduce +  (map #(+  (quot % 10)  (mod % 10))
              (map *  (reverse numbers) factors)))]
      (zero?  (mod sum 10))))


;; validation test fns ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defvalidator email [x]
  (and (string? x) (re-find (re-pattern email-regex) x))
  "This field must be a valid email.")

(defvalidator url [x]
  (and (string? x) (re-find (re-pattern url-regex) x))
  "This field must be a valid URL.")

(defvalidator required [x]
  (and x (not (and (string? x) (empty? x))))
  "This field is required.")

(defvalidator integer [x]
  (or (integer? x) (and (string? x) (integer? (read-string x))))
  "This field must be an integer.")

(defvalidator number [x]
  (or (number? x) (and (string? x) (number? (read-string x))))
  "This field must be a number.")

(defvalidator not-empty [x]
  (and (coll? x) (seq x))
  "At least one item must be selected.")

(defvalidator expiration-format [x]
  (not= nil (or (re-matches #"\d\d\/\d\d" x) (re-matches #"\d\/\d\d" x)))
  "Expiration not valid format")

(defvalidator credit-card [x]
  (luhn? x)
  "Not a valid credit card number.")

;; validation runner ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn validate*
  [m f ks]
  (->> ks
       (map (comp f (partial get m)))
       (zipmap ks)
       (keep-vals identity)))

(defn validate
  [m & kv-pairs]
  (->> kv-pairs
       (partition 2)
       (map (partial apply validate* m))
       (reduce (partial merge-with #(or %1 %2)))
       (keep-vals identity)))

(defn when
  [pred validation]
  (if pred validation (constantly nil)))
