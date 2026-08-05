---
name: dependency-update
description: Update dependencies and plugins in Maven or Gradle projects — baseline build, version checking, risk assessment, inline pin comments, and PR description.
---

# Dependency Update Skill

## Before Starting

Ask the user for `JAVA_HOME` (for Maven projects) or any required SDK/toolchain path before running any build commands.

---

## Phase 1: Baseline Build

Run a clean build to confirm tests pass before making any changes.

**Maven:**
```bash
JAVA_HOME=<user-provided> mvn clean verify 2>&1 | tee build-baseline.log
```

**Gradle:**
```bash
JAVA_HOME=<user-provided> ./gradlew clean build 2>&1 | tee build-baseline.log
```

If the baseline fails, stop and report to the user before proceeding.

---

## Phase 2: Identify All Dependencies

Collect every versioned dependency and plugin declared across all build files.

**Maven:** Read all `pom.xml` files. Capture `groupId`, `artifactId`, and `version` for:
- `<dependencies>`
- `<build><plugins>`
- `<pluginManagement><plugins>`
- `<dependencyManagement>`

**Gradle:** Read all `build.gradle` / `build.gradle.kts` files. Capture coordinates from:
- `dependencies { }` blocks
- `plugins { }` blocks
- Version catalogs (`libs.versions.toml`)
- `gradle/wrapper/gradle-wrapper.properties` (Gradle wrapper version)

Record the source file and line for each entry so updates can be applied precisely.

---

## Phase 3: Check Latest Versions

Fetch the latest available version for **all dependencies in a single script** — do not make one request per dependency.

### Version sources

| Registry | Authoritative URL | Notes |
|----------|-------------------|-------|
| Maven Central | `https://repo1.maven.org/maven2/<group/as/path>/<artifact>/maven-metadata.xml` — read `<versions>` list | Primary source; always current |
| Gradle Plugin Portal | `https://plugins.gradle.org/m2/<plugin/id/as/path>/maven-metadata.xml` — read `<versions>` list | e.g. `org.jreleaser` → `org/jreleaser/jreleaser-gradle-plugin/maven-metadata.xml` |
| Gradle wrapper | `https://services.gradle.org/versions/current` — JSON, read `version` field | |

> **Never use `<release>` or `<latest>` tags.** Maven Central sets `<release>` to the lexicographically highest version string, which can be a pre-release (e.g. `4.0.0-beta-2` sorts above `3.5.0`). Always read the full `<versions>` list and filter for stable yourself.

### Script pattern (Python, no third-party deps)

Populate `checks` with every dependency from Phase 2, then run once:

```bash
python3 - << 'EOF'
import urllib.request, xml.etree.ElementTree as ET, json

PRE_RELEASE_MARKERS = ['alpha', 'beta', 'rc', 'milestone', 'cr',
                       '.m1','.m2','.m3','.m4','.m5','.m6','.m7','.m8','.m9',
                       '-m1','-m2','-m3','-m4','-m5','-m6','-m7','-m8','-m9']

def is_stable(version):
    v = version.lower()
    return not any(m in v for m in PRE_RELEASE_MARKERS)

def major(version):
    return version.split('.')[0]

def version_info_with_current(group, artifact, current):
    """Return (latest_in_current_major, latest_overall)."""
    path = group.replace('.', '/') + '/' + artifact
    url = f"https://repo1.maven.org/maven2/{path}/maven-metadata.xml"
    with urllib.request.urlopen(url, timeout=10) as r:
        tree = ET.parse(r)
    versions = [v.text for v in tree.findall('.//version')]
    stable = [v for v in versions if is_stable(v)]
    if not stable:
        return 'unknown', 'unknown'
    cur_major = major(current)
    same_major = [v for v in stable if major(v) == cur_major]
    latest_in_major = same_major[-1] if same_major else 'unknown'
    latest_overall  = stable[-1]
    return latest_in_major, latest_overall

def latest_stable_gradle_plugin(artifact_path):
    url = f"https://plugins.gradle.org/m2/{artifact_path}/maven-metadata.xml"
    with urllib.request.urlopen(url, timeout=10) as r:
        tree = ET.parse(r)
    versions = [v.text for v in tree.findall('.//version')]
    stable = [v for v in versions if is_stable(v)]
    return stable[-1] if stable else 'unknown'

def gradle_current():
    with urllib.request.urlopen("https://services.gradle.org/versions/current", timeout=10) as r:
        return json.load(r)['version']

# (display name, group, artifact, current_version)
checks = [
    ("com.example:my-lib",  "com.example",            "my-lib",            "1.2.3"),
]

print(f"{'Dependency':<50} {'Current':<12} {'Latest (same major)':<22} {'Latest overall'}")
print("-" * 100)
for name, group, artifact, current in checks:
    same_maj, overall = version_info_with_current(group, artifact, current)
    # Only flag when latest overall is a genuine upgrade: its major is higher AND
    # the current major has stable versions (same_maj != 'unknown'). If same_maj
    # is 'unknown' the current version is a pre-release series with no stable
    # releases — "latest overall" would be a downgrade, not an upgrade.
    new_major = overall != 'unknown' and same_maj != 'unknown' and major(overall) != major(current)
    flag = "  <-- MAJOR" if new_major else ""
    print(f"{name:<50} {current:<12} {same_maj:<22} {overall}{flag}")
EOF
```

### Watch out for version format quirks

- **Spock BOM** versions use `X.Y-groovy-Z.Z` — filter for the Groovy version the project actually uses after collecting the stable list.
- **POM-only aggregators** (e.g. `wiremock-jre8` 3.x): if `maven-metadata.xml` reports a version but the artifact only publishes a `.pom` with no `.jar`, it is a redirect artifact. Check the previous major line for the real latest JAR.
- **`search.maven.org`** Solr index can lag by days and miss recently published releases — always confirm with `repo1.maven.org`. If the current version in the project appears newer than what the search index reports, the index is stale.

### Classify each dependency

| Status | Meaning |
|--------|---------|
| `up-to-date` | Already on latest |
| `patch-available` | New patch release (x.y.Z) |
| `minor-available` | New minor release (x.Y.z) |
| `major-available` | New major release (X.y.z) |

---

## Phase 4: Risk Assessment

For each dependency not already up-to-date:

1. **Patch updates** — treat as low risk; apply directly.
2. **Minor/major updates** — fetch the release notes or changelog and scan for:
   - Breaking API changes
   - Removed or renamed classes/methods used in this project
   - Raised minimum Java/runtime version
   - License changes

Search GitHub releases (`https://api.github.com/repos/<owner>/<repo>/releases?per_page=20`), the project's `CHANGELOG.md`, or migration guides. Summarise findings as `low`, `medium`, or `high` risk with a one-line reason.

---

## Phase 5: Apply Updates

### Step 1 — Apply all patch updates at once

Apply every `patch-available` version bump in one pass across all build files, then run a single full build.

- If the build passes → all patches are done, record every one as **updated**.
- If the build fails → bisect: revert half the patches, rebuild, narrow down until the offending patch is isolated. Revert only that dependency and record it as **blocked** with the failure reason.

### Step 2 — Apply minor and major updates one at a time

Work through `minor-available` and `major-available` dependencies individually, lowest risk first (as assessed in Phase 4).

For each:
1. Edit the version in the build file.
2. Run the full build and test suite.
3. If tests pass → keep the change, record as **updated**.
4. If tests fail → triage the failure:
   - Read the error. Check whether it matches a known breaking change from the release notes.
   - If a straightforward fix exists (e.g. a renamed import, a changed method signature) → apply the fix, re-run, and record as **updated with migration**.
   - If the fix is non-trivial or out of scope → revert the version bump, record as **blocked** with the failure reason.

### Annotating pinned dependencies

Add an inline `pinned:` comment **only** for dependencies intentionally held below the latest available version. Do not annotate dependencies that are already on their latest version.

**Maven (`pom.xml`):**
```xml
<!-- pinned: <brief reason, e.g. "v5 requires Java 17, project targets Java 11"> -->
<version>4.11.0</version>
```

**Gradle (`build.gradle` / `build.gradle.kts`):**
```groovy
// pinned: <brief reason>
implementation 'org.example:lib:4.11.0'
```

---

## Phase 6: PR Description

Produce a PR description using the template below. Output it as a fenced ` ```markdown ` code block in the chat so the user can copy the raw markdown.

```markdown
## Dependencies updated

### Updated

| Dependency | Old | New | Notes |
|------------|-----|-----|-------|
| `com.example:lib` | `1.2.3` | `1.2.5` | patch |
| `plugin: org.foo` | `1.0.0` | `2.1.0` | major; migrated DSL (renamed property) |

### Not updated

| Dependency | Current | Latest | Reason |
|------------|---------|--------|--------|
| `org.example:other` | `2.3.0` | `3.0.0` | v3 requires Java 17; project targets Java 11 |

### Build verification

- Baseline: ✅ X tests passing
- After updates: ✅ X tests passing
```
