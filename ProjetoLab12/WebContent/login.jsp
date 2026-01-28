<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Sistema MEI - Login</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body {
            font-family: 'Segoe UI', Tahoma, sans-serif;
            background: linear-gradient(135deg, #6a11cb 0%, #2575fc 100%);
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
            max-width: 450px;
        }
        h1 {
            text-align: center;
            color: #2d3748;
            margin-bottom: 30px;
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
        }
        input:focus {
            outline: none;
            border-color: #4c51bf;
        }
        .btn {
            width: 100%;
            padding: 14px;
            background: #4c51bf;
            color: white;
            border: none;
            border-radius: 8px;
            font-size: 16px;
            font-weight: 600;
            cursor: pointer;
        }
        .btn:hover {
            background: #434190;
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
        }
    </style>
</head>
<body>
    <div class="container">
        <h1>🔐 Sistema MEI</h1>
        
        <% 
            String erro = (String) request.getAttribute("erro");
            if (erro != null) {
        %>
            <div class="alert error"><%= erro %></div>
        <% } %>
        
        <form action="login" method="post">
            <div class="form-group">
                <label>CPF</label>
                <input type="text" name="cpf" placeholder="000.000.000-00" required>
            </div>
            
            <div class="form-group">
                <label>Senha</label>
                <input type="password" name="senha" placeholder="Digite sua senha" required>
            </div>
            
            <button type="submit" class="btn">Entrar</button>
        </form>
        
        <p style="text-align: center; margin-top: 20px;">
            <a href="cadastro.jsp" style="color: #4c51bf;">Criar nova conta</a>
        </p>
    </div>
</body>
</html>

