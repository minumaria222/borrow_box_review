# BorrowBox – Student Item Sharing

BorrowBox is a Java 21 / Spring Boot prototype for sharing course books, lab gear and other useful items between students. Its demo flow uses real REST APIs and MySQL persistence; the visible student switch is a demonstration convenience, not production authentication.

## Run the demo

Prerequisites: Java 21 or newer, Maven, and Docker Compose.

1. Start MySQL from the project directory:

	```sh
	docker compose up -d db
	```

2. Start the Spring Boot application:

	```sh
	mvn spring-boot:run
	```

3. Open [http://localhost:8080](http://localhost:8080).

The default database settings match `docker-compose.yml`. Override `DB_URL`, `DB_USERNAME` and `DB_PASSWORD` to use another MySQL server. The first application startup creates the schema through JPA and seeds Asha (CSE), Rahul (ECE), Anjali (Mechanical), and eight sample items. The initializer checks existing records, so restarting does not duplicate the demo data. Demo listings are only seeded when the items table is empty.

## Judge flow

1. Select Asha and browse to the Scientific Calculator.
2. Submit a borrowing request with dates.
3. Switch to Rahul and open Owner desk; approve the request.
4. Switch back to Asha and confirm the request is approved under My requests.
5. Switch to Rahul and mark the request returned. The calculator becomes available again.

The same API supports listing, editing and deleting a student's own items, browsing filters, canceling pending requests, rejecting requests and owner-managed returns. A listing with existing request history cannot be deleted so that the request record remains intact.

## API overview

- `GET/POST /api/students`
- `GET/POST /api/items`, `GET/PUT/DELETE /api/items/{id}`
- `POST /api/requests`, `GET /api/requests/mine?studentId=…`, `GET /api/requests/received?ownerId=…`
- `PUT /api/requests/{id}/approve|reject|cancel|return`

Static HTML, CSS and JavaScript are served from Spring Boot at the same origin. Bootstrap, icons, fonts and sample photography load from public CDNs, so those visual assets need an internet connection. Do not use the demo selector as authentication in a production deployment.