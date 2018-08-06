sudo keytool -import -v -trustcacerts -alias test2.huss.nl -file myserver.crt -keystore $(/usr/libexec/java_home)/jre/lib/security/cacerts -keypass changeit -storepass changeit
