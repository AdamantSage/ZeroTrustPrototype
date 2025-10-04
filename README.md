# Zero-Trust IoT + Blockchain Framework

A proof‑of‑concept mono‑repo implementing a zero‑trust architecture for IoT devices using:
- Azure IoT Hub (device ingestion)
- Spring Boot (policy engine & dashboard)
- SQL Server (data persistence)
- Ganache + Truffle + Web3j (blockchain trust recording)

## Getting Started

1. Clone this repo.
2. Install prerequisites: Java 17, Maven, Node.js, Docker.
3. Run infra:
   ```bash
   cd infra
   docker-compose up -d
   ./init-sql.sh
   ```
4. Start services:
   - `device-simulator`
   - `blockchain` (`ganache` + `truffle migrate`)
   - `zero-trust-engine`
5. Browse dashboard: `http://localhost:8080`

See individual README files in each folder for more details.
