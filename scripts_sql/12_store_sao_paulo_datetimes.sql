-- Converte timestamps absolutos em horarios civis de Sao Paulo.
-- A sessao precisa estar em -03:00 durante a alteracao para preservar
-- o horario local exibido antes da troca de TIMESTAMP para DATETIME.

SET @previous_time_zone = @@session.time_zone;
SET time_zone = '-03:00';

ALTER TABLE adega
    MODIFY COLUMN data_cadastro DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE usuario
    MODIFY COLUMN data_cadastro DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE adega_mensalidade
    MODIFY COLUMN data_pagamento DATETIME NULL,
    MODIFY COLUMN data_cadastro DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    MODIFY COLUMN data_atualizacao DATETIME NOT NULL
        DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP;

ALTER TABLE notificacao_email
    MODIFY COLUMN data_envio DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE comanda
    MODIFY COLUMN data_abertura DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    MODIFY COLUMN data_fechamento DATETIME NULL,
    MODIFY COLUMN data_exclusao DATETIME NULL;

SET time_zone = @previous_time_zone;
