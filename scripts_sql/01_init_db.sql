CREATE TABLE IF NOT EXISTS adega (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    uuid BINARY(16) NOT NULL UNIQUE,
    nome VARCHAR(100) NOT NULL,
    cnpj_cpf VARCHAR(14) NOT NULL UNIQUE,
    data_cadastro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS usuario (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    uuid BINARY(16) NOT NULL UNIQUE,
    adega_uuid BINARY(16) NOT NULL,
    nome VARCHAR(100) NOT NULL,
    email VARCHAR(120) NOT NULL UNIQUE,
    senha_hash VARCHAR(255) NOT NULL,
    perfil VARCHAR(20) NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    data_cadastro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_usuario_adega_uuid FOREIGN KEY (adega_uuid) REFERENCES adega(uuid),
    CONSTRAINT chk_usuario_perfil CHECK (perfil IN ('GESTOR', 'ATENDENTE'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

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

CREATE TABLE IF NOT EXISTS produto (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    uuid BINARY(16) NOT NULL UNIQUE,
    adega_uuid BINARY(16) NOT NULL,
    nome VARCHAR(100) NOT NULL,
    quantidade_estoque_unidades INT NOT NULL,
    unidades_por_caixa INT NOT NULL DEFAULT 1,
    valor_unidade DECIMAL(10,2) NOT NULL,
    valor_caixa DECIMAL(10,2) NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_produto_adega_uuid FOREIGN KEY (adega_uuid) REFERENCES adega(uuid),
    CONSTRAINT chk_produto_estoque CHECK (quantidade_estoque_unidades >= 0),
    CONSTRAINT chk_produto_unidades_caixa CHECK (unidades_por_caixa >= 1),
    INDEX idx_produto_adega_nome (adega_uuid, nome)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS comanda (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    uuid BINARY(16) NOT NULL UNIQUE,
    adega_uuid BINARY(16) NOT NULL,
    nome_responsavel VARCHAR(100) NOT NULL,
    data_abertura TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_fechamento TIMESTAMP NULL,
    status VARCHAR(20) NOT NULL,
    CONSTRAINT fk_comanda_adega_uuid FOREIGN KEY (adega_uuid) REFERENCES adega(uuid),
    CONSTRAINT chk_comanda_status CHECK (status IN ('ABERTA', 'PAGA', 'FIADO')),
    INDEX idx_comanda_adega_status (adega_uuid, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS comanda_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    uuid BINARY(16) NOT NULL UNIQUE,
    comanda_uuid BINARY(16) NOT NULL,
    produto_uuid BINARY(16) NOT NULL,
    quantidade_pedida INT NOT NULL,
    unidades_deduzidas INT NOT NULL,
    tipo_medida_vendida VARCHAR(20) NOT NULL,
    valor_cobrado_unitario DECIMAL(10,2) NOT NULL,
    CONSTRAINT fk_item_comanda_uuid FOREIGN KEY (comanda_uuid) REFERENCES comanda(uuid),
    CONSTRAINT fk_item_produto_uuid FOREIGN KEY (produto_uuid) REFERENCES produto(uuid),
    CONSTRAINT chk_item_quantidade CHECK (quantidade_pedida > 0),
    CONSTRAINT chk_item_unidades CHECK (unidades_deduzidas > 0),
    CONSTRAINT chk_item_tipo_medida CHECK (tipo_medida_vendida IN ('UNIDADE', 'CAIXA')),
    INDEX idx_item_comanda (comanda_uuid),
    INDEX idx_item_produto (produto_uuid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
