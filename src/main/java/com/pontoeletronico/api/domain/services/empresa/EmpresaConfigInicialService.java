package com.pontoeletronico.api.domain.services.empresa;

import com.pontoeletronico.api.domain.services.audit.AuditoriaRegistroAsyncService;
import com.pontoeletronico.api.exception.TipoNaoEncontradoException;
import com.pontoeletronico.api.domain.enums.MensagemErro;
import com.pontoeletronico.api.infrastructure.input.dto.empresa.EmpresaConfigInicialRequest;
import com.pontoeletronico.api.infrastructure.input.dto.empresa.UsuarioGeofenceRequest;
import com.pontoeletronico.api.infrastructure.output.repository.empresa.EmpresaBancoHorasConfigRepository;
import com.pontoeletronico.api.infrastructure.output.repository.empresa.EmpresaComplianceRepository;
import com.pontoeletronico.api.infrastructure.output.repository.empresa.EmpresaDadosFiscalRepository;
import com.pontoeletronico.api.infrastructure.output.repository.empresa.EmpresaJornadaConfigRepository;
import com.pontoeletronico.api.infrastructure.output.repository.empresa.GeofenceEmpresaConfigRepository;
import com.pontoeletronico.api.infrastructure.output.repository.empresa.TipoEscalaJornadaRepository;
import com.pontoeletronico.api.infrastructure.output.repository.usuario.UsuarioGeofenceRepository;
import com.pontoeletronico.api.exception.BadRequestException;
import com.pontoeletronico.api.util.CertificadoA1Utils;
import com.pontoeletronico.api.util.CertificadoSenhaCriptografiaService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class EmpresaConfigInicialService {

    private static final String ACAO_CONFIG_INICIAL = "CONFIG_INICIAL_EMPRESA";

    private final EmpresaJornadaConfigRepository empresaJornadaConfigRepository;
    private final EmpresaBancoHorasConfigRepository empresaBancoHorasConfigRepository;
    private final TipoEscalaJornadaRepository tipoEscalaJornadaRepository;
    private final UsuarioGeofenceRepository usuarioGeofenceRepository;
    private final GeofenceEmpresaConfigRepository geofenceEmpresaConfigRepository;
    private final AuditoriaRegistroAsyncService auditoriaRegistroAsyncService;
    private final EmpresaComplianceRepository empresaComplianceRepository;
    private final EmpresaDadosFiscalRepository empresaDadosFiscalRepository;
    private final CertificadoSenhaCriptografiaService certificadoSenhaCriptografiaService;

    public EmpresaConfigInicialService(EmpresaJornadaConfigRepository empresaJornadaConfigRepository,
                                      EmpresaBancoHorasConfigRepository empresaBancoHorasConfigRepository,
                                      TipoEscalaJornadaRepository tipoEscalaJornadaRepository,
                                      UsuarioGeofenceRepository usuarioGeofenceRepository,
                                      GeofenceEmpresaConfigRepository geofenceEmpresaConfigRepository,
                                      AuditoriaRegistroAsyncService auditoriaRegistroAsyncService,
                                      EmpresaComplianceRepository empresaComplianceRepository,
                                      EmpresaDadosFiscalRepository empresaDadosFiscalRepository,
                                      CertificadoSenhaCriptografiaService certificadoSenhaCriptografiaService) {
        this.empresaJornadaConfigRepository = empresaJornadaConfigRepository;
        this.empresaBancoHorasConfigRepository = empresaBancoHorasConfigRepository;
        this.tipoEscalaJornadaRepository = tipoEscalaJornadaRepository;
        this.usuarioGeofenceRepository = usuarioGeofenceRepository;
        this.geofenceEmpresaConfigRepository = geofenceEmpresaConfigRepository;
        this.auditoriaRegistroAsyncService = auditoriaRegistroAsyncService;
        this.empresaComplianceRepository = empresaComplianceRepository;
        this.empresaDadosFiscalRepository = empresaDadosFiscalRepository;
        this.certificadoSenhaCriptografiaService = certificadoSenhaCriptografiaService;
    }

    @Transactional
    /** Configuração inicial sem certificado (jornada, banco de horas, geofences). Chamado pelo endpoint JSON. */
    public void configurar(UUID empresaId, EmpresaConfigInicialRequest request, HttpServletRequest httpRequest) {
        var empresaJornadaConfig = request.empresaJornadaConfig();
        var empresaBancoHorasConfig = request.empresaBancoHorasConfig();
        var now = LocalDateTime.now();

        if (tipoEscalaJornadaRepository.existsByIdAndAtivo(empresaJornadaConfig.tipoEscalaJornadaId()).isEmpty()) {
            auditoriaRegistroAsyncService.registrarSemDispositivoID(empresaId, ACAO_CONFIG_INICIAL, "Configuração inicial da empresa", null, null, false, MensagemErro.TIPO_ESCALA_JORNADA_NAO_ENCONTRADO.getMensagem(), now, httpRequest);
            throw new TipoNaoEncontradoException(MensagemErro.TIPO_ESCALA_JORNADA_NAO_ENCONTRADO);
        }

        var timezone = empresaJornadaConfig.timezone() != null && !empresaJornadaConfig.timezone().isBlank() ? empresaJornadaConfig.timezone() : "America/Sao_Paulo";
        Duration tempoDescansoEntreJornada = empresaJornadaConfig.tempoDescansoEntreJornada() != null ? empresaJornadaConfig.tempoDescansoEntreJornada() : Duration.ofHours(11);
        Duration toleranciaPadrao = empresaJornadaConfig.toleranciaPadrao() != null ? empresaJornadaConfig.toleranciaPadrao() : Duration.ZERO;

        var existenteJornada = empresaJornadaConfigRepository.findByEmpresaId(empresaId);
        if (existenteJornada.isPresent()) {
            empresaJornadaConfigRepository.updateByEmpresaId(
                    empresaId, empresaJornadaConfig.tipoEscalaJornadaId(), empresaJornadaConfig.cargaHorariaDiaria(), empresaJornadaConfig.cargaHorariaSemanal(),
                    toleranciaPadrao, empresaJornadaConfig.intervaloPadrao(), empresaJornadaConfig.entradaPadrao(), empresaJornadaConfig.saidaPadrao(),
                    tempoDescansoEntreJornada, timezone, empresaJornadaConfig.gravaGeoObrigatoria(), empresaJornadaConfig.gravaPontoApenasEmGeofence(), empresaJornadaConfig.permiteAjustePonto(),
                    now);
        } else {
            empresaJornadaConfigRepository.insert(
                    UUID.randomUUID(), empresaId, empresaJornadaConfig.tipoEscalaJornadaId(), empresaJornadaConfig.cargaHorariaDiaria(), empresaJornadaConfig.cargaHorariaSemanal(),
                    toleranciaPadrao, empresaJornadaConfig.intervaloPadrao(), empresaJornadaConfig.entradaPadrao(), empresaJornadaConfig.saidaPadrao(),
                    tempoDescansoEntreJornada, timezone, empresaJornadaConfig.gravaGeoObrigatoria(), empresaJornadaConfig.gravaPontoApenasEmGeofence(), empresaJornadaConfig.permiteAjustePonto(),
                    now);
        }

        var existenteBanco = empresaBancoHorasConfigRepository.findByEmpresaId(empresaId);
        if (existenteBanco.isPresent()) {
            empresaBancoHorasConfigRepository.updateByEmpresaId(empresaId, empresaBancoHorasConfig.ativo(), empresaBancoHorasConfig.totalDiasVencimento(), now);
        } else {
            empresaBancoHorasConfigRepository.insert(UUID.randomUUID(), empresaId, empresaBancoHorasConfig.ativo(), empresaBancoHorasConfig.totalDiasVencimento(), now);
        }

        List<UsuarioGeofenceRequest> geofences = request.usuarioGeofence() != null ? request.usuarioGeofence() : Collections.emptyList();
        for (var usuarioGeofenceRequest : geofences) {
            var geofenceId = UUID.randomUUID();
            usuarioGeofenceRepository.insert(
                    geofenceId, empresaId, usuarioGeofenceRequest.descricao(), usuarioGeofenceRequest.latitude(), usuarioGeofenceRequest.longitude(),
                    usuarioGeofenceRequest.raioMetros() != null ? usuarioGeofenceRequest.raioMetros() : 100, usuarioGeofenceRequest.ativo(),
                    now, now);
            geofenceEmpresaConfigRepository.insert(UUID.randomUUID(), geofenceId, now);
        }

        auditoriaRegistroAsyncService.registrarSemDispositivoID(empresaId, ACAO_CONFIG_INICIAL, "Configuração inicial da empresa", null, null, true, null, now, httpRequest);
    }

    @Transactional
    /** Grava apenas o certificado A1 em empresa_compliance (chamado após config inicial). Valida CNPJ e senha. */
    public void adicionarCertificado(UUID empresaId, MultipartFile certificadoA1, String certificadoA1Senha) {
        if (certificadoA1 == null || certificadoA1.isEmpty()) {
            throw new BadRequestException("Arquivo do certificado é obrigatório.");
        }
        byte[] bytes;
        try {
            bytes = certificadoA1.getBytes();
        } catch (Exception e) {
            throw new BadRequestException("Não foi possível ler o arquivo do certificado.");
        }
        char[] senha = (certificadoA1Senha != null && !certificadoA1Senha.isBlank()) ? certificadoA1Senha.toCharArray() : new char[0];
        var now = LocalDateTime.now();

        try {
            Optional<java.time.LocalDateTime> dataExp = CertificadoA1Utils.extrairDataExpiracao(bytes, senha);
            if (dataExp.isEmpty()) {
                throw new BadRequestException("Certificado inválido ou senha incorreta.");
            }
            Optional<String> hash = CertificadoA1Utils.extrairCertificadoHash(bytes, senha);
            java.time.LocalDateTime dataExpiracao = dataExp.get();

            var dadosFiscal = empresaDadosFiscalRepository.findByEmpresaId(empresaId);
            if (dadosFiscal.isEmpty()) {
                throw new BadRequestException("Dados fiscais da empresa não encontrados. Cadastre a empresa antes de enviar o certificado.");
            }
            String cnpjEmpresa = dadosFiscal.get().getCnpj().replaceAll("\\D", "");
            String cnpjCert = CertificadoA1Utils.extrairCnpjCertificado(bytes, senha).orElse(null);
            if (cnpjCert == null || cnpjCert.isEmpty()) {
                throw new BadRequestException("Certificado não contém CNPJ (não é ICP-Brasil).");
            }
            if (!cnpjEmpresa.equals(cnpjCert)) {
                throw new BadRequestException("Certificado não pertence à empresa: CNPJ do certificado não confere com o CNPJ cadastrado.");
            }
            String senhaCriptografadaFinal = (certificadoA1Senha != null && !certificadoA1Senha.isBlank())
                    ? certificadoSenhaCriptografiaService.criptografar(certificadoA1Senha) : null;
            byte[] certificadoCriptografado = certificadoSenhaCriptografiaService.criptografarBytes(bytes);
            empresaComplianceRepository.findByEmpresaId(empresaId).ifPresent(compliance -> {
                compliance.setCertificado(certificadoCriptografado != null ? certificadoCriptografado : bytes);
                compliance.setDataExpiracaoCertificado(dataExpiracao);
                compliance.setCertificadoHash(hash.orElse(null));
                compliance.setCertificadoSenhaCriptografada(senhaCriptografadaFinal);
                compliance.setUpdatedAt(now);
                empresaComplianceRepository.save(compliance);
            });
        } catch (BadRequestException e) {
            throw e;
        } catch (IllegalArgumentException e) {
            throw new BadRequestException(e.getMessage() != null ? e.getMessage() : "Certificado inválido ou senha incorreta.");
        }
    }

    /** Verifica se a empresa já realizou a configuração inicial (possui registro em empresa_jornada_config). */
    public boolean isConfigInicialRealizada(UUID empresaId) {
        return empresaJornadaConfigRepository.existsByEmpresaId(empresaId).isPresent();
    }
}
