<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="br.com.projeto.model.Usuario" %>
<%@ page import="br.com.projeto.model.Vendas" %>
<%@ page import="br.com.projeto.model.Categoria" %>
<%@ page import="java.util.List" %>
<%@ page import="java.text.NumberFormat" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.util.Locale" %>

<%
    // Verificar se está logado
    Usuario usuario = (Usuario) session.getAttribute("usuario");
    if (usuario == null) {
        response.sendRedirect("login");
        return;
    }
    
    List<Categoria> categorias = (List<Categoria>) request.getAttribute("categorias");
    List<Vendas> ultimasVendas = (List<Vendas>) request.getAttribute("ultimasVendas");
    Double totalMes = (Double) request.getAttribute("totalMes");
    
    NumberFormat moeda = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
    SimpleDateFormat dataFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
%>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Dashboard - Sistema MEI</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body {
            font-family: 'Segoe UI', Tahoma, sans-serif;
            background: #f5f7fa;
        }
        .header {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 20px 40px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
        }
        .header h1 { font-size: 24px; }
        .header p { opacity: 0.9; margin-top: 5px; }
        .container {
            max-width: 1200px;
            margin: 30px auto;
            padding: 0 20px;
        }
        .card {
            background: white;
            border-radius: 10px;
            padding: 25px;
            margin-bottom: 20px;
            box-shadow: 0 2px 8px rgba(0,0,0,0.1);
        }
        .card h2 {
            color: #333;
            margin-bottom: 20px;
            font-size: 20px;
        }
        .stats {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
            gap: 20px;
            margin-bottom: 30px;
        }
        .stat-card {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 20px;
            border-radius: 10px;
            text-align: center;
        }
        .stat-card h3 { font-size: 14px; opacity: 0.9; margin-bottom: 10px; }
        .stat-card .value { font-size: 32px; font-weight: bold; }
        .form-group {
            margin-bottom: 15px;
        }
        label {
            display: block;
            margin-bottom: 5px;
            color: #555;
            font-weight: 600;
        }
        input, select, textarea {
            width: 100%;
            padding: 10px;
            border: 1px solid #ddd;
            border-radius: 5px;
            font-size: 14px;
        }
        button {
            background: #667eea;
            color: white;
            padding: 12px 24px;
            border: none;
            border-radius: 5px;
            cursor: pointer;
            font-size: 16px;
            font-weight: 600;
        }
        button:hover { background: #5568d3; }
        table {
            width: 100%;
            border-collapse: collapse;
        }
        th, td {
            padding: 12px;
            text-align: left;
            border-bottom: 1px solid #eee;
        }
        th {
            background: #f8f9fa;
            font-weight: 600;
            color: #555;
        }
        .logout {
            float: right;
            background: rgba(255,255,255,0.2);
            padding: 8px 16px;
            border-radius: 5px;
            text-decoration: none;
            color: white;
        }
        .alert {
            padding: 12px;
            border-radius: 5px;
            margin-bottom: 20px;
        }
        .alert.success {
            background: #d4edda;
            color: #155724;
            border: 1px solid #c3e6cb;
        }
        .alert.error {
            background: #f8d7da;
            color: #721c24;
            border: 1px solid #f5c6cb;
        }
    </style>
</head>
<body>
    <div class="header">
        <h1>🔐 Sistema MEI - Dashboard</h1>
        <p>Bem-vindo, <%= usuario.getNome() %>!</p>
        <a href="logout" class="logout">Sair</a>
    </div>
    
    <div class="container">
        <% if (session.getAttribute("sucesso") != null) { %>
            <div class="alert success">
                ✅ <%= session.getAttribute("sucesso") %>
                <% session.removeAttribute("sucesso"); %>
            </div>
        <% } %>
        
        <% if (session.getAttribute("erro") != null) { %>
            <div class="alert error">
                ⚠️ <%= session.getAttribute("erro") %>
                <% session.removeAttribute("erro"); %>
            </div>
        <% } %>
        
        <div class="stats">
            <div class="stat-card">
                <h3>Total do Mês</h3>
                <div class="value"><%= moeda.format(totalMes != null ? totalMes : 0) %></div>
            </div>
        </div>
        
        <div class="card">
            <h2>📝 Cadastrar Nova Venda</h2>
            <form action="venda" method="post">
                <div class="form-group">
                    <label>Categoria:</label>
                    <select name="categoria" required>
                        <option value="">Selecione...</option>
                        <% if (categorias != null) {
                            for (Categoria cat : categorias) { %>
                                <option value="<%= cat.getIdCategoria() %>"><%= cat.getNomeCategoria() %></option>
                        <%  }
                        } %>
                    </select>
                </div>
                
                <div class="form-group">
                    <label>Valor (R$):</label>
                    <input type="text" name="valor" placeholder="0,00" required>
                </div>
                
                <div class="form-group">
                    <label>Descrição:</label>
                    <textarea name="descricao" rows="3" placeholder="Descreva a venda..."></textarea>
                </div>
                
                <div class="form-group">
                    <label>Nota Fiscal Emitida?</label>
                    <select name="notaFiscal" id="nfSelect">
                        <option value="N">Não</option>
                        <option value="S">Sim</option>
                    </select>
                </div>
                
                <div id="nfFields" style="display: none;">
                    <div class="form-group">
                        <label>Número da Nota:</label>
                        <input type="text" name="numeroNota" placeholder="123456">
                    </div>
                    <div class="form-group">
                        <label>Data de Emissão:</label>
                        <input type="date" name="dataEmissaoNF">
                    </div>
                </div>
                
                <button type="submit">💾 Cadastrar Venda</button>
            </form>
        </div>
        
        <div class="card">
            <h2>📊 Últimas Vendas</h2>
            <table>
                <thead>
                    <tr>
                        <th>Data</th>
                        <th>Categoria</th>
                        <th>Valor</th>
                        <th>NF</th>
                        <th>Descrição</th>
                    </tr>
                </thead>
                <tbody>
                    <% if (ultimasVendas != null && !ultimasVendas.isEmpty()) {
                        for (Vendas v : ultimasVendas) { %>
                            <tr>
                                <td><%= dataFormat.format(v.getDataVendas()) %></td>
                                <td><%= v.getNomeCategoria() %></td>
                                <td><%= moeda.format(v.getValor()) %></td>
                                <td><%= v.getNotaFiscalEmitida() %></td>
                                <td><%= v.getDescricao() != null ? v.getDescricao() : "-" %></td>
                            </tr>
                    <%  }
                    } else { %>
                        <tr>
                            <td colspan="5" style="text-align: center; color: #999;">
                                Nenhuma venda cadastrada ainda.
                            </td>
                        </tr>
                    <% } %>
                </tbody>
            </table>
        </div>
        
        <div class="card">
            <h2>📄 Gerar Relatório</h2>
            <form action="relatorio" method="post">
                <div style="display: grid; grid-template-columns: 1fr 1fr 1fr; gap: 15px;">
                    <div class="form-group">
                        <label>Mês:</label>
                        <select name="mes" required>
                            <option value="1">Janeiro</option>
                            <option value="2">Fevereiro</option>
                            <option value="3">Março</option>
                            <option value="4">Abril</option>
                            <option value="5">Maio</option>
                            <option value="6">Junho</option>
                            <option value="7">Julho</option>
                            <option value="8">Agosto</option>
                            <option value="9">Setembro</option>
                            <option value="10">Outubro</option>
                            <option value="11">Novembro</option>
                            <option value="12">Dezembro</option>
                        </select>
                    </div>
                    <div class="form-group">
                        <label>Ano:</label>
                        <input type="number" name="ano" value="2026" min="2020" max="2030" required>
                    </div>
                    <div class="form-group">
                        <label>&nbsp;</label>
                        <button type="submit">📥 Gerar PDF</button>
                    </div>
                </div>
            </form>
        </div>
    </div>
    
    <script>
        document.getElementById('nfSelect').addEventListener('change', function() {
            document.getElementById('nfFields').style.display = 
                this.value === 'S' ? 'block' : 'none';
        });
    </script>
</body>
</html>