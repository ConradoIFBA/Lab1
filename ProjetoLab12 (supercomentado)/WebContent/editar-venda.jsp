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

    Vendas venda = (Vendas) request.getAttribute("venda");
    if (venda == null) {
        response.sendRedirect("historico");
        return;
    }

    @SuppressWarnings("unchecked")
    List<Categoria> categorias = (List<Categoria>) request.getAttribute("categorias");
    if (categorias == null) categorias = new java.util.ArrayList<>();

    String mensagemErro = (String) session.getAttribute("erro");
    session.removeAttribute("erro");

    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
    DecimalFormat dfNum = new DecimalFormat("0.00");

    boolean temNF = "S".equalsIgnoreCase(venda.getNotaFiscalEmitida());
    String numeroNF = "";
    if (temNF && venda.getNotaFiscal() != null) {
        numeroNF = venda.getNotaFiscal().getNumero();
    }
%>

<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Editar Venda - Sistema MEI</title>

    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }

        body {
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
            background-color: #0f172a;
            color: #e2e8f0;
        }

        .main-container { display: flex; min-height: 100vh; }

        /* SIDEBAR */
        .sidebar {
            width: 250px;
            background-color: #1e293b;
            padding: 20px 0;
            position: fixed;
            left: 0;
            top: 0;
            bottom: 0;
            border-right: 1px solid #334155;
        }

        .sidebar-header {
            padding: 0 20px 20px;
            border-bottom: 1px solid #334155;
            margin-bottom: 20px;
        }

        .sidebar-header h1 { font-size: 24px; color: #f1f5f9; }

        .sidebar-menu { list-style: none; padding: 0; }

        .sidebar-menu li { margin-bottom: 5px; }

        .sidebar-menu a {
            display: flex;
            align-items: center;
            padding: 12px 20px;
            color: #94a3b8;
            text-decoration: none;
            gap: 12px;
            transition: all 0.2s;
        }

        .sidebar-menu a:hover { background-color: #334155; color: #f1f5f9; }

        .sidebar-menu a.active { background-color: #3b82f6; color: white; }

        .sidebar-menu .icon { font-size: 20px; width: 24px; text-align: center; }

        /* MAIN CONTENT */
        .main-content {
            flex: 1;
            margin-left: 250px;
            display: flex;
            flex-direction: column;
        }

        .topbar {
            background-color: #1e293b;
            padding: 20px 30px;
            display: flex;
            justify-content: space-between;
            align-items: center;
            border-bottom: 1px solid #334155;
        }

        .topbar-left h2 { font-size: 24px; color: #f1f5f9; }

        .user-info { display: flex; align-items: center; gap: 12px; }

        .user-avatar {
            width: 40px;
            height: 40px;
            border-radius: 50%;
            background: linear-gradient(135deg, #667eea, #764ba2);
            display: flex;
            align-items: center;
            justify-content: center;
            font-weight: 600;
            color: white;
        }

        .user-details .name { font-weight: 600; color: #f1f5f9; font-size: 14px; }

        .user-details .role { font-size: 12px; color: #94a3b8; }

        .content { flex: 1; padding: 30px; max-width: 800px; margin: 0 auto; width: 100%; }

        .card {
            background: #1e293b;
            padding: 30px;
            border-radius: 12px;
            border: 1px solid #334155;
        }

        .card h3 { margin: 0 0 20px 0; color: #f1f5f9; font-size: 20px; }

        .alert {
            padding: 14px 18px;
            border-radius: 8px;
            margin-bottom: 20px;
            display: flex;
            align-items: center;
            gap: 10px;
        }

        .alert-error {
            background: #fee2e2;
            color: #991b1b;
            border: 1px solid #fecaca;
        }

        .form-group { margin-bottom: 20px; }

        .form-group label {
            display: block;
            margin-bottom: 8px;
            font-weight: 600;
            color: #e2e8f0;
            font-size: 14px;
        }

        .form-group input,
        .form-group select,
        .form-group textarea {
            width: 100%;
            padding: 12px 14px;
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

        .form-group textarea { min-height: 80px; resize: vertical; }

        .form-group small {
            display: block;
            margin-top: 6px;
            font-size: 13px;
            color: #64748b;
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
            accent-color: #3b82f6;
        }

        .checkbox-group label {
            cursor: pointer;
            margin: 0;
            font-weight: 500;
            color: #e2e8f0;
        }

        #numeroNFContainer { display: none; }

        #numeroNFContainer.show { display: block; }

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
            background: linear-gradient(135deg, #667eea, #764ba2);
            color: white;
        }

        .btn-primary:hover { transform: translateY(-2px); }

        .btn-secondary {
            background: #334155;
            color: #e2e8f0;
            margin-right: 10px;
        }

        .btn-secondary:hover { background: #475569; }

        .form-actions {
            display: flex;
            gap: 10px;
            margin-top: 30px;
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
            }
        }

        window.onload = function() {
            toggleNumeroNF();
        };
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
                <li><a href="dashboard"><span class="icon">🏠</span><span class="label">Dashboard</span></a></li>
                <li><a href="historico" class="active"><span class="icon">📜</span><span class="label">Histórico</span></a></li>
                <li><a href="relatorio.jsp"><span class="icon">📊</span><span class="label">Relatório</span></a></li>
                <li><a href="perfil"><span class="icon">👤</span><span class="label">Perfil</span></a></li>
                <li><a href="logout"><span class="icon">🚪</span><span class="label">Sair</span></a></li>
            </ul>
        </div>

        <!-- MAIN CONTENT -->
        <div class="main-content">

            <!-- TOPBAR -->
            <div class="topbar">
                <div class="topbar-left">
                    <h2>Editar Venda</h2>
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

                <% if (mensagemErro != null) { %>
                    <div class="alert alert-error">
                        <span>❌</span>
                        <span><%= mensagemErro %></span>
                    </div>
                <% } %>

                <div class="card">
                    <h3>✏️ Editar Venda</h3>

                    <!-- ============================================
                         FORMULÁRIO DE EDIÇÃO
                         ============================================

                         IMPORTANTE: Action aponta para venda?acao=editar
                         Isso faz o VendaController processar a edição

                         Parâmetros enviados:
                         - id (hidden)
                         - categoria
                         - valor
                         - emitirNF ("S" ou null)
                         - numeroNF (se emitirNF marcado)
                         - descricao
                    -->
                    <form method="POST" action="venda?acao=editar">
                        <!-- ID da venda (hidden) -->
                        <input type="hidden" name="id" value="<%= venda.getIdVendas() %>">

                        <!-- Categoria -->
                        <div class="form-group">
                            <label for="categoria">Categoria *</label>
                            <select id="categoria" name="categoria" required>
                                <option value="">Selecione...</option>
                                <% for (Categoria cat : categorias) { %>
                                    <option value="<%= cat.getIdCategoria() %>"
                                            <%= venda.getCategoria() != null && cat.getIdCategoria() == venda.getCategoria().getIdCategoria() ? "selected" : "" %>>
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
                                value="<%= dfNum.format(venda.getValor()) %>"
                                required
                            >
                        </div>

                        <!-- Emitir NF -->
                        <div class="checkbox-group">
                            <input
                                type="checkbox"
                                id="emitirNF"
                                name="emitirNF"
                                value="S"
                                <%= temNF ? "checked" : "" %>
                                onchange="toggleNumeroNF()"
                            >
                            <label for="emitirNF">📋 Emitir Nota Fiscal</label>
                        </div>

                        <!-- Número NF (aparece se checkbox marcado) -->
                        <div id="numeroNFContainer" class="form-group">
                            <label for="numeroNF">Número da Nota Fiscal *</label>
                            <input
                                type="text"
                                id="numeroNF"
                                name="numeroNF"
                                placeholder="Ex: NF-2026001"
                                value="<%= numeroNF %>"
                                <%= !temNF ? "disabled" : "" %>
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
                            ><%= venda.getDescricao() != null ? venda.getDescricao() : "" %></textarea>
                        </div>

                        <!-- Botões -->
                        <div class="form-actions">
                            <a href="historico" class="btn btn-secondary">Cancelar</a>
                            <button type="submit" class="btn btn-primary">Salvar Alterações</button>
                        </div>
                    </form>
                </div>

            </div>
        </div>
    </div>
</body>
</html>
