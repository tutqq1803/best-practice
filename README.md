# Best Practices & Integrations

## Table of Content

---

- [Introduction](#introduction)
- [Prerequisites](#prerequisites)
- [Installation and Getting Started](#installation-and-getting-started)
  - [Start Environment](#1-start-environment)
  - [Running app](#2-running-app)
  - [Applications URLs](#3-applications-urls)
  - [Demo](#4-demo)
- [Service Structure](#service-structure)
  - [Project diagram](#1-project-diagram)
  - [Applications](#2-applications)
    - [Movies-api](#movies-api)
    - [Movies-ui](#movies-ui)
    - [Keycloak](#keycloak)
    - [Redis](#redis)
- [Development Practice](#development-practice)
- [Integrations](#integrations)
  - [Testing](#1-testing)
    - [Unit Testing](#11-unit-testing)
    - [Integration Testing](#12-integration-testing)
  - [Development Accelerators](#2-development-accelerators)
    - [Mapstruct](#21-mapstruct)
    - [Lombok](#22-lombok)
  - [Swagger API Documentation](#3-swagger-api-documentation)
  - [Platforms](#4-platforms)
    - [Docker](#41-docker)


---

## Introduction

The goal of this project is to practice with `Spring boot`, `React`, `Redis`, `MongoDB`, `Keycloak` and more.

## Prerequisites

- [`Java 17+`](https://www.oracle.com/java/technologies/downloads/#java17)
- [`npm`](https://docs.npmjs.com/downloading-and-installing-node-js-and-npm)
- [`Docker`](https://www.docker.com/)
- [`Docker-Compose`](https://docs.docker.com/compose/install/)
- [`jq`](https://stedolan.github.io/jq)
- [`OMDb API`](https://www.omdbapi.com/) KEY

  To use the `Wizard` option to search and add a movie, you need to get an API KEY from OMDb API. In order to do it, access https://www.omdbapi.com/apikey.aspx and follow the steps provided by the website.

  Once you have the API KEY, create a file called `.env.local` in `springboot-react-keycloak/movies-ui` folder with the following content
  ```
  REACT_APP_OMDB_API_KEY=<your-api-key>
  ```

## Installation and Getting Started

### 1. Start Environment

- In a terminal and inside `springboot-react-keycloak` root folder run
  ```
  docker-compose up -d
  ```

- Wait for all Docker containers to be up and running. To check it, run
  ```
  docker-compose ps
  ```
### 2. Running app

- **movies-api**

  - Open a terminal and navigate to `springboot-react-keycloak/movies-api` folder

  - Run the following `Maven` command to start the application
    ```
    ./mvnw clean spring-boot:run -Dspring-boot.run.jvmArguments="-Dserver.port=9080"
    ```

    Once the startup finishes, `KeycloakInitializerRunner.java` class will run and initialize `company-services` realm in `Keycloak`. Basically, it will create:
    - Realm: `company-services`
    - Client: `movies-app`
    - Client Roles: `MOVIES_MANAGER` and `USER`
    - Two users
      - `admin`: with roles `MANAGE_MOVIES` and `USER`
      - `user`: only with role `USER`

  - **Social Identity Providers** like `Google`, `Facebook`, `Twitter`, `GitHub`, etc can be configured by following the steps described in [`Keycloak` Documentation](https://www.keycloak.org/docs/latest/server_admin/#social-identity-providers)

- **movies-ui**

  - Open another terminal and navigate to `springboot-react-keycloak/movies-ui` folder

  - Run the command below if you are running the application for the first time
    ```
    npm install
    ```

  - Run the `npm` command below to start the application
    ```
    npm start
    ```
### 3. Applications URLs

| Application | URL                                   | Credentials                           |
|-------------|---------------------------------------|---------------------------------------|
| movie-api   | http://localhost:9080/swagger-ui.html | [Access Token](#getting-access-token) |
| movie-ui    | http://localhost:3000                 | `admin/admin` or `user/user`          |
| Keycloak    | http://localhost:8080/admin           | `admin/admin`                         |
### 4. Demo

- The gif below shows an `admin` logging in and adding one movie using the wizard feature

  ![demo-admin](documentation/demo-admin.gif)

- The gif below shows a `user` logging in using his Github account; then he changes his avatar and comment a movie

  ![demo-user-github](documentation/demo-user-github.gif)

## Service Structure

### 1. Project diagram

![project-diagram](documentation/project-diagram.png)

### 2. Applications

- ### movies-api

  `Spring Boot` Web Java backend application that exposes a REST API to manage **movies**.
  Its secured endpoints can just be accessed if an access token (JWT) issued by `Keycloak` is provided.

  `movies-api` stores its data in a [`Mongo`](https://www.mongodb.com/) database.

  `movie-api` has the following endpoints

| Endpoint                                                          | Secured | Roles                        |
|-------------------------------------------------------------------|---------|------------------------------|
| `GET /api/userextras/me`                                          | Yes     | `MOVIES_MANAGER` and `USER`  |
| `POST /api/userextras/me -d {avatar}`                             | Yes     | `MOVIES_MANAGER` and `USER`  | 
| `GET /api/movies`                                                 | No      |                              |
| `GET /api/movies/{imdbId}`                                        | No      |                              |
| `POST /api/movies -d {"imdb","title","director","year","poster"}` | Yes     | `MOVIES_MANAGER`             |
| `DELETE /api/movies/{imdbId}`                                     | Yes     | `MANAGE_MOVIES`              |
| `POST /api/movies/{imdbId}/comments -d {"text"}`                  | Yes     | `MOVIES_MANAGER` and `USER`  |

- ### movies-ui

  `ReactJS` frontend application where `users` can see and comment movies and `admins` can manage movies. In order to access the application, `user` / `admin` must login using his/her username and password. Those credentials are handled by `Keycloak`. All the requests coming from `movies-ui` to secured endpoints in `movies-api` have a access token (JWT) that is generated when `user` / `admin` logs in.

  `movies-ui` uses [`Semantic UI React`](https://react.semantic-ui.com/) as CSS-styled framework.

- ### keycloak

  As `Keycloak` supports [`PKCE`](https://tools.ietf.org/html/rfc7636) (`Proof Key for Code Exchange`) since version `7.0.0`, we are using it in this project.

- ### redis

## Development Practice

At the core of the Cloud Native Practices in Software Engineering lies the Behavior Driven Development(BDD) and
Test-Driven Development (TDD).<br/>
While developing the code I followed BDD first approach where I wrote a failing feature/acceptance criteria thus
driving our development through behavior and then followed by Test Driven Development.<br/>
A feature is not considered as developed until all the Unit Tests (TDD) and feature (BDD) passes.

![bdd-tdd-cycle](documentation/bdd-tdd-cycle.png)

## Integrations

### 1. Testing

#### 1.1 Unit Testing
-  // TODO: Update later


#### 1.2 Integration Testing

- #### Getting Access Token

  - Open a terminal
  - Run the following commands to get the access token
    ```
    ACCESS_TOKEN="$(curl -s -X POST \
      "http://localhost:8080/realms/company-services/protocol/openid-connect/token" \
      -H "Content-Type: application/x-www-form-urlencoded" \
      -d "username=admin" \
      -d "password=admin" \
      -d "grant_type=password" \
      -d "client_id=movies-app" | jq -r .access_token)"

    echo $ACCESS_TOKEN
    ```
  >   **Note**: In [jwt.io](https://jwt.io), you can decode and verify the `JWT` access token

- #### Calling movies-api endpoints using curl

  - Trying to add a movie without access token
    ```
    curl -i -X POST "http://localhost:9080/api/movies" \
      -H "Content-Type: application/json" \
      -d '{ "imdbId": "tt5580036", "title": "I, Tonya", "director": "Craig Gillespie", "year": 2017, "poster": "https://m.media-amazon.com/images/M/MV5BMjI5MDY1NjYzMl5BMl5BanBnXkFtZTgwNjIzNDAxNDM@._V1_SX300.jpg"}'
    ```

    It should return
    ```
    HTTP/1.1 401
    ```

  - Trying again to add a movie, now with access token (obtained at #getting-access-token)
    ```
    curl -i -X POST "http://localhost:9080/api/movies" \
      -H "Authorization: Bearer $ACCESS_TOKEN" \
      -H "Content-Type: application/json" \
      -d '{ "imdbId": "tt5580036", "title": "I, Tonya", "director": "Craig Gillespie", "year": 2017, "poster": "https://m.media-amazon.com/images/M/MV5BMjI5MDY1NjYzMl5BMl5BanBnXkFtZTgwNjIzNDAxNDM@._V1_SX300.jpg"}'
    ```

    It should return
    ```
    HTTP/1.1 201
    {
      "imdbId": "tt5580036",
      "title": "I, Tonya",
      "director": "Craig Gillespie",
      "year": "2017",
      "poster": "https://m.media-amazon.com/images/M/MV5BMjI5MDY1NjYzMl5BMl5BanBnXkFtZTgwNjIzNDAxNDM@._V1_SX300.jpg"
    }
    ```

  - Getting the list of movies. This endpoint does not require access token
    ```
    curl -i http://localhost:9080/api/movies
    ```

    It should return
    ```
    HTTP/1.1 200
    [
      {
        "imdbId": "tt5580036",
        "title": "I, Tonya",
        "director": "Craig Gillespie",
        "year": "2017",
        "poster": "https://m.media-amazon.com/images/M/MV5BMjI5MDY1NjYzMl5BMl5BanBnXkFtZTgwNjIzNDAxNDM@._V1_SX300.jpg",
        "comments": []
      }
    ]
    ```

- #### Calling movies-api endpoints using Swagger

  - Access `movies-api` Swagger website, http://localhost:9080/swagger-ui.html

  - Click `Authorize` button.

  - In the form that opens, paste the `access token` (obtained at [getting-access-token](#getting-access-token)) in the `Value` field. Then, click `Authorize` and `Close` to finalize.

  - Done! You can now access the secured endpoints


### 2. Development Accelerators

#### [2.1 Mapstruct](https://mapstruct.org/)

An excellent\* library for converting VO to DAO objects and vice versa.
#### [2.2 Lombok](https://projectlombok.org/)

Provides excellent annotations based support for Auto generation of methods, logging, Builders, Validation etc.
We will be using below annotations during this exercise:<br/>
@Data: Auto generates setters, getters, hashcode and toString methods<br/>
@Slf4j: Just add this annotation on top of any Spring Bean and start using the log
### 3. Swagger API Documentation


With the latest version of swagger you just need to include a single dependency as below & that's it.
```bash
implementation "io.springfox:springfox-boot-starter:${swaggerVersion}"
```

although if you are using WebMvc then you would require to additionally add @EnableWebMvc as stated in doc [here.](https://springfox.github.io/springfox/docs/snapshot/)

To test it locally start the application then Swagger UI documentation can be accessed by URL:

```bash
http://localhost:9080/swagger-ui.html
```

![](documentation/swagger-ui.png)
Once application is deployed in a Platform the same documentation will be accessible by below URL:

```bash
http://[HOST_URL]/swagger-ui/index.html
```

### 4. Platforms

#### 4.1 Docker
-  // TODO: Update later




