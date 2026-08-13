# Employee Leave Hub

Aplicatie web pentru gestionarea completa a ciclului de viata al cererilor de concediu:
depunere de catre angajat, aprobare/respingere de catre responsabilul de departament sau
administrator, calendar comun al departamentului si generare de rapoarte PDF

Structura proiectului:
 - backend:    Spring Boot + Spring Security/JWT + Spring Data JPA + PostgreSQL + OpenPDF
 - frontend:   Angular 17 + Angular Router + HttpClient

Necesitati:
- Java 11+ (JDK si JRE)
- Maven 3.6+
- PostgreSQL 13+
- Node.js si npm

* start.bat - doar daca totul este deja set-up, pentru rulari mai simple
(un script mic pentru a rula frontend si backend simultan in terminal)

## 1. Backend

### Configurare bazei de date (PostgreSQL):  *necesar
```sql
CREATE DATABASE leave_hub;
CREATE USER leave_hub_user WITH PASSWORD 'leave_hub_pass';
GRANT ALL PRIVILEGES ON DATABASE leave_hub TO leave_hub_user;
```

Datele de conectare sunt in `backend/src/main/resources/application.yml` (`spring.datasource.*`).
Schema este creata automat de Hibernate (`ddl-auto: update`) la prima pornire

### Rulare manuala
```bash
cd backend
mvn spring-boot:run
```
### Pentru rulare fara PostgreSQL
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

Backend-ul porneste pe `http://localhost:8080`. La prima pornire, `DataSeeder` face:
- tipuri de concediu: CO (concediu de odihna), CM (medical, necesita document), FP (fara plata), SPECIAL
- 3 departamente: IT, HR, Productie
- 3 conturi demo:  Rol / Email / Parola 

 Administrator / admin@draxlmaier.com / Admin123! 

 Responsabil departament (IT) / manager.it@draxlmaier.com / Manager123! 
 
 Angajat (IT) / maria.ionescu@draxlmaier.com / User123! 


## 2. Frontend

### Rulare manuala
```bash
cd frontend
npm install
npm start
```
Aplicatia porneste pe `http://localhost:4200` si comunica cu backend-ul de pe `http://localhost:8080/api`
(configurabil in `frontend/src/environments/environment.ts`)
