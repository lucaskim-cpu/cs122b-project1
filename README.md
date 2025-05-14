# 🎬 Fabflix - Project 3

## 🚀 Deployment Info
- **Public IP**: `54.183.57.170`
- **HTTPS URL**: https://100.27.203.228:8443/main.html
- **Tomcat Manager**: https://100.27.203.228:8443/manager/html
- **Credentials**:
  - Username: `admin`
  - Password: `mypassword`

## 👥 Team Members
- **Brian Seo** — XML Parsing, Secure Auth Implementation, Backend Logic, Dashboard Integration  
- **Lucas Kim** — Frontend Features, UI/UX, AWS Deployment

## 📽️ Demo Video
Could not upload due to AWS server error...

---

## 🔐 Security Enhancements

### ✅ Task 1: reCAPTCHA
- Integrated Google reCAPTCHA on the login page.
- Backend verifies reCAPTCHA token with Google's verification API.
- Keys registered for both `localhost` and AWS IP.

### ✅ Task 2: HTTPS
- Configured Tomcat SSL on port `8443` using a self-signed certificate.
- All HTTP traffic redirected to HTTPS via `web.xml` security constraints.
- HTTPS enforced on all sensitive endpoints (login, payment, employee dashboard).

### ✅ Task 3: PreparedStatement
- All servlets use `PreparedStatement` to prevent SQL injection.
- No user inputs are concatenated directly into SQL strings.
- Example:
  ```java
  String query = "SELECT * FROM movies WHERE title LIKE ?";
  PreparedStatement ps = conn.prepareStatement(query);
  ps.setString(1, "%" + title + "%");
