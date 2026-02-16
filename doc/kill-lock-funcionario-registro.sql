-- Destravar transação travada em funcionario_registro_lock.
-- No Supabase: use a conexão DIRETA (Dashboard > Database > Connection string > "Direct connection", porta 5432) num cliente (DBeaver, psql). No SQL Editor pelo pooler às vezes não enxerga as sessões da API.

-- 1) Mata todas as outras sessões do mesmo banco (libera o lock)
DO $$
DECLARE
  r RECORD;
BEGIN
  FOR r IN (SELECT pid FROM pg_stat_activity WHERE pid != pg_backend_pid() AND datname = current_database())
  LOOP
    BEGIN
      PERFORM pg_terminate_backend(r.pid);
      RAISE NOTICE 'Encerrado pid %', r.pid;
    EXCEPTION WHEN OTHERS THEN
      RAISE NOTICE 'Falha ao encerrar pid %: %', r.pid, SQLERRM;
    END;
  END LOOP;
END $$;

-- 2) Apaga as linhas da tabela de lock
DELETE FROM public.funcionario_registro_lock;
