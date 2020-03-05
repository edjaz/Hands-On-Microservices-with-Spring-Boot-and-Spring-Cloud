rootProject.name = "handsOn"

include(":api")
include(":util")

include(":microservices:product:api")
include(":microservices:product:service")

include(":microservices:product-composite:api")
include(":microservices:product-composite:service")

include(":microservices:recommendation:api")
include(":microservices:recommendation:service")

include(":microservices:review:api")
include(":microservices:review:service")

include(":spring-cloud:eureka-server")
include(":spring-cloud:gateway")
include(":spring-cloud:authorization-server")
include(":spring-cloud:config-server")
