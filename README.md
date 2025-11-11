# Backend
## ER diagram
<img width="1600" height="856" alt="image" src="https://github.com/user-attachments/assets/af29306e-68f9-465a-8051-61b47ecfaa37" />

Testing

## How to run
First, duplicate the .env.example and rename it to .env
<br>
This file contains all the environment variables to run the app such as:
```
DATABASE_USER=user
DATABASE_NAME=e-commerce
DATABASE_PORT=3307
DATABASE_HOST=db
BACKEND_PORT=8000
DATABASE_PASSWORD=password
```
You are free to change them if needed.
<br>
Once this file has been copied, run the command:
```
docker compose watch
```
This will start the containers and enable watch mode, such that the containers will restart with every change made in the code.
<br>
Once you see in the logs something like this
```
2025-09-01T00:46:59.736Z  INFO 1 --- [e-commerce] [           main] c.p.e_commerce.ECommerceApplication      : Started ECommerceApplication in 23.826 seconds (process running for 25.319)
```

The application has started and you're now able to run requests to the api.

## Making requests
There are several options for making requests, in this section we will be discussing how to make requests using the swagger UI or postman.

### Swagger UI
Once the application started head to the following link:
> [!NOTE]
> The URL can change depending on the Environment variables

> http://localhost:8000/swagger-ui/index.html

Once there, you can find all the different endpoints of the application.
After clicking once of them, you can see the path variables, query variables and body needed. Click try out and fill everything and then click <strong>Execute</strong> to execute the request.
<img width="1322" height="649" alt="image" src="https://github.com/user-attachments/assets/f2257679-e1c0-4ec6-bbd3-87d6fe1f6e98" />


### Postman
Once postman is installed, create a new request in your collections.
You can configure this request according to what the endpoint requires.
<img width="1321" height="585" alt="image" src="https://github.com/user-attachments/assets/88104e26-cb28-4124-a54a-3ac6d9f3ca87" />


## Project structure
The project is separeted in several folders, this section explain each one of them

### Controller
This folder contains the controllers and all of its endpoints of the application.
All controllers have the `@RestController` annotation alongside a `@RequestMapping` annotation such as
```
@RequestMapping("/api/v1/example")
```
The `@RequestMapping` annotation indicates that all of the endpoints of that controller starts with the string given to the annotation.

Inside the controller you will find the endpoints, the endpoints are functions that have one of the following annotations:
```
@GetMapping
@PostMapping
@PutMapping
@PatchMapping
@DeleteMapping
```
Each of these annotations can have a string parameter that expands the endpoint URI such that
```
@RestController
@RequestMapping("/api/v1/example")
public class ExampleController{
    @GetMapping("/hello")
    public String helloWorld(){
        return "Hello World!!!";
    }
}
```
would create a <strong>GET</strong> endpoint with the URI <strong>/api/v1/example/hello</strong> that responds with the string "Hello World!!!"
### DTO
This folder contains all the different dtos used throught the project. A dto is an object used for communication (such as a request or response) where we define the data we want to send or receive.
An example is when creating a customer, we only want the username, email and password, so we define a dto that holds those 3 data. When fetching a customer we define another dto that holds all the data of the customer except for the password, as we do not wish to send to the password.
To learn more about DTO you can visit this link:
> https://www.baeldung.com/java-dto-pattern

### Entity
This folder contains the different entities(tables) found in the database. By running migrations, the database gets the schemas found in <strong>resources/db/migration</strong>. The classes found in the entity folder should match the state of the database.
<br>
If it is required to run a specific version of the database structure, it can be specify with the next Environment variable
```
FLYWAY_TARGET=latest
```
### Exceptions
Here we can find the different exceptions that can be thrown durring the execution of the program.
<br>
Each one gets their status code via the `@ResponseStatus` annotation.
<br>
The class `GlobalExceptionHandler` handles how the response is structure when each of the exceptions gets thrown. When creating a new exception make sure to specify it's response on this class.
### Mapper
A mapper handles turning a DTO into an entity and viceversa
### Repository
Here is where we connect to the database and execute queries. We create an interface for each of the tables and said interface inherits from `CrudRepository<T,ID>` or `JpaRepository<T,ID>`, where T is the entity(table) and ID is the type of the entities ID.
Using CrudRepository or JpaRepository depends on the usage we want to give the repository, but overall CrudRepository is fine.
<br>
Each of the repositories needs the `@Repository` annotation.
<br>
If we want to do custom queries, we need to follow the syntax of JPA Query Methods, to know how to write queries enter the following link:
> https://docs.spring.io/spring-data/jpa/reference/jpa/query-methods.html

We can also write queries manually by using the `@Query` annotation.

### Service
This is where the business logic is done. Inside the folder we can find interfaces for each of the services, we can also see a Impl folder with implementations for each of the services.
Each implementation needs to follow interfaces rules, as well as having the `@Service` annotation.
<br>
The functions in the services are usually called by a controller and the repositories are usually used inside a service.

### Utils
Just a folder with utility functions. Doesn't really follow a convention, just add any function that can be reused.

