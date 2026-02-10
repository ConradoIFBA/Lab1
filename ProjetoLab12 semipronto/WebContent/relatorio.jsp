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
    <title>Relatório - Sistema MEI</title>
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
    <script src="js/validations.js"></script>
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
                    <h2>Gerar Relatório</h2>
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

                <!-- GERAR RELATORIO CARD -->
                <div class="card">
                    <h3>Gerar Relatório de Receitas</h3>
                    <p style="color: #666; margin-bottom: 20px;">
                        Selecione o período desejado para gerar o relatório em formato PDF.
                    </p>

                    <form action="relatorio" method="post">
                        <div class="form-row">
                            <div class="form-group">
                                <label>Mês:</label>
                                <select name="mes" required>
                                    <option value="">Selecione um mês...</option>
                                    <option value="1">Janeiro</option>
                                    <option value="2">Fevereiro</option>
                                    <option value="3">Março</option>
                                    <option value="4">Abril</option>
                                    <option value="5">Maio</option>
                                    <option value="6">Junho</option>
                                    <option value="7">Julho</option>
                                    <option value="8">Agosto</option>
                                    <option value="9">Setembro</option>
                                    <option value="10">Outubro</option>
                                    <option value="11">Novembro</option>
                                    <option value="12">Dezembro</option>
                                </select>
                            </div>

                            <div class="form-group">
                                <label>Ano:</label>
                                <select name="ano" required>
                                    <option value="">Selecione um ano...</option>
                                    <option value="2024">2024</option>
                                    <option value="2025">2025</option>
                                    <option value="2026" selected>2026</option>
                                    <option value="2027">2027</option>
                                </select>
                            </div>

                            <div class="form-group">
                                <label>&nbsp;</label>
                                <button type="submit" class="btn btn-primary" style="width: 100%;">
                                    Gerar PDF
                                </button>
                            </div>
                        </div>
                    </form>
                </div>

                <!-- INFORMACOES CARD -->
                <div class="card">
                    <h3>Sobre o Relatório</h3>
                    <p>O relatório gerado contém:</p>
                    <ul style="margin-left: 20px; color: #666;">
                        <li>Receitas por categoria (Revenda, Industrializados, Serviços)</li>
                        <li>Separação entre receitas com e sem nota fiscal</li>
                        <li>Totais parciais por categoria</li>
                        <li>Total geral do período</li>
                        <li>Detalhamento completo de cada venda</li>
                    </ul>
                </div>

                <!-- ULTIMOS RELATORIOS CARD -->
                <div class="card">
                    <h3>Últimos Relatórios Gerados</h3>
                    <div class="table-container">
                        <table>
                            <thead>
                                <tr>
                                    <th>Período</th>
                                    <th>Total</th>
                                    <th>Data de Geração</th>
                                    <th>Ações</th>
                                </tr>
                            </thead>
                            <tbody>
                                <tr>
                                    <td>Janeiro/2026</td>
                                    <td>R$ 1.500,00</td>
                                    <td>29/01/2026 10:30</td>
                                    <td>
                                        <div class="action-buttons">
                                            <button class="btn-small btn-view" type="button">Baixar</button>
                                            <button class="btn-small btn-delete" type="button">Deletar</button>
                                        </div>
                                    </td>
                                </tr>
                                <tr>
                                    <td>Dezembro/2025</td>
                                    <td>R$ 3.200,00</td>
                                    <td>02/01/2026 14:15</td>
                                    <td>
                                        <div class="action-buttons">
                                            <button class="btn-small btn-view" type="button">Baixar</button>
                                            <button class="btn-small btn-delete" type="button">Deletar</button>
                                        </div>
                                    </td>
                                </tr>
                            </tbody>
                        </table>
                    </div>
                </div>

            </div>
        </div>
    </div>
</body>
</html>
