# 📈 TradeShift: Full-Stack Financial Portfolio & Trading Platform

TradeShift is a comprehensive financial application developed to provide modern investors with a secure, all-in-one platform for portfolio management, real-time market data analysis, and trade execution.

This project was built as a solution during my internship at Zaalima Development to showcase proficiency in enterprise-grade full-stack architecture.

## ✨ Key Features & Highlights

* **Real-Time Data Integration:** Integrates with the (**Finnhub API**) to fetch and display live stock prices and market data.
* **Secure Authentication:** Implements token-based security using **Spring Security** and **JWT (JSON Web Tokens)** for user registration and protected API endpoints.
* **Portfolio Management:** Tracks user holdings, calculates **Net Worth**, **Cash Balance**, and market performance.
* **Trade Execution Module:** Provides a secure interface for executing **BUY** and **SELL** orders, which are persisted in the database.
* **Transaction History:** A dedicated module for viewing, filtering, and sorting all historical trades with clear metrics and sequential IDs.
* **Modern UI/UX:** Built with **React** and **Tailwind CSS** for a clean, responsive analytics dashboard.

## 🧠 Tech Stack

TradeShift is built as a secure, full-stack application leveraging the following modern technologies:

| Category | Technologies Used |
| :--- | :--- |
| **Backend & Security** | Java, **Spring Boot**, **Spring Security (JWT)**, REST API |
| **Database** | **MySQL** |
| **Frontend & UI** | **React** (TypeScript), **Tailwind CSS** |
| **Integrations** | Finnhub API (Real-Time Stock Data) |

## Screenshots
<img width="1887" height="889" alt="Screenshot 2025-10-24 080737" src="https://github.com/user-attachments/assets/d4b6a766-9f8e-48d9-b6c6-638acbdeefd4" />
<img width="1889" height="911" alt="Screenshot 2025-10-24 080856" src="https://github.com/user-attachments/assets/f6b27a49-669c-4f86-98b8-a03902582862" />
<img width="1914" height="718" alt="Screenshot 2025-10-24 081102" src="https://github.com/user-attachments/assets/273b173e-c04f-4fb0-a642-c901aaa89079" />
<img width="1505" height="905" alt="Screenshot 2025-10-24 081417" src="https://github.com/user-attachments/assets/77d6ff0d-f1d7-49ab-a5ab-4456713fad88" />
<img width="1721" height="739" alt="Screenshot 2025-10-24 081514" src="https://github.com/user-attachments/assets/5453d5cd-db9c-42b2-97d4-630b3ada5d3d" />
<img width="1572" height="900" alt="Screenshot 2025-10-24 081610" src="https://github.com/user-attachments/assets/7a0b38c5-6394-4ebf-8438-5e7caf3e2b53" />
<img width="1574" height="903" alt="Screenshot 2025-10-24 081709" src="https://github.com/user-attachments/assets/571ce20d-7a25-4be0-b99a-38bb343cc0ac" />
<img width="1602" height="906" alt="Screenshot 2025-10-24 081925" src="https://github.com/user-attachments/assets/3a9288f5-e36a-44ae-bffe-a326a03a609c" />



## 🛠️ Getting Started (Local Setup)

Follow these steps to get a local copy of TradeShift up and running.

### Prerequisites

You'll need the following software installed on your machine to run both the frontend and backend services:

* **Java Development Kit (JDK) 17+** (Required for Spring Boot Backend)
* **Node.js (LTS recommended)** (Required for React Frontend)
* **MySQL Server instance** (Required for Database)
* **Git** (Required for cloning the repository)
* **Maven** (Used by the Spring Boot project—included via the wrapper `./mvnw`)

### 1. Clone the Repository
```bash
git clone [https://github.com/PravinSundarM/TradeShift.git](https://github.com/PravinSundarM/TradeShift.git)
cd TradeShift
````

### 2\. Backend Setup

1.  Navigate to the backend directory:
    ```bash
    cd backend
    ```
2.  Configure your MySQL connection details (URL, username, password) in the appropriate application properties file.
3.  Build and run the Spring Boot application using your IDE (e.g., IntelliJ IDEA) or via the command line:
    ```bash
    ./mvnw spring-boot:run 
    # OR (for Windows)
    .\mvnw spring-boot:run
    ```
    *The backend server should start on port 8080.*

### 3\. Frontend Setup

1.  Open a new terminal and navigate to the frontend directory:
    ```bash
    cd frontend
    ```
2.  Install the required dependencies:
    ```bash
    npm install
    ```
3.  Start the React development server:
    ```bash
    npm run dev
    ```
    *The frontend application will typically open in your browser at `http://localhost:5173`.*


## 🤝 Contributor
* **LinkedIn:** [pravinsundarm](http://www.linkedin.com/in/pravinsundarm)
* **GitHub:** [PravinSundarM](https://github.com/PravinSundarM)
---










