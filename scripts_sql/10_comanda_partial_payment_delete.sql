ALTER TABLE comanda
    ADD COLUMN valor_pago_parcial DECIMAL(10,2) NOT NULL DEFAULT 0.00 AFTER status,
    ADD COLUMN data_exclusao TIMESTAMP NULL AFTER data_fechamento,
    ADD COLUMN observacao_exclusao VARCHAR(500) NULL AFTER valor_pago_parcial;

ALTER TABLE comanda
    DROP CHECK chk_comanda_status;

ALTER TABLE comanda
    ADD CONSTRAINT chk_comanda_status CHECK (status IN ('ABERTA', 'PAGA', 'FIADO', 'EXCLUIDA'));
