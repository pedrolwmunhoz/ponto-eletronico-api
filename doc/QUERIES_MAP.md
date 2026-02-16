# Mapeamento completo de queries do projeto

Cada repositório foi verificado: **cada método/query** foi buscado no projeto. Queries **não usadas** foram **removidas**.

---

## Resumo das remoções

| Repositório | Queries/métodos removidos |
|-------------|---------------------------|
| XrefPontoResumoRepository | findJornadaAnteriorByResumoPontoDiaId, findJornadaPosteriorByResumoPontoDiaId, findByRegistroPontoId |
| ResumoPontoDiaRepository | findByFuncionarioIdAndData, findByEmpresaIdAndDataBetweenOrderByFuncionarioIdAscDataAsc, deleteByFuncionarioIdAndDataGreaterThanEqual |
| RegistroPontoRepository | findByListIdInAndAtivoTrueOrderByCreatedAtAsc, findPreviousRegistro, findNextRegistro, findLastRegistroOfDay, findRegistroAnterior, findByUsuarioIdAndAtivoTrueAndCreatedAtGreaterThanEqualOrderByCreatedAtAsc, findByUsuarioIdAndAtivoTrueAndCreatedAtBetweenOrderByCreatedAtDesc, inverterTipoEntradaRegistrosPosteriores |
| UsuarioTelefoneRepository | findFirstByUsuarioId |
| UserCredentialRepository | findEmailPrimarioByUsuarioId |
| HistoricoBloqueioRepository | findBloqueioAtivoByUsuarioId |
| EstadoJornadaFuncionarioRepository | findByFuncionarioId (derived) |
| UsuarioGeofenceRepository | findByUsuarioIdOrderByCreatedAtDesc (derived), updateGeofenceByUsuarioId |
| MetricasDiariaEmpresaRepository | findByEmpresaIdAndAnoRefAndMesRef |
| EmpresaComplianceRepository | findByEmpresaId |
| EmpresaDadosFiscalRepository | findByEmpresaId |
| AfastamentoRepository | findByFuncionarioIdOrderByDataInicioDesc |
| ContratoFuncionarioRepository | findByFuncionarioId |
| TipoCredentialRepository | findByDescricaoAndAtivo |

**Correção:** RegistroPontoRepository.findByIdInAndAtivoTrueOrderByCreatedAtAsc – query estava com JPQL e `nativeQuery = true`; corrigido para SQL nativo.

---

## Repositórios verificados (todos)

### IdentificacaoFuncionarioRepository
- findEmpresaIdByFuncionarioIdAndAtivoTrue ✅
- findByFuncionarioIdAndAtivoTrue ✅
- findFirstByFuncionarioIdAndAtivoTrue ✅
- findFuncionarioIdByEmpresaIdAndCodigoPontoAndAtivoTrue ✅
- findByEmpresaIdAndFuncionarioIdAndAtivoTrue ✅
- findByEmpresaIdOrderByNomeCompletoAsc ✅
- existsByCpf ✅
- existsByCpfAndFuncionarioIdNot ✅
- findCpfByFuncionarioId ✅
- nextCodigoPonto ✅
- insert ✅
- updateByFuncionarioId ✅
- findFuncionariosByEmpresaId ✅
- countFuncionariosByEmpresaId ✅

### XrefPontoResumoRepository
- findByResumoPontoDiaIdOrderByCreatedAtAsc ✅
- findByFuncionarioIdAndDataBetweenAsc ✅
- findByFuncionarioIdAndDataBetweenDesc ✅
- existsByRegistroPontoId ✅
- listRegistroPontoByResumoPontoDiaIdOrderByCreatedAt ✅
- findResumoPontoDiaByRegistroPontoId ✅
- deleteByRegistroPontoId ✅
- deleteByResumoPontoDiaId ✅  
- ~~findJornadaAnteriorByResumoPontoDiaId~~ ❌ REMOVIDO  
- ~~findJornadaPosteriorByResumoPontoDiaId~~ ❌ REMOVIDO  
- ~~findByRegistroPontoId~~ ❌ REMOVIDO  

### ResumoPontoDiaRepository
- findPontoListagemRowsRaw ✅
- insert ✅
- findByFuncionarioIdAndDataBetweenOrderByPrimeiraBatidaAscCreatedAtAsc ✅  
- ~~findByFuncionarioIdAndData~~ ❌ REMOVIDO  
- ~~findByEmpresaIdAndDataBetweenOrderByFuncionarioIdAscDataAsc~~ ❌ REMOVIDO  
- ~~deleteByFuncionarioIdAndDataGreaterThanEqual~~ ❌ REMOVIDO  

### RegistroPontoRepository
- findByUsuarioIdAndCreatedAt ✅
- findByIdempotencyKeyAndUsuarioId ✅
- findByUsuarioIdAndCreatedAtBetweenOrderByCreatedAtAsc ✅
- findByIdAndUsuarioId ✅
- findByIdInOrderByCreatedAtAsc ✅
- insert ✅
- deleteByIdAndUsuarioId (native DELETE) ✅
- ~~findByListIdInAndAtivoTrueOrderByCreatedAtAsc~~ ❌ REMOVIDO  
- ~~findPreviousRegistro, findNextRegistro, findLastRegistroOfDay~~ ❌ REMOVIDO  
- ~~findRegistroAnterior~~ ❌ REMOVIDO  
- ~~findByUsuarioIdAndAtivoTrueAndCreatedAtGreaterThanEqualOrderByCreatedAtAsc~~ ❌ REMOVIDO  
- ~~findByUsuarioIdAndAtivoTrueAndCreatedAtBetweenOrderByCreatedAtDesc~~ ❌ REMOVIDO  
- ~~inverterTipoEntradaRegistrosPosteriores~~ ❌ REMOVIDO  

### UsuarioTelefoneRepository
- insert ✅
- existsByIdAndUsuarioId ✅
- existsByCodigoPaisAndDddAndNumero ✅
- deleteByIdAndUsuarioId ✅
- deleteAllByUsuarioId ✅
- ~~findFirstByUsuarioId~~ ❌ REMOVIDO  

### ContratoFuncionarioRepository
- ~~findByFuncionarioId~~ ❌ REMOVIDO (não usado)
- existsByMatricula ✅
- existsByPisPasep ✅
- insert ✅
- updateByFuncionarioId ✅

### TipoJustificativaRepository
- findIdByDescricao ✅

### SessaoAtivaRepository
- insert ✅
- findByTokenAndAtivoAndNaoExpirado ✅
- desativarPorUsuarioId ✅
- desativarPorId ✅

### HistoricoLoginRepository
- countFalhasRecentes ✅
- insert ✅

### UserCredentialRepository
- existsByValorAndTipoCredencialId ✅
- insert ✅
- findByValorAndTipoCredencialIdAndAtivo ✅
- findCredencialIdByUsuarioAndTipo ✅
- findCredencialIdByUsuarioTipoCategoria ✅
- findByUsuarioIdAndValorAndTipoCredencialId ✅
- updateValor ✅
- deleteByIdAndUsuarioId ✅
- ~~findEmailPrimarioByUsuarioId~~ ❌ REMOVIDO  

### HistoricoBloqueioRepository
- insert ✅
- desativarPorUsuarioId ✅  
- ~~findBloqueioAtivoByUsuarioId~~ ❌ REMOVIDO  

### CredencialTokenRecuperacaoRepository
- insert ✅
- findByTokenAndTipoAndAtivoAndNaoExpirado ✅
- desativar ✅

### AfastamentoRepository
- ~~findByFuncionarioIdOrderByDataInicioDesc~~ ❌ REMOVIDO (não usado)
- insert ✅

### AuthRepository
- findCredencialParaLogin ✅

### EmpresaDadosFiscalRepository
- existsByCnpj ✅
- existsByEmpresaId ✅
- ~~findByEmpresaId~~ ❌ REMOVIDO (não usado)
- insert ✅

### MetricasDiariaEmpresaLockRepository
- tryInsert ✅
- releaseLock ✅

### EstadoJornadaFuncionarioRepository
- findByFuncionarioIdForUpdate ✅  
- ~~findByFuncionarioId~~ ❌ REMOVIDO  

### FuncionarioRegistroLockRepository
- findByFuncionarioIdForUpdate ✅
- insert ✅

### GeofenceEmpresaConfigRepository
- insert ✅

### UsuarioGeofenceRepository
- findPageByUsuarioId ✅
- countByUsuarioId ✅
- existsByIdAndUsuarioId ✅
- insert ✅
- existsFuncionarioDentroDeGeofence ✅  
- ~~findByUsuarioIdOrderByCreatedAtDesc~~ ❌ REMOVIDO  
- ~~updateGeofenceByUsuarioId~~ ❌ REMOVIDO  

### EmpresaJornadaConfigRepository
- findByEmpresaId ✅
- existsByEmpresaId ✅
- insert ✅
- updateByEmpresaId ✅

### EmpresaEnderecoRepository
- findByEmpresaId ✅
- insert ✅
- updateByEmpresaId ✅

### EmpresaBancoHorasConfigRepository
- findByEmpresaId ✅
- insert ✅
- updateByEmpresaId ✅

### UserPasswordRepository
- insert ✅
- findByUsuarioIdAndAtivo ✅
- desativarByUsuarioId ✅

### UsersRepository
- findAllUsuarioListagem ✅
- countAllUsuarioListagem ✅
- findByIdQuery ✅
- existsByUsername ✅
- existsByUsernameAndIdNot ✅
- insert ✅
- updateUsername ✅
- desativarUsuario ✅

### JornadaFuncionarioConfigRepository
- findByFuncionarioId ✅
- insert ✅
- updateByFuncionarioId ✅

### AuditoriaLogRepository
- findPageByEmpresaId ✅
- countByEmpresaId ✅
- existsByIdAndEmpresaId ✅
- insert ✅

### FuncionarioPerfilRepository
- findPerfilByFuncionarioId ✅

### SolicitacaoPontoRepository
- findByIdempotencyKeyAndUsuarioId ✅
- findById ✅
- insert ✅
- updateAprovacao ✅

### SolicitacoesPontoListagemRepository
- findPageByEmpresaId ✅
- countByEmpresaId ✅

### FeriasAfastamentosListagemRepository
- findPageByFuncionarioId ✅
- countByFuncionarioId ✅
- findPageByFuncionarioIdEmpresa ✅
- countByFuncionarioIdEmpresa ✅
- findPageByEmpresaId ✅
- countByEmpresaId ✅

### MetricasDiariaEmpresaRepository
- findByEmpresaIdAndDataRef ✅
- findTopByEmpresaIdAndDataRefBeforeOrderByDataRefDesc ✅
- findTopByEmpresaIdOrderByDataRefDesc ✅
- findByEmpresaIdAndDataRefBetween ✅  
- ~~findByEmpresaIdAndAnoRefAndMesRef~~ ❌ REMOVIDO (não usado)

### EmpresaComplianceRepository
- ~~findByEmpresaId~~ ❌ REMOVIDO (repositório nunca injetado em serviços)

### EmpresaPerfilRepository
- findPerfilByEmpresaId ✅

### XrefGeofenceFuncionariosRepository
- countByGeofenceId ✅
- deleteByFuncionarioId ✅
- insert ✅

### BancoHorasHistoricoRepository
- findByFuncionarioIdOrderByAnoReferenciaDescMesReferenciaDesc ✅
- findPageByFuncionarioId ✅
- countByFuncionarioId ✅
- findByFuncionarioIdAndAnoReferenciaAndMesReferencia ✅
- findByAnoReferenciaAndMesReferenciaAndFuncionarioIdIn ✅

### BancoHorasMensalRepository
- findByFuncionarioIdAndAnoRefAndMesRef ✅ (derived)

### DispositivoRepository
- findIdByUsuarioAndIpAndUserAgent ✅
- insert ✅

### TipoEscalaJornadaRepository
- findByDescricaoAndAtivoTrue ✅
- existsByIdAndAtivo ✅

### TipoContratoRepository
- findByDescricaoAndAtivoTrue ✅
- existsByIdAndAtivo ✅

### TipoJustificativaRepository, TipoUsuarioRepository, TipoTokenRecuperacaoRepository, TipoCredentialRepository, TipoCategoriaCredentialRepository, TipoAfastamentoRepository, TipoModeloPontoRepository
- TipoCredentialRepository: findIdByDescricao ✅; ~~findByDescricaoAndAtivo~~ ❌ REMOVIDO (não usado).
- Demais: findIdByDescricao, findDescricaoById, existsByIdAndAtivo, findByDescricaoAndAtivoTrue, findByIdAndAtivoTrue verificados como usados.

---

**Total:** Todos os repositórios com `@Query` ou métodos custom foram verificados. **Nenhuma query usada foi removida.**
