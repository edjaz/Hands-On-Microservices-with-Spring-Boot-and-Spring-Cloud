package fr.edjaz.api.composite.product

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import reactor.core.publisher.Mono

@Tag(description = "REST API for composite product information.", name = "ProductCompositeService")
interface ProductCompositeService {
    /**
     * Sample usage:
     *
     *
     * curl -X POST $HOST:$PORT/product-composite \
     * -H "Content-Type: application/json" --data \
     * '{"productId":123,"name":"product 123","weight":123}'
     *
     * @param body
     */
    @Operation(
        security = [SecurityRequirement(name = "bearer-key")],
        summary = "\${api.product-composite.create-composite-product.description}",
        description = "\${api.product-composite.create-composite-product.notes}"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "400",
                description = "Bad Request, invalid format of the request. See response message for more information."
            ), ApiResponse(
                responseCode = "422",
                description = "Unprocessable entity, input parameters caused the processing to fail. See response message for more information."
            )
        ]
    )
    @PostMapping(value = ["/product-composite"], consumes = ["application/json"])
    fun createCompositeProduct(@RequestBody body: ProductAggregate): Mono<Void>

    /**
     * Sample usage: curl $HOST:$PORT/product-composite/1
     *
     * @param productId
     * @return the composite product info, if found, else null
     */
    @Operation(
        security = [SecurityRequirement(name = "bearer-key")],
        summary = "\${api.product-composite.get-composite-product.description}",
        description = "\${api.product-composite.get-composite-product.notes}"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "400",
                description = "Bad Request, invalid format of the request. See response message for more information."
            ), ApiResponse(responseCode = "404", description = "Not found, the specified id does not exist."), ApiResponse(
                responseCode = "422",
                description = "Unprocessable entity, input parameters caused the processing to fail. See response message for more information."
            )
        ]
    )
    @GetMapping(value = ["/product-composite/{productId}"], produces = ["application/json"])
    fun getCompositeProduct(
        @PathVariable productId: Int,
        @RequestParam(value = "delay", required = false, defaultValue = "0") delay: Int,
        @RequestParam(value = "faultPercent", required = false, defaultValue = "0") faultPercent: Int
    ): Mono<ProductAggregate>

    /**
     * Sample usage:
     *
     *
     * curl -X DELETE $HOST:$PORT/product-composite/1
     *
     * @param productId
     */
    @Operation(
        security = [SecurityRequirement(name = "bearer-key")],
        summary = "\${api.product-composite.delete-composite-product.description}",
        description = "\${api.product-composite.delete-composite-product.notes}"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "400",
                description = "Bad Request, invalid format of the request. See response message for more information."
            ), ApiResponse(
                responseCode = "422",
                description = "Unprocessable entity, input parameters caused the processing to fail. See response message for more information."
            )
        ]
    )
    @DeleteMapping(value = ["/product-composite/{productId}"])
    fun deleteCompositeProduct(@PathVariable productId: Int): Mono<Void>
}
