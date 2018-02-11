CREATE TABLE IF NOT EXISTS visits (
  id SERIAL PRIMARY KEY,
  cafe_name TEXT NOT NULL,
  visit_date DATE NOT NULL,
  -- address,
  machine TEXT,
  grinder TEXT,
  roast TEXT,
  beverage_ordered TEXT NOT NULL,
  beverage_rating INTEGER NOT NULL CHECK (beverage_rating > 0 AND beverage_rating <= 5),
  beverage_notes TEXT,
  service_rating INTEGER CHECK (service_rating > 0 AND service_rating <= 5),
  service_notes TEXT,
  ambience_rating INTEGER CHECK (ambience_rating > 0 AND ambience_rating <= 5),
  ambience_notes TEXT,
  other_notes TEXT
);
