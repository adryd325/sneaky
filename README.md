# Sneaky

Sneaky anonymises your server from server scanners like Copenheimer, Shodan or ServerSeeker by only showing server info to players who have previously joined.

Sneaky works by keeping a list of IP addresses of known players and their last join date, only allowing known players to receive the real server info, and appearing as an unconfigured server with no player activity to server scanners and players who have never joined.

Sneaky is meant to be a no compromise alternative to vanilla's `hide-online-players` feature, allowing members of a server to see relevant information without leaking that information to scanners or un-whitelisted players (in the case of a whitelisted server)

## Before joining
Before first joining, players and server scanners see a generic, unconfigured server with no player activity

<img src="https://cdn.modrinth.com/data/HRXgZcrv/images/887cc374e4fc681be15f5617da9d0381262e1bc4.png">

## After joining
After their first join, players will see the real MOTD, server icon, and online players list

<img src="https://cdn.modrinth.com/data/HRXgZcrv/images/83de9372ec2ba50ac49375e5e6e19f1ab720bce0.png">

## Protection from certain Denial of Service attacks
An attack vector against servers with server icons is to repeatedly ping that server to get it to send the icon. If a server is whitelisted, it can prevent random people from attacking a server in this way. 

I plan to eventually implement handshake ratelimits to make this kind of attack harder on all servers, not just whitelisted ones.
