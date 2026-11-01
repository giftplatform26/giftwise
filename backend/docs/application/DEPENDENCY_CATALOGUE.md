# Dependency catalogue

Source target Java 21. Maven parent Spring Boot 4.1.1. A missing explicit Maven version means parent/dependency-management resolution, not a pinned version verified by this catalogue. npm versions below are resolved package-lock values; semver ranges in package.json are not the same thing.

## Maven declared dependencies

| Group | Artifact | Declared version | Scope |
| --- | --- | --- | --- |
| org.jsoup | jsoup | 1.23.2 | compile/default |
| org.apache.httpcomponents.client5 | httpclient5 | Managed by parent/BOM | compile/default |
| org.springframework.boot | spring-boot-starter-jackson | Managed by parent/BOM | compile/default |
| org.springframework.boot | spring-boot-starter-actuator | Managed by parent/BOM | compile/default |
| org.springframework.boot | spring-boot-starter-data-jpa | Managed by parent/BOM | compile/default |
| org.springframework.boot | spring-boot-starter-flyway | Managed by parent/BOM | compile/default |
| org.springframework.boot | spring-boot-starter-validation | Managed by parent/BOM | compile/default |
| org.springframework.boot | spring-boot-starter-webmvc | Managed by parent/BOM | compile/default |
| org.flywaydb | flyway-database-postgresql | Managed by parent/BOM | compile/default |
| org.postgresql | postgresql | Managed by parent/BOM | runtime |
| org.testcontainers | testcontainers-postgresql | Managed by parent/BOM | test |
| org.testcontainers | testcontainers-junit-jupiter | Managed by parent/BOM | test |
| org.springframework.boot | spring-boot-starter-actuator-test | Managed by parent/BOM | test |
| org.springframework.boot | spring-boot-starter-data-jpa-test | Managed by parent/BOM | test |
| org.springframework.boot | spring-boot-starter-flyway-test | Managed by parent/BOM | test |
| org.springframework.boot | spring-boot-starter-validation-test | Managed by parent/BOM | test |
| org.springframework.boot | spring-boot-starter-webmvc-test | Managed by parent/BOM | test |
| org.springframework.boot | spring-boot-starter-security | Managed by parent/BOM | compile/default |
| org.springframework.boot | spring-boot-starter-security-test | Managed by parent/BOM | test |
| org.springframework.boot | spring-boot-starter-validation | Managed by parent/BOM | compile/default |
| software.amazon.awssdk | sns | 2.30.38 | compile/default |
| com.google.zxing | core | 3.5.3 | compile/default |
| com.google.zxing | javase | 3.5.3 | compile/default |

## npm lockfile packages

| Lockfile path | Resolved version | Development | Declared license |
| --- | --- | --- | --- |
| node_modules/@emnapi/runtime | 1.11.3 | False | MIT |
| node_modules/@img/colour | 1.1.0 | False | MIT |
| node_modules/@img/sharp-darwin-arm64 | 0.35.5 | False | Apache-2.0 |
| node_modules/@img/sharp-darwin-x64 | 0.35.5 | False | Apache-2.0 |
| node_modules/@img/sharp-freebsd-wasm32 | 0.35.5 | False | Apache-2.0 |
| node_modules/@img/sharp-libvips-darwin-arm64 | 1.3.4 | False | LGPL-3.0-or-later |
| node_modules/@img/sharp-libvips-darwin-x64 | 1.3.4 | False | LGPL-3.0-or-later |
| node_modules/@img/sharp-libvips-linux-arm | 1.3.4 | False | LGPL-3.0-or-later |
| node_modules/@img/sharp-libvips-linux-arm64 | 1.3.4 | False | LGPL-3.0-or-later |
| node_modules/@img/sharp-libvips-linux-ppc64 | 1.3.4 | False | LGPL-3.0-or-later |
| node_modules/@img/sharp-libvips-linux-riscv64 | 1.3.4 | False | LGPL-3.0-or-later |
| node_modules/@img/sharp-libvips-linux-s390x | 1.3.4 | False | LGPL-3.0-or-later |
| node_modules/@img/sharp-libvips-linux-x64 | 1.3.4 | False | LGPL-3.0-or-later |
| node_modules/@img/sharp-libvips-linuxmusl-arm64 | 1.3.4 | False | LGPL-3.0-or-later |
| node_modules/@img/sharp-libvips-linuxmusl-x64 | 1.3.4 | False | LGPL-3.0-or-later |
| node_modules/@img/sharp-linux-arm | 0.35.5 | False | Apache-2.0 |
| node_modules/@img/sharp-linux-arm64 | 0.35.5 | False | Apache-2.0 |
| node_modules/@img/sharp-linux-ppc64 | 0.35.5 | False | Apache-2.0 |
| node_modules/@img/sharp-linux-riscv64 | 0.35.5 | False | Apache-2.0 |
| node_modules/@img/sharp-linux-s390x | 0.35.5 | False | Apache-2.0 |
| node_modules/@img/sharp-linux-x64 | 0.35.5 | False | Apache-2.0 |
| node_modules/@img/sharp-linuxmusl-arm64 | 0.35.5 | False | Apache-2.0 |
| node_modules/@img/sharp-linuxmusl-x64 | 0.35.5 | False | Apache-2.0 |
| node_modules/@img/sharp-wasm32 | 0.35.5 | False | Apache-2.0 AND LGPL-3.0-or-later AND MIT |
| node_modules/@img/sharp-webcontainers-wasm32 | 0.35.5 | False | Apache-2.0 |
| node_modules/@img/sharp-win32-arm64 | 0.35.5 | False | Apache-2.0 AND LGPL-3.0-or-later |
| node_modules/@img/sharp-win32-ia32 | 0.35.5 | False | Apache-2.0 AND LGPL-3.0-or-later |
| node_modules/@img/sharp-win32-x64 | 0.35.5 | False | Apache-2.0 AND LGPL-3.0-or-later |
| node_modules/@next/env | 15.5.27 | False | MIT |
| node_modules/@next/swc-darwin-arm64 | 15.5.27 | False | MIT |
| node_modules/@next/swc-darwin-x64 | 15.5.27 | False | MIT |
| node_modules/@next/swc-linux-arm64-gnu | 15.5.27 | False | MIT |
| node_modules/@next/swc-linux-arm64-musl | 15.5.27 | False | MIT |
| node_modules/@next/swc-linux-x64-gnu | 15.5.27 | False | MIT |
| node_modules/@next/swc-linux-x64-musl | 15.5.27 | False | MIT |
| node_modules/@next/swc-win32-arm64-msvc | 15.5.27 | False | MIT |
| node_modules/@next/swc-win32-x64-msvc | 15.5.27 | False | MIT |
| node_modules/@swc/helpers | 0.5.15 | False | Apache-2.0 |
| node_modules/@types/node | 22.20.5 | True | MIT |
| node_modules/@types/react | 19.3.0 | True | MIT |
| node_modules/@types/react-dom | 19.3.0 | True | MIT |
| node_modules/caniuse-lite | 1.0.30001815 | False | CC-BY-4.0 |
| node_modules/client-only | 0.0.1 | False | MIT |
| node_modules/csstype | 3.2.3 | True | MIT |
| node_modules/detect-libc | 2.1.2 | False | Apache-2.0 |
| node_modules/lucide-react | 0.468.0 | False | ISC |
| node_modules/nanoid | 3.3.20 | False | MIT |
| node_modules/next | 15.5.27 | False | MIT |
| node_modules/picocolors | 1.1.1 | False | ISC |
| node_modules/postcss | 8.4.31 | False | MIT |
| node_modules/react | 19.3.0 | False | MIT |
| node_modules/react-dom | 19.3.0 | False | MIT |
| node_modules/scheduler | 0.28.0 | False | MIT |
| node_modules/semver | 7.8.5 | False | ISC |
| node_modules/sharp | 0.35.5 | False | Apache-2.0 |
| node_modules/source-map-js | 1.2.2 | False | BSD-3-Clause |
| node_modules/styled-jsx | 5.1.6 | False | MIT |
| node_modules/tslib | 2.8.1 | False | 0BSD |
| node_modules/typescript | 5.9.3 | True | Apache-2.0 |
| node_modules/undici-types | 6.21.0 | True | MIT |
