# Smart Campus Operations Hub

**Smart Campus Operations Hub** is a comprehensive, full-stack campus management platform meticulously designed to streamline college or university operations. The system modernizes day-to-day campus management by introducing automation and integrated communication tools.

## 🚀 Features

- **Google OAuth Integration**: Secure, single-sign-on (SSO) login for all campus users.
- **Role-Based Access Control (RBAC)**: Distinct functionalities tailored for Students, Staff, and Administrators.
- **Resource Booking Workflow**: Simplified reserving of classrooms, labs, and equipment.
- **Incident Ticket Management**: Easy reporting, tracking, and resolving of campus maintenance or IT issues.
- **Dynamic Dashboard**: A real-time hub for personalized analytics, notifications, and overview.

## 🛠️ Technology Stack

**Frontend**
- React.js 
- Vite for rapid development and optimized builds

**Backend**
- Java 17+
- Spring Boot (REST APIs)
- Spring Security (JWT Provider & Authentication handling)
- Hibernate / JPA

**Database**
- MySQL

## ⚙️ Local Development Setup

### Prerequisites
- [Node.js (v18+)](https://nodejs.org/)
- [Java Development Kit (JDK 17+)](https://adoptium.net/)
- [MySQL (v8+)](https://www.mysql.com/)

### 1. Database Configuration
1. Open your MySQL client and create a new database.
2. Update the `application.properties` file in `demo/src/main/resources/` with your MySQL credentials.

### 2. Backend Setup (Spring Boot)
1. Navigate to the backend directory:
```bash
cd demo
```
2. Build and run the project using Maven wrapper:
```bash
./mvnw spring-boot:run
```

### 3. Frontend Setup (React/Vite)
1. Navigate to the frontend directory:
```bash
cd frontend
```
2. Install dependencies:
```bash
npm install
```
3. Set your environment variables within `frontend/.env` (e.g., your Google OAuth Client ID).
4. Run the development server:
```bash
npm run dev
```

## 🏢 System Modules & Ownership

The "Smart Campus Operations Hub" holds a modular architecture to streamline full-stack development, divided logically between two lead developers.

**Developer 1 (@mohsh): Security & Core Management**
- **Authentication:** Google OAuth SSO, User roles, Security Context
- **Resources:** Managing campus facilities (Rooms, Labs, Equipment)
- **Notification:** Real-time push alerts, messaging, & status updates

**Developer 2 (@anas): Operations & Dashboards**
- **Bookings:** Reserving resources, calendar logic, and tracking
- **Tickets:** Incident reporting attachments, maintenance queues
- **Dashboard:** Data aggregation, activity feeds, analytics

## 🤝 Contributing
Contributions are always welcome. Please open an issue or pull request if you want to propose changes.

**Core Contributors:**
- [SharafThawfeek](https://github.com/SharafThawfeek)
- [Anas8410](https://github.com/Anas8410)

## 📄 License
This project is for educational and demonstrative purposes.
