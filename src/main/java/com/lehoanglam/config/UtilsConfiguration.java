package com.yes4all.config;

import java.util.Objects;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UtilsConfiguration {

    public ModelMapper modelMapper;

    @Bean
    public ModelMapper modelMapper() {
        if (Objects.isNull(modelMapper)) {
            modelMapper = new ModelMapper();
            modelMapper
                .getConfiguration()
                .setSkipNullEnabled(true)
                .setMatchingStrategy(MatchingStrategies.STRICT)
                .setAmbiguityIgnored(true);
        }
        return modelMapper;
    }
}
