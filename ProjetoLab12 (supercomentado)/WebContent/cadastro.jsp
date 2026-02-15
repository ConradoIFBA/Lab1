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
    String mensagemErro = (String) session.getAttribute("erro");
    String mensagemSucesso = (String) session.getAttribute("sucesso");

    // ========== REMOVER MENSAGENS (evita duplicação) ==========
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
        /* ================================================================
           CSS COMPLETO - Página de Cadastro
           ================================================================

           SEÇÕES:
           1. Reset e body
           2. Container principal
           3. Card de cadastro
           4. Logo e título
           5. Alerts (erro/sucesso)
           6. Formulário
           7. Campos de input
           8. Botões
           9. Link de voltar
           10. Responsividade
        */

        /* === 1. RESET === */
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }

        body {
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            min-height: 100vh;
            display: flex;
            align-items: center;
            justify-content: center;
            padding: 20px;
        }

        /* === 2. CONTAINER PRINCIPAL === */
        .container {
            width: 100%;
            max-width: 500px;
        }

        /* === 3. CARD DE CADASTRO === */
        .cadastro-card {
            background: #1e293b;
            border-radius: 16px;
            padding: 40px;
            box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
            border: 1px solid #334155;
        }

        /* === 4. LOGO E TÍTULO === */
        .logo-section {
            text-align: center;
            margin-bottom: 30px;
        }

        .logo {
            font-size: 48px;
            margin-bottom: 10px;
        }

        .logo-title {
            font-size: 32px;
            font-weight: 700;
            color: #f1f5f9;
            margin: 0;
        }

        .logo-subtitle {
            font-size: 14px;
            color: #94a3b8;
            margin-top: 5px;
        }

        /* === 5. ALERTS (Erro/Sucesso) === */
        .alert {
            padding: 14px 18px;
            border-radius: 8px;
            margin-bottom: 20px;
            display: flex;
            align-items: center;
            gap: 10px;
            font-size: 14px;
        }

        .alert-error {
            background: #fee2e2;
            color: #991b1b;
            border: 1px solid #fecaca;
        }

        .alert-success {
            background: #d1fae5;
            color: #065f46;
            border: 1px solid #a7f3d0;
        }

        .alert-icon {
            font-size: 18px;
        }

        /* === 6. FORMULÁRIO === */
        .form-header {
            margin-bottom: 25px;
            padding: 15px;
            background: #0f172a;
            border-radius: 8px;
            border-left: 4px solid #667eea;
        }

        .form-header-icon {
            font-size: 16px;
            margin-right: 8px;
        }

        .form-header-text {
            font-size: 13px;
            color: #cbd5e1;
            line-height: 1.5;
        }

        .form-group {
            margin-bottom: 20px;
        }

        .form-group label {
            display: block;
            margin-bottom: 8px;
            font-weight: 600;
            color: #e2e8f0;
            font-size: 14px;
        }

        .required {
            color: #ef4444;
            margin-left: 4px;
        }

        /* === 7. CAMPOS DE INPUT === */
        .form-group input {
            width: 100%;
            padding: 12px 14px;
            background-color: #0f172a;
            border: 1px solid #334155;
            border-radius: 8px;
            color: #e2e8f0;
            font-size: 14px;
            transition: all 0.2s;
        }

        .form-group input:focus {
            outline: none;
            border-color: #667eea;
            box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
        }

        .form-group input::placeholder {
            color: #64748b;
        }

        .form-group small {
            display: block;
            margin-top: 6px;
            font-size: 12px;
            color: #64748b;
        }

        /* === 8. BOTÕES === */
        .form-actions {
            display: flex;
            flex-direction: column;
            gap: 10px;
            margin-top: 30px;
        }

        .btn {
            padding: 12px 24px;
            border: none;
            border-radius: 8px;
            font-size: 14px;
            font-weight: 600;
            cursor: pointer;
            transition: all 0.2s;
            text-decoration: none;
            text-align: center;
        }

        .btn-primary {
            background: linear-gradient(135deg, #667eea, #764ba2);
            color: white;
        }

        .btn-primary:hover {
            transform: translateY(-2px);
            box-shadow: 0 8px 20px rgba(102, 126, 234, 0.4);
        }

        .btn-secondary {
            background: #334155;
            color: #e2e8f0;
        }

        .btn-secondary:hover {
            background: #475569;
        }

        /* === 9. LINK DE VOLTAR === */
        .back-link {
            display: flex;
            align-items: center;
            justify-content: center;
            gap: 8px;
            margin-top: 20px;
            color: #94a3b8;
            text-decoration: none;
            font-size: 14px;
            transition: color 0.2s;
        }

        .back-link:hover {
            color: #cbd5e1;
        }

        /* === 10. RESPONSIVIDADE === */
        @media (max-width: 600px) {
            .cadastro-card {
                padding: 30px 20px;
            }

            .logo-title {
                font-size: 28px;
            }

            .form-actions {
                flex-direction: column;
            }
        }
    </style>

    <script>
        /* ================================================================
           JAVASCRIPT - Validação e Máscaras
           ================================================================

           FUNÇÕES:
           1. aplicarMascaraCPF()  - Máscara XXX.XXX.XXX-XX
           2. aplicarMascaraCNPJ() - Máscara XX.XXX.XXX/XXXX-XX
           3. validarFormulario()  - Validação antes de enviar
        */

        /**
         * Aplica máscara de CPF: XXX.XXX.XXX-XX
         *
         * @param {Event} e - Evento de input
         */
        function aplicarMascaraCPF(e) {
            let valor = e.target.value.replace(/\D/g, '');

            if (valor.length > 11) {
                valor = valor.substring(0, 11);
            }

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

        /**
         * Aplica máscara de CNPJ: XX.XXX.XXX/XXXX-XX
         *
         * @param {Event} e - Evento de input
         */
        function aplicarMascaraCNPJ(e) {
            let valor = e.target.value.replace(/\D/g, '');

            if (valor.length > 14) {
                valor = valor.substring(0, 14);
            }

            if (valor.length > 2) {
                valor = valor.substring(0, 2) + '.' + valor.substring(2);
            }
            if (valor.length > 6) {
                valor = valor.substring(0, 6) + '.' + valor.substring(6);
            }
            if (valor.length > 10) {
                valor = valor.substring(0, 10) + '/' + valor.substring(10);
            }
            if (valor.length > 15) {
                valor = valor.substring(0, 15) + '-' + valor.substring(15);
            }

            e.target.value = valor;
        }

        /**
         * Valida formulário antes de enviar
         *
         * @returns {boolean} true se válido, false caso contrário
         */
        function validarFormulario() {
            const cpf = document.getElementById('cpf').value.replace(/\D/g, '');
            const nome = document.getElementById('nome').value.trim();
            const cnpj = document.getElementById('cnpj').value.replace(/\D/g, '');
            const senha = document.getElementById('senha').value;
            const confirmarSenha = document.getElementById('confirmarSenha').value;

            // Validar CPF
            if (cpf.length !== 11) {
                alert('❌ CPF deve ter 11 dígitos!');
                return false;
            }

            // Validar nome
            if (nome.length < 3) {
                alert('❌ Nome deve ter pelo menos 3 caracteres!');
                return false;
            }

            // Validar CNPJ (se fornecido)
            if (cnpj.length > 0 && cnpj.length !== 14) {
                alert('❌ CNPJ deve ter 14 dígitos!');
                return false;
            }

            // Validar senha
            if (senha.length < 6) {
                alert('❌ Senha deve ter no mínimo 6 caracteres!');
                return false;
            }

            // Validar confirmação
            if (senha !== confirmarSenha) {
                alert('❌ Senhas não coincidem!');
                return false;
            }

            return true;
        }
    </script>
</head>
<body>
    <!-- ================================================================
         CONTAINER PRINCIPAL
         ================================================================ -->
    <div class="container">
        <!-- ========== CARD DE CADASTRO ========== -->
        <div class="cadastro-card">

            <!-- ========== LOGO E TÍTULO ========== -->
            <div class="logo-section">
                <div class="logo">📊</div>
                <h1 class="logo-title">MEI</h1>
                <p class="logo-subtitle">Novo Usuário</p>
            </div>

            <!-- ========== ALERT DE ERRO ========== -->
            <% if (mensagemErro != null) { %>
                <div class="alert alert-error">
                    <span class="alert-icon">❌</span>
                    <span><%= mensagemErro %></span>
                </div>
            <% } %>

            <!-- ========== ALERT DE SUCESSO ========== -->
            <% if (mensagemSucesso != null) { %>
                <div class="alert alert-success">
                    <span class="alert-icon">✅</span>
                    <span><%= mensagemSucesso %></span>
                </div>
            <% } %>

            <!-- ========== HEADER DO FORMULÁRIO ========== -->
            <div class="form-header">
                <span class="form-header-icon">📋</span>
                <span class="form-header-text">
                    Preencha os dados abaixo para criar sua conta no Sistema MEI de Gerenciamento de Vendas.
                </span>
            </div>

            <!-- ========== FORMULÁRIO DE CADASTRO ========== -->
            <!--
                IMPORTANTE:
                - action="cadastro" (SEM .jsp!)
                - Roteado para loginController.doPost()
                - Path detectado: /cadastro
            -->
            <form method="POST" action="cadastro" onsubmit="return validarFormulario()">

                <!-- ========== CAMPO: CPF ========== -->
                <div class="form-group">
                    <label for="cpf">
                        CPF
                        <span class="required">*</span>
                    </label>
                    <input
                        type="text"
                        id="cpf"
                        name="cpf"
                        placeholder="000.000.000-00"
                        required
                        maxlength="14"
                        oninput="aplicarMascaraCPF(event)"
                    >
                    <small>Será usado como seu nome de usuário</small>
                </div>

                <!-- ========== CAMPO: NOME COMPLETO ========== -->
                <div class="form-group">
                    <label for="nome">
                        Nome Completo
                        <span class="required">*</span>
                    </label>
                    <input
                        type="text"
                        id="nome"
                        name="nome"
                        placeholder="Digite seu nome completo"
                        required
                        maxlength="100"
                    >
                </div>

                <!-- ========== CAMPO: EMAIL (OPCIONAL) ========== -->
                <div class="form-group">
                    <label for="email">Email</label>
                    <input
                        type="email"
                        id="email"
                        name="email"
                        placeholder="seu@email.com"
                        maxlength="100"
                    >
                    <small>Opcional - será único se fornecido</small>
                </div>

                <!-- ========== CAMPO: CNPJ (OPCIONAL) ========== -->
                <div class="form-group">
                    <label for="cnpj">CNPJ da Empresa MEI</label>
                    <input
                        type="text"
                        id="cnpj"
                        name="cnpj"
                        placeholder="00.000.000/0000-00"
                        maxlength="18"
                        oninput="aplicarMascaraCNPJ(event)"
                    >
                    <small>Opcional - CNPJ da sua empresa MEI</small>
                </div>

                <!-- ========== CAMPO: SENHA ========== -->
                <div class="form-group">
                    <label for="senha">
                        Senha
                        <span class="required">*</span>
                    </label>
                    <input
                        type="password"
                        id="senha"
                        name="senha"
                        placeholder="Mínimo 6 caracteres"
                        required
                        minlength="6"
                    >
                    <small>Mínimo 6 caracteres</small>
                </div>

                <!-- ========== CAMPO: CONFIRMAR SENHA ========== -->
                <div class="form-group">
                    <label for="confirmarSenha">
                        Confirmar Senha
                        <span class="required">*</span>
                    </label>
                    <input
                        type="password"
                        id="confirmarSenha"
                        name="confirmarSenha"
                        placeholder="Digite a senha novamente"
                        required
                        minlength="6"
                    >
                </div>

                <!-- ========== BOTÕES ========== -->
                <div class="form-actions">
                    <!-- Botão Cadastrar: Submit do formulário -->
                    <button type="submit" class="btn btn-primary">
                        Cadastrar
                    </button>

                    <!--
                        Botão Voltar: Link para rota /login
                        IMPORTANTE: href="login" (SEM .jsp!)
                        Roteado para loginController.doGet() → exibirLogin()
                    -->
                    <a href="login" class="btn btn-secondary">
                        Voltar
                    </a>
                </div>
            </form>

            <!-- ========== LINK PARA LOGIN ========== -->
            <!--
                IMPORTANTE: href="login" (SEM .jsp!)
            -->
            <a href="login" class="back-link">
                <span>←</span>
                <span>Já tem conta? Faça login</span>
            </a>
        </div>
    </div>
</body>
</html>

<!-- ================================================================
     RESUMO DO ARQUIVO
     ================================================================

     CAMPOS:
     - cpf            → Obrigatório, máscara XXX.XXX.XXX-XX
     - nome           → Obrigatório, max 100 caracteres
     - email          → Opcional, validação de formato
     - cnpj           → Opcional, máscara XX.XXX.XXX/XXXX-XX ⭐
     - senha          → Obrigatório, mínimo 6 caracteres
     - confirmarSenha → Obrigatório, deve coincidir

     FORMULÁRIO:
     - action="cadastro" → POST para loginController
     - onsubmit="return validarFormulario()" → Valida antes

     ROTAS:
     - POST /cadastro → loginController.processarCadastro()
     - Link /login → loginController.exibirLogin()

     VALIDAÇÕES FRONTEND:
     - CPF: 11 dígitos (máscara automática)
     - Nome: mínimo 3 caracteres
     - CNPJ: 14 dígitos se fornecido (máscara automática)
     - Senha: mínimo 6 caracteres
     - Confirmação: senhas devem coincidir
     - Alert() mostra erros

     VALIDAÇÕES BACKEND (loginController):
     - CPF único
     - Email único (se fornecido)
     - CNPJ formato correto
     - Senha mínimo 6 caracteres
     - Senhas coincidem
     - Hash BCrypt

     MÁSCARAS:
     - CPF: 123.456.789-01 (aplicada em tempo real)
     - CNPJ: 12.345.678/0001-90 (aplicada em tempo real)
     - Backend remove máscaras antes de salvar

     MENSAGENS:
     - Erro: vem da sessão (setAttribute)
     - Sucesso: vem da sessão (setAttribute)
     - Auto-remove após exibir

     DESIGN:
     - Fundo gradiente roxo
     - Card escuro (#1e293b)
     - Inputs com foco roxo
     - Responsivo (mobile-friendly)

     SEGURANÇA:
     - Verifica se já está logado (redireciona)
     - Senhas type="password" (ocultas)
     - Validação client-side E server-side
     - Hash BCrypt no backend

     COMPATIBILIDADE:
     - ✅ loginController.java (L minúsculo)
     - ✅ LoginController.java (L maiúsculo)
     - ✅ AuthController.java (qualquer nome)
     - Funciona com qualquer servlet em @WebServlet("/cadastro")
     ================================================================ -->
