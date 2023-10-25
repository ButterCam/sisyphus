package com.bybutter.sisyphus.starter.jackson

import com.bybutter.sisyphus.starter.jackson.cbor.Jackson2CborCodecCustomizer
import com.bybutter.sisyphus.starter.jackson.json.Jackson2JsonCodecCustomizer
import com.bybutter.sisyphus.starter.jackson.smile.Jackson2SmileCodecCustomizer
import com.bybutter.sisyphus.starter.jackson.yaml.Jackson2YamlCodecCustomizer
import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import com.fasterxml.jackson.dataformat.smile.SmileFactory
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.http.codec.CodecsAutoConfiguration
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration
import org.springframework.boot.web.codec.CodecCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Import

@AutoConfiguration(
    before = [
        JacksonAutoConfiguration::class,
        CodecsAutoConfiguration::class,
    ],
)
@Import(JacksonAutoRegister::class)
@ComponentScan(basePackageClasses = [SisyphusJacksonAutoConfiguration::class])
class SisyphusJacksonAutoConfiguration {
    @Bean
    @ConditionalOnClass(CBORFactory::class)
    fun jackson2CborCodecCustomizer(): CodecCustomizer {
        return Jackson2CborCodecCustomizer()
    }

    @Bean
    @ConditionalOnClass(SmileFactory::class)
    fun jackson2SmileCodecCustomizer(): CodecCustomizer {
        return Jackson2SmileCodecCustomizer()
    }

    @Bean
    @ConditionalOnClass(YAMLFactory::class)
    fun jackson2YamlCodecCustomizer(): CodecCustomizer {
        return Jackson2YamlCodecCustomizer()
    }

    @Bean
    fun jackson2JsonCodecCustomizer(): CodecCustomizer {
        return Jackson2JsonCodecCustomizer()
    }
}
