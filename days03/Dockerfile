FROM debian:latest

RUN apt-get update && apt-get install -y \
    tor \
    nginx \
    openssh-server \
    && rm -rf /var/lib/apt/lists/*

RUN mkdir /var/run/sshd

COPY torrc /etc/tor/torrc
COPY nginx.conf /etc/nginx/nginx.conf
COPY sshd_config /etc/ssh/sshd_config

COPY assets /var/www/html/assets
COPY index.html /var/www/html/index.html

COPY entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

EXPOSE 80 4242 9050

CMD /entrypoint.sh

