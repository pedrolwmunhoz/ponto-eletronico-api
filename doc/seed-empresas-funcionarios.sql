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

    INSERT INTO user_credential (usuario_id, tipo_credencial_id, categoria_credential_id, valor, ativo)
    VALUES (v_empresa_id, v_tipo_email, v_tipo_primario, 'empresa' || v_empresa_num || '@seed.com', true);

    INSERT INTO user_password (usuario_id, senha_hash, ativo)
    VALUES (v_empresa_id, v_senha_hash, true);

    INSERT INTO empresa_dados_fiscal (empresa_id, razao_social, cnpj)
    VALUES (v_empresa_id, 'Razão Social Empresa ' || v_empresa_num, LPAD((10000000000000 + v_empresa_num)::TEXT, 14, '0'));

    INSERT INTO empresa_endereco (empresa_id, rua, numero, complemento, bairro, cidade, uf, cep)
    VALUES (v_empresa_id, 'Rua das Empresas', (100 + v_empresa_num)::TEXT, 'Sala ' || v_empresa_num, 'Centro', 'São Paulo', 'SP', '01310100');

    INSERT INTO usuario_telefone (usuario_id, codigo_pais, ddd, numero, ativo)
    VALUES (v_empresa_id, '55', '11', LPAD((900000000 + v_empresa_num)::TEXT, 9, '0'), true);

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

      INSERT INTO user_credential (usuario_id, tipo_credencial_id, categoria_credential_id, valor, ativo)
      VALUES (v_func_id, v_tipo_email, v_tipo_primario, 'func' || v_func_num || '.emp' || v_empresa_num || '@seed.com', true);

      INSERT INTO user_password (usuario_id, senha_hash, ativo)
      VALUES (v_func_id, v_senha_hash, true);

      -- CPF único: 11 dígitos, formato 100.empresa(2d).func(2d).digitos
      v_cpf_base := 10000000000 + (v_empresa_num * 1000) + v_func_num;
      v_cpf_str := LPAD(v_cpf_base::TEXT, 11, '0');
      v_cpf_str := SUBSTR(v_cpf_str,1,3) || '.' || SUBSTR(v_cpf_str,4,3) || '.' || SUBSTR(v_cpf_str,7,3) || '-' || SUBSTR(v_cpf_str,10,2);

      v_matricula := 'E' || LPAD(v_empresa_num::TEXT, 2, '0') || 'F' || LPAD(v_func_num::TEXT, 2, '0');
      v_codigo_ponto := (v_empresa_num - 1) * 20 + v_func_num;

      INSERT INTO identificacao_funcionario (funcionario_id, empresa_id, nome_completo, cpf, codigo_ponto, data_nascimento)
      VALUES (v_func_id, v_empresa_id, 'Funcionário ' || v_func_num || ' Empresa ' || v_empresa_num, v_cpf_str, v_codigo_ponto, (CURRENT_DATE - (25 + (v_func_num % 15)) * INTERVAL '1 year')::DATE);

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

      INSERT INTO usuario_telefone (usuario_id, codigo_pais, ddd, numero, ativo)
      VALUES (v_func_id, '55', '11', LPAD((910000000 + v_empresa_num * 20 + v_func_num)::TEXT, 9, '0'), true);
    END LOOP;
  END LOOP;

  RAISE NOTICE 'Seed concluído: 20 empresas e 400 funcionários (20 por empresa). Senha única para todos.';
END $$;
