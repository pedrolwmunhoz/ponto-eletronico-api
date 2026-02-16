-- ============================================================================
-- SEED: 20 empresas + 20 funcionários por empresa (400 funcionários)
-- Senha para TODOS (empresas e funcionários): hash abaixo (bcrypt).
-- Executar APÓS schema.sql (tabelas e INSERTs de tipo_* já existirem).
--
-- Login empresa:  empresa1@seed.com .. empresa20@seed.com  (username: empresa1 .. empresa20)
-- Login funcionário:  func1.emp1@seed.com .. func20.emp20@seed.com  (username: func1_emp1 .. func20_emp20)
-- ============================================================================

DO $$
DECLARE
  v_senha_hash     VARCHAR(255) := '$2a$10$rqNg.2gdi.dQxC9huNOFkuauljL8XuGZNesLa4S4BJAtc2n20nUuy';
  v_tipo_empresa   INTEGER;
  v_tipo_func      INTEGER;
  v_tipo_email     INTEGER;
  v_tipo_primario  INTEGER;
  v_tipo_clt       INTEGER;
  v_escala_5x2     INTEGER;
  v_modelo_ponto   INTEGER;
  v_empresa_id     UUID;
  v_func_id        UUID;
  v_empresa_num    INTEGER;
  v_func_num       INTEGER;
  v_cpf_base       BIGINT;
  v_cpf_str        VARCHAR(14);
  v_matricula      VARCHAR(50);
  v_codigo_ponto   INTEGER;
  v_primeiro       VARCHAR(100);
  v_ultimo         VARCHAR(100);
  v_nome_completo  VARCHAR(255);
  v_idx            INTEGER;
  v_primeiros      VARCHAR[] := ARRAY['João','Maria','Pedro','Ana','Carlos','Lucas','Juliana','Fernanda','Roberto','Patrícia','Ricardo','Camila','Bruno','Amanda','Marcos','Larissa','Felipe','Beatriz','Rafael','Daniela'];
  v_ultimos        VARCHAR[] := ARRAY['Silva','Santos','Oliveira','Souza','Lima','Costa','Ferreira','Rodrigues','Almeida','Nascimento','Pereira','Carvalho','Ribeiro','Alves','Martins','Araújo','Melo','Barbosa','Cardoso','Dias'];
BEGIN
  SELECT id INTO v_tipo_empresa   FROM tipo_usuario WHERE descricao = 'EMPRESA' LIMIT 1;
  SELECT id INTO v_tipo_func      FROM tipo_usuario WHERE descricao = 'FUNCIONARIO' LIMIT 1;
  SELECT id INTO v_tipo_email     FROM tipo_credential WHERE descricao = 'EMAIL' LIMIT 1;
  SELECT id INTO v_tipo_primario  FROM tipo_categoria_credential WHERE descricao = 'PRIMARIO' LIMIT 1;
  SELECT id INTO v_tipo_clt      FROM tipo_contrato WHERE descricao = 'CLT' LIMIT 1;
  SELECT id INTO v_escala_5x2     FROM tipo_escala_jornada WHERE descricao = '5x2' LIMIT 1;
  SELECT id INTO v_modelo_ponto   FROM tipo_modelo_ponto WHERE descricao = 'ELETRONICO' LIMIT 1;

  IF v_tipo_empresa IS NULL OR v_tipo_func IS NULL OR v_tipo_email IS NULL OR v_tipo_primario IS NULL
     OR v_tipo_clt IS NULL OR v_escala_5x2 IS NULL OR v_modelo_ponto IS NULL THEN
    RAISE EXCEPTION 'Tipos/catálogos não encontrados. Execute schema.sql antes.';
  END IF;

  -- ========== 20 EMPRESAS ==========
  FOR v_empresa_num IN 1..20 LOOP
    INSERT INTO users (id, username, ativo, tipo_usuario_id)
    VALUES (gen_random_uuid(), 'empresa' || v_empresa_num, true, v_tipo_empresa)
    RETURNING id INTO v_empresa_id;

    INSERT INTO user_credential (usuario_id, tipo_credencial_id, categoria_credential_id, valor)
    VALUES (v_empresa_id, v_tipo_email, v_tipo_primario, 'empresa' || v_empresa_num || '@seed.com');

    INSERT INTO user_password (usuario_id, senha_hash, ativo)
    VALUES (v_empresa_id, v_senha_hash, true);

    INSERT INTO empresa_dados_fiscal (empresa_id, razao_social, cnpj)
    VALUES (v_empresa_id, 'Razão Social Empresa ' || v_empresa_num, LPAD((10000000000000 + v_empresa_num)::TEXT, 14, '0'));

    INSERT INTO empresa_endereco (empresa_id, rua, numero, complemento, bairro, cidade, uf, cep)
    VALUES (v_empresa_id, 'Rua das Empresas', (100 + v_empresa_num)::TEXT, 'Sala ' || v_empresa_num, 'Centro', 'São Paulo', 'SP', '01310100');

    INSERT INTO usuario_telefone (usuario_id, codigo_pais, ddd, numero)
    VALUES (v_empresa_id, '55', '11', LPAD((900000000 + v_empresa_num)::TEXT, 9, '0'));

    INSERT INTO empresa_jornada_config (
      empresa_id, tipo_escala_jornada_id, carga_horaria_diaria, carga_horaria_semanal,
      tolerancia_padrao, intervalo_padrao, entrada_padrao, saida_padrao,
      tempo_descanso_entre_jornada, timezone, grava_geo_obrigatoria,
      grava_ponto_apenas_em_geofence, permite_ajuste_ponto
    )
    VALUES (
      v_empresa_id, v_escala_5x2, 'PT8H', 'PT44H', 'PT0S', 'PT1H',
      '08:00'::TIME, '17:00'::TIME, 'PT11H', 'America/Sao_Paulo',
      false, false, true
    );

    INSERT INTO empresa_compliance (
      empresa_id, controle_ponto_obrigatorio, tipo_modelo_ponto_id,
      tempo_retencao_anos, auditoria_ativa, assinatura_digital_obrigatoria
    )
    VALUES (v_empresa_id, true, v_modelo_ponto, 5, true, true);

    INSERT INTO empresa_banco_horas_config (empresa_id, ativo, total_dias_vencimento)
    VALUES (v_empresa_id, true, 365);

    -- ========== 20 FUNCIONÁRIOS POR EMPRESA ==========
    FOR v_func_num IN 1..20 LOOP
      INSERT INTO users (id, username, ativo, tipo_usuario_id)
      VALUES (gen_random_uuid(), 'func' || v_func_num || '_emp' || v_empresa_num, true, v_tipo_func)
      RETURNING id INTO v_func_id;

      INSERT INTO user_credential (usuario_id, tipo_credencial_id, categoria_credential_id, valor)
      VALUES (v_func_id, v_tipo_email, v_tipo_primario, 'func' || v_func_num || '.emp' || v_empresa_num || '@seed.com');

      INSERT INTO user_password (usuario_id, senha_hash, ativo)
      VALUES (v_func_id, v_senha_hash, true);

      -- CPF único: 11 dígitos, formato 100.empresa(2d).func(2d).digitos
      v_cpf_base := 10000000000 + (v_empresa_num * 1000) + v_func_num;
      v_cpf_str := LPAD(v_cpf_base::TEXT, 11, '0');
      v_cpf_str := SUBSTR(v_cpf_str,1,3) || '.' || SUBSTR(v_cpf_str,4,3) || '.' || SUBSTR(v_cpf_str,7,3) || '-' || SUBSTR(v_cpf_str,10,2);

      v_matricula := 'E' || LPAD(v_empresa_num::TEXT, 2, '0') || 'F' || LPAD(v_func_num::TEXT, 2, '0');
      v_codigo_ponto := (v_empresa_num - 1) * 20 + v_func_num;

      -- Nome completo com primeiro_nome e ultimo_nome para listagens
      v_idx := 1 + ((v_empresa_num - 1) * 20 + v_func_num - 1) % 20;
      v_primeiro := v_primeiros[v_idx];
      v_ultimo := v_ultimos[v_idx];
      v_nome_completo := v_primeiro || ' ' || v_ultimo;

      INSERT INTO identificacao_funcionario (funcionario_id, empresa_id, nome_completo, primeiro_nome, ultimo_nome, cpf, codigo_ponto, data_nascimento)
      VALUES (v_func_id, v_empresa_id, v_nome_completo, v_primeiro, v_ultimo, v_cpf_str, v_codigo_ponto, (CURRENT_DATE - (25 + (v_func_num % 15)) * INTERVAL '1 year')::DATE);

      INSERT INTO funcionario_registro_lock (funcionario_id, empresa_id)
      VALUES (v_func_id, v_empresa_id);

      INSERT INTO contrato_funcionario (
        funcionario_id, matricula, pis_pasep, cargo, departamento, tipo_contrato_id,
        ativo, data_admissao, salario_mensal, salario_hora
      )
      VALUES (
        v_func_id, v_matricula, NULL, 'Cargo ' || v_func_num, 'Departamento ' || ((v_func_num - 1) % 5 + 1), v_tipo_clt,
        true, (CURRENT_DATE - (v_func_num % 4) * INTERVAL '1 year')::DATE, 3000.00 + (v_func_num * 100), 17.05
      );

      INSERT INTO jornada_funcionario_config (
        funcionario_id, tipo_escala_jornada_id, carga_horaria_diaria, carga_horaria_semanal,
        tolerancia_padrao, intervalo_padrao, entrada_padrao, saida_padrao,
        tempo_descanso_entre_jornada, grava_geo_obrigatoria
      )
      VALUES (
        v_func_id, v_escala_5x2, 'PT8H', 'PT44H', 'PT0S', 'PT1H',
        '08:00'::TIME, '17:00'::TIME, 'PT11H', false
      );

      INSERT INTO usuario_telefone (usuario_id, codigo_pais, ddd, numero)
      VALUES (v_func_id, '55', '11', LPAD((910000000 + v_empresa_num * 20 + v_func_num)::TEXT, 9, '0'));
    END LOOP;
  END LOOP;

  RAISE NOTICE 'Seed concluído: 20 empresas e 400 funcionários (20 por empresa). Senha única para todos.';
END $$;

-- ============================================================================
-- SEED: Registros de ponto do mês atual para cada funcionário
-- 4 batidas por dia útil (entrada, saida, entrada, saida) conforme config.
-- Escala 5x2: segunda a sexta. Cria registro_ponto, resumo_ponto_dia, xref, banco_horas_mensal.
-- Executar APÓS o bloco anterior (empresas e funcionários criados).
-- ============================================================================

DO $$
DECLARE
  v_func_id        UUID;
  v_empresa_id     UUID;
  v_dispositivo_id UUID;
  v_resumo_id      UUID;
  v_reg_id         UUID;
  v_dia            DATE;
  v_fim_mes        DATE;
  v_entrada1       TIMESTAMP;
  v_saida1         TIMESTAMP;
  v_entrada2       TIMESTAMP;
  v_saida2         TIMESTAMP;
  v_dia_semana     VARCHAR(3);
  v_dow            INTEGER;
  v_carga_diaria   VARCHAR(20) := 'PT8H';
  v_tipo_manual    INTEGER := 1;
  r                RECORD;
BEGIN
  SELECT id INTO v_tipo_manual FROM tipo_marcacao WHERE descricao = 'MANUAL' LIMIT 1;
  IF v_tipo_manual IS NULL THEN
    v_tipo_manual := 1;
  END IF;

  FOR r IN
    SELECT if_.funcionario_id, if_.empresa_id
    FROM identificacao_funcionario if_
    JOIN users u ON u.id = if_.funcionario_id
    WHERE u.ativo = true
  LOOP
    v_func_id := r.funcionario_id;
    v_empresa_id := r.empresa_id;

    -- Dispositivo por funcionário (ip/ua fixos para seed)
    SELECT id INTO v_dispositivo_id FROM dispositivo WHERE usuario_id = v_func_id AND ip_address = '127.0.0.1' LIMIT 1;
    IF v_dispositivo_id IS NULL THEN
      v_dispositivo_id := gen_random_uuid();
      INSERT INTO dispositivo (id, usuario_id, ip_address, user_agent)
      VALUES (v_dispositivo_id, v_func_id, '127.0.0.1', 'SEED');
    END IF;

    v_dia := DATE_TRUNC('month', CURRENT_DATE)::DATE;
    v_fim_mes := (v_dia + INTERVAL '1 month' - INTERVAL '1 day')::DATE;

    WHILE v_dia <= v_fim_mes LOOP
      v_dow := EXTRACT(ISODOW FROM v_dia)::INTEGER; -- 1=Mon, 7=Sun
      IF v_dow BETWEEN 1 AND 5 THEN
        v_dia_semana := (ARRAY['','SEG','TER','QUA','QUI','SEX','SAB','DOM'])[v_dow];
        v_entrada1 := v_dia + TIME '08:00';
        v_saida1   := v_dia + TIME '12:00';
        v_entrada2 := v_dia + TIME '13:00';
        v_saida2   := v_dia + TIME '17:00';

        v_resumo_id := gen_random_uuid();
        INSERT INTO resumo_ponto_dia (
          id, funcionario_id, empresa_id,
          primeira_batida, ultima_batida,
          total_horas_trabalhadas, total_horas_esperadas,
          inconsistente, created_at
        )
        VALUES (
          v_resumo_id, v_func_id, v_empresa_id,
          v_entrada1, v_saida2,
          v_carga_diaria, v_carga_diaria,
          false, v_entrada1
        );

        INSERT INTO registro_ponto (id, idempotency_key, usuario_id, dia_semana, dispositivo_id, tipo_marcacao_id, tipo_entrada, created_at)
        VALUES (gen_random_uuid(), gen_random_uuid(), v_func_id, v_dia_semana, v_dispositivo_id, v_tipo_manual, true, v_entrada1)
        RETURNING id INTO v_reg_id;
        INSERT INTO xref_ponto_resumo (id, funcionario_id, registro_ponto_id, resumo_ponto_dia_id, created_at)
        VALUES (gen_random_uuid(), v_func_id, v_reg_id, v_resumo_id, v_entrada1);

        INSERT INTO registro_ponto (id, idempotency_key, usuario_id, dia_semana, dispositivo_id, tipo_marcacao_id, tipo_entrada, created_at)
        VALUES (gen_random_uuid(), gen_random_uuid(), v_func_id, v_dia_semana, v_dispositivo_id, v_tipo_manual, false, v_saida1)
        RETURNING id INTO v_reg_id;
        INSERT INTO xref_ponto_resumo (id, funcionario_id, registro_ponto_id, resumo_ponto_dia_id, created_at)
        VALUES (gen_random_uuid(), v_func_id, v_reg_id, v_resumo_id, v_saida1);

        INSERT INTO registro_ponto (id, idempotency_key, usuario_id, dia_semana, dispositivo_id, tipo_marcacao_id, tipo_entrada, created_at)
        VALUES (gen_random_uuid(), gen_random_uuid(), v_func_id, v_dia_semana, v_dispositivo_id, v_tipo_manual, true, v_entrada2)
        RETURNING id INTO v_reg_id;
        INSERT INTO xref_ponto_resumo (id, funcionario_id, registro_ponto_id, resumo_ponto_dia_id, created_at)
        VALUES (gen_random_uuid(), v_func_id, v_reg_id, v_resumo_id, v_entrada2);

        INSERT INTO registro_ponto (id, idempotency_key, usuario_id, dia_semana, dispositivo_id, tipo_marcacao_id, tipo_entrada, created_at)
        VALUES (gen_random_uuid(), gen_random_uuid(), v_func_id, v_dia_semana, v_dispositivo_id, v_tipo_manual, false, v_saida2)
        RETURNING id INTO v_reg_id;
        INSERT INTO xref_ponto_resumo (id, funcionario_id, registro_ponto_id, resumo_ponto_dia_id, created_at)
        VALUES (gen_random_uuid(), v_func_id, v_reg_id, v_resumo_id, v_saida2);
      END IF;
      v_dia := v_dia + 1;
    END LOOP;
  END LOOP;

  -- Recalcular banco_horas_mensal (soma real dos resumos do mês por funcionário)
  -- Parse PT8H / PT8H30M para minutos, soma, formata de volta
  INSERT INTO banco_horas_mensal (
    id, funcionario_id, empresa_id, mes_ref, ano_ref,
    total_horas_esperadas, total_horas_trabalhadas, total_horas_trabalhadas_feriado,
    inconsistente, ativo, created_at
  )
  WITH parsed AS (
    SELECT
      res.funcionario_id,
      res.empresa_id,
      EXTRACT(MONTH FROM res.primeira_batida)::INTEGER AS mes_ref,
      EXTRACT(YEAR FROM res.primeira_batida)::INTEGER AS ano_ref,
      COALESCE((regexp_match(res.total_horas_esperadas, 'PT([0-9]+)H'))[1]::int, 0) * 60
        + COALESCE((regexp_match(res.total_horas_esperadas, '([0-9]+)M'))[1]::int, 0) AS mins_esp,
      COALESCE((regexp_match(res.total_horas_trabalhadas, 'PT([0-9]+)H'))[1]::int, 0) * 60
        + COALESCE((regexp_match(res.total_horas_trabalhadas, '([0-9]+)M'))[1]::int, 0) AS mins_trab
    FROM resumo_ponto_dia res
    WHERE res.primeira_batida >= DATE_TRUNC('month', CURRENT_DATE)
      AND res.primeira_batida < DATE_TRUNC('month', CURRENT_DATE) + INTERVAL '1 month'
  ),
  aggregated AS (
    SELECT
      funcionario_id,
      empresa_id,
      mes_ref,
      ano_ref,
      SUM(mins_esp) AS total_mins_esp,
      SUM(mins_trab) AS total_mins_trab
    FROM parsed
    GROUP BY funcionario_id, empresa_id, mes_ref, ano_ref
  )
  SELECT
    gen_random_uuid(),
    a.funcionario_id,
    a.empresa_id,
    a.mes_ref,
    a.ano_ref,
    CASE WHEN a.total_mins_esp = 0 THEN 'PT0S'
         WHEN a.total_mins_esp % 60 = 0 THEN 'PT' || (a.total_mins_esp / 60) || 'H'
         ELSE 'PT' || (a.total_mins_esp / 60) || 'H' || (a.total_mins_esp % 60) || 'M' END,
    CASE WHEN a.total_mins_trab = 0 THEN 'PT0S'
         WHEN a.total_mins_trab % 60 = 0 THEN 'PT' || (a.total_mins_trab / 60) || 'H'
         ELSE 'PT' || (a.total_mins_trab / 60) || 'H' || (a.total_mins_trab % 60) || 'M' END,
    'PT0S',
    false,
    true,
    CURRENT_TIMESTAMP
  FROM aggregated a
  ON CONFLICT (funcionario_id, ano_ref, mes_ref) DO UPDATE SET
    total_horas_esperadas = EXCLUDED.total_horas_esperadas,
    total_horas_trabalhadas = EXCLUDED.total_horas_trabalhadas,
    total_horas_trabalhadas_feriado = EXCLUDED.total_horas_trabalhadas_feriado;

  RAISE NOTICE 'Registros de ponto do mês atual criados: 4 batidas/dia útil por funcionário. banco_horas_mensal atualizado.';
END $$;

-- ============================================================================
-- SEED: Métricas diárias da empresa (por dia + hoje)
-- Acumulativos: quantidade_funcionarios (distribuído), solicitacoes_pendentes=0
-- Resetam por dia: total_do_dia (horas), total_ponto_hoje (registros)
-- Executar APÓS o bloco de ponto. Pode rodar sozinho para refrescar métricas.
-- ============================================================================

DO $$
DECLARE
  v_inicio_mes DATE;
  v_fim_mes    DATE;
  v_hoje       DATE;
BEGIN
  v_inicio_mes := DATE_TRUNC('month', (NOW() AT TIME ZONE 'America/Sao_Paulo')::TIMESTAMP)::DATE;
  v_fim_mes    := v_inicio_mes + INTERVAL '1 month' - INTERVAL '1 day';
  v_hoje       := (NOW() AT TIME ZONE 'America/Sao_Paulo')::DATE;

  INSERT INTO metricas_diaria_empresa (
    id, empresa_id, data_ref, ano_ref, mes_ref,
    quantidade_funcionarios, solicitacoes_pendentes,
    total_do_dia, total_ponto_hoje
  )
  WITH dias_mes AS (
    SELECT (v_inicio_mes + (g - 1) * INTERVAL '1 day')::DATE AS d
    FROM generate_series(1, EXTRACT(DAY FROM v_fim_mes)::INT) g
  ),
  empresas AS (
    SELECT DISTINCT empresa_id FROM identificacao_funcionario
  ),
  funcs_com_rn AS (
    SELECT if_.empresa_id, if_.funcionario_id,
      row_number() OVER (PARTITION BY if_.empresa_id ORDER BY if_.funcionario_id) AS rn
    FROM identificacao_funcionario if_
    JOIN users u ON u.id = if_.funcionario_id
    WHERE u.ativo = true
  ),
  dias_no_mes AS (
    SELECT EXTRACT(DAY FROM v_fim_mes)::INT AS n
  ),
  qtd_func_por_dia AS (
    SELECT e.empresa_id, dm.d,
      (SELECT COUNT(*) FROM funcs_com_rn f
       WHERE f.empresa_id = e.empresa_id
         AND 1 + ((f.rn - 1) * 7 + (f.rn % 5)) % GREATEST(1, (SELECT n FROM dias_no_mes)) <= EXTRACT(DAY FROM dm.d)
      ) AS qtd_func
    FROM empresas e
    CROSS JOIN dias_mes dm
  ),
  totais_resumo AS (
    SELECT res.empresa_id, res.primeira_batida::DATE AS d,
      COALESCE((regexp_match(res.total_horas_trabalhadas, 'PT([0-9]+)H'))[1]::int, 0) * 60
        + COALESCE((regexp_match(res.total_horas_trabalhadas, '([0-9]+)M'))[1]::int, 0) AS mins
    FROM resumo_ponto_dia res
    WHERE res.primeira_batida >= v_inicio_mes
      AND res.primeira_batida < v_fim_mes + INTERVAL '1 day'
  ),
  soma_horas_dia AS (
    SELECT empresa_id, d, SUM(mins) AS total_mins
    FROM totais_resumo
    GROUP BY empresa_id, d
  ),
  registros_por_dia AS (
    SELECT res.empresa_id, xpr.created_at::DATE AS d,
      COUNT(*) AS cnt
    FROM xref_ponto_resumo xpr
    JOIN resumo_ponto_dia res ON res.id = xpr.resumo_ponto_dia_id
    WHERE xpr.created_at >= v_inicio_mes
      AND xpr.created_at < v_fim_mes + INTERVAL '1 day'
    GROUP BY res.empresa_id, xpr.created_at::DATE
  )
  SELECT
    gen_random_uuid(),
    q.empresa_id,
    q.d,
    EXTRACT(YEAR FROM q.d)::INTEGER,
    EXTRACT(MONTH FROM q.d)::INTEGER,
    q.qtd_func,
    0,
    CASE WHEN COALESCE(s.total_mins, 0) = 0 THEN 'PT0S'
         WHEN COALESCE(s.total_mins, 0) % 60 = 0 THEN 'PT' || (COALESCE(s.total_mins, 0) / 60) || 'H'
         ELSE 'PT' || (COALESCE(s.total_mins, 0) / 60) || 'H' || (COALESCE(s.total_mins, 0) % 60) || 'M' END,
    COALESCE(rh.cnt, 0)::INTEGER
  FROM qtd_func_por_dia q
  LEFT JOIN soma_horas_dia s ON s.empresa_id = q.empresa_id AND s.d = q.d
  LEFT JOIN registros_por_dia rh ON rh.empresa_id = q.empresa_id AND rh.d = q.d
  ON CONFLICT (empresa_id, data_ref) DO UPDATE SET
    quantidade_funcionarios = EXCLUDED.quantidade_funcionarios,
    solicitacoes_pendentes = EXCLUDED.solicitacoes_pendentes,
    total_do_dia = EXCLUDED.total_do_dia,
    total_ponto_hoje = EXCLUDED.total_ponto_hoje;

  RAISE NOTICE 'Métricas diárias atualizadas: total_do_dia e total_ponto_hoje por dia (incl. hoje).';
END $$;