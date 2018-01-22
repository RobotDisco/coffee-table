create table if not exists visits (
  id serial PRIMARY KEY,
  cafe_name text NOT NULL,
  date_visited date NOT NULL,
  -- address,
  machine text,
  grinder text,
  roast text,
  beverage_ordered text NOT NULL,
  beverage_rating integer NOT NULL CHECK (beverage_rating > 0 AND beverage_rating <= 5),
  beverage_notes text,
  service_rating integer CHECK (service_rating > 0 AND service_rating <= 5),
  service_notes text,
  ambience_rating integer CHECK (ambience_rating > 0 AND ambience_rating <= 5),
  ambience_notes text,
  other_notes text
);
