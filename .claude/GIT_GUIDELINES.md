# Git Commit Guidelines

## Commit Message Format

Always use **one-line commit messages** following [Conventional Commits](https://www.conventionalcommits.org/en/v1.0.0/) format:

```
<type>(<scope>): <description>
```

### Types
- `feat` - A new feature
- `fix` - A bug fix
- `docs` - Documentation changes
- `style` - Code style changes (formatting, missing semicolons, etc.)
- `refactor` - Code refactoring without feature changes
- `perf` - Performance improvements
- `test` - Adding or updating tests
- `chore` - Build, dependencies, tooling changes

### Examples
```
feat(validation): add Jakarta Bean Validation for envelope creation
fix(controller): handle null envelope in getById
docs: update API documentation
refactor(service): simplify envelope transfer logic
test(repository): add integration tests for findById
```

## Permission Protocol

**MANDATORY WORKFLOW - Must follow in exact order:**

You MUST follow these steps in order. Do not skip or reorder them.

### Step 1: Show Files
List all files that will be committed using `git status` and `git diff`

### Step 2: Show Commit Message (MANDATORY - DO NOT SKIP)
Display the proposed commit message using this EXACT template:

```
FILES TO COMMIT:
- <file path 1>
- <file path 2>

COMMIT MESSAGE:
<type>(<scope>): <description>

<optional longer description if needed>

🤖 Generated with [Claude Code](https://claude.com/claude-code)

Co-Authored-By: Claude <noreply@anthropic.com>
```

### Step 3: Ask for Approval
Only after showing both files AND the full message, ask:
"May I proceed with this commit?"

### Step 4: Wait for Approval
Wait for explicit user approval ("yes", "proceed", "ok", etc.)

### Step 5: Execute Commit
Only then run: `git add <files> && git commit -m "..."`

---

## ⚠️ CRITICAL VIOLATIONS (DO NOT DO THESE)

- ❌ DO NOT commit without showing the message
- ❌ DO NOT ask for approval before showing the message
- ❌ DO NOT assume implied approval
- ❌ DO NOT skip Step 2 - showing the full commit message is MANDATORY
- ❌ DO NOT say "May I commit?" without first displaying the message in the template above

**NOTE**: Claude has violated this protocol twice. This guideline exists to prevent that. If you see the assistant asking for approval without showing the full message in the template, reject it and demand the proper workflow.

## File Exclusions

Do not commit:
- Local configuration files (e.g., `.local.json`, `.local.yml`)
- IDE-specific settings
- Generated build artifacts
- Temporary files

Use `.gitignore` for patterns that should always be excluded.

## Pre-Commit Checklist

Before committing, verify:
- ✅ Tests pass (run relevant test suite)
- ✅ No unintended files staged
- ✅ Commit message is clear and follows format
- ✅ Documentation updated if necessary (`.claude/CLAUDE.md`, README, etc.)
- ✅ Code follows project conventions

## Examples

### Good Commits
```
feat(auth): add JWT token refresh mechanism
fix(database): resolve connection pool leak in EnvelopeRepository
docs(readme): update installation instructions
refactor(validation): extract validation logic to separate service
test(e2e): add comprehensive envelope CRUD tests
```

### Bad Commits
```
fixed stuff
working on validation
update
WIP: random changes
```
