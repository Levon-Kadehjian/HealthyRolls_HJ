# Development Setup for HealthyRolls

## Java Version
We are using Java 25.
```sh
java --version  # needs to be 25
```

If it isn't 25, either you have it installed and haven't configured your path, or you need to install it.

## VSCode Plugins

```sh
# windows / linux / mac
code --install-extension vscjava.vscode-java-pack
code --install-extension shengchen.vscode-checkstyle

```
Refresh the window by going `Cmd+Shift+P > Developer: Reload Window`.

Then to run linting on the `server` :
```sh
cd server

# mac / linux
chmod +x mvnw   # mark mvnw as executable

# windows powershell / mac / linux
./mvnw spotless:apply
./mvnw spotless:check
```
