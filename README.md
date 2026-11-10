# GiftWise

Gift and group contribution platform.

- `backend/`: Java 21 / Spring Boot API with Maven wrapper.
- `web/`: Next.js application.
- `website/`: public marketing website.

## Local setup

Copy `backend/src/main/resources/application.properties.example` to `application.properties` in the same directory. Supply the environment variables referenced in that file, including database, payment, email and OTP credentials. The local configuration is deliberately excluded from Git.

Run the backend with `cd backend && ./mvnw spring-boot:run`. For either frontend, run `npm ci` and `npm run dev` in its directory. See component documentation for service-specific configuration.

Do not commit local credentials, email inboxes, database exports, backups or generated build output.

## Current documentation

Start with [the documentation index](DOCUMENTATION_INDEX.md) for the maintained technical, application and deployment guides. Historical backups have been moved outside this project without permanent deletion.
