<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Cadastro - Sistema MEI</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body {
            font-family: 'Segoe UI', Tahoma, sans-serif;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            height: 100vh;
            display: flex;
            justify-content: center;
            align-items: center;
        }
        .container {
            background: white;
            padding: 40px;
            border-radius: 15px;
            box-shadow: 0 15px 35px rgba(0,0,0,0.2);
            width: 90%;
            max-width: 500px;
        }
        h1 {
            text-align: center;
            color: #2d3748;
            margin-bottom: 10px;
        }
        .subtitle {
            text-align: center;
            color: #718096;
            margin-bottom: 30px;
            font-size: 14px;
        }
        .form-group {
            margin-bottom: 20px;
        }
        label {
            display: block;
            margin-bottom: 8px;
            color: #4a5568;
            font-weight: 500;
        }
        input {
            width: 100%;
            padding: 12px;
            border: 2px solid #e2e8f0;
            border-radius: 8px;
            font-size: 16px;
            transition: border 0.3s;
        }
        input:focus {
            outline: none;
            border-color: #667eea;
        }
        .btn {
            width: 100%;
            padding: 14px;
            background: #667eea;
            color: white;
            border: none;
            border-radius: 8px;
            font-size: 16px;
            font-weight: 600;
            cursor: pointer;
            transition: background 0.3s;
            margin-top: 10px;
        }
        .btn:hover {
            background: #5568d3;
        }
        .alert {
            padding: 12px;
            border-radius: 8px;
            margin-bottom: 20px;
            text-align: center;
        }
        .alert.error {
            background: #fed7d7;
            color: #c53030;
            border: 1px solid #fc8181;
        }
        .alert.success {
            background: #c6f6d5;
            color: #22543d;
            border: 1px solid #9ae6b4;
        }
        .back-link {
            text-align: center;
            margin-top: 20px;
        }
        .back-link a {
            color: #667eea;
            text-decoration: none;
            font-weight: 500;
        }
        .back-link a:hover {
            text-decoration: underline;
        }
        .hint {
            font-size: 12px;
            color: #718096;
            margin-top: 5px;
        }
    </style>
</head>
<body>
    <div class="container">
        <h1>🔐 Criar Conta</h1>
        <p class="subtitle">Cadastre-se no Sistema MEI</p>

        <%
            String erro = (String) request.getAttribute("erro");
            String sucesso = (String) request.getAttribute("sucesso");
            if (erro != null) {
        %>
            <div class="alert error"><%= erro %></div>
        <% } %>

        <% if (sucesso != null) { %>
            <div class="alert success"><%= sucesso %></div>
        <% } %>

        <form action="login" method="post" id="cadastroForm">
            <input type="hidden" name="acao" value="cadastrar">

            <div class="form-group">
                <label>Nome Completo</label>
                <input type="text" name="nome" id="nome" placeholder="Digite seu nome completo" required>
            </div>

            <div class="form-group">
                <label>CPF</label>
                <input type="text" name="cpf" id="cpf" placeholder="000.000.000-00" maxlength="14" required>
                <div class="hint">Apenas números (11 dígitos)</div>
            </div>

            <div class="form-group">
                <label>E-mail</label>
                <input type="email" name="email" id="email" placeholder="seu@email.com" required>
            </div>

            <div class="form-group">
                <label>Senha</label>
                <input type="password" name="senha" id="senha" placeholder="Mínimo 6 caracteres" required minlength="6">
            </div>

            <div class="form-group">
                <label>Confirmar Senha</label>
                <input type="password" name="confirmarSenha" id="confirmarSenha" placeholder="Digite a senha novamente" required>
            </div>

            <button type="submit" class="btn">Cadastrar</button>
        </form>

        <div class="back-link">
            <a href="login">← Voltar para o Login</a>
        </div>
    </div>

    <script>
        // Máscara de CPF - apenas formatação visual
        const cpfInput = document.getElementById('cpf');
        cpfInput.addEventListener('input', function(e) {
            let value = e.target.value.replace(/\D/g, '');
            if (value.length <= 11) {
                value = value.replace(/(\d{3})(\d)/, '$1.$2');
                value = value.replace(/(\d{3})(\d)/, '$1.$2');
                value = value.replace(/(\d{3})(\d{1,2})$/, '$1-$2');
                e.target.value = value;
            }
        });

        // Validação simples antes de enviar
        const form = document.getElementById('cadastroForm');
        form.addEventListener('submit', function(e) {
            const cpf = cpfInput.value.replace(/\D/g, '');
            const senha = document.getElementById('senha').value;
            const confirmarSenha = document.getElementById('confirmarSenha').value;

            // Verificar se CPF tem 11 dígitos
            if (cpf.length !== 11) {
                alert('CPF deve ter 11 dígitos!');
                e.preventDefault();
                return;
            }

            // Verificar se senhas coincidem
            if (senha !== confirmarSenha) {
                alert('As senhas não coincidem!');
                e.preventDefault();
                return;
            }

            // Verificar senha mínima
            if (senha.length < 6) {
                alert('Senha deve ter no mínimo 6 caracteres!');
                e.preventDefault();
                return;
            }
        });
    </script>
</body>
</html>