📊 SISTEMA MEI - Gerador de Relatório Mensal de Receitas Brutas
📋 Sobre o Projeto
Sistema web desenvolvido em Java para microempreendedores individuais (MEI) automatizar a geração do relatório mensal de receitas brutas conforme exigido pelo modelo oficial.

Status do Projeto: 🟡 Em Desenvolvimento (Fase Inicial)

🎯 Funcionalidades Implementadas
✅ CONCLUÍDO
Autenticação de Usuários

Cadastro de novos MEIs

Login com CPF e senha

Hash de senhas com BCrypt

Sessões de usuário

Infraestrutura

Arquitetura MVC (Model-View-Controller)

Conexão com banco MySQL

DAOs para persistência

Páginas JSP com Bootstrap

🚧 EM DESENVOLVIMENTO
Dashboard principal

Cadastro de vendas

Geração de relatório PDF

Histórico de relatórios

📅 PLANEJADO
Cálculo automático de impostos (DAS)

Exportação para Excel

Notificações de vencimento

Módulo de clientes

🛠️ Tecnologias Utilizadas
Backend
Java 8+

Servlets & JSP

Apache Tomcat 9.0

MySQL 8.0

JDBC para conexão com banco

Frontend
HTML5, CSS3, JavaScript

Bootstrap 5 (planejado)

JSP (JavaServer Pages)

Chart.js (planejado)

Bibliotecas
BCrypt (hash de senhas)

iText PDF (geração de relatórios)

MySQL Connector/J

Ferramentas de Desenvolvimento
Eclipse IDE

XAMPP (MySQL + phpMyAdmin)

Git para controle de versão

📁 Estrutura do Projeto
text
ProjetoLab1.2/
├── 📂 src/br/com/projeto/
│   ├── 📂 controller/        # Controladores MVC
│   │   ├── LoginController.java
│   │   ├── DashboardController.java
│   │   ├── VendaController.java
│   │   └── RelatorioController.java
│   │
│   ├── 📂 dao/              # Data Access Objects
│   │   ├── UsuarioDAO.java
│   │   ├── VendasDAO.java
│   │   ├── CategoriaDAO.java
│   │   ├── MetPagDAO.java
│   │   ├── PagamentoDAO.java
│   │   └── NotaFiscalDAO.java
│   │
│   ├── 📂 model/            # Entidades/Models
│   │   ├── Usuario.java
│   │   ├── Vendas.java
│   │   ├── Categoria.java
│   │   ├── MetPag.java
│   │   ├── Pagamento.java
│   │   └── NotaFiscal.java
│   │
│   └── 📂 utils/            # Utilitários
│       ├── Conexao.java
│       └── RelatorioPDF.java
│
├── 📂 WebContent/           # Recursos Web
│   ├── login.jsp            # Tela de login/cadastro
│   ├── dashboard.jsp        # Painel principal (a criar)
│   ├── cadastro-venda.jsp   # Cadastro de vendas (a criar)
│   ├── relatorio.jsp        # Geração de relatório (a criar)
│   │
│   ├── 📂 WEB-INF/
│   │   ├── web.xml          # Configuração do projeto
│   │   └── 📂 lib/          # Bibliotecas JAR
│   │       ├── mysql-connector-java-8.0.33.jar
│   │       ├── jbcrypt-0.4.jar
│   │       └── itextpdf-5.5.13.3.jar
│   │
│   ├── 📂 css/              # Estilos (a criar)
│   └── 📂 js/               # JavaScript (a criar)
│
└── 📄 README.md             # Este arquivo
🗄️ Modelo de Banco de Dados
Tabelas Principais
sql
-- Usuários do sistema
CREATE TABLE Usuario (
    id_usuario INT AUTO_INCREMENT PRIMARY KEY,
    cpf VARCHAR(14) UNIQUE NOT NULL,
    nome VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL,
    senha VARCHAR(255) NOT NULL  -- Hash BCrypt
);

-- Categorias de receita
CREATE TABLE Categoria (
    id_categoria INT AUTO_INCREMENT PRIMARY KEY,
    nome_categoria VARCHAR(45) NOT NULL,
    tipo ENUM('REVENDA', 'INDUSTRIALIZADOS', 'SERVICOS') NOT NULL
);

-- Vendas/Receitas
CREATE TABLE Vendas (
    id_vendas INT AUTO_INCREMENT PRIMARY KEY,
    data_vendas DATE NOT NULL,
    valor DECIMAL(10,2) NOT NULL,
    nota_fiscal_emitida ENUM('S', 'N') DEFAULT 'N',
    categoria_id INT NOT NULL,
    usuario_id INT NOT NULL,
    FOREIGN KEY (categoria_id) REFERENCES Categoria(id_categoria),
    FOREIGN KEY (usuario_id) REFERENCES Usuario(id_usuario)
);
🚀 Como Executar o Projeto
Pré-requisitos
Java JDK 8 ou superior

Eclipse IDE for Enterprise Java Developers

Apache Tomcat 9.0+

MySQL 8.0+

XAMPP ou WAMP (opcional)

Configuração do Ambiente
1. Banco de Dados
sql
CREATE DATABASE MEI;
USE MEI;

-- Execute o script SQL completo fornecido em /database/
2. Configuração no Eclipse
bash
1. File → Import → Existing Projects into Workspace
2. Selecione a pasta do projeto
3. Configure Tomcat: Window → Preferences → Server → Runtime Environments
4. Adicione os JARs em WebContent/WEB-INF/lib/
3. Configuração da Conexão
Edite src/br/com/projeto/utils/Conexao.java:

java
private static final String URL = "jdbc:mysql://localhost:3306/MEI";
private static final String USER = "root";      // Seu usuário MySQL
private static final String PASSWORD = "";      // Sua senha MySQL
Execução
text
1. Clique direito no projeto → Run As → Run on Server
2. Selecione Tomcat v9.0
3. Acesse: http://localhost:8080/ProjetoLab1.2/
📊 Fluxo de Funcionalidades
Cadastro de Usuário
text
1. Acesse http://localhost:8080/ProjetoLab1.2/
2. Clique em "CADASTRAR"
3. Preencha: CPF, Nome, Email, Senha
4. Sistema valida CPF e cria hash da senha
5. Usuário é salvo no banco de dados
Login
text
1. Na tela inicial, digite CPF e senha
2. Sistema verifica no banco
3. Cria sessão para usuário autenticado
4. Redireciona para dashboard
Cadastro de Venda (em desenvolvimento)
text
1. No dashboard, acesse "Nova Venda"
2. Selecione categoria (Revenda/Industrializados/Serviços)
3. Informe valor e se emitiu nota fiscal
4. Sistema calcula totais automaticamente
Geração de Relatório (em desenvolvimento)
text
1. Selecione período (mês/ano)
2. Sistema calcula totais por categoria
3. Separa receitas com/sem nota fiscal
4. Gera PDF no formato oficial MEI
🔧 Configuração de Desenvolvimento
Dependências (JARs Necessários)
Biblioteca	Versão	Finalidade
mysql-connector-java	8.0.33	Conexão com MySQL
jbcrypt	0.4	Hash de senhas
itextpdf	5.5.13.3	Geração de PDF
Configuração do Eclipse
xml
<!-- web.xml mínimo -->
<web-app version="4.0" xmlns="http://xmlns.jcp.org/xml/ns/javaee">
    <welcome-file-list>
        <welcome-file>login.jsp</welcome-file>
    </welcome-file-list>
</web-app>
Configuração do Tomcat
text
Porta: 8080
Context Path: /ProjetoLab1.2
Deployment: war exploded
🧪 Testes Realizados
Testes de Funcionalidade
Acesso à página inicial

Formatação automática de CPF

Cadastro de novo usuário

Hash BCrypt de senhas

Persistência no banco MySQL

Login com credenciais válidas

Validação de formulários

Tratamento de erros

Testes Técnicos
Conexão com banco de dados

Carregamento de drivers JDBC

Funcionamento de servlets

Sessões HTTP

Geração de PDF

📝 Padrões de Código
Convenções de Nomenclatura
java
// Classes: PascalCase
public class LoginController {}

// Métodos: camelCase
public void autenticarUsuario() {}

// Variáveis: camelCase
private String nomeUsuario;

// Constantes: UPPER_SNAKE_CASE
private static final String DATABASE_URL;
Estrutura de Packages
text
br.com.projeto.controller  # Controladores
br.com.projeto.dao         # Data Access Objects  
br.com.projeto.model       # Modelos/Entidades
br.com.projeto.utils       # Utilitários
br.com.projeto.test        # Testes
Tratamento de Exceções
java
try {
    // Código que pode falhar
    usuarioDAO.inserir(usuario);
} catch (SQLException e) {
    // Log e tratamento apropriado
    logger.error("Erro ao salvar usuário", e);
    throw new ServletException("Erro no sistema", e);
}
🐛 Problemas Conhecidos
Problemas Resolvidos
Driver MySQL não encontrado

Solução: Adicionar JAR na pasta WEB-INF/lib/

Erro 404 ao acessar página

Solução: Configurar welcome-file no web.xml

CPF não formatando automaticamente

Solução: Implementar JavaScript no frontend

Problemas em Aberto
Dashboard não implementado

Validação de campos no frontend

Mensagens de erro não estilizadas

Logout não implementado

📈 Próximas Etapas
Fase 1: Autenticação (ATUAL)
Tela de login/cadastro

Controller de autenticação

DAO de usuários

Validações de formulário

Recuperação de senha

Fase 2: Dashboard
Layout com menu lateral

Cards de resumo

Gráfico de receitas

Lista de últimas vendas

Fase 3: Cadastro de Vendas
Formulário de venda

Integração com nota fiscal

Cálculo de totais

Validações de negócio

Fase 4: Relatórios
Seleção de período

Cálculo de totais por categoria

Geração de PDF

Histórico de relatórios

👨‍💻 Desenvolvedor
Gabriel Conrado da Silva

Projeto: Sistema Gerador de Relatório Mensal de Receitas Brutas para MEI

Disciplina: Laboratório de Programação I

Instituição: [Sua Instituição]

Período: 2024

📞 Suporte
Canais de Ajuda
Issues do GitHub: Para reportar bugs

Documentação: Consulte este README

Email: [seu-email@instituicao.edu.br]

Solução de Problemas
Consulte a seção Problemas Conhecidos ou abra uma issue.

Última Atualização: Dezembro 2024
Versão: 0.1.0 (Beta Inicial)
Status: Em desenvolvimento ativo 🚧

📋 Checklist de Progresso
Infraestrutura
Projeto criado no Eclipse

Estrutura de pacotes definida

Banco de dados projetado

Conexão com MySQL configurada

Autenticação
Modelo Usuario criado

UsuarioDAO implementado

LoginController desenvolvido

Tela login.jsp estilizada

Hash BCrypt funcionando

Validações completas

Logout implementado

Frontend
Página de login responsiva

Formatação automática de CPF

Dashboard básico

Menu de navegação

Mensagens de feedback

Backend
Arquitetura MVC estabelecida

DAOs básicos criados

Controllers mapeados

Lógica de negócio

Tratamento de exceções

Próximos Passos Imediatos
Criar dashboard.jsp básico

Implementar logout

Adicionar validações no frontend

Criar formulário de cadastro de venda

Implementar sessões de usuário

<div align="center">
📊 SISTEMA MEI
Simplificando a vida do microempreendedor

</div>
FORMATAÇÃO PARA COPIAR E COLAR EM DOCUMENTO:

SISTEMA MEI - Gerador de Relatório Mensal de Receitas Brutas
Sobre o Projeto
Sistema web desenvolvido em Java para microempreendedores individuais (MEI) automatizar a geração do relatório mensal de receitas brutas conforme exigido pelo modelo oficial.

Status do Projeto: Em Desenvolvimento (Fase Inicial)

Funcionalidades Implementadas
CONCLUÍDO
Autenticação de Usuários

Cadastro de novos MEIs

Login com CPF e senha

Hash de senhas com BCrypt

Sessões de usuário

Infraestrutura

Arquitetura MVC (Model-View-Controller)

Conexão com banco MySQL

DAOs para persistência

Páginas JSP

EM DESENVOLVIMENTO
Dashboard principal

Cadastro de vendas

Geração de relatório PDF

Histórico de relatórios

Tecnologias Utilizadas
Backend: Java 8+, Servlets, JSP, Apache Tomcat 9.0, MySQL 8.0

Frontend: HTML5, CSS3, JavaScript, JSP

Bibliotecas: BCrypt, iText PDF, MySQL Connector/J

Ferramentas: Eclipse IDE, XAMPP, Git

Estrutura do Projeto
Projeto organizado em pacotes MVC:

controller/: Controladores da aplicação

dao/: Data Access Objects para banco de dados

model/: Entidades do sistema

utils/: Utilitários e helpers

Como Executar
Criar banco de dados MySQL "MEI"

Configurar conexão em Conexao.java

Adicionar JARs na pasta WEB-INF/lib/

Executar no Tomcat via Eclipse

Acessar: http://localhost:8080/ProjetoLab1.2/

Progresso Atual
O sistema atualmente possui:

Tela de login/cadastro funcional

Validação de CPF com máscara automática

Hash seguro de senhas com BCrypt

Persistência em banco MySQL

Arquitetura MVC implementada

Próximas Etapas
Implementar dashboard básico

Criar formulário de cadastro de vendas

Desenvolver geração de relatório PDF

Adicionar validações e tratamento de erros

Desenvolvedor
Gabriel Conrado da Silva
Projeto acadêmico para Laboratório de Programação I - 2024

Versão: 0.1.0 (Beta Inicial)
Última Atualização: Dezembro 2024

