package com.pontoeletronico.api.domain.services.bancohoras;

import com.pontoeletronico.api.domain.entity.empresa.BancoHorasMensal;
import com.pontoeletronico.api.domain.entity.registro.ResumoPontoDia;
import com.pontoeletronico.api.infrastructure.output.repository.bancohoras.BancoHorasMensalRepository;
import com.pontoeletronico.api.infrastructure.output.repository.registro.ResumoPontoDiaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Banco_horas_mensal: recalcularMensal (entrada manual e soft delete) e acumularNoMensal (aplicativo).
 */
@Service
public class BancoHorasMensalService {

    private final BancoHorasMensalRepository bancoHorasMensalRepository;
    private final ResumoPontoDiaRepository resumoPontoDiaRepository;

    public BancoHorasMensalService(BancoHorasMensalRepository bancoHorasMensalRepository,
                                   ResumoPontoDiaRepository resumoPontoDiaRepository) {
        this.bancoHorasMensalRepository = bancoHorasMensalRepository;
        this.resumoPontoDiaRepository = resumoPontoDiaRepository;
    }

    /**
     * Recalcula o banco_horas_mensal do mês a partir das jornadas (entrada manual e soft delete).
     * Obtém ou cria o registro, zera totais, lista jornadas ativas do mês e soma totalHorasEsperadas e totalHorasTrabalhadas.
     * Não usado pelo aplicativo (lá usa {@link #acumularNoMensal}).
     */
    @Transactional
    public void recalcularMensal(UUID funcionarioId, UUID empresaId, int ano, int mes) {
        LocalDate inicio = LocalDate.of(ano, mes, 1);
        LocalDate fim = inicio.withDayOfMonth(inicio.lengthOfMonth());

        BancoHorasMensal mensal = obterOuCriarMensal(funcionarioId, empresaId, ano, mes);
        mensal.setTotalHorasEsperadas(Duration.ZERO);
        mensal.setTotalHorasTrabalhadas(Duration.ZERO);

        List<ResumoPontoDia> jornadas = resumoPontoDiaRepository
                .findByFuncionarioIdAndDataBetweenOrderByPrimeiraBatidaAscCreatedAtAsc(funcionarioId, inicio, fim);

        Duration somaEsperadas = Duration.ZERO;
        Duration somaTrabalhadas = Duration.ZERO;
        for (ResumoPontoDia r : jornadas) {
            somaEsperadas = somaEsperadas.plus(Objects.requireNonNullElse(r.getTotalHorasEsperadas(), Duration.ZERO));
            somaTrabalhadas = somaTrabalhadas.plus(Objects.requireNonNullElse(r.getTotalHorasTrabalhadas(), Duration.ZERO));
        }

        mensal.setTotalHorasEsperadas(somaEsperadas);
        mensal.setTotalHorasTrabalhadas(somaTrabalhadas);
        bancoHorasMensalRepository.save(mensal);
    }

    /**
     * Acumula deltas no banco_horas_mensal do mês (aplicativo: nova jornada = +esperadas; mesma jornada SAIDA = +trabalhadas).
     * Obtém ou cria o registro, soma os deltas (nulos/zero são ignorados) e faz um único save.
     */
    @Transactional
    public void acumularNoMensal(UUID funcionarioId, UUID empresaId, int ano, int mes, Duration deltaEsperadas, Duration deltaTrabalhadas) {
        boolean addEsperadas = deltaEsperadas != null && !deltaEsperadas.isZero();
        boolean addTrabalhadas = deltaTrabalhadas != null && !deltaTrabalhadas.isZero();
        if (!addEsperadas && !addTrabalhadas) return;

        BancoHorasMensal m = obterOuCriarMensal(funcionarioId, empresaId, ano, mes);
        if (addEsperadas) m.setTotalHorasEsperadas(Objects.requireNonNullElse(m.getTotalHorasEsperadas(), Duration.ZERO).plus(deltaEsperadas));
        if (addTrabalhadas) m.setTotalHorasTrabalhadas(Objects.requireNonNullElse(m.getTotalHorasTrabalhadas(), Duration.ZERO).plus(deltaTrabalhadas));
        bancoHorasMensalRepository.save(m);
    }

    private BancoHorasMensal obterOuCriarMensal(UUID funcionarioId, UUID empresaId, int ano, int mes) {
        return bancoHorasMensalRepository
                .findByFuncionarioIdAndAnoRefAndMesRef(funcionarioId, ano, mes)
                .orElseGet(() -> {
                    BancoHorasMensal novo = new BancoHorasMensal();
                    novo.setId(UUID.randomUUID());
                    novo.setFuncionarioId(funcionarioId);
                    novo.setEmpresaId(empresaId);
                    novo.setMesRef(mes);
                    novo.setAnoRef(ano);
                    novo.setTotalHorasEsperadas(Duration.ZERO);
                    novo.setTotalHorasTrabalhadas(Duration.ZERO);
                    novo.setInconsistente(false);
                    novo.setMotivoInconsistencia(null);
                    novo.setAtivo(true);
                    novo.setDataDesativacao(null);
                    novo.setCreatedAt(LocalDateTime.now());
                    return bancoHorasMensalRepository.save(novo);
                });
    }
}
