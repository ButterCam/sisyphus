<h1 align="center">
<p>
<img src="../sisyphus_logo_github.svg" width="180px" alt="Sisyphus" />
</p>
<p>
<a href="https://github.com/grpc-ecosystem/awesome-grpc"><img alt="Awesome gRPC" src="https://raw.githubusercontent.com/sindresorhus/awesome/main/media/badge.svg" /></a>
<img src="https://img.shields.io/badge/jdk-11-green?logo=java" alt="JDK version"/>
<img src="https://img.shields.io/badge/gradle-%5E6.5-green?logo=gradle" alt="Gradle version"/>
<a href="https://mvnrepository.com/artifact/com.bybutter.sisyphus/sisyphus-bom"><img src="https://img.shields.io/maven-central/v/com.bybutter.sisyphus/sisyphus-bom" alt="Maven Central"/></a>
</p>
</h1>

Sisyphus 是我们用于提供黄油相机等产品 API 的后端框架，集成了所有我们用于设计遵循 [Google API 设计指南](https://aip.bybutter.com) 的 API 的工具和类库。

## 我们正推着巨石前进

通常在项目初期，我们可以完整的分析需求文档，所以设计出比较健全、优雅并且易于使用的 API 并不是一件非常困难的事情。

但是，随之而来的无止境的新需求往往会让我们打破之前的 API 设计。

在整个项目的生命周期中，能够一直向前端提供健壮并且拓展性强的 API 是十分困难的，就像西西弗斯那样，在陡峭的山道上推着巨石前进。

所以我们可能需要一本包罗万象的 API 设计指南来指引我们设计、更新与修改 API。

《[Google API 设计指南](https://aip.bybutter.com)》 就是我们要找的指南。Google 基于他们丰富的经验缔造了这本指南，并将其开放给任何人，能够让我们也能借鉴 Google 的经验与模式来设计健壮的 API。

## 好的工具很重要

选择好的工具可以帮助我们更快地“推动巨石”。而 Sisyphus 项目就能在我们推动巨石的道路上提供各式各样的帮助。

[**Kotlin**](https://kotlinlang.org/) 是我们主要的编程语音，我们选择它最主要的理由是，它与 Java 代码完全兼容，并且提供了非常优雅的语法能够帮助我们更快地开发。

[**Spring boot**](https://spring.io/projects/spring-boot) 是我们的基础框架，提供我们组件基本的 IoC 与 DI 功能。

[**gRPC**](https://grpc.io/) 是我们的 API 基础框架。Sisyphus 也提供了 [HTTP 与 gRPC 转码](https://aip.bybutter.com/127) 组件用于为不支持 HTTP2 的前端提供优雅的 RESTful 接口。

[**Sisyphus JS**](https://github.com/ButterCam/sisyphus-js) 是我们为 Javascript/Typescript 打造的基于 HTTP 与 gRPC 转码功能的 Protobuf 与 gRPC 运行时，可用于 Web 端。

[**Sisyphus Protobuf**](/lib/sisyphus-protobuf) 是我们自制的 Protobuf 运行时，它专为 Kotlin 设计，能够在 Kotlin 中优雅的创建 Protobuf 实体。

[**Sisyphus gRPC Coroutine**](/lib/sisyphus-grpc) 是我们自制的 gRPC stub 运行时，专为 Kotlin Coroutine 设计，客户端与服务端通用。

[**Sisyphus gRPC RxJava**](/lib/sisyphus-grpc) 是我们自制的 gRPC stub 运行时，专为 RxJava2 设计，仅包含客户端，可以用于 Android 应用。

[**Sisyphus DTO**](/lib/sisyphus-dto) 是我们在没有 Protobuf 环境下创建实体类的方式。

[**Sisyphus Test**](/lib/sisyphus-test) 是我们基于数据驱动的测试 gRPC API 的方式。

[**Sisyphus Middleware**](/middleware) 是一系列组件用于在 Sisyphus 中方便地使用各种开源组件。

[**Sisyphus Configuration Artifact**](/middleware/sisyphus-configuration-artifact) 是我们用于管理各种环境中的各种配置的组件。

[**Sisyphus Kubernetes gRPC client**](/middleware/sisyphus-grpc-client-kubernetes) 是能够在 Kubernetes 集群中快速连接别的 gRPC 服务的组件。

[**Sisyphus Protobuf Compiler**](/tools/sisyphus-protoc) 是我们的自制 Protobuf 编译器，专为 Kotlin 与我们自制的 Protobuf 运行时设计。

[**Sisyphus Project Plugin**](/tools/sisyphus-project-gradle-plugin) 是我们用于管理项目的 Gradle 插件。

[**Sisyphus Protobuf Plugin**](/tools/sisyphus-protobuf-gradle-plugin) 是我们在 Gradle 中使用自制 Protobuf 编译器的插件。

**更多** 类似于 [CEL(Common Expression Language)](https://github.com/google/cel-spec) ，[Filtering](https://aip.bybutter.com/160) 与 [Ordering](https://aip.bybutter.com/132#ordering) 工具能够帮我们更方便地遵循《Google API 设计指南》来设计 API。

## 与 Sisyphus 一起推动巨石

已经做好准备使用 Sisyphus 了吗？按照我们的教程一步一步的来吧。

1. **前置要求**

    - Gradle 6.7+
    - JDK 11+

2. **通过 gradle.properties 来配置我们的开发环境**

   我们使用 [gradle.properties](https://docs.gradle.org/current/userguide/build_environment.html#sec:gradle_configuration_properties) 来配置我们的开发环境，Sisyphus Project Gradle 插件能够读取这个文件来帮助我们简化 Gradle 配置。

   ```properties
   # [DEV,RT] 可选，设置开发者名称，当此属性被设置后，将会把项目的版本号改为 ‘<name>-SNAPSHOT’
   sisyphus.developer=higan
   # [DEV] 可选，设置用于替换开发时依赖的开发层，可以为 'API', 'PLATFORM', 'FRAMEWORK'
   sisyphus.layer=FRAMEWORK
   # [RT] 可选，设置配置 jar 中需要使用环境
   sisyphus.environment=production
   
   # 用 'sisyphus.repository.<name>.url' 来注册在 Sisyphus 中被使用到的各种仓库源（Maven、Docker）
   # [DEV,RT] 可选，设置名为 'snapshot' 的仓库地址
   sisyphus.repositories.snapshot.url=https://repo1.maven.org/maven2/
   # [DEV,RT] 可选，设置名为 'snapshot' 的仓库的用户名
   sisyphus.repositories.snapshot.username=
   # [DEV,RT] 可选，设置名为 'snapshot' 的仓库的密码
   sisyphus.repositories.snapshot.password=
   
   # 不同用途的仓库配置，Sisyphus 内置了 5 种 Maven 仓库
   # 'local'(maven local), 'central'(maven central), 'portal'(gradle portal), 'google'
   
   # [DEV,RT] 用于解析运行时依赖的 Maven 仓库，默认值为 'local,central,portal,google'
   sisyphus.dependency.repositories=local,central,portal
   # [DEV] 当版本号为快照版本时，需要推送到 Maven 仓库，默认值为 'snapshot'
   sisyphus.snapshot.repositories=snapshot
   # [DEV] 当版本号为 Release 版本时，需要推送到 Maven 仓库，默认值为 'release'
   sisyphus.release.repositories=release
   # [DEV] 推送 Docker 镜像的 Docker 仓库
   sisyphus.docker.repositories=
   
   # [RT] 配置 Jar，会在运行时下载并载入配置到 Spring 上下文中
   sisyphus.config.artifacts=foo.bar:baz:1.2.2
   ```

   > **[DEV]** 为开发时会用到的配置
   >
   > **[RT]** 为运行时会用到的配置

   `gradle.properties` 被 Gradle 与 Spring 共享，Sisyphus Project 插件会载入这些配置用于自动配置 Gradle，例如仓库配置，推送配置等等。而 Sisyphus Configuration Artifact 组件会把这些配置加入到 Spring Framework 中供运行时使用.

3. **提供 Protobuf 定义**

   下一步我们需要定义我们的 API，需要创建一个 schema 项目并且在这个项目中编写一些 `.proto` 文件。

   这是一个示例 schema 项目的 `build.gradle.kts` 配置：

   ```kotlin
   plugins {
       `java-library` // 这是一个 Java 类库项目
       kotlin("jvm") version "1.3.72" // 加入 Kotlin 插件用于编译 Kotlin 文件
       id("com.bybutter.sisyphus.project") version "1.2.2" // 用 Sisyphus Project 插件来管理与配置 Gradle
       id("com.bybutter.sisyphus.protobuf") version "1.2.2" // 使用 Sisyphus Protobuf 插件来编译 Protobuf 文件
   }
   
   dependencies {
       api("com.bybutter.sisyphus:sisyphus-grpc-coroutine:1.2.2") // 依赖于 Sisyphus gRPC 运行时，如果只是一些实体类的定义可以只依赖 Protobuf 运行时
       /*proto("com.foo.bar:baz:1.0.0")*/ // 如果有一些 Protobuf 文件是由别的 Jar 包提供，可以使用 proto 依赖来配置编译外部的 Protobuf 文件
       /*protoApi("com.foo.bar:baz:1.0.0")*/ // 如果 Protobuf 文件依赖了别的 Jar 包提供的 Protobuf 文件，可以用 protoApi 来提供这些依赖
       // 'protoApi' 配置会自动从 'implementation' 配置中拉取依赖
   }
   ```

   现在可以在 `src/main/proto` 文件夹中编写 `.proto` 文件了。

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

   > 此外 `kotlin` 与 `java` 类也可以加入到 schema 项目中。但是并不建议添加除了一些针对实体类的工具函数之外的代码。

   接下来使用 `gradlew generateProtos` 命令来通过 Protobuf 文件生成 Kotlin 类。

4. **实现 API**

   API 定义已经准备就绪，下一步就是来实现这些定义，创建一个 service 项目并且依赖于之前的 schema 项目。

   这是一个示例 service 项目的 `build.gradle.kts` 配置：

   ```kotlin
   plugins {
       `java-library`
       kotlin("jvm") version "1.3.72"
       id("com.bybutter.sisyphus.project") version "1.2.2"
   }
   
   dependencies {
       api("com.bybutter.sisyphus.middleware:sisyphus-grpc-client:1.2.2") // 依赖于 gRPC 客户端来访问其他 gRPC 服务。
       api(project("schema:example-schema")) // 依赖于 schema 项目来使用 Protobuf 编译出来的实体类与服务。
   }
   ```

   使用 Spring Autoconfig 来配置组件。

   ```kotlin
   @Configuration
   @ComponentScan(basePackageClasses = [AutoConfig::class])
   class AutoConfig
   ```

   将自动配置加入到 `src/main/resources/META-INF/spring.factories` 中。

   ```properties
   org.springframework.boot.autoconfigure.EnableAutoConfiguration=com.bybutter.sisyphus.examples.helloworld.AutoConfig
   ```

   接下来实现我们的 API 业务逻辑。

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

5. **运行**

   Service 项目并不是一个可执行的 Jar 包，我们需要创建一个 application 项目来执行所有的 service 项目。这样设计的理由将会在 [设计理念] 中补充说明。

   这是一个示例 application 项目的 `build.gradle.kts` 配置：

   ```kotlin
   plugins {
       application
       kotlin("jvm") version "1.3.72"
       `kotlin-spring`
       id("com.bybutter.sisyphus.project") version "1.2.2"
   }
   
   dependencies {
       implementation("com.bybutter.sisyphus.starter:sisyphus-grpc-server-starter:1.2.2") // 依赖于 Sisyphus gRPC server starter
       implementation("com.bybutter.sisyphus.starter:sisyphus-grpc-transcoding-starter:1.2.2") // [可选] 添加 HTTP 与 gRPC 转码功能
       implementation("com.bybutter.sisyphus.starter:sisyphus-protobuf-type-server-starter:1.2.2") // [可选] 添加 typeserver 功能
       implementation(project("service:example-service")) 	// 依赖于 service 项目
   }
   ```

   接下来只需要在 application 项目中添加唯一需要的代码—— `main` 函数。

   ```kotlin
   @SpringBootApplication
   @EnableHttpToGrpcTranscoding
   class MarcoApplication
   
   fun main(args: Array<String>) {
       SpringApplication.run(MarcoApplication::class.java, *args)
   }
   ```

   就可以使用 `gradlew bootRun` 命令来提供我们的服务了。
