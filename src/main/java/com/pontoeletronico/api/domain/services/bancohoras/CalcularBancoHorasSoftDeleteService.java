package com.pontoeletronico.api.domain.services.bancohoras;

import com.pontoeletronico.api.domain.entity.empresa.BancoHorasMensal;
import com.pontoeletronico.api.domain.entity.registro.RegistroPonto;
import com.pontoeletronico.api.domain.entity.registro.ResumoPontoDia;
import com.pontoeletronico.api.domain.entity.registro.XrefPontoResumo;
import com.pontoeletronico.api.domain.services.bancohoras.record.JornadaConfig;
import com.pontoeletronico.api.domain.services.bancohoras.utils.CalcularResumoDiaUtils;
import com.pontoeletronico.api.domain.services.empresa.MetricasDiariaEmpresaContadorService;
import com.pontoeletronico.api.infrastructure.output.repository.bancohoras.BancoHorasMensalRepository;
import com.pontoeletronico.api.infrastructure.output.repository.registro.RegistroPontoRepository;
import com.pontoeletronico.api.infrastructure.output.repository.registro.ResumoPontoDiaRepository;
import com.pontoeletronico.api.exception.RegistroNaoEncontradoException;
import com.pontoeletronico.api.infrastructure.output.repository.registro.XrefPontoResumoRepository;

import lombok.AllArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Lógica separada - calcular banco horas após soft delete de registro.
 * Busca jornada afetada; remove registro da lista; reordena/recalcula.
 * Se distância entre anterior e posterior ao deletado >= tempoDescansoEntreJornada, separa em duas jornadas (jornadaNova1Afetada e jornadaNova2Afetada).
 * Não impacta jornada anterior nem posterior, somente a jornadaAfetada.
 */
@Service
@AllArgsConstructor
public class CalcularBancoHorasSoftDeleteService {

    private final RegistroPontoRepository registroPontoRepository;
    private final ResumoPontoDiaRepository resumoPontoDiaRepository;
    private final XrefPontoResumoRepository xrefPontoResumoRepository;
    private final BancoHorasMensalService calcularBancoHorasMensal;
    private final CalcularResumoDiaUtils calcularResumoDiaUtils;
    private final BancoHorasMensalRepository bancoHorasMensalRepository;
    private final MetricasDiariaEmpresaContadorService metricasDiariaEmpresaContadorService;


    /**
     * Processa soft delete: remove registro da jornada; reordena; se distância anterior-posterior >= tempoDescansoEntreJornada, separa em duas jornadas.
     * Nunca impacta jornada anterior nem posterior.
     */
    @Transactional
    public void processarSoftDelete(UUID funcionarioId, UUID empresaId, UUID idRegistroDeletado, LocalDateTime dataRegistroDeletado, JornadaConfig jornadaConfig) {

        // Buscar QUAL jornada vai ser afetada - por id do registro deletado na xref
        ResumoPontoDia jornadaAfetada = xrefPontoResumoRepository.findResumoPontoDiaByRegistroPontoId(idRegistroDeletado)
        .orElseThrow(() -> new RegistroNaoEncontradoException("Jornada não encontrada para o registro deletado"));
        
        Optional<BancoHorasMensal> bancoHorasMensal = bancoHorasMensalRepository
        .findByFuncionarioIdAndAnoRefAndMesRef(
            funcionarioId, 
            dataRegistroDeletado.getYear(), 
            dataRegistroDeletado.getMonthValue()
        );

        Duration totalTrabalhadoAntes = Duration.ZERO;
        if(bancoHorasMensal.isPresent()) {
            totalTrabalhadoAntes = bancoHorasMensal.get().getTotalHorasTrabalhadas();
        }

        var listaRegistrosJornadaAfetada = xrefPontoResumoRepository
        .listRegistroPontoByResumoPontoDiaIdOrderByCreatedAt(jornadaAfetada.getId());
        
        if (listaRegistrosJornadaAfetada.size() == 1) {
            apagarJornadaAndXref(jornadaAfetada.getId());
            calcularBancoHorasMensal.recalcularMensal(funcionarioId, empresaId, dataRegistroDeletado.getYear(), dataRegistroDeletado.getMonthValue());
            return;
        }
        
        // Remover xref do registro deletado
        xrefPontoResumoRepository.deleteByRegistroPontoId(idRegistroDeletado);
        listaRegistrosJornadaAfetada.removeIf(reg -> reg.getId().equals(idRegistroDeletado));

        var tempoDescansoEntreJornada = jornadaConfig.tempoDescansoEntreJornada();

        // Pegar registro anterior por data
        Optional<RegistroPonto> registroAnterior = listaRegistrosJornadaAfetada.stream()
                .filter(reg -> reg.getCreatedAt().isBefore(dataRegistroDeletado))
                .max(Comparator.comparing(RegistroPonto::getCreatedAt));

        // Pegar somente os registros antes da data; os demais são excluídos/ignorados
        List<RegistroPonto> listaRegistrosJornadaAfetadaAnterior = new ArrayList<>();
        for (RegistroPonto reg : listaRegistrosJornadaAfetada) {
            if (reg.getCreatedAt().isBefore(dataRegistroDeletado)) {
                listaRegistrosJornadaAfetadaAnterior.add(reg);
            }
        }

        // Pegar registro posterior por data
        Optional<RegistroPonto> registroPosterior = listaRegistrosJornadaAfetada.stream()
                .filter(reg -> reg.getCreatedAt().isAfter(dataRegistroDeletado))
                .min(Comparator.comparing(RegistroPonto::getCreatedAt));

       // Pegar somente os registros antes da data; os demais são excluídos/ignorados
       List<RegistroPonto> listaRegistrosJornadaAfetadaPosterior = new ArrayList<>();
       for (RegistroPonto reg : listaRegistrosJornadaAfetada) {
           if (reg.getCreatedAt().isAfter(dataRegistroDeletado)) {
               listaRegistrosJornadaAfetadaPosterior.add(reg);
           }
       }

        // Se existir registro anterior e posterior calcular a distancia entre eles
        if(registroAnterior.isPresent() && registroPosterior.isPresent()) {
            RegistroPonto registroAnteriorObj = registroAnterior.get();
            RegistroPonto registroPosteriorObj = registroPosterior.get();
            LocalDateTime anterior = registroAnteriorObj.getCreatedAt();
            LocalDateTime posterior = registroPosteriorObj.getCreatedAt();

            long distanciaEmSegundos = Math.abs(anterior.until(posterior, ChronoUnit.SECONDS));

            // Se a distância em segundos entre anterior e posterior for maior ou igual ao tempoDescansoEntreJornada em segundos, divide jornada afetada ao meio
            if (distanciaEmSegundos >= tempoDescansoEntreJornada.getSeconds()) {

                var now = LocalDateTime.now();
                UUID idNovaJornadaAfetadaAnterior = UUID.randomUUID();
                criarJornadaDividida(idNovaJornadaAfetadaAnterior, funcionarioId, empresaId, now, listaRegistrosJornadaAfetadaAnterior, jornadaConfig);

                UUID idNovaJornadaAfetadaPosterior = UUID.randomUUID();
                criarJornadaDividida(idNovaJornadaAfetadaPosterior, funcionarioId, empresaId, now, listaRegistrosJornadaAfetadaPosterior, jornadaConfig);

                for (RegistroPonto registro : listaRegistrosJornadaAfetadaAnterior) {
                    xrefPontoResumoRepository.deleteByRegistroPontoId(registro.getId());
                    inserirXrefPontoResumo(funcionarioId, idNovaJornadaAfetadaAnterior, registro.getId(), registro.getCreatedAt());
                }
                for (RegistroPonto registro : listaRegistrosJornadaAfetadaPosterior) {
                    xrefPontoResumoRepository.deleteByRegistroPontoId(registro.getId());
                    inserirXrefPontoResumo(funcionarioId, idNovaJornadaAfetadaPosterior, registro.getId(), registro.getCreatedAt());
                }
                apagarJornadaAndXref(jornadaAfetada.getId());
                // Jornada original já foi substituída pelas duas novas; não recalcular/salvar a entidade deletada
            } else {
                recalcularJornadaAfetada(jornadaAfetada, listaRegistrosJornadaAfetada, jornadaConfig);
            }
        } else {
            // Se nao existir registro anterior e posterior, recalcular jornada afetada
            recalcularJornadaAfetada(jornadaAfetada, listaRegistrosJornadaAfetada, jornadaConfig);
        }


        var primeiraBatida = jornadaAfetada.getPrimeiraBatida();
        var ultimaBatida = jornadaAfetada.getUltimaBatida();

        LocalDateTime rangeAnterior = primeiraBatida.minus(tempoDescansoEntreJornada).plus(1, ChronoUnit.MILLIS);
        LocalDateTime rangePosterior = ultimaBatida.plus(tempoDescansoEntreJornada).minus(1, ChronoUnit.MILLIS);
        Optional<ResumoPontoDia> jornadaAfetadaAnterior = xrefPontoResumoRepository.findByFuncionarioIdAndDataBetweenDesc(funcionarioId, rangeAnterior, primeiraBatida);
        Optional<ResumoPontoDia> jornadaAfetadaPosterior = xrefPontoResumoRepository.findByFuncionarioIdAndDataBetweenAsc(funcionarioId, ultimaBatida, rangePosterior);
        if (jornadaAfetadaAnterior.isPresent()) {
            ResumoPontoDia jornadaAfetadaAnteriorObj = jornadaAfetadaAnterior.get();
            LocalDateTime anterior = jornadaAfetadaAnteriorObj.getUltimaBatida();   
            long distanciaEmSegundosAnterior = Duration.between(anterior, primeiraBatida).getSeconds();
            
            if (distanciaEmSegundosAnterior > 0 ) {
                juntarJornadas(funcionarioId,jornadaAfetada, jornadaAfetadaAnteriorObj, listaRegistrosJornadaAfetada, jornadaConfig);
            }

        }

        if (jornadaAfetadaPosterior.isPresent()) {
            ResumoPontoDia jornadaAfetadaPosteriorObj = jornadaAfetadaPosterior.get();
            LocalDateTime posterior = jornadaAfetadaPosteriorObj.getPrimeiraBatida();
            long distanciaEmSegundosPosterior = Duration.between(ultimaBatida, posterior).getSeconds();
    
            if (distanciaEmSegundosPosterior > 0) {
                juntarJornadas(funcionarioId, jornadaAfetada, jornadaAfetadaPosteriorObj, listaRegistrosJornadaAfetada, jornadaConfig);
            }
        }

        calcularBancoHorasMensal.recalcularMensal(funcionarioId, empresaId, dataRegistroDeletado.getYear(), dataRegistroDeletado.getMonthValue());

        var bancoHorasMensalDepois = bancoHorasMensalRepository
                .findByFuncionarioIdAndAnoRefAndMesRef(funcionarioId, dataRegistroDeletado.getYear(), dataRegistroDeletado.getMonthValue());
        Duration totalTrabalhadoDepois = bancoHorasMensalDepois.map(BancoHorasMensal::getTotalHorasTrabalhadas).orElse(Duration.ZERO);
        Duration deltaTotalTrabalhadoJornadaAfetada = totalTrabalhadoDepois.minus(totalTrabalhadoAntes);

        metricasDiariaEmpresaContadorService.ajustarMetricasAposRecalculo(empresaId, dataRegistroDeletado.toLocalDate(), -1, deltaTotalTrabalhadoJornadaAfetada);
    }

    private void inserirXrefPontoResumo( UUID funcionarioId, UUID idJornadaNova, UUID idRegistroPonto, LocalDateTime dataRegistro) {
        if (xrefPontoResumoRepository.existsByRegistroPontoId(idRegistroPonto)) {
            return;
        }
        var xref = new XrefPontoResumo();
        xref.setId(UUID.randomUUID());
        xref.setFuncionarioId(funcionarioId);
        xref.setRegistroPontoId(idRegistroPonto);
        xref.setResumoPontoDiaId(idJornadaNova);
        xref.setCreatedAt(dataRegistro);
        xrefPontoResumoRepository.save(xref);
    }

    private void criarJornadaDividida(UUID idJornadaNova, UUID funcionarioId, UUID empresaId, LocalDateTime now, List<RegistroPonto> listaRegistrosJornada, JornadaConfig jornadaConfig) {
        var inconsistente = listaRegistrosJornada.size() % 2 != 0 ? true : false;
        ResumoPontoDia jornadaNova = new ResumoPontoDia();
        jornadaNova.setId(idJornadaNova);
        jornadaNova.setFuncionarioId(funcionarioId);
        jornadaNova.setEmpresaId(empresaId);
        jornadaNova.setPrimeiraBatida(listaRegistrosJornada.get(0).getCreatedAt());
        jornadaNova.setUltimaBatida(listaRegistrosJornada.get(listaRegistrosJornada.size() - 1).getCreatedAt());
        jornadaNova.setTotalHorasTrabalhadas(Duration.ZERO);
        jornadaNova.setTotalHorasEsperadas(jornadaConfig.cargaDiaria());
        jornadaNova.setInconsistente(inconsistente);
        jornadaNova.setMotivoInconsistencia(inconsistente ? "IMPAR" : null);
        jornadaNova.setCreatedAt(now);
        recalcularJornadaAfetada(jornadaNova, listaRegistrosJornada, jornadaConfig);
    }

    private void recalcularJornadaAfetada(ResumoPontoDia jornadaAfetada, List<RegistroPonto> listaRegistrosJornadaAfetada, JornadaConfig jornadaConfig) {
        calcularResumoDiaUtils.recalcularResumoDoDia(jornadaAfetada, listaRegistrosJornadaAfetada, jornadaConfig);
        reordenarTipos(listaRegistrosJornadaAfetada);
        salvarJornadaAfetada(jornadaAfetada);
    }

    private void reordenarTipos(List<RegistroPonto> listaRegistrosJornadaAfetada) {
        // Reordena tipoEntrada: primeira batida = entrada (true), segunda = saída (false), alternando
        boolean entrada = true;
        for (RegistroPonto registro : listaRegistrosJornadaAfetada) {
            registro.setTipoEntrada(entrada);
            entrada = !entrada;
            salvarRegistroPonto(registro);
        }
    }


    @Transactional
    private void juntarJornadas(UUID funcionarioId, ResumoPontoDia jornadaAfetada, ResumoPontoDia jornadaAdd, List<RegistroPonto> listaRegistrosJornadaAfetada, JornadaConfig jornadaConfig) {
        if (jornadaAdd.getId().equals(jornadaAfetada.getId())) {
            return;
        }
        List<RegistroPonto> listaRegistrosJornadaAdd = xrefPontoResumoRepository
        .listRegistroPontoByResumoPontoDiaIdOrderByCreatedAt(jornadaAdd.getId());

        listaRegistrosJornadaAfetada.addAll(listaRegistrosJornadaAdd);
        
        for (RegistroPonto registro : listaRegistrosJornadaAdd) {
            resumoPontoDiaRepository.deleteById(jornadaAdd.getId());
            xrefPontoResumoRepository.deleteByRegistroPontoId(registro.getId());
            salvarXrefPontoResumo(funcionarioId, jornadaAfetada.getId(), registro.getId(), registro.getCreatedAt());
        }

        recalcularJornadaAfetada(jornadaAfetada, listaRegistrosJornadaAfetada, jornadaConfig);
    }

    private void salvarXrefPontoResumo(UUID funcionarioId, UUID idJornada, UUID idRegistroPonto, LocalDateTime dataRegistro) {
        if (xrefPontoResumoRepository.existsByRegistroPontoId(idRegistroPonto)) {
            return;
        }
        var xref = new XrefPontoResumo();
        xref.setId(UUID.randomUUID());
        xref.setFuncionarioId(funcionarioId);
        xref.setRegistroPontoId(idRegistroPonto);
        xref.setResumoPontoDiaId(idJornada);
        xref.setCreatedAt(dataRegistro);
        xrefPontoResumoRepository.save(xref);
    }

    private void salvarRegistroPonto(RegistroPonto registro) {
        registroPontoRepository.save(registro);
    }

    private void salvarJornadaAfetada(ResumoPontoDia jornadaAfetada) {
        resumoPontoDiaRepository.save(jornadaAfetada);
    }

    private void apagarJornadaAndXref(UUID idJornada) {
        xrefPontoResumoRepository.deleteByResumoPontoDiaId(idJornada);
        resumoPontoDiaRepository.deleteById(idJornada);
    }
}
