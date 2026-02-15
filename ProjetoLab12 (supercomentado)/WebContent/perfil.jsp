<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="br.com.projeto.model.Usuario" %>
<%--
    ================================================================
    PERFIL JSP - Gestão de Dados do Usuário
    ================================================================

    PROPÓSITO:
    Permite ao MEI gerenciar:
    1. Dados pessoais (nome, email, CNPJ)
    2. Senha de acesso
    3. Dados do MEI (atividade, CNAE)

    FUNCIONALIDADES:
    - Editar nome e email
    - Alterar senha (com BCrypt)
    - Atualizar CNPJ
    - Visualizar informações da conta

    SEGURANÇA:
    - Validação de sessão
    - CPF somente leitura
    - Senha atual obrigatória para trocar
    - Mínimo 6 caracteres

    ACTIONS:
    POST /perfil                    → Atualizar dados pessoais
    POST /perfil?acao=alterarSenha  → Mudar senha
    POST /perfil?acao=atualizarMEI  → Atualizar dados MEI
--%>

<%
    // ========== VALIDAÇÃO DE SESSÃO ==========
    // Redireciona para login se não autenticado
    Usuario usuario = (Usuario) session.getAttribute("usuario");
    if (usuario == null) {
        response.sendRedirect("login");
        return;
    }

    // ========== MENSAGENS (sucesso/erro) ==========
    String mensagemSucesso = (String) session.getAttribute("sucesso");
    String mensagemErro = (String) session.getAttribute("erro");
    session.removeAttribute("sucesso");
    session.removeAttribute("erro");
%>

<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Meu Perfil - Sistema MEI</title>
    <style>
        /* ================================================
           RESET E BASE
           ================================================ */
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }

        body {
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
            background-color: #0f172a;
            color: #e2e8f0;
            line-height: 1.6;
        }

        /* ================================================
           LAYOUT PRINCIPAL (sidebar + content)
           ================================================ */
        .main-container {
            display: flex;
            min-height: 100vh;
        }

        /* ================================================
           SIDEBAR
           ================================================ */
        .sidebar {
            width: 250px;
            background-color: #1e293b;
            padding: 20px 0;
            position: fixed;
            left: 0;
            top: 0;
            bottom: 0;
            overflow-y: auto;
            border-right: 1px solid #334155;
        }

        .sidebar-header {
            padding: 0 20px 20px;
            border-bottom: 1px solid #334155;
            margin-bottom: 20px;
        }

        .sidebar-header h1 {
            font-size: 24px;
            font-weight: 700;
            color: #f1f5f9;
        }

        .sidebar-menu {
            list-style: none;
        }

        .sidebar-menu a {
            display: flex;
            align-items: center;
            padding: 12px 20px;
            color: #94a3b8;
            text-decoration: none;
            transition: all 0.2s;
            gap: 12px;
        }

        .sidebar-menu a:hover {
            background-color: #334155;
            color: #f1f5f9;
        }

        .sidebar-menu a.active {
            background-color: #3b82f6;
            color: white;
            font-weight: 600;
        }

        .sidebar-menu .icon {
            font-size: 20px;
            width: 24px;
        }

        /* ================================================
           MAIN CONTENT
           ================================================ */
        .main-content {
            flex: 1;
            margin-left: 250px;
            display: flex;
            flex-direction: column;
        }

        /* ================================================
           TOPBAR
           ================================================ */
        .topbar {
            background-color: #1e293b;
            padding: 20px 30px;
            display: flex;
            justify-content: space-between;
            align-items: center;
            border-bottom: 1px solid #334155;
            position: sticky;
            top: 0;
            z-index: 100;
        }

        .topbar h2 {
            font-size: 24px;
            color: #f1f5f9;
        }

        .user-info {
            display: flex;
            align-items: center;
            gap: 12px;
        }

        .user-avatar {
            width: 40px;
            height: 40px;
            border-radius: 50%;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            display: flex;
            align-items: center;
            justify-content: center;
            font-weight: 600;
            color: white;
        }

        .user-details .name {
            font-weight: 600;
            color: #f1f5f9;
            font-size: 14px;
        }

        .user-details .role {
            font-size: 12px;
            color: #94a3b8;
        }

        /* ================================================
           CONTENT AREA
           ================================================ */
        .content {
            flex: 1;
            padding: 30px;
        }

        /* ================================================
           ALERTAS (sucesso/erro)
           ================================================ */
        .alert {
            padding: 16px 20px;
            border-radius: 8px;
            margin-bottom: 20px;
            font-weight: 500;
        }

        .alert-success {
            background-color: #10b981;
            color: white;
        }

        .alert-error {
            background-color: #ef4444;
            color: white;
        }

        /* ================================================
           CARDS
           ================================================ */
        .card {
            background-color: #1e293b;
            border-radius: 12px;
            padding: 24px;
            margin-bottom: 24px;
            border: 1px solid #334155;
        }

        .card h3 {
            font-size: 18px;
            font-weight: 600;
            color: #f1f5f9;
            margin-bottom: 20px;
            padding-bottom: 12px;
            border-bottom: 1px solid #334155;
        }

        /* ================================================
           FORMULÁRIOS
           ================================================ */
        .form-group {
            margin-bottom: 20px;
        }

        .form-group label {
            display: block;
            margin-bottom: 8px;
            font-weight: 500;
            color: #cbd5e1;
            font-size: 14px;
        }

        .form-group input,
        .form-group select {
            width: 100%;
            padding: 12px 16px;
            background-color: #0f172a;
            border: 1px solid #334155;
            border-radius: 8px;
            color: #e2e8f0;
            font-size: 14px;
            transition: all 0.2s;
        }

        .form-group input:focus,
        .form-group select:focus {
            outline: none;
            border-color: #3b82f6;
            box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
        }

        .form-group input[readonly] {
            background-color: #1e293b;
            color: #64748b;
            cursor: not-allowed;
        }

        .form-row {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 20px;
        }

        /* ================================================
           BOTÕES
           ================================================ */
        .btn {
            padding: 12px 24px;
            border: none;
            border-radius: 8px;
            font-weight: 600;
            cursor: pointer;
            transition: all 0.2s;
            font-size: 14px;
        }

        .btn-primary {
            background-color: #3b82f6;
            color: white;
        }

        .btn-primary:hover {
            background-color: #2563eb;
            transform: translateY(-1px);
            box-shadow: 0 4px 12px rgba(59, 130, 246, 0.4);
        }

        /* ================================================
           INFO TABLE (informações da conta)
           ================================================ */
        .info-table {
            width: 100%;
        }

        .info-table tr {
            border-bottom: 1px solid #334155;
        }

        .info-table td {
            padding: 16px 0;
        }

        .info-table td:first-child {
            color: #94a3b8;
            width: 200px;
        }

        .info-table td:last-child {
            color: #e2e8f0;
        }

        .badge {
            display: inline-block;
            padding: 6px 12px;
            border-radius: 6px;
            font-size: 12px;
            font-weight: 600;
        }

        .badge-success {
            background-color: #10b98120;
            color: #10b981;
        }

        /* ================================================
           RESPONSIVE
           ================================================ */
        @media (max-width: 768px) {
            .form-row {
                grid-template-columns: 1fr;
            }

            .sidebar {
                transform: translateX(-100%);
            }

            .main-content {
                margin-left: 0;
            }
        }
    </style>
</head>
<body>
    <div class="main-container">

        <!-- ================================================
             SIDEBAR - Menu de navegação
             ================================================ -->
        <aside class="sidebar">
            <div class="sidebar-header">
                <h1>📊 MEI</h1>
            </div>
            <ul class="sidebar-menu">
                <li>
                    <a href="dashboard">
                        <span class="icon">🏠</span>
                        <span>Dashboard</span>
                    </a>
                </li>
                <li>
                    <a href="historico">
                        <span class="icon">📜</span>
                        <span>Histórico</span>
                    </a>
                </li>
                <li>
                    <a href="relatorio">
                        <span class="icon">📊</span>
                        <span>Relatórios</span>
                    </a>
                </li>
                <li>
                    <a href="perfil" class="active">
                        <span class="icon">👤</span>
                        <span>Perfil</span>
                    </a>
                </li>
                <li>
                    <a href="logout">
                        <span class="icon">🚪</span>
                        <span>Sair</span>
                    </a>
                </li>
            </ul>
        </aside>

        <!-- ================================================
             MAIN CONTENT AREA
             ================================================ -->
        <div class="main-content">

            <!-- ================================================
                 TOPBAR - Cabeçalho fixo
                 ================================================ -->
            <div class="topbar">
                <h2>👤 Meu Perfil</h2>
                <div class="user-info">
                    <div class="user-avatar">
                        <%= usuario.getNome().substring(0, 1).toUpperCase() %>
                    </div>
                    <div class="user-details">
                        <div class="name"><%= usuario.getNome() %></div>
                        <div class="role">Microempreendedor</div>
                    </div>
                </div>
            </div>

            <!-- ================================================
                 CONTENT - Área principal
                 ================================================ -->
            <div class="content">

                <!-- ALERTAS (sucesso/erro do backend) -->
                <% if (mensagemSucesso != null) { %>
                    <div class="alert alert-success">
                        ✓ <%= mensagemSucesso %>
                    </div>
                <% } %>

                <% if (mensagemErro != null) { %>
                    <div class="alert alert-error">
                        ✕ <%= mensagemErro %>
                    </div>
                <% } %>

                <!-- ========================================
                     CARD 1: DADOS PESSOAIS
                     ======================================== -->
                <div class="card">
                    <h3>📝 Dados Pessoais</h3>
                    <form method="POST" action="perfil">
                        <div class="form-row">
                            <!-- Nome -->
                            <div class="form-group">
                                <label>Nome Completo *</label>
                                <input type="text" name="nome"
                                       value="<%= usuario.getNome() %>"
                                       required
                                       placeholder="Seu nome completo">
                            </div>

                            <!-- Email -->
                            <div class="form-group">
                                <label>Email *</label>
                                <input type="email" name="email"
                                       value="<%= usuario.getEmail() != null ? usuario.getEmail() : "" %>"
                                       required
                                       placeholder="seu@email.com">
                            </div>
                        </div>

                        <div class="form-row">
                            <!-- CPF (readonly) -->
                            <div class="form-group">
                                <label>CPF (não editável)</label>
                                <input type="text"
                                       value="<%= usuario.getCpf() %>"
                                       readonly>
                            </div>

                            <!-- CNPJ -->
                            <div class="form-group">
                                <label>CNPJ do MEI</label>
                                <input type="text" name="cnpj"
                                       value="<%= usuario.getCnpj() != null ? usuario.getCnpj() : "" %>"
                                       maxlength="14"
                                       placeholder="12345678000190">
                                <small style="color: #64748b; font-size: 12px;">
                                    14 dígitos (apenas números)
                                </small>
                            </div>
                        </div>

                        <button type="submit" class="btn btn-primary">
                            💾 Salvar Alterações
                        </button>
                    </form>
                </div>

                <!-- ========================================
                     CARD 2: ALTERAR SENHA
                     ======================================== -->
                <div class="card">
                    <h3>🔒 Alterar Senha</h3>
                    <form method="POST" action="perfil">
                        <input type="hidden" name="acao" value="alterarSenha">

                        <!-- Senha Atual -->
                        <div class="form-group">
                            <label>Senha Atual *</label>
                            <input type="password" name="senhaAtual"
                                   required
                                   placeholder="Digite sua senha atual">
                        </div>

                        <div class="form-row">
                            <!-- Nova Senha -->
                            <div class="form-group">
                                <label>Nova Senha *</label>
                                <input type="password" name="novaSenha"
                                       required
                                       minlength="6"
                                       placeholder="Mínimo 6 caracteres">
                            </div>

                            <!-- Confirmar -->
                            <div class="form-group">
                                <label>Confirmar Nova Senha *</label>
                                <input type="password" name="confirmarSenha"
                                       required
                                       minlength="6"
                                       placeholder="Digite novamente">
                            </div>
                        </div>

                        <button type="submit" class="btn btn-primary">
                            🔑 Alterar Senha
                        </button>
                    </form>
                </div>

                <!-- ========================================
                     CARD 3: INFORMAÇÕES DA CONTA
                     ======================================== -->
                <div class="card">
                    <h3>ℹ️ Informações da Conta</h3>
                    <table class="info-table">
                        <!-- Data de Cadastro removida (método getDataCadastro() não existe na classe Usuario) -->
                        <tr>
                            <td>CPF</td>
                            <td><%= usuario.getCpf() %></td>
                        </tr>
                        <tr>
                            <td>CNPJ</td>
                            <td>
                                <%= usuario.getCnpj() != null && !usuario.getCnpj().isEmpty()
                                    ? usuario.getCnpj()
                                    : "<span style='color: #64748b'>Não informado</span>" %>
                            </td>
                        </tr>
                        <tr>
                            <td>Status da Conta</td>
                            <td>
                                <span class="badge badge-success">
                                    ✓ Ativa
                                </span>
                            </td>
                        </tr>
                    </table>
                </div>

            </div><!-- /content -->
        </div><!-- /main-content -->
    </div><!-- /main-container -->
</body>
</html>
