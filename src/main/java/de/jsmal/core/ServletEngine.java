package de.jsmal.core;

import de.jsmal.core.searchObject.SearchQuery;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import de.jsmal.core.engine.model.entity.CObject;
import de.jsmal.core.engine.model.exeptions.InitDObjectIsNotFoundException;
import de.jsmal.core.engine.model.source.data.DataSource;
import de.jsmal.core.engine.model.source.data.MySqlDataSource;
import de.jsmal.core.engine.model.source.dictionary.DictionarySource;
import de.jsmal.core.engine.model.source.dictionary.InstanceDictionary;
import de.jsmal.core.engine.model.source.dictionary.MySqlDictionarySource;
import de.jsmal.core.engine.model.utils.Dictionary;
import de.jsmal.core.engine.search.ResultSearchList;
import de.jsmal.core.engine.search.SearchEngine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

@Slf4j
@Service
public class ServletEngine {

    InstanceDictionary instanceDictionary;
    DataSource dataSource;
    HashMap<String, HashMap<String, HashMap<String,String>>> languageMap;

    // --- constructor ---
    public ServletEngine(
            @Value("${structure.version}") String structure_version,
            @Value("${de.jsmal.cruddao.version}") String cruddao_version,
            @Value("${servlet.app.version}") String app_version,
            @Value("${spring.datasource.url}") String datasource_url,
            @Value("${spring.datasource.username}") String datasource_username,
            @Value("${spring.datasource.password}") String datasource_password
    ) {
        DictionarySource dictionarySource = null;
        log.info("===== de.jsmal.structure.version: " + structure_version);
        log.info("===== de.jsmal.cruddao.version: " + cruddao_version);
        log.info("===== de.jsmal.servlet.app.version: " + app_version);

        //------DB PARAMETERS-------------
        try {
            ArrayList<String> parameters = new ArrayList<>(Arrays.asList(
//                "jdbc:mysql://localhost:3306/db_proto?useUnicode=true&allowPublicKeyRetrieval=true&serverTimezone=UTC&useSSL=false&useLegacyDatetimeCode=false",
//                "admuser",
//                "ThePassword"
                    datasource_url,
                    datasource_username,
                    datasource_password
            ));
            //------INIT DICTIONARY FROM DB----------
            dictionarySource = new MySqlDictionarySource().initDictionarySource(parameters);
        } catch (InitDObjectIsNotFoundException e) {
            log.error("ERROR by initialization DB parameters");
            e.printStackTrace();
        }

        this.instanceDictionary = Dictionary.getDictionaryFromMySqlSource(dictionarySource);
        log.info("=================================================");
        log.info("===== Instance Dictionary is initialised ========");
        instanceDictionary.getDbIndexMap();
        log.info("===== Indexes are initialised ===================");
        //====================================

        //-----INIT DATA SOURCE---------------------
        this.dataSource = null;
        try {
            dataSource = new MySqlDataSource().initDataSource();
            log.info("===== Data Source is initialised ================");
            log.info("=================================================");
        } catch (InitDObjectIsNotFoundException e) {
            log.error("ERROR by initialization Data Source");
            e.printStackTrace();
        }
        //--------------------------------
        //----Languages-------------------
        //-- EN -> Table(systranslate) -> FieldsName - Translation
        this.languageMap = new HashMap<>();
        // -- languageMap -> tableNameFieldTranslationMap -> filedNameTranslationMap

        //load all records from systranslation
        // languageMap = InitTranslation.getLanguagesMap(languageMap, this.instanceDictionary, this.dataSource);

//        log.info("===== Translation parameters: languages: "+languageMap.size());
//        log.info("===== Translation parameters: tables en: "+languageMap.get("en").size());
//        log.info("===== Translation parameters: tables en incident: "+languageMap.get("en").get("incident").size());

    }

    public String search(SearchQuery query){

        if (query==null) return new ResultSearchList("ERROR: Syntax of query has errors. NULL query").toJSON();
        String tableName = query.getTable();
        if (tableName==null || tableName.isEmpty()) return new ResultSearchList("ERROR: Syntax of query has errors. No table name").toJSON();
        CObject search_CObject = instanceDictionary.getNewCObjectByName(tableName);

        if(search_CObject==null) return new ResultSearchList("ERROR: Syntax of query has errors. No table in dictionary").toJSON();
        if(      query.getFields()==null||
                query.getValues()==null||
                query.getColumns()==null||
                query.getFields().size()!=query.getValues().size()
        ) return new ResultSearchList("ERROR: Syntax of query has errors. Incorrect parameters").toJSON();
        if(!query.isReturnAllColumns()&&query.getColumns().size()==0) return new ResultSearchList("ERROR: Syntax of query has errors. No columns").toJSON();


//--------CONDITIONS--------------------------------
        HashMap<String,String> condition = new HashMap<>();
        if (query.getCondition()!=null && query.getCondition().size()>0) {
            query.getCondition().forEach((key,value)->{
                log.info("in condition key = " + key + " value = " + value);
                condition.put(key,value);
            });
        }
//-------------------------------------------------------------------------------
        //----------INITIALIZING search_CObject from [fields] and [values]--------
        //-----------GET search_CObject from JSON--------------
        for (int i = 0; i < query.getFields().size(); i++) {

            String current_field_name = query.getFields().get(i);
            String current_field_value = query.getValues().get(i);
            if (!current_field_name.isEmpty()&&!search_CObject.updateAttributeStringValueByName (current_field_name, current_field_value)){
                return new ResultSearchList("ERROR: Syntax of search field parameter has error by prepare query object").toJSON();
            }
        }
        log.debug("search_object: "+search_CObject);

        //search_CObject.updateAttributeValueByName(field,new Integer(val));

        //search_CObject.updateAttributeValueByName("string_field","*test*");

        //condition.put("create_time",">");
        //condition.put("number","=");


        //log.info("---array for search : "+CObject.cObjectToStringsArraysForSearch(search_CObject,null));
        //--------------SEARCH---------------------------------

        ArrayList<ArrayList<Integer>> limitOrder = new ArrayList<>();
        limitOrder.add(query.getLimit());
        limitOrder.add(query.getOrder());

        //return search_CObject.toString()+"  condition: "+query.getCondition().toString();
        //boolean returnAllColumns = true;
        //System.out.println("query.isReturnAllColumns() = "+query.isReturnAllColumns());
//       System.out.println("search_CObject update_time = "+search_CObject.getUpdate_time());

        //------Preparation column parameters to SearchEngine -------
        ArrayList<String> columns_parameter = query.getColumns();
        for(String fields_value : query.getFields()) {
            if (!columns_parameter.contains(fields_value)) columns_parameter.add(fields_value);
        }

        log.debug("columns_parameter = " + columns_parameter);
        log.debug("isReturnAllColumns = " + query.isReturnAllColumns());
        log.debug("condition = " + condition);
        log.debug("limitOrder = " + limitOrder);

        ResultSearchList searchResultRecords = SearchEngine.searchRecordsByParameters(search_CObject, columns_parameter, query.isReturnAllColumns(),condition, limitOrder, dataSource, instanceDictionary);

        log.debug("searchResultRecords = "+searchResultRecords); //add here columns_parameter - to build view - can be null
        //log.debug("JSON: " + searchResultRecords.toJSON());
        return searchResultRecords.toJSON();
        //add language translation

    }
}
