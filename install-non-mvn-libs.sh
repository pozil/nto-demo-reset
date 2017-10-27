#!/bin/sh
CUR_PATH="$PWD"
mvn deploy:deploy-file -Durl=file:///$CUR_PATH/repo/ -Dfile=non-mvn-libs/enterprise-39.jar -DgroupId=com.salesforce -DartifactId=enterprise -Dversion=39 -Dpackaging=jar
mvn deploy:deploy-file -Durl=file:///$CUR_PATH/repo/ -Dfile=non-mvn-libs/force-wsc-39.0.1-uber.jar -DgroupId=com.salesforce -DartifactId=force-wsc -Dversion=39.0.1 -Dpackaging=jar
mvn deploy:deploy-file -Durl=file:///$CUR_PATH/repo/ -Dfile=non-mvn-libs/metadata-39.jar -DgroupId=com.salesforce -DartifactId=metadata -Dversion=39 -Dpackaging=jar
