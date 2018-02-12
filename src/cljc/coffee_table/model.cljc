(ns coffee-table.model
  (:require [schema.core :as s]))

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
   :visit_date s/Inst
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
