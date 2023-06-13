# kambi-assignment
## binary file runner

Make a call to an API with a json structure containing binary file and its needed arguments. 

## Implementation
1. The assignment has been implemented with java17 and as a spring boot application  
2. you can change the below default setting by modifining its concerend value in `src/main/resources` file  
2-1) if the execution takes more than `5000 milliseconds`, the API will return response code `408`, for modify it, please change the value of `running.process.timeout` key based on the millisecond  
2-2) If SIGINT is sent to the API, open threads should be fulfilled in `1 minute`. feel free to change it by modifying the value of the `spring.lifecycle.timeout-per-shutdown-phase` key based on minute 
## Running the app
there are some options for running the applicaiton
### 1) in your local machine
the only requrenment is `java JDK 17`  
please open a terminal and go to the root of the application then follow the below instructions  
* build the applicaiton  
  ```$ ./mvnw package```
* run the application  
  ```$ java -jar target/binaryRunner-0.0.1-SNAPSHOT.jar```  

### 2) as a docker image  
> warnning  
> the primary application responsibility is to run binary files on the OS, so I had to use a `Ubuntu` image instead of a simple image containing just `Java` so apologize for downloading the big image.
>
>
> as well as this, the binary files are hosted on the host machine and not the application's image. I created a volume and mapped the root of the host machine to the root of the container.  
>Docker restricts mounting the root directory of the host to the root directory of the container for security and isolation reasons. Therefore the `\host` directory of the container has been mounted to the root directory of the host machine. if you want to run the application on docker please in your `JSON` request, add `\host` at the beginning of `fileName` instead of addressing from the root (`\`)  
>sample:  

> ```shell
> {
>    "binaryfile":"/host/binary_file_address/binary_file",
>    "arguments": ["arg1","arg2"]
> }
> ```

please open a terminal and go to the root of the application then follow the below instructions    
* run the docker-compose file  
  ```$ docker-compose up```

## Application doc
  [swagger-ui](http://localhost:8080/swagger-ui/index.html)

## sample binary files
there are some binary files in the `src/main/resources` folder as an example

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
