# MongoDB Shell Commands Guide

This guide covers MongoDB commands, starting from basic operations to advanced topics like the Aggregation Framework. All commands are designed for use in the MongoDB shell (`mongo`).

---

## Table of Contents

1. [Basic Commands](#basic-commands)
2. [CRUD Operations](#crud-operations)
3. [Indexing](#indexing)
4. [Aggregation Framework](#aggregation-framework)
5. [Data Modeling](#data-modeling)
6. [Geospatial Queries](#geospatial-queries)
7. [Backup and Restore](#backup-and-restore)
8. [User Management](#user-management)
9. [Replication](#replication)
10. [Sharding](#sharding)
11. [Advanced Topics](#advanced-topics)

---

## Basic Commands

### Show Databases
```bash
show dbs
```
### Use Database
```bash
use <database_name>
```
### Show Collections
```bash
show collections
```
### Create a Collection
```bash
db.createCollection('<collection_name>')
```
## CRUD Operations
### Insert Document
```bash
db.<collection_name>.insert({
  key1: "value1",
  key2: "value2"
})
```
### Find Document
```bash
db.<collection_name>.find({ key1: "value1" })
```
### Find One Document
```bash
db.<collection_name>.findOne({ key1: "value1" })
```
### Update Document
```bash
db.<collection_name>.update(
  { key1: "value1" },
  { $set: { key2: "new_value" } }
)
```