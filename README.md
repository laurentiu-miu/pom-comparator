# Maven pom.xml Comparator

## Input
Add output from mvn dependency:tree to the Input files:
 - old.pom.xml 
 - new.pom.xml
## Output
Generates a text file with the filename yyyyMMdd-HHmmss (ex: 20231017-124101.txt) and content:
```
Total Dep OLD:134
Total Dep NEW:134
-------Difference Versions-----
org.hamcrest:hamcrest .................................. 2.2        -> 2.4
org.springframework:spring-test ........................ 4.1.8.RELEASE -> 4.2.8.RELEASE
org.springframework.boot:spring-boot-starter-log4j ..... 1.2.7.RELEASE -> 1.3.7.RELEASE
-------In Old Not In New-------
org.hamcrest:hamcrest:jar:2.2:test
org.springframework:spring-test:jar:4.1.8.RELEASE:test
org.springframework.boot:spring-boot-starter-log4j:jar:1.2.7.RELEASE:compile
-------In New Not In Old-------
org.hamcrest:hamcrest:jar:2.4:test
org.springframework:spring-test:jar:4.2.8.RELEASE:test
org.springframework.boot:spring-boot-starter-log4j:jar:1.3.7.RELEASE:compile
```
