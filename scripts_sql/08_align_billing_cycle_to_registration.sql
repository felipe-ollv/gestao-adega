CREATE TEMPORARY TABLE mensalidade_ciclo_migracao AS
SELECT
    m.id,
    m.adega_uuid,
    m.competencia AS competencia_anterior,
    DATE_ADD(
        DATE(a.data_cadastro),
        INTERVAL GREATEST(
            TIMESTAMPDIFF(
                MONTH,
                DATE_FORMAT(a.data_cadastro, '%Y-%m-01'),
                m.competencia
            ),
            0
        ) MONTH
    ) AS competencia_nova,
    DATE_SUB(
        DATE_ADD(
            DATE(a.data_cadastro),
            INTERVAL (
                GREATEST(
                    TIMESTAMPDIFF(
                        MONTH,
                        DATE_FORMAT(a.data_cadastro, '%Y-%m-01'),
                        m.competencia
                    ),
                    0
                ) + 1
            ) MONTH
        ),
        INTERVAL 1 DAY
    ) AS vencimento_novo
FROM adega_mensalidade m
JOIN (
    SELECT
        uuid,
        DATE(CONVERT_TZ(data_cadastro, @@session.time_zone, '-03:00')) AS data_cadastro
    FROM adega
) a ON a.uuid = m.adega_uuid;

UPDATE notificacao_email n
JOIN mensalidade_ciclo_migracao c ON c.adega_uuid = n.adega_uuid
SET n.chave_referencia = REPLACE(
    n.chave_referencia,
    CONCAT(':', DATE_FORMAT(c.competencia_anterior, '%Y-%m-%d'), ':'),
    CONCAT(':', DATE_FORMAT(c.competencia_nova, '%Y-%m-%d'), ':')
)
WHERE n.tipo IN (
    'PAGAMENTO_CONFIRMADO',
    'AVISO_VENCIMENTO_MENSALIDADE',
    'MENSALIDADE_VENCIDA'
)
AND n.chave_referencia LIKE CONCAT('%:', DATE_FORMAT(c.competencia_anterior, '%Y-%m-%d'), ':%');

UPDATE adega_mensalidade m
JOIN mensalidade_ciclo_migracao c ON c.id = m.id
SET
    m.competencia = c.competencia_nova,
    m.data_vencimento = c.vencimento_novo;

DROP TEMPORARY TABLE mensalidade_ciclo_migracao;
