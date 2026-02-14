package com.pontoeletronico.api.domain.services.admin;

import com.pontoeletronico.api.exception.NaoAdminException;
import com.pontoeletronico.api.infrastructure.input.dto.admin.UsuarioListagemResponse;
import com.pontoeletronico.api.infrastructure.input.dto.admin.UsuarioListagemPageResponse;
import com.pontoeletronico.api.infrastructure.input.dto.common.Paginacao;
import com.pontoeletronico.api.infrastructure.output.repository.empresa.TipoUsuarioRepository;
import com.pontoeletronico.api.infrastructure.output.repository.usuario.UsersRepository;
import com.pontoeletronico.api.util.JwtUtil;
import com.pontoeletronico.api.util.ListagemParseUtil;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AdminListarUsuariosService {

    private final JwtUtil jwtUtil;
    private final TipoUsuarioRepository tipoUsuarioRepository;
    private final UsersRepository usersRepository;

    public AdminListarUsuariosService(JwtUtil jwtUtil,
                                     TipoUsuarioRepository tipoUsuarioRepository,
                                     UsersRepository usersRepository) {
        this.jwtUtil = jwtUtil;
        this.tipoUsuarioRepository = tipoUsuarioRepository;
        this.usersRepository = usersRepository;
    }

    /** Doc id 52: Listar usuários (admin). page e size vindos do controller; query usa limit e offset. */
    public UsuarioListagemPageResponse listar(String authorizationHeader, int page, int size) {
        var adminUsuarioId = jwtUtil.extractUserIdFromToken(authorizationHeader);
        validarAdmin(adminUsuarioId);
        int limit = Math.max(1, Math.min(size, 100));
        int offset = Math.max(0, page) * limit;
        var list = usersRepository.findAllUsuarioListagem(limit, offset);
        long total = usersRepository.countAllUsuarioListagem();
        var conteudo = list.stream()
                .map(p -> {
                    var emails = ListagemParseUtil.parseEmails(p.getEmails());
                    var telefones = ListagemParseUtil.parseTelefones(p.getTelefones());
                    return new UsuarioListagemResponse(p.getUsuarioId(), p.getUsername(), p.getTipo(), emails, telefones);
                })
                .toList();
        int totalPaginas = (int) Math.max(1, (total + limit - 1) / limit);
        var paginacao = new Paginacao(totalPaginas, total, conteudo.size(), Math.max(0, page));
        return new UsuarioListagemPageResponse(paginacao, conteudo);
    }

    private void validarAdmin(UUID usuarioId) {
        var tipoAdminId = tipoUsuarioRepository.findIdByDescricao("ADMIN");
        if (tipoAdminId == null) {
            throw new NaoAdminException();
        }
        var user = usersRepository.findByIdQuery(usuarioId).orElseThrow(NaoAdminException::new);
        if (!tipoAdminId.equals(user.getTipoUsuarioId())) {
            throw new NaoAdminException();
        }
    }
}
