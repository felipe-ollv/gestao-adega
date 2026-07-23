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

SET @has_grupo_uuid = (
    SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'comanda_item' AND column_name = 'grupo_uuid'
);
CALL run_if(@has_grupo_uuid = 0, 'ALTER TABLE comanda_item ADD COLUMN grupo_uuid BINARY(16) NULL AFTER valor_cobrado_unitario');

SET @has_ordem_grupo = (
    SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'comanda_item' AND column_name = 'ordem_grupo'
);
CALL run_if(@has_ordem_grupo = 0, 'ALTER TABLE comanda_item ADD COLUMN ordem_grupo INT NULL AFTER grupo_uuid');

SET @has_idx_item_grupo = (
    SELECT COUNT(*) FROM information_schema.statistics
    WHERE table_schema = DATABASE() AND table_name = 'comanda_item' AND index_name = 'idx_item_grupo'
);
CALL run_if(@has_idx_item_grupo = 0, 'CREATE INDEX idx_item_grupo ON comanda_item (grupo_uuid)');

DROP PROCEDURE IF EXISTS run_if;
