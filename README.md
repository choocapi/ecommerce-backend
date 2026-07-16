# ACB Computer — Backend

REST API server powering the ACB Computer ecommerce platform.

## Diagrams
<img width="633" height="520" alt="Picture1" src="https://github.com/user-attachments/assets/dfd842cd-98a1-4152-96bb-51e348c83397" />


## Features

- **Payment processing:** VNPay, MoMo, ZaloPay webhook handling
- **Multi-method auth:** Email/password, Google, Facebook via JWT
- **AI consultation:** Spring AI integration for product advisor
- **Notifications:** Transactional email via Resend
- **Secure & fast:** Role-based security, optimized data access

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Framework | Spring MVC |
| Security | Spring Security, JWT |
| AI | Spring AI |
| ORM | Hibernate |
| Database | PostgreSQL |
| Infra | Docker, Ubuntu 22.04 LTS |
| Email | Resend |

## Getting Started

```bash
git clone https://github.com/choocapi/ecommerce-backend.git
cd ecommerce-backend

# You must create your own .env file, for attribute, look in resources/application.yaml

# Database container
docker compose up -d

# For run project
./mvnw spring-boot:run

# For build project
./mvnw clean package -DskipTests
```

## Project Navigation

This is part of a 3-repo project:

| Repo | Description |
|------|-------------|
| [Storefront](https://github.com/choocapi/ecommerce-storefront) | Customer-facing store |
| [Dashboard](https://github.com/choocapi/ecommerce-dashboard) | Admin & staff management panel |
| ▶ **Backend** (this repo) | API server |
