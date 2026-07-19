CREATE TABLE IF NOT EXISTS adega_mensalidade (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    adega_uuid BINARY(16) NOT NULL,
    competencia DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDENTE',
    data_vencimento DATE NOT NULL,
    data_pagamento TIMESTAMP NULL,
    data_cadastro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_atualizacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_mensalidade_adega_uuid FOREIGN KEY (adega_uuid) REFERENCES adega(uuid),
    CONSTRAINT chk_mensalidade_status CHECK (status IN ('PENDENTE', 'PAGO')),
    CONSTRAINT uk_mensalidade_adega_competencia UNIQUE (adega_uuid, competencia),
    INDEX idx_mensalidade_competencia_status (competencia, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO adega_mensalidade (adega_uuid, competencia, status, data_vencimento)
SELECT a.uuid, DATE_FORMAT(CURRENT_DATE, '%Y-%m-01'), 'PENDENTE', LAST_DAY(CURRENT_DATE)
FROM adega a
WHERE NOT EXISTS (
    SELECT 1
    FROM adega_mensalidade m
    WHERE m.adega_uuid = a.uuid
      AND m.competencia = DATE_FORMAT(CURRENT_DATE, '%Y-%m-01')
);
