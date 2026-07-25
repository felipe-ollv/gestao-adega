-- Execute uma unica vez em bancos antigos que ainda possuem adega_pagamento.
-- adega_mensalidade e a fonte de verdade: um pagamento ja registrado nela
-- tem prioridade sobre qualquer registro duplicado da tabela legada.

CREATE TEMPORARY TABLE pagamento_legado_pago (
    adega_uuid BINARY(16) NOT NULL,
    data_pagamento DATETIME NOT NULL,
    data_inicio DATE NOT NULL,
    PRIMARY KEY (adega_uuid)
);

SET @adega_pagamento_existe = (
    SELECT COUNT(*)
    FROM information_schema.tables
    WHERE table_schema = DATABASE()
      AND table_name = 'adega_pagamento'
);

SET @importar_pagamentos_legados = IF(
    @adega_pagamento_existe > 0,
    'INSERT INTO pagamento_legado_pago (adega_uuid, data_pagamento, data_inicio)
     SELECT
         pagamento.adega_uuid,
         COALESCE(pagamento.data_pagamento, pagamento.data_atualizacao),
         DATE(COALESCE(pagamento.data_pagamento, pagamento.data_atualizacao))
     FROM adega_pagamento pagamento
     WHERE pagamento.status = ''PAGO''
       AND NOT EXISTS (
           SELECT 1
           FROM adega_mensalidade mensalidade
           WHERE mensalidade.adega_uuid = pagamento.adega_uuid
             AND mensalidade.status = ''PAGO''
       )',
    'SELECT 1'
);

PREPARE importar_pagamentos_legados FROM @importar_pagamentos_legados;
EXECUTE importar_pagamentos_legados;
DEALLOCATE PREPARE importar_pagamentos_legados;

CREATE TEMPORARY TABLE mensalidade_pendente_legada AS
SELECT
    mensalidade.adega_uuid,
    MAX(mensalidade.id) AS mensalidade_id
FROM adega_mensalidade mensalidade
JOIN pagamento_legado_pago pagamento
  ON pagamento.adega_uuid = mensalidade.adega_uuid
WHERE mensalidade.status = 'PENDENTE'
GROUP BY mensalidade.adega_uuid;

DELETE mensalidade_conflitante
FROM adega_mensalidade mensalidade_conflitante
JOIN mensalidade_pendente_legada mensalidade_pendente
  ON mensalidade_pendente.adega_uuid = mensalidade_conflitante.adega_uuid
 AND mensalidade_pendente.mensalidade_id <> mensalidade_conflitante.id
JOIN pagamento_legado_pago pagamento
  ON pagamento.adega_uuid = mensalidade_conflitante.adega_uuid
 AND pagamento.data_inicio = mensalidade_conflitante.competencia
WHERE mensalidade_conflitante.status = 'PENDENTE';

UPDATE adega_mensalidade mensalidade
JOIN mensalidade_pendente_legada mensalidade_pendente
  ON mensalidade_pendente.mensalidade_id = mensalidade.id
JOIN pagamento_legado_pago pagamento
  ON pagamento.adega_uuid = mensalidade.adega_uuid
SET
    mensalidade.competencia = pagamento.data_inicio,
    mensalidade.status = 'PAGO',
    mensalidade.data_vencimento = DATE_SUB(
        DATE_ADD(pagamento.data_inicio, INTERVAL 1 MONTH),
        INTERVAL 1 DAY
    ),
    mensalidade.data_pagamento = pagamento.data_pagamento;

INSERT INTO adega_mensalidade (
    adega_uuid,
    competencia,
    status,
    data_vencimento,
    data_pagamento
)
SELECT
    pagamento.adega_uuid,
    pagamento.data_inicio,
    'PAGO',
    DATE_SUB(DATE_ADD(pagamento.data_inicio, INTERVAL 1 MONTH), INTERVAL 1 DAY),
    pagamento.data_pagamento
FROM pagamento_legado_pago pagamento
WHERE NOT EXISTS (
    SELECT 1
    FROM adega_mensalidade mensalidade
    WHERE mensalidade.adega_uuid = pagamento.adega_uuid
      AND mensalidade.status = 'PAGO'
);

DELETE mensalidade
FROM adega_mensalidade mensalidade
JOIN pagamento_legado_pago pagamento
  ON pagamento.adega_uuid = mensalidade.adega_uuid
WHERE mensalidade.status = 'PENDENTE'
  AND mensalidade.competencia < pagamento.data_inicio;

DROP TABLE IF EXISTS adega_pagamento;
DROP TEMPORARY TABLE mensalidade_pendente_legada;
DROP TEMPORARY TABLE pagamento_legado_pago;

SELECT COUNT(*) AS tabela_legada_restante
FROM information_schema.tables
WHERE table_schema = DATABASE()
  AND table_name = 'adega_pagamento';
