# ISO 20022 pain.001 Lite

Builds a **simplified** ISO 20022 Customer Credit Transfer Initiation (`pain.001.001.03`-style) XML document from JSON.

Inspired by the pain.001 generation idea in projects such as [bank4j](https://github.com/inisos/bank4j) (MIT). This code is an independent lite builder for learning and demos, not a full XSD-validated payments stack.

## Architecture

```mermaid
sequenceDiagram
    participant C as Client
    participant API as PainController
    participant B as Pain001Builder
    C->>API: POST /api/pain001/build
    API->>B: parties + amounts + remittance
    B-->>API: messageId + XML
    API-->>C: JSON (or /build.xml)
```

## Quick start

```bash
./mvnw test
./mvnw spring-boot:run
```

HTTP: `http://localhost:8087`

## License

[MIT](LICENSE)

## Notes

Not a full XSD-validated payments stack; use for learning and demos.

