SET @adega_uuid_texto = 'SUBSTITUA-PELO-UUID-DO-COMERCIO';
SET @adega_uuid = UUID_TO_BIN(@adega_uuid_texto);
SET @data_inicio = DATE(CONVERT_TZ(CURRENT_TIMESTAMP, @@session.time_zone, '-03:00'));

-- O agendador de e-mails detectara o status PAGO e enviara a confirmacao.

START TRANSACTION;

SET @mensalidade_id = (
    SELECT id
    FROM adega_mensalidade
    WHERE adega_uuid = @adega_uuid
      AND competencia = @data_inicio
    ORDER BY id DESC
    LIMIT 1
);

SET @mensalidade_id = COALESCE(
    @mensalidade_id,
    (
        SELECT id
        FROM adega_mensalidade
        WHERE adega_uuid = @adega_uuid
          AND status = 'PENDENTE'
        ORDER BY id DESC
        LIMIT 1
    )
);

UPDATE adega_mensalidade
SET
    competencia = @data_inicio,
    status = 'PAGO',
    data_vencimento = DATE_SUB(
        DATE_ADD(@data_inicio, INTERVAL 1 MONTH),
        INTERVAL 1 DAY
    ),
    data_pagamento = CURRENT_TIMESTAMP
WHERE id = @mensalidade_id;

INSERT INTO adega_mensalidade (
    adega_uuid,
    competencia,
    status,
    data_vencimento,
    data_pagamento
)
SELECT
    @adega_uuid,
    @data_inicio,
    'PAGO',
    DATE_SUB(DATE_ADD(@data_inicio, INTERVAL 1 MONTH), INTERVAL 1 DAY),
    CURRENT_TIMESTAMP
WHERE @mensalidade_id IS NULL;

DELETE FROM adega_mensalidade
WHERE adega_uuid = @adega_uuid
  AND status = 'PENDENTE'
  AND competencia <= @data_inicio;

COMMIT;

SELECT
    BIN_TO_UUID(mensalidade.adega_uuid) AS adega_uuid,
    mensalidade.competencia,
    mensalidade.status,
    mensalidade.data_vencimento,
    mensalidade.data_pagamento
FROM adega_mensalidade mensalidade
WHERE mensalidade.adega_uuid = @adega_uuid
ORDER BY mensalidade.competencia DESC;
