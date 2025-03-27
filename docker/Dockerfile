FROM debian:trixie


RUN apt-get -y update
RUN apt -y upgrade

#build-essential
RUN apt install -y  procps git nano  unzip sudo wget  \
    file apt-transport-https make pkg-config cmake  net-tools netcat-openbsd \
    openjdk-21-jdk-headless  iputils-ping


RUN echo 'PS1="\[\033[01;31m\]\u@\h\[\033[01;32m\] \w \$\[\033[00m\] "' >> /root/.bashrc
RUN useradd -s /bin/bash xtras  -G sudo,users
RUN mkdir /home/xtras
COPY ./home/* /home/xtras
RUN chown -R xtras:xtras /home/xtras
RUN echo "xtras:xtras" | chpasswd
RUN echo "root:xtras" | chpasswd

#RUN bash /home/xtras/ndk_install.sh








