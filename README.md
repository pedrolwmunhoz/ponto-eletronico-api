# Ponto Eletrônico API

API REST para sistema de ponto eletrônico em modelo SaaS: registro de ponto (manual, aplicativo e público), jornadas diárias, banco de horas, solicitações de alteração, relatórios (PDF/Excel), férias/afastamentos, geofences e auditoria.

---

## Índice

- [Tecnologias](#tecnologias)
- [Pré-requisitos](#pré-requisitos)
- [Configuração](#configuração)
- [Executando](#executando)
- [Autenticação](#autenticação)
- [Documentação da API (Swagger)](#documentação-da-api-swagger)
- [Endpoints por módulo](#endpoints-por-módulo)
- [Modelo de dados (resumo)](#modelo-de-dados-resumo)
- [Estrutura do projeto](#estrutura-do-projeto)
- [Frontend](#frontend)
- [Testes](#testes)
- [Observações de desenvolvimento](#observações-de-desenvolvimento)

---

## Tecnologias

| Tecnologia | Uso |
|------------|-----|
| **Java 21** | Linguagem |
| **Spring Boot 4** | Framework |
| **Spring Data JPA** | Persistência |
| **PostgreSQL** | Banco de dados |
| **Spring Security** + **OAuth2 Resource Server** | Autenticação/autorização via JWT |
| **SpringDoc OpenAPI 2.x** | Documentação Swagger UI |
| **Lombok** | Redução de boilerplate |
| **JJWT** | Geração e validação de tokens JWT |
| **OpenPDF** | Geração de relatórios em PDF |
| **Apache POI** | Export de relatórios em Excel |

---

## Pré-requisitos

- **JDK 21**
- **Maven 3.8+**
- **PostgreSQL 13+** (recomendado; `gen_random_uuid()` usado no schema)

---

## Configuração

### 1. Banco de dados

Crie o banco e execute o schema (ordem respeitando FKs):

```bash
createdb ponto_db
psql -d ponto_db -f doc/schema.sql
```

O script em `doc/schema.sql`:

- Cria a extensão `unaccent` (busca insensível a acentos).
- Cria tabelas de tipos/catálogos (tipo_usuario, tipo_contrato, tipo_justificativa, etc.).
- Insere dados iniciais (seeds) idempotentes.
- Cria tabelas de negócio: `users`, `identificacao_funcionario`, `empresa_*`, `registro_ponto`, `resumo_ponto_dia`, `xref_ponto_resumo`, `solicitacao_ponto`, `banco_horas_*`, `auditoria_log`, etc.

**Importante:** Todas as colunas de data/hora no schema são **TIMESTAMP (sem time zone)** e devem ser mapeadas como `LocalDateTime` na aplicação. Não usar `TIMESTAMPTZ` nem `Instant` no mapeamento.

### 2. Chaves JWT

A aplicação espera um par de chaves para assinar/validar JWT:

- **Chave privada:** `src/main/resources/keys/app.key`
- **Chave pública:** `src/main/resources/keys/app.pub`

Exemplo de geração (RSA 2048):

```bash
mkdir -p src/main/resources/keys
openssl genrsa -out src/main/resources/keys/app.key 2048
openssl rsa -in src/main/resources/keys/app.key -pubout -out src/main/resources/keys/app.pub
```

### 3. Arquivo de configuração

Edite `src/main/resources/application.yml` conforme o ambiente:

| Propriedade | Descrição | Padrão |
|-------------|-----------|--------|
| `server.port` | Porta HTTP da API | `8081` |
| `spring.datasource.url` | URL JDBC do PostgreSQL | `jdbc:postgresql://localhost:5432/ponto_db` |
| `spring.datasource.username` | Usuário do banco | `postgres` |
| `spring.datasource.password` | Senha do banco | `postgres` |
| `jwt.public.key` | Classpath da chave pública JWT | `classpath:keys/app.pub` |
| `jwt.private.key` | Classpath da chave privada JWT | `classpath:keys/app.key` |
| `app.jwt.expiration-ms` | Expiração do access token (ms) | `900000` (15 min) |
| `app.jwt.refresh-expiration-ms` | Expiração do refresh token (ms) | `86400000` (24 h) |
| `app.brute-force.max-tentativas` | Tentativas de login antes de bloqueio | `5` |
| `app.brute-force.janela-minutos` | Janela de tempo para contagem de tentativas | `15` |

O JPA está configurado com `ddl-auto: none`; o schema é gerenciado apenas pelo script SQL.

---

## Executando

```bash
mvn spring-boot:run
```

A API fica disponível em **http://localhost:8081**.

---

## Autenticação

A API usa **JWT** (Bearer token) como access token.

1. **Login** – `POST /api/auth`  
   - Body: `{ "email": "...", "senha": "..." }` (campos podem variar conforme DTO).  
   - Resposta: access token, refresh token e dados do usuário (conforme `LoginResponse`).

2. **Requisições autenticadas** – enviar o access token no header:
   ```http
   Authorization: Bearer <access_token>
   ```

3. **Refresh** – `POST /api/auth/refresh`  
   - Body: `{ "refreshToken": "..." }`.  
   - Retorna novo par de tokens.

4. **Logout** – `POST /api/auth/logout`  
   - Header `Authorization: Bearer <access_token>` para invalidar a sessão.

5. **Recuperação de senha** – fluxo em etapas:  
   - `POST /api/auth/recuperar-senha` → envia código (ex.: e-mail).  
   - `POST /api/auth/validar-codigo` → valida código.  
   - `POST /api/auth/resetar-senha` → define nova senha.

Endpoints públicos (sem Bearer) costumam ser apenas login, recuperar-senha, validar-codigo, resetar-senha e possivelmente registro de ponto público (conforme regras de segurança do controller).

---

## Documentação da API (Swagger)

Com a aplicação rodando:

- **Swagger UI:** http://localhost:8081/swagger-ui.html

Lá é possível ver todos os endpoints, DTOs e testar as requisições (informando o Bearer token quando necessário).

---

## Endpoints por módulo

Base URL: `http://localhost:8081`  
Prefixo comum: `/api`.  
Autenticação: Bearer JWT, exceto onde indicado como público.

### Auth — `POST /api/auth`

| Método | Caminho | Descrição |
|--------|---------|-----------|
| POST | `/api/auth` | Login (público) |
| POST | `/api/auth/recuperar-senha` | Iniciar recuperação de senha |
| POST | `/api/auth/validar-codigo` | Validar código de recuperação |
| POST | `/api/auth/resetar-senha` | Redefinir senha com código válido |
| POST | `/api/auth/refresh` | Renovar access e refresh token |
| POST | `/api/auth/logout` | Encerrar sessão |

### Perfil — `/api`

| Método | Caminho | Descrição |
|--------|---------|-----------|
| GET | `/api/funcionario/perfil` | Perfil do funcionário logado |
| GET | `/api/empresa/perfil` | Perfil da empresa logada |

### Registro de ponto (funcionário / empresa) — `/api`

| Método | Caminho | Descrição |
|--------|---------|-----------|
| GET | `/api/funcionario/{funcionarioId}/ponto` | Listar ponto do mês (jornadas e marcações) |
| POST | `/api/funcionario/registro-ponto/manual` | Registro manual (exige **Idempotency-Key**) |
| POST | `/api/empresa/registro-ponto/publico` | Registro de ponto público |
| POST | `/api/empresa/funcionario/registro-ponto` | Registro de ponto por empresa (ex.: tablet) |
| DELETE | `/api/empresa/registro-ponto/{idRegistro}` | Soft delete de registro (recalcula jornadas/banco) |

### Empresa — `/api/empresa`

| Método | Caminho | Descrição |
|--------|---------|-----------|
| POST | `/api/empresa` | Cadastrar empresa |
| PUT | `/api/empresa/endereco` | Atualizar endereço |
| GET | `/api/empresa/config-inicial/status` | Status da configuração inicial |
| POST | `/api/empresa/config-inicial` | Concluir configuração inicial |
| POST | `/api/empresa/resetar-senha` | Resetar senha da empresa |
| PUT | `/api/empresa/jornada-padrao` | Atualizar jornada padrão |
| PUT | `/api/empresa/banco-horas-config` | Configurar banco de horas |
| GET | `/api/empresa/metricas-dia` | Métricas do dia |
| GET | `/api/empresa/metricas-dia/por-periodo` | Métricas por período |

### Empresa – Ponto do funcionário e solicitações — `/api/empresa`

| Método | Caminho | Descrição |
|--------|---------|-----------|
| GET | `/api/empresa/funcionario/{funcionarioId}/ponto` | Listar ponto do funcionário (visão empresa) |
| DELETE | `/api/empresa/funcionario/{funcionarioId}/registro-ponto/{registroId}` | Excluir registro de ponto |
| PUT | `/api/empresa/funcionario/{funcionarioId}/registro-ponto/{registroId}` | Alterar registro (ex.: justificativa) |
| POST | `/api/empresa/funcionario/{funcionarioId}/registro-ponto` | Inserir registro manual (empresa) |
| GET | `/api/empresa/solicitacoes-ponto` | Listar solicitações de ponto |
| POST | `/api/empresa/solicitacoes-ponto/{idRegistroPendente}/aprovar` | Aprovar solicitação |
| POST | `/api/empresa/solicitacoes-ponto/{idRegistroPendente}/reprovar` | Reprovar solicitação |

### Funcionários (empresa) — `/api/empresa`

| Método | Caminho | Descrição |
|--------|---------|-----------|
| POST | `/api/empresa/funcionario` | Cadastrar funcionário |
| GET | `/api/empresa/funcionario` | Listar funcionários |
| GET | `/api/empresa/funcionario/{funcionarioId}/perfil` | Perfil do funcionário |
| PUT | `/api/empresa/funcionario/{funcionarioId}` | Atualizar funcionário |
| DELETE | `/api/empresa/funcionario/{funcionarioId}` | Remover funcionário |
| POST | `/api/empresa/funcionario/{funcionarioId}/resetar-senha` | Resetar senha do funcionário |
| POST | `/api/empresa/funcionario/{funcionarioId}/resetar-email` | Resetar e-mail do funcionário |
| POST | `/api/empresa/funcionario/{funcionarioId}/desbloquear` | Desbloquear funcionário (bloqueio por tentativas) |

### Banco de horas — `/api/empresa`

| Método | Caminho | Descrição |
|--------|---------|-----------|
| GET | `/api/empresa/funcionario/{funcionarioId}/resumo-banco-horas` | Resumo do banco de horas do funcionário |
| GET | `/api/empresa/funcionario/{funcionarioId}/banco-horas-historico` | Histórico de banco de horas |
| POST | `/api/empresa/banco-horas/compensacao` | Registrar compensação |
| POST | `/api/empresa/funcionario/{funcionarioId}/banco-horas/fechamento` | Fechamento de banco de horas |

### Relatórios — `/api/empresa/relatorios`

| Método | Caminho | Descrição |
|--------|---------|-----------|
| POST | `/api/empresa/relatorios/ponto-detalhado` | Relatório de ponto detalhado (PDF/Excel, comprimido) |
| POST | `/api/empresa/relatorios/ponto-resumo` | Relatório de ponto resumo (PDF/Excel, comprimido) |

### Férias e afastamentos — `/api`

| Método | Caminho | Descrição |
|--------|---------|-----------|
| GET | `/api/funcionario/ferias-afastamentos` | Listar do funcionário logado |
| GET | `/api/empresa/funcionario/{funcionarioId}/ferias-afastamentos` | Listar por funcionário |
| GET | `/api/empresa/ferias-afastamentos` | Listar da empresa |
| POST | `/api/empresa/funcionario/{funcionarioId}/afastamentos` | Cadastrar afastamento |

### Geofences — `/api/empresa`

| Método | Caminho | Descrição |
|--------|---------|-----------|
| GET | `/api/empresa/geofences` | Listar geofences |
| POST | `/api/empresa/geofences` | Criar geofence |

### Auditoria — `/api/empresa`

| Método | Caminho | Descrição |
|--------|---------|-----------|
| GET | `/api/empresa/auditoria` | Listar logs de auditoria |
| GET | `/api/empresa/auditoria/{logId}` | Detalhe de um log |

### Admin — `/api/admin`

| Método | Caminho | Descrição |
|--------|---------|-----------|
| POST | `/api/admin/usuarios` | Criar usuário admin |
| GET | `/api/admin/usuarios` | Listar usuários |
| POST | `/api/admin/usuario/{usuarioId}/desbloquear` | Desbloquear usuário |

### Usuário — `/api/usuario`

| Método | Caminho | Descrição |
|--------|---------|-----------|
| PUT | `/api/usuario/perfil` | Atualizar perfil do usuário logado |
| POST | `/api/usuario/email` | Adicionar/alterar e-mail |
| DELETE | `/api/usuario/email` | Remover e-mail |
| POST | `/api/usuario/telefone` | Adicionar telefone |
| DELETE | `/api/usuario/telefone/{telefoneId}` | Remover telefone |
| POST | `/api/usuario/credential` | Adicionar credencial |
| DELETE | `/api/usuario/credential/{credentialId}` | Remover credencial |

---

## Modelo de dados (resumo)

- **Usuários e perfis:** `users`, `user_credential`, `user_password`, `usuario_telefone`, `tipo_usuario`, `tipo_credential`, etc.
- **Empresa:** `empresa_dados_fiscal`, `empresa_endereco`, `empresa_jornada_config`, `empresa_banco_horas_config`, `jornada_funcionario_config`, `identificacao_funcionario`, `contrato_funcionario`.
- **Ponto:** `registro_ponto` (marcações), `resumo_ponto_dia` (jornada diária), `xref_ponto_resumo` (vínculo registro ↔ resumo), `estado_jornada_funcionario` (última batida/jornada para app), `registro_metadados`, `solicitacao_ponto`, `tipo_justificativa`.
- **Banco de horas:** `banco_horas_mensal`, `banco_horas_historico`, `tipo_status_pagamento`.
- **Segurança e auditoria:** `historico_bloqueio`, `historico_login`, `sessao_ativa`, `dispositivo`, `credencial_token_recuperacao`, `auditoria_log`.
- **Outros:** `geofence_empresa_config`, `usuario_geofence`, `afastamento`, `metricas_diaria_empresa`, `alerta_divergencia`, `notificacao`, etc.

O schema completo, tipos ENUM e seeds estão em **`doc/schema.sql`**.

---

## Estrutura do projeto

```
src/main/java/com/pontoeletronico/api/
├── Application.java
├── domain/
│   ├── entity/          # Entidades JPA (registro, empresa, auth, usuario, etc.)
│   ├── enums/            # Enums de negócio (MensagemErro, FormatoRelatorio, etc.)
│   └── services/         # Regras de negócio
│       ├── auth/         # Login, refresh, recuperar senha, logout, dispositivo
│       ├── admin/        # Admin: criar/listar usuários, desbloquear
│       ├── bancohoras/   # Jornada, resumo diário, banco horas (manual, app, soft delete)
│       ├── registro/     # Registro de ponto, validação, lock, listagem
│       ├── empresa/      # Cadastro empresa, jornada, banco horas config, métricas, solicitações
│       ├── funcionario/  # CRUD funcionário, reset senha/email, desbloquear
│       ├── perfil/       # Perfil funcionário e empresa
│       ├── relatorio/    # Relatórios ponto (detalhado/resumo, PDF/Excel)
│       ├── feriasafastamentos/
│       ├── geofence/
│       ├── auditoria/
│       └── util/         # Utils (ex.: ObterJornadaConfigUtils)
├── infrastructure/
│   ├── input/
│   │   ├── controller/           # REST controllers
│   │   └── controller/openapi/   # Contratos Swagger (interfaces)
│   ├── output/
│   │   └── repository/           # JPA repositories (registro, empresa, auth, etc.)
│   └── input/dto/                # DTOs de request/response
└── exception/                    # GlobalExceptionHandler, exceções de negócio
```

- **`doc/schema.sql`** – Criação completa do banco (tabelas, FKs, índices, seeds).

---

## Frontend

O repositório inclui um frontend em **`frontend/ponto-eletronico-frontend/`**:

- **Stack:** React 18, TypeScript, Vite, React Router, TanStack Query, React Hook Form, Zod, Tailwind CSS, Radix UI (shadcn), Axios, date-fns, Recharts.
- **Comandos típicos:**
  ```bash
  cd frontend/ponto-eletronico-frontend
  npm install
  npm run dev
  ```
- Configure a URL base da API (ex.: `http://localhost:8081`) no cliente HTTP (Axios ou env) para apontar para esta API.

---

## Testes

```bash
mvn test
```

O projeto usa Spring Boot Test, incluindo suporte a testes de segurança, web e JPA.

---

## Observações de desenvolvimento

- **Registro manual de ponto:** O endpoint de registro manual exige o header **Idempotency-Key** (UUID) para evitar duplicidade em retentativas.
- **Soft delete de registro:** Ao excluir um registro de ponto (DELETE), a API recalcula jornadas e pode dividir uma jornada em duas se o intervalo entre o registro anterior e o posterior for maior ou igual ao `tempoDescansoEntreJornada` (evitando salvar entidade já deletada).
- **Xref e listagem:** O vínculo `registro_ponto` ↔ `resumo_ponto_dia` é único por `registro_ponto_id`; inserções de xref são idempotentes (não duplicam).
- **Datas:** Persistência e APIs usam `LocalDateTime`/`LocalDate`; no banco, colunas são TIMESTAMP sem time zone.
- **Convenção de nomes:** Repositórios e entidades seguem snake_case no banco; projeções nativas retornam `Object[]` quando necessário para evitar problemas de mapeamento de colunas (ex.: listagem de ponto com marcações em JSON).

---

## Licença

Conforme definido no projeto.
