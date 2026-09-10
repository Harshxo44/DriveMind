# Upgrade Plan: guardian-backend (20260803183043)

- **Generated**: 2026-08-04T00:01:05.547+05:30
- **HEAD Branch**: N/A
- **HEAD Commit ID**: N/A

## Available Tools

**JDKs**
- JDK 21.0.11: C:\Users\harsh\AppData\Roaming\Code\User\globalStorage\pleiades.java-extension-pack-jdk\java\21\bin (current project JDK, used by baseline step)
- JDK 25.0.3: C:\Program Files\Java\jdk-25.0.3\bin (target upgrade JDK)

**Build Tools**
- Maven: **<TO_BE_INSTALLED>** (required by step 1; no Maven installation detected)

## Guidelines

- Changes are not version-controlled during this upgrade because no VCS was detected in the workspace.

> Note: You can add any specific guidelines or constraints for the upgrade process here if needed, bullet points are preferred.

## Options

- Working branch: appmod/java-upgrade-20260803183043
- Run tests before and after the upgrade: true

## Upgrade Goals

- Java runtime: 25 (latest LTS)

## Technology Stack

| Technology/Dependency | Current | Min Compatible Version | Why Incompatible |
| --- | --- | --- | --- |
| Java | 21 | 25 | User requested latest LTS runtime |
| Spring Boot | 3.5.16 | 3.5.x | Compatible with Java 25 target path |
| Maven | Not installed | 3.9.0 | Build and test commands cannot run without Maven |
| spring-boot-maven-plugin | 3.5.16 (managed) | 3.5.x | Already aligned with Spring Boot parent |
| JUnit Jupiter | 5.x (managed) | 5.x | Compatible, no blocking changes needed |

## Derived Upgrades

- Java 21 -> Java 25 by updating `java.version` to satisfy user upgrade goal.
- Maven installation is required to execute baseline, compile, and test verification commands.
- No framework-major migration is required because Spring Boot remains on the existing compatible line.

## Impact Analysis

### Dependency Changes

| File | Dependency | Current | Action | Target | Reason |
| --- | --- | --- | --- | --- | --- |
| pom.xml | java.version | 21 | upgrade | 25 | User requested latest LTS runtime |

### Source Code Changes

No source code changes required based on compatibility scan.

### Configuration Changes

No application configuration changes required.

### CI/CD Changes

No CI/CD files detected with hardcoded Java versions.

### Risks & Warnings

- **Maven not installed**: Build/test verification is blocked until Maven is installed. **Mitigation**: Install Maven in Step 1.
- **Java 25 ecosystem variance**: Some plugins may surface compatibility issues at compile/test time. **Mitigation**: run compile+tests after upgrade and fix immediately if failures occur.

## Upgrade Steps

- Step 1: Setup Environment
  - **Rationale**: Ensure required toolchain is available before verification.
  - **Changes to Make**: Install Maven 3.9.15; verify target JDK 25 path is available.
  - **Verification**: `mvn -v` and JDK listing; Expected Result: Maven executable available and JDK 25 present.

- Step 2: Setup Baseline
  - **Rationale**: Capture pre-upgrade compile/test baseline on current JDK.
  - **Changes to Make**: No file changes; run baseline verification commands using JDK 21.
  - **Verification**: `mvn clean compile test-compile -q && mvn clean test -q`; Expected Result: Baseline compile and tests documented.

- Step 3: Upgrade Java Runtime Property to 25
  - **Rationale**: Apply the user-requested runtime target with minimal impact.
  - **Changes to Make**: Apply Dependency Changes for `pom.xml` (`java.version` 21 -> 25).
  - **Verification**: `mvn clean test-compile -q` with JDK 25; Expected Result: Main and test code compile successfully.

- Step 4: CVE Validation & Fix
  - **Rationale**: Validate dependency security posture after runtime upgrade.
  - **Changes to Make**: Scan direct dependencies for CVEs and upgrade patched versions if required.
  - **Verification**: CVE scan + `mvn clean test-compile -q`; Expected Result: All fixable CVEs remediated and project compiles.

- Step 5: Final Validation
  - **Rationale**: Ensure upgrade success criteria are fully met.
  - **Changes to Make**: Resolve any remaining issues from prior steps.
  - **Verification**: `mvn clean test-compile -q` and `mvn clean test -q` with JDK 25; Expected Result: 100% test pass rate and target version met.
