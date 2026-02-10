-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Tempo de geração: 28/01/2026 às 04:00
-- Versão do servidor: 10.4.32-MariaDB
-- Versão do PHP: 8.0.30

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Banco de dados: `mei`
--

-- --------------------------------------------------------

--
-- Estrutura para tabela `categoria`
--

CREATE TABLE `categoria` (
  `id_categoria` int(11) NOT NULL,
  `Nome_categoria` varchar(100) NOT NULL,
  `ativo` tinyint(1) DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Despejando dados para a tabela `categoria`
--

INSERT INTO `categoria` (`id_categoria`, `Nome_categoria`, `ativo`) VALUES
(1, 'Revenda de Mercadorias', 1),
(2, 'Produtos Industrializados', 1),
(3, 'Prestação de Serviços', 1);

-- --------------------------------------------------------

--
-- Estrutura para tabela `metpag`
--

CREATE TABLE `metpag` (
  `id_MetPag` int(11) NOT NULL,
  `Descricao` varchar(100) NOT NULL,
  `ativo` tinyint(1) DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Despejando dados para a tabela `metpag`
--

INSERT INTO `metpag` (`id_MetPag`, `Descricao`, `ativo`) VALUES
(1, 'Dinheiro', 1),
(2, 'Cartão de Crédito', 1),
(3, 'Cartão de Débito', 1),
(4, 'PIX', 1),
(5, 'Transferência Bancária', 1),
(6, 'Boleto', 1);

-- --------------------------------------------------------

--
-- Estrutura para tabela `notafiscal`
--

CREATE TABLE `notafiscal` (
  `id_NotaFiscal` int(11) NOT NULL,
  `numero` varchar(50) NOT NULL,
  `dataEmissao` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `valor` float NOT NULL,
  `vendas_id` int(11) NOT NULL,
  `usuario_id` int(11) NOT NULL,
  `ativo` tinyint(1) DEFAULT 1,
  `data_criacao` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Estrutura para tabela `pagamento`
--

CREATE TABLE `pagamento` (
  `id_pag` int(11) NOT NULL,
  `Vendas_id_Vendas` int(11) NOT NULL,
  `MetPag_id_MetPag` int(11) NOT NULL,
  `Valor` float NOT NULL,
  `data_pagamento` timestamp NOT NULL DEFAULT current_timestamp(),
  `ativo` tinyint(1) DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Estrutura para tabela `usuario`
--

CREATE TABLE `usuario` (
  `id_usuario` int(11) NOT NULL,
  `cpf` varchar(11) NOT NULL,
  `nome` varchar(100) NOT NULL,
  `email` varchar(100) NOT NULL,
  `senha` varchar(255) NOT NULL,
  `data_cadastro` timestamp NOT NULL DEFAULT current_timestamp(),
  `ativo` tinyint(1) DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Despejando dados para a tabela `usuario`
--

INSERT INTO `usuario` (`id_usuario`, `cpf`, `nome`, `email`, `senha`, `data_cadastro`, `ativo`) VALUES
(2, '99999999999', 'Teste Login', 'teste@login.com', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5gyUI/rYs8K8i', '2026-01-26 23:08:17', 1),
(3, '88888888888', 'teste again', 'cadastro@teste.com', '$2a$12$iZ4az9TWEOYrvKdrVtekp.s1Evz6kp1o5i9p.xEFUIciW0sVw.N6W', '2026-01-26 23:27:16', 1),
(4, '77777777777', 'mais um teste', 'testemaisum@teste.com', '$2a$12$M6n/DQqUDhWVnXkQsAymdOxwvzNM.Za7dMOYlsjgix2haoK/4NaHm', '2026-01-26 23:40:40', 1);

-- --------------------------------------------------------

--
-- Estrutura para tabela `vendas`
--

CREATE TABLE `vendas` (
  `id_Vendas` int(11) NOT NULL,
  `Data_Vendas` timestamp NOT NULL DEFAULT current_timestamp(),
  `Valor` float NOT NULL,
  `NotaFiscalEmitida` char(1) DEFAULT 'N' CHECK (`NotaFiscalEmitida` in ('S','N')),
  `Categoria` int(11) NOT NULL,
  `usuario_id` int(11) NOT NULL,
  `descricao` text DEFAULT NULL,
  `ativo` tinyint(1) DEFAULT 1,
  `data_criacao` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Índices para tabelas despejadas
--

--
-- Índices de tabela `categoria`
--
ALTER TABLE `categoria`
  ADD PRIMARY KEY (`id_categoria`),
  ADD KEY `idx_nome` (`Nome_categoria`);

--
-- Índices de tabela `metpag`
--
ALTER TABLE `metpag`
  ADD PRIMARY KEY (`id_MetPag`);

--
-- Índices de tabela `notafiscal`
--
ALTER TABLE `notafiscal`
  ADD PRIMARY KEY (`id_NotaFiscal`),
  ADD KEY `idx_numero` (`numero`),
  ADD KEY `idx_vendas` (`vendas_id`),
  ADD KEY `idx_usuario` (`usuario_id`);

--
-- Índices de tabela `pagamento`
--
ALTER TABLE `pagamento`
  ADD PRIMARY KEY (`id_pag`),
  ADD KEY `idx_vendas` (`Vendas_id_Vendas`),
  ADD KEY `idx_metpag` (`MetPag_id_MetPag`),
  ADD KEY `idx_data` (`data_pagamento`);

--
-- Índices de tabela `usuario`
--
ALTER TABLE `usuario`
  ADD PRIMARY KEY (`id_usuario`),
  ADD UNIQUE KEY `cpf` (`cpf`),
  ADD KEY `idx_cpf` (`cpf`);

--
-- Índices de tabela `vendas`
--
ALTER TABLE `vendas`
  ADD PRIMARY KEY (`id_Vendas`),
  ADD KEY `idx_usuario` (`usuario_id`),
  ADD KEY `idx_data` (`Data_Vendas`),
  ADD KEY `idx_categoria` (`Categoria`);

--
-- AUTO_INCREMENT para tabelas despejadas
--

--
-- AUTO_INCREMENT de tabela `categoria`
--
ALTER TABLE `categoria`
  MODIFY `id_categoria` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT de tabela `metpag`
--
ALTER TABLE `metpag`
  MODIFY `id_MetPag` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;

--
-- AUTO_INCREMENT de tabela `notafiscal`
--
ALTER TABLE `notafiscal`
  MODIFY `id_NotaFiscal` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de tabela `pagamento`
--
ALTER TABLE `pagamento`
  MODIFY `id_pag` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de tabela `usuario`
--
ALTER TABLE `usuario`
  MODIFY `id_usuario` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT de tabela `vendas`
--
ALTER TABLE `vendas`
  MODIFY `id_Vendas` int(11) NOT NULL AUTO_INCREMENT;

--
-- Restrições para tabelas despejadas
--

--
-- Restrições para tabelas `notafiscal`
--
ALTER TABLE `notafiscal`
  ADD CONSTRAINT `notafiscal_ibfk_1` FOREIGN KEY (`vendas_id`) REFERENCES `vendas` (`id_Vendas`) ON DELETE CASCADE,
  ADD CONSTRAINT `notafiscal_ibfk_2` FOREIGN KEY (`usuario_id`) REFERENCES `usuario` (`id_usuario`) ON DELETE CASCADE;

--
-- Restrições para tabelas `pagamento`
--
ALTER TABLE `pagamento`
  ADD CONSTRAINT `pagamento_ibfk_1` FOREIGN KEY (`Vendas_id_Vendas`) REFERENCES `vendas` (`id_Vendas`) ON DELETE CASCADE,
  ADD CONSTRAINT `pagamento_ibfk_2` FOREIGN KEY (`MetPag_id_MetPag`) REFERENCES `metpag` (`id_MetPag`);

--
-- Restrições para tabelas `vendas`
--
ALTER TABLE `vendas`
  ADD CONSTRAINT `vendas_ibfk_1` FOREIGN KEY (`Categoria`) REFERENCES `categoria` (`id_categoria`),
  ADD CONSTRAINT `vendas_ibfk_2` FOREIGN KEY (`usuario_id`) REFERENCES `usuario` (`id_usuario`) ON DELETE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
