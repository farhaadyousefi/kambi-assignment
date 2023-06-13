# kambi-assignment
## binary file runner

Make a call to an API with a json structure containing binary file and its needed arguments. 

## Implementation
1. The assignment has been implemented with java17 and as a spring boot application  
> the `JAVA_HOME` as a system variable should be set and Java bin directory should be added to the `path` variable as well
2. you can change the below default setting by modifining its concerend value in `src/main/resources` file  
2-1) if the execution takes more than `5000 milliseconds`, the API will return http code `408(REQUEST_TIMEOUT)`, for modify it, please change the value of `running.process.timeout` key based on the millisecond  
2-2) If SIGINT is sent to the API, the application will shutdown gracefully, and open requests should be fulfilled by `1 minute`. feel free to change it by modifying the value of the `spring.lifecycle.timeout-per-shutdown-phase` key based on minute 

## Running the app
>note:
>  for running `sudo` commands without a password prompt in Java, the `/etc/sudoers` file should be modified otherwise the request will be reached to timeout
> ```shell
> # for user
> USER_THAT_RUN_THE_APP ALL= NOPASSWD: ALL
> # for group
> GROUP_OF_USER_THAT_RUN_THE_APP ALL= NOPASSWD: ALL
> ```  

there are some options for running the applicaiton  

### 1) in your local machine
the only requrenment is `java JDK 17`  
please open a terminal and go to the root folder of the application then follow the below instructions  
* build the applicaiton  
  ```$ ./mvnw package```
* run the application  
  ```$ java -jar target/binaryRunner-0.0.1-SNAPSHOT.jar```  
* then a post API in the below address will handle the request  
```http://host_address:8080/api/v1/runner```  
### 2) as a docker image (not recommanded ;))
> note:
> the primary application responsibility is to run binary files on the OS, so I had to use a `Ubuntu` image instead of a simple image containing just `Java`. apologize for downloading the big image.
>
>
> as well as this,because the binary files are hosted on the host machine and not the application's image. I created a volume and mapped the root of the host machine to the root of the container.  
>Docker restricts mounting the root directory of the host to the root directory of the container for security and isolation reasons. Therefore the `\host` directory of the container has been mounted to the root directory of the host machine. which means the `binaryFile` key in `JSON` request, should start with `\host`
>sample:  

> ```shell
> {
>    "binaryfile":"/host/binary_file_address/binary_file",
>    "arguments": ["arg1","arg2"]
> }
> ```

for running applicaiotn on docker, please open a terminal and go to the root folder of the application then follow the below instructions    
* run the docker-compose file  
  ```$ docker-compose up```

## Application doc
please check the [swagger-ui](http://localhost:8080/swagger-ui/index.html) for calling the API or checking the input schema

## sample binary files
there are some binary files in the `src/main/resources` folder and you can use them as an request example
 
```ls.sh ```
> ```shell
> {
>    "binaryfile":"/full_path/src/main/resources/ls.sh",
>    "arguments": ["path_for_running_ls","options like -l"]
> }
> ```

```files.sh: all files and directory in specific path ```
> ```shell
> {
>    "binaryfile":"/full_path/src/main/resources/files.sh",
>    "arguments": ["path_for_running_ls"]
> }
> ```

```ping.sh: please limit the ping count otherwise it will raise timeout_exception ```
> ```shell
> {
>    "binaryfile":"/full_path/src/main/resources/ping.sh",
>    "arguments": ["google.com","-c 3"]
> }
> ```
