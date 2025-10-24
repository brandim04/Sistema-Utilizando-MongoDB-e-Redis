package com.livraria.mongodbrediscrud;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.types.ObjectId;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Sistema de Gerenciamento de Livros
 * MongoDB Atlas + Redis (Contadores)
 * 
 * @author Brand
 */
public class SistemaLivros {
    private static final String MONGODB_URI = "mongodb+srv://brandim04:root@cluster0.lfa0bsy.mongodb.net/?appName=Cluster0";
    private static final String DATABASE_NAME = "LivrariaLarissa";
    private static final String COLLECTION_NAME = "livros";
    
    private static final String REDIS_HOST = "localhost";
    private static final int REDIS_PORT = 6379;
    
    private static MongoClient mongoClient;
    private static MongoDatabase database;
    private static MongoCollection<Document> collection;
    private static JedisPool jedisPool;
    private static Scanner scanner = new Scanner(System.in);
    
    public static void main(String[] args) {
        
        try {
            conectarMongoDB();
            conectarRedis();
            
            boolean executando = true;
            while (executando) {
                exibirMenu();
                int opcao = lerOpcao();
                
                switch (opcao) {
                    case 1 -> cadastrarLivro();
                    case 2 -> listarLivros();
                    case 3 -> buscarPorTitulo();
                    case 4 -> atualizarLivro();
                    case 5 -> excluirLivro();
                    case 6 -> exibirEstatisticas();
                    case 0 -> {
                        System.out.println("\n Encerrando sistema...");
                        executando = false;
                    }
                    default -> System.out.println("\n Opção inválida!");
                }
                
                if (executando) {
                    System.out.println("\nPressione ENTER para continuar...");
                    scanner.nextLine();
                }
            }
            
        } catch (Exception e) {
            System.err.println("Erro: " + e.getMessage());
            e.printStackTrace();
        } finally {
            fecharConexoes();
            scanner.close();
        }
    }
      
    private static void exibirMenu() {
        System.out.println("                  MENU PRINCIPAL");
        System.out.println("1. Cadastrar Livro");
        System.out.println("2. Listar Todos os Livros");
        System.out.println("3. Buscar Livro por Título");
        System.out.println("4. Atualizar Livro");
        System.out.println("5. Excluir Livro");
        System.out.println("6. Estatísticas do Sistema");
        System.out.println("0. Sair");
        System.out.println("\n");
        System.out.print("Escolha uma opção: ");
    }
    
    private static int lerOpcao() {
        try {
            int opcao = Integer.parseInt(scanner.nextLine());
            return opcao;
        } catch (NumberFormatException e) {
            return -1;
        }
    }
    
    private static void conectarMongoDB() {
        try {
            mongoClient = MongoClients.create(MONGODB_URI);
            database = mongoClient.getDatabase(DATABASE_NAME);
            collection = database.getCollection(COLLECTION_NAME);
            
            database.runCommand(new Document("ping", 1));
            
            System.out.println("Conectado ao MongoDB Atlas!");
        } catch (Exception e) {
            System.err.println("Erro ao conectar MongoDB: " + e.getMessage());
            System.err.println("Verifique sua string de conexão!");
            System.exit(1);
        }
    }
    
    private static void conectarRedis() {
        try {
            JedisPoolConfig poolConfig = new JedisPoolConfig();
            poolConfig.setMaxTotal(10);
            poolConfig.setMaxIdle(5);
            poolConfig.setMinIdle(1);
            
            jedisPool = new JedisPool(poolConfig, REDIS_HOST, REDIS_PORT);
            
            try (Jedis jedis = jedisPool.getResource()) {
                jedis.ping();
            }
            
            System.out.println("Conectado ao Redis!");
        } catch (Exception e) {
            System.err.println("Erro ao conectar Redis: " + e.getMessage());
            System.err.println("Certifique-se que o Redis está rodando!");
            System.err.println("   Execute: redis-server");
            System.exit(1);
        }
    }
    
    private static void fecharConexoes() {
        if (mongoClient != null) {
            mongoClient.close();
            System.out.println("MongoDB desconectado.");
        }
        if (jedisPool != null) {
            jedisPool.close();
            System.out.println("Redis desconectado.");
        }
    }
    
    private static void cadastrarLivro() {
        System.out.println("           CADASTRAR NOVO LIVRO                 ");
        
        try {
            System.out.print("\nTítulo: ");
            String titulo = scanner.nextLine().trim();
            
            if (titulo.isEmpty()) {
                System.out.println("Título não pode ser vazio!");
                return;
            }
            
            System.out.print("Autor: ");
            String autor = scanner.nextLine().trim();
            
            if (autor.isEmpty()) {
                System.out.println("Autor não pode ser vazio!");
                return;
            }
            
            System.out.print("Ano de Publicação: ");
            int anoPublicacao = Integer.parseInt(scanner.nextLine().trim());
            
            if (anoPublicacao < 1000 || anoPublicacao > 2025) {
                System.out.println("Ano inválido!");
                return;
            }
            
            System.out.print("Categoria: ");
            String categoria = scanner.nextLine().trim();
            
            if (categoria.isEmpty()) {
                System.out.println("Categoria não pode ser vazia!");
                return;
            }
            
            Livro livro = new Livro(titulo, autor, anoPublicacao, categoria);
            
            Document doc = livro.toDocument();
            collection.insertOne(doc);
            String id = doc.getObjectId("_id").toString();
            
            incrementarContador("total:cadastros");
            
            System.out.println("  Livro cadastrado com sucesso!                 ");
            
        } catch (NumberFormatException e) {
            System.err.println("\nErro: Digite um número válido para o ano!");
        } catch (Exception e) {
            System.err.println("\nErro ao cadastrar livro: " + e.getMessage());
        }
    }
        
    private static void listarLivros() {
        System.out.println("             LISTA DE LIVROS                   ");
        
        List<Livro> livros = new ArrayList<>();
        
        try (MongoCursor<Document> cursor = collection.find().iterator()) {
            while (cursor.hasNext()) {
                Document doc = cursor.next();
                livros.add(Livro.fromDocument(doc));
            }
        } catch (Exception e) {
            System.err.println("Erro ao listar livros: " + e.getMessage());
            return;
        }
        
        if (livros.isEmpty()) {
            System.out.println("\n Nenhum livro cadastrado.");
            return;
        }
        
        System.out.println("\nTotal de livros: " + livros.size());
        
        for (int i = 0; i < livros.size(); i++) {
            Livro livro = livros.get(i);
            System.out.println("\n" + (i + 1) + ". " + livro.getTitulo());
            System.out.println("   Autor: " + livro.getAutor());
            System.out.println("   Ano: " + livro.getAnoPublicacao());
            System.out.println("   ️Categoria: " + livro.getCategoria());
        }
        
        
        incrementarContador("total:consultas");
    }
    
    private static void buscarPorTitulo() {
        System.out.println("          BUSCAR LIVRO POR TÍTULO               ");
        
        System.out.print("\nDigite o título (ou parte dele): ");
        String titulo = scanner.nextLine().trim();
        
        if (titulo.isEmpty()) {
            System.out.println("Título não pode ser vazio!");
            return;
        }
        
        try {
            List<Livro> livros = new ArrayList<>();
            
            try (MongoCursor<Document> cursor = collection.find(
                    Filters.regex("titulo", titulo, "i")).iterator()) {
                while (cursor.hasNext()) {
                    livros.add(Livro.fromDocument(cursor.next()));
                }
            }
            
            if (livros.isEmpty()) {
                System.out.println("\nNenhum livro encontrado com o título: \"" + titulo + "\"");
                return;
            }
            
            System.out.println("\nLivros encontrados: " + livros.size());
            
            for (int i = 0; i < livros.size(); i++) {
                Livro livro = livros.get(i);
                System.out.println("\n" + (i + 1) + ". " + livro.getTitulo());
                System.out.println("   Autor: " + livro.getAutor());
                System.out.println("   Ano: " + livro.getAnoPublicacao());
                System.out.println("  Categoria: " + livro.getCategoria());
            }
            
            
        } catch (Exception e) {
            System.err.println("Erro ao buscar: " + e.getMessage());
        }
        
        incrementarContador("total:consultas");
    }
    
    private static void atualizarLivro() {
        System.out.println("║            ATUALIZAR LIVRO                   ");
        
        List<Livro> livros = new ArrayList<>();
        try (MongoCursor<Document> cursor = collection.find().iterator()) {
            while (cursor.hasNext()) {
                livros.add(Livro.fromDocument(cursor.next()));
            }
        } catch (Exception e) {
            System.err.println("Erro ao listar livros: " + e.getMessage());
            return;
        }
        
        if (livros.isEmpty()) {
            System.out.println("\nNenhum livro cadastrado.");
            return;
        }
        
        System.out.println("\nLivros cadastrados:");
        for (int i = 0; i < livros.size(); i++) {
            System.out.println((i + 1) + ". " + livros.get(i).getTitulo());
        }
        
        System.out.print("\nEscolha o número do livro para atualizar: ");
        
        try {
            int escolha = Integer.parseInt(scanner.nextLine().trim());
            
            if (escolha < 1 || escolha > livros.size()) {
                System.out.println("Opção inválida!");
                return;
            }
            
            Livro livro = livros.get(escolha - 1);
            
            System.out.println("\nLivro selecionado: " + livro.getTitulo());
            System.out.println("\nDeixe em branco para manter o valor atual:\n");
            
            System.out.print("Título [" + livro.getTitulo() + "]: ");
            String novoTitulo = scanner.nextLine().trim();
            
            System.out.print("Autor [" + livro.getAutor() + "]: ");
            String novoAutor = scanner.nextLine().trim();
            
            System.out.print("Ano [" + livro.getAnoPublicacao() + "]: ");
            String novoAnoStr = scanner.nextLine().trim();
            
            System.out.print("Categoria [" + livro.getCategoria() + "]: ");
            String novaCategoria = scanner.nextLine().trim();
            
            Document updateDoc = new Document();
            
            if (!novoTitulo.isEmpty()) {
                updateDoc.append("titulo", novoTitulo);
            }
            if (!novoAutor.isEmpty()) {
                updateDoc.append("autor", novoAutor);
            }
            if (!novoAnoStr.isEmpty()) {
                updateDoc.append("anoPublicacao", Integer.parseInt(novoAnoStr));
            }
            if (!novaCategoria.isEmpty()) {
                updateDoc.append("categoria", novaCategoria);
            }
            
            if (updateDoc.isEmpty()) {
                System.out.println("\n⚠️  Nenhum dado foi alterado.");
                return;
            }
            
            Document setDoc = new Document("$set", updateDoc);
            collection.updateOne(Filters.eq("_id", livro.getId()), setDoc);
            
            incrementarContador("total:atualizacoes");
            
            System.out.println("    Livro atualizado com sucesso!                 ");
            
        } catch (NumberFormatException e) {
            System.err.println("\nErro: Digite um número válido!");
        } catch (Exception e) {
            System.err.println("\nErro ao atualizar livro: " + e.getMessage());
        }
    }

    private static void excluirLivro() {
        System.out.println(" EXCLUIR LIVRO");
        
        List<Livro> livros = new ArrayList<>();
        try (MongoCursor<Document> cursor = collection.find().iterator()) {
            while (cursor.hasNext()) {
                livros.add(Livro.fromDocument(cursor.next()));
            }
        } catch (Exception e) {
            System.err.println("Erro ao listar livros: " + e.getMessage());
            return;
        }
        
        if (livros.isEmpty()) {
            System.out.println("\nNenhum livro cadastrado.");
            return;
        }
        
        System.out.println("\nLivros cadastrados:");
        for (int i = 0; i < livros.size(); i++) {
            System.out.println((i + 1) + ". " + livros.get(i).getTitulo());
        }
        
        System.out.print("\nEscolha o número do livro para excluir: ");
        
        try {
            int escolha = Integer.parseInt(scanner.nextLine().trim());
            
            if (escolha < 1 || escolha > livros.size()) {
                System.out.println("Opção inválida!");
                return;
            }
            
            Livro livro = livros.get(escolha - 1);
            
            System.out.println("\nLivro selecionado: " + livro.getTitulo());
            System.out.print("\nConfirma a exclusão? (S/N): ");
            String confirmacao = scanner.nextLine().trim().toUpperCase();
            
            if (confirmacao.equals("S")) {
                collection.deleteOne(Filters.eq("_id", livro.getId()));
                
                incrementarContador("total:exclusoes");
                
                System.out.println("Livro excluído com sucesso!                   ");
            } else {
                System.out.println("\nExclusão cancelada.");
            }
            
        } catch (NumberFormatException e) {
            System.err.println("\nErro: Digite um número válido!");
        } catch (Exception e) {
            System.err.println("\nErro ao excluir livro: " + e.getMessage());
        }
    }
        
    private static void exibirEstatisticas() {
        System.out.println("  ESTATÍSTICAS DO SISTEMA               ");
        
        try {
            long totalLivros = collection.countDocuments();
            
            long totalCadastros = obterContador("total:cadastros");
            long totalConsultas = obterContador("total:consultas");
            long totalAtualizacoes = obterContador("total:atualizacoes");
            long totalExclusoes = obterContador("total:exclusoes");
            
            System.out.println("\nBANCO DE DADOS (MongoDB Atlas):");
            System.out.println("   ├─ Livros cadastrados: " + totalLivros);
            
            System.out.println("\nCONTADORES DE OPERAÇÕES (Redis):");
            System.out.println("   ├─ Total de cadastros: " + totalCadastros);
            System.out.println("   ├─ Total de consultas: " + totalConsultas);
            System.out.println("   ├─ Total de atualizações: " + totalAtualizacoes);
            System.out.println("   └─ Total de exclusões: " + totalExclusoes);
            
            System.out.println("\nLIVROS POR CATEGORIA:");
            
            List<String> categorias = collection.distinct("categoria", String.class)
                    .into(new ArrayList<>());
            
            if (categorias.isEmpty()) {
                System.out.println("   └─ Nenhuma categoria cadastrada");
            } else {
                for (int i = 0; i < categorias.size(); i++) {
                    String categoria = categorias.get(i);
                    long count = collection.countDocuments(Filters.eq("categoria", categoria));
                    
                    if (i == categorias.size() - 1) {
                        System.out.println("   └─ " + categoria + ": " + count + " livro(s)");
                    } else {
                        System.out.println("   ├─ " + categoria + ": " + count + " livro(s)");
                    }
                }
            }
                        
        } catch (Exception e) {
            System.err.println("Erro ao obter estatísticas: " + e.getMessage());
        }
    }

    private static void incrementarContador(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.incr(key);
        } catch (Exception e) {
            System.err.println("Erro ao incrementar contador no Redis: " + e.getMessage());
        }
    }
    
    private static long obterContador(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            String value = jedis.get(key);
            return value != null ? Long.parseLong(value) : 0;
        } catch (Exception e) {
            System.err.println("Erro ao obter contador do Redis: " + e.getMessage());
            return 0;
        }
    }
}