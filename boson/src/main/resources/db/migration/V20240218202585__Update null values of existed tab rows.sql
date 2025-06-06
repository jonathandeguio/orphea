UPDATE kepler_tabs
SET top_padding = 2
WHERE top_padding IS NULL;

UPDATE kepler_tabs
SET right_padding = 2
WHERE right_padding IS NULL;

UPDATE kepler_tabs
SET bottom_padding = 2
WHERE bottom_padding IS NULL;

UPDATE kepler_tabs
SET left_padding = 2
WHERE left_padding IS NULL;

UPDATE kepler_tabs
SET prevent_collision = false
WHERE prevent_collision IS NULL;

UPDATE kepler_tabs
SET prevent_overlap = false
WHERE prevent_overlap IS NULL;