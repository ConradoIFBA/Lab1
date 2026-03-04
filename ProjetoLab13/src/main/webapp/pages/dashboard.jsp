<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.text.DecimalFormat" %>
<%@ page import="br.com.projeto.model.Usuario" %>
<%@ page import="br.com.projeto.model.Vendas" %>
<%@ page import="br.com.projeto.model.Categoria" %>

<%
    Usuario usuario = (Usuario) session.getAttribute("usuario");
    if (usuario == null) {
        response.sendRedirect("login");
        return;
    }

    @SuppressWarnings("unchecked")
    List<Categoria> categorias = (List<Categoria>) request.getAttribute("categorias");
    if (categorias == null) categorias = new java.util.ArrayList<>();

    @SuppressWarnings("unchecked")
    List<Vendas> ultimasVendas = (List<Vendas>) request.getAttribute("ultimasVendas");
    if (ultimasVendas == null) ultimasVendas = new java.util.ArrayList<>();

    Double totalMes = (Double) request.getAttribute("totalMes");
    if (totalMes == null) totalMes = 0.0;

    String mensagemSucesso = (String) session.getAttribute("sucesso");
    String mensagemErro = (String) session.getAttribute("erro");
    session.removeAttribute("sucesso");
    session.removeAttribute("erro");

    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
    DecimalFormat df = new DecimalFormat("R$ #,##0.00");

    double limiteMEI = 81000.00;
    
    Integer totalVendasAttr = (Integer) request.getAttribute("totalVendas");
    int vendasRealizadas = (totalVendasAttr != null) ? totalVendasAttr : 0;
    
    Integer vendasAnoAttr = (Integer) request.getAttribute("vendasAno");
    Double totalAnoAttr = (Double) request.getAttribute("totalAno");
    
    int vendasAno = (vendasAnoAttr != null) ? vendasAnoAttr : 0;
    double totalAno = (totalAnoAttr != null) ? totalAnoAttr : 0.0;
    
    double disponivel = limiteMEI - totalMes;
%>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Dashboard - Sistema MEI</title>
    <style>
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

        .main-container {
            display: flex;
            min-height: 100vh;
        }

        .sidebar {
            width: 250px;
            background-color: #1e293b;
            padding: 20px 0;
            position: fixed;
            left: 0;
            top: 0;
            bottom: 0;
            overflow-y: auto;
            z-index: 1000;
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
            padding: 0;
        }

        .sidebar-menu li {
            margin-bottom: 5px;
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
            text-align: center;
        }

        .main-content {
            flex: 1;
            margin-left: 250px;
            display: flex;
            flex-direction: column;
            min-height: 100vh;
        }

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

        .topbar-left {
            display: flex;
            align-items: center;
            gap: 12px;
        }

        .topbar-left h2 {
            font-size: 24px;
            font-weight: 600;
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
            font-size: 16px;
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

        .content {
            flex: 1;
            padding: 30px;
        }

        .alert {
            padding: 16px 20px;
            border-radius: 8px;
            margin-bottom: 20px;
            display: flex;
            align-items: center;
            gap: 12px;
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

        .dashboard-grid {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 30px;
            margin-bottom: 30px;
        }

        @media (max-width: 1024px) {
            .dashboard-grid {
                grid-template-columns: 1fr;
            }
        }

        .card {
            background-color: #1e293b;
            border-radius: 12px;
            padding: 24px;
            border: 1px solid #334155;
        }

        .card h3 {
            font-size: 20px;
            font-weight: 600;
            color: #f1f5f9;
            margin-bottom: 8px;
        }

        .subtitle {
            color: #94a3b8;
            font-size: 14px;
            margin-bottom: 20px;
        }

        /* NÃO definir gradiente aqui - deixar para o corToggle */
        .stats-card {
            color: white;
            border: none;
        }

        .stats-grid {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 20px;
        }

        .stat-item {
            background-color: rgba(255, 255, 255, 0.1);
            padding: 16px;
            border-radius: 8px;
        }

        .stat-label {
            font-size: 14px;
            opacity: 0.9;
            margin-bottom: 8px;
        }

        .stat-value {
            font-size: 24px;
            font-weight: 700;
        }

        .text-danger {
            color: #fca5a5 !important;
        }

        .form-group {
            margin-bottom: 20px;
        }

        .form-group label {
            display: block;
            margin-bottom: 8px;
            font-weight: 500;
            color: #e2e8f0;
            font-size: 14px;
        }

        .form-group input,
        .form-group select,
        .form-group textarea {
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
        .form-group select:focus,
        .form-group textarea:focus {
            outline: none;
            border-color: #3b82f6;
            box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
        }

        .form-group textarea {
            resize: vertical;
            min-height: 80px;
        }

        .checkbox-group {
            display: flex;
            align-items: center;
            gap: 10px;
            margin-bottom: 20px;
            padding: 12px;
            background-color: #0f172a;
            border-radius: 8px;
            border: 1px solid #334155;
        }

        .checkbox-group input[type="checkbox"] {
            width: 20px;
            height: 20px;
            cursor: pointer;
        }

        #numeroNFContainer {
            display: none;
        }

        #numeroNFContainer.show {
            display: block;
        }

        .btn {
            padding: 12px 24px;
            border: none;
            border-radius: 8px;
            font-size: 14px;
            font-weight: 600;
            cursor: pointer;
            transition: all 0.2s;
        }

        .btn-primary {
            background-color: #3b82f6;
            color: white;
        }

        .btn-primary:hover {
            background-color: #2563eb;
        }

        .btn-block {
            width: 100%;
        }

        table {
            width: 100%;
            border-collapse: collapse;
        }

        thead {
            background-color: #0f172a;
        }

        th {
            padding: 14px 16px;
            text-align: left;
            font-weight: 600;
            font-size: 13px;
            color: #94a3b8;
            text-transform: uppercase;
            border-bottom: 2px solid #334155;
        }

        td {
            padding: 14px 16px;
            border-bottom: 1px solid #334155;
            color: #e2e8f0;
        }

        tr:hover {
            background-color: #0f172a;
        }

        .badge {
            padding: 4px 12px;
            border-radius: 12px;
            font-size: 12px;
            font-weight: 600;
            display: inline-block;
        }

        .badge-success {
            background-color: #10b981;
            color: white;
        }

        .badge-secondary {
            background-color: #64748b;
            color: white;
        }

        .empty-state {
            text-align: center;
            padding: 60px 20px;
            color: #64748b;
        }

        .empty-state .icon {
            font-size: 64px;
            margin-bottom: 16px;
            opacity: 0.5;
        }
    </style>
    <script>
        function toggleNumeroNF() {
            const checkbox = document.getElementById('emitirNF');
            const container = document.getElementById('numeroNFContainer');
            const input = document.getElementById('numeroNF');

            if (checkbox.checked) {
                container.classList.add('show');
                input.required = true;
                input.disabled = false;
            } else {
                container.classList.remove('show');
                input.required = false;
                input.disabled = true;
                input.value = '';
            }
        }
    </script>
    
    <!-- Modo de cor -->
    <%@ include file="corToggle.jsp" %>
</head>
<body>
    <div class="main-container">
        <div class="sidebar">
            <div class="sidebar-header">
                <h1>📊 MEI</h1>
            </div>
            <ul class="sidebar-menu">
                <li>
                    <a href="${pageContext.request.contextPath}/dashboard" class="active">
                        <span class="icon">🏠</span>
                        <span class="label">Dashboard</span>
                    </a>
                </li>
                <li>
                    <a href="${pageContext.request.contextPath}/historico">
                        <span class="icon">📜</span>
                        <span class="label">Histórico</span>
                    </a>
                </li>
                <li>
                    <a href="${pageContext.request.contextPath}/relatorio">
                        <span class="icon">📊</span>
                        <span class="label">Relatório</span>
                    </a>
                </li>
                <li>
                    <a href="${pageContext.request.contextPath}/perfil">
                        <span class="icon">👤</span>
                        <span class="label">Perfil</span>
                    </a>
                </li>
                <li>
                    <a href="${pageContext.request.contextPath}/logout">
                        <span class="icon">🚪</span>
                        <span class="label">Sair</span>
                    </a>
                </li>
            </ul>
        </div>

        <div class="main-content">
            <div class="topbar">
                <div class="topbar-left">
                    <h2>Dashboard</h2>
                    <button class="theme-toggle-btn" onclick="toggleTheme()" title="Alternar tema">
                        <span id="theme-icon">L</span>
                    </button>
                </div>
                <div class="topbar-right">
                    <div class="user-info">
                        <div class="user-avatar"><%= usuario.getNome().substring(0,1).toUpperCase() %></div>
                        <div class="user-details">
                            <p class="name"><%= usuario.getNome() %></p>
                            <p class="role">MEI</p>
                        </div>
                    </div>
                </div>
            </div>

            <div class="content">
                <% if (mensagemSucesso != null) { %>
                    <div class="alert alert-success">
                        <span>✅</span>
                        <span><%= mensagemSucesso %></span>
                    </div>
                <% } %>

                <% if (mensagemErro != null) { %>
                    <div class="alert alert-error">
                        <span>❌</span>
                        <span><%= mensagemErro %></span>
                    </div>
                <% } %>

                <div class="dashboard-grid">
                    <div class="card">
                        <h3>➕ Nova Venda</h3>
                        <form method="POST" action="${pageContext.request.contextPath}/dashboard">
                            <div class="form-group">
                                <label for="categoria">Categoria *</label>
                                <select id="categoria" name="categoria" required>
                                    <option value="">Selecione...</option>
                                    <% for (Categoria cat : categorias) { %>
                                        <option value="<%= cat.getIdCategoria() %>">
                                            <%= cat.getNomeCategoria() %>
                                        </option>
                                    <% } %>
                                </select>
                            </div>

                            <div class="form-group">
                                <label for="valor">Valor (R$) *</label>
                                <input type="number" id="valor" name="valor" step="0.01" min="0.01" placeholder="0.00" required>
                            </div>

                            <div class="checkbox-group">
                                <input type="checkbox" id="emitirNF" name="emitirNF" value="S" onchange="toggleNumeroNF()">
                                <label for="emitirNF">📋 Emitir Nota Fiscal</label>
                            </div>

                            <div id="numeroNFContainer" class="form-group">
                                <label for="numeroNF">Número da Nota Fiscal *</label>
                                <input type="text" id="numeroNF" name="numeroNF" placeholder="Ex: NF-2026001" disabled>
                            </div>

                            <div class="form-group">
                                <label for="descricao">Descrição (opcional)</label>
                                <textarea id="descricao" name="descricao" rows="3" placeholder="Detalhes da venda..."></textarea>
                            </div>

                            <button type="submit" class="btn btn-primary btn-block">Cadastrar Venda</button>
                        </form>
                    </div>

                    <!-- CARD COM 6 ESTATÍSTICAS -->
                    <div class="card stats-card">
                        <h3>📈 Resumo Financeiro</h3>
                        <p class="subtitle">Suas vendas do mês e ano atual</p>

                        <div class="stats-grid">
                            <div class="stat-item">
                                <div class="stat-label">Total do Mês</div>
                                <div class="stat-value"><%= df.format(totalMes) %></div>
                            </div>

                            <div class="stat-item">
                                <div class="stat-label">Vendas do Mês</div>
                                <div class="stat-value"><%= vendasRealizadas %></div>
                            </div>

                            <div class="stat-item">
                                <div class="stat-label">Limite MEI</div>
                                <div class="stat-value">R$ 81.000</div>
                            </div>

                            <div class="stat-item">
                                <div class="stat-label">Disponível</div>
                                <div class="stat-value <%= disponivel < 0 ? "text-danger" : "" %>">
                                    <%= disponivel < 0 ? "-" : "" %><%= df.format(Math.abs(disponivel)) %>
                                </div>
                            </div>

                            <div class="stat-item">
                                <div class="stat-label">Total do Ano</div>
                                <div class="stat-value"><%= df.format(totalAno) %></div>
                            </div>

                            <div class="stat-item">
                                <div class="stat-label">Vendas do Ano</div>
                                <div class="stat-value"><%= vendasAno %></div>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="card">
                    <h3>📋 Últimas Vendas</h3>
                    <% if (ultimasVendas.isEmpty()) { %>
                        <div class="empty-state">
                            <div class="icon">📭</div>
                            <p>Nenhuma venda cadastrada ainda</p>
                        </div>
                    <% } else { %>
                        <table>
                            <thead>
                                <tr>
                                    <th>Data</th>
                                    <th>Categoria</th>
                                    <th>Descrição</th>
                                    <th>Valor</th>
                                    <th>NF</th>
                                </tr>
                            </thead>
                            <tbody>
                                <% for (Vendas v : ultimasVendas) { %>
                                    <tr>
                                        <td>
                                            <%
                                            try {
                                                if (v.getDataVendas() instanceof java.util.Date) {
                                                    out.print(sdf.format((java.util.Date)v.getDataVendas()));
                                                } else {
                                                    out.print(v.getDataVendasAsLocalDateTime().toLocalDate().toString());
                                                }
                                            } catch (Exception e) {
                                                out.print("-");
                                            }
                                            %>
                                        </td>
                                        <td><%= v.getCategoria() != null ? v.getCategoria().getNomeCategoria() : "-" %></td>
                                        <td>
                                            <%= v.getDescricao() != null && !v.getDescricao().isEmpty() ?
                                                (v.getDescricao().length() > 30 ?
                                                    v.getDescricao().substring(0, 30) + "..." :
                                                    v.getDescricao()) :
                                                "-" %>
                                        </td>
                                        <td><strong><%= df.format(v.getValor()) %></strong></td>
                                        <td>
                                            <% if ("S".equalsIgnoreCase(v.getNotaFiscalEmitida())) { %>
                                                <span class="badge badge-success">S</span>
                                            <% } else { %>
                                                <span class="badge badge-secondary">N</span>
                                            <% } %>
                                        </td>
                                    </tr>
                                <% } %>
                            </tbody>
                        </table>
                    <% } %>
                </div>
            </div>
        </div>
    </div>
</body>
</html>
