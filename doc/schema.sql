-- ============================================================================
-- SCRIPT COMPLETO DE CRIAÇÃO DO SCHEMA - SISTEMA DE PONTO SaaS
-- PostgreSQL - Todas as tabelas e tipos de dados
-- Ordem respeitando dependências de Foreign Keys
-- ============================================================================
-- DATAS: Todas as colunas de data/hora são TIMESTAMP (sem time zone) = LocalDateTime.
--       Não usar TIMESTAMPTZ. Mapear como LocalDateTime.
-- ============================================================================

-- UUID: usar gen_random_uuid() (built-in desde PostgreSQL 13). Se estiver em versão antiga, descomente abaixo:
-- CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
-- e troque gen_random_uuid() por gen_random_uuid() no script.

-- Busca por nome insensível a acentos (ex.: "joao" encontra "João")
CREATE EXTENSION IF NOT EXISTS unaccent;

-- Tipo ENUM para severidade de alertas (opcional)
DO $$ BEGIN
    CREATE TYPE severidade_alerta AS ENUM ('BAIXA', 'MEDIA', 'ALTA', 'CRITICA');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

-- ============================================================================
-- TABELAS DE DOMÍNIO (Tipos/Catálogos) - Sem dependências
-- ============================================================================

-- 1. tipo_usuario
CREATE TABLE IF NOT EXISTS tipo_usuario (
    id              SERIAL PRIMARY KEY,
    descricao       VARCHAR(100) NOT NULL UNIQUE,
    ativo           BOOLEAN NOT NULL DEFAULT true
);

-- 2. tipo_contrato
CREATE TABLE IF NOT EXISTS tipo_contrato (
    id              SERIAL PRIMARY KEY,
    descricao       VARCHAR(100) NOT NULL UNIQUE,
    ativo           BOOLEAN NOT NULL DEFAULT true
);

-- 3. tipo_adicional_trabalho
CREATE TABLE IF NOT EXISTS tipo_adicional_trabalho (
    id              SERIAL PRIMARY KEY,
    descricao       VARCHAR(100) NOT NULL UNIQUE,
    ativo           BOOLEAN NOT NULL DEFAULT true
);

-- 4. tipo_escala_jornada
CREATE TABLE IF NOT EXISTS tipo_escala_jornada (
    id              SERIAL PRIMARY KEY,
    descricao       VARCHAR(100) NOT NULL UNIQUE,
    ativo           BOOLEAN NOT NULL DEFAULT true
);

-- 5. tipo_dispositivo
CREATE TABLE IF NOT EXISTS tipo_dispositivo (
    id              SERIAL PRIMARY KEY,
    descricao       VARCHAR(100) NOT NULL UNIQUE,
    ativo           BOOLEAN NOT NULL DEFAULT true
);

-- 6. tipo_modelo_ponto
CREATE TABLE IF NOT EXISTS tipo_modelo_ponto (
    id              SERIAL PRIMARY KEY,
    descricao       VARCHAR(100) NOT NULL UNIQUE,
    ativo           BOOLEAN NOT NULL DEFAULT true
);

-- 7. tipo_marcacao
CREATE TABLE IF NOT EXISTS tipo_marcacao (
    id              SERIAL PRIMARY KEY,
    descricao       VARCHAR(100) NOT NULL UNIQUE,
    ativo           BOOLEAN NOT NULL DEFAULT true
);

-- 8. tipo_justificativa
CREATE TABLE IF NOT EXISTS tipo_justificativa (
    id              SERIAL PRIMARY KEY,
    descricao       VARCHAR(255) NOT NULL UNIQUE,
    ativo           BOOLEAN NOT NULL DEFAULT true
);

-- 9. tipo_solicitacao_ponto (CRIAR | REMOVER)
CREATE TABLE IF NOT EXISTS tipo_solicitacao_ponto (
    id              SERIAL PRIMARY KEY,
    descricao       VARCHAR(50) NOT NULL UNIQUE,
    ativo           BOOLEAN NOT NULL DEFAULT true
);

-- 10. tipo_credential
CREATE TABLE IF NOT EXISTS tipo_credential (
    id              SERIAL PRIMARY KEY,
    descricao       VARCHAR(100) NOT NULL UNIQUE,
    ativo           BOOLEAN NOT NULL DEFAULT true
);

-- 11. tipo_alerta
CREATE TABLE IF NOT EXISTS tipo_alerta (
    id              SERIAL PRIMARY KEY,
    descricao       VARCHAR(100) NOT NULL UNIQUE,
    ativo           BOOLEAN NOT NULL DEFAULT true
);

-- 12. tipo_afastamento
CREATE TABLE IF NOT EXISTS tipo_afastamento (
    id              SERIAL PRIMARY KEY,
    descricao       VARCHAR(255) NOT NULL UNIQUE,
    ativo           BOOLEAN NOT NULL DEFAULT true
);

-- 13. tipo_notificacao
CREATE TABLE IF NOT EXISTS tipo_notificacao (
    id              SERIAL PRIMARY KEY,
    descricao       VARCHAR(100) NOT NULL UNIQUE,
    ativo           BOOLEAN NOT NULL DEFAULT true
);

-- 14. tipo_token_recuperacao
CREATE TABLE IF NOT EXISTS tipo_token_recuperacao (
    id              SERIAL PRIMARY KEY,
    descricao       VARCHAR(100) NOT NULL UNIQUE,
    ativo           BOOLEAN NOT NULL DEFAULT true
);

-- 15. tipo_categoria_credential (PRIMARIO | SECUNDARIO - indica se credencial é principal ou secundária do usuário)
CREATE TABLE IF NOT EXISTS tipo_categoria_credential (
    id              SERIAL PRIMARY KEY,
    descricao       VARCHAR(100) NOT NULL UNIQUE,
    ativo           BOOLEAN NOT NULL DEFAULT true
);

-- 16. tipo_status_pagamento (PENDENTE | PARCIAL | PAGO - status de pagamento/compensação do banco de horas)
CREATE TABLE IF NOT EXISTS tipo_status_pagamento (
    id              SERIAL PRIMARY KEY,
    descricao       VARCHAR(50) NOT NULL UNIQUE,
    ativo           BOOLEAN NOT NULL DEFAULT true
);

-- 17. tipo_feriado (NACIONAL | ESTADUAL | MUNICIPAL - abrangência do feriado)
CREATE TABLE IF NOT EXISTS tipo_feriado (
    id              SERIAL PRIMARY KEY,
    descricao       VARCHAR(50) NOT NULL UNIQUE,
    ativo           BOOLEAN NOT NULL DEFAULT true
);

-- ============================================================================
-- DADOS INICIAIS - Tipos/Catálogos (Seed Data)
-- INSERT com WHERE NOT EXISTS = idempotente, funciona sem UNIQUE(descricao)
-- ============================================================================

INSERT INTO tipo_usuario (descricao)
SELECT x FROM (VALUES ('FUNCIONARIO'), ('EMPRESA'), ('ADMIN')) AS t(x)
WHERE NOT EXISTS (SELECT 1 FROM tipo_usuario WHERE descricao = t.x);

INSERT INTO tipo_contrato (descricao)
SELECT x FROM (VALUES ('CLT'), ('PJ'), ('ESTAGIO')) AS t(x)
WHERE NOT EXISTS (SELECT 1 FROM tipo_contrato WHERE descricao = t.x);

INSERT INTO tipo_adicional_trabalho (descricao)
SELECT x FROM (VALUES ('NOTURNO'), ('PERICULOSIDADE'), ('INSALUBRIDADE')) AS t(x)
WHERE NOT EXISTS (SELECT 1 FROM tipo_adicional_trabalho WHERE descricao = t.x);

INSERT INTO tipo_escala_jornada (descricao)
SELECT x FROM (VALUES ('5x2'), ('6x1'), ('12x36')) AS t(x)
WHERE NOT EXISTS (SELECT 1 FROM tipo_escala_jornada WHERE descricao = t.x);

INSERT INTO tipo_dispositivo (descricao)
SELECT x FROM (VALUES ('WEB'), ('MOBILE'), ('BIOMETRIA')) AS t(x)
WHERE NOT EXISTS (SELECT 1 FROM tipo_dispositivo WHERE descricao = t.x);

INSERT INTO tipo_modelo_ponto (descricao)
SELECT x FROM (VALUES ('ELETRONICO')) AS t(x)
WHERE NOT EXISTS (SELECT 1 FROM tipo_modelo_ponto WHERE descricao = t.x);

INSERT INTO tipo_marcacao (descricao)
SELECT x FROM (VALUES ('MANUAL'), ('SISTEMA')) AS t(x)
WHERE NOT EXISTS (SELECT 1 FROM tipo_marcacao WHERE descricao = t.x);

INSERT INTO tipo_credential (descricao)
SELECT x FROM (VALUES ('EMAIL'), ('TELEFONE'), ('CPF'), ('CNPJ'), ('USERNAME')) AS t(x)
WHERE NOT EXISTS (SELECT 1 FROM tipo_credential WHERE descricao = t.x);

INSERT INTO tipo_alerta (descricao)
SELECT x FROM (VALUES ('ATRASO'), ('SAIDA_ANTECIPADA'), ('HORAS_EXCEDIDAS'), ('FALTA_REGISTRO'), ('DIVERGENCIA_ESPELHAMENTO')) AS t(x)
WHERE NOT EXISTS (SELECT 1 FROM tipo_alerta WHERE descricao = t.x);

INSERT INTO tipo_justificativa (descricao)
SELECT x FROM (VALUES ('ATRASO_TRANSPORTE'), ('EMERGENCIA_MEDICA'), ('PROBLEMA_TECNICO'), ('ESQUECI_BATER'), ('AJUSTE_MANUAL'), ('FALHA_SISTEMA'), ('REGISTRO_DUPLICADO'), ('REGISTRO_ERRADO'), ('OUTROS')) AS t(x)
WHERE NOT EXISTS (SELECT 1 FROM tipo_justificativa WHERE descricao = t.x);

INSERT INTO tipo_solicitacao_ponto (descricao)
SELECT x FROM (VALUES ('CRIAR'), ('REMOVER')) AS t(x)
WHERE NOT EXISTS (SELECT 1 FROM tipo_solicitacao_ponto WHERE descricao = t.x);

INSERT INTO tipo_afastamento (descricao)
SELECT x FROM (VALUES ('FERIAS'), ('LICENCA_MEDICA'), ('ATESTADO'), ('LICENCA_MATERNIDADE'), ('FALTA_JUSTIFICADA')) AS t(x)
WHERE NOT EXISTS (SELECT 1 FROM tipo_afastamento WHERE descricao = t.x);

INSERT INTO tipo_notificacao (descricao)
SELECT x FROM (VALUES ('ALERTA_DIVERGENCIA'), ('AJUSTE_APROVADO'), ('BANCO_HORAS_VENCIDO'), ('SISTEMA')) AS t(x)
WHERE NOT EXISTS (SELECT 1 FROM tipo_notificacao WHERE descricao = t.x);

INSERT INTO tipo_token_recuperacao (descricao)
SELECT x FROM (VALUES ('CODIGO_EMAIL'), ('TOKEN_RESET')) AS t(x)
WHERE NOT EXISTS (SELECT 1 FROM tipo_token_recuperacao WHERE descricao = t.x);

INSERT INTO tipo_categoria_credential (descricao)
SELECT x FROM (VALUES ('PRIMARIO'), ('SECUNDARIO')) AS t(x)
WHERE NOT EXISTS (SELECT 1 FROM tipo_categoria_credential WHERE descricao = t.x);

INSERT INTO tipo_status_pagamento (descricao)
SELECT x FROM (VALUES ('PENDENTE'), ('PARCIAL'), ('PAGO')) AS t(x)
WHERE NOT EXISTS (SELECT 1 FROM tipo_status_pagamento WHERE descricao = t.x);

INSERT INTO tipo_feriado (descricao)
SELECT x FROM (VALUES ('NACIONAL'), ('ESTADUAL'), ('MUNICIPAL')) AS t(x)
WHERE NOT EXISTS (SELECT 1 FROM tipo_feriado WHERE descricao = t.x);

-- ============================================================================
-- USERS - Entidade base
-- ============================================================================

-- 17. users
CREATE TABLE IF NOT EXISTS users (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username        VARCHAR(255) NOT NULL UNIQUE,
    ativo           BOOLEAN NOT NULL DEFAULT true,
    data_desativacao TIMESTAMP NULL,
    tipo_usuario_id INTEGER NOT NULL REFERENCES tipo_usuario(id),
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================================
-- USUÁRIO - Tabelas compartilhadas (funcionários e empresas)
-- ============================================================================

-- 18. usuario_telefone (delete físico; sem ativo/data_desativacao)
CREATE TABLE IF NOT EXISTS usuario_telefone (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id      UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    codigo_pais     VARCHAR(10) NOT NULL,
    ddd             VARCHAR(5) NOT NULL,
    numero          VARCHAR(20) NOT NULL,
    UNIQUE(codigo_pais, ddd, numero)
);

CREATE INDEX IF NOT EXISTS idx_usuario_telefone_usuario_id ON usuario_telefone(usuario_id);

-- 19. usuario_geofence
CREATE TABLE IF NOT EXISTS usuario_geofence (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id      UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    descricao       VARCHAR(255) NOT NULL,
    latitude        NUMERIC(12, 8) NOT NULL,
    longitude       NUMERIC(12, 8) NOT NULL,
    raio_metros     INTEGER NOT NULL DEFAULT 100,
    ativo           BOOLEAN NOT NULL DEFAULT true,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_usuario_geofence_usuario_id ON usuario_geofence(usuario_id);

-- ============================================================================
-- FUNCIONÁRIO
-- ============================================================================

-- 20. identificacao_funcionario (entidade: funcionario_id sem UNIQUE; apenas UNIQUE(empresa_id, funcionario_id))
CREATE TABLE IF NOT EXISTS identificacao_funcionario (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    funcionario_id  UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    empresa_id      UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    nome_completo   VARCHAR(255) NOT NULL,
    primeiro_nome   VARCHAR(100) NOT NULL,
    ultimo_nome     VARCHAR(100) NOT NULL,
    cpf             VARCHAR(14) NOT NULL UNIQUE,
    codigo_ponto    INTEGER NOT NULL CHECK (codigo_ponto >= 0 AND codigo_ponto <= 999999),
    data_nascimento DATE NULL,
    updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(empresa_id, funcionario_id)
);

-- 21. funcionario_registro_lock (lock por funcionário para serializar registro de ponto)
CREATE TABLE IF NOT EXISTS funcionario_registro_lock (
    funcionario_id  UUID PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    empresa_id      UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE
);

-- 22. contrato_funcionario
CREATE TABLE IF NOT EXISTS contrato_funcionario (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    funcionario_id  UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    matricula       VARCHAR(50) NULL UNIQUE,
    pis_pasep       VARCHAR(20) NULL UNIQUE,
    cargo           VARCHAR(255) NOT NULL,
    departamento    VARCHAR(255) NULL,
    tipo_contrato_id INTEGER NOT NULL REFERENCES tipo_contrato(id),
    ativo           BOOLEAN NOT NULL DEFAULT true,
    data_admissao   DATE NOT NULL,
    data_demissao   DATE NULL,
    salario_mensal  NUMERIC(15, 2) NOT NULL,
    salario_hora    NUMERIC(10, 4) NOT NULL,
    updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 23. xref_contrato_adicionais (N:N contrato <-> adicionais)
CREATE TABLE IF NOT EXISTS xref_contrato_adicionais (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    contrato_id             UUID NOT NULL REFERENCES contrato_funcionario(id) ON DELETE CASCADE,
    tipo_adicional_trabalho_id INTEGER NOT NULL REFERENCES tipo_adicional_trabalho(id),
    UNIQUE(contrato_id, tipo_adicional_trabalho_id)
);

-- 24. jornada_funcionario_config (se existir, sobrescreve empresa_jornada_config para o funcionário)
CREATE TABLE IF NOT EXISTS jornada_funcionario_config (
    id                          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    funcionario_id              UUID NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    tipo_escala_jornada_id      INTEGER NOT NULL REFERENCES tipo_escala_jornada(id),
    carga_horaria_diaria        VARCHAR(20) NOT NULL,
    carga_horaria_semanal       VARCHAR(20) NOT NULL,
    tolerancia_padrao           VARCHAR(20) NOT NULL DEFAULT 'PT0S',
    intervalo_padrao            VARCHAR(20) NOT NULL,
    entrada_padrao              TIME NOT NULL,
    saida_padrao                TIME NOT NULL,
    tempo_descanso_entre_jornada VARCHAR(20) NOT NULL DEFAULT 'PT11H',
    grava_geo_obrigatoria       BOOLEAN NOT NULL DEFAULT false,
    updated_at                  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================================
-- EMPRESA
-- ============================================================================

-- 25. empresa_dados_fiscal
CREATE TABLE IF NOT EXISTS empresa_dados_fiscal (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    empresa_id      UUID NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    razao_social    VARCHAR(255) NOT NULL,
    cnpj            VARCHAR(18) NOT NULL UNIQUE,
    updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 26. empresa_endereco
CREATE TABLE IF NOT EXISTS empresa_endereco (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    empresa_id      UUID NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    rua             VARCHAR(255) NOT NULL,
    numero          VARCHAR(20) NOT NULL,
    complemento     VARCHAR(255) NULL,
    bairro          VARCHAR(255) NOT NULL,
    cidade          VARCHAR(255) NOT NULL,
    uf              CHAR(2) NOT NULL,
    cep             VARCHAR(8) NOT NULL CHECK (LENGTH(cep) = 8),
    updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 27. geofence_empresa_config
CREATE TABLE IF NOT EXISTS geofence_empresa_config (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    geofence_id     UUID NOT NULL UNIQUE REFERENCES usuario_geofence(id) ON DELETE CASCADE,
    updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 28. xref_geofence_funcionarios (N:N geofence <-> funcionários)
CREATE TABLE IF NOT EXISTS xref_geofence_funcionarios (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    geofence_id     UUID NOT NULL REFERENCES usuario_geofence(id) ON DELETE CASCADE,
    funcionario_id  UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE(geofence_id, funcionario_id)
);

-- 29. empresa_jornada_config
CREATE TABLE IF NOT EXISTS empresa_jornada_config (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    empresa_id              UUID NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    tipo_escala_jornada_id  INTEGER NOT NULL REFERENCES tipo_escala_jornada(id),
    carga_horaria_diaria    VARCHAR(20) NOT NULL,
    carga_horaria_semanal   VARCHAR(20) NOT NULL,
    tolerancia_padrao           VARCHAR(20) NOT NULL DEFAULT 'PT0S',
    intervalo_padrao            VARCHAR(20) NOT NULL,
    entrada_padrao          TIME NOT NULL,
    saida_padrao            TIME NOT NULL,
    tempo_descanso_entre_jornada VARCHAR(20) NOT NULL DEFAULT 'PT11H',
    timezone                VARCHAR(50) NOT NULL DEFAULT 'America/Sao_Paulo',
    grava_geo_obrigatoria   BOOLEAN NOT NULL DEFAULT false,
    grava_ponto_apenas_em_geofence BOOLEAN NOT NULL DEFAULT false,
    permite_ajuste_ponto    BOOLEAN NOT NULL DEFAULT false,
    updated_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 30. empresa_compliance
CREATE TABLE IF NOT EXISTS empresa_compliance (
    id                          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    empresa_id                  UUID NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    controle_ponto_obrigatorio  BOOLEAN NOT NULL DEFAULT true,
    tipo_modelo_ponto_id        INTEGER NOT NULL REFERENCES tipo_modelo_ponto(id),
    tempo_retencao_anos         INTEGER NOT NULL CHECK (tempo_retencao_anos >= 5),
    auditoria_ativa             BOOLEAN NOT NULL DEFAULT true,
    assinatura_digital_obrigatoria BOOLEAN NOT NULL DEFAULT true,
    updated_at                  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 31. empresa_banco_horas_config
CREATE TABLE IF NOT EXISTS empresa_banco_horas_config (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    empresa_id              UUID NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    ativo                   BOOLEAN NOT NULL DEFAULT false,
    total_dias_vencimento   INTEGER NOT NULL,
    updated_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 32. metricas_diaria_empresa
-- Snapshot diário por empresa (um registro por dia). total_ponto_hoje reseta a cada dia;
-- total_do_dia = total de horas trabalhadas no dia (Duration).
CREATE TABLE IF NOT EXISTS metricas_diaria_empresa (
    id                                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    empresa_id                          UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    data_ref                            DATE NOT NULL,
    ano_ref                             INTEGER NOT NULL,
    mes_ref                             INTEGER NOT NULL,
    quantidade_funcionarios             INTEGER NOT NULL DEFAULT 0,
    solicitacoes_pendentes              INTEGER NOT NULL DEFAULT 0,
    total_do_dia                        VARCHAR(20) NOT NULL DEFAULT 'PT0S',
    total_ponto_hoje                    INTEGER NOT NULL DEFAULT 0,
    UNIQUE(empresa_id, data_ref)
);

CREATE INDEX IF NOT EXISTS idx_metricas_diaria_empresa_empresa_data ON metricas_diaria_empresa(empresa_id, data_ref);
CREATE INDEX IF NOT EXISTS idx_metricas_diaria_empresa_ano_mes ON metricas_diaria_empresa(empresa_id, ano_ref, mes_ref);

-- 33. metricas_diaria_empresa_lock (lock por empresa + data_ref: adquirir ANTES de obter/criar métrica para evitar race)
CREATE TABLE IF NOT EXISTS metricas_diaria_empresa_lock (
    empresa_id   UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    data_ref     DATE NOT NULL,
    PRIMARY KEY (empresa_id, data_ref)
);

-- ============================================================================
-- CREDENCIAL / DISPOSITIVO (precisam existir antes de registro_ponto)
-- Cadastro de empresa: cria user_credential (valor=email, tipo=EMAIL) + user_password
-- ============================================================================

-- 34. user_credential (delete físico; sem ativo/data_desativacao)
CREATE TABLE IF NOT EXISTS user_credential (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id              UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    tipo_credencial_id      INTEGER NOT NULL REFERENCES tipo_credential(id),
    categoria_credential_id INTEGER NOT NULL REFERENCES tipo_categoria_credential(id) DEFAULT 1,
    valor                   VARCHAR(255) NOT NULL UNIQUE
);

CREATE INDEX IF NOT EXISTS idx_user_credential_usuario_id ON user_credential(usuario_id);

-- 35. user_password (usuario_id: senha única por usuário, não por credencial. Desativar antiga, inserir nova. Nunca UPDATE.)
CREATE TABLE IF NOT EXISTS user_password (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id      UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    senha_hash      VARCHAR(255) NOT NULL,
    ativo           BOOLEAN NOT NULL DEFAULT true,
    data_expiracao  TIMESTAMP NULL,
    data_desativacao TIMESTAMP NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
-- Índice parcial: apenas uma senha ativa por usuário
CREATE UNIQUE INDEX IF NOT EXISTS idx_user_password_usuario_ativo ON user_password(usuario_id) WHERE ativo = true AND data_desativacao IS NULL;

-- 36. historico_bloqueio (usuario_id UNIQUE - uma única linha ativa por usuário)
CREATE TABLE IF NOT EXISTS historico_bloqueio (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id      UUID NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    ativo           BOOLEAN NOT NULL DEFAULT true,
    data_bloqueio   TIMESTAMP NULL,
    motivo_bloqueio TEXT NULL,
    data_desativacao TIMESTAMP NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_historico_bloqueio_usuario_ativo ON historico_bloqueio(usuario_id, ativo);

-- 37. credencial_token_recuperacao
CREATE TABLE IF NOT EXISTS credencial_token_recuperacao (
    id                          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id                  UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    tipo_token_recuperacao_id   INTEGER NOT NULL REFERENCES tipo_token_recuperacao(id),
    token                       VARCHAR(255) NOT NULL UNIQUE,
    data_expiracao              TIMESTAMP NOT NULL,
    ativo                       BOOLEAN NOT NULL DEFAULT true,
    data_desativacao            TIMESTAMP NULL,
    created_at                  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_credencial_token_usuario_expiracao ON credencial_token_recuperacao(usuario_id, data_expiracao);
CREATE INDEX IF NOT EXISTS idx_credencial_token_token ON credencial_token_recuperacao(token);

-- 38. dispositivo
CREATE TABLE IF NOT EXISTS dispositivo (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id          UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    nome_dispositivo    VARCHAR(255) NULL,
    sistema_operacional VARCHAR(100) NULL,
    versao_app          VARCHAR(50) NULL,
    modelo_dispositivo  VARCHAR(255) NULL,
    ip_address          VARCHAR(45) NULL,
    user_agent          TEXT NULL
);

CREATE INDEX IF NOT EXISTS idx_dispositivo_usuario_id ON dispositivo(usuario_id);

-- 39. historico_login
CREATE TABLE IF NOT EXISTS historico_login (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id      UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    credencial_id   UUID NOT NULL REFERENCES user_credential(id) ON DELETE CASCADE,
    data_login      TIMESTAMP NOT NULL,
    dispositivo_id  UUID NULL REFERENCES dispositivo(id) ON DELETE SET NULL,
    sucesso         BOOLEAN NOT NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_historico_login_credencial_data ON historico_login(credencial_id, data_login);
CREATE INDEX IF NOT EXISTS idx_historico_login_dispositivo ON historico_login(dispositivo_id);

-- 40. sessao_ativa
CREATE TABLE IF NOT EXISTS sessao_ativa (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id      UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    credencial_id   UUID NOT NULL REFERENCES user_credential(id) ON DELETE CASCADE,
    token           VARCHAR(512) NOT NULL UNIQUE,
    dispositivo_id  UUID NULL REFERENCES dispositivo(id) ON DELETE SET NULL,
    ativo           BOOLEAN NOT NULL DEFAULT true,
    data_expiracao  TIMESTAMP NOT NULL,
    data_desativacao TIMESTAMP NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_sessao_ativa_credencial ON sessao_ativa(credencial_id);
CREATE INDEX IF NOT EXISTS idx_sessao_ativa_token ON sessao_ativa(token);
CREATE INDEX IF NOT EXISTS idx_sessao_ativa_expiracao ON sessao_ativa(data_expiracao);

-- ============================================================================
-- REGISTRO DE PONTO (Imutável - Verdade Fiscal)
-- ============================================================================

-- 41. registro_ponto
-- created_at: TIMESTAMP = LocalDateTime (sem timezone). Momento do registro. dia_semana gravado no INSERT a partir de created_at (SEG, TER, QUA, QUI, SEX, SAB, DOM)
-- tipo_entrada: true=entrada, false=saída (definido no registro com base no estado da jornada)
CREATE TABLE IF NOT EXISTS registro_ponto (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    idempotency_key UUID NOT NULL UNIQUE,
    usuario_id      UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    dia_semana      VARCHAR(3) NOT NULL,
    dispositivo_id  UUID NOT NULL REFERENCES dispositivo(id) ON DELETE RESTRICT,
    tipo_marcacao_id INTEGER NOT NULL REFERENCES tipo_marcacao(id),
    tipo_entrada    BOOLEAN NOT NULL DEFAULT true,
    descricao       TEXT NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(usuario_id, created_at)
);

CREATE INDEX IF NOT EXISTS idx_registro_ponto_usuario_created ON registro_ponto(usuario_id, created_at);

-- 42. resumo_ponto_dia (jornada - conjunto de registros com intervalo menor que tempo_descanso_entre_jornada)
-- total_horas_*: VARCHAR(20), formato serializado pela JVM (ex: PT8H30M)
CREATE TABLE IF NOT EXISTS resumo_ponto_dia (
    id                              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    funcionario_id                  UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    empresa_id                      UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    primeira_batida                 TIMESTAMP NULL,
    ultima_batida                   TIMESTAMP NULL,
    total_horas_trabalhadas         VARCHAR(20) NOT NULL DEFAULT 'PT0S',
    total_horas_trabalhadas_feriado VARCHAR(20) NOT NULL DEFAULT 'PT0S',
    total_horas_esperadas           VARCHAR(20) NOT NULL DEFAULT 'PT0S',
    inconsistente           BOOLEAN NOT NULL DEFAULT false,
    motivo_inconsistencia   VARCHAR(50) NULL,
    created_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_resumo_ponto_dia_empresa_primeira ON resumo_ponto_dia(empresa_id, primeira_batida);
CREATE INDEX IF NOT EXISTS idx_resumo_ponto_dia_funcionario_primeira ON resumo_ponto_dia(funcionario_id, primeira_batida);

-- 43. estado_jornada_funcionario (estado vivo: última batida, tipo e última jornada para decidir próxima)
CREATE TABLE IF NOT EXISTS estado_jornada_funcionario (
    funcionario_id      UUID PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    empresa_id          UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    ultima_batida       TIMESTAMP NOT NULL,
    tipo_ultima_batida  VARCHAR(7) NOT NULL CHECK (tipo_ultima_batida IN ('ENTRADA', 'SAIDA')),
    ultima_jornada_id   UUID NULL REFERENCES resumo_ponto_dia(id) ON DELETE SET NULL,
    updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 44. xref_ponto_resumo (vínculo entre registro_ponto e resumo_ponto_dia / jornada)
CREATE TABLE IF NOT EXISTS xref_ponto_resumo (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    funcionario_id      UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    registro_ponto_id   UUID NOT NULL UNIQUE REFERENCES registro_ponto(id) ON DELETE CASCADE,
    resumo_ponto_dia_id UUID NOT NULL REFERENCES resumo_ponto_dia(id) ON DELETE CASCADE,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(funcionario_id, created_at)
);

CREATE INDEX IF NOT EXISTS idx_xref_ponto_resumo_resumo_id ON xref_ponto_resumo(resumo_ponto_dia_id);
CREATE INDEX IF NOT EXISTS idx_xref_ponto_resumo_registro_id ON xref_ponto_resumo(registro_ponto_id);
CREATE INDEX IF NOT EXISTS idx_xref_ponto_resumo_funcionario_id ON xref_ponto_resumo(funcionario_id);

-- 45. registro_metadados
CREATE TABLE IF NOT EXISTS registro_metadados (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    registro_id         UUID NOT NULL UNIQUE REFERENCES registro_ponto(id) ON DELETE CASCADE,
    geo_latitude        DOUBLE PRECISION NULL,
    geo_longitude       DOUBLE PRECISION NULL,
    assinatura_digital  TEXT NOT NULL,
    certificado_serial  VARCHAR(255) NOT NULL,
    timestamp_assinatura TIMESTAMP NOT NULL,
    updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 46. solicitacao_ponto (unificada: criar registro ou remover registro)
-- tipo_solicitacao_id: 1=CRIAR (usa data_hora_registro), 2=REMOVER (usa registro_ponto_id)
-- empresa_aprovacao_id = empresa (users.id da empresa) que aprovou
-- idempotency_key: evita duplicidade ao criar solicitação (header Idempotency-Key).
-- Datas: TIMESTAMP = LocalDateTime (não usar timestamptz)
CREATE TABLE IF NOT EXISTS solicitacao_ponto (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    idempotency_key         UUID NOT NULL UNIQUE,
    usuario_id              UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    tipo_solicitacao_id     INTEGER NOT NULL REFERENCES tipo_solicitacao_ponto(id),
    data_hora_registro      TIMESTAMP NULL,
    registro_ponto_id       UUID NULL REFERENCES registro_ponto(id) ON DELETE CASCADE,
    tipo_justificativa_id   INTEGER NOT NULL REFERENCES tipo_justificativa(id),
    aprovado                BOOLEAN NULL,
    empresa_aprovacao_id    UUID NULL REFERENCES users(id) ON DELETE SET NULL,
    observacao_aprovacao    TEXT NULL,
    created_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_aprovacao          TIMESTAMP NULL
);

CREATE INDEX IF NOT EXISTS idx_solicitacao_ponto_usuario ON solicitacao_ponto(usuario_id);
CREATE INDEX IF NOT EXISTS idx_solicitacao_ponto_tipo ON solicitacao_ponto(tipo_solicitacao_id);
CREATE INDEX IF NOT EXISTS idx_solicitacao_ponto_aprovado ON solicitacao_ponto(aprovado);

-- ============================================================================
-- ALERTAS / DIVERGÊNCIAS
-- ============================================================================

-- 47. alerta_divergencia
CREATE TABLE IF NOT EXISTS alerta_divergencia (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    funcionario_id          UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    data_divergencia        DATE NOT NULL,
    tipo_alerta_id          INTEGER NOT NULL REFERENCES tipo_alerta(id),
    registro_ponto_id       UUID NULL REFERENCES registro_ponto(id) ON DELETE SET NULL,
    descricao               TEXT NOT NULL,
    severidade              VARCHAR(20) NOT NULL CHECK (severidade IN ('BAIXA', 'MEDIA', 'ALTA', 'CRITICA')),
    ativo                   BOOLEAN NOT NULL DEFAULT true,
    data_resolucao          TIMESTAMP NULL,
    usuario_resolucao_id    UUID NULL REFERENCES users(id) ON DELETE SET NULL,
    observacao_resolucao    TEXT NULL,
    tipo_justificativa_id   INTEGER NULL REFERENCES tipo_justificativa(id),
    created_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_alerta_divergencia_funcionario_data ON alerta_divergencia(funcionario_id, data_divergencia);
CREATE INDEX IF NOT EXISTS idx_alerta_divergencia_tipo_ativo ON alerta_divergencia(tipo_alerta_id, ativo);
CREATE INDEX IF NOT EXISTS idx_alerta_divergencia_ativo_created ON alerta_divergencia(ativo, created_at);

-- ============================================================================
-- BANCO DE HORAS (fechamento mensal)
-- ============================================================================

-- 48. banco_horas_historico (fechamento mensal por funcionário)
CREATE TABLE IF NOT EXISTS banco_horas_historico (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    funcionario_id          UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    ano_referencia          INTEGER NOT NULL,
    mes_referencia          INTEGER NOT NULL,
    total_horas_esperadas   INTEGER NOT NULL DEFAULT 0,
    total_horas_trabalhadas INTEGER NOT NULL DEFAULT 0,
    total_banco_horas_final INTEGER NOT NULL DEFAULT 0,
    status                  VARCHAR(20) NOT NULL DEFAULT 'FECHADO',
    valor_compensado_parcial INTEGER NOT NULL DEFAULT 0,
    tipo_status_pagamento_id INTEGER NOT NULL DEFAULT 1 REFERENCES tipo_status_pagamento(id),
    ativo                   BOOLEAN NOT NULL DEFAULT true,
    data_desativacao        TIMESTAMP NULL,
    created_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(funcionario_id, ano_referencia, mes_referencia)
);

CREATE INDEX IF NOT EXISTS idx_banco_horas_historico_funcionario ON banco_horas_historico(funcionario_id);
CREATE INDEX IF NOT EXISTS idx_banco_horas_historico_ativo ON banco_horas_historico(ativo);
CREATE INDEX IF NOT EXISTS idx_banco_horas_historico_ano_mes ON banco_horas_historico(ano_referencia, mes_referencia);

-- 49. banco_horas_mensal (totais mensais por funcionário; recalculado ao alterar ponto manual / soft delete)
-- total_horas_*: VARCHAR(20), formato serializado pela JVM (ex: PT8H30M), como em resumo_ponto_dia
CREATE TABLE IF NOT EXISTS banco_horas_mensal (
    id                          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    funcionario_id              UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    empresa_id                  UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    mes_ref                     INTEGER NOT NULL,
    ano_ref                     INTEGER NOT NULL,
    total_horas_esperadas       VARCHAR(20) NOT NULL DEFAULT 'PT0S',
    total_horas_trabalhadas     VARCHAR(20) NOT NULL DEFAULT 'PT0S',
    total_horas_trabalhadas_feriado VARCHAR(20) NOT NULL DEFAULT 'PT0S',
    inconsistente               BOOLEAN NOT NULL DEFAULT false,
    motivo_inconsistencia       VARCHAR(255) NULL,
    ativo                       BOOLEAN NOT NULL DEFAULT true,
    data_desativacao            TIMESTAMP NULL,
    created_at                  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(funcionario_id, ano_ref, mes_ref)
);

CREATE INDEX IF NOT EXISTS idx_banco_horas_mensal_funcionario ON banco_horas_mensal(funcionario_id);
CREATE INDEX IF NOT EXISTS idx_banco_horas_mensal_empresa_ano_mes ON banco_horas_mensal(empresa_id, ano_ref, mes_ref);

-- 50. afastamento
CREATE TABLE IF NOT EXISTS afastamento (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    funcionario_id      UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    tipo_afastamento_id INTEGER NOT NULL REFERENCES tipo_afastamento(id),
    data_inicio         DATE NOT NULL,
    data_fim            DATE NULL,
    observacao          TEXT NULL,
    ativo               BOOLEAN NOT NULL DEFAULT true,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_afastamento_funcionario_inicio ON afastamento(funcionario_id, data_inicio);
CREATE INDEX IF NOT EXISTS idx_afastamento_ativo_inicio ON afastamento(ativo, data_inicio);

-- ============================================================================
-- FERIADOS (Nacional, Estadual, Municipal)
-- ============================================================================

-- 51. feriado
-- usuario_id = usuário que criou o feriado (da tabela users). Empresa ou Admin.
-- Listagem empresa: feriados onde usuario_id = empresa logada OU usuario_id pertence a Admin (join users.tipo_usuario_id = ADMIN).
CREATE TABLE IF NOT EXISTS feriado (
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

-- ============================================================================
-- AUDITORIA
-- ============================================================================

-- 52. auditoria_log
CREATE TABLE IF NOT EXISTS auditoria_log (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id      UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    acao            VARCHAR(50) NOT NULL,
    descricao       TEXT NULL,
    dados_antigos   JSONB NULL,
    dados_novos     JSONB NULL,
    dispositivo_id  UUID NULL REFERENCES dispositivo(id) ON DELETE SET NULL,
    ip_address      VARCHAR(45) NULL,
    user_agent      TEXT NULL,
    sucesso         BOOLEAN NOT NULL,
    mensagem_erro   TEXT NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_auditoria_log_usuario_created ON auditoria_log(usuario_id, created_at);
CREATE INDEX IF NOT EXISTS idx_auditoria_log_dispositivo ON auditoria_log(dispositivo_id);
CREATE INDEX IF NOT EXISTS idx_auditoria_log_acao_created ON auditoria_log(acao, created_at);

-- ============================================================================
-- NOTIFICAÇÕES
-- ============================================================================

-- 53. notificacao
CREATE TABLE IF NOT EXISTS notificacao (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id          UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    titulo              VARCHAR(255) NOT NULL,
    mensagem            TEXT NOT NULL,
    tipo_notificacao_id INTEGER NOT NULL REFERENCES tipo_notificacao(id),
    lida                BOOLEAN NOT NULL DEFAULT false,
    data_leitura        TIMESTAMP NULL,
    link_acao           VARCHAR(500) NULL,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_notificacao_usuario_lida ON notificacao(usuario_id, lida);
CREATE INDEX IF NOT EXISTS idx_notificacao_lida_created ON notificacao(lida, created_at);

-- ============================================================================
-- FIM DO SCRIPT
-- ============================================================================
