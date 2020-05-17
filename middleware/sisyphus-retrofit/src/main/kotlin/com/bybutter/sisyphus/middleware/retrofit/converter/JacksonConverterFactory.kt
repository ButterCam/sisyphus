package com.bybutter.sisyphus.middleware.retrofit.converter

import com.bybutter.sisyphus.jackson.Json
import java.lang.reflect.Type
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit

class JacksonConverterFactory : Converter.Factory() {
    override fun responseBodyConverter(
        type: Type,
        annotations: Array<Annotation>,
        retrofit: Retrofit
    ): Converter<ResponseBody, *> {
        return JacksonResponseBodyConverter<Any>(type)
    }

    override fun requestBodyConverter(
        type: Type,
        parameterAnnotations: Array<Annotation>,
        methodAnnotations: Array<Annotation>,
        retrofit: Retrofit
    ): Converter<*, RequestBody> {
        return JacksonRequestBodyConverter
    }
}

class JacksonResponseBodyConverter<T>(private val type: Type) : Converter<ResponseBody, T> {
    override fun convert(value: ResponseBody): T? {
        value.use {
            return Json.deserialize(it.byteStream(), type)
        }
    }
}

object JacksonRequestBodyConverter : Converter<Any?, RequestBody> {
    override fun convert(value: Any?): RequestBody {
        value ?: return "".toRequestBody()
        return Json.serialize(value).toRequestBody("application/json".toMediaType())
    }
}
