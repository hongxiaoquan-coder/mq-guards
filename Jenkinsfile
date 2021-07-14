#!/usr/bin/env groovy

// The library is defined under <Jenkins_URL>/configure - "Global Pipeline Libraries".
library("quark@lib")
//library("quark@master")

// Defined in http://gitlab-ee.hktrd.cn/quark/jenkins-pipeline-library/blob/master/vars/buildSpringbootDubboApp.groovy
buildSpringbootDubboApp builtJar: "mq-guards-service/target/mq-guards-service-1.0.0-SNAPSHOT.jar", deployJar: "service-mq-guards.jar",
        rdSvcName: "service-mq-guards",  dubboPort: 20110,  rdK8s: true, projectType: "service", springProfile: "mq-guards", javaOpts: " -XX:HeapDumpPath=/usr/local/service-mq-guards/logs/dump/",
        mvnCompileOpts: " -Denv=rd -Dapollo.meta=http://meta.apollo.hkbackend.com sonar:sonar -Dsonar.host.url=http://192.168.100.46:9000 -Dsonar.login=54d15b87324f97b1974c9f2c1e127fa026691817 -Dsonar.scm.provider=git -Dsonar.userHome=/tmp -Duser.home=/tmp "
//-Dgroups=com.hk.simba.contract.test.UnitTests