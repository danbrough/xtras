FROM debian:trixie


RUN apt-get -y update
RUN apt -y upgrade


RUN apt install -y build-essential procps git nano  unzip sudo wget  \
    file apt-transport-https make pkg-config cmake  net-tools netcat-openbsd

RUN echo 'PS1="\[\033[01;31m\]\u@\h\[\033[01;32m\] \w \$\[\033[00m\] "' >> /root/.bashrc

RUN useradd -s /bin/bash xtras  -m -G sudo,users


RUN apt install -y openjdk-21-jdk-headless
RUN apt install -y iputils-ping


