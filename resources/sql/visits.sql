-- src/coffee_table/resources/sql/visits.sql
-- Café Visits

-- :name insert-visit! :<!
-- :doc Create a café visit
INSERT INTO visits (
cafe_name
,visit_date
,machine
,grinder
,roast
,address1
,address2
,city
,region
,country
,beverage_ordered
,beverage_rating
,beverage_notes
,service_rating
,service_notes
,ambience_rating
,ambience_notes
,other_notes
) VALUES (
:cafe_name
,:visit_date
,:machine
,:grinder
,:roast
,:address1
,:address2
,:city
,:region
,:country
,:beverage_ordered
,:beverage_rating
,:beverage_notes
,:service_rating
,:service_notes
,:ambience_rating
,:ambience_notes
,:other_notes
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
,machine = :machine
,grinder = :grinder
,roast = :roast
,address1 = :address1
,address2 = :address2
,city = :city
,region = :region
,country = :country
,beverage_ordered = :beverage_ordered
,beverage_rating = :beverage_rating
,beverage_notes = :beverage_notes
,service_rating = :service_rating
,service_notes = :service_notes
,ambience_rating = :ambience_rating
,ambience_notes = :ambience_notes
,other_notes = :other_notes
WHERE id = :id
