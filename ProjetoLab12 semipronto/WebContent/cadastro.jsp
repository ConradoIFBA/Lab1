<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="br.com.projeto.model.Usuario" %>

<%
    // Verificar se usuário já está logado
    Usuario usuario = (Usuario) session.getAttribute("usuario");
    if (usuario != null) {
        response.sendRedirect("dashboard");
        return;
    }

    String erro = (String) session.getAttribute("erro");
    String sucesso = (String) session.getAttribute("sucesso");
    session.removeAttribute("erro");
    session.removeAttribute("sucesso");
%>

<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Cadastro - Sistema MEI</title>
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }

        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            min-height: 100vh;
            display: flex;
            justify-content: center;
            align-items: center;
            padding: 20px;
        }

        .signup-container {
            background: white;
            border-radius: 12px;
            box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
            width: 100%;
            max-width: 450px;
            padding: 40px;
        }

        .logo {
            text-align: center;
            margin-bottom: 30px;
        }

        .logo h1 {
            color: #667eea;
            font-size: 32px;
            margin-bottom: 5px;
            font-weight: 700;
        }

        .logo p {
            color: #718096;
            font-size: 14px;
        }

        .divider {
            height: 2px;
            background: #e2e8f0;
            margin: 20px 0;
        }

        .form-group {
            margin-bottom: 20px;
        }

        .form-group label {
            display: block;
            color: #2d3748;
            font-weight: 500;
            margin-bottom: 8px;
            font-size: 14px;
        }

        .form-group input {
            width: 100%;
            padding: 12px;
            border: 2px solid #e2e8f0;
            border-radius: 6px;
            font-size: 14px;
            transition: border-color 0.3s;
            font-family: inherit;
        }

        .form-group input:focus {
            outline: none;
            border-color: #667eea;
            background-color: #f7fafc;
        }

        .error-message {
            background-color: #fed7d7;
            color: #742a2a;
            padding: 12px;
            border-radius: 6px;
            margin-bottom: 20px;
            border-left: 4px solid #fc8181;
            font-size: 14px;
        }

        .success-message {
            background-color: #c6f6d5;
            color: #22543d;
            padding: 12px;
            border-radius: 6px;
            margin-bottom: 20px;
            border-left: 4px solid #9ae6b4;
            font-size: 14px;
        }

        .buttons {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 12px;
            margin-top: 25px;
        }

        button {
            padding: 12px;
            border: none;
            border-radius: 6px;
            font-size: 14px;
            font-weight: 600;
            cursor: pointer;
            transition: all 0.3s;
            font-family: inherit;
        }

        .btn-signup {
            background-color: #667eea;
            color: white;
            grid-column: 1;
        }

        .btn-signup:hover {
            background-color: #5568d3;
            box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
        }

        .btn-back {
            background-color: #e2e8f0;
            color: #2d3748;
            grid-column: 2;
        }

        .btn-back:hover {
            background-color: #cbd5e0;
        }

        .info {
            background-color: #ebf4ff;
            border-left: 4px solid #667eea;
            color: #2d3748;
            padding: 12px;
            border-radius: 6px;
            margin-bottom: 20px;
            font-size: 13px;
            line-height: 1.5;
        }

        @media (max-width: 480px) {
            .signup-container {
                padding: 30px 20px;
            }

            .buttons {
                grid-template-columns: 1fr;
            }

            .btn-signup {
                grid-column: auto;
            }

            .btn-back {
                grid-column: auto;
            }
        }
    </style>


</head>
<body>
    <div class="signup-container">
        <div class="logo">
            <h1>📊 MEI</h1>
            <p>Novo Usuário</p>
        </div>

        <div class="divider"></div>

        <% if (erro != null && !erro.isEmpty()) { %>
            <div class="error-message">
                ✗ <%= erro %>
            </div>
        <% } %>

        <% if (sucesso != null && !sucesso.isEmpty()) { %>
            <div class="success-message">
                ✓ <%= sucesso %>
            </div>
        <% } %>

        <div class="info">
            📝 Preencha os dados abaixo para criar sua conta no Sistema MEI de Gerenciamento de Vendas.
        </div>

        <form method="POST" action="cadastro" onsubmit="return validarFormulario()">
            <div class="form-group">
                <label for="cpf">CPF *</label>
                <input
                    type="text"
                    id="cpf"
                    name="cpf"
                    placeholder="123.456.789-10"
                    required
                    autofocus
                >
            </div>

            <div class="form-group">
                <label for="nome">Nome Completo *</label>
                <input
                    type="text"
                    id="nome"
                    name="nome"
                    placeholder="Seu nome aqui"
                    required
                    minlength="3"
                >
            </div>

            <div class="form-group">
                <label for="email">Email *</label>
                <input
                    type="email"
                    id="email"
                    name="email"
                    placeholder="seu@email.com"
                    required
                >
            </div>

            <div class="form-group">
                <label for="senha">Senha *</label>
                <input
                    type="password"
                    id="senha"
                    name="senha"
                    placeholder="••••••••"
                    required
                    minlength="6"
                >
                <small style="color: #718096;">Mínimo 6 caracteres</small>
            </div>

            <div class="form-group">
                <label for="confirmar">Confirmar Senha *</label>
                <input
                    type="password"
                    id="confirmar"
                    name="confirmar"
                    placeholder="••••••••"
                    required
                    minlength="6"
                >
            </div>

            <div class="buttons">
                <button type="submit" class="btn-signup">Cadastrar</button>
                <button type="button" class="btn-back" onclick="window.location.href='login'">
                    Voltar
                </button>
            </div>
        </form>
    </div>

    <script>
        function validarFormulario() {
            const cpf = document.getElementById('cpf').value;
            const nome = document.getElementById('nome').value;
            const email = document.getElementById('email').value;
            const senha = document.getElementById('senha').value;
            const confirmar = document.getElementById('confirmar').value;

            // Validar CPF vazio
            if (!cpf || cpf.trim() === '') {
                alert('CPF é obrigatório!');
                return false;
            }

            // Validar nome
            if (!nome || nome.trim().length < 3) {
                alert('Nome deve ter no mínimo 3 caracteres!');
                return false;
            }

            // Validar email
            const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
            if (!emailRegex.test(email)) {
                alert('Email inválido!');
                return false;
            }

            // Validar senha
            if (senha.length < 6) {
                alert('Senha deve ter no mínimo 6 caracteres!');
                return false;
            }

            // Validar confirmação
            if (senha !== confirmar) {
                alert('As senhas não conferem!');
                return false;
            }

            return true;
        }
    </script>
</body>
</html>
