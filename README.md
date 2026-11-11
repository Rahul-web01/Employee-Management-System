<div align="center">

# 👥 Employee Management System

**A full-stack CRUD web app to manage employees, departments, and salary records**

![Java](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring_Security-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white)
![Thymeleaf](https://img.shields.io/badge/Thymeleaf-005F0F?style=for-the-badge&logo=thymeleaf&logoColor=white)
![Bootstrap](https://img.shields.io/badge/Bootstrap-7952B3?style=for-the-badge&logo=bootstrap&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)

*Java Full Stack minor project, built during my internship at **Adyapan** 🚀*

</div>

---

## 📖 About

Small and medium organizations need a simple, secure portal to track personnel records, department assignments, and salary details. This project is a **server-side rendered web application** that does exactly that, using a clean layered architecture (Controller → Service → Repository → Database).

---

## ✨ Features

| Status | Feature |
|:---:|---|
| ✅ | Full CRUD for employees: create, view, update, delete |
| ✅ | Login with three roles (Admin, HR, Viewer) using Spring Security |
| ✅ | Passwords stored as BCrypt hashes, CSRF protection on every form |
| ✅ | Buttons shown or hidden by role, plus server-side checks (403 page if a URL is typed directly) |
| ✅ | Many-to-one relationship: each employee belongs to a department |
| ✅ | Search employees by first or last name (case-insensitive) |
| ✅ | Responsive Thymeleaf pages styled with Bootstrap |
| ✅ | Form validation with field-level error messages (`@NotBlank`, `@Email`, `@Size`, and more) |
| ✅ | Duplicate email protection |
| ✅ | Global exception handler with friendly 400, 404, 409, and 500 pages |
| ✅ | Unit tests (JUnit 5 + Mockito), repository tests (H2), and access-rule tests for every role (MockMvc) |
| ✅ | Deployed with Docker on Render, database on TiDB Cloud |

---

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| **Language** | Java 17 |
| **Framework** | Spring Boot 4 |
| **Security** | Spring Security 7 (form login, roles, BCrypt, CSRF) |
| **Persistence** | Spring Data JPA, Hibernate |
| **Database** | MySQL 8 locally, TiDB Cloud (MySQL-compatible) in production, H2 for tests. PostgreSQL also supported. |
| **UI** | Thymeleaf (+ Spring Security extras), Bootstrap 5 |
| **Validation** | Jakarta Bean Validation |
| **Testing** | JUnit 5, Mockito, MockMvc, Spring Security Test |
| **Build tool** | Maven (wrapper included) |
| **Deployment** | Docker (multi-stage, CDS for fast startup) on Render |

---

## 🏗️ Architecture

```
   Browser
      │
      ▼
┌─────────────┐   ┌─────────────┐   ┌──────────────┐   ┌─────────┐
│ Controller  │ → │   Service   │ → │  Repository  │ → │  MySQL  │
│ (web layer) │   │ (business)  │   │ (Spring JPA) │   │   DB    │
└─────────────┘   └─────────────┘   └──────────────┘   └─────────┘
      │
      ▼
 Thymeleaf templates (HTML)
```

---

## 🗂️ Project Structure

```
src/main/java/com/example/employee_management_system
├── config         # SecurityConfig (who can open what), DataLoader (sample departments), UserSeeder (sign-in accounts)
├── controller     # Handles web requests (employees, login, access denied)
├── security       # Loads users from the database for Spring Security
├── service        # Business logic
├── repository     # Database access (Spring Data JPA)
├── entity         # JPA entities (Department, Employee, AppUser, Role)
└── exception      # Custom exceptions + global handler

src/main/resources
├── templates      # Thymeleaf pages (login, employees/, error/, fragments)
└── application.properties

src/test/java/...  # Service, repository, validation, and security access tests
```

---

## 🗄️ Database Design

```
┌──────────────────┐            ┌──────────────────────┐
│   departments    │            │      employees       │
├──────────────────┤            ├──────────────────────┤
│ id (PK)          │◄───────────│ department_id (FK)   │
│ name (unique)    │  1      *  │ id (PK)              │
│ location         │            │ first_name           │
└──────────────────┘            │ last_name            │
                                │ email (unique)       │
                                │ designation          │
                                │ salary               │
                                │ hire_date            │
                                └──────────────────────┘

┌──────────────────────┐
│      app_users       │
├──────────────────────┤
│ id (PK)              │
│ username (unique)    │
│ password (BCrypt)    │
│ role                 │   ADMIN | HR | VIEWER
└──────────────────────┘
```

Hibernate creates the tables automatically on the first run. Four sample departments (Engineering, Human Resources, Finance, Marketing) are added if the table is empty, and the three sign-in accounts are created on every start if missing.

---

## 🌐 Routes

| Method | URL | Purpose | Who |
|---|---|---|---|
| GET / POST | `/login` | Sign in | Everyone |
| POST | `/logout` | Sign out | Signed in |
| GET | `/employees` | List employees (optional `?keyword=`) | Admin, HR, Viewer |
| GET | `/employees/new` | Show the add form | Admin, HR |
| GET | `/employees/edit/{id}` | Show the edit form | Admin, HR |
| POST | `/employees/save` | Create or update an employee | Admin, HR |
| POST | `/employees/delete/{id}` | Delete an employee | Admin |

Full details, form fields, validation rules, and error responses are in [docs/API.md](docs/API.md).

---

## 🔐 Login and Roles

Every page needs a sign-in. Three accounts are created automatically:

| Username | Default password | Role | Can |
|---|---|---|---|
| `admin` | `admin123` | ADMIN | view, search, add, edit, **delete** |
| `hr` | `hr123` | HR | view, search, add, edit |
| `viewer` | `viewer123` | VIEWER | view and search only |

- Passwords are stored as **BCrypt hashes**, never as plain text.
- Buttons a role can't use are hidden, **and** the server blocks the URL too: typing `/employees/new` as a viewer shows a 403 page.
- Every form carries a **CSRF token**, so other websites can't submit forms on a signed-in user's behalf.

To use your own passwords, set these environment variables (changes apply on the next start):

| Variable | Default | Purpose |
|---|---|---|
| `ADMIN_PASSWORD` | `admin123` | Password for `admin` |
| `HR_PASSWORD` | `hr123` | Password for `hr` |
| `VIEWER_PASSWORD` | `viewer123` | Password for `viewer` |
| `SHOW_DEMO_LOGINS` | `true` | Show the demo accounts box on the login page. Set to `false` once you change the passwords. |

---

## 🚀 Getting Started

### Prerequisites

- ☕ JDK 17 or higher
- 🐬 MySQL 8 (server running)
- 🔧 Git

> You do **not** need to install Maven. The Maven Wrapper is included.

### 1️⃣ Clone the repository

```bash
git clone https://github.com/Rahul-web01/Employee-Management-System.git
cd Employee-Management-System
```

### 2️⃣ Create the database

Run in MySQL Workbench or the MySQL command line:

```sql
CREATE DATABASE IF NOT EXISTS ems_db;
```

### 3️⃣ Set your database password

Create a file named `local.properties` in the project root (next to `pom.xml`) with one line:

```properties
spring.datasource.password=YOUR_PASSWORD
```

> 🔒 `local.properties` is in `.gitignore`, so your password is never pushed to GitHub.

The app connects to `jdbc:mysql://localhost:3306/ems_db` as user `root` by default. You can override this with the environment variables `DB_URL`, `DB_USER`, and `DB_PASSWORD`.

### 4️⃣ Run the application

**Windows (PowerShell):**

```powershell
.\mvnw.cmd spring-boot:run
```

**Mac / Linux:**

```bash
./mvnw spring-boot:run
```

Then open 👉 **http://localhost:8080** and sign in with one of the accounts above (for example `admin` / `admin123`).

---

## ☁️ Deployment (Render + TiDB Cloud)

The app runs as a Docker container on Render's free plan. The database is a free TiDB Cloud Starter instance (MySQL-compatible, no expiry).

Environment variables set on Render:

| Variable | Example |
|---|---|
| `DB_URL` | `jdbc:mysql://<tidb-host>:4000/ems_db?sslMode=VERIFY_IDENTITY&enabledTLSProtocols=TLSv1.2,TLSv1.3&createDatabaseIfNotExist=true` |
| `DB_USER` | `<prefix>.root` (from TiDB's Connect dialog) |
| `DB_PASSWORD` | the TiDB password |
| `SPRING_DATASOURCE_HIKARI_MAX_LIFETIME` | `300000` (recycle connections every 5 minutes; TiDB closes idle ones) |
| `ADMIN_PASSWORD`, `HR_PASSWORD`, `VIEWER_PASSWORD` | optional, see [Login and Roles](#-login-and-roles) |

Render sets `PORT` itself; the app listens on it automatically. The Dockerfile builds a Class Data Sharing archive so the app starts in about 2 minutes even on Render's 0.1 CPU free plan.

---

## 🧪 Running Tests

Tests use an in-memory H2 database, so MySQL is not needed.

```bash
./mvnw test
```

On Windows PowerShell: `.\mvnw.cmd test`

---

## 📸 Screenshots

### Employee list and search
![Employee list](docs/screenshots/employee-list.png)

### Add employee form with validation
![Validation](docs/screenshots/form-validation.png)

---

## 🗺️ Roadmap

- [x] Role-based login with Spring Security
- [x] Live deployment (Render + TiDB Cloud)
- [ ] Pagination and sorting for the employee list
- [ ] Department management pages
- [ ] REST API with Swagger docs
- [ ] Dashboard with charts
- [ ] Flyway database migrations
- [ ] GitHub Actions CI

🔗 **Live Link:** https://employee-management-system-yuzi.onrender.com
> Hosted on a free server that sleeps when idle, so the first load can take about a minute. Sign in with a demo account from the [Login and Roles](#-login-and-roles) section.

---

## 👨‍💻 Author

**Rahul Singh**
B.Tech Computer Science and Engineering student, Lucknow, India

[![GitHub](https://img.shields.io/badge/GitHub-Rahul--web01-181717?style=flat-square&logo=github)](https://github.com/Rahul-web01)

---

<div align="center">

⭐ If you like this project, give it a star!

</div>
