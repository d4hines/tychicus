# Tychicus

 > Tychicus, the dear brother and faithful servant in the Lord, will tell you everything, so that you also may know how I am and what I am doing.
- Paul of Tarsus

Have you ever needed a ready and able messenger to forward emails between two accounts? Tychicus is your man! Just provide him at a specific IMAP folder, give him a forwarding address, and he'll make sure your important emails get where they need to go!

I've only tested this for my personal use with Gmail, but it uses really standard stuff, and should work anywhere. Please let me know if you encounter any issues in using it.

## Installation, Configuration and Usage

I recommend using Docker. Here's an example configuration for Gmail:
```bash
docker run \
-e "TYCHICUS_USERNAME=youremail@gmail.com" \
-e "TYCHICUS_PASSWORD=super-secret-app-password" \
-e "TYCHICUS_FORWARDING_ADDRESS=your@forwarding.address" \
-e "TYCHICUS_FOLDER=YourTargetFolder" \
-e "TYCHICUS_SMTP_HOST=smtp.gmail.com" \
-e "TYCHICUS_SMTP_PORT=465" \
-e "TYCHICUS_IMAP_HOST=imap.gmail.com" \
d4hines/tychicus:latest
```

### Notes on Gmail usage
- If you have 2-factor authentication (and you should!) you'll need to [set an app password](https://support.google.com/accounts/answer/185833?hl=en).
- Gmail has [some idiosyncracies](https://developers.google.com/gmail/imap/imap-extensions) around their folder names. For example, if you want to get at the "Important" folder, do something like:
```
TYCHICUS_FOLDER=[Gmail]/Important
```

## TODO
- [ ] Set up ELK monitoring stack in Docker Compose.


## License

Copyright © 2018 Daniel Hines

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.


