-- Consolida a mensalidade como fonte unica do acesso e inicia a validade no pagamento.

CREATE TEMPORARY TABLE mensalidade_paga_normalizada AS
SELECT
    MAX(id) AS id,
    adega_uuid,
    DATE(
        CONVERT_TZ(
            COALESCE(data_pagamento, data_atualizacao),
            @@session.time_zone,
            '-03:00'
        )
    ) AS data_inicio
FROM adega_mensalidade
WHERE status = 'PAGO'
GROUP BY
    adega_uuid,
    DATE(
        CONVERT_TZ(
            COALESCE(data_pagamento, data_atualizacao),
            @@session.time_zone,
            '-03:00'
        )
    );

DELETE mensalidade_conflitante
FROM adega_mensalidade mensalidade_conflitante
JOIN mensalidade_paga_normalizada mensalidade_paga
  ON mensalidade_paga.adega_uuid = mensalidade_conflitante.adega_uuid
 AND mensalidade_paga.data_inicio = mensalidade_conflitante.competencia
 AND mensalidade_paga.id <> mensalidade_conflitante.id;

UPDATE adega_mensalidade mensalidade
JOIN mensalidade_paga_normalizada mensalidade_paga
  ON mensalidade_paga.id = mensalidade.id
SET
    mensalidade.competencia = mensalidade_paga.data_inicio,
    mensalidade.data_vencimento = DATE_SUB(
        DATE_ADD(mensalidade_paga.data_inicio, INTERVAL 1 MONTH),
        INTERVAL 1 DAY
    );

DROP TEMPORARY TABLE mensalidade_paga_normalizada;

-- Importa pagamentos antigos apenas quando o comercio ainda nao tem mensalidade paga.
CREATE TEMPORARY TABLE pagamento_legado_pago AS
SELECT
    pagamento.adega_uuid,
    COALESCE(pagamento.data_pagamento, pagamento.data_atualizacao) AS data_pagamento,
    DATE(
        CONVERT_TZ(
            COALESCE(pagamento.data_pagamento, pagamento.data_atualizacao),
            @@session.time_zone,
            '-03:00'
        )
    ) AS data_inicio
FROM adega_pagamento pagamento
WHERE pagamento.status = 'PAGO'
  AND NOT EXISTS (
      SELECT 1
      FROM adega_mensalidade mensalidade
      WHERE mensalidade.adega_uuid = pagamento.adega_uuid
        AND mensalidade.status = 'PAGO'
  );

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
  AND mensalidade.competencia <= pagamento.data_inicio;

DROP TEMPORARY TABLE mensalidade_pendente_legada;
DROP TEMPORARY TABLE pagamento_legado_pago;

-- Pendencias nao representam periodo de acesso antes da confirmacao do pagamento.
UPDATE adega_mensalidade
SET data_vencimento = competencia
WHERE status = 'PENDENTE';

DROP TABLE adega_pagamento;
