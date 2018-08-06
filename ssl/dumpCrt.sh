openssl s_client -showcerts -connect selfcare4me.test.huss.nl:443 2>&1 < "/" | sed -ne '/-BEGIN CERTIFICATE-/,/-END CERTIFICATE-/p' > myserver.crt
