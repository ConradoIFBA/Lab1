<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="br.com.projeto.model.Usuario" %>
<%--
    ================================================================
    RELATORIO JSP - Geração de Relatórios em PDF
    ================================================================

    PROPÓSITO:
    Permite ao MEI gerar relatórios mensais de receitas em PDF
    para declarações fiscais e controle financeiro.

    FUNCIONALIDADES:
    1. Formulário seleção mês/ano
    2. Gerar PDF via RelatorioController
    3. Download automático do arquivo
    4. Informações sobre conteúdo do relatório

    FLUXO:
    1. Usuário seleciona mês + ano
    2. Clica "Gerar PDF"
    3. POST /relatorio (RelatorioController)
    4. Controller busca vendas do período
    5. Gera PDF (RelatorioPDF.java)
    6. Download inicia automaticamente

    CONTEÚDO DO PDF:
    - Cabeçalho (nome, CPF, CNPJ, período)
    - Tabela receitas por categoria (Revenda, Industrial, Serviços)
    - Separação com/sem Nota Fiscal
    - Detalhamento de todas as vendas
    - Totais parciais e geral

    VALIDAÇÕES:
    ✅ Mês obrigatório (1-12)
    ✅ Ano obrigatório
    ✅ Período deve ter vendas

    ARQUIVO GERADO:
    Nome: relatorio_mei_[mes]_[ano].pdf
    Exemplo: relatorio_mei_2_2026.pdf

    CONTROLLER:
    RelatorioController.doPost()

    UTILITY:
    RelatorioPDF.gerarRelatorio()

    @author Sistema MEI
    @version 2.0
--%>

<%
    /* ================================================================
       VALIDAÇÃO E PREPARAÇÃO
       ================================================================ */

    // ========== VALIDAR SESSÃO ==========
    // Redireciona para login se não autenticado
    Usuario usuario = (Usuario) session.getAttribute("usuario");
    if (usuario == null) {
        response.sendRedirect("login");
        return;
    }

    // ========== MENSAGENS (sucesso/erro) ==========
    String mensagemSucesso = (String) session.getAttribute("sucesso");
    String mensagemErro = (String) session.getAttribute("erro");
    session.removeAttribute("sucesso");
    session.removeAttribute("erro");

    // ========== MÊS ATUAL (para pré-selecionar) ==========
    java.util.Calendar cal = java.util.Calendar.getInstance();
    int mesAtual = cal.get(java.util.Calendar.MONTH) + 1; // 0-based
    int anoAtual = cal.get(java.util.Calendar.YEAR);
%>

<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Relatórios - Sistema MEI</title>

    <style>
        /* ================================================
           RESET E BASE
           ================================================ */
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }

        body {
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
            background-color: #0f172a;
            color: #e2e8f0;
            line-height: 1.6;
        }

        /* ================================================
           LAYOUT PRINCIPAL
           ================================================ */
        .main-container {
            display: flex;
            min-height: 100vh;
        }

        /* ================================================
           SIDEBAR
           ================================================ */
        .sidebar {
            width: 250px;
            background-color: #1e293b;
            padding: 20px 0;
            position: fixed;
            left: 0;
            top: 0;
            bottom: 0;
            overflow-y: auto;
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
        }

        /* ================================================
           MAIN CONTENT
           ================================================ */
        .main-content {
            flex: 1;
            margin-left: 250px;
            display: flex;
            flex-direction: column;
        }

        /* ================================================
           TOPBAR
           ================================================ */
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
        
        .topbar-left {
    		display: flex;
    		align-items: center;
   			gap: 12px;
		}

        .topbar h2 {
            font-size: 24px;
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

        /* ================================================
           CONTENT
           ================================================ */
        .content {
            flex: 1;
            padding: 30px;
        }

        /* ================================================
           ALERTAS
           ================================================ */
        .alert {
            padding: 16px 20px;
            border-radius: 8px;
            margin-bottom: 20px;
            font-weight: 500;
        }

        .alert-success {
            background-color: #10b981;
            color: white;
        }

        .alert-error {
            background-color: #ef4444;
            color: white;
        }

        /* ================================================
           CARDS
           ================================================ */
        .card {
            background-color: #1e293b;
            border-radius: 12px;
            padding: 24px;
            margin-bottom: 24px;
            border: 1px solid #334155;
        }

        .card h3 {
            font-size: 18px;
            font-weight: 600;
            color: #f1f5f9;
            margin-bottom: 16px;
            padding-bottom: 12px;
            border-bottom: 1px solid #334155;
        }

        .card p {
            color: #94a3b8;
            margin-bottom: 20px;
            line-height: 1.6;
        }

        .card ul {
            margin: 16px 0 16px 20px;
            color: #94a3b8;
        }

        .card ul li {
            margin-bottom: 8px;
            line-height: 1.6;
        }

        /* ================================================
           FORMULÁRIO
           ================================================ */
        .form-row {
            display: grid;
            grid-template-columns: 1fr 1fr 1fr;
            gap: 16px;
            margin-bottom: 20px;
        }

        .form-group {
            display: flex;
            flex-direction: column;
        }

        .form-group label {
            display: block;
            margin-bottom: 8px;
            font-weight: 500;
            color: #cbd5e1;
            font-size: 14px;
        }

        .form-group select {
            width: 100%;
            padding: 12px 16px;
            background-color: #0f172a;
            border: 1px solid #334155;
            border-radius: 8px;
            color: #e2e8f0;
            font-size: 14px;
            transition: all 0.2s;
            cursor: pointer;
        }

        .form-group select:focus {
            outline: none;
            border-color: #3b82f6;
            box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
        }

        /* ================================================
           BOTÕES
           ================================================ */
        .btn {
            padding: 12px 24px;
            border: none;
            border-radius: 8px;
            font-weight: 600;
            cursor: pointer;
            transition: all 0.2s;
            font-size: 14px;
        }

        .btn-primary {
            background-color: #3b82f6;
            color: white;
            width: 100%;
        }

        .btn-primary:hover {
            background-color: #2563eb;
            transform: translateY(-1px);
            box-shadow: 0 4px 12px rgba(59, 130, 246, 0.4);
        }

        /* ================================================
           INFO BOX (destaque)
           ================================================ */
        .info-box {
            background-color: #1e40af20;
            border-left: 4px solid #3b82f6;
            padding: 16px;
            border-radius: 8px;
            margin-top: 20px;
        }

        .info-box h4 {
            color: #60a5fa;
            font-size: 14px;
            font-weight: 600;
            margin-bottom: 8px;
        }

        .info-box p {
            color: #94a3b8;
            font-size: 13px;
            margin: 0;
        }

        /* ================================================
           RESPONSIVE
           ================================================ */
        @media (max-width: 1024px) {
            .form-row {
                grid-template-columns: 1fr 1fr;
            }
        }

        @media (max-width: 768px) {
            .form-row {
                grid-template-columns: 1fr;
            }

            .sidebar {
                transform: translateX(-100%);
            }

            .main-content {
                margin-left: 0;
            }
        }
    </style>
    
    <!-- Modo de cor -->
    <%@ include file="corToggle.jsp" %>

</head>
<body>
    <div class="main-container">

        <!-- ================================================
             SIDEBAR - Menu de Navegação
             ================================================ -->
        <aside class="sidebar">
            <div class="sidebar-header">
                <h1>📊 MEI</h1>
            </div>
            <ul class="sidebar-menu">
                <li>
                    <a href="${pageContext.request.contextPath}/dashboard">
                        <span class="icon">🏠</span>
                        <span>Dashboard</span>
                    </a>
                </li>
                <li>
                    <a href="${pageContext.request.contextPath}/historico">
                        <span class="icon">📜</span>
                        <span>Histórico</span>
                    </a>
                </li>
                <li>
                    <a href="${pageContext.request.contextPath}/relatorio" class="active">
                        <span class="icon">📊</span>
                        <span>Relatórios</span>
                    </a>
                </li>
                <li>
                    <a href="${pageContext.request.contextPath}/perfil">
                        <span class="icon">👤</span>
                        <span>Perfil</span>
                    </a>
                </li>
                <li>
                    <a href="${pageContext.request.contextPath}/logout">
                        <span class="icon">🚪</span>
                        <span>Sair</span>
                    </a>
                </li>
            </ul>
        </aside>

        <!-- ================================================
             MAIN CONTENT
             ================================================ -->
        <div class="main-content">

            <!-- ================================================
                 TOPBAR
                 ================================================ -->
            <div class="topbar">
    <div class="topbar-left">
        <h2>📄 Relatórios</h2>
    </div>
    <div class="topbar-right">
        <div class="user-info">
            <div class="user-avatar">
                        <%= usuario.getNome().substring(0, 1).toUpperCase() %>
                    </div>
                    <div class="user-details">
                        <div class="name"><%= usuario.getNome() %></div>
                        <div class="role">Microempreendedor</div>
                    </div>
                </div>
            </div>
            </div>
       
    


            <!-- ================================================
                 CONTENT
                 ================================================ -->
            <div class="content">

                <!-- ALERTAS (mensagens do backend) -->
                <% if (mensagemSucesso != null) { %>
                    <div class="alert alert-success">
                        ✓ <%= mensagemSucesso %>
                    </div>
                <% } %>

                <% if (mensagemErro != null) { %>
                    <div class="alert alert-error">
                        ✕ <%= mensagemErro %>
                    </div>
                <% } %>

                <!-- ========================================
                     CARD 1: GERAR RELATÓRIO
                     ======================================== -->
                <div class="card">
                    <h3>📥 Gerar Relatório Mensal</h3>
                    <p>
                        Selecione o mês e ano para gerar o relatório de receitas em formato PDF.
                        O arquivo será baixado automaticamente.
                    </p>

                    <!-- FORMULÁRIO GERAR PDF -->
                    <form method="POST" action="${pageContext.request.contextPath}/relatorio">

                        <div class="form-row">
                            <!-- MÊS -->
                            <div class="form-group">
                                <label>Mês *</label>
                                <select name="mes" required>
                                    <option value="">Selecione...</option>
                                    <option value="1" <%= mesAtual == 1 ? "selected" : "" %>>Janeiro</option>
                                    <option value="2" <%= mesAtual == 2 ? "selected" : "" %>>Fevereiro</option>
                                    <option value="3" <%= mesAtual == 3 ? "selected" : "" %>>Março</option>
                                    <option value="4" <%= mesAtual == 4 ? "selected" : "" %>>Abril</option>
                                    <option value="5" <%= mesAtual == 5 ? "selected" : "" %>>Maio</option>
                                    <option value="6" <%= mesAtual == 6 ? "selected" : "" %>>Junho</option>
                                    <option value="7" <%= mesAtual == 7 ? "selected" : "" %>>Julho</option>
                                    <option value="8" <%= mesAtual == 8 ? "selected" : "" %>>Agosto</option>
                                    <option value="9" <%= mesAtual == 9 ? "selected" : "" %>>Setembro</option>
                                    <option value="10" <%= mesAtual == 10 ? "selected" : "" %>>Outubro</option>
                                    <option value="11" <%= mesAtual == 11 ? "selected" : "" %>>Novembro</option>
                                    <option value="12" <%= mesAtual == 12 ? "selected" : "" %>>Dezembro</option>
                                </select>
                            </div>

                            <!-- ANO -->
                            <div class="form-group">
                                <label>Ano *</label>
                                <select name="ano" required>
                                    <option value="">Selecione...</option>
                                    <option value="2024">2024</option>
                                    <option value="2025">2025</option>
                                    <option value="2026" <%= anoAtual == 2026 ? "selected" : "" %>>2026</option>
                                    <option value="2027">2027</option>
                                </select>
                            </div>

                            <!-- BOTÃO -->
                            <div class="form-group">
                                <label>&nbsp;</label>
                                <button type="submit" class="btn btn-primary">
                                    📥 Gerar PDF
                                </button>
                            </div>
                        </div>
                    </form>

                    <!-- INFO BOX -->
                    <div class="info-box">
                        <h4>💡 Dica</h4>
                        <p>
                            O relatório é gerado com base nas vendas cadastradas no período selecionado.
                            Se nenhuma venda for encontrada, uma mensagem de erro será exibida.
                        </p>
                    </div>
                </div>

                <!-- ========================================
                     CARD 2: SOBRE O RELATÓRIO
                     ======================================== -->
                <div class="card">
                    <h3>ℹ️ Conteúdo do Relatório</h3>
                    <p>O PDF gerado contém as seguintes informações:</p>
                    <ul>
                        <li><strong>Cabeçalho:</strong> Nome, CPF, CNPJ (se cadastrado) e período</li>
                        <li><strong>Receitas por categoria:</strong> Revenda, Produtos Industrializados e Serviços</li>
                        <li><strong>Separação fiscal:</strong> Valores com e sem Nota Fiscal</li>
                        <li><strong>Totais:</strong> Parciais por categoria e total geral do período</li>
                        <li><strong>Detalhamento:</strong> Lista completa de todas as vendas do mês</li>
                    </ul>

                    <div class="info-box">
                        <h4>📋 Uso do Relatório</h4>
                        <p>
                            Este relatório pode ser usado para declarações fiscais (DASN-SIMEI),
                            comprovação de renda, controle financeiro ou envio à contabilidade.
                        </p>
                    </div>
                </div>

                <!-- ========================================
                     CARD 3: INSTRUÇÕES
                     ======================================== -->
                <div class="card">
                    <h3>📌 Como Usar</h3>
                    <p style="margin-bottom: 16px;">
                        <strong>Passo 1:</strong> Selecione o mês e ano desejado nos campos acima.
                    </p>
                    <p style="margin-bottom: 16px;">
                        <strong>Passo 2:</strong> Clique em "Gerar PDF".
                    </p>
                    <p style="margin-bottom: 16px;">
                        <strong>Passo 3:</strong> O download iniciará automaticamente.
                    </p>
                    <p style="margin-bottom: 0;">
                        <strong>Passo 4:</strong> Abra o PDF para visualizar ou imprimir.
                    </p>

                    <div class="info-box" style="margin-top: 20px;">
                        <h4>⚠️ Importante</h4>
                        <p>
                            Certifique-se de cadastrar todas as suas vendas antes de gerar o relatório.
                            Vendas não cadastradas não aparecerão no PDF.
                        </p>
                    </div>
                </div>

            </div><!-- /content -->
        </div><!-- /main-content -->
    </div><!-- /main-container -->

    <!-- ================================================
         JAVASCRIPT - Validações
         ================================================ -->
    <script>
        /* ================================================================
           VALIDAÇÃO DO FORMULÁRIO
           ================================================================

           Valida se mês e ano foram selecionados antes de enviar.
           Exibe alerta se campos estiverem vazios.
        */

        document.querySelector('form').addEventListener('submit', function(e) {
            const mes = document.querySelector('select[name="mes"]').value;
            const ano = document.querySelector('select[name="ano"]').value;

            // Validar mês
            if (!mes || mes === '') {
                e.preventDefault();
                alert('Por favor, selecione o mês!');
                return false;
            }

            // Validar ano
            if (!ano || ano === '') {
                e.preventDefault();
                alert('Por favor, selecione o ano!');
                return false;
            }

            // Confirmação
            const nomeMes = document.querySelector('select[name="mes"] option:checked').text;
            const confirmacao = confirm(
                'Gerar relatório de ' + nomeMes + '/' + ano + '?\n\n' +
                'O download do PDF iniciará automaticamente.'
            );

            if (!confirmacao) {
                e.preventDefault();
                return false;
            }
        });
    </script>
</body>
</html>

<%--
    ================================================================
    FLUXO COMPLETO DO RELATÓRIO
    ================================================================

    1. USUÁRIO ACESSA /relatorio (GET)
       → RelatorioController.doGet() (se houver)
       → Ou acessa direto relatorio.jsp
       → Exibe formulário

    2. USUÁRIO SELECIONA MÊS E ANO
       → Preenche campos do formulário
       → Clica "Gerar PDF"

    3. SUBMIT DO FORMULÁRIO (POST /relatorio)
       → RelatorioController.doPost()
       → Valida mês (1-12) e ano (numérico)
       → Busca vendas: VendasDAO.listarPorMesAno(userId, mes, ano)
       → Se vazio: erro "Nenhuma venda encontrada"
       → Se tem vendas: continua

    4. GERAÇÃO DO PDF
       → RelatorioPDF.calcularTotais(vendas)
       → Calcula por categoria + com/sem NF
       → RelatorioPDF.gerarRelatorio(usuario, mes, ano, vendas, totais)
       → Retorna byte[] do PDF

    5. CONFIGURAÇÃO DO RESPONSE
       → Content-Type: application/pdf
       → Content-Disposition: attachment; filename="relatorio_mei_2_2026.pdf"
       → Content-Length: [tamanho]

    6. ENVIO PARA DOWNLOAD
       → response.getOutputStream().write(pdfBytes)
       → Navegador inicia download
       → Usuário salva ou abre PDF

    ================================================================

    ESTRUTURA DO PDF GERADO:

    ┌──────────────────────────────────────────────┐
    │  RELATÓRIO MENSAL DE RECEITAS BRUTAS         │
    │  MICROEMPREENDEDOR INDIVIDUAL (MEI)          │
    │                                              │
    │  Nome: João Silva                            │
    │  CPF: 123.456.789-01                         │
    │  CNPJ: 12.345.678/0001-90                    │
    │  Período: Fevereiro/2026                     │
    ├──────────────────────────────────────────────┤
    │  RECEITAS DO MÊS                             │
    │                                              │
    │  Categoria                 Com NF    Sem NF  │
    │  I - Revenda              R$ 500    R$ 300   │
    │  II - Industrializados    R$ 0      R$ 200   │
    │  III - Serviços           R$ 1000   R$ 0     │
    │  ──────────────────────────────────────────  │
    │  TOTAL GERAL                      R$ 2.000   │
    ├──────────────────────────────────────────────┤
    │  DETALHAMENTO DAS VENDAS                     │
    │                                              │
    │  Data    Categoria  Descrição  NF    Valor  │
    │  01/02   Produtos   Venda X    S     R$ 100 │
    │  05/02   Serviços   Consult.   N     R$ 500 │
    │  ...                                         │
    └──────────────────────────────────────────────┘

    ================================================================
--%>
