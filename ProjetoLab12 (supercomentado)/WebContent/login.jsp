<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="br.com.projeto.model.Usuario" %>

<%
    /* ================================================================
       VALIDAÇÃO DE SESSÃO E MENSAGENS
       ================================================================

       1. Verifica se usuário já está logado
       2. Busca mensagens de erro/sucesso da sessão
       3. Remove mensagens após exibir (evita duplicação)
    */

    // ========== VERIFICAR SE JÁ ESTÁ LOGADO ==========
    Usuario usuario = (Usuario) session.getAttribute("usuario");
    if (usuario != null) {
        // Já está logado, redireciona para dashboard
        response.sendRedirect("dashboard");
        return;
    }

    // ========== BUSCAR MENSAGENS ==========
    String erro = (String) session.getAttribute("erro");
    String sucesso = (String) session.getAttribute("sucesso");

    // ========== REMOVER MENSAGENS (evita duplicação) ==========
    session.removeAttribute("erro");
    session.removeAttribute("sucesso");
%>

<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Login - Sistema MEI</title>

    <style>
        /* ================================================================
           CSS COMPLETO - Página de Login
           ================================================================

           SEÇÕES:
           1. Reset e body
           2. Container principal
           3. Logo e título
           4. Divider
           5. Mensagens (erro/sucesso)
           6. Formulário
           7. Botões
           8. Footer
           9. Responsividade
        */

        /* === 1. RESET === */
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

        /* === 2. CONTAINER === */
        .login-container {
            background: white;
            border-radius: 12px;
            box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
            width: 100%;
            max-width: 400px;
            padding: 40px;
        }

        /* === 3. LOGO E TÍTULO === */
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

        /* === 4. DIVIDER === */
        .divider {
            height: 2px;
            background: #e2e8f0;
            margin: 20px 0;
        }

        /* === 5. MENSAGENS === */
        .error-message {
            background-color: #fed7d7;
            color: #742a2a;
            padding: 12px;
            border-radius: 6px;
            margin-bottom: 20px;
            border-left: 4px solid #fc8181;
            font-size: 14px;
            display: flex;
            align-items: center;
            gap: 8px;
        }

        .success-message {
            background-color: #c6f6d5;
            color: #22543d;
            padding: 12px;
            border-radius: 6px;
            margin-bottom: 20px;
            border-left: 4px solid #9ae6b4;
            font-size: 14px;
            display: flex;
            align-items: center;
            gap: 8px;
        }

        /* === 6. FORMULÁRIO === */
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

        .form-group input::placeholder {
            color: #a0aec0;
        }

        /* === 7. BOTÕES === */
        .buttons {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 12px;
            margin-top: 25px;
        }

        button, .btn {
            padding: 12px;
            border: none;
            border-radius: 6px;
            font-size: 14px;
            font-weight: 600;
            cursor: pointer;
            transition: all 0.3s;
            font-family: inherit;
            text-decoration: none;
            text-align: center;
            display: inline-block;
        }

        .btn-login {
            background-color: #667eea;
            color: white;
            grid-column: 1;
        }

        .btn-login:hover {
            background-color: #5568d3;
            box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
            transform: translateY(-1px);
        }

        .btn-register {
            background-color: #e2e8f0;
            color: #2d3748;
            grid-column: 2;
        }

        .btn-register:hover {
            background-color: #cbd5e0;
            transform: translateY(-1px);
        }

        /* === 8. FOOTER === */
        .footer {
            text-align: center;
            margin-top: 20px;
            color: #718096;
            font-size: 12px;
        }

        .footer a {
            color: #667eea;
            text-decoration: none;
        }

        .footer a:hover {
            text-decoration: underline;
        }

        /* === 9. RESPONSIVIDADE === */
        @media (max-width: 480px) {
            .login-container {
                padding: 30px 20px;
            }

            .buttons {
                grid-template-columns: 1fr;
            }

            .btn-login, .btn-register {
                grid-column: auto;
            }
        }
    </style>

    <script>
        /* ================================================================
           JAVASCRIPT - Máscara de CPF
           ================================================================ */

        /**
         * Aplica máscara de CPF: XXX.XXX.XXX-XX
         *
         * @param {Event} e - Evento de input
         */
        function aplicarMascaraCPF(e) {
            let valor = e.target.value.replace(/\D/g, ''); // Remove não-numéricos

            // Limita a 11 dígitos
            if (valor.length > 11) {
                valor = valor.substring(0, 11);
            }

            // Aplica máscara progressivamente
            if (valor.length > 3) {
                valor = valor.substring(0, 3) + '.' + valor.substring(3);
            }
            if (valor.length > 7) {
                valor = valor.substring(0, 7) + '.' + valor.substring(7);
            }
            if (valor.length > 11) {
                valor = valor.substring(0, 11) + '-' + valor.substring(11);
            }

            e.target.value = valor;
        }
    </script>
</head>
<body>
    <!-- ================================================================
         CONTAINER PRINCIPAL
         ================================================================ -->
    <div class="login-container">

        <!-- ========== LOGO E TÍTULO ========== -->
        <div class="logo">
            <h1>📊 MEI</h1>
            <p>Sistema de Gerenciamento de Vendas</p>
        </div>

        <!-- ========== DIVIDER ========== -->
        <div class="divider"></div>

        <!-- ========== MENSAGEM DE ERRO ========== -->
        <% if (erro != null && !erro.isEmpty()) { %>
            <div class="error-message">
                <span>✗</span>
                <span><%= erro %></span>
            </div>
        <% } %>

        <!-- ========== MENSAGEM DE SUCESSO ========== -->
        <% if (sucesso != null && !sucesso.isEmpty()) { %>
            <div class="success-message">
                <span>✓</span>
                <span><%= sucesso %></span>
            </div>
        <% } %>

        <!-- ========== FORMULÁRIO DE LOGIN ========== -->
        <!--
            IMPORTANTE:
            - action="login" (SEM .jsp!)
            - Roteado para loginController.doPost()
            - Path detectado: /login
        -->
        <form method="POST" action="login">

            <!-- ========== CAMPO CPF ========== -->
            <div class="form-group">
                <label for="cpf">CPF</label>
                <input
                    type="text"
                    id="cpf"
                    name="cpf"
                    placeholder="123.456.789-10"
                    required
                    autofocus
                    maxlength="14"
                    oninput="aplicarMascaraCPF(event)"
                >
            </div>

            <!-- ========== CAMPO SENHA ========== -->
            <div class="form-group">
                <label for="senha">Senha</label>
                <input
                    type="password"
                    id="senha"
                    name="senha"
                    placeholder="••••••••"
                    required
                    minlength="6"
                >
            </div>

            <!-- ========== BOTÕES ========== -->
            <div class="buttons">
                <!-- Botão Entrar: Submit do formulário -->
                <button type="submit" class="btn-login">
                    Entrar
                </button>

                <!--
                    Botão Cadastrar: Link para rota /cadastro
                    IMPORTANTE: href="cadastro" (SEM .jsp!)
                    Roteado para loginController.doGet() → exibirCadastro()
                -->
                <a href="cadastro" class="btn btn-register">
                    Cadastrar
                </a>
            </div>
        </form>

        <!-- ========== FOOTER ========== -->
        <div class="footer">
            <p>Credenciais de teste:</p>
            <p>
                CPF: <strong>123.456.789-10</strong><br>
                Senha: <strong>123456</strong>
            </p>
            <p style="margin-top: 10px;">
                <a href="cadastro">Não tem conta? Cadastre-se</a>
            </p>
        </div>
    </div>
</body>
</html>

<!-- ================================================================
     RESUMO DO ARQUIVO
     ================================================================

     FORMULÁRIO:
     - action="login" → POST para loginController
     - Campo CPF: máscara automática XXX.XXX.XXX-XX
     - Campo Senha: type="password", minlength="6"

     ROTAS:
     - POST /login → loginController.processarLogin()
     - Link /cadastro → loginController.exibirCadastro()

     VALIDAÇÕES:
     - CPF e Senha obrigatórios (required)
     - Senha mínimo 6 caracteres (minlength)
     - Máscara CPF aplicada em tempo real

     MENSAGENS:
     - Erro: fundo vermelho, vem da sessão
     - Sucesso: fundo verde, vem após cadastro
     - Auto-remove da sessão após exibir

     DESIGN:
     - Gradiente roxo de fundo
     - Card branco centralizado
     - Responsivo (mobile-friendly)
     - Botões com hover animado

     SEGURANÇA:
     - Verifica se já está logado (redireciona)
     - Senha type="password" (oculta)
     - Máscara remove ao enviar (backend trata)

     COMPATIBILIDADE:
     - ✅ loginController.java (L minúsculo)
     - ✅ LoginController.java (L maiúsculo)
     - ✅ AuthController.java (qualquer nome)
     - Funciona com qualquer servlet em @WebServlet("/login")
     ================================================================ -->
