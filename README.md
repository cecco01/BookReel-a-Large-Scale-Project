# BookReel 📚🎥: A Large-Scale and Multi-Structured Database Project

BookReel is a content discovery and sharing platform focused on books and movies.  
The system brings together users with comparable preferences by offering personalized recommendations, analytical insights, and interactive social functionalities.

---

## Features

- **Books & Movies Tracking**  
  Build customized lists to monitor what you are reading or watching and keep track of your media's consumption over time.

- **Social Interactions**  
  Follow other users, view their collections, and engage with a community built around shared interests.

- **Smart Recommendations**  
  Receive suggestions generated from your preferences and overall platform trends.

- **Admin Tools**  
  Oversee and maintain the media catalog, moderate reviews, and analyze users' activity.

- **Scalable Performance**  
  Designed for high availabilit, and quick response times even when handling large volumes of data.

---

## Tech Stack

- **Backend**: Spring Framework, RESTful APIs 
- **Databases**: MongoDB (document database), Neo4j (graph database)  
- **Security**: JWT for authentication, encrypted passwords
- **Dataset**: Data sourced from the TMDB API and Open Library API


---

## Repository Structure

- `/src` – Core application code  
- `/models` – Data models for MongoDB and Neo4j  
- `/controllers` – REST API endpoints for user, media, and admin operations  
- `/services` – Core business logic and analytical components  

---

## API

- **Authentication**  
  `POST /api/auth/register`  
  `POST /api/auth/login`

- **User Operations**  
  Handle profile management, list organization, and follower relationships.

- **Media Management**  
  Explore available books and films, submit reviews, and track media consumption progress.

- **Admin Analytics**  
  Monitor trends, evaluate user engagement, and assess content performance.

- **Recommendations**  
  Discover potentially interesting content and users with shared interests.
