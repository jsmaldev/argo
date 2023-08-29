package de.jsmal;

import de.jsmal.core.ServletEngine;
import de.jsmal.core.engine.model.exeptions.InitDObjectIsNotFoundException;
import de.jsmal.core.engine.model.source.data.DataSource;
import de.jsmal.core.engine.model.source.data.MySqlDataSource;
import de.jsmal.core.engine.model.source.dictionary.DictionarySource;
import de.jsmal.core.engine.model.source.dictionary.InstanceDictionary;
import de.jsmal.core.engine.model.source.dictionary.LanguageDictionary;
import de.jsmal.core.engine.model.source.dictionary.MySqlDictionarySource;
import de.jsmal.core.engine.model.utils.Dictionary;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;


@Slf4j
@Configuration
public
class SpringBeansConfigTest {

    //-------------------------------------------
    @Bean
    DataSource dataSource(
            @Value("${spring.datasource.url}") String datasource_url,
            @Value("${spring.datasource.username}") String datasource_username,
            @Value("${spring.datasource.password}") String datasource_password
    ) {

        try {
            return new MySqlDataSource().initDataSource();
        } catch (InitDObjectIsNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Bean
    InstanceDictionary instanceDictionary (
            @Value("${spring.datasource.url}") String datasource_url,
            @Value("${spring.datasource.username}") String datasource_username,
            @Value("${spring.datasource.password}") String datasource_password
    ) {
        InstanceDictionary ret_instanceDictionary;
        //------DB PARAMETERS-------------
        try {
            ArrayList<String> parameters = new ArrayList<>(Arrays.asList(
                    datasource_url,
                    datasource_username,
                    datasource_password
            ));
            //------INIT DICTIONARY FROM DB----------
            DictionarySource dictionarySource = new MySqlDictionarySource().initDictionarySource(parameters);

            ret_instanceDictionary = Dictionary.getDictionaryFromMySqlSource(dictionarySource);
            ret_instanceDictionary.getDbIndexMap();
            return ret_instanceDictionary;

        } catch (InitDObjectIsNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Bean
    LanguageDictionary languageDictionary (){
        return new LanguageDictionary();
    }

    @Bean
    ServletEngine servletEngine (){
        return new ServletEngine("test","test","test");
    }

}