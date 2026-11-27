# Development Setup for HealthyRolls

## Java Version
We are using Java 25.
```sh
java --version  # needs to be 25
```

If it isn't 25, either you have it installed and haven't configured your path, or you need to install it.

## VSCode Plugins

```sh
# windows / linux / mac
code --install-extension vscjava.vscode-java-pack
code --install-extension shengchen.vscode-checkstyle

```
Refresh the window by going `Cmd+Shift+P > Developer: Reload Window`.

Then to run linting on the `server` :
```sh
cd server

# mac / linux
chmod +x mvnw   # mark mvnw as executable

# windows powershell / mac / linux
./mvnw spotless:apply
./mvnw spotless:check
```
## Frontend

Requires Node.js 22.12 or newer and npm.

In a second terminal, from the repository root:
```sh
cd client
npm ci
npm run dev
```

Open http://localhost:5173. Keep the backend running on port 8080.

To build the frontend:
```sh
npm run build
```
The output is generated in `client/dist`.

### Frontend characterisation tests

From `client`, install the locked dependencies and run the Navbar tests:
```sh
npm ci
npm test -- src/dash/components/Navbar.test.jsx
```

`npm test` runs all frontend tests once; `npm run test:watch` reruns them as files change.
The tests use Vitest, React Testing Library and Happy DOM. They exercise the existing
Navbar through its UI, including account-menu interactions, role permissions, logout,
and system reset confirmation, success and failure. The reset API is mocked, so no
backend or database is needed and running the tests cannot reset application data.

For Phase IV, commit the passing characterisation tests before extracting the account
menu, then run these same tests on the refactor commit. Their assertions describe
observable behaviour and do not depend on the internal component structure.

## Database

Postgres is the default. Start it first:
```sh
docker compose up -d # from the repo root
```

Then run the server:
```sh
cd server
./mvnw spring-boot:run
```

Port used: **5433**
Connect a client with
`postgresql://healthyrolls:healthyrolls@localhost:5433/healthyrolls`.

Data lives in the `healthyrolls-db` volume and survives restarts. To wipe it:
```sh
docker compose down -v
```

### Seeding

To seed, pass `--seed`:
```sh
./mvnw spring-boot:run "-Dspring-boot.run.arguments=--seed"
```

Restaurants seed and users seed is located in `server/src/main/resources/seed/` folder as `restaurants.json` and `seed/users.json` respectively. Restaurants are skipped when the table is non-empty. Users are matched on email, so re-running `--seed` is safe.

### Running without Docker

The `dev` profile uses an in-memory H2 database instead. It does not persist between restarts.
```sh
# mac/linux
SPRING_PROFILES_ACTIVE=dev ./mvnw spring-boot:run

# windows ps
$env:SPRING_PROFILES_ACTIVE="dev"; ./mvnw spring-boot:run
```
