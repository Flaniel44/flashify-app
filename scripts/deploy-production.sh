#!/bin/bash

set -euo pipefail

PRODUCTION_DIR="${FLASHIFY_PRODUCTION_DIR:-/Users/daniel/flashify}"
BACKEND_REPO_DIR="${FLASHIFY_BACKEND_REPO_DIR:-/Users/daniel/flashify-app}"
FRONTEND_REPO_DIR="${FLASHIFY_FRONTEND_REPO_DIR:-/Users/daniel/flashify-frontend}"
STATE_DIR="${PRODUCTION_DIR}/.deploy"
STATE_FILE="${STATE_DIR}/deployed-commits"
LOCK_DIR="${TMPDIR:-/tmp}/flashify-deploy.lock"
FRONTEND_HEALTH_URL="${FLASHIFY_FRONTEND_HEALTH_URL:-http://127.0.0.1/}"
BACKEND_HEALTH_URL="${FLASHIFY_BACKEND_HEALTH_URL:-http://127.0.0.1:8080/}"

timestamp() {
  date '+%Y-%m-%d %H:%M:%S'
}

log() {
  printf '[%s] %s\n' "$(timestamp)" "$*"
}

cleanup() {
  rm -f "${LOCK_DIR}/pid"
  rmdir "${LOCK_DIR}" 2>/dev/null || true
  if [[ -n "${STAGING_DIR:-}" && -d "${STAGING_DIR}" ]]; then
    rm -rf "${STAGING_DIR}"
  fi
}

if ! mkdir "${LOCK_DIR}" 2>/dev/null; then
  lock_pid=""
  [[ -f "${LOCK_DIR}/pid" ]] && lock_pid="$(cat "${LOCK_DIR}/pid")"
  if [[ -n "${lock_pid}" ]] && kill -0 "${lock_pid}" 2>/dev/null; then
    log "Another deployment is already running; exiting."
    exit 0
  fi
  log "Removing a stale deployment lock."
  rm -f "${LOCK_DIR}/pid"
  rmdir "${LOCK_DIR}"
  mkdir "${LOCK_DIR}"
fi
printf '%s\n' "$$" > "${LOCK_DIR}/pid"
trap cleanup EXIT

for repo_dir in "${BACKEND_REPO_DIR}" "${FRONTEND_REPO_DIR}"; do
  if [[ ! -d "${repo_dir}/.git" ]]; then
    log "Missing Git checkout: ${repo_dir}"
    exit 1
  fi
  if [[ -n "$(git -C "${repo_dir}" status --porcelain --untracked-files=no)" ]]; then
    log "Tracked files have local changes in ${repo_dir}; refusing to overwrite them."
    exit 1
  fi
done

if [[ ! -f "${PRODUCTION_DIR}/docker-compose.yml" ]]; then
  log "Missing production Compose file: ${PRODUCTION_DIR}/docker-compose.yml"
  exit 1
fi

log "Checking for tested backend and frontend commits..."
git -C "${BACKEND_REPO_DIR}" fetch --quiet origin \
  refs/heads/production:refs/remotes/origin/production
git -C "${FRONTEND_REPO_DIR}" fetch --quiet origin \
  refs/heads/production:refs/remotes/origin/production

backend_target="$(git -C "${BACKEND_REPO_DIR}" rev-parse origin/production)"
frontend_target="$(git -C "${FRONTEND_REPO_DIR}" rev-parse origin/production)"
deployed_backend=""
deployed_frontend=""
if [[ -f "${STATE_FILE}" ]]; then
  deployed_backend="$(awk -F= '$1 == "backend" { print $2 }' "${STATE_FILE}")"
  deployed_frontend="$(awk -F= '$1 == "frontend" { print $2 }' "${STATE_FILE}")"
fi

if [[ "${backend_target}" == "${deployed_backend}" && "${frontend_target}" == "${deployed_frontend}" ]]; then
  log "Production already runs the latest tested commits."
  exit 0
fi

for repo_dir in "${BACKEND_REPO_DIR}" "${FRONTEND_REPO_DIR}"; do
  current_sha="$(git -C "${repo_dir}" rev-parse HEAD)"
  target_sha="$(git -C "${repo_dir}" rev-parse origin/production)"
  if ! git -C "${repo_dir}" merge-base --is-ancestor "${current_sha}" "${target_sha}"; then
    log "${repo_dir} cannot fast-forward from ${current_sha} to ${target_sha}."
    exit 1
  fi
  git -C "${repo_dir}" merge --ff-only "${target_sha}"
done

STAGING_DIR="$(mktemp -d "${TMPDIR:-/tmp}/flashify-release.XXXXXX")"
mkdir -p "${STAGING_DIR}/react-build"

log "Packaging backend ${backend_target}..."
docker run --rm \
  --volume flashify_maven_cache:/root/.m2 \
  --volume "${BACKEND_REPO_DIR}:/workspace" \
  --workdir /workspace \
  maven:3.9-eclipse-temurin-21 \
  mvn --batch-mode -DskipTests clean package
backend_jar="$(find "${BACKEND_REPO_DIR}/target" -maxdepth 1 -type f -name '*.jar' ! -name '*.original' | head -n 1)"
if [[ -z "${backend_jar}" ]]; then
  log "Backend build produced no deployable JAR."
  exit 1
fi
cp "${backend_jar}" "${STAGING_DIR}/flashify-app-0.0.1-SNAPSHOT.jar"

log "Building frontend ${frontend_target}..."
docker run --rm \
  --volume flashify_npm_cache:/root/.npm \
  --volume "${FRONTEND_REPO_DIR}:/workspace" \
  --workdir /workspace \
  node:22-bookworm-slim \
  sh -c 'npm ci && npm run build'
rsync -a --delete "${FRONTEND_REPO_DIR}/build/" "${STAGING_DIR}/react-build/"

log "Installing staged artifacts..."
cp "${STAGING_DIR}/flashify-app-0.0.1-SNAPSHOT.jar" \
  "${PRODUCTION_DIR}/flashify-app-0.0.1-SNAPSHOT.jar.new"
mv "${PRODUCTION_DIR}/flashify-app-0.0.1-SNAPSHOT.jar.new" \
  "${PRODUCTION_DIR}/flashify-app-0.0.1-SNAPSHOT.jar"
mkdir -p "${PRODUCTION_DIR}/react/build"
rsync -a --delete "${STAGING_DIR}/react-build/" "${PRODUCTION_DIR}/react/build/"

log "Rebuilding Spring Boot and refreshing nginx..."
(
  cd "${PRODUCTION_DIR}"
  docker compose build springboot
  docker compose up -d --no-deps springboot
  docker compose up -d --no-deps nginx
)

log "Waiting for frontend and backend HTTP responses..."
healthy=false
for _ in {1..30}; do
  frontend_code="$(curl --silent --output /dev/null --write-out '%{http_code}' \
    --max-time 5 "${FRONTEND_HEALTH_URL}" || true)"
  backend_code="$(curl --silent --output /dev/null --write-out '%{http_code}' \
    --max-time 5 "${BACKEND_HEALTH_URL}" || true)"
  if [[ "${frontend_code}" =~ ^[234][0-9][0-9]$ && "${backend_code}" =~ ^[234][0-9][0-9]$ ]]; then
    healthy=true
    break
  fi
  sleep 5
done
if [[ "${healthy}" != "true" ]]; then
  log "Deployment failed: frontend=${frontend_code:-000}, backend=${backend_code:-000}."
  (cd "${PRODUCTION_DIR}" && docker compose ps)
  exit 1
fi

mkdir -p "${STATE_DIR}"
printf 'backend=%s\nfrontend=%s\n' "${backend_target}" "${frontend_target}" > "${STATE_FILE}"
log "Deployment complete: backend=${backend_target}, frontend=${frontend_target}."
