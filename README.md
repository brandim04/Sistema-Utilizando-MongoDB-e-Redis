# 📚 MongoDB-Redis - Gerenciamento de Livros

Projeto Java simples para gerenciamento de livros, integrando **MongoDB Atlas** para persistência e **Redis** para rastrear métricas de uso em tempo real.

## 🏗️ Arquitetura

- **MongoDB**: Armazena os dados principais dos livros (título, autor, ano, categoria).  
- **Redis**: Contadores em memória para métricas de cadastro, consultas, atualizações e exclusões, garantindo alta velocidade e atomicidade.

## 🗄️ MongoDB

O MongoDB é utilizado como **fonte de verdade** do sistema. Ele armazena todas as informações essenciais dos livros e permite consultas rápidas e flexíveis.  

Principais funcionalidades:

- Inserir, atualizar, excluir e buscar livros  
- Contar total de livros e agrupar por categoria  
- Suporta consultas complexas caso o projeto evolua  

## ⚡ Uso do Redis

O Redis é utilizado para rastrear operações do usuário de forma rápida e confiável:

| Ação do Usuário     | Chave no Redis       | Comando Redis | Vantagem |
|--------------------|-------------------|---------------|----------|
| Cadastrar Livro     | `total:cadastros`  | `INCR`        | Contagem instantânea de novos livros |
| Listar/Buscar Livro | `total:consultas`  | `INCR`        | Medição do tráfego de leitura |
| Atualizar Livro     | `total:atualizacoes`| `INCR`       | Frequência de modificações |
| Excluir Livro       | `total:exclusoes`  | `INCR`        | Frequência de remoções |

**Por que usar Redis para contadores?**  
O comando `INCR` do Redis garante **atomicidade**, ou seja, incrementos concorrentes não causam perda de dados. Se 100 usuários cadastrarem livros ao mesmo tempo, o contador aumentará exatamente 100 vezes.

## 🖥️ Menu da Aplicação

1. Cadastrar Livro  
2. Listar Todos os Livros  
3. Buscar Livro por Título  
4. Atualizar Livro  
5. Excluir Livro  
6. Estatísticas do Sistema  
0. Sair  

## 📊 Estatísticas do Sistema

- **MongoDB**: Total de livros e contagem por categoria  
- **Redis**: Total de cadastros, consultas, atualizações e exclusões, atualizado em tempo real  


## 🎬 Vídeo de Explicação

Assista ao vídeo demonstrando o funcionamento do projeto:  
[Link do vídeo](https://youtu.be/Ezj56VZnJik)

