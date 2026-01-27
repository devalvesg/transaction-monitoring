# Transaction Monitoring System

<div align="center">

![Java](https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.3.5-green?style=for-the-badge&logo=springboot)
![Apache Kafka](https://img.shields.io/badge/Kafka-7.5.0-black?style=for-the-badge&logo=apachekafka)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue?style=for-the-badge&logo=postgresql)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?style=for-the-badge&logo=docker)

**Plataforma de detecção de fraudes em tempo real para transações de criptomoedas e pagamentos**

[Como Iniciar](#-como-iniciar) |
[Arquitetura](#-arquitetura) |
[API](#-endpoints-da-api) |
[Roadmap](#-roadmap)

</div>

---

## Sobre

Sistema de detecção de fraudes baseado em microserviços que analisa transações em tempo real usando regras configuráveis. A plataforma processa transações via Kafka, aplica 12 regras de detecção de fraude e sinaliza atividades suspeitas automaticamente.

### Principais Funcionalidades

- **Análise em Tempo Real** - Arquitetura orientada a eventos com Kafka
- **Regras Configuráveis** - 12 regras de detecção com pesos e limites ajustáveis
- **Blacklist Automática** - Bloqueia endereços que excedem o limite de risco
- **Design Escalável** - Microserviços independentes que escalam conforme demanda

---

## Arquitetura

```
                    +------------------+
                    |    REST API      |
                    |   (Porta 8080)   |
                    +--------+---------+
                             |
                             v
+----------------------------+----------------------------+
|                                                         |
|              TRANSACTION SERVICE                        |
|    Receber -> Validar -> Salvar -> Publicar Eventos     |
|                                                         |
+----------------------------+----------------------------+
                             |
                             v
                    +------------------+
                    |      KAFKA       |
                    |------------------|
                    | transactions-*   |
                    | fraud-alerts     |
                    +--------+---------+
                             |
                             v
+----------------------------+----------------------------+
|                                                         |
|            FRAUD ANALYSIS SERVICE                       |
|    Consumir -> Analisar -> Pontuar -> Alertar           |
|                                                         |
+----------------------------+----------------------------+
                             |
                             v
              +-----------------------------+
              |   Transação Atualizada com  |
              |   Flag de Fraude & Score    |
              +-----------------------------+
```

---

## Tecnologias utilizadas

| Categoria | Tecnologia |
|-----------|------------|
| **Linguagem** | Java 21 |
| **Framework** | Spring Boot 3.3.5, Spring Cloud 2023.0.3 |
| **Mensageria** | Apache Kafka (modo KRaft) |
| **Banco de Dados** | PostgreSQL 16 (DB separado por serviço) |
| **Migrations** | Flyway |
| **Mapeamento** | MapStruct 1.5.5 |
| **Métricas** | Micrometer + Prometheus |
| **Build** | Maven (multi-módulo) |
| **Containers** | Docker Compose |

---

## Como Iniciar

### Pré-requisitos

- Docker & Docker Compose
- Java 21 (para desenvolvimento local)
- Maven 3.8+

### Quick Start

```bash
# Clone o repositório
git clone https://github.com/seu-usuario/transaction-monitoring.git
cd transaction-monitoring

# Inicie todos os serviços
docker-compose up -d

# Verifique o status
docker-compose ps
```

### URLs dos Serviços

| Serviço | URL |
|---------|-----|
| Transaction API | http://localhost:8080 |
| Fraud Analysis API | http://localhost:8081 |
| Kafka UI | http://localhost:8090 |
| Grafana | http://localhost:3000 |
| Prometheus | http://localhost:9090 |

### Crie Sua Primeira Transação

```bash
curl -X POST http://localhost:8080/transaction \
  -H "Content-Type: application/json" \
  -d '{
    "transactionId": "TXN-001",
    "fromAddress": "0xABC123",
    "toAddress": "0xDEF456",
    "amount": 15000.00,
    "currency": "ETH",
    "paymentNetwork": "ETHEREUM",
    "label": "Transação de teste"
  }'
```

---

## Estrutura do Projeto

```
transaction-monitoring/
├── transaction-service/       # CRUD de transações & publicação de eventos
│   ├── domain/               # Entidades, enums, contratos
│   ├── application/          # Lógica de negócio
│   └── adapters/             # REST, Kafka, persistência
│
├── fraud-analysis-service/    # Detecção de fraudes & cálculo de risco
│   ├── domain/               # Regras, entidade de blacklist
│   ├── application/          # Motor de regras, serviço de análise
│   └── adapters/             # Consumers e producers Kafka
│
├── monitoring/                # Dashboards Grafana & config Prometheus
└── docker-compose.yml         # Deploy completo da stack
```

---

## Endpoints da API

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| `POST` | `/transaction` | Criar nova transação |
| `GET` | `/transaction/{id}` | Buscar transação por ID |
| `GET` | `/transaction` | Listar todas as transações |
| `PUT` | `/transaction/{id}` | Atualizar transação |
| `GET` | `/transaction/search/flagged-as-fraud` | Listar transações fraudulentas |
| `GET` | `/transaction/search/by-status?status={status}` | Filtrar por status |

---

## Regras de Detecção de Fraude

| Regra | Pontos | Descrição |
|-------|--------|-----------|
| ADDRESS_IN_BLACKLIST | 100 | Endereço está na blacklist |
| SUSPICIOUS_LABELS | 60 | Contém "mix", "bot", "scam" |
| HIGH_VALUE_AT_NIGHT | 40 | Alto valor entre 00:00-05:00 UTC |
| NEW_ADDRESS_HIGH_VALUE | 35 | Endereço novo (<5 txs) com valor alto |
| RANDOM_LABEL | 25 | Labels sem sentido ("aaa", "xxx") |
| PENDING_TOO_LONG | 20 | Pendente há mais de 48 horas |
| FIRST_TIME_TRANSACTION | 15 | Primeira transação do endereço |

> Transações com pontuação total >= 100 são sinalizadas como fraude.

---

## Monitoramento & Dashboards

O sistema inclui um dashboard completo no **Grafana** para monitoramento em tempo real. Acesse em http://localhost:3000.

### Painéis Disponíveis

```
┌─────────────────────────────────────────────────────────────────────┐
│                     TRANSACTION OVERVIEW                            │
├─────────────────────┬─────────────────────┬─────────────────────────┤
│  Total Transactions │  Transaction Rate   │  Pending Transactions   │
│      (counter)      │    (over time)      │        (gauge)          │
├─────────────────────┴─────────────────────┴─────────────────────────┤
│                      FRAUD DETECTION                                │
├─────────────────────┬─────────────────────┬─────────────────────────┤
│   Fraud Detected    │  Detection Rate %   │   Alerts Received       │
│      (total)        │      (gauge)        │      (counter)          │
├─────────────────────┴─────────────────────┴─────────────────────────┤
│                      FRAUD ANALYSIS                                 │
├─────────────────────┬─────────────────────┬─────────────────────────┤
│ Analyses Completed  │  Clean vs Fraud     │   Blacklist Size        │
│     (counter)       │    (pie chart)      │      (gauge)            │
├─────────────────────┴─────────────────────┴─────────────────────────┤
│              TOP 10 BLACKLISTED ADDRESSES (table)                   │
├─────────────────────────────────────────────────────────────────────┤
│                      TOP FRAUD RULES                                │
├────────────────────────────────┬────────────────────────────────────┤
│   Top 5 Rules Triggered        │    Rules Over Time                 │
│       (bar chart)              │      (time series)                 │
├────────────────────────────────┴────────────────────────────────────┤
│                      SYSTEM HEALTH                                  │
├─────────────────────┬─────────────────────┬─────────────────────────┤
│ Transaction Service │ Fraud Analysis Svc  │    JVM Heap Usage       │
│     (UP/DOWN)       │     (UP/DOWN)       │    (time series)        │
└─────────────────────┴─────────────────────┴─────────────────────────┘
```

### Métricas Coletadas

| Métrica | Descrição |
|---------|-----------|
| `transactions_database` | Total de transações no banco |
| `transactions_pending_count` | Transações aguardando processamento |
| `fraud_detected_total` | Total de fraudes detectadas |
| `fraud_rules_triggered_total` | Contador por regra de fraude acionada |
| `blacklist_size_current` | Quantidade de endereços na blacklist |
| `fraud_analysis_completed_total` | Análises de fraude concluídas |
| `jvm_memory_used_bytes` | Uso de memória heap da JVM |

---

## Roadmap

### Fase Atual
- [x] Detecção de fraudes com 7 regras implementadas
- [x] Integração com Kafka para processamento em tempo real
- [x] Mecanismo de blacklist automático
- [x] Dashboards Grafana & métricas Prometheus

### Próximos Passos
- [ ] **Regras baseadas em histórico** - Análise de padrões com histórico de transações
- [ ] **Integração com APIs externas** - Stripe, PayPal, blockchain explorers
- [ ] **Notificações** - Alertas via webhook para transações sinalizadas
- [ ] **Dashboard UI** - Interface web para monitoramento

---

## Desenvolvimento

```bash
# Build de todos os módulos
mvn clean install

# Executar testes
mvn test

# Build de serviço específico
mvn clean install -pl transaction-service

# Ver logs
docker logs -f transaction-service
docker logs -f fraud-analysis-service
```

---

## Licença

Este projeto está licenciado sob a MIT License.

---

<div align="center">

Feito com Java e Kafka

</div>
