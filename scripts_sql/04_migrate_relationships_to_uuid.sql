DROP PROCEDURE IF EXISTS run_if;

DELIMITER //

CREATE PROCEDURE run_if(IN should_run BOOLEAN, IN sql_stmt TEXT)
BEGIN
    IF should_run THEN
        SET @stmt = sql_stmt;
        PREPARE migration_stmt FROM @stmt;
        EXECUTE migration_stmt;
        DEALLOCATE PREPARE migration_stmt;
    END IF;
END//

DELIMITER ;

SET @has_old = (
    SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'usuario' AND column_name = 'adega_id'
);
SET @has_new = (
    SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'usuario' AND column_name = 'adega_uuid'
);
CALL run_if(@has_new = 0, 'ALTER TABLE usuario ADD COLUMN adega_uuid BINARY(16) NULL AFTER uuid');
CALL run_if(@has_old > 0, 'UPDATE usuario u JOIN adega a ON a.id = u.adega_id SET u.adega_uuid = a.uuid WHERE u.adega_uuid IS NULL');
CALL run_if(@has_old > 0, 'ALTER TABLE usuario DROP FOREIGN KEY fk_usuario_adega');
CALL run_if(@has_old > 0, 'ALTER TABLE usuario DROP COLUMN adega_id');
CALL run_if(TRUE, 'ALTER TABLE usuario MODIFY adega_uuid BINARY(16) NOT NULL');
SET @has_fk = (
    SELECT COUNT(*) FROM information_schema.referential_constraints
    WHERE constraint_schema = DATABASE() AND constraint_name = 'fk_usuario_adega_uuid'
);
CALL run_if(@has_fk = 0, 'ALTER TABLE usuario ADD CONSTRAINT fk_usuario_adega_uuid FOREIGN KEY (adega_uuid) REFERENCES adega(uuid)');

SET @has_old = (
    SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'adega_pagamento' AND column_name = 'adega_id'
);
SET @has_new = (
    SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'adega_pagamento' AND column_name = 'adega_uuid'
);
CALL run_if(@has_new = 0, 'ALTER TABLE adega_pagamento ADD COLUMN adega_uuid BINARY(16) NULL AFTER id');
CALL run_if(@has_old > 0, 'UPDATE adega_pagamento p JOIN adega a ON a.id = p.adega_id SET p.adega_uuid = a.uuid WHERE p.adega_uuid IS NULL');
CALL run_if(@has_old > 0, 'ALTER TABLE adega_pagamento DROP FOREIGN KEY fk_pagamento_adega');
CALL run_if(@has_old > 0, 'ALTER TABLE adega_pagamento DROP COLUMN adega_id');
CALL run_if(TRUE, 'ALTER TABLE adega_pagamento MODIFY adega_uuid BINARY(16) NOT NULL');
SET @has_fk = (
    SELECT COUNT(*) FROM information_schema.referential_constraints
    WHERE constraint_schema = DATABASE() AND constraint_name = 'fk_pagamento_adega_uuid'
);
CALL run_if(@has_fk = 0, 'ALTER TABLE adega_pagamento ADD CONSTRAINT fk_pagamento_adega_uuid FOREIGN KEY (adega_uuid) REFERENCES adega(uuid)');

SET @has_old = (
    SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'adega_mensalidade' AND column_name = 'adega_id'
);
SET @has_new = (
    SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'adega_mensalidade' AND column_name = 'adega_uuid'
);
CALL run_if(@has_new = 0, 'ALTER TABLE adega_mensalidade ADD COLUMN adega_uuid BINARY(16) NULL AFTER id');
CALL run_if(@has_old > 0, 'UPDATE adega_mensalidade m JOIN adega a ON a.id = m.adega_id SET m.adega_uuid = a.uuid WHERE m.adega_uuid IS NULL');
CALL run_if(@has_old > 0, 'ALTER TABLE adega_mensalidade DROP FOREIGN KEY fk_mensalidade_adega');
CALL run_if(@has_old > 0, 'ALTER TABLE adega_mensalidade DROP INDEX uk_mensalidade_adega_competencia');
CALL run_if(@has_old > 0, 'ALTER TABLE adega_mensalidade DROP COLUMN adega_id');
CALL run_if(TRUE, 'ALTER TABLE adega_mensalidade MODIFY adega_uuid BINARY(16) NOT NULL');
SET @has_key = (
    SELECT COUNT(*) FROM information_schema.statistics
    WHERE table_schema = DATABASE() AND table_name = 'adega_mensalidade' AND index_name = 'uk_mensalidade_adega_competencia'
);
CALL run_if(@has_key = 0, 'ALTER TABLE adega_mensalidade ADD CONSTRAINT uk_mensalidade_adega_competencia UNIQUE (adega_uuid, competencia)');
SET @has_fk = (
    SELECT COUNT(*) FROM information_schema.referential_constraints
    WHERE constraint_schema = DATABASE() AND constraint_name = 'fk_mensalidade_adega_uuid'
);
CALL run_if(@has_fk = 0, 'ALTER TABLE adega_mensalidade ADD CONSTRAINT fk_mensalidade_adega_uuid FOREIGN KEY (adega_uuid) REFERENCES adega(uuid)');

SET @has_old = (
    SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'produto' AND column_name = 'adega_id'
);
SET @has_new = (
    SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'produto' AND column_name = 'adega_uuid'
);
CALL run_if(@has_new = 0, 'ALTER TABLE produto ADD COLUMN adega_uuid BINARY(16) NULL AFTER uuid');
CALL run_if(@has_old > 0, 'UPDATE produto p JOIN adega a ON a.id = p.adega_id SET p.adega_uuid = a.uuid WHERE p.adega_uuid IS NULL');
CALL run_if(@has_old > 0, 'ALTER TABLE produto DROP FOREIGN KEY fk_produto_adega');
CALL run_if(@has_old > 0, 'ALTER TABLE produto DROP INDEX idx_produto_adega_nome');
CALL run_if(@has_old > 0, 'ALTER TABLE produto DROP COLUMN adega_id');
CALL run_if(TRUE, 'ALTER TABLE produto MODIFY adega_uuid BINARY(16) NOT NULL');
SET @has_key = (
    SELECT COUNT(*) FROM information_schema.statistics
    WHERE table_schema = DATABASE() AND table_name = 'produto' AND index_name = 'idx_produto_adega_nome'
);
CALL run_if(@has_key = 0, 'CREATE INDEX idx_produto_adega_nome ON produto (adega_uuid, nome)');
SET @has_fk = (
    SELECT COUNT(*) FROM information_schema.referential_constraints
    WHERE constraint_schema = DATABASE() AND constraint_name = 'fk_produto_adega_uuid'
);
CALL run_if(@has_fk = 0, 'ALTER TABLE produto ADD CONSTRAINT fk_produto_adega_uuid FOREIGN KEY (adega_uuid) REFERENCES adega(uuid)');

SET @has_old = (
    SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'comanda' AND column_name = 'adega_id'
);
SET @has_new = (
    SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'comanda' AND column_name = 'adega_uuid'
);
CALL run_if(@has_new = 0, 'ALTER TABLE comanda ADD COLUMN adega_uuid BINARY(16) NULL AFTER uuid');
CALL run_if(@has_old > 0, 'UPDATE comanda c JOIN adega a ON a.id = c.adega_id SET c.adega_uuid = a.uuid WHERE c.adega_uuid IS NULL');
CALL run_if(@has_old > 0, 'ALTER TABLE comanda DROP FOREIGN KEY fk_comanda_adega');
CALL run_if(@has_old > 0, 'ALTER TABLE comanda DROP INDEX idx_comanda_adega_status');
CALL run_if(@has_old > 0, 'ALTER TABLE comanda DROP COLUMN adega_id');
CALL run_if(TRUE, 'ALTER TABLE comanda MODIFY adega_uuid BINARY(16) NOT NULL');
SET @has_key = (
    SELECT COUNT(*) FROM information_schema.statistics
    WHERE table_schema = DATABASE() AND table_name = 'comanda' AND index_name = 'idx_comanda_adega_status'
);
CALL run_if(@has_key = 0, 'CREATE INDEX idx_comanda_adega_status ON comanda (adega_uuid, status)');
SET @has_fk = (
    SELECT COUNT(*) FROM information_schema.referential_constraints
    WHERE constraint_schema = DATABASE() AND constraint_name = 'fk_comanda_adega_uuid'
);
CALL run_if(@has_fk = 0, 'ALTER TABLE comanda ADD CONSTRAINT fk_comanda_adega_uuid FOREIGN KEY (adega_uuid) REFERENCES adega(uuid)');

SET @has_old = (
    SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'comanda_item' AND column_name = 'comanda_id'
);
SET @has_new = (
    SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'comanda_item' AND column_name = 'comanda_uuid'
);
CALL run_if(@has_new = 0, 'ALTER TABLE comanda_item ADD COLUMN comanda_uuid BINARY(16) NULL AFTER id');
CALL run_if(@has_old > 0, 'UPDATE comanda_item i JOIN comanda c ON c.id = i.comanda_id SET i.comanda_uuid = c.uuid WHERE i.comanda_uuid IS NULL');
CALL run_if(@has_old > 0, 'ALTER TABLE comanda_item DROP FOREIGN KEY fk_item_comanda');
CALL run_if(@has_old > 0, 'ALTER TABLE comanda_item DROP INDEX idx_item_comanda');
CALL run_if(@has_old > 0, 'ALTER TABLE comanda_item DROP COLUMN comanda_id');
CALL run_if(TRUE, 'ALTER TABLE comanda_item MODIFY comanda_uuid BINARY(16) NOT NULL');
SET @has_key = (
    SELECT COUNT(*) FROM information_schema.statistics
    WHERE table_schema = DATABASE() AND table_name = 'comanda_item' AND index_name = 'idx_item_comanda'
);
CALL run_if(@has_key = 0, 'CREATE INDEX idx_item_comanda ON comanda_item (comanda_uuid)');
SET @has_fk = (
    SELECT COUNT(*) FROM information_schema.referential_constraints
    WHERE constraint_schema = DATABASE() AND constraint_name = 'fk_item_comanda_uuid'
);
CALL run_if(@has_fk = 0, 'ALTER TABLE comanda_item ADD CONSTRAINT fk_item_comanda_uuid FOREIGN KEY (comanda_uuid) REFERENCES comanda(uuid)');

SET @has_old = (
    SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'comanda_item' AND column_name = 'produto_id'
);
SET @has_new = (
    SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'comanda_item' AND column_name = 'produto_uuid'
);
CALL run_if(@has_new = 0, 'ALTER TABLE comanda_item ADD COLUMN produto_uuid BINARY(16) NULL AFTER comanda_uuid');
CALL run_if(@has_old > 0, 'UPDATE comanda_item i JOIN produto p ON p.id = i.produto_id SET i.produto_uuid = p.uuid WHERE i.produto_uuid IS NULL');
CALL run_if(@has_old > 0, 'ALTER TABLE comanda_item DROP FOREIGN KEY fk_item_produto');
CALL run_if(@has_old > 0, 'ALTER TABLE comanda_item DROP INDEX idx_item_produto');
CALL run_if(@has_old > 0, 'ALTER TABLE comanda_item DROP COLUMN produto_id');
CALL run_if(TRUE, 'ALTER TABLE comanda_item MODIFY produto_uuid BINARY(16) NOT NULL');
SET @has_key = (
    SELECT COUNT(*) FROM information_schema.statistics
    WHERE table_schema = DATABASE() AND table_name = 'comanda_item' AND index_name = 'idx_item_produto'
);
CALL run_if(@has_key = 0, 'CREATE INDEX idx_item_produto ON comanda_item (produto_uuid)');
SET @has_fk = (
    SELECT COUNT(*) FROM information_schema.referential_constraints
    WHERE constraint_schema = DATABASE() AND constraint_name = 'fk_item_produto_uuid'
);
CALL run_if(@has_fk = 0, 'ALTER TABLE comanda_item ADD CONSTRAINT fk_item_produto_uuid FOREIGN KEY (produto_uuid) REFERENCES produto(uuid)');

DROP PROCEDURE IF EXISTS run_if;
