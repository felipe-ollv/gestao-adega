SELECT
    BIN_TO_UUID(adega.uuid) AS adega_uuid,
    adega.nome,
    mensalidade.competencia,
    mensalidade.status,
    mensalidade.data_vencimento,
    mensalidade.data_pagamento
FROM adega
LEFT JOIN adega_mensalidade mensalidade
  ON mensalidade.adega_uuid = adega.uuid
ORDER BY adega.nome, mensalidade.competencia DESC;
