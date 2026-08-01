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

SET @has_data_adicao = (
    SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'comanda_item'
      AND column_name = 'data_adicao'
);
CALL run_if(
    @has_data_adicao = 0,
    'ALTER TABLE comanda_item ADD COLUMN data_adicao DATETIME NULL AFTER ordem_grupo'
);

UPDATE comanda_item item
JOIN comanda ON comanda.uuid = item.comanda_uuid
SET item.data_adicao = COALESCE(item.data_adicao, comanda.data_abertura, CURRENT_TIMESTAMP)
WHERE item.data_adicao IS NULL;

CALL run_if(
    TRUE,
    'ALTER TABLE comanda_item MODIFY COLUMN data_adicao DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP'
);

SET @has_idx_item_data_adicao = (
    SELECT COUNT(*) FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'comanda_item'
      AND index_name = 'idx_item_comanda_data_adicao'
);
CALL run_if(
    @has_idx_item_data_adicao = 0,
    'CREATE INDEX idx_item_comanda_data_adicao ON comanda_item (comanda_uuid, data_adicao)'
);

DROP PROCEDURE IF EXISTS run_if;
