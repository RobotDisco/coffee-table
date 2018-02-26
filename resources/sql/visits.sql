-- src/coffee_table/resources/sql/visits.sql
-- Café Visits

-- :name insert-visit! :<!
-- :doc Create a café visit
INSERT INTO visits (
cafe_name
,visit_date
--~ (when (contains? params :machine) ",machine")
--~ (when (contains? params :grinder) ",grinder")
--~ (when (contains? params :roast) ",roast")
,beverage_ordered
,beverage_rating
--~ (when (contains? params :beverage_notes) ",beverage_notes")
--~ (when (contains? params :service_rating) ",service_rating")
--~ (when (contains? params :service_notes) ",service_notes")
--~ (when (contains? params :ambience_rating) ",ambience_rating")
--~ (when (contains? params :ambience_notes) ",ambience_notes")
--~ (when (contains? params :other_notes) ",other_notes")
) VALUES (
:cafe_name
,:visit_date
--~ (when (contains? params :machine) ",:machine")
--~ (when (contains? params :grinder) ",:grinder")
--~ (when (contains? params :roast) ",:roast")
,:beverage_ordered
,:beverage_rating
--~ (when (contains? params :beverage_notes) ",:beverage_notes")
--~ (when (contains? params :service_rating) ",:service_rating")
--~ (when (contains? params :service_notes) ",:service_notes")
--~ (when (contains? params :ambience_rating) ",:ambience_rating")
--~ (when (contains? params :ambience_notes) ",:ambience_notes")
--~ (when (contains? params :other_notes) ",:other_notes")
) RETURNING id

-- :name get-visit :? :1
-- doc retrieve a visit record given the id
SELECT * FROM visits
WHERE id = :id

-- :name list-visit-summaries :? :*
-- doc get a list of summaries of all visits
SELECT id, cafe_name, visit_date, beverage_rating FROM visits

-- :name delete-visit-by-id! :! :n
-- :doc Delete a café visit with the given ID
DELETE FROM visits WHERE id = :id

-- :name update-visit-by-id! :! :n
-- :doc Update a café visit with the following values
UPDATE visits SET
cafe_name = :cafe_name
,visit_date = :visit_date
--~ (when (contains? params :machine) ",machine = :machine")
--~ (when (contains? params :grinder) ",grinder = :grinder")
--~ (when (contains? params :roast) ",roast = :roast")
,beverage_ordered = :beverage_ordered
,beverage_rating = :beverage_rating
--~ (when (contains? params :beverage_notes) ",beverage_notes = :beverage_notes")
--~ (when (contains? params :service_rating) ",service_rating = :service_rating")
--~ (when (contains? params :service_notes) ",service_notes = :service_notes")
--~ (when (contains? params :ambience_rating) ",ambience_rating = :ambience_rating")
--~ (when (contains? params :ambience_notes) ",ambience_notes = :ambience_notes")
--~ (when (contains? params :other_notes) ",other_notes = :other_notes")
WHERE id = :id
