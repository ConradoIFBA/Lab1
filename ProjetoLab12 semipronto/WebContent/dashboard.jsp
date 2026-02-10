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
    int vendasRealizadas = ultimasVendas.size();
    double disponivel = limiteMEI - totalMes;
%>

<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Dashboard - Sistema MEI</title>
    <style>
        /* ============================================
           RESET E BASE
           ============================================ */
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }

        body {
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
            background-color: #0f172a;
            color: #e2e8f0;
            line-height: 1.6;
        }

        /* ============================================
           CONTAINER PRINCIPAL
           ============================================ */
        .main-container {
            display: flex;
            min-height: 100vh;
        }

        /* ============================================
           SIDEBAR
           ============================================ */
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

        /* ============================================
           MAIN CONTENT
           ============================================ */
        .main-content {
            flex: 1;
            margin-left: 250px;
            display: flex;
            flex-direction: column;
            min-height: 100vh;
        }

        /* ============================================
           TOPBAR
           ============================================ */
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

        /* ============================================
           CONTENT
           ============================================ */
        .content {
            flex: 1;
            padding: 30px;
        }

        /* ============================================
           ALERTAS
           ============================================ */
        .alert {
            padding: 16px 20px;
            border-radius: 8px;
            margin-bottom: 20px;
            display: flex;
            align-items: center;
            gap: 12px;
            font-weight: 500;
            transition: opacity 0.3s;
        }

        .alert-success {
            background-color: #10b981;
            color: white;
        }

        .alert-error {
            background-color: #ef4444;
            color: white;
        }

        /* ============================================
           GRID DASHBOARD
           ============================================ */
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

        /* ============================================
           CARDS
           ============================================ */
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

        /* ============================================
           CARD STATS
           ============================================ */
        .stats-card {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
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

        /* ============================================
           FORMULÁRIO
           ============================================ */
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

        .form-group input:disabled {
            background-color: #1e293b;
            cursor: not-allowed;
            opacity: 0.5;
        }

        .form-group textarea {
            resize: vertical;
            min-height: 80px;
        }

        .form-group small {
            display: block;
            margin-top: 6px;
            font-size: 13px;
            color: #64748b;
        }

        /* ============================================
           CHECKBOX GROUP
           ============================================ */
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
            accent-color: #3b82f6;
        }

        .checkbox-group label {
            cursor: pointer;
            margin: 0;
            font-weight: 500;
            color: #e2e8f0;
        }

        /* ============================================
           CAMPO NÚMERO NF (toggle)
           ============================================ */
        #numeroNFContainer {
            display: none;
            margin-bottom: 20px;
        }

        #numeroNFContainer.show {
            display: block;
        }

        /* ============================================
           BOTÕES
           ============================================ */
        .btn {
            padding: 12px 24px;
            border: none;
            border-radius: 8px;
            font-size: 14px;
            font-weight: 600;
            cursor: pointer;
            transition: all 0.2s;
            text-decoration: none;
            display: inline-block;
        }

        .btn-primary {
            background-color: #3b82f6;
            color: white;
        }

        .btn-primary:hover {
            background-color: #2563eb;
            transform: translateY(-2px);
            box-shadow: 0 4px 12px rgba(59, 130, 246, 0.4);
        }

        .btn-block {
            width: 100%;
            display: block;
        }

        /* ============================================
           TABELA
           ============================================ */
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
            letter-spacing: 0.5px;
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

        /* ============================================
           BADGES
           ============================================ */
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

        /* ============================================
           EMPTY STATE
           ============================================ */
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

        .empty-state p {
            margin-bottom: 8px;
        }

        /* ============================================
           SCROLLBAR
           ============================================ */
        ::-webkit-scrollbar {
            width: 8px;
            height: 8px;
        }

        ::-webkit-scrollbar-track {
            background: #1e293b;
        }

        ::-webkit-scrollbar-thumb {
            background: #475569;
            border-radius: 4px;
        }

        ::-webkit-scrollbar-thumb:hover {
            background: #64748b;
        }
    </style>
    <script>
        // Toggle campo Número NF
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

        // Auto-dismiss alerts
        document.addEventListener('DOMContentLoaded', function() {
            const alerts = document.querySelectorAll('.alert');
            alerts.forEach(alert => {
                setTimeout(() => {
                    alert.style.opacity = '0';
                    setTimeout(() => alert.remove(), 300);
                }, 5000);
            });
        });
    </script>
</head>
<body>
    <div class="main-container">

        <!-- SIDEBAR -->
        <div class="sidebar">
            <div class="sidebar-header">
                <h1>📊 MEI</h1>
            </div>
            <ul class="sidebar-menu">
                <li>
                    <a href="dashboard" class="active">
                        <span class="icon">🏠</span>
                        <span class="label">Dashboard</span>
                    </a>
                </li>
                <li>
                    <a href="historico">
                        <span class="icon">📜</span>
                        <span class="label">Histórico</span>
                    </a>
                </li>
                <li>
                    <a href="relatorio.jsp">
                        <span class="icon">📊</span>
                        <span class="label">Relatório</span>
                    </a>
                </li>
                <li>
                    <a href="perfil">
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

        <!-- MAIN CONTENT -->
        <div class="main-content">

            <!-- TOPBAR -->
            <div class="topbar">
                <div class="topbar-left">
                    <h2>Dashboard</h2>
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

            <!-- CONTENT -->
            <div class="content">

                <!-- ALERTAS -->
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

                <!-- GRID PRINCIPAL -->
                <div class="dashboard-grid">

                    <!-- FORMULÁRIO NOVA VENDA -->
                    <div class="card">
                        <h3>➕ Nova Venda</h3>

                        <form method="POST" action="dashboard">

                            <!-- Categoria -->
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

                            <!-- Valor -->
                            <div class="form-group">
                                <label for="valor">Valor (R$) *</label>
                                <input
                                    type="number"
                                    id="valor"
                                    name="valor"
                                    step="0.01"
                                    min="0.01"
                                    placeholder="0.00"
                                    required
                                >
                            </div>

                            <!-- CHECKBOX EMITIR NF -->
                            <div class="checkbox-group">
                                <input
                                    type="checkbox"
                                    id="emitirNF"
                                    name="emitirNF"
                                    value="S"
                                    onchange="toggleNumeroNF()"
                                >
                                <label for="emitirNF">📋 Emitir Nota Fiscal</label>
                            </div>

                            <!-- CAMPO NÚMERO NF (aparece se marcar checkbox) -->
                            <div id="numeroNFContainer" class="form-group">
                                <label for="numeroNF">Número da Nota Fiscal *</label>
                                <input
                                    type="text"
                                    id="numeroNF"
                                    name="numeroNF"
                                    placeholder="Ex: NF-2026001"
                                    disabled
                                >
                                <small>Digite o número da nota fiscal emitida</small>
                            </div>

                            <!-- Descrição -->
                            <div class="form-group">
                                <label for="descricao">Descrição (opcional)</label>
                                <textarea
                                    id="descricao"
                                    name="descricao"
                                    rows="3"
                                    placeholder="Detalhes da venda..."
                                ></textarea>
                            </div>

                            <!-- Botão -->
                            <button type="submit" class="btn btn-primary btn-block">
                                Cadastrar Venda
                            </button>
                        </form>
                    </div>

                    <!-- CARD RESUMO DO MÊS -->
                    <div class="card stats-card">
                        <h3>📈 Resumo do Mês</h3>
                        <p class="subtitle">Suas vendas do mês atual</p>

                        <div class="stats-grid">
                            <div class="stat-item">
                                <div class="stat-label">Total do Mês</div>
                                <div class="stat-value"><%= df.format(totalMes) %></div>
                            </div>

                            <div class="stat-item">
                                <div class="stat-label">Vendas Realizadas</div>
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
                        </div>
                    </div>

                </div>

                <!-- TABELA ÚLTIMAS VENDAS -->
                <div class="card">
                    <h3>📋 Últimas Vendas</h3>

                    <% if (ultimasVendas.isEmpty()) { %>
                        <div class="empty-state">
                            <div class="icon">📭</div>
                            <p>Nenhuma venda cadastrada ainda</p>
                            <p style="font-size: 14px; color: #64748b;">
                                Cadastre sua primeira venda usando o formulário acima
                            </p>
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
                                        <td>
                                            <%= v.getCategoria() != null ? v.getCategoria().getNomeCategoria() : "-" %>
                                        </td>
                                        <td>
                                            <%= v.getDescricao() != null && !v.getDescricao().isEmpty() ?
                                                (v.getDescricao().length() > 30 ?
                                                    v.getDescricao().substring(0, 30) + "..." :
                                                    v.getDescricao()) :
                                                "-" %>
                                        </td>
                                        <td>
                                            <strong><%= df.format(v.getValor()) %></strong>
                                        </td>
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
