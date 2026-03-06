# Sistema MEI — Gerador de Relatório Mensal de Receitas Brutas

Sistema web para Microempreendedores Individuais (MEI) que automatiza o cadastro de receitas mensais e a geração de relatórios em PDF, facilitando o cumprimento das obrigações fiscais.

---

## Funcionalidades

- Cadastro e autenticação de usuários MEI
- Registro de vendas por categoria (Revenda de Mercadorias, Produtos Industrializados, Prestação de Serviços)
- Separação entre receitas com e sem emissão de Nota Fiscal
- Cálculo automático de totais por categoria e total geral
- Histórico de vendas com filtros por ano, mês, categoria e status de NF
- Geração de relatório mensal em PDF para download
- Edição de perfil e alteração de senha

---

## Tecnologias

| Camada | Tecnologia |
|--------|-----------|
| Backend | Java (Jakarta EE) |
| Servidor | Apache Tomcat 10.1 |
| Banco de dados | MySQL |
| Frontend | JSP + HTML + CSS + JavaScript |
| Build | Maven |
| Criptografia | BCrypt (jBCrypt) |
| PDF | iText / RelatorioPDF |

---

## Estrutura do Projeto

```
src/main/
├── java/br/com/projeto/
│   ├── controller/       # Servlets (rotas da aplicação)
│   ├── dao/              # Acesso ao banco de dados
│   ├── model/            # Entidades do sistema
│   ├── test/             # Classes de teste de conexão
│   └── utils/            # Conexão e geração de PDF
└── webapp/
    ├── assets/           # CSS e JavaScript
    ├── pages/            # Páginas JSP
    └── WEB-INF/
        ├── lib/          # JARs externos
        └── web.xml       # Configuração do servlet container
```

---

## Pré-requisitos

- Java 17+
- Apache Tomcat 10.1
- MySQL 8+ ou MariaDB 10.4+
- Maven 3.8+
- Eclipse IDE (recomendado) ou IntelliJ IDEA

---

## Instalação e Configuração

### 1. Banco de dados

Execute o script SQL para criar o banco e as tabelas:

```bash
mysql -u root -p < MYSQL.sql
```

O script cria o banco `mei`, todas as tabelas, índices, foreign keys e dados iniciais.

### 2. Conexão com o banco

Edite o arquivo `src/main/java/br/com/projeto/utils/Conexao.java` com as suas credenciais:

```java
private static final String URL = "jdbc:mysql://localhost:3306/mei";
private static final String USER = "root";
private static final String PASSWORD = "sua_senha";
```

### 3. Build e deploy

Importe o projeto no Eclipse como **Maven Project**, configure o servidor Tomcat 10.1 e execute com **Run on Server**.

---

## Usuários de Teste

| CPF | Senha | Nome |
|-----|-------|------|
| 12345678910 | 123456 | teste da silva |
| 99999999999 | 123456 | Teste Login |
| 11111111111 | 123456 | teste |

---

## Rotas da Aplicação

| Rota | Método | Descrição |
|------|--------|-----------|
| `/login` | GET / POST | Tela de login e autenticação |
| `/cadastro` | GET / POST | Cadastro de novo usuário |
| `/logout` | GET | Encerramento de sessão |
| `/dashboard` | GET / POST | Painel principal e cadastro rápido de venda |
| `/venda` | GET / POST | CRUD completo de vendas |
| `/historico` | GET | Histórico com filtros |
| `/relatorio` | GET / POST | Seleção de período e download do PDF |
| `/perfil` | GET / POST | Edição de dados pessoais e senha |

---

## Modelo de Dados

```
usuario (1) ──< vendas >── categoria
                  │
            nota_fiscal
                  │
             pagamento >── metodo_pagamento
```

---

## Autor

Gabriel Conrado da Silva  
Laboratório de Programação I

