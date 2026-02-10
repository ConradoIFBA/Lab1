<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="br.com.projeto.model.Usuario" %>

<%
    Usuario usuario = (Usuario) session.getAttribute("usuario");
    if (usuario == null) {
        response.sendRedirect("login");
        return;
    }
%>

<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Perfil - Sistema MEI</title>
    <link rel="stylesheet" href="css/layout-sidebar.css">
    <script>
        document.addEventListener('DOMContentLoaded', function() {
            const toggleBtn = document.getElementById('toggleSidebar');
            const sidebar = document.getElementById('sidebar');
            const mainContent = document.getElementById('mainContent');

            if (toggleBtn) {
                toggleBtn.addEventListener('click', function() {
                    sidebar.classList.toggle('collapsed');
                    mainContent.classList.toggle('collapsed-margin');
                });
            }
        });
    </script>
        <link rel="stylesheet" href="css/sidebar.css">

</head>
<body>
    <div class="main-container">

        <!-- SIDEBAR -->
        <aside class="sidebar" id="sidebar">
            <div class="sidebar">
                <div class="sidebar-header">
                    <h1>📊 MEI</h1>
                </div>
                <ul class="sidebar-menu">
                    <li>
                        <a href="dashboard" class="<%= request.getRequestURI().contains("dashboard") ? "active" : "" %>">
                            <span class="icon">🏠</span>
                            <span class="label">Dashboard</span>
                        </a>
                    </li>
                    <li>
                        <a href="historico" class="<%= request.getRequestURI().contains("historico") ? "active" : "" %>">
                            <span class="icon">📜</span>
                            <span class="label">Histórico</span>
                        </a>
                    </li>
                    <li>
                        <a href="relatorio.jsp" class="<%= request.getRequestURI().contains("relatorio") ? "active" : "" %>">
                            <span class="icon">📊</span>
                            <span class="label">Relatório</span>
                        </a>
                    </li>
                    <li>
                        <a href="perfil" class="<%= request.getRequestURI().contains("perfil") ? "active" : "" %>">
                            <span class="icon">👤</span>
                            <span class="label">Perfil</span>
                        </a>
                    </li>
                    <li>
                        <a href="logout">
                            <span class="icon">🚪</span>
                            <span class="label">Sair</span>
                        </a>
                    </li>
                </ul>
            </div>
        </aside>

        <!-- MAIN CONTENT -->
        <div class="main-content" id="mainContent">

            <!-- TOPBAR -->
            <div class="topbar">
                <div class="topbar-left">
                    <h2>Perfil do Usuário</h2>
                </div>

                <div class="topbar-right">
                    <div class="user-info">
                        <div class="user-avatar"><%= usuario.getNome().charAt(0) %></div>
                        <div class="user-details">
                            <p class="name"><%= usuario.getNome() %></p>
                            <p class="role">Microempreendedor</p>
                        </div>
                    </div>
                    <a href="logout" class="logout-btn">Sair</a>
                </div>
            </div>

            <!-- CONTENT -->
            <div class="content">

                <!-- DADOS PESSOAIS CARD -->
                <div class="card">
                    <h3>Dados Pessoais</h3>
                    <form method="post" action="perfil">
                        <div class="form-row">
                            <div class="form-group">
                                <label>Nome Completo:</label>
                                <input type="text" name="nome" value="<%= usuario.getNome() %>" required>
                            </div>

                            <div class="form-group">
                                <label>CPF:</label>
                                <input type="text" name="cpf" value="<%= usuario.getCpf() %>" readonly style="background: #f5f5f5;">
                            </div>
                        </div>

                        <div class="form-group">
                            <label>Email:</label>
                            <input type="email" name="email" value="<%= usuario.getEmail() %>" required>
                        </div>

                        <button type="submit" class="btn btn-primary">Salvar Alterações</button>
                    </form>
                </div>

                <!-- ALTERAR SENHA CARD -->
                <div class="card">
                    <h3>Alterar Senha</h3>
                    <form method="post" action="perfil">
                        <input type="hidden" name="acao" value="alterarSenha">

                        <div class="form-group">
                            <label>Senha Atual:</label>
                            <input type="password" name="senhaAtual" required>
                        </div>

                        <div class="form-group">
                            <label>Nova Senha:</label>
                            <input type="password" name="novaSenha" required minlength="6">
                        </div>

                        <div class="form-group">
                            <label>Confirmar Nova Senha:</label>
                            <input type="password" name="confirmarSenha" required minlength="6">
                        </div>

                        <button type="submit" class="btn btn-primary">Alterar Senha</button>
                    </form>
                </div>

                <!-- DADOS MEI CARD -->
                <div class="card">
                    <h3>Dados do MEI</h3>
                    <form method="post" action="perfil">
                        <input type="hidden" name="acao" value="atualizarMEI">

                        <div class="form-group">
                            <label>Atividade Principal:</label>
                            <select name="atividade">
                                <option value="">Selecione...</option>
                                <option value="revenda">Revenda de Mercadorias</option>
                                <option value="industria">Produtos Industrializados</option>
                                <option value="servicos" selected>Prestação de Serviços</option>
                            </select>
                        </div>

                        <div class="form-group">
                            <label>CNAE (Opcional):</label>
                            <input type="text" name="cnae" placeholder="Ex: 4711-301">
                        </div>

                        <button type="submit" class="btn btn-primary">Salvar Dados do MEI</button>
                    </form>
                </div>

                <!-- INFORMACOES CONTA CARD -->
                <div class="card">
                    <h3>Informações da Conta</h3>
                    <table style="width: 100%; border: none;">
                        <tr>
                            <td style="border: none; padding: 10px 0;"><strong>Cadastrado em:</strong></td>
                            <td style="border: none; padding: 10px 0;">26/01/2026</td>
                        </tr>
                        <tr style="background: #f5f7fa;">
                            <td style="border: none; padding: 10px 0;"><strong>Último acesso:</strong></td>
                            <td style="border: none; padding: 10px 0;">29/01/2026 00:39</td>
                        </tr>
                        <tr>
                            <td style="border: none; padding: 10px 0;"><strong>Status:</strong></td>
                            <td style="border: none; padding: 10px 0;">
                                <span style="padding: 5px 10px; background: #c6f6d5; border-radius: 3px; color: #22543d;">
                                    &#10004; Ativo
                                </span>
                            </td>
                        </tr>
                    </table>
                </div>

            </div>
        </div>
    </div>
</body>
</html>
