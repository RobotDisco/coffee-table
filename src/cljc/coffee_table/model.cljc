(ns coffee-table.model
  (:require [java-time.local]
            [schema.core :as s]
            [schema.coerce :as coerce]
            [buddy.hashers :as bhash]
            [clojure.walk :refer [keywordize-keys]])
  (:import [java.time LocalDate]))

(s/defschema Rating
  "Numeric score for various visit factors"
  (s/constrained s/Int #(<= 1 % 5)))

(s/defschema Visit
  "Schema for coffee table visits"
  {(s/optional-key :id) s/Int
   :cafe_name s/Str
   :visit_date LocalDate
   :machine (s/maybe s/Str)
   :grinder (s/maybe s/Str)
   :roast (s/maybe s/Str)
   :address1 (s/maybe s/Str)
   :address2 (s/maybe s/Str)
   :city (s/maybe s/Str)
   :region (s/maybe s/Str)
   :country (s/maybe s/Str)
   :beverage_ordered s/Str
   :beverage_rating Rating
   :beverage_notes (s/maybe s/Str)
   (s/optional-key :service_rating) (s/maybe s/Int)
   :service_notes (s/maybe s/Str)
   (s/optional-key :ambience_rating) (s/maybe s/Int)
   :ambience_notes (s/maybe s/Str)
   :other_notes (s/maybe s/Str)})

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
         {:address1 nil
          :address2 nil
          :city nil
          :region nil
          :country nil
          :machine nil
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

(s/defschema PublicUser
  "Coffee Table Users (password removed for security reasons)"
  {(s/optional-key :id) s/Int
   :username s/Str
   :is_admin s/Bool})

(s/defschema PrivateUser
  "Coffee Table User accounts"
  (merge PublicUser
         {:password s/Str}))

(s/defn make-user :- PrivateUser
  [username :- String
   password :- String]
  {:username username
   :password (bhash/derive password)
   :is_admin false})
