This plugin provides a lightweight HTTP API that exports the real-time status of all Velocity backend servers in JSON format.
Exported fields are fully configurable via config.yml, allowing safe public exposure.

The game version is for all versions supported by Velocity 3.4.0-SNAPSHOT as it is added to the proxy server only. The game version must be specified and is specified, but see the [PaperMC website](https://docs.papermc.io/velocity/server-compatibility/) for details.
# Installation
1. [Download](https://modrinth.com/plugin/velostat) the latest release of the plugin
2. Put velostat-x.x.jar in /plugins directory of velocity

This completes the process!
# Config reference
| Setting name               | Description                                                                        | Default |
|:---------------------------|:-----------------------------------------------------------------------------------|:--------|
| `api.port`                 | The port number for the internal HTTP server to listen on.                         | `8080`  |
| `ping.interval_seconds`    | The interval (in seconds) at which the plugin pings sub-servers to collect status. | `10`    |
| `export.server.name`       | Whether to include the server name (defined in Velocity) in the JSON output.       | `true`  |
| `export.server.address`    | Whether to include the server address in the JSON output.                          | `true`  |
| `export.server.status`     | Whether to include the online/offline status in the JSON output.                   | `true`  |
| `export.server.motd`       | Whether to include the server's MOTD (Message of the Day) in the JSON output.      | `true`  |
| `export.server.players`    | Whether to include player counts (online/max) in the JSON output.                  | `true`  |
| `export.server.version`    | Whether to include the game version name and protocol version in the JSON output.  | `true`  |
| `export.server.updated_at` | Whether to include the last update timestamp in the JSON output.                   | `true`  |
# Example Json output
Accessing `http://<SERVER_IP>:<PORT>/status>` will return data with a `Content-Type: application/json`.
### FULL data
```json
{
  "lobby": {
    "name": "lobby",
    "address": "127.0.0.1:25565",
    "online": true,
    "motd": "Welcome to the Lobby!",
    "players": {
      "online": 12,
      "max": 100
    },
    "version": {
      "name": "Paper 1.21.1",
      "protocol": 767
    },
    "updated_at": 1704681000000
  },
  "survival": {
    "name": "survival",
    "address": "127.0.0.1:25566",
    "online": false,
    "motd": null,
    "players": {
      "online": 0,
      "max": 0
    },
    "version": null,
    "updated_at": 1704681000000
  }
}
```
### Configured data
```json
{
  "lobby": {
    "name": "lobby",
    "online": true,
    "motd": "Welcome to the Lobby!",
    "players": {
      "online": 12,
      "max": 100
    },
    "updated_at": 1704681000000
  },
  "survival": {
    "name": "survival",
    "online": false,
    "motd": null,
    "players": {
      "online": 0,
      "max": 0
    },
    "updated_at": 1704681000000
  }
}

```
## Field Description

### Root Object

| Field     | Type    | Description                                |
|-----------|---------|--------------------------------------------|
| `servers` | `array` | List of servers defined in `velocity.toml` |

---

### Server Object

|    Field     |   Type    | Description                                                       |
|:------------:|:---------:|:------------------------------------------------------------------|
|    `name`    | `string`  | Server name defined in `[servers]` section of `velocity.toml`     |
|  `address`   | `string`  | Server address (`host:port`)                                      |
|   `online`   | `boolean` | Whether the server responded to a ping                            |
|    `motd`    | `string`  | Server MOTD (may contain legacy color codes)                      |
|  `players`   | `object`  | Player information (only present if enabled and server is online) |
|  `version`   | `object`  | Minecraft version information                                     |
| `updated_at` | `number`  | Last update timestamp(UNIX time)                                  |

---

### Players Object

|  Field   |   Type   | Description                      |
|:--------:|:--------:|----------------------------------|
| `online` | `number` | Current number of online players |
|  `max`   | `number` | Maximum player capacity          |

---

### Version Object

|   Field    |   Type   | Description                         |
|:----------:|:--------:|-------------------------------------|
|   `name`   | `string` | Version name reported by the server |
| `protocol` | `number` | Minecraft protocol version          |

---

## Notes

* Fields may be **omitted** depending on `config.yml` settings.
* If a server is offline, only `name`, `address`, and `online` are guaranteed.
* This API is intended for **internal dashboards, status pages, or monitoring tools**.
# Issue and question
If the plugin does not work properly, try deleting config.yml.  
Problems and questions feature request [here]([https://github.com/stellarcielo/velostat/issues).