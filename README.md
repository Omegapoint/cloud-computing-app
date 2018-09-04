# Cloud Computing with AWS

## Goal

The goal of this course is to introduce some of the most common services found in Amazon Web Services (AWS). More specifically we'll run a simple Spring Boot application on a virtual server, connect the application to a database and make the Spring Boot application reachable through a load balancer and with an external DNS name. Finally, we'll setup a deployment pipeline for the Spring Boot application.

## Content

1. [Project setup](#setup)
2. [Deploy on EC2](#ec2deploy)
3. [Connecting your application to a RDS instance](#database)
4. [Code Pipeline Preparations](#CodePipeline1)
5. [Create the CodePipeline](#CodePipeline2)
6. [Load balancing with Elastic Load Balancer](#Balancer)
7. [Subdomain with Route53](#Route53)
8. [Labs with less instructions](#less)


<a name="setup"></a>
## 1. Project setup 

### Installation

 1. Install [Java 8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
 2. Install [Git](https://git-scm.com/book/en/v2/Getting-Started-Installing-Git)
 3. Install the latest [PgAdmin](https://www.postgresql.org/ftp/pgadmin/pgadmin4)  

 
### Verify local environment

 1. Create a new [Git repository](https://github.com/Omegapoint/) called `aws-<your-name>` Don't forget to delete the repository when your done with the course.
 2. Clone the template repository `$ git clone --bare https://github.com/Omegapoint/cloud-computing-app.git`
 3. `$ cd cloud-computing-app.git`
 4. `$ git push --mirror https://github.com/Omegapoint/aws-<your-name>.git` If you're using ssh to access Github, run: `$ git push --mirror git@github.com:Omegapoint/aws-<your-name>.git`
 5. `$ cd ..`
 6. `$ rm -rf cloud-computing-app.git`
 7. `$ git clone https://github.com/Omegapoint/aws-<your-name>.git` If you're using ssh to access Github, run: `$ git clone  git@github.com:Omegapoint/aws-<your-name>.git`
 8. `$ cd aws-<your-name>`
 3. Build the application `$ ./gradlew clean build`
 4. Run the application `$ ./gradlew bootRun -Dspring.profiles.active=local`


<span style="color:orange">**Checkpoint 1**</span> You can now go to `http://localhost:8080/cloud-computing-template-app/ping` in your browser att receive a _"pong"_.

If you're using IntelliJ, import the new project by: _New Project_ --> _From Existing Sources_ . Choose Gradle as build tool.

### Modify application properties

Change the name of the application in `application.properties`:

 1 .`spring.application.name=<application-name>`  
 2. `$ ./gradlew clean build`  

<span style="color:orange">**Checkpoint 2**</span> Gradle should now have built the jar `build/libs/<application-name>-1.0-SNAPSHOT.jar`. If you once again run the application as above in step 10, you should be able to go to `http://localhost:8080/<application-name>/ping` in your browser att receive a _"pong"_.


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


<span style="color:orange">**Checkpoint 3**</span> Verify that you can run the application with the new production profile and production configuration. Build the application as before, then run the application with:

`$ java -jar -Dspring.profiles.active=production build/libs/<application-name>-1.0-SNAPSHOT.jar`   

on your local machine. 
<a name="ec2deploy"></a>
## Deploy on EC2

### Setup instance
[Login to AWS](https://sts.omegapoint.se/adfs/ls/IdpInitiatedSignOn.aspx).

Note: Don't forget to always use Ireland as the region in AWS.

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



<span style="color:orange">**Checkpoint 4**</span> You can now SSH to your EC2 instance with the key you've generated. The username is **ec2-user**. To ssh with a your key use the following command: `ssh -i <path-to-your-pem-file> ec2-user@<public IP of the EC2 instance>`


### Manual deploy

 1. Upload your jar to to the instance with scp.
 2. Install java with `yes | sudo yum install java-1.8.0`
 3. Start the application by using the command `java -jar -Dspring.profiles.active=production <application-name>.jar`.

<span style="color:orange">**Checkpoint 5**</span> You can browse to `http://<URL-of-your-ec2>:8080/<application-name>/ping` and get the response _"pong"_

<a name="database"></a>
## Connect application to a DB

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
	"applicationName": "<your application name>",
	"timeStamp": "<timestamp>",
	"data": "tniopagemo"
}	
```

### Cloud DB

[Login to AWS](https://sts.omegapoint.se/adfs/ls/IdpInitiatedSignOn.aspx). Go to **RDS** under _"Database"_. Click on the orange button _"Create database"_ and choose _PostgreSQL_. 


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
  - Databasename: `<application-name>_db`
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

 
<span style="color:orange">**Checkpoint 7**</span> You should now be able to run `java -jar -Dspring.profiles.active=production <application-name>-1.0-SNAPSHOT.jar` **on your ec2 instance** and connect your application to the RDS instance. Confirm by going to `http://<ec2-url>:8080/<application-name>/reverse/omegapoint` in your browser att receive the following content:

```javascript
{
	"input": <	
}	
```
<a name="CodePipeline1"></a>
## Code Pipeline preparations
The goal in this lab is to automatically test and build using CodeBuild and deploy the application after every push to master.
You will deploy using CodeDeploy which requires an agent application to be running on the EC2 instance. Therefore you will provision a new instance (using CloudFormation) and install the agent automatically.

### Provision new EC2 instance using CloudFormation
 1. Terminate the EC2 instance you provisioned in the previous lab
 2. Download this [CloudFormation template](http://s3-eu-west-1.amazonaws.com/aws-codedeploy-eu-west-1/templates/latest/CodeDeploy_SampleCF_Template.json).
 3. Open the template in an editor. After line 233 we want to install Java 8 and uninstall Java 7 as we did with our previous instance. Add the following lines:

```bash
"yum install java-1.8.0 -y \n",
"yum remove java-1.7.0 -y \n",
```
4. Go to the AWS service CloudFormation and click Create stack.
5. Choose _Upload a template to Amazon S3_ and choose your modified template .json file
6. Specify details and parameters
 - Stack name: `<application-name>`
 - InstanceCount: `1`
 - InstanceType: `t1.micro`
 - KeyPairName: `<application-name>-key`
 - OperatingSystem: `Linux`
 - SSHLocation: `My IP`
 - TagKey: `Name`
 - TagValue: `<application-name>`
7.  Click Next, then Next again,
8. Tick the checkbox _I acknowledge that AWS CloudFormation might create IAM resources._ and click Create.

<span style="color:orange">**Checkpoint 8**</span> Make sure the template creation runs smooth and that your stack in CloudFormation gets the status _CREATE\_COMPLETE_. Find the new EC2 instance and make sure you can SSH to it.


### Bulid specification for CodeBuild
CodeBuild requires a buildspec.yml file to be in the root of your application:

##### buildspec.yml

```yaml
version: 0.2
phases:
  build:
    commands:
      - echo Build started on `date`
      - sh gradlew clean assemble
artifacts:
  files:
    - appspec.yml
    - 'build/libs/*.jar'
    - start_application.sh
  discard-paths: yes
```

### Deploy specification for CodeDeploy
CodeDeploy requires two files:

##### appspec.yml

```yaml
version: 0.0
os: linux
files:
  - source: <application-name>-<version>.jar
    destination: /tmp
hooks:
  ApplicationStart:
    - location: start_application.sh
      timeout: 500
      runas: root
```

##### start_application.sh

```bash
#!/bin/bash

touch app.log
nohup java -jar /tmp/*.jar -Dspring.profiles.active=production > app.log 2>&1 &
```
You may have to modify these files to fit your application.

<span style="color:orange">**Checkpoint 9**</span> Push these files to your repository.
<a name="CodePipeline2"></a>
## Create the CodePipeline
 1. Go to the service CodePipeline and click _Create Pipeline_
 2. Name it `<application-name>-CodePipeline` and click next step
 3. For _Source provider_ chooce Github and click _Connect to Github_ and authorize AWS to access your Github resources
 4. In _Repository_ choose your application repository, then select the branch on which the version of the application that you want to deploy is (typiclly _master_). Then click _next_.
 
#### CodeBuild

 * **Build**
  - Build provider: `AWS CodeBuild`
  - Configure your project -> Create a new build project
  - Project Name: `<application-name>-CodeBuild`
 * **Environment: How to build**
	  - Environment image: `Use an image managed by AWS CodeBuild`
	  - Operating system: `Ubuntu`
	  - Runtime: `Java`
	  - Version: _"aws/codebuild/java:openjdk-8"_
	  - Build specification: _"Use the buildspec.yml in the source code root directory"_
 * **AWS CodeBuild service role**
	  - Select _Create a service role in your account_
	  - Role name: Leave as default
	  - Click _Save build project_
	  - After the build project is saved click _Next step_

#### CodeDeploy
 
 Deployment provider: `AWS CodeDeploy`
 
  * **AWS CodeDeploy**
  	- Click the link _create a new one in AWS CodeDeploy_
  	- Application name: `<application-name>-Application`
  	- Deployment group: `<application-name>-DeploymentGroup`
  	- Deployment type: In-place deployment
  * **Environment configuration**
	  - Choose Amazon EC2 instances
	  - Key: `Name`
	  - Value: `<application-name>` make sure you see the EC2 instance created by the CloudFormation template in the _Matching instances_ section
	  - Do not tick the box _Enable load balancing_
 * **Deployment configuration**
	  - Leave as default
 * **Service role**
	  - Service role ARN: Select the role named `<your-application-name>-CodeDeployTrustRole-<hash>`
	  - Click create application

#### CodePipeline
 
 Go back to the pipeline tab
 
  * **AWS CodeDeploy**
	  - Application name: `<application-name>-Application`
	  - Deployment group: `<application-name>-DeploymentGroup`
	  - Click _Next step_
	  - Role name: `AWS-CodePipeline-Service`
	  - Click _Next step_
	  - Review your pipeline, then click _Create pipeline_

<span style="color:orange">**Checkpoint 10**</span> At this point you should see your pipeline. Unless it starts automatically click `Release change`. Make sure your source code is built and deployed successfully. If there are issues, resolve these before moving on.

<a name="Balancer"></a>
## Load balancing with Elastic Load Balancer
 1. Go to EC2 -> Load Balancers and click _Create Load Balancer_
 2. _Create_ an Application Load Balancer
 3. **Basic Configuration**
    - Name: `<application-name>-ALB`
    - Scheme: _internet-facing_
    - IP address type: _ipv4_
 4. **Listeners**
    - Leave as default
 5. **Availability Zones**
    - VPC: Select whichever VPC is marked _default_
    - Select all availability zones
    - Click _Next: Configure Security Settings
 6. **Conigure Security Settings**
    - Leave as defualt, click _Next: Configure Security Groups_
 7. **Configure Security Groups**
    - Choose _Select an **existing** seurity group_
    - Select the group previously created: `<application-name>-security-group`
    - Click _Next: Configure Routing_
 8. **Configure Routing**
    - Target group: New target group
    - Name: `<application-name>-TargetGroup`
    - Protocol: HTTP
    - Port: 8080 (or the custom port on which your application is running)
    - Target type: instance
    - **Healtch Checks**
    - Protocol: HTTP
    - Path: any valid path where your application responds with `HTTP 200`
    - **Advanced healthcheck settings**
    - Port: traffic port
    - Healthy treshold: 2
    - Unhealthy treshold: 2
    - Timeout: 5
    - Interval: 10
    - Success codes: 200-299
    - Click _Next: Register Targets_
 9. **Register Targets**
    - Search for your EC2 instance in the _Instances_ section
    - Select your instance and click _Add to registered_
    - Click _Next: Review_
 10. **Review**
    - Review your Load Balancer configuration and click _Create_

<span style="color:orange">**Checkpoint 10**</span> Verify your ALB is working

* Go to EC2 -> Load Balancers and find your Load Balancer
* Wait until the state of the Load Balancer changes from _provisioning_ to _active_
* Go to EC2 -> Target groups and find the target group containing your instance
* Click the _Targets_ tab and verify that your instance has the status healthy. If properly configured it will take up to 20 seconds for the instance to pass its healthcheck.
* Browse to the DNS name of the Load Balancer (including request mapping) and verify that you reach your application

<a name="Route53"></a>
## Subdomain with Route53
 1. Go to Route53 -> Hosted zones and click _lab.omegapint.academy_.
 2. Click _Create record set_
 3. **Creat record set**
    - Name: choose a subdomain (e.g. test.lab.omegapoint.academy)
    - Type: A - IPv4 address
    - Alias: Yes
    - Alias target: select your ALB
    - Routing policy: Simple
    - Evaluate Targe Health: No
    - Click: _Save Record Set_

<span style="color:orange">**Checkpoint 10**</span> Verify that you reach your application using the subdomain
<a name="less"></a>
## Labs with less instructions


### Auto scaling
Make your application highly available by launching your EC2 instances from/in an auto scaling group.

High level outline
  - Create an auto scaling group and launch configuration
  - Configure CodeDeploy to deploy to the auto scaling group instead of the single EC2 instance used in the previous labs
  - Configure the ALB to route to the EC2 instances in your auto scaling group.

### Lambda
Create a Lambda function with API Gateway replicating the behaviour of the Spring application used in previous labs.

Java is optional. Consider using the [Serverless framework](https://serverless.com/).

This lab could be finished by configuring weighted routing for your record set in Route53. Equal weights would send half the traffic to your EC2 instance, and half the traffic to your Lambda.
