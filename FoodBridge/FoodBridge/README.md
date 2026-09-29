# FoodBridge - Food Waste Donation and Distribution Management System

A complete full-stack Java application built with Spring Boot 3.x, MySQL, and Bootstrap 5 for managing food donations between donors (restaurants, hotels, canteens) and verified NGOs/receiver organizations.

## 🌟 Features

### Core Functionality
- **User Authentication & Authorization** - JWT-based authentication with BCrypt password hashing
- **Role-Based Access Control** - Three roles: ADMIN, DONOR, RECEIVER
- **Donation Management** - Create, edit, cancel, and track food donations
- **Request Workflow** - NGOs request food → Donors approve/reject → Collection → Completion
- **Quantity Validation** - Backend-enforced quantity validation prevents over-allocation
- **Expiry Handling** - Automatic expiry detection and prevention of requests for expired food
- **Organization Verification** - Admin verification required for NGOs to request food
- **Real-time Notifications** - Database-backed notifications for all key events
- **Dashboard Analytics** - Role-specific dashboards with statistics
- **Search & Filtering** - Backend-powered search and filter for available food

### User Roles

| Role | Capabilities |
|------|-------------|
| **ADMIN** | View all users, manage organizations, verify NGOs, monitor all donations/requests, view reports |
| **DONOR** | Create donations, manage own donations, approve/reject requests, mark food as collected |
| **RECEIVER** | Browse available food, request food, track requests, view collection history |

## 🛠 Technology Stack

### Backend
- **Java 17+**
- **Spring Boot 3.2.0**
- **Spring Web** - REST API
- **Spring Data JPA** - Database abstraction
- **Hibernate** - ORM
- **Spring Security** - Authentication & Authorization
- **JWT (jjwt 0.11.5)** - Token-based authentication
- **BCrypt** - Password hashing
- **Bean Validation** - Input validation
- **Maven** - Build tool

### Frontend
- **HTML5, CSS3, JavaScript (ES6+)**
- **Bootstrap 5.3** - UI Framework
- **Bootstrap Icons** - Icon library
- **Fetch API** - Backend communication
- **Inter Font** - Typography

### Database
- **MySQL 8.x** - Primary database
- **MySQL Workbench** - Database inspection (optional)

## 📁 Project Structure

```
FoodBridge/
├── backend/
│   ├── pom.xml
│   └── src/
│       └── main/
│           ├── java/com/foodbridge/
│           │   ├── config/          # Configuration classes
│           │   ├── controller/      # REST controllers
│           │   ├── dto/             # Data Transfer Objects
│           │   ├── entity/          # JPA Entities
│           │   ├── exception/       # Custom exceptions
│           │   ├── repository/      # Spring Data Repositories
│           │   ├── security/        # Security configuration
│           │   ├── service/         # Business logic
│           │   └── FoodBridgeApplication.java
│           └── resources/
│               └── application.properties
├── frontend/
│   ├── index.html                   # Landing page
│   ├── login.html                   # Login page
│   ├── register.html                # Registration page
│   ├── css/
│   │   └── style.css               # Custom styles
│   ├── js/
│   │   └── api.js                  # API client & utilities
│   ├── donor/                      # Donor dashboard pages
│   ├── receiver/                   # Receiver dashboard pages
│   └── admin/                      # Admin dashboard pages
├── database/
│   └── foodbridge_db.sql           # Database schema & sample data
├── README.md
└── .gitignore
```

## 🗄 Database Design

### Entity Relationships

```
User (1) ────── (1) Organization
    │
    ├───< Donation (1) ──────< DonationItem
    │              │
    │              └──< Request (1) ────── (1) Collection
    │
    └───< Notification
```

### Tables
1. **users** - Core user accounts with roles
2. **organizations** - NGO/Receiver organization details
3. **donations** - Food donations from donors
4. **donation_items** - Individual items within a donation
5. **requests** - Food requests from NGOs
6. **collections** - Collection records
7. **notifications** - User notifications

## 🚀 Getting Started

### Prerequisites
- Java 17 or later
- Maven 3.8+
- MySQL 8.x
- Modern web browser

### Database Setup

1. Install MySQL 8.x and start the service
2. Create the database (or let Hibernate auto-create):
   ```sql
   CREATE DATABASE foodbridge_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```
3. Import the schema and sample data:
   ```bash
   mysql -u root -p foodbridge_db < database/foodbridge_db.sql
   ```

### Backend Configuration

1. Copy the example configuration:
   ```bash
   cp backend/src/main/resources/application.properties backend/src/main/resources/application-local.properties
   ```

2. Edit `application-local.properties` with your MySQL credentials:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/foodbridge_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
   spring.datasource.username=root
   spring.datasource.password=YOUR_MYSQL_PASSWORD
   ```

3. Update JWT secret (minimum 256 bits for production):
   ```properties
   app.jwt.secret=your-super-secret-key-minimum-256-bits-long
   app.jwt.expiration=86400000
   ```

### Running the Application

#### Backend
```bash
cd backend
mvn spring-boot:run
```
The API will be available at `http://localhost:8080/api`

#### Frontend
Serve the frontend using any static file server:

**Option 1: Python (built-in)**
```bash
cd frontend
python -m http.server 5500
```

**Option 2: Node.js (http-server)**
```bash
npx http-server frontend -p 5500
```

**Option 3: VS Code Live Server**
Right-click `index.html` → "Open with Live Server"

Access the application at `http://localhost:5500`

## 🔐 Demo Credentials

All demo accounts use password: `password123`

| Role | Email | Description |
|------|-------|-------------|
| Admin | admin@foodbridge.com | Full system access |
| Donor | donor1@foodbridge.com | Restaurant owner |
| Donor | donor2@foodbridge.com | Hotel manager |
| Receiver (Verified) | receiver1@foodbridge.com | Helping Hands NGO |
| Receiver (Verified) | receiver2@foodbridge.com | Food for All Charity |
| Receiver (Pending) | pending@foodbridge.com | New Hope Foundation |

## 📚 API Endpoints

### Authentication
```
POST   /api/auth/register          # Register new user
POST   /api/auth/login             # Login
GET    /api/auth/me                # Get current user
```

### Users
```
GET    /api/users                  # List all users (Admin)
GET    /api/users/{id}             # Get user by ID
PUT    /api/users/{id}             # Update user
PATCH  /api/users/{id}/status      # Update user status (Admin)
POST   /api/users/{id}/change-password
DELETE /api/users/{id}             # Delete user (Admin)
```

### Organizations
```
GET    /api/organizations          # List organizations (Admin)
GET    /api/organizations/me       # Get my organization (Receiver)
POST   /api/organizations          # Create organization (Receiver)
PUT    /api/organizations/{id}     # Update organization
PATCH  /api/organizations/{id}/verify  # Verify organization (Admin)
```

### Donations
```
GET    /api/donations              # List available food (paginated)
GET    /api/donations/my-donations # Get my donations (Donor)
GET    /api/donations/{id}         # Get donation details
POST   /api/donations              # Create donation (Donor)
PUT    /api/donations/{id}         # Update donation (Donor)
PATCH  /api/donations/{id}/cancel  # Cancel donation (Donor)
DELETE /api/donations/{id}         # Delete donation
```

### Requests
```
GET    /api/requests/my-requests   # Get my requests (Receiver)
GET    /api/requests/donation/{id} # Get requests for donation (Donor)
GET    /api/requests/{id}          # Get request details
POST   /api/requests               # Create request (Receiver)
PATCH  /api/requests/{id}/approve  # Approve request (Donor)
PATCH  /api/requests/{id}/reject   # Reject request (Donor)
PATCH  /api/requests/{id}/cancel   # Cancel request
PATCH  /api/requests/{id}/collect  # Mark as collected (Donor)
PATCH  /api/requests/{id}/complete # Mark as completed
```

### Notifications
```
GET    /api/notifications          # Get my notifications
GET    /api/notifications/unread   # Get unread notifications
GET    /api/notifications/unread/count
PATCH  /api/notifications/{id}/read
PATCH  /api/notifications/read-all
```

### Dashboards
```
GET    /api/dashboard/admin        # Admin statistics
GET    /api/dashboard/donor        # Donor statistics
GET    /api/dashboard/receiver     # Receiver statistics
```

## 🔒 Security Features

- **JWT Authentication** - Stateless token-based auth
- **BCrypt Password Hashing** - Secure password storage
- **Role-Based Authorization** - Method & endpoint level security
- **CORS Configuration** - Configured for localhost development
- **Input Validation** - Bean Validation on all DTOs
- **Global Exception Handling** - Structured error responses
- **SQL Injection Protection** - JPA/Hibernate parameterized queries

## 📱 Frontend Pages

### Public Pages
- `index.html` - Landing page with hero, features, stats
- `login.html` - User login with demo credentials
- `register.html` - Role-based registration

### Donor Pages
- `donor/donor-dashboard.html` - Statistics, recent donations, pending requests
- `donor/create-donation.html` - Multi-item donation creation form
- `donor/my-donations.html` - List all donations with actions
- `donor/donation-details.html` - Detailed view with requests
- `donor/requests.html` - All requests for donor's donations
- `donor/donor-profile.html` - Profile & password management

### Receiver Pages
- `receiver/receiver-dashboard.html` - Statistics, available food, recent requests
- `receiver/available-food.html` - Search/filter/sort available donations
- `receiver/food-details.html` - Detailed food view with request option
- `receiver/my-requests.html` - Tabbed request status view
- `receiver/collection-history.html` - Completed collections
- `receiver/receiver-profile.html` - Profile, org details, password

### Admin Pages
- `admin/admin-dashboard.html` - System-wide statistics
- `admin/manage-users.html` - User management with status actions
- `admin/manage-organizations.html` - Organization verification
- `admin/manage-donations.html` - All donations oversight
- `admin/manage-requests.html` - All requests oversight
- `admin/reports.html` - Analytics & reports

## 🔄 Business Workflows

### Donation Lifecycle
```
CREATE → AVAILABLE → REQUESTED → PARTIALLY_RESERVED → FULLY_RESERVED
                                          ↓
                                    EXPIRED (auto)
                                          ↓
                                    COMPLETED (after collection)
```

### Request Lifecycle
```
CREATE → PENDING → APPROVED → COLLECTED → COMPLETED
              ↓
           REJECTED
              ↓
           CANCELLED
```

### Quantity Validation
- Donation has total quantity (e.g., 100 portions)
- Multiple requests can be approved
- Sum of approved quantities cannot exceed total
- Backend enforces this with `@Transactional` methods

### Expiry Handling
- Expired donations cannot be requested
- Scheduled task runs every 5 minutes to update expired donations
- Frontend displays expiry status clearly

## 🧪 Testing

Run backend tests:
```bash
cd backend
mvn test
```

### Test Coverage Includes:
- User registration & login
- Password hashing verification
- Role-based authorization
- Donation creation & retrieval
- Request creation & quantity validation
- Expired donation validation
- Organization verification

## 📦 Building for Production

```bash
cd backend
mvn clean package -DskipTests
```

The JAR will be in `backend/target/foodbridge-1.0.0.jar`

Run with production profile:
```bash
java -jar -Dspring.profiles.active=prod foodbridge-1.0.0.jar
```

## 🐛 Troubleshooting

### Common Issues

**Database Connection Failed**
- Verify MySQL is running
- Check credentials in `application.properties`
- Ensure database `foodbridge_db` exists

**CORS Errors**
- Frontend must be served from `http://localhost:5500` or configured origin
- Check `app.cors.allowed-origins` in application.properties

**JWT Token Expired**
- Token expires in 24 hours (configurable)
- User will be redirected to login automatically

**Port Conflicts**
- Backend: 8080 (change `server.port`)
- Frontend: 5500 (change server port)

## 🔮 Future Enhancements

- [ ] Email notifications for key events
- [ ] Mobile-responsive PWA
- [ ] Real-time updates with WebSockets
- [ ] Food image uploads
- [ ] Geolocation-based food search
- [ ] Advanced analytics & charts (Chart.js)
- [ ] Multi-language support
- [ ] API rate limiting
- [ ] Audit logging
- [ ] Automated backup scheduling

## 📄 License

This is a college project demonstration. Not for production use without proper security review.

## 👨‍💻 Author

Built as a complete full-stack academic project demonstrating:
- Spring Boot 3.x best practices
- Clean architecture (Controller → Service → Repository → Entity)
- JWT authentication & authorization
- MySQL database design with JPA/Hibernate
- Responsive Bootstrap 5 frontend
- RESTful API design

---

**FoodBridge** - Connecting surplus food with those who need it most. 🍽️♥️