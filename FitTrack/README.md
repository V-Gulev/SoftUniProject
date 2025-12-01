# FitTrack - Fitness Tracking Application

FitTrack is a comprehensive web application designed to help users track their fitness journey, including setting goals, creating workout plans, logging workouts, and earning badges for their achievements.

## Tech Stack

### Backend
- **Java 17**
- **Spring Boot 3**
- **Spring Security:** For authentication, authorization, and security.
- **Spring Data JPA (Hibernate):** For database interaction and persistence.
- **MySQL:** Relational database for the main application.
- **Lombok:** To reduce boilerplate code in model/DTO classes.
- **Spring Cache:** For performance optimization.
- **Spring Scheduler:** For running background tasks.
- **Maven:** For dependency management and build automation.

### Frontend
- **Thymeleaf:** Server-side template engine for rendering dynamic HTML.
- **HTML5 & CSS3:** For structure and styling.

### Microservice & Integration
- **Spring Cloud OpenFeign:** For declarative REST API communication.
- **badge-service (External Microservice):** A separate service responsible for managing user badges.

## Supported Features & Functionalities

### User Management (Does not count toward graded functionalities)
- User Registration and Login
- Secure Password Hashing (BCrypt)
- Role-based access control (USER, ADMIN)
- Profile Viewing and Editing
- Automatic logout of inactive users.

### Core Domain Functionalities
1.  **Create, Update, Delete, and Complete Goals:** Users can define specific, measurable fitness goals (e.g., "Run 5k", "Bench Press 100kg").
2.  **Create, Update, and Delete Workout Plans:** Users can create detailed weekly workout schedules.
3.  **Set a Workout Plan as Active:** Users can designate one of their plans as their primary, active plan.
4.  **Log a Workout:** Users can log their daily workouts, optionally associating them with a workout plan.
5.  **Update and Delete Workout Logs:** Users can correct or remove past workout entries.

### Admin Functionalities
- **User Management:** Admins can view a list of all users.
- **Block/Unblock Users:** Admins have the ability to block or unblock users, preventing or allowing them from logging into the application.

## Integrations
The main application integrates with an external **`badge-service`** microservice via a REST API.

- **Award a Badge:** When a user achieves a milestone (e.g., creates their first goal, completes 10 workouts), the main application calls the `badge-service` via a `POST` request to award a new badge.
- **Revoke a Badge:** Admins (or users, depending on UI) can trigger a `DELETE` request to the `badge-service` to remove a badge.
- **View Badges:** The user's profile page makes a `GET` request to the `badge-service` to fetch and display all earned badges.

## Bonus Features

### AI Integration
- **Spring AI with Google Gemini:** Implemented an AI-powered chatbot using Spring AI and the Google Gemini LLM to provide fitness advice and answer user queries.

### Enhanced Security & Authentication
- **OAuth2 Integration:** Implemented social login functionality allowing users to sign in using their existing accounts from:
    - **Google**
    - **Facebook**
    - **GitHub**