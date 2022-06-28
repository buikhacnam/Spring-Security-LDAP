# Spring Security LDAP

LDAP definition: https://en.wikipedia.org/wiki/Lightweight_Directory_Access_Protocol

In order to write a Spring Security application that authenticates users against an LDAP server, we need an LDAP server.

In this project we will be running our own dev instance of an open source LDAP server instead of using an LDAP server that we have created.

This LDAP server is going to be running on our local machine. It's going to hold all information about our users in memory.

It's not something that we want to use in production, but it's something that we want to use in development.

This then can be easily switched to a real LDAP server in the future.

### Some necessary dependencies:

```html
<dependency>
    <groupId>com.unboundid</groupId>
    <artifactId>unboundid-ldapsdk</artifactId>
</dependency>

<dependency>
    <groupId>org.springframework.ldap</groupId>
    <artifactId>spring-ldap-core</artifactId>
</dependency>

<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-ldap</artifactId>
</dependency>
```

#### UnboundID LDAP SDK For Java
This is the open source LDAP server that we are using.

#### Spring LDAP Core
This is Spring integration that works with the LDAP.

#### Spring Security LDAP
This basically helps spring security to work with LDAP.


### Configure the LDAP server

In the file application.yml, we need to configure the LDAP server.

```manifest
spring: # This is the root element of the Spring configuration
  ldap:
    embedded:
      port: 8389
      ldif: classpath:ldap-data.ldif
      base-dn: dc=springframework,dc=org

```
- port: where the embedded LDAP server will listen for connections
- ldif (ldap data interchange format): The LDIF file that contains the 'seeded' data that we want to store in the LDAP server.
- base-dn: The base DN tells our embedded LDAP what the root node is. This is what it maps to `dc` value in the LDIF file. In this file, we have a `dc` value of `springframework` and `org`. But it can be any value.(Ex: 'foo', 'bar', 'baz')

### Set up mock User Data
LDAP servers can use LDIF (LDAP Data Interchange Format) files to exchange user data. The spring.ldap.embedded.ldif property inside application.properties lets Spring Boot pull in an LDIF data file. This makes it easy to pre-load demonstration data. The following listing (from src/main/resources/test-server.ldif) shows an LDIF file that works with this example:

Please check file `src/main/resources/ldap-data.ldif`
```ldif
dn: dc=springframework,dc=org
objectclass: top
objectclass: domain
objectclass: extensibleObject
dc: springframework

dn: ou=groups,dc=springframework,dc=org
objectclass: top
objectclass: organizationalUnit
ou: groups

dn: ou=subgroups,ou=groups,dc=springframework,dc=org
objectclass: top
objectclass: organizationalUnit
ou: subgroups

dn: ou=people,dc=springframework,dc=org
objectclass: top
objectclass: organizationalUnit
ou: people

dn: ou=space cadets,dc=springframework,dc=org
objectclass: top
objectclass: organizationalUnit
ou: space cadets

dn: ou=\"quoted people\",dc=springframework,dc=org
objectclass: top
objectclass: organizationalUnit
ou: "quoted people"

dn: ou=otherpeople,dc=springframework,dc=org
objectclass: top
objectclass: organizationalUnit
ou: otherpeople

dn: uid=ben,ou=people,dc=springframework,dc=org
objectclass: top
objectclass: person
objectclass: organizationalPerson
objectclass: inetOrgPerson
cn: Ben Alex
sn: Alex
uid: ben
userPassword: $2a$10$c6bSeWPhg06xB1lvmaWNNe4NROmZiSpYhlocU/98HNr2MhIOiSt36

dn: uid=bob,ou=people,dc=springframework,dc=org
objectclass: top
objectclass: person
objectclass: organizationalPerson
objectclass: inetOrgPerson
cn: Bob Hamilton
sn: Hamilton
uid: bob
userPassword: bobspassword

dn: uid=joe,ou=otherpeople,dc=springframework,dc=org
objectclass: top
objectclass: person
objectclass: organizationalPerson
objectclass: inetOrgPerson
cn: Joe Smeth
sn: Smeth
uid: joe
userPassword: joespassword

dn: cn=mouse\, jerry,ou=people,dc=springframework,dc=org
objectclass: top
objectclass: person
objectclass: organizationalPerson
objectclass: inetOrgPerson
cn: Mouse, Jerry
sn: Mouse
uid: jerry
userPassword: jerryspassword

dn: cn=slash/guy,ou=people,dc=springframework,dc=org
objectclass: top
objectclass: person
objectclass: organizationalPerson
objectclass: inetOrgPerson
cn: slash/guy
sn: Slash
uid: slashguy
userPassword: slashguyspassword

dn: cn=quote\"guy,ou=\"quoted people\",dc=springframework,dc=org
objectclass: top
objectclass: person
objectclass: organizationalPerson
objectclass: inetOrgPerson
cn: quote\"guy
sn: Quote
uid: quoteguy
userPassword: quoteguyspassword

dn: uid=space cadet,ou=space cadets,dc=springframework,dc=org
objectclass: top
objectclass: person
objectclass: organizationalPerson
objectclass: inetOrgPerson
cn: Space Cadet
sn: Cadet
uid: space cadet
userPassword: spacecadetspassword



dn: cn=developers,ou=groups,dc=springframework,dc=org
objectclass: top
objectclass: groupOfUniqueNames
cn: developers
ou: developer
uniqueMember: uid=ben,ou=people,dc=springframework,dc=org
uniqueMember: uid=bob,ou=people,dc=springframework,dc=org

dn: cn=managers,ou=groups,dc=springframework,dc=org
objectclass: top
objectclass: groupOfUniqueNames
cn: managers
ou: manager
uniqueMember: uid=ben,ou=people,dc=springframework,dc=org
uniqueMember: cn=mouse\, jerry,ou=people,dc=springframework,dc=org

dn: cn=submanagers,ou=subgroups,ou=groups,dc=springframework,dc=org
objectclass: top
objectclass: groupOfUniqueNames
cn: submanagers
ou: submanager
uniqueMember: uid=ben,ou=people,dc=springframework,dc=org
```

- dn: The distinguished name of the entry.
- objectclass: The object class of the entry.
- cn: The common name of the entry.
- sn: The surname of the entry.
- uid: The user ID of the entry.
- userPassword: The password of the entry.
- uniqueMember: The unique member of the entry.
- ou: The organizational unit of the entry.
- dc: The domain component of the entry.
- c: The country component of the entry.
- o: The organization component of the entry.
- description: The description of the entry.
- mail: The email of the entry.
- telephoneNumber: The telephone number of the entry.
- mobile: The mobile number of the entry.
- postalAddress: The postal address of the entry.
- street: The street of the entry.
- l: The city of the entry.
- st: The state of the entry.
- postalCode: The postal code of the entry.

### SecurityConfiguration

What we need to do is to allow people to be able to authenticate based on their user ID and password. 

So what we want to do is to build our Spring Security to look at LDAP instance and see if the user ID and password are correct.

In the file `src/main/java/com/buinam/springsecurityldap/SecurityConfiguration.java`, we need to add the following code:

```java
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter  {

    //tell spring security to use ldap to authenticate users
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.ldapAuthentication()
                .userDnPatterns("uid={0},ou=people") // Dn stands for distinguished name. It's basically a way to identify a user. User information is stored in ldif format
                .groupSearchBase("ou=groups") // tell the organization unit to search for is groups
                .contextSource()
                .url("ldap://localhost:8389/dc=springframework,dc=org") // url of the ldap server hosted on localhost:8389
                .and()
                .passwordCompare() // tell the ldap server to compare passwords
                .passwordEncoder(new BCryptPasswordEncoder()) // tell the ldap server to use bcrypt to hash passwords
                .passwordAttribute("userPassword"); // tell spring security to use the userPassword attribute to authenticate users
    }


    // authorize any request to the application
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                .anyRequest().fullyAuthenticated()
                .and()
                .formLogin();
    }
}


```

### HomeController For Testing

```java
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "Home Page!";
    }
}
```

If you visit the site at http://localhost:8080, you should be redirected to a login page provided by Spring Security.

Enter a user name of `ben` and a password of `benspassword`. You should see the following message in your browser:

    ```
    Home Page!
    ```
🎉🎉🎉🎉🎉🎉🎉🎉🎉

Congratulations! You have successfully authenticated with Spring Security LDAP.