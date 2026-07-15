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
    adega_id BIGINT NOT NULL,
    nome VARCHAR(100) NOT NULL,
    email VARCHAR(120) NOT NULL UNIQUE,
    senha_hash VARCHAR(255) NOT NULL,
    perfil VARCHAR(20) NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    data_cadastro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_usuario_adega FOREIGN KEY (adega_id) REFERENCES adega(id),
    CONSTRAINT chk_usuario_perfil CHECK (perfil IN ('GESTOR', 'ATENDENTE'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS produto (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    uuid BINARY(16) NOT NULL UNIQUE,
    adega_id BIGINT NOT NULL,
    nome VARCHAR(100) NOT NULL,
    quantidade_estoque_unidades INT NOT NULL,
    unidades_por_caixa INT NOT NULL DEFAULT 1,
    valor_unidade DECIMAL(10,2) NOT NULL,
    valor_caixa DECIMAL(10,2) NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_produto_adega FOREIGN KEY (adega_id) REFERENCES adega(id),
    CONSTRAINT chk_produto_estoque CHECK (quantidade_estoque_unidades >= 0),
    CONSTRAINT chk_produto_unidades_caixa CHECK (unidades_por_caixa >= 1),
    INDEX idx_produto_adega_nome (adega_id, nome)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS comanda (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    uuid BINARY(16) NOT NULL UNIQUE,
    adega_id BIGINT NOT NULL,
    nome_responsavel VARCHAR(100) NOT NULL,
    data_abertura TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_fechamento TIMESTAMP NULL,
    status VARCHAR(20) NOT NULL,
    CONSTRAINT fk_comanda_adega FOREIGN KEY (adega_id) REFERENCES adega(id),
    CONSTRAINT chk_comanda_status CHECK (status IN ('ABERTA', 'PAGA', 'FIADO')),
    INDEX idx_comanda_adega_status (adega_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS comanda_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    comanda_id BIGINT NOT NULL,
    produto_id BIGINT NOT NULL,
    quantidade_pedida INT NOT NULL,
    unidades_deduzidas INT NOT NULL,
    tipo_medida_vendida VARCHAR(20) NOT NULL,
    valor_cobrado_unitario DECIMAL(10,2) NOT NULL,
    CONSTRAINT fk_item_comanda FOREIGN KEY (comanda_id) REFERENCES comanda(id),
    CONSTRAINT fk_item_produto FOREIGN KEY (produto_id) REFERENCES produto(id),
    CONSTRAINT chk_item_quantidade CHECK (quantidade_pedida > 0),
    CONSTRAINT chk_item_unidades CHECK (unidades_deduzidas > 0),
    CONSTRAINT chk_item_tipo_medida CHECK (tipo_medida_vendida IN ('UNIDADE', 'CAIXA')),
    INDEX idx_item_comanda (comanda_id),
    INDEX idx_item_produto (produto_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
