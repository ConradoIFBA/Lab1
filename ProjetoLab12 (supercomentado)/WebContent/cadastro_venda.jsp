<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="br.com.projeto.model.Usuario" %>
<%@ page import="br.com.projeto.model.Categoria" %>

<%
    // Obter dados que o Controller enviou
    Usuario usuario = (Usuario) session.getAttribute("usuario");
    @SuppressWarnings("unchecked")
    List<Categoria> categorias = (List<Categoria>) request.getAttribute("categorias");

    // Se não está logado, redireciona
    if (usuario == null) {
        response.sendRedirect("login");
        return;
    }

    // Se categorias é null, inicializa vazia
    if (categorias == null) {
        categorias = new java.util.ArrayList<>();
    }

    // Obter mensagens de erro/sucesso
    String erro = (String) session.getAttribute("erro");
    String sucesso = (String) session.getAttribute("sucesso");
    if (erro != null) session.removeAttribute("erro");
    if (sucesso != null) session.removeAttribute("sucesso");
%>

<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Cadastrar Venda - Sistema MEI</title>
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }

        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background: #f5f5f5;
        }

        .container {
            display: flex;
            min-height: 100vh;
        }

        .sidebar {
            width: 250px;
            background: #2c3e50;
            color: white;
            padding: 20px;
            box-shadow: 2px 0 5px rgba(0,0,0,0.1);
            position: fixed;
            height: 100vh;
            left: 0;
            top: 0;
            overflow-y: auto;
        }

        .sidebar h2 {
            margin-bottom: 30px;
            color: #667eea;
            font-size: 24px;
        }

        .sidebar nav {
            display: flex;
            flex-direction: column;
            gap: 10px;
        }

        .sidebar nav a {
            color: white;
            text-decoration: none;
            padding: 12px;
            border-radius: 4px;
            transition: background 0.3s;
            display: block;
        }

        .sidebar nav a:hover,
        .sidebar nav a.active {
            background: #667eea;
        }

        .main-content {
            flex: 1;
            margin-left: 250px;
            display: flex;
            flex-direction: column;
        }

        .topbar {
            background: white;
            padding: 15px 30px;
            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
            text-align: right;
            color: #333;
        }

        .content {
            flex: 1;
            padding: 30px;
            overflow-y: auto;
        }

        .page-title {
            font-size: 28px;
            font-weight: bold;
            margin-bottom: 30px;
            color: #333;
        }

        .form-container {
            background: white;
            padding: 30px;
            border-radius: 8px;
            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
            max-width: 600px;
        }

        .form-group {
            margin-bottom: 20px;
        }

        label {
            display: block;
            margin-bottom: 8px;
            font-weight: 600;
            color: #333;
        }

        input, select, textarea {
            width: 100%;
            padding: 10px;
            border: 1px solid #ddd;
            border-radius: 4px;
            font-size: 14px;
            box-sizing: border-box;
            font-family: Arial, sans-serif;
        }

        input:focus, select:focus, textarea:focus {
            outline: none;
            border-color: #667eea;
            box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
        }

        textarea {
            min-height: 100px;
            resize: vertical;
        }

        .required {
            color: #dc3545;
        }

        .button-group {
            display: flex;
            gap: 10px;
            margin-top: 30px;
        }

        button {
            flex: 1;
            padding: 12px;
            font-size: 16px;
            font-weight: 600;
            border: none;
            border-radius: 4px;
            cursor: pointer;
            transition: background 0.3s;
        }

        .btn-primary {
            background: #667eea;
            color: white;
        }

        .btn-primary:hover {
            background: #5568d3;
        }

        .btn-secondary {
            background: #ddd;
            color: #333;
        }

        .btn-secondary:hover {
            background: #bbb;
        }

        .alert {
            padding: 12px 16px;
            border-radius: 4px;
            margin-bottom: 20px;
        }

        .alert-success {
            background: #d4edda;
            color: #155724;
            border: 1px solid #c3e6cb;
        }

        .alert-danger {
            background: #f8d7da;
            color: #721c24;
            border: 1px solid #f5c6cb;
        }

        .info-box {
            background: #e7f3ff;
            border-left: 4px solid #2196F3;
            padding: 15px;
            margin-bottom: 20px;
            border-radius: 4px;
            color: #1976D2;
            font-size: 14px;
        }

        .info-box ul {
            margin: 10px 0 0 20px;
        }

        .info-box li {
            margin: 5px 0;
        }
    </style>
        <link rel="stylesheet" href="css/sidebar.css">

</head>
<body>
    <div class="container">
        <!-- Sidebar -->
        <div class="sidebar">
            <h2>📊 MEI</h2>
            <nav>
                <a href="dashboard">Dashboard</a>
                <a href="venda" class="active">Cadastrar Venda</a>
                <a href="notas">Verificar Notas</a>
                <a href="historico">Histórico</a>
                <a href="relatorio.jsp">Relatório</a>
                <a href="perfil">Perfil</a>
                <a href="login?logout=true">Sair</a>
            </nav>
        </div>

        <!-- Main Content -->
        <div class="main-content">
            <!-- Topbar -->
            <div class="topbar">
                Bem-vindo, <strong><%= usuario.getNome() %></strong>
            </div>

            <!-- Conteúdo -->
            <div class="content">
                <h1 class="page-title">Cadastrar Nova Venda</h1>

                <!-- Mensagens -->
                <% if (sucesso != null) { %>
                    <div class="alert alert-success"><%= sucesso %></div>
                <% } %>

                <% if (erro != null) { %>
                    <div class="alert alert-danger"><%= erro %></div>
                <% } %>

                <!-- Info Box -->
                <div class="info-box">
                    <strong>💡 Dicas:</strong>
                    <ul>
                        <li>Selecione a categoria do serviço</li>
                        <li>Valor em reais (R$)</li>
                        <li>Limite anual MEI: R$ 81.000,00</li>
                    </ul>
                </div>

                <!-- Formulário -->
                <div class="form-container">
                    <form method="POST" action="venda" onsubmit="return validarFormulario()">

                        <!-- Categoria -->
                        <div class="form-group">
                            <label for="categoria">
                                Categoria <span class="required">*</span>
                            </label>
                            <select id="categoria" name="categoria" required>
                                <option value="">-- Selecione uma categoria --</option>
                                <%
                                    if (categorias != null && categorias.size() > 0) {
                                        for (Categoria cat : categorias) {
                                %>
                                    <option value="<%= cat.getIdCategoria() %>">
                                        <%= cat.getNomeCategoria() %>
                                    </option>
                                <%
                                        }
                                    } else {
                                %>
                                    <option value="">Nenhuma categoria disponível</option>
                                <%
                                    }
                                %>
                            </select>
                        </div>

                        <!-- Valor -->
                        <div class="form-group">
                            <label for="valor">
                                Valor (R$) <span class="required">*</span>
                            </label>
                            <input
                                type="number"
                                id="valor"
                                name="valor"
                                placeholder="0.00"
                                step="0.01"
                                min="0"
                                required
                            >
                        </div>

                        <!-- Descrição -->
                        <div class="form-group">
                            <label for="descricao">Descrição (opcional)</label>
                            <textarea
                                id="descricao"
                                name="descricao"
                                placeholder="Descreva os detalhes da venda..."
                            ></textarea>
                        </div>

                        <!-- Botões -->
                        <div class="button-group">
                            <button type="submit" class="btn-primary">Cadastrar Venda</button>
                            <button type="reset" class="btn-secondary">Limpar</button>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    </div>

    <script>
        function validarFormulario() {
            const categoria = document.getElementById('categoria').value;
            const valor = document.getElementById('valor').value;

            if (!categoria) {
                alert('Selecione uma categoria!');
                return false;
            }

            if (!valor || parseFloat(valor) <= 0) {
                alert('Informe um valor válido!');
                return false;
            }

            return true;
        }
    </script>
</body>
</html>
