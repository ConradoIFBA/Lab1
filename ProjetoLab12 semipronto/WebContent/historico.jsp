<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.text.DecimalFormat" %>
<%@ page import="br.com.projeto.model.Usuario" %>
<%@ page import="br.com.projeto.model.Vendas" %>

<%
    /* ================================================================
       VALIDAÇÃO DE SESSÃO E DADOS
       ================================================================

       1. Verifica se usuário está logado
       2. Busca dados enviados pelo HistoricoController
       3. Define valores padrão se dados não existirem
    */

    // Usuário logado (obrigatório)
    Usuario usuario = (Usuario) session.getAttribute("usuario");
    if (usuario == null) {
        response.sendRedirect("login");
        return;
    }

    // Ano selecionado (padrão: 2026)
    Integer ano = (Integer) request.getAttribute("ano");
    if (ano == null) ano = 2026;

    // Lista de anos disponíveis (para dropdown)
    @SuppressWarnings("unchecked")
    List<Integer> anos = (List<Integer>) request.getAttribute("anos");
    if (anos == null) anos = new java.util.ArrayList<>();

    // Filtro de NF (padrão: "todas")
    String filtroNF = (String) request.getAttribute("filtroNF");
    if (filtroNF == null) filtroNF = "todas";

    // Lista de vendas filtradas
    @SuppressWarnings("unchecked")
    List<Vendas> vendasDetalhadas = (List<Vendas>) request.getAttribute("vendasDetalhadas");
    if (vendasDetalhadas == null) vendasDetalhadas = new java.util.ArrayList<>();

    // Estatísticas (cards no topo)
    Integer totalVendas = (Integer) request.getAttribute("totalVendas");
    if (totalVendas == null) totalVendas = 0;

    Double totalValor = (Double) request.getAttribute("totalValor");
    if (totalValor == null) totalValor = 0.0;

    Integer totalComNF = (Integer) request.getAttribute("totalComNF");
    if (totalComNF == null) totalComNF = 0;

    Integer totalSemNF = (Integer) request.getAttribute("totalSemNF");
    if (totalSemNF == null) totalSemNF = 0;

    Double valorComNF = (Double) request.getAttribute("valorComNF");
    if (valorComNF == null) valorComNF = 0.0;

    Double valorSemNF = (Double) request.getAttribute("valorSemNF");
    if (valorSemNF == null) valorSemNF = 0.0;

    // Formatadores de data e valor
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
    DecimalFormat df = new DecimalFormat("R$ #,##0.00");
%>

<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Histórico - Sistema MEI</title>

    <style>
        /* ================================================================
           CSS INLINE COMPLETO - Não depende de arquivo externo
           ================================================================

           SEÇÕES:
           1. Reset básico
           2. Sidebar (menu lateral)
           3. Main content e topbar
           4. Filtros
           5. Cards de estatísticas
           6. Tabela de vendas
           7. Menu de ações (dropdown)
           8. Modal de confirmação
           9. Responsividade
        */

        /* === 1. RESET === */
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }

        body {
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
            background-color: #0f172a;  /* Fundo escuro */
            color: #e2e8f0;              /* Texto claro */
            line-height: 1.6;
        }

        /* === 2. CONTAINER PRINCIPAL === */
        .main-container {
            display: flex;
            min-height: 100vh;
        }

        /* === 3. SIDEBAR (Menu Lateral Fixo) === */
        .sidebar {
            width: 250px;
            background-color: #1e293b;   /* Cinza escuro */
            padding: 20px 0;
            position: fixed;              /* Fica fixo na tela */
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

        /* Menu de navegação */
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
            color: #94a3b8;              /* Cinza médio */
            text-decoration: none;
            transition: all 0.2s;
            gap: 12px;
        }

        .sidebar-menu a:hover {
            background-color: #334155;   /* Fundo ao passar mouse */
            color: #f1f5f9;
        }

        .sidebar-menu a.active {
            background-color: #3b82f6;   /* Azul para página ativa */
            color: white;
            font-weight: 600;
        }

        .sidebar-menu .icon {
            font-size: 20px;
            width: 24px;
            text-align: center;
        }

        /* === 4. MAIN CONTENT (Área de conteúdo) === */
        .main-content {
            flex: 1;
            margin-left: 250px;          /* Margem = largura sidebar */
            display: flex;
            flex-direction: column;
            min-height: 100vh;
        }

        /* === 5. TOPBAR (Barra superior) === */
        .topbar {
            background-color: #1e293b;
            padding: 20px 30px;
            display: flex;
            justify-content: space-between;
            align-items: center;
            border-bottom: 1px solid #334155;
            position: sticky;            /* Fica fixa ao rolar */
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
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);  /* Gradiente roxo */
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

        /* === 6. CONTENT (Área interna) === */
        .content {
            flex: 1;
            padding: 30px;
        }

        /* === 7. FILTROS === */
        .filtros-container {
            background: #1e293b;
            padding: 25px;
            border-radius: 12px;
            border: 1px solid #334155;
            margin-bottom: 30px;
        }

        .filtros-container h3 {
            margin: 0 0 20px 0;
            color: #f1f5f9;
            font-size: 18px;
        }

        .filtros-grid {
            display: grid;
            grid-template-columns: 1fr 1fr;  /* 2 colunas iguais */
            gap: 20px;
        }

        .form-group label {
            display: block;
            margin-bottom: 8px;
            font-weight: 600;
            color: #e2e8f0;
            font-size: 14px;
        }

        .form-group select {
            width: 100%;
            padding: 12px 14px;
            background-color: #0f172a;
            border: 1px solid #334155;
            border-radius: 8px;
            color: #e2e8f0;
            font-size: 14px;
            transition: all 0.2s;
        }

        .form-group select:focus {
            outline: none;
            border-color: #3b82f6;       /* Borda azul ao focar */
            box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
        }

        /* === 8. CARDS DE ESTATÍSTICAS === */
        .stats-cards {
            display: grid;
            grid-template-columns: repeat(4, 1fr);  /* 4 colunas iguais */
            gap: 20px;
            margin-bottom: 30px;
        }

        .stat-card {
            background: #1e293b;
            padding: 25px;
            border-radius: 12px;
            border: 1px solid #334155;
            transition: all 0.3s ease;
        }

        .stat-card:hover {
            transform: translateY(-4px);      /* Levanta ao passar mouse */
            box-shadow: 0 8px 24px rgba(0, 0, 0, 0.3);
        }

        /* Cards com gradientes coloridos */
        .stat-card.primary {
            background: linear-gradient(135deg, #667eea, #764ba2);  /* Roxo */
            border: none;
        }

        .stat-card.success {
            background: linear-gradient(135deg, #10b981, #059669);  /* Verde */
            border: none;
        }

        .stat-card.warning {
            background: linear-gradient(135deg, #f59e0b, #d97706);  /* Laranja */
            border: none;
        }

        .stat-card h4 {
            font-size: 11px;
            opacity: 0.9;
            margin: 0 0 10px 0;
            font-weight: 500;
            text-transform: uppercase;
            letter-spacing: 1px;
            color: inherit;
        }

        .stat-card .value {
            font-size: 32px;
            font-weight: 700;
            margin: 0;
            line-height: 1;
            color: inherit;
        }

        .stat-card small {
            display: block;
            margin-top: 8px;
            font-size: 14px;
            opacity: 0.95;
        }

        /* === 9. TABELA DE VENDAS === */
        .vendas-detalhadas {
            background: #1e293b;
            padding: 25px;
            border-radius: 12px;
            border: 1px solid #334155;
        }

        .vendas-detalhadas h3 {
            margin: 0 0 20px 0;
            color: #f1f5f9;
            font-size: 18px;
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
            letter-spacing: 0.5px;
            border-bottom: 2px solid #334155;
        }

        td {
            padding: 14px 16px;
            border-bottom: 1px solid #334155;
            color: #e2e8f0;
        }

        tbody tr {
            transition: all 0.2s;
        }

        tbody tr:hover {
            background-color: #0f172a;       /* Fundo ao passar mouse */
        }

        /* === 10. BADGES (Etiquetas S/N) === */
        .badge-nf {
            display: inline-flex;
            align-items: center;
            padding: 5px 12px;
            border-radius: 12px;
            font-size: 11px;
            font-weight: 700;
            text-transform: uppercase;
            letter-spacing: 0.5px;
        }

        .badge-nf.sim {
            background: #10b981;             /* Verde */
            color: white;
        }

        .badge-nf.nao {
            background: #64748b;             /* Cinza */
            color: white;
        }

        /* === 11. MENU DE AÇÕES (3 pontos ⋮) === */
        .actions-menu {
            position: relative;
            display: inline-block;
        }

        .actions-btn {
            background: none;
            border: none;
            color: #94a3b8;
            font-size: 20px;
            cursor: pointer;
            padding: 4px 8px;
            border-radius: 4px;
            transition: all 0.2s;
        }

        .actions-btn:hover {
            background: #334155;
            color: #f1f5f9;
        }

        /* Dropdown que aparece ao clicar */
        .actions-dropdown {
            display: none;                   /* Escondido por padrão */
            position: absolute;
            right: 0;
            top: 100%;
            background: #1e293b;
            border: 1px solid #334155;
            border-radius: 8px;
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
            min-width: 150px;
            z-index: 1000;
            margin-top: 4px;
        }

        .actions-dropdown.show {
            display: block;                  /* Aparece ao clicar */
        }

        .actions-dropdown a,
        .actions-dropdown button {
            display: flex;
            align-items: center;
            gap: 8px;
            padding: 10px 16px;
            color: #e2e8f0;
            text-decoration: none;
            border: none;
            background: none;
            width: 100%;
            text-align: left;
            font-size: 14px;
            cursor: pointer;
            transition: all 0.2s;
        }

        .actions-dropdown a:hover,
        .actions-dropdown button:hover {
            background: #334155;
        }

        .actions-dropdown a:first-child {
            border-radius: 8px 8px 0 0;
        }

        .actions-dropdown button:last-child {
            border-radius: 0 0 8px 8px;
            color: #ef4444;                  /* Vermelho para excluir */
        }

        .actions-dropdown button:last-child:hover {
            background: #7f1d1d;
        }

        /* === 12. MODAL DE CONFIRMAÇÃO === */
        .modal {
            display: none;                   /* Escondido por padrão */
            position: fixed;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            background: rgba(0, 0, 0, 0.7);  /* Overlay escuro */
            z-index: 2000;
            align-items: center;
            justify-content: center;
        }

        .modal.show {
            display: flex;                   /* Aparece ao clicar excluir */
        }

        .modal-content {
            background: #1e293b;
            border: 1px solid #334155;
            border-radius: 12px;
            padding: 30px;
            max-width: 500px;
            width: 90%;
            box-shadow: 0 8px 32px rgba(0, 0, 0, 0.5);
        }

        .modal-header h3 {
            margin: 0 0 10px 0;
            color: #f1f5f9;
            font-size: 20px;
        }

        .modal-body {
            margin: 20px 0;
            color: #94a3b8;
            font-size: 14px;
        }

        .modal-footer {
            display: flex;
            gap: 10px;
            justify-content: flex-end;
        }

        .btn {
            padding: 10px 20px;
            border: none;
            border-radius: 8px;
            font-size: 14px;
            font-weight: 600;
            cursor: pointer;
            transition: all 0.2s;
        }

        .btn-secondary {
            background: #334155;
            color: #e2e8f0;
        }

        .btn-secondary:hover {
            background: #475569;
        }

        .btn-danger {
            background: #ef4444;
            color: white;
        }

        .btn-danger:hover {
            background: #dc2626;
        }

        /* === 13. EMPTY STATE (Sem dados) === */
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

        code {
            background: #0f172a;
            padding: 4px 10px;
            border-radius: 4px;
            font-size: 12px;
            font-family: 'Courier New', monospace;
            color: #94a3b8;
        }

        /* === 14. RESPONSIVIDADE === */
        @media (max-width: 1024px) {
            .stats-cards {
                grid-template-columns: repeat(2, 1fr);  /* 2 colunas em tablets */
            }
        }

        @media (max-width: 768px) {
            .filtros-grid {
                grid-template-columns: 1fr;            /* 1 coluna em mobile */
            }
            .stats-cards {
                grid-template-columns: 1fr;            /* 1 coluna em mobile */
            }
        }
    </style>

    <script>
        /* ================================================================
           JAVASCRIPT - Controle do Menu e Modal
           ================================================================

           FUNÇÕES:
           1. toggleActions(id)      - Abre/fecha dropdown de ações
           2. confirmarExclusao()    - Abre modal de confirmação
           3. fecharModal()          - Fecha modal
           4. excluirVenda()         - Redireciona para exclusão
           5. Event listener         - Fecha dropdown ao clicar fora
        */

        // 1. Toggle dropdown de ações (⋮)
        function toggleActions(id) {
            // Fecha todos os dropdowns abertos (exceto o clicado)
            document.querySelectorAll('.actions-dropdown').forEach(dropdown => {
                if (dropdown.id !== 'dropdown-' + id) {
                    dropdown.classList.remove('show');
                }
            });

            // Toggle o dropdown clicado (abre se fechado, fecha se aberto)
            const dropdown = document.getElementById('dropdown-' + id);
            dropdown.classList.toggle('show');
        }

        // 2. Fecha dropdown ao clicar fora dele
        document.addEventListener('click', function(event) {
            if (!event.target.closest('.actions-menu')) {
                // Clicou fora do menu, fecha todos os dropdowns
                document.querySelectorAll('.actions-dropdown').forEach(dropdown => {
                    dropdown.classList.remove('show');
                });
            }
        });

        // 3. Abre modal de confirmação de exclusão
        function confirmarExclusao(id, descricao) {
            document.getElementById('vendaId').value = id;
            document.getElementById('vendaDescricao').textContent = descricao || 'esta venda';
            document.getElementById('modalExcluir').classList.add('show');
        }

        // 4. Fecha modal
        function fecharModal(modalId) {
            document.getElementById(modalId).classList.remove('show');
        }

        // 5. Confirma exclusão e redireciona
        function excluirVenda() {
            const id = document.getElementById('vendaId').value;
            // Redireciona para controller de exclusão
            window.location.href = 'venda?acao=excluir&id=' + id;
        }
    </script>
</head>
<body>
    <!-- ================================================================
         ESTRUTURA HTML - Layout com Sidebar
         ================================================================

         ORGANIZAÇÃO:
         1. Container principal (.main-container)
            ├── Sidebar (menu lateral fixo)
            └── Main Content
                ├── Topbar (barra superior)
                └── Content
                    ├── Filtros
                    ├── Cards de estatísticas
                    └── Tabela de vendas
         2. Modal de confirmação (fora do container)
    -->

    <div class="main-container">

        <!-- ========== SIDEBAR (Menu Lateral) ========== -->
        <div class="sidebar">
            <div class="sidebar-header">
                <h1>📊 MEI</h1>
            </div>
            <ul class="sidebar-menu">
                <li>
                    <a href="dashboard">
                        <span class="icon">🏠</span>
                        <span class="label">Dashboard</span>
                    </a>
                </li>
                <li>
                    <!-- Histórico está ativo (class="active") -->
                    <a href="historico" class="active">
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

        <!-- ========== MAIN CONTENT ========== -->
        <div class="main-content">

            <!-- ========== TOPBAR (Barra Superior) ========== -->
            <div class="topbar">
                <div class="topbar-left">
                    <h2>Histórico e Notas Fiscais</h2>
                </div>
                <div class="topbar-right">
                    <div class="user-info">
                        <!-- Avatar com inicial do nome -->
                        <div class="user-avatar"><%= usuario.getNome().substring(0,1).toUpperCase() %></div>
                        <div class="user-details">
                            <p class="name"><%= usuario.getNome() %></p>
                            <p class="role">MEI</p>
                        </div>
                    </div>
                </div>
            </div>

            <!-- ========== CONTENT (Área de Conteúdo) ========== -->
            <div class="content">

                <!-- ========== FILTROS ==========
                     Formulário GET que recarrega a página com novos filtros
                     onchange="this.form.submit()" faz submit automático ao mudar select
                -->
                <div class="filtros-container">
                    <h3>🔍 Filtros</h3>
                    <form method="GET" action="historico">
                        <div class="filtros-grid">
                            <!-- FILTRO 1: Ano -->
                            <div class="form-group">
                                <label for="ano">Ano</label>
                                <select id="ano" name="ano" onchange="this.form.submit()">
                                    <%
                                    // Se não veio lista de anos, cria uma padrão
                                    if (anos.isEmpty()) {
                                        anos.add(2024);
                                        anos.add(2025);
                                        anos.add(2026);
                                    }
                                    // Gera options dinamicamente
                                    for (Integer a : anos) {
                                    %>
                                        <option value="<%= a %>" <%= a.equals(ano) ? "selected" : "" %>>
                                            <%= a %>
                                        </option>
                                    <% } %>
                                </select>
                            </div>

                            <!-- FILTRO 2: Nota Fiscal -->
                            <div class="form-group">
                                <label for="filtroNF">📋 Filtro de Notas Fiscais</label>
                                <select id="filtroNF" name="filtroNF" onchange="this.form.submit()">
                                    <option value="todas" <%= "todas".equals(filtroNF) ? "selected" : "" %>>
                                        📋 Todas as vendas
                                    </option>
                                    <option value="comNF" <%= "comNF".equals(filtroNF) ? "selected" : "" %>>
                                        ✅ Apenas com NF
                                    </option>
                                    <option value="semNF" <%= "semNF".equals(filtroNF) ? "selected" : "" %>>
                                        ❌ Sem NF
                                    </option>
                                </select>
                            </div>
                        </div>
                    </form>
                </div>

                <!-- ========== CARDS DE ESTATÍSTICAS ==========
                     4 cards mostrando resumo dos dados filtrados
                -->
                <div class="stats-cards">
                    <!-- CARD 1: Total de Vendas (roxo) -->
                    <div class="stat-card primary">
                        <h4>Total de Vendas</h4>
                        <div class="value"><%= totalVendas %></div>
                    </div>

                    <!-- CARD 2: Com NF (verde) -->
                    <div class="stat-card success">
                        <h4>✅ Com NF</h4>
                        <div class="value"><%= totalComNF %></div>
                        <small><%= df.format(valorComNF) %></small>
                    </div>

                    <!-- CARD 3: Sem NF (laranja) -->
                    <div class="stat-card warning">
                        <h4>❌ Sem NF</h4>
                        <div class="value"><%= totalSemNF %></div>
                        <small><%= df.format(valorSemNF) %></small>
                    </div>

                    <!-- CARD 4: Valor Total (cinza) -->
                    <div class="stat-card">
                        <h4 style="color: #94a3b8;">Valor Total</h4>
                        <div class="value" style="font-size: 24px; color: #e2e8f0;"><%= df.format(totalValor) %></div>
                    </div>
                </div>

                <!-- ========== TABELA DE VENDAS ========== -->
                <div class="vendas-detalhadas">
                    <h3>
                        📋 Vendas Detalhadas
                        <!-- Mostra filtro ativo ao lado do título -->
                        <% if ("comNF".equals(filtroNF)) { %>
                            <span style="color: #10b981; font-size: 15px; font-weight: 500;">(Apenas com NF)</span>
                        <% } else if ("semNF".equals(filtroNF)) { %>
                            <span style="color: #ef4444; font-size: 15px; font-weight: 500;">(Sem NF)</span>
                        <% } %>
                    </h3>

                    <!-- SE NÃO TEM VENDAS: mostra empty state -->
                    <% if (vendasDetalhadas.isEmpty()) { %>
                        <div class="empty-state">
                            <div class="icon">📭</div>
                            <p style="font-size: 16px; margin-bottom: 10px;">Nenhuma venda encontrada</p>
                            <p style="font-size: 14px; color: #64748b;">Tente ajustar os filtros ou cadastrar uma nova venda.</p>
                        </div>

                    <!-- SE TEM VENDAS: mostra tabela -->
                    <% } else { %>
                        <table>
                            <thead>
                                <tr>
                                    <th>Data</th>
                                    <th>Categoria</th>
                                    <th>Descrição</th>
                                    <th>Valor</th>
                                    <th>NF</th>
                                    <th>Número</th>
                                    <th style="text-align: center;">Ações</th>  <!-- NOVA COLUNA -->
                                </tr>
                            </thead>
                            <tbody>
                                <!-- Loop em cada venda -->
                                <% for (Vendas v : vendasDetalhadas) { %>
                                    <tr>
                                        <!-- COLUNA 1: Data -->
                                        <td>
                                            <%
                                            try {
                                                // Tenta formatar data (compatível com Date e LocalDateTime)
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

                                        <!-- COLUNA 2: Categoria -->
                                        <td><%= v.getNomeCategoria() != null ? v.getNomeCategoria() : "-" %></td>

                                        <!-- COLUNA 3: Descrição -->
                                        <td><%= v.getDescricao() != null && !v.getDescricao().isEmpty() ? v.getDescricao() : "-" %></td>

                                        <!-- COLUNA 4: Valor (negrito) -->
                                        <td><strong><%= df.format(v.getValor()) %></strong></td>

                                        <!-- COLUNA 5: NF (badge verde/cinza) -->
                                        <td>
                                            <% if ("S".equalsIgnoreCase(v.getNotaFiscalEmitida())) { %>
                                                <span class="badge-nf sim">✅ Sim</span>
                                            <% } else { %>
                                                <span class="badge-nf nao">❌ Não</span>
                                            <% } %>
                                        </td>

                                        <!-- COLUNA 6: Número NF (código monospace) -->
                                        <td>
                                            <%
                                            if (v.getNotaFiscal() != null &&
                                                v.getNotaFiscal().getNumero() != null &&
                                                !v.getNotaFiscal().getNumero().isEmpty()) {
                                            %>
                                                <code><%= v.getNotaFiscal().getNumero() %></code>
                                            <% } else { %>
                                                <span style="color: #64748b;">-</span>
                                            <% } %>
                                        </td>

                                        <!-- COLUNA 7: Ações (⋮ menu) -->
                                        <td style="text-align: center;">
                                            <!-- MENU DE AÇÕES -->
                                            <div class="actions-menu">
                                                <!-- Botão ⋮ -->
                                                <button class="actions-btn" onclick="toggleActions(<%= v.getIdVendas() %>)">
                                                    ⋮
                                                </button>

                                                <!-- Dropdown (escondido por padrão) -->
                                                <div class="actions-dropdown" id="dropdown-<%= v.getIdVendas() %>">
                                                    <!-- Opção 1: Editar -->
                                                    <a href="venda?acao=editar&id=<%= v.getIdVendas() %>">
                                                        ✏️ Editar
                                                    </a>

                                                    <!-- Opção 2: Excluir (abre modal) -->
                                                    <button onclick="confirmarExclusao(<%= v.getIdVendas() %>, '<%= v.getDescricao() != null ? v.getDescricao().replace("'", "\\'") : "esta venda" %>')">
                                                        🗑️ Excluir
                                                    </button>
                                                </div>
                                            </div>
                                        </td>
                                    </tr>
                                <% } %>
                            </tbody>
                        </table>

                        <!-- Contador de vendas -->
                        <p style="margin-top: 20px; text-align: center; color: #64748b; font-size: 13px;">
                            Mostrando <%= vendasDetalhadas.size() %>
                            <%= vendasDetalhadas.size() == 1 ? "venda" : "vendas" %>
                            de <%= ano %>
                        </p>
                    <% } %>
                </div>

            </div>
        </div>
    </div>

    <!-- ========== MODAL DE CONFIRMAÇÃO DE EXCLUSÃO ==========
         Aparece ao clicar "🗑️ Excluir" no menu de ações
    -->
    <div class="modal" id="modalExcluir">
        <div class="modal-content">
            <div class="modal-header">
                <h3>🗑️ Confirmar Exclusão</h3>
            </div>
            <div class="modal-body">
                <p>Tem certeza que deseja excluir <strong id="vendaDescricao"></strong>?</p>
                <p style="margin-top: 10px; color: #ef4444;">Esta ação não pode ser desfeita.</p>
                <!-- Campo hidden com ID da venda a excluir -->
                <input type="hidden" id="vendaId">
            </div>
            <div class="modal-footer">
                <!-- Botão Cancelar -->
                <button class="btn btn-secondary" onclick="fecharModal('modalExcluir')">
                    Cancelar
                </button>
                <!-- Botão Excluir (vermelho) -->
                <button class="btn btn-danger" onclick="excluirVenda()">
                    Excluir
                </button>
            </div>
        </div>
    </div>

</body>
</html>

<!-- ================================================================
     RESUMO DO ARQUIVO
     ================================================================

     ESTRUTURA:
     1. Validação de sessão e dados (linhas 8-56)
     2. CSS inline completo (linhas 65-442)
     3. JavaScript (linhas 444-478)
     4. HTML (linhas 480-750)

     FUNCIONALIDADES:
     ✅ Filtros dinâmicos (ano + NF)
     ✅ 4 cards de estatísticas (total, com NF, sem NF, valor)
     ✅ Tabela responsiva com todas as vendas
     ✅ Menu de ações (⋮) em cada linha
     ✅ Dropdown com Editar e Excluir
     ✅ Modal de confirmação ao excluir
     ✅ Exclusão lógica (preserva histórico)
     ✅ CSS inline (não depende de arquivo externo)
     ✅ Design escuro moderno
     ✅ Responsivo (mobile, tablet, desktop)

     ROTAS USADAS:
     - GET  /historico?ano=X&filtroNF=Y  → Busca vendas filtradas
     - GET  /venda?acao=editar&id=123    → Edita venda
     - GET  /venda?acao=excluir&id=123   → Exclui venda

     TECNOLOGIAS:
     - JSP (Java Server Pages)
     - CSS inline (Flexbox + Grid)
     - JavaScript Vanilla (sem bibliotecas)
     - Bootstrap-like design (sem dependência)
     ================================================================ -->
