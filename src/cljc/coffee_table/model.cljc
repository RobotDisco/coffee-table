(ns coffee-table.model
  (:require [java-time.local]
            [schema.core :as s]
            [schema.coerce :as coerce])
  (:import [java.time LocalDate]))

(s/defschema Rating
  "Numeric score for various visit factors"
  (s/enum 0 1 2 3 4 5))

;;; No idea how implement this, will come back to later
 #_ (s/defschema Address
      "Location information"
      {:address1 s/Str
       (s/optional-key :address2) s/Str
       :city s/Str
       :region s/Str
       :country s/Str})

(s/defschema Visit
  "Schema for coffee table visits"
  {(s/optional-key :id) s/Int
   :cafe_name s/Str
   :visit_date LocalDate
   #_ (s/optional-key :address) #_ Address
   (s/optional-key :machine) (s/maybe s/Str)
   (s/optional-key :grinder) (s/maybe s/Str)
   (s/optional-key :roast) (s/maybe s/Str)
   :beverage_ordered s/Str
   :beverage_rating Rating
   (s/optional-key :beverage_notes) (s/maybe s/Str)
   (s/optional-key :service_rating) (s/maybe Rating)
   (s/optional-key :service_notes) (s/maybe s/Str)
   (s/optional-key :ambience_rating) (s/maybe Rating)
   (s/optional-key :ambience_notes) (s/maybe s/Str)
   (s/optional-key :other_notes) (s/maybe s/Str)})

(s/defn make-visit :- Visit
  "Create a visit object with default values"
  [cafe-name :- s/Str
   date-visited :- LocalDate
   beverage-ordered :- s/Str
   beverage-rating :- Rating]
  (merge {:cafe_name cafe-name
          :visit_date date-visited
          :beverage_ordered beverage-ordered
          :beverage_rating beverage-rating}
         {:machine nil
          :grinder nil
          :roast nil
          :beverage_notes nil
          :service_rating nil
          :service_notes nil
          :ambience_rating nil
          :ambience_notes nil
          :other_notes nil}))

(defn localdate-matcher [schema]
  (let [datetime-regex #"\d{4}-\d{2}-\d{2}"]
    (when (= LocalDate schema)
      (coerce/safe
       (fn [x]
         (if (and (string? x) (re-matches datetime-regex x))
           (java-time.local/local-date x)
           x))))))

(def visit-matcher (coerce/first-matcher [localdate-matcher coerce/json-coercion-matcher]))

(defn json->visit [json]
  ((coerce/coercer Visit visit-matcher) json))

(s/defschema Summary
  "Schema for coffee table summaries"
  {:id s/Int
   :cafe_name s/Str
   :visit_date LocalDate
   :beverage_rating s/Int})

(defn json->summary [json]
  ((coerce/coercer Summary visit-matcher) json))
