# Shop Application - Spring Boot, Angular, MongoDB, Redis, Docker

## Project overview 
This is a monolithic e-commerce application built with:
- **Backend:** Spring Boot (RESTful API)
- **Frontend:** Angular with Bootstrap
- **Database:** MongoDB (primary data store)
- **Cache/State:** Redis (token management & shopping cart)
- **Containerization:** Docker Compose

### Customer Functionality
✅ User registration & authentication (JWT tokens)  
✅ Product catalog with filtering  
✅ Shopping cart persistence (Redis)  
✅ Order placement with status tracking  
✅ Order history with simulated status transitions  

### Admin Functionality
🛠️ Product management (availability toggle)  
🛠️ Customer order viewing/refunding  

## Technology Stack

| Component        | Technology                       |
|------------------|----------------------------------|
| Backend          | Java 17, Spring Boot 3.4.3       |
| Security         | JWT (Access + Refresh tokens)    |
| Database         | MongoDB (document storage)       |
| Cache            | Redis (tokens, cart & theme)     |
| Frontend         | Angular 19, Bootstrap 5          |
| Containerization | Docker + Docker Compose          |
| Build Tools      | Gradle (backend), npm (frontend) |


## System Architecture
```yaml
┌─────────────┐    ┌─────────────┐    ┌────────────────────┐
│ Angular     │ ←→ │ Spring Boot │ ←→ │ MongoDB            │
│ Frontend    │    │ (REST API)  │    │(Primary Database)  │
└─────────────┘    └─────────────┘    └────────────────────┘
                        ↑                        ↑
                        │                        │
                        ↓                        │
                        ┌────────────────┐       │
                        │ Redis          │ ←─────┘
                        │ (Tokens +      │
                        │ Shopping Cart) │
                        └────────────────┘
```

## Installation & Setup

### Prerequisites
- Docker 20.10+
- Docker Compose 2.0+
- JDK 17 (for local development)
- Node.js 22+ (for frontend development)

### Quick Start with Docker
1. Clone the repository:
   ```bash
   git clone [https://github.com/your-repo/shop-app.git](https://github.com/KarolWojnar/SportStore.git)
   cd sportstore
2. Set up `.env`:
```yaml
  # Secret key used for generating and verifying JWT tokens
JWT_SECRET=your_jwt_secret_key

  # Email credentials for sending messages
FORUM_MAIL_USERNAME=your_email@example.com
FORUM_MAIL_PASSWORD=your_email_password

  # Stripe API secret key for handling payments
STRIPE_SECRET=your_stripe_secret_key

  # Webhook key for verifying Stripe events
WEBHOOK_KEY=your_webhook_secret_key
```

3. Build backend:
```yaml
./gradlew clean build
```

4. Start all services:
```yaml
docker-compose up -d --build
```

### API Documentation
`
http://localhost:8080/swagger-ui/index.html
`
