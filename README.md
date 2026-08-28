# AI Resource Scheduler

An AI-powered scheduling assistant that recommends the best-fit employee for a shift — filtering by skill match and availability, then ranking candidates with a trained ML model that explains its own reasoning via SHAP.

Built as a portfolio project demonstrating a production-style Spring Boot backend, real JWT authentication, and a genuine ML service (not a hardcoded rule engine) working together across two languages.

---

## Overview

Manually assigning employees to shifts is slow and inconsistent. **AI Resource Scheduler** solves this in two stages: first, a deterministic filter finds every employee who has the required skill *and* is available on that day/time; then a scikit-learn model ranks those qualified candidates by workload and recency, so work gets distributed fairly rather than always going to the same person — and every ranking comes with a feature-level explanation of *why*.

**Core use case:** *"I have a shift open on Tuesday requiring Java Backend skills — who should I assign, and why?"*

---

## Features

- **Full CRUD** for employees, skills, shifts, and availability windows
- **Rule-based candidate filtering** — skill match + day-of-week/time-window availability, computed in Spring Boot before anything touches the ML service
- **AI-powered ranking** — qualified candidates are scored by a trained GradientBoostingRegressor based on current workload and days since last assignment
- **SHAP explainability** — every score comes with a per-feature breakdown of what drove it, not a black-box number
- **Real JWT authentication** — register/login, BCrypt-hashed passwords, stateless token auth; every business endpoint is locked down except health checks and auth itself
- **Interactive API docs** via Swagger/OpenAPI

---

## Tech Stack

| Layer | Technology |
|---|---|
| Backend API | Java 25, Spring Boot 4.1.1, Spring Security 7, Spring Data JPA |
| Auth | JWT (jjwt 0.12.6), BCrypt |
| Database | PostgreSQL 16 |
| ML Service | Python, FastAPI, scikit-learn, pandas |
| Explainability | SHAP |
| Docs | Swagger / OpenAPI (springdoc 3.0.3) |
| Containerization | Docker (Postgres); backend/ML run natively in development |
| Build | Maven |

---

## Architecture
                 ┌─────────────────────┐
                 │   curl / Swagger UI   │
                 └──────────┬───────────┘
                            │ REST (JWT Bearer token)
                 ┌──────────▼───────────┐
                 │   Spring Boot API     │
                 │  auth, employees,     │
                 │  skills, shifts,      │
                 │  availability,        │
                 │  recommendations      │
                 └──────────┬───────────┘
                    │REST           │
                    │               │
         ┌──────────▼───┐   ┌───────▼────────┐
         │ PostgreSQL   │   │ Python ML API  │
         │ (Docker)     │   │ (FastAPI)      │
         └──────────────┘   │ /recommend      │
                             └────────┬────────┘
                                      │
                             ┌────────▼────────┐
                             │ GradientBoosting │
                             │ + SHAP explainer │
                             │ (trained on boot)│
                             └──────────────────┘

The Spring Boot service owns all business data, auth, and the skill/availability filtering logic. When ranking is needed, it computes workload and recency features for each qualified candidate, then calls the Python ML service, which returns a score and a SHAP-based explanation per candidate. Results are merged and sorted before being returned to the client.

---

## Data Model

- **User** — auth account (username, BCrypt password hash, role)
- **Employee** — profile, department, many-to-many skills
- **Skill** — name, category
- **Availability** — recurring day-of-week + time window per employee
- **Shift** — date, time window, required skill, location, status
- **Assignment** — links an Employee to a Shift; stores the ML confidence score for auditability

---

## AI / ML Approach

- **Problem framing:** given a shift and a pool of skill/availability-qualified employees, rank them by fit.
- **Model:** GradientBoostingRegressor (scikit-learn), currently trained on synthetic data at service startup — no real historical assignments exist yet.
- **Features:** `workload_count` (total past assignments), `days_since_last_assignment` (999 if never assigned — signals high availability).
- **Ground truth (synthetic):** lower workload and more days since last assignment score higher — this encodes a fairness preference, not just "pick anyone."
- **Explainability:** SHAP TreeExplainer returns each feature's contribution to the score, per candidate.
- **Retraining path:** once real `Assignment` history accumulates, the synthetic training data generator can be swapped for a query against real records.

---

## Getting Started

### Prerequisites
- JDK 25 (`brew install --cask temurin@25`)
- Python 3.12 (Python 3.14 is currently incompatible with numpy/scikit-learn's pinned versions — use 3.11 or 3.12)
- Docker Desktop
- Maven (bundled via `./mvnw`)

### 1. Start PostgreSQL
```bash
docker run --name scheduler-db \
  -e POSTGRES_DB=scheduler \
  -e POSTGRES_USER=scheduler \
  -e POSTGRES_PASSWORD=scheduler \
  -p 5432:5432 \
  -d postgres:16
```
(If it already exists: `docker start scheduler-db`)

### 2. Start the ML service
```bash
cd ml-service
python3.12 -m venv venv
source venv/bin/activate
pip install -r requirements.txt
uvicorn app.main:app --reload --port 8000
```
Confirm: `curl http://localhost:8000/health` → `{"status":"UP","service":"ml-service","model_ready":true}`

### 3. Start the backend
```bash
cd backend
export JAVA_HOME=$(/usr/libexec/java_home -v 25)
./mvnw spring-boot:run
```
Confirm: `curl http://localhost:8080/api/health` → `{"status":"UP","service":"ai-resource-scheduler-backend"}`

### API Docs
`http://localhost:8080/swagger-ui.html`

---

## Authentication

All endpoints except `/api/health`, `/api/auth/**`, and Swagger require a JWT.

**Register:**
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username": "yourname", "password": "yourpassword"}'
```
Returns a token immediately. **Login** (`/api/auth/login`) works the same way for existing users.

**Use the token:**
```bash
curl http://localhost:8080/api/employees \
  -H "Authorization: Bearer <token>"
```

---

## API Endpoints

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/api/auth/register` | Public | Create account, receive JWT |
| POST | `/api/auth/login` | Public | Authenticate, receive JWT |
| GET | `/api/health` | Public | Health check |
| GET/POST | `/api/employees` | JWT | List / create employees |
| GET | `/api/employees/{id}` | JWT | Get one employee |
| GET/POST | `/api/skills` | JWT | List / create skills |
| GET | `/api/skills/{id}` | JWT | Get one skill |
| GET/POST | `/api/shifts` | JWT | List (optional `?status=`) / create shifts |
| GET | `/api/shifts/{id}` | JWT | Get one shift |
| GET/POST | `/api/availability` | JWT | List (optional `?employeeId=`) / create availability |
| GET | `/api/recommendations/shifts/{shiftId}/candidates` | JWT | Ranked, explained candidate list for a shift |

---

## Project Structure
AI-resource-Scheduler/
├── backend/
│ ├── src/main/java/com/scheduler/backend/
│ │ ├── controller/ # REST endpoints
│ │ ├── service/ # business logic
│ │ ├── repository/ # Spring Data JPA
│ │ ├── entity/ # JPA entities
│ │ ├── dto/ # request/response + ml/ subpackage
│ │ ├── security/ # JWT filter, UserDetailsService, JwtService
│ │ ├── config/ # SecurityConfig, MlServiceConfig
│ │ └── exception/
│ └── pom.xml
├── ml-service/
│ ├── app/main.py # FastAPI app, model training, /recommend
│ ├── requirements.txt
│ └── venv/ # (gitignored)
└── README.md

---

## Known Limitations / Roadmap

- [ ] PATCH/DELETE endpoints not yet implemented (create/read only)
- [ ] No automated tests yet (JUnit / pytest)
- [ ] Model trained on synthetic data — needs real assignment history to retrain meaningfully
- [ ] Docker Compose for one-command startup not yet verified end-to-end
- [ ] No frontend — API-only currently

---

## License

MIT
