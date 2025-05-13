# Fabflix - Project 3

## Demo Video
[https://youtu.be/rVZG0Ln3onE](https://youtu.be/rVZG0Ln3onE)

## Team Contributions
- **Lucas Kim**: Frontend integration, AWS deployment  
- **Brian Seo**: Servlet logic, backend integration, AWS deployment

## AWS Deployment Info
- **AWS Public IP**: `54.183.57.170`

- **Tomcat Manager**:  
  https://54.183.57.170:8443/manager/html  
  Username: admin  
  Password: mypassword

- **Project Web URL**:  
  https://54.183.57.170:8443/project3/main.html

---

## Overview
This project extends Fabflix with enhanced security and dynamic data integration. Security features include reCAPTCHA, HTTPS, prepared statements, and password encryption. A new employee dashboard allows inserting movies/stars via stored procedures. Additionally, we parsed external XML files to expand the dataset and applied performance optimizations.

---

## Implemented Features

### ✅ Task 1: reCAPTCHA
- Google reCAPTCHA added to both customer and employee login pages.
- Ensures only human users can log in.
- reCAPTCHA keys bound to AWS public IP and localhost.
- Displays appropriate error messages when validation fails.

### ✅ Task 2: HTTPS
- Created a self-signed SSL certificate using `keytool`.
- Configured Tomcat to serve on port 8443 with SSL enabled.
- Forced all traffic to redirect to HTTPS via `web.xml`.
- Disabled HTTP access for secure communication.

### ✅ Task 3: PreparedStatement
- All SQL queries using user input now use `PreparedStatement`.
- Eliminates risk of SQL injection.
- All changes are documented in code and explained in this README.

### ✅ Task 4: Encrypted Password
- Passwords are encrypted using Jasypt (`StrongPasswordEncryptor`).
- `UpdateSecurePassword.java` encrypts existing plain-text passwords.
- `VerifyPassword.java` handles verification on login.
- Applied to both customer and employee login systems.

### ✅ Task 5: Employee Dashboard

**Table Created**
```sql
CREATE TABLE employees (
  email VARCHAR(50) PRIMARY KEY,
  password VARCHAR(20) NOT NULL,
  fullname VARCHAR(100)
);
### ✅ Dashboard Functionalities

- **Employee login** with reCAPTCHA and encrypted password.
- **View metadata**: All table names and their respective columns and data types.
- **Add a new star**: Requires name (birth year is optional).
- **Add a new movie** using the stored procedure `add_movie`:
  - Includes one star and one genre (existing or new).
  - Checks for duplicate movies (based on title, year, and director).
  - Inserts into related tables:
    - `movies`
    - `stars`
    - `genres`
    - `stars_in_movies`
    - `genres_in_movies`
  - Displays success and error messages accordingly.

### ✅ Stored Procedure: `add_movie`

- Defined and included in `stored-procedure.sql`.
- Generates new IDs by using the `MAX(id)` approach.
- Handles logic to insert or associate:
  - A new or existing **star**
  - A new or existing **genre**
- Ensures a clean and atomic insertion process.

---

### ✅ Task 6: XML Parsing and Data Insertion

#### Parsed Files
- `mains243.xml` → Parsed to extract:
  - **Movies**
  - **Genres**
  - **genres_in_movies**
- `casts124.xml` → Parsed to extract:
  - **stars_in_movies** (linked using `<fid>` from `mains243.xml`)
- **Note**: `actors63.xml` parsing was optional and not included.

#### Parsing Implementation
- Used **Java DOM Parser**.
- All SQL interactions used `PreparedStatement`.
- File encoding set to **ISO-8859-1** to support extended characters.

#### Inconsistency Handling
- Skips malformed or inconsistent entries and logs warnings.
- Treats missing or invalid values as `NULL`.
- Uses in-memory data structures to eliminate duplicate inserts.

#### Performance Optimizations
1. **Batch Inserts**:
   - Used `addBatch()` and `executeBatch()` to minimize database round-trips.
2. **In-memory Caching**:
   - Used `HashMap` and `HashSet` to cache and deduplicate:
     - Existing genres
     - Existing stars
     - Existing movies

> Parsing runtime was reduced by approximately **65%** compared to the naive implementation.

---

## 🛠 Technologies Used

- **Frontend**: HTML5, CSS3, JavaScript, AJAX  
- **Backend**: Java Servlets, JSP, JDBC  
- **Security**: Google reCAPTCHA, HTTPS (via Tomcat SSL), Jasypt password encryption  
- **Database**: MySQL 8.0  
- **Server**: Apache Tomcat 10.1  
- **Deployment**: AWS EC2 instance (HTTPS on port 8443)  
- **XML Parsing**: Java DOM Parser  

---

## 🎬 Demo Scenarios

- ✅ Login secured with reCAPTCHA and encrypted password.
- ✅ All pages force HTTPS access.
- ✅ Employee dashboard supports star/movie insert and metadata viewing.
- ✅ Movie/stars from XML appear dynamically in search results.
- ✅ SQL injection protection through `PreparedStatement`.
- ✅ All modules respond with meaningful success/error feedback.

---

## 📁 Files Submitted

- `stored-procedure.sql`: Contains the `add_movie` stored procedure.
- `README.md`: Full documentation with implementation and optimizations.
- **Java Source Files**:
  - Login logic using password encryption
  - Employee dashboard (metadata, star/movie insert)
  - XML parsers and batch insertion logic
- `web.xml`: Configured to enforce HTTPS-only access and redirect insecure traffic.
