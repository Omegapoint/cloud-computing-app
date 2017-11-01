# Cloud Computing with AWS

1. [Project setup](#setup)
2. [Deploy on EC2](#ec2deploy)
3. [Connecting your application to a RDS instance](#database)


<a name="setup"></a>
## 1. Project setup 

### Installation

 1. Install [Java 8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
 2. Install [Git](https://git-scm.com/book/en/v2/Getting-Started-Installing-Git)
 4. Install [PgAdmin](https://www.postgresql.org/ftp/pgadmin/pgadmin4/v2.0/)  

 
### Verify local environment

 1. Create a new [Git repository](https://github.com/Omegapoint/) called `<application-name>`
 2. Clone the template repository `$ git clone --bare https://github.com/Omegapoint/cloud-computing-app.git`
 3. `$ cd cloud-computing-app.git`
 4. `$ git push --mirror https://github.com/Omegapoint/<application-name>.git`
 5. `$ cd ..`
 6. `$ rm -rf cloud-computing-app.git`
 7. `$ git clone https://github.com/Omegapoint/<application-name>.git`
 8. `$ cd <application-name> b`
 3. Build the application `$ ./gradlew clean build`
 4. Run the application `$ ./gradlew bootRun -Dspring.profiles.active=local`


<span style="color:orange">**Checkpoint 1**</span> You can now go to `http://localhost:8080/cloud-computing-template-app/ping` in your browser att receive a _"pong"_.

If you're using IntelliJ, import the new project by: _New Project_ --> _From Existing Sources_ . Choose Gradle as build tool.

### Modify applicatin properties

Change the name of the application in `application.properties`:

 1 .`spring.application.name=<application-name>`  
 2. `$ ./gradlew clean build`  

<span style="color:orange">**Checkpoint 2**</span> Gradle should now have built the jar `build/libs/<application-name>-1.0-SNAPSHOT.jar` and you should be able to go to `http://localhost:8080/<application-name>/ping` in your browser att receive a _"pong"_.


Create a new property file in `src/resources/` for production environment: `application-production.properties` with the following content:

```properties
spring.profiles.active=production
server.port=8080

# Datasource
spring.jpa.hibernate.ddl-auto=create-drop
spring.datasource.url=jdbc:h2:file:~/test
spring.datasource.username=sa
spring.datasource.password=
spring.datasource.driver-class-name=org.h2.Driver

# h2 settings
spring.h2.console.enabled=true
spring.h2.console.path=/h2

```


<span style="color:orange">**Checkpoint 3**</span> You can now run the application with:

`$ java jar -Dspring.profiles.active=production build/libs/<application-name>-1.0-SNAPSHOT.jar`   

on your local machine. 
<a name="ec2deploy"></a>
## Deploy on EC2

### Setup instance
[Login to AWS](https://sts.omegapoint.se/adfs/ls/IdpInitiatedSignOn.aspx). 

 1. **Go to the Service -> EC2 -> Key pairs.** 
 2. **Create a new key** and download an ssh key. You will use this to connect to instances that you provision during this lab. The key should be called `<application-name>-key` 
 3. **Choose an Instance Type**
  - Go to the Service -> EC2 -> Instances. 
  - Launch an EC2-instance using Amazon Linux on a *t2.micro* instance. Click _Next: Configure Instance Details_
 5. **Configure Instance Details** - Leave all fields as default
 6. **Add Storage** - Leave all fields as default 
 7. **Add Tags** 
  - Key: `Name`
  - Value `<application-name>`
 8. **Configure Security Group 1**
  - Security group name: `<application-name>-security-group`
  - Type: _SSH_
  - Protocol: _TCP_
  - Port Range: `22`
  - Source: `My IP`
  - Description: `SSH for admin`
 9. **Configure Security Group 2**
  - Type: _Custom TCP_
  - Protocol: _TCP_
  - Port Range: `8080` (or whichever port your application is running)
  - Source: `My IP`
  - Description `<insert-something-smart>`
 10. **Review Instance Launch** - Leave all fields as default
 11. **Select an existing key pair or create a new key pair**
  - _Choose an existing key pair_
  - `<application-name>-key`



<span style="color:orange">**Checkpoint 4**</span> You can now SSH to your EC2 instance with the key you've generated. The username is **ec2-user**.


### Manual deploy

 1. Upload your jar to to the instance with scp.
 2. Install java with `yes | sudo yum install java-1.8.0` and remove Java 1.7 with `yes | sudo yum remove java-1.7.0`
 3. Start the application by using the command `java -jar -Dspring.profiles.active=production <application-name>.jar`.
 4. Verify that your app is accessible from the internet by browsing the public IP address of the instance.

<span style="color:orange">**Checkpoint 5**</span> You can browse to `http://<URL-of-your-ec2>:8080/<application-name>/ping` and get the response _"pong"_

<a name="database"></a>
## Connect applicatoin to a DB

### Local DB
_Note: This section ("Local DB") is not mandatory and you could jump directly to "Cloud DB" if you want._

 1. Install PostgreSQL on your local machine
 2. Modify the content of `application-local.properties`:
    
    ```properties
    spring.profiles.active=local
	server.port=8080

	# Datasource
	spring.jpa.hibernate.ddl-auto=create-drop
	spring.datasource.url=jdbc:postgresql://localhost:5432/<db-name>
	spring.datasource.username=<username>
	spring.datasource.password=<password>
    ```
    
    
<span style="color:orange">**Checkpoint 6**</span> You can now go to `http://localhost:8080/<application-name>/reverse/omegapoint` in your browser att receive the following content:

```javascript
{
	"input": <	
}	
```

### Cloud DB

[Login to AWS](https://sts.omegapoint.se/adfs/ls/IdpInitiatedSignOn.aspx). Go to **RDS** under _"Database"_. Click on the orange button _"Launch a DB Instance"_ and choose _PostgreSQL_. 


Make the following choices:

 1. **Choose use case**
  - Dev/Test
 
 2. **Specify DB details** 
  - DB engine version: _PostgreSQL 9.6.2-R1_
  - DB instance class: _db.t2.micro_
  - Allocated storage: `20 GB`
  - DB instance identifier: `<application-name>-db-instance`
  - Master username: `<username>`
  - Master password: `<password>`
 3. **Configure advanced settings**
  - Virtual Private Cloud (VPC): _Default VPC_
  - Public accessibility: _Yes_
  - Databasename: `<application-name>-db`
  - Databaseport: `5432`
  - Backup: _No preference_
  - Monitoring: _Disable_
  - Maintenance: _Disable_
  - Maintenance window: _No preference_
 
 On your RDS overview, choose _"Instances"_ and collect the following info:
 
  * Endpoint `<endpoint>`
  * DB Name `<db-name>`
  * Username `<username>`
  * Password `<password>`

Insert the information into `application-production.properties` in your project:

```properties 
spring.profiles.active=production
server.port=8080

# Datasource
spring.jpa.hibernate.ddl-auto=create-drop
spring.datasource.url=jdbc:postgresql://<endpoint>:5432/<db-name>
spring.datasource.username=<username>
spring.datasource.password=<password>
```

Remember to configure the security group of your RDS instance so it allows _inbound_ traffic from your EC2 instance. 

 
<span style="color:orange">**Checkpoint 7**</span> You should now be able to run `java jar -Dspring.profiles.active=production <application-name>-1.0-SNAPSHOT.jar` **on your ec2 instance** and connect your application to the RDS instance. Confirm by going to `http://<ec2-url>:8080/<application-name>/reverse/omegapoint` in your browser att receive the following content:

```javascript
{
	"input": <	
}	
```
