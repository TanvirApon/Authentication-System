package com.substring.auth.auth_app_backend.config;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProjectConfig {

    /*
    Taken a Model Mapper Bean which is essential
    for Map between entity and DTO class
    Required or otherwise will get an error
     */
    @Bean
    public ModelMapper modelMapper(){
        return new ModelMapper();
    }
}
