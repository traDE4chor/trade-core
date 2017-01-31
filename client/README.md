# Client Code Generation with Swagger and 'org.detoeuf.swagger-codegen' Gradle Plugin
The complete client code is generated automatically during the build process by Gradle and is located in 
'client/build/generated-sources/swagger/..'.

In order to enable the testing of the client and the invocation of the API through the client, the automatically 
generated test classes are used as a basis for implementation of integration tests located in the 'core' project module 
('core/src/integrationTests/..').

The resulting JAR contains all generated Java client classes and can be integrated into other systems to enable the 
communication with the TraDE middleware.