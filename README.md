# Authentication-System
# 🔐 Spring Boot Authorization System

A complete Authentication & Authorization system built using Spring Boot and Spring Security.

This project implements secure authentication using Basic Authentication, Custom JWT Authentication, Refresh Token, HTTP-Only Cookies, Session Management, Login, Logout, and Protected APIs.

---

## 🚀 Features

- Basic Authentication
- Custom JWT Authentication
- Access Token & Refresh Token
- Refresh Token stored in HTTP-Only Cookie
- Secure Login System
- Logout Functionality
- Session Management
- Protected APIs
- Spring Security Integration
- Exception Handling
- Layered Architecture
- DTO & Entity Separation

---

## 🛠️ Technologies Used

- Java
- Spring Boot
- Spring Security
- JWT (JSON Web Token)
- Maven
- MySQL
- Hibernate / JPA
- Lombok

---

## 🔑 Authentication Flow

### 1️⃣ Login

User logs in using email and password.

```json
{
  "email": "user@gmail.com",
  "password": "123456"
}
```

---

### 2️⃣ JWT Generation

After successful authentication:

- Access Token generated
- Refresh Token generated

---

### 3️⃣ Refresh Token Cookie

Refresh token is stored in a secure HTTP-Only Cookie.

```http
Set-Cookie: refreshToken=xxxxxx;
HttpOnly;
Secure;
SameSite=Strict
```

---

### 4️⃣ Access Protected APIs

Client sends JWT access token in Authorization Header.

```http
Authorization: Bearer your_access_token
```

---

### 5️⃣ Refresh Access Token

When access token expires:

- Refresh token validates user
- New access token generated

---

### 6️⃣ Logout

Logout will:

- Clear refresh token cookie
- Invalidate session
- Remove authentication context

---

## 🔒 Security Features

- Password Encryption using BCrypt
- Stateless Authentication
- Secure Cookie Handling
- JWT Signature Verification
- Role-Based Authorization
- Session Protection

---

## ⚙️ Running the Project

### Clone Repository

```bash
git clone https://github.com/your-username/project-name.git
```

### Navigate to Project

```bash
cd project-name
```

### Run Application

```bash
./mvnw spring-boot:run
```

---

## 📖 Learning Goals

This project was created to practice:

- Spring Security
- Authentication & Authorization
- JWT Token Handling
- Secure Backend Development
- Session & Cookie Management
- REST API Security

---

## 🧠 Future Improvements

- Email Verification
- OAuth2 Login
- Google Authentication
- Redis Token Storage
- Two Factor Authentication (2FA)

---

## 👨‍💻 Author

### Firoz Tanvir Hossain

Software Engineer & Backend Developer
