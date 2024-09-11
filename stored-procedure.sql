DELIMITER $$

CREATE PROCEDURE `add_star`(IN p_starName VARCHAR(100), IN p_birthYear INT)
BEGIN
  DECLARE new_id VARCHAR(10);

  -- Assuming 'id' is a VARCHAR type and you want to increment the numeric part
SELECT CONCAT('nm', LPAD(IFNULL(MAX(CAST(SUBSTRING(id, 3) AS UNSIGNED)), 0) + 1, 7, '0')) INTO new_id
FROM stars;

-- Insert the new star with the generated ID
INSERT INTO stars (id, name, birthYear) VALUES (new_id, p_starName, p_birthYear);
END$$

DELIMITER ;

DELIMITER $$

CREATE PROCEDURE add_movie(
    IN movie_title VARCHAR(100),
    IN movie_year INTEGER,
    IN movie_director VARCHAR(100),
    IN star_name VARCHAR(100),
    IN star_birthYear INTEGER,
    IN genre_name VARCHAR(32)
)
movie_block: BEGIN
    DECLARE movie_id VARCHAR(10);
    DECLARE star_id VARCHAR(10);
    DECLARE genre_id INT;
    DECLARE existing_movie_id VARCHAR(10);
    DECLARE error_message VARCHAR(255);

    DECLARE CONTINUE HANDLER FOR SQLEXCEPTION
    BEGIN
    GET DIAGNOSTICS CONDITION 1
        error_message = MESSAGE_TEXT;
        -- Rollback the transaction on error
        ROLLBACK;
        -- Select the error message to be caught by the calling application
        SELECT error_message AS error;
    END;

    -- Start the transaction
    START TRANSACTION;

    -- Check if a movie with the same title, year, and director already exists
    SELECT id INTO existing_movie_id FROM movies WHERE title = movie_title AND year = movie_year AND director = movie_director LIMIT 1;
    IF existing_movie_id IS NOT NULL THEN
        -- Set the error message and exit the procedure
        SET error_message = 'A movie with the same title, year, and director already exists.';
        SELECT error_message AS error;
        ROLLBACK;
        LEAVE movie_block;
    END IF;

    -- Generate a new movie_id here
    SET movie_id = (SELECT CONCAT('tt', LPAD(CONVERT(IFNULL(MAX(SUBSTRING(id, 3)), 0) + 1, CHAR), 7, '0')) FROM movies);

    -- Add movie logic with random price between 10 and 30 TODO: Don't add if title, year, and director already exist. Error out.
    INSERT INTO movies (id, title, year, director, price)
    VALUES (movie_id, movie_title, movie_year, movie_director, ROUND(10 + (RAND() * (30 - 10)), 2));

    -- Check if the star exists, if not, add the star
    SELECT id INTO star_id FROM stars WHERE name = star_name;
    IF star_id IS NULL THEN
        -- Generate a new star_id here
        SET star_id = (SELECT CONCAT('nm', LPAD(CONVERT(IFNULL(MAX(SUBSTRING(id, 3)), 0) + 1, CHAR), 7, '0')) FROM stars);
        INSERT INTO stars (id, name, birthYear) VALUES (star_id, star_name, star_birthYear);
    END IF;

    -- Check if the genre exists, if not, add the genre
    SELECT id INTO genre_id FROM genres WHERE name = genre_name;
    IF genre_id IS NULL THEN
        INSERT INTO genres (name) VALUES (genre_name);
        SET genre_id = LAST_INSERT_ID();
    END IF;

    -- Associate the movie with the star and genre
    INSERT INTO stars_in_movies (starId, movieId) VALUES (star_id, movie_id);
    INSERT INTO genres_in_movies (genreId, movieId) VALUES (genre_id, movie_id);

    -- Add to Ratings
    INSERT INTO ratings (movieId, rating, numVotes) VALUES (movie_id, 0.0, 0);

    -- Commit the transaction
    COMMIT;
END$$

DELIMITER ;