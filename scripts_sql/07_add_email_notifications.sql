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

SET @has_alerta_notificado = (
    SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'produto' AND column_name = 'alerta_estoque_notificado'
);
CALL run_if(@has_alerta_notificado = 0, 'ALTER TABLE produto ADD COLUMN alerta_estoque_notificado BOOLEAN NOT NULL DEFAULT FALSE AFTER alerta_estoque_unidades');

SET @has_alerta_ciclo = (
    SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'produto' AND column_name = 'alerta_estoque_ciclo'
);
CALL run_if(@has_alerta_ciclo = 0, 'ALTER TABLE produto ADD COLUMN alerta_estoque_ciclo INT NOT NULL DEFAULT 0 AFTER alerta_estoque_notificado');

SET @has_alerta_ciclo_check = (
    SELECT COUNT(*) FROM information_schema.check_constraints
    WHERE constraint_schema = DATABASE() AND constraint_name = 'chk_produto_alerta_ciclo'
);
CALL run_if(@has_alerta_ciclo_check = 0, 'ALTER TABLE produto ADD CONSTRAINT chk_produto_alerta_ciclo CHECK (alerta_estoque_ciclo >= 0)');

CREATE TABLE IF NOT EXISTS notificacao_email (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    uuid BINARY(16) NOT NULL UNIQUE,
    adega_uuid BINARY(16) NOT NULL,
    tipo VARCHAR(50) NOT NULL,
    chave_referencia VARCHAR(255) NOT NULL UNIQUE,
    destinatario VARCHAR(120) NOT NULL,
    provider_id VARCHAR(100) NULL,
    data_envio TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notificacao_email_adega_uuid FOREIGN KEY (adega_uuid) REFERENCES adega(uuid),
    INDEX idx_notificacao_email_adega_tipo (adega_uuid, tipo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

DROP PROCEDURE IF EXISTS run_if;
