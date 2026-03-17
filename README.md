# HotelManagementApp — How to compile & run

This project contains `HotelManagementApp.java` — a simple CLI hotel management demo that uses MySQL.

Prerequisites
- Java JDK (javac/java) installed and on PATH.
- MySQL server running and accessible at the URL in `HotelManagementApp.java` (default: `jdbc:mysql://localhost:3306/hotel_management?serverTimezone=UTC`).
- A database named `hotel_management` (create if needed):

  ```sql
  CREATE DATABASE hotel_management;
  ```

- MySQL Connector/J (JAR) placed at `mysql-connector-j-9.4.0/mysql-connector-j-9.4.0.jar` relative to the project root. If your jar is in a different path, update `run.sh` or use full path when running.

Files added
- `run.sh` — helper script to compile and run. Usage shown below.

Quick commands (zsh/macOS)

Compile and run interactively:

```bash
./run.sh
```

Only compile:

```bash
./run.sh compile
```

Run the built-in non-interactive test (useful for CI or quick checks):

```bash
./run.sh test
```

Manual compile & run (equivalent):

```bash
javac -cp .:/path/to/mysql-connector-j-9.4.0.jar HotelManagementApp.java
java -cp .:/path/to/mysql-connector-j-9.4.0.jar HotelManagementApp
```

Notes & troubleshooting
- If you see a "Could not make reservation" or FK error, ensure the guest and room IDs you use exist in the database, or add them via the menu (options 1-3).
- If the app cannot connect to MySQL, verify credentials in `HotelManagementApp.java` and your MySQL server is running and accepting connections from your host.
- If you expect heavier usage or multi-threaded DB access, consider using a connection pool (HikariCP) rather than opening raw connections in the background monitor.

Want me to:
- Create a fat JAR so you can run `java -jar HotelManagementApp.jar`?
- Wire a small Gradle or Maven build file to manage the MySQL connector dependency?
- Add a sample SQL file that inserts sample hotels/rooms/guests for quick testing?

Tell me which and I will add it.