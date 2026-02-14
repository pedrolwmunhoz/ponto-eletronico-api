package com.pontoeletronico.api.domain.services.perfil;

import com.pontoeletronico.api.domain.entity.empresa.IdentificacaoFuncionario;
import com.pontoeletronico.api.domain.enums.MensagemErro;
import com.pontoeletronico.api.domain.services.audit.AuditoriaRegistroAsyncService;
import com.pontoeletronico.api.exception.FuncionarioNaoPertenceEmpresaException;
import com.pontoeletronico.api.exception.UsuarioNaoEncontradoException;
import com.pontoeletronico.api.infrastructure.input.dto.empresa.ContratoFuncionarioRequest;
import com.pontoeletronico.api.infrastructure.input.dto.empresa.JornadaFuncionarioConfigRequest;
import com.pontoeletronico.api.infrastructure.input.dto.empresa.UsuarioTelefoneRequest;
import com.pontoeletronico.api.infrastructure.input.dto.perfil.FuncionarioPerfilResponse;
import com.pontoeletronico.api.infrastructure.output.repository.empresa.FuncionarioPerfilProjection;
import com.pontoeletronico.api.infrastructure.output.repository.empresa.FuncionarioPerfilRepository;
import com.pontoeletronico.api.infrastructure.output.repository.empresa.IdentificacaoFuncionarioRepository;
import com.pontoeletronico.api.infrastructure.output.repository.empresa.TipoContratoRepository;
import com.pontoeletronico.api.infrastructure.output.repository.empresa.TipoEscalaJornadaRepository;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Service
@AllArgsConstructor
public class FuncionarioPerfilService {

    private final FuncionarioPerfilRepository funcionarioPerfilRepository;
    private final TipoContratoRepository tipoContratoRepository;
    private final TipoEscalaJornadaRepository tipoEscalaJornadaRepository;
    private final IdentificacaoFuncionarioRepository identificacaoFuncionarioRepository;
    private final AuditoriaRegistroAsyncService auditoriaRegistroAsyncService;

    private static final String ACAO_BUSCAR_FUNCIONARIO_PERFIL = "BUSCAR_FUNCIONARIO_PERFIL";

    /** Recuperar informações do funcionário. Reutiliza Request DTOs (telefone, contrato, jornada) para o mesmo shape do editar. */
    public FuncionarioPerfilResponse buscar(UUID funcionarioId, HttpServletRequest httpRequest) {
        var dataRef = LocalDateTime.now();
        identificacaoFuncionarioRepository.findByFuncionarioIdAndAtivoTrue(funcionarioId)
        .orElseThrow(() -> {
            auditoriaRegistroAsyncService.registrarSemDispositivoID(funcionarioId, ACAO_BUSCAR_FUNCIONARIO_PERFIL, "Buscar funcionário perfil", null, null, false, MensagemErro.FUNCIONARIO_NAO_PERTENCE_EMPRESA.getMensagem(), dataRef, httpRequest);
            throw new FuncionarioNaoPertenceEmpresaException();
        });
        

        FuncionarioPerfilProjection p = funcionarioPerfilRepository.findPerfilByFuncionarioId(funcionarioId)
                .orElseThrow(UsuarioNaoEncontradoException::new);

        UsuarioTelefoneRequest usuarioTelefone = (p.getCodigoPais() != null && p.getDdd() != null && p.getNumero() != null)
                ? new UsuarioTelefoneRequest(p.getCodigoPais(), p.getDdd(), p.getNumero())
                : null;

        ContratoFuncionarioRequest contratoFuncionario = null;
        if (p.getCargo() != null || p.getTipoContrato() != null) {
            Integer tipoContratoId = p.getTipoContrato() != null
                    ? tipoContratoRepository.findByDescricaoAndAtivoTrue(p.getTipoContrato()).map(tc -> tc.getId()).orElse(1)
                    : 1;
            contratoFuncionario = new ContratoFuncionarioRequest(
                    p.getMatricula(),
                    null,
                    p.getCargo() != null ? p.getCargo() : "",
                    p.getDepartamento(),
                    tipoContratoId,
                    Boolean.TRUE.equals(p.getContratoAtivo()),
                    p.getDataAdmissao() != null ? p.getDataAdmissao() : java.time.LocalDate.EPOCH,
                    p.getDataDemissao(),
                    p.getSalarioMensal() != null ? p.getSalarioMensal() : BigDecimal.ZERO,
                    p.getSalarioHora() != null ? p.getSalarioHora() : BigDecimal.ZERO
            );
        }

        JornadaFuncionarioConfigRequest jornadaFuncionarioConfig = null;
        if (p.getTipoEscala() != null || p.getCargaHorariaDiaria() != null) {
            Integer tipoEscalaJornadaId = p.getTipoEscala() != null
                    ? tipoEscalaJornadaRepository.findByDescricaoAndAtivoTrue(p.getTipoEscala()).map(tej -> tej.getId()).orElse(1)
                    : 1;
            Duration cargaDiaria = p.getCargaHorariaDiaria() != null ? Duration.parse(p.getCargaHorariaDiaria()) : Duration.ofHours(8);
            Duration cargaSemanal = p.getCargaHorariaSemanal() != null ? Duration.parse(p.getCargaHorariaSemanal()) : Duration.ofHours(44);
            Duration tolerancia = p.getToleranciaPadrao() != null ? Duration.parse(p.getToleranciaPadrao()) : Duration.ZERO;
            Duration intervalo = p.getIntervaloPadrao() != null ? Duration.parse(p.getIntervaloPadrao()) : Duration.ofHours(1);
            LocalTime entrada = p.getEntradaPadrao() != null ? p.getEntradaPadrao() : LocalTime.of(8, 0);
            LocalTime saida = p.getSaidaPadrao() != null ? p.getSaidaPadrao() : LocalTime.of(17, 0);
            jornadaFuncionarioConfig = new JornadaFuncionarioConfigRequest(
                    tipoEscalaJornadaId,
                    cargaDiaria,
                    cargaSemanal,
                    entrada,
                    saida,
                    tolerancia,
                    intervalo,
                    null,
                    false
            );
        }

        return new FuncionarioPerfilResponse(
                p.getUsername(),
                p.getFuncionarioAtivo(),
                p.getNomeCompleto(),
                p.getCpf(),
                p.getDataNascimento(),
                p.getMatricula(),
                p.getEmail(),
                usuarioTelefone,
                contratoFuncionario,
                jornadaFuncionarioConfig
        );
    }
}
