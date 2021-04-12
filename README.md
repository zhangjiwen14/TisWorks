cp -p /etc/profile /etc/profile.`date +%Y%m%d`.bak
vi /etc/profile

export JAVA_HOME=/usr/lib/jvm/java-1.8.0-openjdk-1.8.0.262.b10-0.el8_2.x86_64
lvresize -L +40G /dev/mapper/rootvg-homelv
xfs_growfs /dev/mapper/rootvg-homelv
