(ns ui.form)

(defmacro with-form
  [[binding form-machine] & body]
  `(let [form-machine# ~form-machine
         ~binding      form-machine#
         {data#      :data
          error#     :error
          state#     :state
          exception# :exception
          loading#   :loading} form-machine#]
     (binding [*form-machine*   form-machine#
               *form-exception* exception#
               *form-inputs*    data#
               *form-errors*    error#
               *form-state*     state#
               *form-loading*   loading#]
       ~@body)))

(defmacro with-field
  [attr & body]
  `(let [[error# data#] (map ~attr [*form-errors* *form-inputs*])]
     (binding [*input-error* error#
               *input-data*  data#
               *input-name* (gensym)]
       ~@body)))

(defmacro definput
  [name [attr kids] & body]
  `(hoplon.core/defelem ~name
     [{state# :state
       id# :id
       :as attr#} kids#]
     (let [state# (or state# *input-data*)
           id#    (or id# *input-name*)
           ~attr (merge attr# {:state state# :id id#})
           ~kids kids#]
       ~@body)))
