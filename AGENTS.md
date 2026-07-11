# DBeaver PEV2 Project - Agent Guide

## Build Prerequisites
- **Java 21**: `JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64` (Java 25 is installed but NOT compatible)
- **Maven 3.9.11**: Use `/tmp/apache-maven-3.9.11/bin/mvn` (3.9.12 is installed but has a Tycho regression: `No implementation for TargetPlatformArtifactResolver was bound`)
- **Tycho**: 5.0.3

## Build Commands
```bash
# Full build
JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 /tmp/apache-maven-3.9.11/bin/mvn clean install -Duser.language=en -Duser.country=US

# Run SWTBot tests only (with reactor deps)
JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 /tmp/apache-maven-3.9.11/bin/mvn test -f pom.xml -pl bundles/org.eclipse.dbeaver-pev2.tests -am -Duser.language=en -Duser.country=US

# With coverage
JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 /tmp/apache-maven-3.9.11/bin/mvn verify -Pcoverage -Duser.language=en -Duser.country=US
```

## Project Structure
- `bundles/org.eclipse.dbeaver-pev2` — main PEV2 editor plugin
- `bundles/org.eclipse.dbeaver-pev2.tests` — SWTBot integration tests
- `features/org.eclipse.dbeaver-pev2` — feature for update site
- `releng/` — target platform + releng pom
- `update-site/` — p2 repository

## Test Architecture
- **Packaging**: `eclipse-test-plugin` (Tycho launches full Eclipse workbench)
- **SWTBot**: `SWTWorkbenchBot` for UI interaction
- **Locale**: Must force English with `-Duser.language=en -Duser.country=US` (UI is French by default, wizard labels like "Database Connection", "Next >" need English)
- **Display**: X11 at `:0` is available (no Xvfb needed)
- **Connection wizard**: Opened programmatically via `IHandlerService.executeCommand("org.jkiss.dbeaver.core.new.connection")` (non-modal, SWTBot can interact)

## Key Files
- `releng/pom.xml` — locale argLine config, Tycho, target platform
- `releng/dbeaver-pev2.target` — Eclipse + DBeaver IDE feature + SWTBot repos
- `bundles/org.eclipse.dbeaver-pev2.tests/META-INF/MANIFEST.MF` — Require-Bundle includes `org.jkiss.dbeaver.ui.app.eclipse`
- `bundles/org.eclipse.dbeaver-pev2.tests/src/.../PostgreSQLConnectionHelper.java` — init DBeaver app + wizard via reflection + SWTBot
- `bundles/org.eclipse.dbeaver-pev2.tests/src/.../IntegrationTest.java` — test flow: connect → EXPLAIN → verify PEV2 editor

## Common Issues
1. **Maven 3.9.12** → `No implementation for TargetPlatformArtifactResolver` — use 3.9.11 instead
2. **Java 25** → Tycho errors — use Java 21
3. **`Missing requirement: org.eclipse.dbeaver-pev2`** → `-am` flag for reactor includes, or build main plugin first
4. **Wizard not found** → Locale must be English, or use command-based opening instead of SWTBot menus
5. **NoSuchElementException in ProjectsPanel** → Create Eclipse IProject with `org.jkiss.dbeaver.DBeaverNature` before DBeaver platform init