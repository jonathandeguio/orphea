ALTER TABLE build_specifications
ALTER COLUMN build_id TYPE UUID USING build_id::UUID;