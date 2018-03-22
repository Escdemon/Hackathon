-- Target Database : MYSQL
-- ===================
SET FOREIGN_KEY_CHECKS=0;

-- Delete tables content
DELETE FROM BALISE;
DELETE FROM LOCALISATION;


-- Insert new data
INSERT INTO BALISE (ID, NOM) 
  VALUES (1, 'Opérateur');

SET FOREIGN_KEY_CHECKS=1;

