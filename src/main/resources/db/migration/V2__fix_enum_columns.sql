ALTER TABLE users ALTER COLUMN personality TYPE VARCHAR(20) USING personality::text;
ALTER TABLE expenses ALTER COLUMN time_of_day TYPE VARCHAR(20) USING time_of_day::text;
ALTER TABLE learning_data ALTER COLUMN time_of_day TYPE VARCHAR(20) USING time_of_day::text;
DROP TYPE IF EXISTS personality_type;
DROP TYPE IF EXISTS time_of_day;