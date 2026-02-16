package com.pontoeletronico.api.infrastructure.output.repository.empresa;

import com.pontoeletronico.api.domain.entity.empresa.IdentificacaoFuncionario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EspelhoPontoListagemRepository extends JpaRepository<IdentificacaoFuncionario, UUID> {

    /** Listagem espelho de ponto: funcionários da empresa com nome e todos os totais mensais (ano/mês). */
    @Query(value = """
            SELECT
                u.id                               AS usuarioId,
                if_.nome_completo                  AS nomeCompleto,
                bhm.total_horas_esperadas          AS totalHorasEsperadas,
                bhm.total_horas_trabalhadas        AS totalHorasTrabalhadas,
                bhm.total_horas_trabalhadas_feriado AS totalHorasTrabalhadasFeriado
            FROM users u
            INNER JOIN identificacao_funcionario if_ ON if_.funcionario_id = u.id
            LEFT JOIN banco_horas_mensal bhm ON bhm.funcionario_id = u.id
                AND bhm.empresa_id = :empresaId
                AND bhm.ano_ref = :anoRef
                AND bhm.mes_ref = :mesRef
                AND bhm.ativo = true
                AND bhm.data_desativacao IS NULL
            WHERE if_.empresa_id = :empresaId
              AND u.ativo = true
              AND u.data_desativacao IS NULL
              AND unaccent(LOWER(if_.nome_completo)) LIKE unaccent(LOWER(:nomePattern))
            ORDER BY if_.nome_completo
            LIMIT :limit OFFSET :offset
            """, nativeQuery = true)
    List<EspelhoPontoListagemProjection> findEspelhoPontoByEmpresaId(
            @Param("empresaId") UUID empresaId,
            @Param("anoRef") int anoRef,
            @Param("mesRef") int mesRef,
            @Param("nomePattern") String nomePattern,
            @Param("limit") int limit,
            @Param("offset") int offset);

    @Query(value = """
            SELECT COUNT(*)
            FROM users u
            INNER JOIN identificacao_funcionario if_ ON if_.funcionario_id = u.id
            WHERE if_.empresa_id = :empresaId
              AND u.ativo = true
              AND u.data_desativacao IS NULL
              AND unaccent(LOWER(if_.nome_completo)) LIKE unaccent(LOWER(:nomePattern))
            """, nativeQuery = true)
    long countEspelhoPontoByEmpresaId(
            @Param("empresaId") UUID empresaId,
            @Param("nomePattern") String nomePattern);
}
