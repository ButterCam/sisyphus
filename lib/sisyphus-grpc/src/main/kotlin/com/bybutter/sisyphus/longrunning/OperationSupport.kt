package com.bybutter.sisyphus.longrunning

import com.bybutter.sisyphus.protobuf.primitives.Empty

interface OperationSupport {
    suspend fun supportOperationParent(operation: String): Boolean

    suspend fun supportOperation(operation: String): Boolean

    suspend fun listOperations(input: ListOperationsRequest): ListOperationsResponse

    suspend fun getOperation(input: GetOperationRequest): Operation

    suspend fun deleteOperation(input: DeleteOperationRequest): Empty

    suspend fun cancelOperation(input: CancelOperationRequest): Empty

    suspend fun waitOperation(input: WaitOperationRequest): Operation
}
