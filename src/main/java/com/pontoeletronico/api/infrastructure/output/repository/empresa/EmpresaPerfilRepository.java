package com.pontoeletronico.api.infrastructure.output.repository.empresa;

import com.pontoeletronico.api.domain.entity.usuario.Users;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface EmpresaPerfilRepository extends Repository<Users, UUID> {

    @Query(value = """
            SELECT
                u.username                          AS "username",
                edf.cnpj                            AS "cnpj",
                edf.razao_social                    AS "razaoSocial",
                email.valor                         AS "email",
                tel.codigo_pais                     AS "codigoPais",
                tel.ddd                             AS "ddd",
                tel.numero                          AS "numero",
                ee.rua                              AS "rua",
                ee.numero                           AS "numeroEndereco",
                ee.complemento                      AS "complemento",
                ee.bairro                           AS "bairro",
                ee.cidade                           AS "cidade",
                ee.uf                               AS "uf",
                ee.cep                              AS "cep",
                ejc.timezone                        AS "timezone",
                ejc.carga_horaria_diaria            AS "cargaDiariaPadrao",
                ejc.carga_horaria_semanal           AS "cargaSemanalPadrao",
                ejc.tolerancia_padrao               AS "toleranciaPadrao",
                ejc.intervalo_padrao                AS "intervaloPadrao",
                ec.controle_ponto_obrigatorio       AS "controlePontoObrigatorio",
                tmp.descricao                       AS "tipoModeloPonto",
                ec.tempo_retencao_anos              AS "tempoRetencao",
                ec.auditoria_ativa                  AS "auditoriaAtiva",
                ec.assinatura_digital_obrigatoria   AS "assinaturaDigitalObrigatoria",
                ejc.grava_geo_obrigatoria           AS "gravarGeolocalizacaoObrigatoria",
                ejc.permite_ajuste_ponto            AS "permitirAjustePontoDireto"
            FROM users u

            LEFT JOIN empresa_dados_fiscal edf
                   ON edf.empresa_id = u.id

            LEFT JOIN empresa_endereco ee
                   ON ee.empresa_id = u.id

            LEFT JOIN empresa_jornada_config ejc
                   ON ejc.empresa_id = u.id

            LEFT JOIN empresa_compliance ec
                   ON ec.empresa_id = u.id

            LEFT JOIN tipo_modelo_ponto tmp
                   ON tmp.id = ec.tipo_modelo_ponto_id

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

            WHERE u.id = :empresaId
            LIMIT 1
            """, nativeQuery = true)
    Optional<EmpresaPerfilProjection> findPerfilByEmpresaId(@Param("empresaId") UUID empresaId);
}
