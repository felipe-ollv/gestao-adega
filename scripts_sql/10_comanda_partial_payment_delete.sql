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

SET @has_valor_pago_parcial = (
    SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'comanda'
      AND column_name = 'valor_pago_parcial'
);
CALL run_if(
    @has_valor_pago_parcial = 0,
    'ALTER TABLE comanda ADD COLUMN valor_pago_parcial DECIMAL(10,2) NOT NULL DEFAULT 0.00 AFTER status'
);

SET @has_data_exclusao = (
    SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'comanda'
      AND column_name = 'data_exclusao'
);
CALL run_if(
    @has_data_exclusao = 0,
    'ALTER TABLE comanda ADD COLUMN data_exclusao DATETIME NULL AFTER data_fechamento'
);

SET @has_observacao_exclusao = (
    SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'comanda'
      AND column_name = 'observacao_exclusao'
);
CALL run_if(
    @has_observacao_exclusao = 0,
    'ALTER TABLE comanda ADD COLUMN observacao_exclusao VARCHAR(500) NULL AFTER valor_pago_parcial'
);

SET @has_comanda_status_check = (
    SELECT COUNT(*) FROM information_schema.table_constraints
    WHERE constraint_schema = DATABASE()
      AND table_name = 'comanda'
      AND constraint_name = 'chk_comanda_status'
);
CALL run_if(
    @has_comanda_status_check > 0,
    'ALTER TABLE comanda DROP CHECK chk_comanda_status'
);
CALL run_if(
    TRUE,
    'ALTER TABLE comanda ADD CONSTRAINT chk_comanda_status CHECK (status IN (''ABERTA'', ''PAGA'', ''FIADO'', ''EXCLUIDA''))'
);

DROP PROCEDURE IF EXISTS run_if;
