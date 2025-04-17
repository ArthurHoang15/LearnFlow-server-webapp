# 🌐 LearnFlow Backend Server

Welcome to the backend of **LearnFlow**, a dynamic and feature-rich online English learning platform designed to deliver an engaging and effective learning experience. This server-side application powers the core functionalities of LearnFlow, enabling seamless user interactions, robust data management, and scalable performance.

---

## 🚀 Features

- 🔐 **Secure Authentication**: Supports user registration, login, logout, password recovery, and Google OAuth2 integration for quick account creation and data synchronization.
- 📊 **Learning Management**: Handles a rich question bank, vocabulary tracking, and progress monitoring with error logging to enhance user learning outcomes.
- 🌍 **Interactive Community**: Facilitates friend connections, direct messaging, and competitive leaderboards to foster a collaborative learning environment.
- 🎮 **Gamification**: Powers mini-games, daily quests, and an item shop for avatar customization, boosting user engagement.
- ⚙️ **Admin Controls**: Enables administrators to manage lessons, track user statistics, and disable accounts for policy violations.
- 📈 **Scalable Architecture**: Built with a modular design to handle both small and large user bases efficiently.

---

## 📚 Tech Stack

- **Node.js**: Runtime environment for executing JavaScript server-side.
- **Express.js**: Lightweight and flexible web framework for building RESTful APIs.
- **Java Spring Boot**: Core framework for backend logic, running on Apache Tomcat for robust performance.
- **PostgreSQL**: Open-source relational database for secure and efficient data storage.
- **Maven**: Build automation tool for streamlined project distribution and deployment.

---

## 📂 Branch Naming Convention

Branches should be named according to their purpose and task:

```plaintext
<prefix>/<SE-XX>-<task-name>
```

- **Prefix** options:
  - `feature/` – for new features
  - `fix/` – for bug fixes
  - `chore/` – for non-functional tasks
  - `refactor/` – for code restructuring

> Example: If your task is `[SE01][BE]Set up Github repository`, your branch name would be `feature/SE01-setup-repository`.

---

## 💾 Commit Message Convention

Follow a structured commit message format to maintain a clear history:

```plaintext
<prefix>(<SE-XX>): <commit message>
```

- **Prefix** options:
  - `feat` – for new features
  - `fix` – for bug fixes
  - `chore` – for maintenance tasks
  - `refactor` – for code restructuring

> Example: If your branch is `[SE05][BE]create delete account API`, your commit message would be `feat(SE05): create delete account API`.

---

## 🔄 Development Workflow

The development process is organized for efficiency and consistency:

1. **Pull** the latest code from the main branch.
2. **Create a new branch** from the main branch.
3. **Code** your assigned task.
4. **Commit** changes and **stash** if needed.
5. **Switch to main branch** and pull any new updates.
6. **Switch back to your working branch** and merge any updates from `main` into it.
7. **Resolve conflicts** if any.
8. **Push** your branch to the remote repository.
9. **Create a pull request** and request reviews.
10. After approval, **squash and merge** the pull request.

```plaintext
┌───────────────────────────────┐
│        Pull from Main         │
└──────────────┬────────────────┘
               │
               ▼
┌───────────────────────────────┐
│    Create New Branch from     │
│           Main                │
└──────────────┬────────────────┘
               │
               ▼
┌───────────────────────────────┐
│             Code              │
└──────────────┬────────────────┘
               │
               ▼
┌───────────────────────────────┐
│     Commit and Stash if       │
│           Needed              │
└──────────────┬────────────────┘
               │
               ▼
┌───────────────────────────────┐
│   Switch to Main Branch and   │
│         Pull Updates          │
└──────────────┬────────────────┘
               │
               ▼
┌───────────────────────────────┐
│   Switch to Working Branch    │
│    and Merge Updates from     │
│            Main               │
└──────────────┬────────────────┘
               │
               ▼
┌───────────────────────────────┐
│   Resolve Conflicts if Any    │
└──────────────┬────────────────┘
               │
               ▼
┌───────────────────────────────┐
│          Push to Remote       │
└──────────────┬────────────────┘
               │
               ▼
┌───────────────────────────────┐
│      Create Pull Request      │
│   and Request Review from     │
│            Others             │
└──────────────┬────────────────┘
               │
               ▼
┌───────────────────────────────┐
│  After Approval, Squash and   │
│            Merge              │
└───────────────────────────────┘
```

---

### 📌 Notes for Enhanced Development Progress

- **Consistency**: Adhere strictly to naming and commit conventions to maintain a clear, readable history.
- **Frequent Pulls**: Regularly pull updates from the main branch to avoid large conflicts.
- **Communication**: Keep the team informed about your progress and any blockers to ensure a smooth workflow.
- **Review Requests**: Aim for timely reviews by tagging appropriate reviewers, especially for critical features.

---

## 📂 Project Structure

```plaintext
.
├── src
│   ├── main
│   │   ├── java
│   │   │   ├── config        # Configuration files (e.g., security, database)
│   │   │   ├── controller    # Handles HTTP requests and routes
│   │   │   ├── service       # Business logic and API interactions
│   │   │   ├── repository    # Database queries and data access
│   │   │   ├── dto           # Data Transfer Objects for API communication
│   │   │   └── model         # Entity models for database mapping
│   │   └── resources
│   │       ├── application.properties  # Spring Boot configuration
│   │       └── static        # Static assets (if applicable)
├── target                    # Compiled Java classes and JAR files
├── pom.xml                   # Maven configuration for dependencies and build
├── docker-compose.yml        # (Optional) Docker setup for local development
├── tsconfig.json             # TypeScript configuration (for TypeScript modules, if used)
├── nodemon.json              # Nodemon configuration for development (if applicable)
├── package.json              # NPM configuration (for hybrid TypeScript modules)
└── README.md                 # Project documentation
```

---

## 🔧 Setup and Installation

1. **Clone the repository**:
   ```bash
   git clone https://github.com/learnflow/backend.git
   cd learnflow-backend
   ```

2. **Install dependencies**:
   ```bash
   mvn clean install
   npm install  # If using TypeScript for specific modules
   ```

3. **Configure environment**:
   - Create a `.env` file or update `application.properties` in `src/main/resources` with your database credentials, OAuth2 keys, and AWS configurations.
   - Example:
     ```properties
     spring.datasource.url=jdbc:postgresql://localhost:5432/learnflow
     spring.datasource.username=your_username
     spring.datasource.password=your_password
     google.oauth2.client-id=your_client_id
     google.oauth2.client-secret=your_client_secret
     ```

4. **Start the development server**:
   ```bash
   mvn spring-boot:run
   ```
   Or, for TypeScript modules:
   ```bash
   npm run dev
   ```

5. **Build for production**:
   ```bash
   mvn clean package
   ```

6. **Run the production build**:
   ```bash
   java -jar target/learnflow-backend.jar
   ```

7. **Optional: Run with Docker**:
   ```bash
   docker-compose up --build
   ```

---

## 📜 Scripts

- **Maven**:
  ```bash
  mvn spring-boot:run  # Starts the Spring Boot server in development mode
  mvn clean package    # Compiles and packages the application into a JAR
  mvn test             # Runs unit tests
  ```

- **NPM**:
  ```bash
  npm run dev   # Starts the server in development mode with ts-node and nodemon
  npm run build # Compiles TypeScript into JavaScript
  npm start     # Runs the compiled JavaScript
  ```

---

> **Happy Coding!** 🎉 Keep innovating and contributing to making LearnFlow the go-to platform for making English learning fun and effective! 🚀
