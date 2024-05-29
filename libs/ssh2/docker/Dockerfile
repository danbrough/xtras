FROM debian:bookworm-slim
ARG PASSWORD
RUN apt-get update && apt-get install -y openssh-server nano
RUN mkdir /var/run/sshd
# Set root password for SSH access (change 'your_password' to your desired password)
RUN useradd -m fred -s /bin/bash
RUN useradd -m sally -s /bin/bash

RUN mkdir -p /home/sally/.ssh
COPY sally.key.pub /home/sally/.ssh/authorized_keys
RUN chown -R sally:sally /home/sally/.ssh

RUN echo "root:$PASSWORD"  | chpasswd
RUN echo "fred:$PASSWORD"  | chpasswd
RUN echo "sally:$PASSWORD"  | chpasswd
RUN echo 'PermitRootLogin yes' >>  /etc/ssh/sshd_config
RUN sed 's@session\s*required\s*pam_loginuid.so@session optional pam_loginuid.so@g' -i /etc/pam.d/sshd
RUN echo
EXPOSE 22
CMD ["/usr/sbin/sshd", "-D"]


