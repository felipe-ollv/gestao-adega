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

SET @has_alerta_estoque = (
    SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'produto' AND column_name = 'alerta_estoque_unidades'
);
CALL run_if(@has_alerta_estoque = 0, 'ALTER TABLE produto ADD COLUMN alerta_estoque_unidades INT NOT NULL DEFAULT 12 AFTER quantidade_estoque_unidades');

SET @has_alerta_check = (
    SELECT COUNT(*) FROM information_schema.check_constraints
    WHERE constraint_schema = DATABASE() AND constraint_name = 'chk_produto_alerta_estoque'
);
CALL run_if(@has_alerta_check = 0, 'ALTER TABLE produto ADD CONSTRAINT chk_produto_alerta_estoque CHECK (alerta_estoque_unidades >= 0)');

DROP PROCEDURE IF EXISTS run_if;
