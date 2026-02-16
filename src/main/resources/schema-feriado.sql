-- Tabela feriado (executado na inicialização da aplicação).
-- DROP primeiro para recriar caso a estrutura tenha mudado (ex.: troca de empresa_id por usuario_id).
DROP TABLE IF EXISTS feriado CASCADE;
CREATE TABLE feriado (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    data                DATE NOT NULL,
    descricao           VARCHAR(255) NOT NULL,
    tipo_feriado_id     INTEGER NOT NULL REFERENCES tipo_feriado(id),
    usuario_id          UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    ativo               BOOLEAN NOT NULL DEFAULT true,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_feriado_data ON feriado(data);
CREATE INDEX IF NOT EXISTS idx_feriado_usuario_data ON feriado(usuario_id, data);
