<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>📊 Sistema MEI - Login</title>
    <style>
        /* ESTILOS BÁSICOS - pode melhorar depois */
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
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
        .logo {
            text-align: center;
            margin-bottom: 30px;
        }
        .logo h1 {
            color: #2d3748;
            font-size: 28px;
            margin-bottom: 10px;
        }
        .logo p {
            color: #718096;
            font-size: 14px;
        }
        .alert {
            padding: 12px;
            border-radius: 8px;
            margin-bottom: 20px;
            text-align: center;
            font-size: 14px;
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
        .tabs {
            display: flex;
            margin-bottom: 25px;
            border-bottom: 2px solid #e2e8f0;
        }
        .tab-btn {
            flex: 1;
            background: none;
            border: none;
            padding: 12px;
            font-size: 16px;
            color: #718096;
            cursor: pointer;
            transition: all 0.3s;
        }
        .tab-btn.active {
            color: #4c51bf;
            font-weight: 600;
            border-bottom: 3px solid #4c51bf;
            margin-bottom: -2px;
        }
        .form-container {
            display: none;
        }
        .form-container.active {
            display: block;
        }
        .form-group {
            margin-bottom: 20px;
        }
        label {
            display: block;
            margin-bottom: 8px;
            color: #4a5568;
            font-weight: 500;
            font-size: 14px;
        }
        input {
            width: 100%;
            padding: 12px 15px;
            border: 2px solid #e2e8f0;
            border-radius: 8px;
            font-size: 16px;
            transition: border-color 0.3s;
        }
        input:focus {
            outline: none;
            border-color: #4c51bf;
            box-shadow: 0 0 0 3px rgba(76, 81, 191, 0.1);
        }
        input.error-input {
            border-color: #fc8181;
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
            transition: background 0.3s;
        }
        .btn:hover {
            background: #434190;
        }
        .btn-success {
            background: #38a169;
        }
        .btn-success:hover {
            background: #2f855a;
        }
        .links {
            text-align: center;
            margin-top: 25px;
            padding-top: 20px;
            border-top: 1px solid #e2e8f0;
            color: #718096;
            font-size: 14px;
        }
        .links a {
            color: #4c51bf;
            text-decoration: none;
            font-weight: 500;
        }
        .links a:hover {
            text-decoration: underline;
        }
        .password-container {
            position: relative;
        }
        .toggle-password {
            position: absolute;
            right: 15px;
            top: 50%;
            transform: translateY(-50%);
            background: none;
            border: none;
            color: #718096;
            cursor: pointer;
            font-size: 18px;
        }
    </style>
</head>
<body>
    <div class="container">
        <div class="logo">
            <h1>📊 Sistema MEI</h1>
            <p>Gerador de Relatório Mensal de Receitas Brutas</p>
        </div>
        
        <%-- MENSAGENS DO SISTEMA --%>
        <%
            String erro = (String) request.getAttribute("erro");
            String sucesso = (String) request.getAttribute("sucesso");
            
            if (erro != null) {
        %>
            <div class="alert error">
                ⚠️ <%= erro %>
            </div>
        <%
            }
            if (sucesso != null) {
        %>
            <div class="alert success">
                ✅ <%= sucesso %>
            </div>
        <%
            }
        %>
        
        <%-- ABA DE LOGIN/CADASTRO --%>
        <div class="tabs">
            <button class="tab-btn active" onclick="mostrarForm('login')">ENTRAR</button>
            <button class="tab-btn" onclick="mostrarForm('cadastro')">CADASTRAR</button>
        </div>
        
        <%-- FORMULÁRIO DE LOGIN --%>
        <form id="formLogin" class="form-container active" action="login" method="post">
            <div class="form-group">
                <label for="cpfLogin">CPF</label>
                <input type="text" id="cpfLogin" name="cpf" 
                       placeholder="000.000.000-00" required
                       oninput="formatarCPF(this)">
            </div>
            
            <div class="form-group">
                <label for="senhaLogin">Senha</label>
                <div class="password-container">
                    <input type="password" id="senhaLogin" name="senha" 
                           placeholder="Digite sua senha" required>
                    <button type="button" class="toggle-password" 
                            onclick="togglePassword('senhaLogin')">👁️</button>
                </div>
            </div>
            
            <button type="submit" class="btn">🔓 Entrar no Sistema</button>
        </form>
        
        <%-- FORMULÁRIO DE CADASTRO --%>
        <form id="formCadastro" class="form-container" action="login" method="post">
            <input type="hidden" name="acao" value="cadastrar">
            
            <div class="form-group">
                <label for="cpfCadastro">CPF</label>
                <input type="text" id="cpfCadastro" name="cpf" 
                       placeholder="000.000.000-00" required
                       oninput="formatarCPF(this)">
            </div>
            
            <div class="form-group">
                <label for="nome">Nome Completo</label>
                <input type="text" id="nome" name="nome" 
                       placeholder="Seu nome completo" required>
            </div>
            
            <div class="form-group">
                <label for="email">E-mail</label>
                <input type="email" id="email" name="email" 
                       placeholder="seu@email.com" required>
            </div>
            
            <div class="form-group">
                <label for="senhaCadastro">Senha</label>
                <div class="password-container">
                    <input type="password" id="senhaCadastro" name="senha" 
                           placeholder="Mínimo 6 caracteres" required minlength="6">
                    <button type="button" class="toggle-password" 
                            onclick="togglePassword('senhaCadastro')">👁️</button>
                </div>
            </div>
            
            <button type="submit" class="btn btn-success">📝 Criar Conta MEI</button>
        </form>
        
        <div class="links">
            <p>Problemas para acessar? <a href="mailto:suporte@meisistema.com">Contate o suporte</a></p>
            <p style="margin-top: 5px; font-size: 12px;">Versão 1.0 - Para Microempreendedores Individuais</p>
        </div>
    </div>
    
    <script>
        // FUNÇÃO PARA ALTERNAR ENTRE LOGIN E CADASTRO
        function mostrarForm(tipo) {
            // Atualizar botões das abas
            document.querySelectorAll('.tab-btn').forEach(btn => {
                btn.classList.remove('active');
            });
            event.target.classList.add('active');
            
            // Esconder todos os formulários
            document.getElementById('formLogin').classList.remove('active');
            document.getElementById('formCadastro').classList.remove('active');
            
            // Mostrar formulário selecionado
            document.getElementById('form' + tipo.charAt(0).toUpperCase() + tipo.slice(1))
                .classList.add('active');
        }
        
        // FUNÇÃO PARA FORMATAR CPF
        function formatarCPF(input) {
            let value = input.value.replace(/\D/g, '');
            
            if (value.length > 11) {
                value = value.substring(0, 11);
            }
            
            // Aplicar máscara: 000.000.000-00
            if (value.length > 9) {
                value = value.replace(/(\d{3})(\d{3})(\d{3})(\d{2})/, '$1.$2.$3-$4');
            } else if (value.length > 6) {
                value = value.replace(/(\d{3})(\d{3})(\d{1,3})/, '$1.$2.$3');
            } else if (value.length > 3) {
                value = value.replace(/(\d{3})(\d{1,3})/, '$1.$2');
            }
            
            input.value = value;
            
            // Validação básica
            if (value.length === 14) { // CPF completo tem 14 chars com máscara
                input.classList.remove('error-input');
            } else {
                input.classList.add('error-input');
            }
        }
        
        // FUNÇÃO PARA MOSTRAR/ESCONDER SENHA
        function togglePassword(inputId) {
            const input = document.getElementById(inputId);
            const button = event.target;
            
            if (input.type === 'password') {
                input.type = 'text';
                button.textContent = '👁️‍🗨️';
            } else {
                input.type = 'password';
                button.textContent = '👁️';
            }
        }
        
        // VALIDAÇÃO DE FORMULÁRIO
        document.addEventListener('DOMContentLoaded', function() {
            const forms = document.querySelectorAll('form');
            forms.forEach(form => {
                form.addEventListener('submit', function(e) {
                    let valid = true;
                    
                    // Verificar campos obrigatórios
                    const inputs = form.querySelectorAll('input[required]');
                    inputs.forEach(input => {
                        if (!input.value.trim()) {
                            valid = false;
                            input.classList.add('error-input');
                        } else {
                            input.classList.remove('error-input');
                        }
                    });
                    
                    // Validação específica para CPF
                    const cpfInputs = form.querySelectorAll('input[name="cpf"]');
                    cpfInputs.forEach(cpf => {
                        const cpfValue = cpf.value.replace(/\D/g, '');
                        if (cpfValue.length !== 11) {
                            valid = false;
                            cpf.classList.add('error-input');
                        }
                    });
                    
                    if (!valid) {
                        e.preventDefault();
                        alert('Por favor, preencha todos os campos corretamente.');
                    }
                });
            });
        });
        
        // FOCO NO PRIMEIRO CAMPO DO FORMULÁRIO ATIVO
        window.onload = function() {
            const activeForm = document.querySelector('.form-container.active');
            if (activeForm) {
                const firstInput = activeForm.querySelector('input');
                if (firstInput) firstInput.focus();
            }
        };
    </script>
</body>
</html>