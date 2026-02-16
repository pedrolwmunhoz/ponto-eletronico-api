package com.pontoeletronico.api.infrastructure.output.repository.registro;

import com.pontoeletronico.api.domain.entity.registro.SolicitacaoPonto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface SolicitacoesPontoListagemRepository extends JpaRepository<SolicitacaoPonto, UUID> {

    /** Só retorna linhas se sp.usuario_id existir em identificacao_funcionario com empresa_id = :empresaId (e funcionário ativo). Filtro opcional por nome do funcionário. */
    @Query(value = """
            SELECT sp.id AS "id",
                CASE WHEN sp.tipo_solicitacao_id = 1 THEN 'criar registro' ELSE 'excluir' END AS "tipo",
                (COALESCE(sp.data_hora_registro, rp.created_at))::date AS "data",
                COALESCE(tj.descricao, '') AS "motivo",
                if_.nome_completo AS "nomeFuncionario",
                CASE WHEN sp.aprovado IS NULL THEN 'pendente' WHEN sp.aprovado = true THEN 'aprovado' ELSE 'reprovado' END AS "status"
            FROM solicitacao_ponto sp
            INNER JOIN identificacao_funcionario if_ ON if_.funcionario_id = sp.usuario_id AND if_.empresa_id = :empresaId
            INNER JOIN users u ON u.id = sp.usuario_id AND u.ativo = true AND u.data_desativacao IS NULL
            LEFT JOIN tipo_justificativa tj ON tj.id = sp.tipo_justificativa_id
            LEFT JOIN registro_ponto rp ON rp.id = sp.registro_ponto_id
            WHERE unaccent(LOWER(if_.nome_completo)) LIKE unaccent(LOWER('%' || REPLACE(TRIM(COALESCE(:nome, '')), ' ', '%') || '%'))
            ORDER BY sp.created_at DESC
            LIMIT :limit OFFSET :offset
            """, nativeQuery = true)
    List<SolicitacaoPontoListagemProjection> findPageByEmpresaId(@Param("empresaId") UUID empresaId, @Param("nome") String nome, @Param("limit") int limit, @Param("offset") int offset);

    @Query(value = """
            SELECT COUNT(*) FROM solicitacao_ponto sp
            INNER JOIN identificacao_funcionario if_ ON if_.funcionario_id = sp.usuario_id AND if_.empresa_id = :empresaId
            INNER JOIN users u ON u.id = sp.usuario_id AND u.ativo = true AND u.data_desativacao IS NULL
            WHERE unaccent(LOWER(if_.nome_completo)) LIKE unaccent(LOWER('%' || REPLACE(TRIM(COALESCE(:nome, '')), ' ', '%') || '%'))
            """, nativeQuery = true)
    long countByEmpresaId(@Param("empresaId") UUID empresaId, @Param("nome") String nome);
}
