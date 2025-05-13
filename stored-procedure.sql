-- MySQL Stored Procedure: add_movie
-- This procedure takes movie details, star, and genre information and adds them to the database
-- It handles checking for duplicates, generating IDs, and creating proper relationships
DELIMITER //

DROP PROCEDURE IF EXISTS add_movie //

CREATE PROCEDURE add_movie(
    IN p_title VARCHAR(100),
    IN p_year INTEGER,
    IN p_director VARCHAR(100),
    IN p_star_name VARCHAR(100),
    IN p_genre_name VARCHAR(32),
    OUT p_status VARCHAR(255)
)
BEGIN
    -- Declare variables
    DECLARE v_movie_id VARCHAR(10);
    DECLARE v_star_id VARCHAR(10);
    DECLARE v_genre_id INTEGER;
    DECLARE v_max_movie_id INTEGER;
    DECLARE v_max_star_id INTEGER;
    DECLARE v_movie_exists INT DEFAULT 0;
    DECLARE v_star_exists INT DEFAULT 0;
    DECLARE v_genre_exists INT DEFAULT 0;
    DECLARE v_star_message VARCHAR(50) DEFAULT 'Linked to existing star';
    DECLARE v_genre_message VARCHAR(50) DEFAULT 'Linked to existing genre';
    
    -- Declare handler for SQL exceptions
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        SET p_status = 'Error: SQL exception occurred during execution';
    END;
    
    -- Input validation
    IF p_title IS NULL OR p_title = '' THEN
        SET p_status = 'Error: Movie title is required';
    ELSEIF p_year IS NULL THEN
        SET p_status = 'Error: Release year is required';
    ELSEIF p_director IS NULL OR p_director = '' THEN
        SET p_status = 'Error: Director name is required';
    ELSEIF p_star_name IS NULL OR p_star_name = '' THEN
        SET p_status = 'Error: Star name is required';
    ELSEIF p_genre_name IS NULL OR p_genre_name = '' THEN
        SET p_status = 'Error: Genre name is required';
    ELSE
    
    -- Start transaction to ensure data consistency
    START TRANSACTION;
    
    -- Check if movie already exists with same title, year, and director
    SELECT COUNT(*) INTO v_movie_exists 
    FROM movies 
    WHERE title = p_title AND year = p_year AND director = p_director;
    
    IF v_movie_exists > 0 THEN
        SET p_status = 'Movie already exists with the same title, year, and director';
        ROLLBACK;
    ELSE
        -- Generate new movie ID
        -- Extract the highest numeric part from existing movie IDs
        SELECT MAX(CAST(SUBSTRING(id, 3) AS UNSIGNED)) INTO v_max_movie_id 
        FROM movies 
        WHERE id LIKE 'tt%';
        
        -- Create new movie ID in format 'tt0000123'
        SET v_movie_id = CONCAT('tt', LPAD(CAST(v_max_movie_id + 1 AS CHAR), 7, '0'));
        
        -- Insert new movie
        INSERT INTO movies (id, title, year, director) 
        VALUES (v_movie_id, p_title, p_year, p_director);
        
        -- Check if star already exists
        SELECT COUNT(*) INTO v_star_exists 
        FROM stars 
        WHERE name = p_star_name;
        
        IF v_star_exists > 0 THEN
            -- Get the first star ID with this name
            SELECT id INTO v_star_id 
            FROM stars 
            WHERE name = p_star_name 
            LIMIT 1;
        ELSE
            -- Generate new star ID
            SELECT MAX(CAST(SUBSTRING(id, 3) AS UNSIGNED)) INTO v_max_star_id 
            FROM stars 
            WHERE id LIKE 'nm%';
            
            -- Create new star ID in format 'nm0000123'
            SET v_star_id = CONCAT('nm', LPAD(CAST(v_max_star_id + 1 AS CHAR), 7, '0'));
            
            -- Insert new star (birth year is NULL since it's not provided)
            INSERT INTO stars (id, name) 
            VALUES (v_star_id, p_star_name);
            
            SET v_star_message = 'Created new star';
        END IF;
        
        -- Link movie to star in stars_in_movies
        INSERT INTO stars_in_movies (starId, movieId)
        VALUES (v_star_id, v_movie_id);
        
        -- Check if genre already exists
        SELECT COUNT(*) INTO v_genre_exists 
        FROM genres
        WHERE name = p_genre_name;
        
        IF v_genre_exists > 0 THEN
            -- Get the genre ID for this name
            SELECT id INTO v_genre_id 
            FROM genres 
            WHERE name = p_genre_name 
            LIMIT 1;
        ELSE
            -- Generate new genre ID (increment from max)
            SELECT COALESCE(MAX(id) + 1, 1) INTO v_genre_id FROM genres;
            
            -- Insert new genre
            INSERT INTO genres (id, name)
            VALUES (v_genre_id, p_genre_name);
            
            SET v_genre_message = 'Created new genre';
        END IF;
        
        -- Link movie to genre in genres_in_movies
        INSERT INTO genres_in_movies (genreId, movieId)
        VALUES (v_genre_id, v_movie_id);
        
        -- Set success status with movie ID
        SET p_status = CONCAT('Movie added successfully. Movie ID: ', v_movie_id, 
                             '. ', v_star_message, '. ', v_genre_message);
        
        -- Commit the transaction
        COMMIT;
    END IF;
    END IF; -- End of validation IF/ELSE
END //

DELIMITER ;

-- Example of how to call the procedure:
-- CALL add_movie('The Godfather', 1972, 'Francis Ford Coppola', 'Marlon Brando', 'Drama', @status);
-- SELECT @status;
