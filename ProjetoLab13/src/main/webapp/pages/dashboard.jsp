<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.text.DecimalFormat" %>
<%@ page import="br.com.projeto.model.Usuario" %>
<%@ page import="br.com.projeto.model.Vendas" %>
<%@ page import="br.com.projeto.model.Categoria" %>
<%--
    ================================================================
    DASHBOARD JSP - Painel Principal Sistema MEI
    ================================================================
    VERSÃO: 3.0 - Com 6 cards (mês + ano)
    
    FUNCIONALIDADES:
    1. Cards estatísticas 3x2 (Total Mês, Vendas Mês, Limite, Disponível, Total Ano, Vendas Ano)
    2. Formulário inline cadastro rápido de venda
    3. Tabela últimas 10 vendas cadastradas
    4. Navegação sidebar + topbar sticky

    DADOS DO CONTROLLER (DashboardController):
    - categorias: List<Categoria> - Para dropdown do formulário
    - ultimasVendas: List<Vendas> - Últimas 10 vendas
    - totalMes: Double - Soma vendas do mês atual
    - totalVendas: Integer - Quantidade vendas do mês
    - totalAno: Double - Soma vendas do ano ⬅️ NOVO!
    - vendasAno: Integer - Quantidade vendas do ano ⬅️ NOVO!
--%>

<%
    // ========== VALIDAÇÃO DE SESSÃO ==========
    Usuario usuario = (Usuario) session.getAttribute("usuario");
    if (usuario == null) {
        response.sendRedirect("login");
        return;
    }

    // ========== DADOS DO CONTROLLER ==========
    @SuppressWarnings("unchecked")
    List<Categoria> categorias = (List<Categoria>) request.getAttribute("categorias");
    if (categorias == null) categorias = new java.util.ArrayList<>();

    @SuppressWarnings("unchecked")
    List<Vendas> ultimasVendas = (List<Vendas>) request.getAttribute("ultimasVendas");
    if (ultimasVendas == null) ultimasVendas = new java.util.ArrayList<>();

    Double totalMes = (Double) request.getAttribute("totalMes");
    if (totalMes == null) totalMes = 0.0;

    // ========== MENSAGENS ==========
    String mensagemSucesso = (String) session.getAttribute("sucesso");
    String mensagemErro = (String) session.getAttribute("erro");
    session.removeAttribute("sucesso");
    session.removeAttribute("erro");

    // ========== FORMATADORES ==========
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
    DecimalFormat df = new DecimalFormat("R$ #,##0.00");

    // ========== CÁLCULOS DO MEI ==========
    double limiteMEI = 81000.00;
    
    // Dados do mês
    Integer totalVendasAttr = (Integer) request.getAttribute("totalVendas");
    int vendasRealizadas = (totalVendasAttr != null) ? totalVendasAttr : 0;
    
    // Dados do ano (NOVOS)
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
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
            background-color: #0f172a;
            color: #e2e8f0;
            line-height: 1.6;
        }

        .main-container {
            display: flex;
            min-height: 100vh;
        }

        /* SIDEBAR */
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

        /* MAIN CONTENT */
        .main-content {
            flex: 1;
            margin-left: 250px;
            display: flex;
            flex-direction: column;
            min-height: 100vh;
        }

        /* TOPBAR */
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

        /* CONTENT */
        .content {
            flex: 1;
            padding: 30px;
        }

        /* ALERTAS */
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

        /* GRID DASHBOARD */
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

        /* CARDS */
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

        /* CARD STATS - NOVO GRID 3x2 */
        .stats-card {
            background-color: #1e293b;
            border: 1px solid #334155;
        }

        .stats-grid-6 {
            display: grid;
            grid-template-columns: repeat(3, 1fr);
            gap: 20px;
            margin-top: 20px;
        }

        .stat-card {
            text-align: center;
            padding: 20px;
            border-radius: 10px;
            color: white;
        }

        .stat-card-title {
            font-size: 14px;
            opacity: 0.9;
            margin-bottom: 10px;
        }

        .stat-card-value {
            font-size: 24px;
            font-weight: bold;
        }

        .stat-card-subtitle {
            font-size: 11px;
            margin-top: 5px;
            opacity: 0.9;
        }

        /* Cores dos cards */
        .stat-card-purple {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        }

        .stat-card-pink {
            background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
        }

        .stat-card-cyan {
            background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
        }

        .stat-card-green {
            background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
        }

        .stat-card-red {
            background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
        }

        .stat-card-blue {
            background: linear-gradient(135deg, #a8edea 0%, #fed6e3 100%);
            color: #333;
        }

        .stat-card-blue .stat-card-value {
            color: #667eea;
        }

        .stat-card-orange {
            background: linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%);
            color: #333;
        }

        .stat-card-orange .stat-card-value {
            color: #f5576c;
        }

        @media (max-width: 768px) {
            .stats-grid-6 {
                grid-template-columns: repeat(2, 1fr);
            }
        }

        /* FORMULÁRIO */
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

        /* CHECKBOX */
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

        #numeroNFContainer {
            display: none;
            margin-bottom: 20px;
        }

        #numeroNFContainer.show {
            display: block;
        }

        /* BOTÕES */
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

        /* TABELA */
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

        /* BADGES */
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

        /* EMPTY STATE */
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

        /* SCROLLBAR */
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

                        <form method="POST" action="${pageContext.request.contextPath}/dashboard">

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

                            <!-- CAMPO NÚMERO NF -->
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

                    <!-- CARD RESUMO - 6 CARDS EM GRID 3x2 -->
                    <div class="card stats-card">
                        <h3>📊 Resumo Financeiro</h3>
                        
                        <!-- GRID 3x2 COM 6 CARDS -->
                        <div class="stats-grid-6">
                            
                            <!-- CARD 1: Total do Mês -->
                            <div class="stat-card stat-card-purple">
                                <div class="stat-card-title">💰 Total do Mês</div>
                                <div class="stat-card-value"><%= df.format(totalMes) %></div>
                            </div>
                            
                            <!-- CARD 2: Vendas do Mês -->
                            <div class="stat-card stat-card-pink">
                                <div class="stat-card-title">🛒 Vendas do Mês</div>
                                <div class="stat-card-value"><%= vendasRealizadas %></div>
                            </div>
                            
                            <!-- CARD 3: Limite MEI -->
                            <div class="stat-card stat-card-cyan">
                                <div class="stat-card-title">📋 Limite MEI</div>
                                <div class="stat-card-value"><%= df.format(limiteMEI) %></div>
                            </div>
                            
                            <!-- CARD 4: Disponível -->
                            <div class="stat-card <%= disponivel >= 0 ? "stat-card-green" : "stat-card-red" %>">
                                <div class="stat-card-title">💵 Disponível</div>
                                <div class="stat-card-value"><%= df.format(disponivel) %></div>
                                <% if (disponivel < 0) { %>
                                    <div class="stat-card-subtitle">⚠️ Limite ultrapassado!</div>
                                <% } %>
                            </div>
                            
                            <!-- CARD 5: Total do Ano -->
                            <div class="stat-card stat-card-blue">
                                <div class="stat-card-title">📅 Total do Ano</div>
                                <div class="stat-card-value"><%= df.format(totalAno) %></div>
                            </div>
                            
                            <!-- CARD 6: Vendas do Ano -->
                            <div class="stat-card stat-card-orange">
                                <div class="stat-card-title">📦 Vendas do Ano</div>
                                <div class="stat-card-value"><%= vendasAno %></div>
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
