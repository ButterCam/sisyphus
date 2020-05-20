# Sisyphus  

[![Maven Central](https://img.shields.io/maven-central/v/com.bybutter.sisyphus/sisyphus-bom)](https://mvnrepository.com/artifact/com.bybutter.sisyphus/sisyphus-bom) [![Dependencies](https://img.shields.io/librariesio/release/maven/com.bybutter.sisyphus:sisyphus-dependencies)](https://libraries.io/maven/com.bybutter.sisyphus:sisyphus-dependencies)

Sisyphus is the way how we provide backend services. It integrates all tools and libraries needed for designing API which follows the [Google API Improvement Proposals](https://aip.bybutter.com).

## We are rolling a huge boulder

Due to analyzing product documents completely, it is not particularly difficult to write an exquisite and easy-to-use API at the beginning for most APIs.

However, many people will break the initial design of the API in the endless updates of products.

It's hard to create a strong and extensible API in the whole project lifetime, just like rolling a huge boulder endlessly up a steep hill.

So we need an all-encompassing guide book to guide us in creating, updating, and modifying APIs.  

The [Google API Improvement Proposals](https://aip.bybutter.com) is the all-encompassing guide book. Google created it in their rich and extensive API design experience. It laid the foundation for anyone to create an extensible API.

## Good tools can help you

Choosing good tools can help you 'rolling a huge boulder' faster and easier. Sisyphus provides and integrates many tools in your 'boulder rolling' route.

[**Kotlin**](https://kotlinlang.org/) is our target language. The mature JVM community and concise grammar are the reasons.

[**Spring boot**](https://spring.io/projects/spring-boot) is our old friend to manage and organize our components.

[**gRPC**](https://grpc.io/) is our target API framework. Sisyphus also provides the [HTTP and gRPC Transcoding](https://aip.bybutter.com/127) component for the environment which isn't compatible with gRPC.

[**Sisyphus Protobuf**](/lib/sisyphus-protobuf) is our customized protobuf runtime, which designed for Kotlin.

[**Sisyphus gRPC**](/lib/sisyphus-grpc) is our customized gRPC runtime, which designed for Kotlin coroutine.

[**Sisyphus DTO**](/lib/sisyphus-dto) is the way how we create struct without protobuf.

[**Sisyphus Middleware**](/middleware) is the way how we connect Sisyphus and other systems.

[**Sisyphus Configuration Artifact**](/middleware/sisyphus-configuration-artifact) is the way how we manage configurations and developing environment.

[**Sisyphus Protobuf Compiler**](/tools/sisyphus-protoc) is the way how we generate Kotlin codes by `.proto` files.

[**Sisyphus Project Plugin**](/tools/sisyphus-project-gradle-plugin) is the way how we manage project and configuring Gradle.

[**Sisyphus Protobuf Plugin**](/tools/sisyphus-protobuf-gradle-plugin) is the way how we generate code by `.proto` files in Gradle.

**And More** tools like [CEL(Common Expression Language)](https://github.com/google/cel-spec), [Filtering](https://aip.bybutter.com/160) and [Ordering](https://aip.bybutter.com/132#ordering) Script will help you to design APIs follow Google AIP.

## Rolling with Sisyphus

Ready to rolling boulder with Sisyphus yet? Hold on! We need to plan our route first.

1. **Configure Sisyphus with gradle.properties**

   We use [gradle.properties](https://docs.gradle.org/current/userguide/build_environment.html#sec:gradle_configuration_properties) to configure global settings of Sisyphus and all Sisyphus Projects.

   ```properties
   # Set developer name for developing environment. If be set
   sisyphus.developer=higan
   
   # Use 'sisyphus.repository.<name>.url' Register maven repository for Sisyphus usage.
   # Url of repository named 'snapshot'.
   sisyphus.repositories.snapshot.url=https://repo1.maven.org/maven2/
   # Optional, user name of repository.
   sisyphus.repositories.snapshot.username=
   # Optional, password of repository.
   sisyphus.repositories.snapshot.password=
   
   # Repositories for different usage, there are 4 embeded repositories.
   # 'local'(maven local), 'central'(maven central), 'jcenter', 'portal'(gradle portal).
   
   # Repositories for resolving dependencies, default value is 'local,central,jcenter,portal'.
   sisyphus.dependency.repositories=local,central,jcenter,portal
   # Repositories for resolving configuration artifact, default value is 'local,central,jcenter,portal'. It will be used in next chapters.
   sisyphus.config.repositories=local,central,jcenter,portal
   # Repositories for snapshot publishing, default value is 'snapshot'.
   sisyphus.snapshot.repositories=snapshot
   # Repositories for release publishing, default value is 'release'.
   sisyphus.release.repositories=release
   
   # Configuration artifacts, it will be resolved in runtime.
   sisyphus.config.artifacts=com.bybutter.config:incubator:higan-SNAPSHOT
   ```

   `gradle.properties` are shared between Gradle and Spring. Sisyphus Project Plugin will load it and configure Gradle, Sisyphus Configuration Artifact will load it for Spring Framework.

2. **Write protobuf schema**