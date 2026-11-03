This plugin provides a lightweight HTTP API that exports the real-time status of all Velocity backend servers in JSON format.
Exported fields are fully configurable via config.yml, allowing safe public exposure.

The game version is for all versions supported by Velocity 4.3.0 as it is added to the proxy server only. The game version must be specified and is specified, but see the [PaperMC website](https://docs.papermc.io/velocity/server-compatibility/) for details.
# Requirements
- Velocity 4.3.0 or later
- Java 25 or later

Velocity 3.x is no longer supported since velostat 2.0.
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
| `prometheus.enabled`       | Whether to expose metrics in the Prometheus format.                                | `true`  |
| `prometheus.path`          | The path of the Prometheus metrics endpoint.                                       | `/metrics` |
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
# Prometheus metrics
Accessing `http://<SERVER_IP>:<PORT>/metrics` returns metrics in the Prometheus text format.
The path can be changed with `prometheus.path`, and the endpoint can be disabled with `prometheus.enabled`.

| Metric                                          | Labels                          | Description                                                     |
|:------------------------------------------------|:--------------------------------|:----------------------------------------------------------------|
| `velostat_proxy_players_online`                 | -                               | Number of players connected to the proxy                        |
| `velostat_server_up`                            | `server`                        | `1` if the server responded to the last ping, otherwise `0`     |
| `velostat_server_players_online`                | `server`                        | Number of online players (online servers only)                  |
| `velostat_server_players_max`                   | `server`                        | Maximum player capacity (online servers only)                   |
| `velostat_server_ping_duration_seconds`         | `server`                        | Time taken by the last successful ping (online servers only)    |
| `velostat_server_info`                          | `server`, `version`, `protocol` | Always `1`; carries the version information (online servers only) |
| `velostat_server_last_update_timestamp_seconds` | `server`                        | Unix time of the last status update                             |

All metrics are gauges. The `server` label is the server name defined in `velocity.toml`.
### Example output
```
# HELP velostat_proxy_players_online Number of players connected to the proxy.
# TYPE velostat_proxy_players_online gauge
velostat_proxy_players_online 12
# HELP velostat_server_up Whether the backend server responded to the last ping (1 = online, 0 = offline).
# TYPE velostat_server_up gauge
velostat_server_up{server="lobby"} 1
velostat_server_up{server="survival"} 0
# HELP velostat_server_players_online Number of players online reported by the backend server.
# TYPE velostat_server_players_online gauge
velostat_server_players_online{server="lobby"} 12
# HELP velostat_server_players_max Maximum number of players reported by the backend server.
# TYPE velostat_server_players_max gauge
velostat_server_players_max{server="lobby"} 100
# HELP velostat_server_ping_duration_seconds Time taken by the last successful ping to the backend server.
# TYPE velostat_server_ping_duration_seconds gauge
velostat_server_ping_duration_seconds{server="lobby"} 0.003916254
# HELP velostat_server_info Version information reported by the backend server.
# TYPE velostat_server_info gauge
velostat_server_info{server="lobby",version="Paper 1.21.1",protocol="767"} 1
# HELP velostat_server_last_update_timestamp_seconds Unix time of the last status update for the backend server.
# TYPE velostat_server_last_update_timestamp_seconds gauge
velostat_server_last_update_timestamp_seconds{server="lobby"} 1704681000.000
velostat_server_last_update_timestamp_seconds{server="survival"} 1704681000.000
```
### Prometheus configuration example
```yaml
scrape_configs:
  - job_name: velostat
    static_configs:
      - targets: ["<SERVER_IP>:8080"]
```
# Issue and question
If the plugin does not work properly, try deleting config.yml.  
Problems and questions feature request [here]([https://github.com/stellarcielo/velostat/issues).