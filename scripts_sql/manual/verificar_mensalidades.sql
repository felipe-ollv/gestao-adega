SELECT
    BIN_TO_UUID(adega.uuid) AS adega_uuid,
    adega.nome,
    mensalidade.competencia,
    mensalidade.status,
    mensalidade.data_vencimento,
    mensalidade.data_pagamento,
    CASE
        WHEN mensalidade.status <> 'PAGO' THEN NULL
        WHEN mensalidade.competencia = DATE(
            COALESCE(mensalidade.data_pagamento, mensalidade.data_atualizacao)
        )
        AND mensalidade.data_vencimento = DATE_SUB(
            DATE_ADD(
                DATE(COALESCE(mensalidade.data_pagamento, mensalidade.data_atualizacao)),
                INTERVAL 1 MONTH
            ),
            INTERVAL 1 DAY
        ) THEN 'SIM'
        ELSE 'NAO'
    END AS vigencia_consistente
FROM adega
LEFT JOIN adega_mensalidade mensalidade
  ON mensalidade.adega_uuid = adega.uuid
ORDER BY adega.nome, mensalidade.competencia DESC;
