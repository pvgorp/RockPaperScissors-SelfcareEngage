sudo keytool -import -v -trustcacerts -alias test.huss.nl -file \*.test.huss.nl.cer -keystore $(/usr/libexec/java_home)/jre/lib/security/cacerts -keypass changeit -storepass changeit
