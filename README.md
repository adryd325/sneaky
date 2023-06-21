# Sneaky

Sneaky anonymises your server from server scanners like Copenheimer, Shodan or ServerSeeker by only showing server info to players who have previously joined.

Sneaky has other options to protect your server including disabling logs for unauthorised players, and ratelimiting the creation of new connections.

## Configuration
The configuration is stored in config/sneakyserver/config.properties

`hide-server-ping-data`: (Default: true) Only sends server ping data to known players

`hide-player-list`: (Default: false) Sends server ping data to unknown players, but doesn't send a user list. Only works when "hide-server-ping-data" is false.

`dont-log-unauthed-client-disconnects`: (Default: false) Prevents the server from logging failed offline-mode player logins. Prevents logs when the client closes the connection. Example of a server being spammed with offline mode logins: https://www.reddit.com/r/Minecraft/comments/12ip3gd/who_is_shepan/

`dont-log-unauthed-server-disconnects`: (Default: false) Prevents the server from logging failed offline-mode player logins. Prevents logs when the server closes the connection

`rate-limit-new-connections`: (Default: true) Enables a rate limit for clients creating new connections.

`new-connection-rate-limit`: (Default: 5) Connection limit over 15 seconds. If a client exceeds this limit new connections will be closed.


## Hiding ping data
Sneaky hides server info by keeping a list of IP addresses of known players and their last join date, only allowing known players to receive the real server info, and appearing as an unconfigured server with no player activity to server scanners and players who have never joined. this feature is meant to be a no compromise alternative to vanilla's `hide-online-players` feature, allowing members of a server to see relevant information without leaking that information to scanners or un-whitelisted players (in the case of a whitelisted server)

### Before joining
Before first joining, players and server scanners see a generic, unconfigured server with no player activity

<img src="https://cdn.modrinth.com/data/HRXgZcrv/images/887cc374e4fc681be15f5617da9d0381262e1bc4.png">

### After joining
After their first join, players will see the real MOTD, server icon, and online players list

<img src="https://cdn.modrinth.com/data/HRXgZcrv/images/83de9372ec2ba50ac49375e5e6e19f1ab720bce0.png">