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

**Always ask for permission before committing:**

1. Show the user what files will be committed
2. Display the proposed commit message
3. Wait for explicit approval ("yes", "proceed", etc.)
4. Only commit if approved

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
