# Oracle Cloud Wallet Directory

This directory should contain your Oracle Cloud database wallet files:

- cwallet.sso
- ewallet.p12
- tnsnames.ora
- sqlnet.ora
- ojdbc.properties
- truststore.jks
- keystore.jks

**IMPORTANT:** 
- Do NOT commit wallet files to version control
- Download your wallet from Oracle Cloud Console
- Extract all wallet files to this directory
- Update the database.properties file with your credentials

For security reasons, wallet files are excluded from git via .gitignore.
