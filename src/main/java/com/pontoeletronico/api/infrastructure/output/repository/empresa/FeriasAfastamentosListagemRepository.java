package com.pontoeletronico.api.infrastructure.output.repository.empresa;

import com.pontoeletronico.api.domain.entity.empresa.Afastamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

/**
 * Listagem paginada de férias/afastamentos por funcionário ou por empresa.
 * Tabelas: afastamento, tipo_afastamento, identificacao_funcionario.
 */
public interface FeriasAfastamentosListagemRepository extends JpaRepository<Afastamento, UUID> {

    /** Listagem do funcionário (seus afastamentos). Doc id 38. */
    @Query(value = """
            SELECT
                NULL::VARCHAR AS "nomeFuncionario",
                ta.descricao AS "nomeAfastamento",
                a.data_inicio AS "inicio",
                a.data_fim AS "fim",
                CASE WHEN a.ativo = true THEN 'ativo' ELSE 'inativo' END AS "status"
            FROM afastamento a
            INNER JOIN tipo_afastamento ta ON ta.id = a.tipo_afastamento_id
            WHERE a.funcionario_id = :funcionarioId AND a.ativo = true
            ORDER BY a.data_inicio DESC
            LIMIT :limit OFFSET :offset
            """, nativeQuery = true)
    List<FeriasAfastamentosListagemProjection> findPageByFuncionarioId(
            @Param("funcionarioId") UUID funcionarioId,
            @Param("limit") int limit,
            @Param("offset") int offset);

    @Query(value = "SELECT COUNT(*) FROM afastamento WHERE funcionario_id = :funcionarioId AND ativo = true", nativeQuery = true)
    long countByFuncionarioId(@Param("funcionarioId") UUID funcionarioId);

    /** Listagem por funcionário (empresa acessando um funcionário). Doc id 39. */
    @Query(value = """
            SELECT
                NULL::VARCHAR AS "nomeFuncionario",
                ta.descricao AS "nomeAfastamento",
                a.data_inicio AS "inicio",
                a.data_fim AS "fim",
                CASE WHEN a.ativo = true THEN 'ativo' ELSE 'inativo' END AS "status"
            FROM afastamento a
            INNER JOIN tipo_afastamento ta ON ta.id = a.tipo_afastamento_id
            WHERE a.funcionario_id = :funcionarioId
            ORDER BY a.data_inicio DESC
            LIMIT :limit OFFSET :offset
            """, nativeQuery = true)
    List<FeriasAfastamentosListagemProjection> findPageByFuncionarioIdEmpresa(
            @Param("funcionarioId") UUID funcionarioId,
            @Param("limit") int limit,
            @Param("offset") int offset);

    @Query(value = "SELECT COUNT(*) FROM afastamento WHERE funcionario_id = :funcionarioId AND ativo = true", nativeQuery = true)
    long countByFuncionarioIdEmpresa(@Param("funcionarioId") UUID funcionarioId);

    /** Listagem da empresa (todos funcionários). Doc id 40. Filtro por nome do funcionário. */
    @Query(value = """
            SELECT
                if_.nome_completo AS "nomeFuncionario",
                ta.descricao AS "nomeAfastamento",
                a.data_inicio AS "inicio",
                a.data_fim AS "fim",
                CASE WHEN a.ativo = true THEN 'ativo' ELSE 'inativo' END AS "status"
            FROM afastamento a
            INNER JOIN tipo_afastamento ta ON ta.id = a.tipo_afastamento_id
            INNER JOIN identificacao_funcionario if_ ON if_.funcionario_id = a.funcionario_id
            WHERE if_.empresa_id = :empresaId
              AND unaccent(LOWER(if_.nome_completo)) LIKE unaccent(LOWER(:nomePattern))
            ORDER BY a.data_inicio DESC
            LIMIT :limit OFFSET :offset
            """, nativeQuery = true)
    List<FeriasAfastamentosListagemProjection> findPageByEmpresaId(
            @Param("empresaId") UUID empresaId,
            @Param("nomePattern") String nomePattern,
            @Param("limit") int limit,
            @Param("offset") int offset);

    @Query(value = """
            SELECT COUNT(*) FROM afastamento a
            INNER JOIN identificacao_funcionario if_ ON if_.funcionario_id = a.funcionario_id
            WHERE if_.empresa_id = :empresaId
              AND a.ativo = true
              AND unaccent(LOWER(if_.nome_completo)) LIKE unaccent(LOWER(:nomePattern))
            """, nativeQuery = true)
    long countByEmpresaId(@Param("empresaId") UUID empresaId, @Param("nomePattern") String nomePattern);
}
