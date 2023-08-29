package de.jsmal.core;

import de.jsmal.SpringBeansConfig;
import de.jsmal.SpringBeansConfigTest;
import de.jsmal.core.engine.model.source.data.DataSource;
import de.jsmal.core.engine.model.source.dictionary.InstanceDictionary;
import de.jsmal.core.engine.model.source.dictionary.LanguageDictionary;
import de.jsmal.core.searchObject.SearchQuery;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = { SpringBeansConfigTest.class})
class ServletEngineTest {
    @Autowired
    InstanceDictionary instanceDictionary;

    @Autowired
    DataSource dataSource;

    @Autowired
    LanguageDictionary languageDictionary;

    @Autowired
    ServletEngine servletEngine;

    @Test
    void search() {
        //ServletEngine servletEngine = new ServletEngine(instanceDictionary,dataSource,languageDictionary);

        System.out.println("InstanceDictionary Initial: " + instanceDictionary);
        System.out.println("servletEngine Initial: " + servletEngine);

        // searchObject is null
        servletEngine.search(null);

        // searchObject exist but empty
        SearchQuery searchQuery = new SearchQuery();
        servletEngine.search(searchQuery);

        searchQuery.setTable("incident");
        searchQuery.setReturnAllColumns(true);
        servletEngine.search(searchQuery);

        searchQuery.setLanguage("en");
        searchQuery.setColumns(new ArrayList<>(Arrays.asList("number","array_field")));
        searchQuery.setFields(new ArrayList<>(Arrays.asList("number","boolean_field")));
        searchQuery.setValues(new ArrayList<>(Arrays.asList("27", "false")));
        searchQuery.setLimit(new ArrayList<>(Arrays.asList(0, 5)));
        searchQuery.setOrder(new ArrayList<>(Arrays.asList(7, 0)));
        HashMap <String, String> condition = new HashMap<>();
        condition.put("number","<");
        searchQuery.setCondition(condition);
        searchQuery.setReturnAllColumns(false);
        String response = servletEngine.search(searchQuery);
        System.out.println("response: " + response);
    }
}