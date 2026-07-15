# Gestão Adega

Sistema de gestão para adega com frontend React/Vite, backend Quarkus, MySQL e Docker Compose.

## Rodar com Docker

```bash
docker compose up -d --build
```

Acesse:

- Frontend: http://localhost
- Backend health: http://localhost/api/health

O primeiro acesso deve ser feito em **Nova adega**, na tela de login. Esse cadastro cria a adega e o usuário gestor.

## Serviços

- `db`: MySQL 8 com schema inicial em `scripts_sql/01_init_db.sql`
- `backend`: Quarkus JVM exposto em `8080`
- `frontend`: Nginx servindo o build Vite e fazendo proxy de `/api/*` para o backend

## Comandos úteis

```bash
docker compose ps
docker compose logs -f backend
docker compose down
```

Para rodar só o build local do frontend:

```bash
npm install
npm run build
```
