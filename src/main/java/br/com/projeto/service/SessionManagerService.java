package br.com.projeto.service;

import br.com.projeto.model.Usuario;
import br.com.projeto.util.AppException;

public final class SessionManagerService {

    private static final SessionManagerService INSTANCIA = new SessionManagerService();

    private Usuario usuarioLogado;

    private SessionManagerService() {
    }

    public static SessionManagerService getInstance() {
        return INSTANCIA;
    }

    public synchronized void iniciar(Usuario usuario) {
        this.usuarioLogado = usuario;
    }

    public synchronized void encerrar() {
        this.usuarioLogado = null;
    }

    public synchronized boolean estaLogado() {
        return usuarioLogado != null;
    }

    public synchronized Usuario exigirUsuario() {
        if (usuarioLogado == null) {
            throw new AppException("Sessão expirada. Faça login novamente.");
        }
        return usuarioLogado;
    }
}