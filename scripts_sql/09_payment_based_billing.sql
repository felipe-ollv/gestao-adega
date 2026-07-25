-- Consolida a mensalidade como fonte unica do acesso e inicia a validade no pagamento.

CREATE TEMPORARY TABLE mensalidade_paga_normalizada AS
SELECT
    MAX(id) AS id,
    adega_uuid,
    DATE(COALESCE(data_pagamento, data_atualizacao)) AS data_inicio
FROM adega_mensalidade
WHERE status = 'PAGO'
GROUP BY
    adega_uuid,
    DATE(COALESCE(data_pagamento, data_atualizacao));

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

-- Pendencias nao representam periodo de acesso antes da confirmacao do pagamento.
UPDATE adega_mensalidade
SET data_vencimento = competencia
WHERE status = 'PENDENTE';
