# Sisyphus

![JDK version](https://img.shields.io/badge/jdk-11-green?logo=java) ![Gradle Version](https://img.shields.io/badge/gradle-%5E6.5-green?logo=gradle) [![Maven Central](https://img.shields.io/maven-central/v/com.bybutter.sisyphus/sisyphus-bom)](https://mvnrepository.com/artifact/com.bybutter.sisyphus/sisyphus-bom) [![Dependencies](https://img.shields.io/librariesio/release/maven/com.bybutter.sisyphus:sisyphus-dependencies)](https://libraries.io/maven/com.bybutter.sisyphus:sisyphus-dependencies)

Sisyphus is the way how we provide backend services. It integrates all tools and libraries needed for designing API
which follows the [Google API Improvement Proposals](https://aip.bybutter.com).

[中文文档](doc/zh-cn/ReadMe.md)

## We are rolling a huge boulder

Due to analyzing product documents completely, it is not particularly difficult to write an exquisite and easy-to-use
API at the beginning for most APIs.

However, many people will break the initial design of the API in the endless updates of products.

It's hard to create a strong and extensible API in the whole project lifetime, just like rolling a huge boulder
endlessly up a steep hill.

So we need an all-encompassing guide book to guide us in creating, updating, and modifying APIs.

The [Google API Improvement Proposals](https://aip.bybutter.com) is the all-encompassing guide book. Google created it
in their rich and extensive API design experience. It laid the foundation for anyone to create an extensible API.

## Good tools can help you

Choosing good tools can help you 'rolling a huge boulder' faster and easier. Sisyphus provides and integrates many tools
in your 'boulder rolling' route.

[**Kotlin**](https://kotlinlang.org/) is our target language. The mature JVM community and concise grammar are the
reasons.

[**Spring boot**](https://spring.io/projects/spring-boot) is our old friend to manage and organize our components.

[**gRPC**](https://grpc.io/) is our target API framework. Sisyphus also provides
the [HTTP and gRPC Transcoding](https://aip.bybutter.com/127) component for the environment which isn't compatible with
gRPC.

[**Sisyphus JS**](https://github.com/ButterCam/sisyphus-js) is our customized protobuf and gRPC runtime for Javascript/Typescript.

[**Sisyphus Protobuf**](/lib/sisyphus-protobuf) is our customized protobuf runtime, which designed for Kotlin.

[**Sisyphus gRPC Coroutine**](/lib/sisyphus-grpc) is our customized gRPC stub runtime, which designed for Kotlin coroutine.

[**Sisyphus gRPC RxJava**](/lib/sisyphus-grpc) is our customized gRPC stub runtime, which designed for RxJava2(Client only, design for Android).

[**Sisyphus DTO**](/lib/sisyphus-dto) is the way how we create struct without protobuf.

[**Sisyphus Test**](/lib/sisyphus-test) is the way how we test our gRPC API by data-driven.

[**Sisyphus Middleware**](/middleware) is the way how we connect Sisyphus and other systems.

[**Sisyphus Configuration Artifact**](/middleware/sisyphus-configuration-artifact) is the way how we manage
configurations and developing environment.

[**Sisyphus Kubernetes gRPC client**](/middleware/sisyphus-grpc-client-kubernetes) is the way how we implement service
discovery in Kubernetes.

[**Sisyphus Protobuf Compiler**](/tools/sisyphus-protoc) is the way how we generate Kotlin codes by `.proto` files.

[**Sisyphus Project Plugin**](/tools/sisyphus-project-gradle-plugin) is the way how we manage project and configuring
Gradle.

[**Sisyphus Protobuf Plugin**](/tools/sisyphus-protobuf-gradle-plugin) is the way how we generate code by `.proto` files
in Gradle.

**And More** tools like [CEL(Common Expression Language)](https://github.com/google/cel-spec)
, [Filtering](https://aip.bybutter.com/160) and [Ordering](https://aip.bybutter.com/132#ordering) scripts will help you
to design APIs following Google AIP.

## Rolling with Sisyphus

Ready to rolling boulder with Sisyphus already? Hold on! We need to plan our route first.

1. **System requirement**

   - Gradle 6.7+
   - JDK 11+

2. **Configure Sisyphus with gradle.properties**

   We
   use [gradle.properties](https://docs.gradle.org/current/userguide/build_environment.html#sec:gradle_configuration_properties)
   to configure global settings of Sisyphus and all Sisyphus projects.

   ```properties
   # [DEV,RT] Set developer name for developing environment.
   sisyphus.developer=higan
   # [RT] Set environment for configuration artifact.
   sisyphus.environment=production
   
   # Use 'sisyphus.repository.<name>.url' Register maven repository for Sisyphus usage.
   # [DEV,RT] Url of repository named 'snapshot'.
   sisyphus.repositories.snapshot.url=https://repo1.maven.org/maven2/
   # [DEV,RT] Optional, user name of repository.
   sisyphus.repositories.snapshot.username=
   # [DEV,RT] Optional, password of repository.
   sisyphus.repositories.snapshot.password=
   
   # Repositories for different usage, there are 4 embedded repositories.
   # 'local'(maven local), 'central'(maven central), 'jcenter', 'portal'(gradle portal).
   
   # [DEV,RT] Repositories for resolving dependencies, default value is 'local,central,jcenter,portal'.
   sisyphus.dependency.repositories=local,central,jcenter,portal
   # [DEV] Repositories for snapshot publishing, default value is 'snapshot'.
   sisyphus.snapshot.repositories=snapshot
   # [DEV] Repositories for release publishing, default value is 'release'.
   sisyphus.release.repositories=release
   # [DEV] Repositories for docker publishing.
   sisyphus.docker.repositories=
   
   # [RT] Configuration artifacts, it will be resolved in runtime.
   sisyphus.config.artifacts=foo.bar:baz:1.0.0
   ```

   > **[DEV]** for developing environment properties.
   >
   > **[RT]** for runtime environment properties.

   `gradle.properties` are shared between Gradle and Spring. Sisyphus Project Plugin will load them and configure Gradle
   automatically. Sisyphus Configuration Artifact will load them for Spring Framework.

3. **Write Protobuf schemas**

   The next step is to design APIs, which means to create a schema project and to write `.proto` files in this project.

   This is a sample schema project `build.gradle.kts` config.

   ```kotlin
   plugins {
       `java-library` // We build this project as a java library.
       kotlin("jvm") version "1.3.72" // Use the kotlin plugin to compile .kt files
       id("com.bybutter.sisyphus.project") version "1.2.2" // Use the sisyphus project management plugin.
       id("com.bybutter.sisyphus.protobuf") version "1.2.2" // Use the sisyphus protobuf compiler plugin.
   }
   
   dependencies {
       api("com.bybutter.sisyphus:sisyphus-grpc-coroutine:1.2.2") // Dependent on sisyphus grpc runtime.
       /*proto("com.foo.bar:baz:1.0.0")*/ // Use 'proto' configuration to config jars need to compile proto.
       /*protoApi("com.foo.bar:baz:1.0.0")*/ // Use 'protoApi' configuration to config needed jars in proto compiling.
       // All dependencies in 'implementation' configuration will auto add to 'protoApi' configuration.
   }
   ```

   Now we can write `.proto` files in `src/main/proto` folder.

   ```protobuf
   syntax = "proto3";
   
   option java_multiple_files = true;
   option java_package = "com.bybutter.sisyphus.examples.helloworld";
   
   package sisyphus.examples.helloworld;
   
   import "google/api/annotations.proto";
   
   // The greeting api definition.
   service GreetingApi {
     // Sends a greeting
     rpc Greet (GreetRequest) returns (GreetResponse) {
       option (google.api.http) = {
           post: "/v1:greet"
           body: "*"
       };
     }
   }
   
   // The request message containing the user's name.
   message GreetRequest {
     string name = 1;
   }
   
   // The response message containing the greetings
   message GreetResponse {
     string message = 1;
   }
   ```

   > Additionally, `kotlin` and `java` classes are able to be added to schema project too. However, we do not recommend anything like this added excluding util or helper classes.

   Use the `gradlew generateProtos` task to generate kotlin files from proto files.

4. **Implement API**

   API schema is ready now. The next step is to implement this API schema. Create a service project and refer to the
   schema project.

   This is a sample service project `build.gradle.kts` config.

   ```kotlin
   plugins {
       `java-library`
       kotlin("jvm") version "1.3.72"
       id("com.bybutter.sisyphus.project") version "1.2.2"
   }
   
   dependencies {
       api("com.bybutter.sisyphus.middleware:sisyphus-grpc-client:1.2.2") // Dependent on spring grpc runtime.
       api(project("schema:example-schema")) // Dependent on schema project.
   }
   ```

   Create spring auto-config for service project.

   ```kotlin
   @Configuration
   @ComponentScan(basePackageClasses = [AutoConfig::class])
   class AutoConfig
   ```

   Register auto-config in `src/main/resources/META-INF/spring.factories`.

   ```properties
   org.springframework.boot.autoconfigure.EnableAutoConfiguration=com.bybutter.sisyphus.examples.helloworld.AutoConfig
   ```

   Write the implementation by extend generated service classes.

   ```kotlin
   @RpcServiceImpl
   class GreetingApiImpl : GreetingApi() {
       override suspend fun greet(input: GreetRequest): GreetResponse {
           return GreetResponse {
               message = "Hello ${input.name}!"
           }
       }
   }
   ```

5. **Run the Application**

   The service project is just a non-runnable library. We need to create an application project to run our service
   projects.

   This is a sample application project `build.gradle.kts` config.

   ```kotlin
   plugins {
       application
       kotlin("jvm") version "1.3.72"
       `kotlin-spring`
       id("com.bybutter.sisyphus.project") version "1.2.2"
   }
   
   dependencies {
       implementation("com.bybutter.sisyphus.starter:sisyphus-grpc-server-starter:1.2.2") // Dependent on spring grpc starter.
       implementation("com.bybutter.sisyphus.starter:sisyphus-grpc-transcoding-starter:1.2.2") // [Optional] Enable the http-transcoding feature.
       implementation("com.bybutter.sisyphus.starter:sisyphus-protobuf-type-server-starter:1.2.2") // [Optional] Enable the type server feature.
       implementation(project("service:example-service")) 	// Dependent on service project.
   }
   ```

   We only need one function in the application project, the `main` function.

   ```kotlin
   @SpringBootApplication
   @EnableHttpToGrpcTranscoding
   class MarcoApplication
   
   fun main(args: Array<String>) {
       SpringApplication.run(MarcoApplication::class.java, *args)
   }
   ```

   Use the `gradlew bootRun` task to run our application.
