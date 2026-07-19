CREATE TABLE IF NOT EXISTS adega_pagamento (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    adega_uuid BINARY(16) NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDENTE',
    data_cadastro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_atualizacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    data_pagamento TIMESTAMP NULL,
    CONSTRAINT fk_pagamento_adega_uuid FOREIGN KEY (adega_uuid) REFERENCES adega(uuid),
    CONSTRAINT chk_pagamento_status CHECK (status IN ('PENDENTE', 'PAGO')),
    INDEX idx_pagamento_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO adega_pagamento (adega_uuid, status)
SELECT a.uuid, 'PENDENTE'
FROM adega a
WHERE NOT EXISTS (
    SELECT 1
    FROM adega_pagamento p
    WHERE p.adega_uuid = a.uuid
);
