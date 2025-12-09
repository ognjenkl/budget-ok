# CI/CD Pipeline Documentation

## Overview

Budget OK uses a **multi-stage CI/CD pipeline** with GitHub Actions workflows that implements an ATDD (Acceptance Test-Driven Development) approach. The pipeline consists of five workflows that orchestrate building, testing, and deploying the application across multiple environments.

## Pipeline Architecture

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           BUDGET OK CI/CD PIPELINE                          │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  ┌──────────────────────┐                                                  │
│  │   COMMIT STAGE       │  (External - Frontend & Backend Built Separately)│
│  │                      │                                                  │
│  │ • Frontend builds in │                                                  │
│  │   budget-ok-web repo │                                                  │
│  │ • Backend builds in  │                                                  │
│  │   this repo          │                                                  │
│  │ • Images pushed to   │                                                  │
│  │   GitHub Container   │                                                  │
│  │   Registry (ghcr.io) │                                                  │
│  └──────────┬───────────┘                                                  │
│             │ Docker images tagged :latest published                       │
│             ▼                                                              │
│  ┌──────────────────────────────────────────────────────┐                 │
│  │        ACCEPTANCE STAGE                              │                 │
│  │  (.github/workflows/acceptance-stage.yml)            │                 │
│  │                                                      │                 │
│  │ ⏰ Runs every 30 min (or manual trigger)             │                 │
│  │                                                      │                 │
│  │ 1. Find Latest Images                               │                 │
│  │    └─ Query ghcr.io for latest digests              │                 │
│  │                                                      │                 │
│  │ 2. Check if Should Run                              │                 │
│  │    └─ Compare image timestamps with last run        │                 │
│  │                                                      │                 │
│  │ 3. Deploy System & Run Tests (if new images)        │                 │
│  │    ├─ Start Docker Compose stack                    │                 │
│  │    ├─ Wait for services (Bank OK, Backend, Frontend)│                 │
│  │    ├─ Run Smoke Tests                               │                 │
│  │    └─ Run E2E Tests                                 │                 │
│  │                                                      │                 │
│  │ 4. Create Prerelease                                │                 │
│  │    └─ Generate version (e.g., v1.0.4-rc)            │                 │
│  │    └─ Tag images with prerelease version            │                 │
│  │    └─ Create GitHub Release                         │                 │
│  │                                                      │                 │
│  │ 5. Summary Report                                   │                 │
│  │    └─ Summarize stage results                       │                 │
│  └──────────┬─────────────────────────────────────────┘                 │
│             │ Prerelease created (v1.0.4-rc)                             │
│             │ Images tagged: ghcr.io/.../backend:v1.0.4-rc               │
│             ▼                                                            │
│  ┌──────────────────────────────────────────────────────┐               │
│  │        QA STAGE (Manual Dispatch)                    │               │
│  │  (.github/workflows/qa-stage.yml)                    │               │
│  │                                                      │               │
│  │ Input: Prerelease version (e.g., v1.0.4-rc)         │               │
│  │                                                      │               │
│  │ 1. Check Release Exists                             │               │
│  │    └─ Verify prerelease exists in GitHub Releases   │               │
│  │                                                      │               │
│  │ 2. Resolve Docker Images                            │               │
│  │    └─ Pull image digests for specified version      │               │
│  │                                                      │               │
│  │ 3. Deploy System (QA Environment)                   │               │
│  │    ├─ Start Docker Compose with QA images           │               │
│  │    ├─ Wait for services                             │               │
│  │    └─ Run Smoke Tests                               │               │
│  │                                                      │               │
│  │ 4. Create Deployment Release                        │               │
│  │    └─ Generate deployment status version             │               │
│  │    └─ Create GitHub Release (QA deployed)           │               │
│  │                                                      │               │
│  │ 5. Summary Report                                   │               │
│  │    └─ Summarize deployment status                   │               │
│  └──────────┬─────────────────────────────────────────┘               │
│             │ QA environment deployed                                  │
│             │ Ready for manual QA testing                             │
│             ▼                                                         │
│  ┌──────────────────────────────────────────────────────┐            │
│  │    QA SIGNOFF (Manual Dispatch)                      │            │
│  │  (.github/workflows/qa-signoff.yml)                  │            │
│  │                                                      │            │
│  │ Input: Version (v1.0.4-rc) + Result (success/fail)  │            │
│  │                                                      │            │
│  │ 1. Check Version Exists                             │            │
│  │    └─ Verify release exists                         │            │
│  │                                                      │            │
│  │ 2. Generate Signoff Status                          │            │
│  │    └─ Create version with signoff status            │            │
│  │                                                      │            │
│  │ 3. Create Signoff Release                           │            │
│  │    └─ GitHub Release with QA signoff result         │            │
│  │                                                      │            │
│  │ 4. Summary Report                                   │            │
│  │    └─ Report signoff outcome                        │            │
│  │                                                      │            │
│  │ ⚠️  If QA Signoff = SUCCESS → Continue to Prod      │            │
│  │ ❌ If QA Signoff = FAILURE → Stop (back to dev)     │            │
│  └──────────┬─────────────────────────────────────────┘            │
│             │ QA Signoff Successful                                  │
│             │ Ready for production deployment                       │
│             ▼                                                       │
│  ┌──────────────────────────────────────────────────────┐          │
│  │    PRODUCTION STAGE (Manual Dispatch)                │          │
│  │  (.github/workflows/prod-stage.yml)                  │          │
│  │                                                      │          │
│  │ Input: Prerelease version (v1.0.4-rc)               │          │
│  │                                                      │          │
│  │ 1. Generate Production Version                      │          │
│  │    └─ Convert v1.0.4-rc → v1.0.4 (final release)   │          │
│  │                                                      │          │
│  │ 2. Check Prerelease Exists                          │          │
│  │    └─ Verify input version exists                   │          │
│  │                                                      │          │
│  │ 3. Resolve Docker Images                            │          │
│  │    └─ Pull image digests for prerelease version     │          │
│  │                                                      │          │
│  │ 4. Tag Docker Images for Production                 │          │
│  │    └─ Re-tag images: v1.0.4-rc → v1.0.4            │          │
│  │    └─ Push to GitHub Container Registry             │          │
│  │                                                      │          │
│  │ 5. Deploy to Production                             │          │
│  │    ├─ Start Docker Compose with prod images         │          │
│  │    ├─ Wait for services                             │          │
│  │    └─ Run Smoke Tests                               │          │
│  │                                                      │          │
│  │ 6. Create Production Release                        │          │
│  │    └─ GitHub Release v1.0.4 (final)                │          │
│  │                                                      │          │
│  │ 7. Summary Report                                   │          │
│  │    └─ Confirm production deployment                │          │
│  └──────────┬──────────────────────────────────────────┘          │
│             │ Production deployment complete                      │
│             │ v1.0.4 live in production                           │
│             ▼                                                    │
│  ┌──────────────────────┐                                       │
│  │   PRODUCTION LIVE    │                                       │
│  │   (End of Pipeline)  │                                       │
│  └──────────────────────┘                                       │
│                                                                  │
└──────────────────────────────────────────────────────────────────┘
```

## Workflows Detailed Breakdown

### 1. Acceptance Stage (`.github/workflows/acceptance-stage.yml`)

**Purpose**: Automatically test new Docker images after commit stage builds them.

**Trigger**:
- Every 30 minutes (via cron schedule)
- Manual dispatch via GitHub UI

**Key Jobs**:

| Job | Purpose | Notes |
|-----|---------|-------|
| `find-latest-images` | Query GitHub Container Registry for latest image digests | Uses `optivem/find-latest-docker-images-action@v1` |
| `should-run` | Check if images are newer than last acceptance run | Prevents redundant test runs |
| `system-test` | Deploy stack and run E2E + External API tests | Uses custom `deploy-docker-images` action |
| `prerelease` | Create prerelease version if tests pass | Tags images with prerelease version |
| `summary` | Generate pipeline summary report | Always runs (success/failure) |

**Images Checked**:
- `ghcr.io/{owner}/budget-ok-web/frontend:latest` (Frontend React app)
- `ghcr.io/{owner}/budget-ok-backend/backend:latest` (Backend Spring Boot)
- `ghcr.io/{owner}/bank-ok-repo/bank-ok:latest` (Bank OK mock service)

**Test Types**:
- **Smoke Tests**: Basic connectivity checks (HTTP 200, correct response types)
- **E2E Tests**: Full user workflows (envelope CRUD, transfers, expenses)
- **External API Tests**: DummyJSON API availability

**Outputs**:
- Prerelease version: `v1.0.4-rc` (semantic versioning with `-rc` suffix)
- Tagged Docker images: `ghcr.io/{owner}/{repo}/backend:v1.0.4-rc`
- GitHub Release (prerelease marked)

---

### 2. QA Stage (`.github/workflows/qa-stage.yml`)

**Purpose**: Deploy prerelease to QA environment for manual testing.

**Trigger**: Manual dispatch via GitHub UI

**Required Input**:
```
version: v1.0.4-rc  (example: prerelease from acceptance stage)
```

**Key Jobs**:

| Job | Purpose | Notes |
|-----|---------|-------|
| `check-release-exists` | Verify prerelease exists in GitHub Releases | Prevents deploying non-existent versions |
| `resolve-docker-images` | Get digest URLs for specified prerelease version | Ensures reproducible deployments |
| `deployment` | Deploy stack to QA and run smoke tests | Uses custom `deploy-docker-images` action |
| `status-version` | Generate deployment status version | Tracks QA deployment state |
| `deployment-release` | Create GitHub Release with deployment info | Marks version as deployed to QA |
| `summary` | Generate deployment summary | Always runs |

**Environment**: QA (typically staging/test environment)

**Outputs**:
- QA stack running with specified prerelease version
- GitHub Release marking version as "deployed to QA"
- Ready for manual QA team testing

---

### 3. QA Signoff (`.github/workflows/qa-signoff.yml`)

**Purpose**: Record QA testing result and determine if production deployment is approved.

**Trigger**: Manual dispatch via GitHub UI

**Required Inputs**:
```
version: v1.0.4-rc         (version tested in QA)
result: success | failure  (QA testing outcome)
```

**Key Jobs**:

| Job | Purpose | Notes |
|-----|---------|-------|
| `check-version-exists` | Verify version exists | Safety check |
| `signoff-status-version` | Generate version with signoff status | Includes success/failure in version metadata |
| `signoff-release` | Create GitHub Release with signoff | Documents QA approval |
| `summary` | Generate signoff summary | Reports workflow and QA result |

**Decision Point**:
- ✅ **Success**: Workflow passes → Proceed to **Production Stage**
- ❌ **Failure**: Workflow or QA tests fail → **Stop** (return to development)

**Outputs**:
- GitHub Release with QA signoff status
- Go/no-go decision for production

---

### 4. Production Stage (`.github/workflows/prod-stage.yml`)

**Purpose**: Final deployment to production after QA signoff approval.

**Trigger**: Manual dispatch via GitHub UI

**Required Input**:
```
version: v1.0.4-rc  (prerelease to promote to production)
```

**Key Jobs**:

| Job | Purpose | Notes |
|-----|---------|-------|
| `generate-production-version` | Convert prerelease → production version | `v1.0.4-rc` → `v1.0.4` |
| `check-release-exists` | Verify prerelease exists | Can't promote non-existent version |
| `resolve-docker-images` | Get digests for prerelease images | Ensures correct images promoted |
| `tag-docker-images` | Re-tag images with production version | `backend:v1.0.4-rc` → `backend:v1.0.4` |
| `deploy-docker-images` | Deploy to production with final images | Uses custom `deploy-docker-images` action |
| `production-release` | Create final GitHub Release | Marks `v1.0.4` as production release |
| `summary` | Generate production deployment summary | Always runs |

**Promotion Strategy**:
1. Start with prerelease version: `v1.0.4-rc`
2. Generate final version: `v1.0.4`
3. Re-tag images from `:v1.0.4-rc` → `:v1.0.4`
4. Deploy tagged images to production
5. Create non-prerelease GitHub Release

**Outputs**:
- Production version: `v1.0.4` (without `-rc` suffix)
- Tagged production images: `ghcr.io/{owner}/{repo}/backend:v1.0.4`
- Production GitHub Release (non-prerelease)
- Live application serving production traffic

---

## Custom Action: Deploy Docker Images

**Location**: `.github/actions/deploy-docker-images/action.yml`

**Purpose**: Reusable composite action for deploying application stack and running smoke tests.

**Inputs**:
```yaml
environment:  acceptance | qa | production
version:      v1.0.4-rc (release version)
image-urls:   JSON array of Docker image URLs
```

**Deployment Process**:

1. **Parse Image URLs**: Extract digest URLs from JSON array
2. **Start Docker Compose**: `docker compose up -d` in `system-test` directory
   - Starts 3 services: Frontend, Backend, Bank OK
   - Uses fixed ports: 5173 (frontend), 8080 (backend), 8081 (Bank OK)

3. **Health Checks** (Wait for services):
   - Bank OK: `GET http://localhost:8081/api/expenses` (max 30 attempts, 10s each)
   - Backend: `GET http://localhost:8080/` (max 30 attempts, 10s each)
   - Frontend: `GET http://localhost:5173/` (max 30 attempts, 10s each)

4. **Run Smoke Tests**:
   ```
   ./mvnw test -Dtest="com.ognjen.template.systemtest.smoketests.**"
   ```
   - API endpoint connectivity tests
   - Frontend HTML/HTTP validation
   - External API availability checks

**Note**: This is simulated deployment for testing. Production would use actual cloud deployment targets (AWS ECS/EKS, Azure AKS, GCP Cloud Run, etc.)

---

## Version Scheme

Budget OK uses **semantic versioning** with prerelease/production phases:

```
Acceptance Stage      QA Stage           Production Stage
      ↓                  ↓                      ↓
v1.0.4-rc      →   v1.0.4-rc (deployed)  →  v1.0.4 (final)
(prerelease)       (QA testing)               (production)
```

**Version Format**:
- Prerelease: `v{major}.{minor}.{patch}-rc` (release candidate)
  - Example: `v1.0.4-rc`, `v2.1.0-rc`
- Production: `v{major}.{minor}.{patch}` (final release)
  - Example: `v1.0.4`, `v2.1.0`

**Version Generation**:
- **Acceptance**: Auto-generated from git tags and current state
- **QA**: Uses version input from acceptance stage
- **Production**: Strips `-rc` suffix to create final version

---

## Docker Image Registry

All images stored in **GitHub Container Registry (GHCR)**:

```
ghcr.io/ognjenkl/budget-ok-web/frontend:v1.0.4-rc
ghcr.io/ognjenkl/budget-ok-backend/backend:v1.0.4-rc
ghcr.io/ognjenkl/bank-ok-repo/bank-ok:v1.0.4-rc
```

**Tags**:
- `:latest` — Most recent build from commit stage
- `:v1.0.4-rc` — Prerelease version from acceptance stage
- `:v1.0.4` — Production version from production stage

---

## Repository Structure

```
budget-ok (This Repository)
├── .github/
│   ├── workflows/
│   │   ├── acceptance-stage.yml      ← Automatic testing (30 min schedule)
│   │   ├── qa-stage.yml              ← Manual QA deployment
│   │   ├── qa-signoff.yml            ← Manual QA approval
│   │   └── prod-stage.yml            ← Manual production deployment
│   └── actions/
│       └── deploy-docker-images/
│           └── action.yml             ← Reusable deployment action
│
├── backend/                          ← Spring Boot backend
│   ├── src/main/java/.../
│   ├── pom.xml
│   └── target/*.jar                  ← Built JAR (mvn clean package)
│
├── system-test/                      ← E2E & smoke tests
│   ├── src/test/java/.../
│   ├── docker-compose.yml             ← Development stack
│   └── pom.xml
│
└── .claude/
    ├── CLAUDE.md                      ← Project overview
    └── cicd.md                        ← This file

budget-ok-web (Separate Repository)
├── src/components/...                ← React frontend
├── package.json
└── Dockerfile                        ← Frontend image build
```

---

## Common Workflows

### Scenario 1: Normal Release (Commit → Prod)

```
1. Developer commits to main
   ↓
2. Commit stage builds backend image → ghcr.io/.../backend:latest
   ↓
3. Acceptance stage (auto, every 30 min)
   - Finds latest images
   - Deploys to acceptance environment
   - Runs E2E + external API tests
   - Creates prerelease: v1.0.4-rc
   ↓
4. QA team manually dispatches QA stage
   - Input: v1.0.4-rc
   - Deploys to QA environment
   - Smoke tests pass
   ↓
5. QA team manually tests (manual testing)
   ↓
6. QA team manually dispatches QA signoff
   - Input: v1.0.4-rc, result: success
   ↓
7. Release manager manually dispatches Prod stage
   - Input: v1.0.4-rc
   - Generates v1.0.4
   - Deploys to production
   - Live!
```

### Scenario 2: QA Test Failure (Back to Dev)

```
1. Acceptance stage creates v1.0.4-rc ✓
   ↓
2. QA stage deploys to QA ✓
   ↓
3. QA team finds bug during testing
   ↓
4. QA team dispatches QA signoff
   - Input: v1.0.4-rc, result: failure
   ↓
5. Release stops → Dev team fixes bug
   ↓
6. Next commit triggers new acceptance stage
   ↓
7. Creates NEW prerelease: v1.0.5-rc
   ↓
8. Cycle repeats...
```

### Scenario 3: Force Acceptance Re-run

```
1. Development work complete
   ↓
2. Manually dispatch acceptance stage
   - Enable: force_run = true
   ↓
3. Re-runs tests even if images unchanged
   ↓
4. If successful, creates/updates prerelease
```

---

## GitHub Actions Permissions

**Required Secrets**:
- `GITHUB_TOKEN` — Automatically available, used for GitHub operations
- `DOCKER_REGISTRY_TOKEN` — Required for tagging/pushing Docker images (must be configured in repository settings)

**Workflow Permissions**:
- `contents: read` — Read repository files and releases
- `contents: write` — Create releases (acceptance, QA, prod stages)
- `packages: write` — Push Docker images to registry (prod stage tagging)

---

## Key Concepts

### Concurrency Groups
Each workflow has a concurrency group to prevent simultaneous runs:
```yaml
concurrency:
  group: acceptance-stage
  cancel-in-progress: true  # Cancel previous runs if new one starts
```

This prevents race conditions and overlapping deployments.

### Conditional Job Execution
Jobs run based on previous results:
```yaml
if: needs.system-test.result == 'success'  # Run only if tests pass
if: needs.find-latest-images.result == 'success'  # Run only if images found
```

### Job Dependencies & Outputs
Jobs pass data via outputs:
```yaml
outputs:
  image-digest-urls: ${{ steps.get-digest.outputs.image-digest-urls }}
```

Later jobs receive via `needs`:
```yaml
needs: [find-latest-images]
image-urls: ${{ needs.find-latest-images.outputs.image-digest-urls }}
```

### Summary Actions
Each workflow ends with a summary report using `optivem/summarize-system-stage-action@v1` for consistent output.

---

## Troubleshooting

### Acceptance Stage Not Running
- Check: Is it 30-minute interval? Manual dispatch available.
- Check: Did new images get built in commit stage?
- Solution: Manually dispatch acceptance-stage with `force_run: true`

### Tests Fail in Acceptance
- Check: Docker Compose services healthy? (See logs in `system-test/`)
- Check: Ports 5173, 8080, 8081 free?
- Check: E2E tests timeout? Increase wait attempts in action.yml

### QA Stage Fails with "Release not found"
- Check: Prerelease version from acceptance stage exists in GitHub Releases
- Solution: Re-run acceptance stage or manually trigger with valid version

### Production Deployment Stuck
- Check: QA signoff marked as `success`?
- Check: Prerelease version matches input version exactly
- Solution: Manually re-dispatch prod-stage with correct version

---

## Future Enhancements

Potential improvements:
- [ ] Auto-trigger QA stage after successful acceptance (remove manual step)
- [ ] Auto-trigger prod stage after QA signoff (remove manual step)
- [ ] Slack/email notifications at each stage
- [ ] Performance baseline tracking across versions
- [ ] Database migration validation in acceptance stage
- [ ] Security scanning (SAST/DAST) in acceptance stage
- [ ] Load testing before production deployment
