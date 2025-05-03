# Fabflix - Project 2

## Demo Video
https://youtu.be/rVZG0Ln3onE

## Team Contributions
- **Lucas Kim**: Frontend integration, AWS deployment
- **Brian Seo**: Servlet logic, backend integration, AWS deployment

## AWS Deployment Info
- **AWS Public IP**: `54.183.57.170`
- **Tomcat Manager**:  
  http://54.183.57.170:8080/manager/html  
  Username: admin  
  Password: mypassword

- **Project Web URL**:  
  http://54.183.57.170:8080/project2/main.html

---

## Overview
This project extends the Fabflix application with full-featured browsing, searching, session-based shopping cart, checkout, and payment functionalities using a modern web architecture. The frontend communicates with the backend via RESTful servlets. We deployed the system on an AWS EC2 instance using Apache Tomcat.

---

## Implemented Features

### Login Page
- Entry point of Fabflix.
- Validates credentials using HTTP POST against the MySQL `customers` table.
- Redirects to `main.html` upon successful login.
- Displays error messages on invalid credentials.
- Enforces session-based access — all other pages redirect to login if unauthenticated.

### Main Page
- Entry point after login.
- Allows browsing or searching for movies.
- Contains global navigation bar with links to checkout, search, browse, and logout.

### Search Functionality
- Search by **title**, **year**, **director**, **star name**.
- Supports substring matching using SQL `LIKE` for VARCHAR fields:
  ```sql
  SELECT * FROM movies WHERE title LIKE '%keyword%' AND director LIKE '%keyword%';
  ```
- Uses AND logic between multiple filters.

### Browse Functionality
- Browse by **genre** or by **title's first character** (0-9, A-Z, and *)
- Clicking a genre/title character fetches the corresponding movie list from the database.

### Movie List Page
- Displays movies with:
  - Title (hyperlinked to single movie page)
  - Year, director, rating
  - Top 3 genres (alphabetically), each hyperlinked
  - Top 3 stars (by movie count), each hyperlinked
- Features:
  - Sorting (title/rating, asc/desc)
  - Pagination (prev/next buttons)
  - Change number of items per page (10, 25, 50, 100)
  - Maintains state across navigation via session attributes

### Jump Functionality
- Jump between:
  - Jump back returns user to the same movie list view with all filters, pagination, and sort state maintained (via HTTP session).

### Shopping Cart
- Adds movies from any movie or detail page.
- Cart stored in session.
- Shopping cart page supports:
  - Update quantity
  - Delete item
  - Display movie title, quantity, price, total
  - Proceed to payment

### Payment Page
- Form asks for:
  - First and last name
  - Credit card number
  - Expiration date
- Validates against `creditcards` table
- On success:
  - Records sale into `sales` table
  - Redirects to order confirmation

### Order Confirmation Page
- Displays sale ID, items, quantities, total price
- Confirms successful transaction

---

## Substring Matching
Implemented using SQL `LIKE` pattern matching:
```sql
SELECT * FROM movies WHERE title LIKE '%term%' AND director LIKE '%vid m%' AND year = 2007;
```
- Only VARCHAR fields (`title`, `director`, `star name`) support `%keyword%` matching.
- Integer fields like `year` require exact match.

Examples:
- title="term" → matches "Terminator", "Terminal"
- director="vid m" → matches "David Mamet"

---

## Technologies Used
- **Frontend**: HTML5, CSS3, JavaScript, AJAX
- **Backend**: Java Servlets
- **Database**: MySQL 8.0
- **Server**: Apache Tomcat 10.1
- **Deployment**: AWS EC2, port 8080 open to UCI IP range

---

2. Confirm MySQL and Tomcat are running
3. Build WAR:
   ```bash
   cd ~/project2
   mvn clean package
   cp target/project1.war /var/lib/tomcat/webapps/
   ```
4. Navigate to `http://54.183.57.170:8080/project2/main.html` in a browser

---

## Demo Scenarios
See full walkthrough in the [Demo Video](https://youtu.be/BdUxGIbBSNo?si=uE2D6VM92ZIQTU5U).

