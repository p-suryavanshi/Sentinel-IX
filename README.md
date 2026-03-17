# 🛡️ Sentinel-IX: Enterprise DLP & Audit Engine

**Sentinel-IX** is a high-performance Data Leak Prevention (DLP) solution designed to intercept, analyze, and audit sensitive data in real-time. It provides an automated "First Line of Defense" against the accidental exposure of API keys, passwords, and PII.

---

## 🚀 Core Features

* **Real-Time Heuristic Scanning:** Instant detection of sensitive patterns (Credentials, Tokens, Secrets).
* **Persistent Audit Vault:** Powered by **Spring Data JPA** and **H2 File-Based Storage**, ensuring zero data loss across application restarts.
* **Administrative HUD:** A futuristic, responsive dashboard built with **Tailwind CSS** for security monitoring.
* **RESTful Intelligence:** Full API suite for `/scan`, `/logs`, and `/health` monitoring.

---

## 🛠️ Technical Architecture

The application follows a robust **N-Tier Architecture**, ensuring scalability and separation of concerns:

1.  **Presentation Layer:** Modern UI using HTML5, JavaScript (ES6+), and Tailwind CSS.
2.  **Service Layer (The Engine):** Java-based logic that evaluates content risk and manages business rules.
3.  **Persistence Layer:** H2 Relational Database utilizing `jdbc:h2:file` for permanent data retention and audit integrity.

---

## 📋 API Documentation

| Endpoint | Method | Description |
| :--- | :--- | :--- |
| `/api/scan` | `POST` | Processes raw text and returns a `BLOCKED` or `CLEAN` status. |
| `/api/logs` | `GET` | Fetches the complete persistent audit history from the database. |
| `/api/health` | `GET` | Returns system operational status and server heartbeat. |

---

## ⚙️ Installation & Setup

1.  **Clone the Repository:**
    ```bash
    git clone [https://github.com/p-suryavanshi/Sentinel-IX.git](https://github.com/p-suryavanshi/Sentinel-IX.git)
    ```
2.  **Build the Project:**
    ```bash
    mvn clean install
    ```
3.  **Run the Application:**
    ```bash
    mvn spring-boot:run
    ```
4.  **Access the Dashboard:** Open `http://localhost:8080` in your browser.
5.  **Database Console:** Access the SQL interface at `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:file:./data/sentinel_db`).

---

## 🔮 Future Roadmap

* **AI-Powered Detection:** Implementing NLP (Natural Language Processing) to detect sensitive context beyond simple keywords.
* **OAuth2 Integration:** Secure administrative login via GitHub or Google.
* **Distributed Architecture:** Transitioning to **PostgreSQL** and **Docker** for cloud-scale deployments.

---
© 2026 Sentinel-IX Security Solutions
