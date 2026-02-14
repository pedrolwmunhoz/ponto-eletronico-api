package com.pontoeletronico.api.infrastructure.output.repository.empresa;

import com.pontoeletronico.api.domain.entity.usuario.Users;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface FuncionarioPerfilRepository extends Repository<Users, UUID> {

    @Query(value = """
            SELECT
                u.username                          AS username,
                u.ativo                             AS funcionarioAtivo,

                if_.nome_completo                   AS nomeCompleto,
                if_.cpf                             AS cpf,
                if_.data_nascimento                 AS dataNascimento,

                cf.matricula                        AS matricula,

                email.valor                         AS email,

                tel.codigo_pais                     AS codigoPais,
                tel.ddd                             AS ddd,
                tel.numero                          AS numero,

                cf.ativo                            AS contratoAtivo,
                cf.cargo                            AS cargo,
                cf.departamento                     AS departamento,

                tc.descricao                        AS tipoContrato,

                cf.data_admissao                    AS dataAdmissao,
                cf.data_demissao                    AS dataDemissao,

                cf.salario_mensal                   AS salarioMensal,
                cf.salario_hora                     AS salarioHora,

                tej.descricao                       AS tipoEscala,

                jfc.carga_horaria_diaria            AS cargaHorariaDiaria,
                jfc.carga_horaria_semanal           AS cargaHorariaSemanal,
                jfc.entrada_padrao                  AS entradaPadrao,
                jfc.saida_padrao                    AS saidaPadrao,
                jfc.tolerancia_padrao               AS toleranciaPadrao,
                jfc.intervalo_padrao                AS intervaloPadrao

            FROM users u

            LEFT JOIN identificacao_funcionario if_
                   ON if_.funcionario_id = u.id

            LEFT JOIN contrato_funcionario cf
                   ON cf.funcionario_id = u.id

            LEFT JOIN tipo_contrato tc
                   ON tc.id = cf.tipo_contrato_id

            LEFT JOIN jornada_funcionario_config jfc
                   ON jfc.funcionario_id = u.id

            LEFT JOIN tipo_escala_jornada tej
                   ON tej.id = jfc.tipo_escala_jornada_id

            LEFT JOIN LATERAL (
                SELECT uc.valor
                FROM user_credential uc
                JOIN tipo_credential tc ON tc.id = uc.tipo_credencial_id AND tc.descricao = 'EMAIL'
                JOIN tipo_categoria_credential tcc ON tcc.id = uc.categoria_credential_id AND tcc.descricao = 'PRIMARIO'
                WHERE uc.usuario_id = u.id
                  AND uc.ativo = true
                  AND uc.data_desativacao IS NULL
                ORDER BY uc.id
                LIMIT 1
            ) email ON true

            LEFT JOIN LATERAL (
                SELECT ut.codigo_pais, ut.ddd, ut.numero
                FROM usuario_telefone ut
                WHERE ut.usuario_id = u.id
                  AND ut.ativo = true
                  AND ut.data_desativacao IS NULL
                ORDER BY ut.id
                LIMIT 1
            ) tel ON true

            WHERE u.id = :funcionarioId
            LIMIT 1
            """, nativeQuery = true)
    Optional<FuncionarioPerfilProjection> findPerfilByFuncionarioId(@Param("funcionarioId") UUID funcionarioId);
}
