package com.bybutter.sisyphus.starter.grpc.support.operation

import com.bybutter.sisyphus.longrunning.CancelOperationRequest
import com.bybutter.sisyphus.longrunning.DeleteOperationRequest
import com.bybutter.sisyphus.longrunning.GetOperationRequest
import com.bybutter.sisyphus.longrunning.ListOperationsRequest
import com.bybutter.sisyphus.longrunning.ListOperationsResponse
import com.bybutter.sisyphus.longrunning.Operation
import com.bybutter.sisyphus.longrunning.OperationSupport
import com.bybutter.sisyphus.longrunning.Operations
import com.bybutter.sisyphus.longrunning.WaitOperationRequest
import com.bybutter.sisyphus.protobuf.primitives.Empty
import com.bybutter.sisyphus.rpc.Code
import com.bybutter.sisyphus.rpc.StatusException

class Operations(private val supports: Iterable<OperationSupport>) : Operations() {
    override suspend fun listOperations(input: ListOperationsRequest): ListOperationsResponse {
        for (repository in supports) {
            if (!repository.operationName.matches("${input.name}/operations/-")) continue

            return repository.listOperations(input)
        }

        return ListOperationsResponse {
        }
    }

    override suspend fun getOperation(input: GetOperationRequest): Operation {
        for (repository in supports) {
            if (!repository.operationName.matches(input.name)) continue

            return repository.getOperation(input)
        }

        throw StatusException(Code.NOT_FOUND, "Operation '${input.name}' not found")
    }

    override suspend fun deleteOperation(input: DeleteOperationRequest): Empty {
        for (repository in supports) {
            if (!repository.operationName.matches(input.name)) continue

            return repository.deleteOperation(input)
        }

        throw StatusException(Code.NOT_FOUND, "Operation '${input.name}' not found")
    }

    override suspend fun cancelOperation(input: CancelOperationRequest): Empty {
        for (repository in supports) {
            if (!repository.operationName.matches(input.name)) continue

            return repository.cancelOperation(input)
        }

        throw StatusException(Code.NOT_FOUND, "Operation '${input.name}' not found")
    }

    override suspend fun waitOperation(input: WaitOperationRequest): Operation {
        for (repository in supports) {
            if (!repository.operationName.matches(input.name)) continue

            return repository.waitOperation(input)
        }

        throw StatusException(Code.NOT_FOUND, "Operation '${input.name}' not found")
    }
}
