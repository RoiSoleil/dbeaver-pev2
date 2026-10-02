# DBeaver PEV2 : the PostgreSQL explain plans of DBeaver in PEV2

[![GitHub Workflow Status](https://img.shields.io/github/actions/workflow/status/RoiSoleil/dbeaver-pev2/build.yml)](https://github.com/RoiSoleil/dbeaver-pev2/actions/workflows/build.yml)
[![codecov](https://codecov.io/gh/RoiSoleil/dbeaver-pev2/branch/main/graph/badge.svg)](https://codecov.io/gh/RoiSoleil/dbeaver-pev2)
[![GitHub](https://img.shields.io/github/license/RoiSoleil/dbeaver-pev2)](LICENSE)

A DBeaver plug-in which runs `EXPLAIN` on the active query of the SQL editor and opens the plan in
[PEV2](https://github.com/dalibo/pev2), the PostgreSQL explain visualizer of Dalibo. PEV2 is
embedded in the plug-in: the plan never leaves your machine.

*SQL editor (PostgreSQL connection) > side toolbar > PEV2 button*

![dbeaver-pev2](https://github.com/RoiSoleil/dbeaver-pev2/raw/update-site/pev2.png)

# Update Site

You can find the latest build of dbeaver-pev2 here:

https://github.com/RoiSoleil/dbeaver-pev2/raw/update-site/latest/

# Features

- PEV2 button in the side toolbar of the SQL editor, shown only for PostgreSQL connections.
- A click on the button runs the active query with
  `EXPLAIN (ANALYZE, COSTS, VERBOSE, BUFFERS, FORMAT JSON)` and opens the plan in a PEV2 editor.
- The drop down of the button gives the same explain without `ANALYZE`
  (`EXPLAIN (COSTS, VERBOSE, FORMAT JSON)`): the query is planned but not executed, which suits slow
  queries and `INSERT` / `UPDATE` / `DELETE` statements.
- Query parameters are asked as for a normal execution in DBeaver.
- *Save As...* in the PEV2 editor writes the query and its plan to a `.pev2` file, which reopens in
  the same editor.
- The embedded PEV2 follows the upstream releases: a daily GitHub Actions workflow opens a pull
  request when a new version is published.

# Build

Requires JDK 21 and Maven 3.9.11 (Maven 3.9.12 has a regression with Tycho).

```bash
mvn clean install   # update site in update-site/org.eclipse.dbeaver-pev2/target/repository
```

Every push to `main` is built by GitHub Actions, which publishes the update site to the
`update-site` branch and a zipped copy to the `latest` release.

The tests are SWTBot tests: they start a full DBeaver workbench, so they open windows on the
current display, and the integration test connects to a PostgreSQL database initialized with
`bundles/org.eclipse.dbeaver-pev2.tests/schema.sql`:

```bash
mvn clean verify -Pcoverage -Duser.language=en -Duser.country=US \
  -Ddb.host=localhost -Ddb.port=5432 -Ddb.database=dbeaver_test -Ddb.user=test -Ddb.password=test
```

The locale must be English because the tests look for the labels of the DBeaver wizards. GitHub
Actions runs them against PostgreSQL 18 and sends the coverage to Codecov.

# Platform notes

- The plug-in is built against the latest Eclipse release and the latest DBeaver update site.
- With `ANALYZE` the query is really executed: use the drop down entry without `ANALYZE` for a
  statement which modifies data, or run it in a transaction you roll back.
- Without `ANALYZE` PEV2 shows the estimated costs and rows only, there are no timings or buffers.

# License

[MIT](LICENSE). The bundled [PEV2](https://github.com/dalibo/pev2) is distributed under the
PostgreSQL license.
