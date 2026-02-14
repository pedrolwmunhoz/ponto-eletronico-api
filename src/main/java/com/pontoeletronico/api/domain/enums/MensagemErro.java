package com.pontoeletronico.api.domain.enums;

public enum MensagemErro {

    BAD_REQUEST("Dados inválidos"),
    CREDENCIAL_INVALIDA("Credencial inválida"),
    USUARIO_INATIVO("Usuário inativo"),
    USUARIO_BLOQUEADO("Usuário bloqueado"),
    TENTATIVAS_EXCEDIDAS("Usuário bloqueado. Muitas tentativas de login."),
    SENHA_EXPIRADA("Senha expirada. É necessário redefinir a senha."),
    TIPO_CREDENCIAL_NAO_ENCONTRADO("Tipo de credencial não encontrado no cadastro"),
    TIPO_USUARIO_NAO_ENCONTRADO("Tipo de usuário não encontrado no cadastro"),
    TIPO_CONTRATO_NAO_ENCONTRADO("Tipo de contrato não encontrado ou inativo"),
    TIPO_ESCALA_JORNADA_NAO_ENCONTRADO("Tipo de escala de jornada não encontrado ou inativo"),
    TIPO_TOKEN_RECUPERACAO_NAO_ENCONTRADO("Tipo de token de recuperação não encontrado no cadastro"),
    TIPO_MOTIVO_SOLICITACAO_NAO_ENCONTRADO("Tipo de motivo de solicitação não encontrado ou inativo"),
    TOKEN_JWT_INVALIDO("Token inválido ou expirado"),
    AUTORIZACAO_INVALIDA("Cabeçalho de autorização inválido ou ausente"),
    DISPOSITIVO_DIFERENTE("O refresh deve ser feito do mesmo dispositivo em que o login foi realizado"),
    REFRESH_TOKEN_INVALIDO("Refresh token inválido ou expirado"),
    CODIGO_RECUPERACAO_INVALIDO("Código de recuperação inválido ou expirado"),
    TOKEN_RECUPERACAO_INVALIDO("Token de recuperação inválido ou expirado"),
    EMAIL_NAO_CADASTRADO("Email não cadastrado no sistema"),
    USUARIO_NAO_ENCONTRADO("Usuário não encontrado"),
    EMPRESA_NAO_ENCONTRADA("Empresa não encontrada."),
    CREDENCIAL_NAO_ENCONTRADA("Credencial não encontrada para o usuário"),
    ERRO_ATUALIZACAO_SENHA("Não foi possível atualizar a senha"),
    SENHA_ANTIGA_INCORRETA("Senha atual incorreta"),
    IP_USER_AGENT_OBRIGATORIOS("Não foi possível identificar o dispositivo. IP ou User-Agent são obrigatórios para validação."),
    USERNAME_JA_CADASTRADO("Username já cadastrado"),
    CNPJ_JA_CADASTRADO("CNPJ já cadastrado"),
    EMAIL_JA_CADASTRADO("Email já cadastrado"),
    CPF_JA_CADASTRADO("CPF já cadastrado"),
    VALOR_CREDENCIAL_JA_CADASTRADO("Valor da credencial já cadastrado"),
    NAO_ADMIN("Acesso negado. Apenas administradores podem executar esta ação."),
    FUNCIONARIO_NAO_PERTENCE_EMPRESA("Funcionário não pertence a esta empresa."),
    FUNCIONARIO_NAO_ENCONTRADO("Funcionário não encontrado."),
    BLOQUEIO_NAO_ENCONTRADO("Nenhum bloqueio ativo encontrado para o usuário."),
    TELEFONE_NAO_ENCONTRADO("Telefone não encontrado para o usuário."),
    TELEFONE_JA_CADASTRADO("Telefone já cadastrado no sistema."),
    ERRO("Erro");


    private final String mensagem;

    MensagemErro(String mensagem) {
        this.mensagem = mensagem;
    }

    public String getMensagem() {
        return mensagem;
    }
}
