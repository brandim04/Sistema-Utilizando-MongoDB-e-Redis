/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.livraria.mongodbrediscrud;

import org.bson.Document;
import org.bson.types.ObjectId;
import java.util.Date;

/**
 *
 * @author brand
 */
public class Livro {
    private ObjectId id;
    private String titulo;
    private String autor;
    private Integer anoPublicacao;
    private String categoria;
    private Date dataCadastro;

    // Construtor vazio
    public Livro() {
        this.dataCadastro = new Date();
    }
    
    public Livro(String titulo, String autor, 
                 Integer anoPublicacao, String categoria) {
        this.titulo = titulo;
        this.autor = autor;
        this.anoPublicacao = anoPublicacao;
        this.categoria = categoria;
        this.dataCadastro = new Date();
    }
    
   public Document toDocument() {
        Document doc = new Document();
        if (id != null) {
            doc.append("_id", id);
        }
        doc.append("titulo", titulo)
           .append("autor", autor)
           .append("anoPublicacao", anoPublicacao)
           .append("categoria", categoria)
           .append("dataCadastro", dataCadastro);
        
        return doc;
    }
    
    public static Livro fromDocument(Document doc) {
        Livro livro = new Livro();
        livro.setId(doc.getObjectId("_id"));
        livro.setTitulo(doc.getString("titulo"));
        livro.setAutor(doc.getString("autor"));
        livro.setAnoPublicacao(doc.getInteger("anoPublicacao"));
        livro.setCategoria(doc.getString("categoria"));
        livro.setDataCadastro(doc.getDate("dataCadastro"));
        return livro;
    }
    
    public ObjectId getId() {
        return id;
    }
    
    public void setId(ObjectId id) {
        this.id = id;
    }
    
    public String getTitulo() {
        return titulo;
    }
    
    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }
    
    public String getAutor() {
        return autor;
    }
    
    public void setAutor(String autor) {
        this.autor = autor;
    }
    
    public Integer getAnoPublicacao() {
        return anoPublicacao;
    }
    
    public void setAnoPublicacao(Integer anoPublicacao) {
        this.anoPublicacao = anoPublicacao;
    }
    
    public String getCategoria() {
        return categoria;
    }
    
    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }
    
    public Date getDataCadastro() {
        return dataCadastro;
    }
    
    public void setDataCadastro(Date dataCadastro) {
        this.dataCadastro = dataCadastro;
    }
    
    @Override
    public String toString() {
        return "Livro{" +
                "titulo='" + titulo + '\'' +
                ", autor='" + autor + '\'' +
                ", ano=" + anoPublicacao +
                ", categoria='" + categoria + '\'' +
                '}';
    }
}