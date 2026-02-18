package com.pontoeletronico.api.domain.services.bancohoras;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Guarda ano/mês da jornada afetada e, quando aplicável, da jornada anterior e posterior.
 * Usado pelo BancoHorasMensalService para decidir quais meses recalcular: sempre o mês da
 * jornada afetada; anterior/posterior só se estiverem em outro ano/mês.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RangeJornadasAfetadas {

    private int jornadaAfetadaAno;
    private int jornadaAfetadaMes;
    private Integer jornadaAnteriorAno;
    private Integer jornadaAnteriorMes;
    private Integer jornadaPosteriorAno;
    private Integer jornadaPosteriorMes;
}
