# Automatic production deployment

Flashify production updates have two gates:

1. Each repository runs its tests and production build on every pull request and
   every push to `main`.
2. A successful `main` run advances that repository's `production` branch to the
   exact tested commit. The MacBook deployer watches both `production` branches
   and updates only when at least one tested commit changes.

The deployer does not recreate or modify the PostgreSQL service or its named
volume. Production secrets remain only in `/Users/daniel/flashify/docker-compose.yml`.

## One-time MacBook installation

After the automation is merged into both repositories and the first CI runs
pass:

```bash
cd /Users/daniel
git clone https://github.com/Flaniel44/flashify-app.git flashify-app
git clone https://github.com/Flaniel44/flashify-frontend.git flashify-frontend
chmod +x flashify-app/scripts/*.sh
flashify-app/scripts/install-macos-auto-deploy.sh
```

If either checkout already exists, use `git pull --ff-only origin main` instead
of cloning it.

Docker Desktop must be running. Java 21, Node.js, npm, and `rsync` must be
available to the logged-in `daniel` user.

## Verify

```bash
launchctl print gui/$(id -u)/place.whatisthis.flashify-deploy
tail -f ~/Library/Logs/flashify-deploy.log
docker compose -f /Users/daniel/flashify/docker-compose.yml ps
curl --fail http://127.0.0.1/
```

## Operation

The LaunchAgent checks every 15 minutes. A normal check only fetches the two
tested `production` branches. When either changes, it:

- refuses to overwrite tracked local source changes;
- requires fast-forward-only source updates;
- packages the backend and builds the frontend in their source checkouts;
- stages artifacts before replacing the live JAR and React build;
- rebuilds only the Spring Boot image and refreshes nginx;
- leaves PostgreSQL and `postgres_data` untouched;
- records both deployed commits only after the frontend and backend respond over
  HTTP.

Trigger an immediate check:

```bash
launchctl kickstart -k gui/$(id -u)/place.whatisthis.flashify-deploy
```

Disable automatic deployment:

```bash
launchctl bootout gui/$(id -u) \
  ~/Library/LaunchAgents/place.whatisthis.flashify-deploy.plist
```
