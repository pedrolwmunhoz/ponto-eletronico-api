package com.pontoeletronico.api.domain.services.perfil;

import com.pontoeletronico.api.exception.UsuarioNaoEncontradoException;
import com.pontoeletronico.api.infrastructure.input.dto.perfil.EmpresaPerfilResponse;
import com.pontoeletronico.api.infrastructure.output.repository.empresa.EmpresaPerfilProjection;
import com.pontoeletronico.api.infrastructure.output.repository.empresa.EmpresaPerfilRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class EmpresaPerfilService {

    private final EmpresaPerfilRepository empresaPerfilRepository;

    public EmpresaPerfilService(EmpresaPerfilRepository empresaPerfilRepository) {
        this.empresaPerfilRepository = empresaPerfilRepository;
    }

    /** Doc id 27: Recuperar informações da empresa. */
    public EmpresaPerfilResponse buscar(UUID empresaId) {
        EmpresaPerfilProjection p = empresaPerfilRepository.findPerfilByEmpresaId(empresaId)
                .orElseThrow(UsuarioNaoEncontradoException::new);
        return new EmpresaPerfilResponse(
                p.getUsername(),
                p.getCnpj(),
                p.getRazaoSocial(),
                p.getEmail(),
                p.getCodigoPais(),
                p.getDdd(),
                p.getNumero(),
                p.getRua(),
                p.getNumeroEndereco(),
                p.getComplemento(),
                p.getBairro(),
                p.getCidade(),
                p.getUf(),
                p.getCep(),
                p.getTimezone(),
                p.getCargaDiariaPadrao(),
                p.getCargaSemanalPadrao(),
                p.getToleranciaPadrao(),
                p.getIntervaloPadrao(),
                p.getControlePontoObrigatorio(),
                p.getTipoModeloPonto(),
                p.getTempoRetencao(),
                p.getAuditoriaAtiva(),
                p.getAssinaturaDigitalObrigatoria(),
                p.getGravarGeolocalizacaoObrigatoria(),
                p.getPermitirAjustePontoDireto()
        );
    }
}
