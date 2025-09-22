package com.Vakta.Vakta_Chat_Bot.Model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.util.UUID;

@Entity
public class VaktaChatBot {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    private String name;
    private String qdrantCollectionName;

    // JPA requires a no-argument constructor
    public VaktaChatBot() {}

    public VaktaChatBot(String name, String qdrantCollectionName) {
        this.name = name;
        this.qdrantCollectionName = qdrantCollectionName;
    }

    // Getters
    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getQdrantCollectionName() { return qdrantCollectionName; }

    // Setters
    public void setId(UUID id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setQdrantCollectionName(String qdrantCollectionName) { this.qdrantCollectionName = qdrantCollectionName; }
}
