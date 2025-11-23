# Final Project - Gaming Store Application

A JavaFX-based e-commerce application for gaming products with user authentication, shopping cart, and order management.

## Prerequisites

- Java 21 or higher
- MySQL 8.0 or higher
- Maven 3.6 or higher

## Initial Setup

### 1. Database Configuration

**IMPORTANT:** Before running the application, you must configure your database connection.

1. Navigate to `src/main/resources/`
2. Copy `db.properties.example` to `db.properties`:
   ```bash
   # On Windows (PowerShell)
   Copy-Item src\main\resources\db.properties.example src\main\resources\db.properties

   # On Windows (Command Prompt)
   copy src\main\resources\db.properties.example src\main\resources\db.properties

   # On Linux/Mac
   cp src/main/resources/db.properties.example src/main/resources/db.properties
   ```

3. Edit `db.properties` with your MySQL credentials:
   ```properties
   db.url=jdbc:mysql://localhost:3306/pr1
   db.username=your_mysql_username
   db.password=your_mysql_password
   ```

**Note:** The `db.properties` file is excluded from version control for security. Never commit database credentials.

### 2. Database Setup

1. Create the database:
   ```sql
   CREATE DATABASE pr1;
   ```

2. Run the schema file:
   ```bash
   mysql -u your_username -p pr1 < src/main/resources/schema.sql
   ```

### 3. Build the Project

```bash
mvn clean install
```

This will:
- Compile the Java source code
- Copy resources (including db.properties) to target/classes
- Download dependencies
- Run tests

### 4. Run the Application

Option 1 - Using Maven:
```bash
mvn javafx:run
```

Option 2 - Using your IDE:
- Run the main class: `com.example.finalproject.HelloApplication`

## Troubleshooting

### "Unable to find db.properties file" Error

This means the database configuration file is missing. Follow step 1 in the Initial Setup section above.

### "The url cannot be null" Error

This means:
1. The `db.properties` file exists but is empty or malformed
2. The project wasn't rebuilt after adding db.properties
3. Solution: Verify db.properties content and run `mvn clean install` again

### Connection Refused Errors

1. Verify MySQL is running on your machine
2. Check the database name, username, and password in db.properties
3. Ensure the database `pr1` exists

## Project Structure

```
FinalProjectSrc/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/finalproject/
│   │   │       ├── controller/    # UI Controllers
│   │   │       ├── dao/           # Database Access Objects
│   │   │       ├── model/         # Data Models
│   │   │       ├── service/       # Business Logic
│   │   │       └── HelloApplication.java
│   │   └── resources/
│   │       ├── com/example/finalproject/
│   │       ├── db.properties.example  # Database config template
│   │       └── schema.sql            # Database schema
│   └── test/
└── pom.xml
```

## Security Notes

- Never commit `db.properties` to version control
- The file is already in `.gitignore` for your protection
- Use different credentials for development and production environments
- Update database passwords regularly

## Features

- User Authentication (Registration/Login)
- Product Browsing and Search
- Shopping Cart Management
- Order Processing
- Wishlist
- User Profile Management
- Admin Dashboard
- Loyalty Points System

## Technologies Used

- JavaFX 21
- MySQL 8.3
- Maven
- BCrypt for password hashing
- JWT for session management
- iText PDF for receipts
