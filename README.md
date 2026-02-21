# RestaurantMenuJava

A small Java project for learning object-oriented programming, collections/streams, JSON parsing, JavaFX UI, and basic database persistence with JPA/Hibernate.

## Why this project exists

This repository is primarily a **learning sandbox**. It combines multiple Java topics in one practical example: a restaurant menu and ordering system.

## What is implemented

- **Domain models** for products, pizzas, drinks, users, tables, and orders.
- **Menu loading from JSON** (`config.json`) and export back to JSON.
- **Console ordering flow** with role-style behavior (owner/waiter/client).
- **JavaFX interface** with Guest / Staff / Admin access paths.
- **Persistence layer** with repositories and JPA/Hibernate entities.
- **PostgreSQL integration** through `persistence.xml`.


## Notes for learning

This project is useful for practicing:

- inheritance & polymorphism (`Produs`, `Pizza`, `Mancare`, `Bautura`)
- builder pattern (`Pizza.Builder`)
- lambda expressions and simple discount strategies
- streams (`filter`, `sorted`, `anyMatch`, etc.)
- layered architecture (entities/repositories/services/UI)
- JPA entity mapping and basic CRUD workflows
