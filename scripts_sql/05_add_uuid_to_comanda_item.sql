DROP PROCEDURE IF EXISTS run_if;

DELIMITER //

CREATE PROCEDURE run_if(IN should_run BOOLEAN, IN sql_stmt TEXT)
BEGIN
    IF should_run THEN
        SET @stmt = sql_stmt;
        PREPARE migration_stmt FROM @stmt;
        EXECUTE migration_stmt;
        DEALLOCATE PREPARE migration_stmt;
    END IF;
END//

DELIMITER ;

SET @has_uuid = (
    SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'comanda_item' AND column_name = 'uuid'
);
CALL run_if(@has_uuid = 0, 'ALTER TABLE comanda_item ADD COLUMN uuid BINARY(16) NULL AFTER id');

UPDATE comanda_item
SET uuid = UUID_TO_BIN(UUID())
WHERE uuid IS NULL;

CALL run_if(TRUE, 'ALTER TABLE comanda_item MODIFY uuid BINARY(16) NOT NULL');

SET @has_key = (
    SELECT COUNT(*) FROM information_schema.statistics
    WHERE table_schema = DATABASE() AND table_name = 'comanda_item' AND index_name = 'uk_comanda_item_uuid'
);
CALL run_if(@has_key = 0, 'CREATE UNIQUE INDEX uk_comanda_item_uuid ON comanda_item (uuid)');

DROP PROCEDURE IF EXISTS run_if;
