package de.jsmal.core;

import de.jsmal.core.createObject.CreateByParametersQuery;
import de.jsmal.core.engine.create.CreateEngine;
import de.jsmal.core.engine.create.ResultCreateRecord;
import de.jsmal.core.engine.model.source.dictionary.LanguageDictionary;
import de.jsmal.core.engine.update.ResultUpdateRecord;
import de.jsmal.core.engine.update.UpdateEngine;
import de.jsmal.core.searchObject.SearchQuery;
import de.jsmal.core.searchObject.ViewQuery;
import de.jsmal.core.updateObject.UpdateByUUIDQuery;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import de.jsmal.core.engine.model.entity.CObject;
import de.jsmal.core.engine.model.source.data.DataSource;
import de.jsmal.core.engine.model.source.dictionary.InstanceDictionary;
import de.jsmal.core.engine.search.ResultSearchList;
import de.jsmal.core.engine.search.SearchEngine;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
@Service
public class ServletEngine {

    @Autowired
    InstanceDictionary instanceDictionary;
    @Autowired
    DataSource dataSource;

    @Autowired
    LanguageDictionary languageDictionary;

    // --- test constructor ---
//    public ServletEngine(InstanceDictionary instanceDictionary, DataSource dataSource, LanguageDictionary languageDictionary){
//        this("1","1","1");
//        this.instanceDictionary = instanceDictionary;
//        this.dataSource = dataSource;
//        this.languageDictionary = languageDictionary;
//    }
    // --- constructor ---
    public ServletEngine(
            @Value("${structure.version}") String structure_version,
            @Value("${de.jsmal.cruddao.version}") String cruddao_version,
            @Value("${servlet.app.version}") String app_version
    ) {
        log.info("===== de.jsmal.structure.version: " + structure_version);
        log.info("===== de.jsmal.cruddao.version: " + cruddao_version);
        log.info("===== de.jsmal.servlet.app.version: " + app_version);

//        this.languageMap = new HashMap<>();
        // -- languageMap -> tableNameFieldTranslationMap -> filedNameTranslationMap

        //load all records from systranslation
//         languageMap = InitTranslation.getLanguagesMap(languageMap, this.instanceDictionary, this.dataSource);

//        log.info("===== Translation parameters: languages: "+languageMap.size());
//        log.info("===== Translation parameters: tables en: "+languageMap.get("en").size());
//        log.info("===== Translation parameters: tables en incident: "+languageMap.get("en").get("incident").size());

    }

    // internal function to get Objects in servlet
    public ResultSearchList searchList(SearchQuery query) {
        if (query==null) return new ResultSearchList("ERROR: Syntax of query has errors. NULL query", "en");
        String tableName = query.getTable();
        log.info("tableName: " + tableName);
        if (tableName==null || tableName.isEmpty()) return new ResultSearchList("ERROR: Syntax of query has errors. No table name", "en");
        log.info("instanceDictionary: " + instanceDictionary);
        CObject search_CObject = instanceDictionary.getNewCObjectByName(tableName);

        if(search_CObject==null) return new ResultSearchList("ERROR: Syntax of query has errors. No table in dictionary", "en");
        if(      query.getFields()==null||
                query.getValues()==null||
                query.getColumns()==null||
                query.getFields().size()!=query.getValues().size()
        ) return new ResultSearchList("ERROR: Syntax of query has errors. Incorrect parameters", "en");
        if(!query.isReturnAllColumns()&&query.getColumns().size()==0) return new ResultSearchList("ERROR: Syntax of query has errors. No columns","en");


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
                return new ResultSearchList("ERROR: Syntax of search field parameter has error by prepare query object","en");
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

        log.debug("language = " + query.getLanguage());
        log.debug("columns_parameter = " + columns_parameter);
        log.debug("isReturnAllColumns = " + query.isReturnAllColumns());
        log.debug("condition = " + condition);
        log.debug("limitOrder = " + limitOrder);

        ResultSearchList searchResultRecords = SearchEngine.searchRecordsByParameters(search_CObject, columns_parameter, query.isReturnAllColumns(),query.getLanguage(),condition, limitOrder, dataSource, instanceDictionary, null);

        log.debug("searchResultRecords = "+searchResultRecords); //add here columns_parameter - to build view - can be null
        //log.debug("JSON: " + searchResultRecords.toJSON());
        //here add from dictionary languages
        return searchResultRecords;
    }

    public ResultSearchList getView(ViewQuery query) {
        //errors -------------
        if (query==null) return new ResultSearchList("ERROR: Syntax of query has errors. NULL query", "en");
        if (query.getTable()==null || query.getTable().isEmpty()) return new ResultSearchList("ERROR: Syntax of query has errors. No table name", "en");

        String viewName = query.getViewName(); //query for this view
        String userName = query.getUserName();
        HashMap<String, String> condition = new HashMap<>();

        log.info("MAIN Query: " + query.toString());
        log.info("QUERY View viewName: " + viewName);
        log.info("QUERY View userName: " + query.getUserName());

        CObject search_CObject = instanceDictionary.getNewCObjectByName("sysview");
        if ((viewName==null || viewName.isEmpty() && (userName==null || userName.isEmpty())))
        {
            //get all records with empty user
            userName = "NULL";
        }


        search_CObject.updateAttributeStringValueByName ("table", "="+query.getTable());
        if (viewName != null && !viewName.isEmpty()){
            search_CObject.updateAttributeStringValueByName ("name", "="+viewName);
        }
        String strEqual = "=";
        if (userName.equals("NULL")) { strEqual = "";}
        search_CObject.updateAttributeStringValueByName ("user", strEqual + userName);

        // search_CObject.updateAttributeStringValueByName ("user", query.getUserName());
        // for user name  work but with errors!!!!
        log.info("QUERY View: " + search_CObject.toString() + " condition: " + condition.toString());
        ResultSearchList searchResultRecords = SearchEngine.searchRecordsByParameters(search_CObject, new ArrayList<String> (Arrays.asList("uuid","table","name", "user", "conf","default")), false,"en",condition, null, dataSource, instanceDictionary, null);
        if (searchResultRecords.getSize()<=0){
            search_CObject = instanceDictionary.getNewCObjectByName("sysview");
            search_CObject.updateAttributeStringValueByName ("table", "="+query.getTable());
            search_CObject.updateAttributeStringValueByName ("user", "NULL");
//            search_CObject.updateAttributeStringValueByName ("default", "true");
            condition = new HashMap<>();
            searchResultRecords = SearchEngine.searchRecordsByParameters(search_CObject, new ArrayList<String> (Arrays.asList("uuid","table","name", "user", "conf","default")), false,"en",condition, null, dataSource, instanceDictionary, null);
        }

return searchResultRecords;

    }
    // function to response in JSON as a servlet
    public String search(SearchQuery query){
        ResultSearchList result = this.searchList(query);
            log.info("return result: " + Base64.getEncoder().encodeToString(result.toJSON(languageDictionary, dataSource, instanceDictionary).getBytes(StandardCharsets.UTF_8)));
//            return Base64.getEncoder().encodeToString(result.toJSON(languageDictionary, dataSource, instanceDictionary).getBytes(StandardCharsets.UTF_8));
//        return result.toJSON();
        return result.toJSON(languageDictionary, dataSource, instanceDictionary);
    }

    public  String dbdict(){
        ArrayList <String> result = new ArrayList<>();
        for (Map.Entry<String, CObject> entry : instanceDictionary.getStructure().entrySet()) {
            result.add(entry.getKey());
        }
        JSONArray jsonArray = new JSONArray(result);
        return jsonArray.toString();
    }

    public String view(ViewQuery query){
        ResultSearchList result = this.getView(query);
        return result.toJSON(languageDictionary, dataSource, instanceDictionary);
    }

    public String viewUpdateByUUID (UpdateByUUIDQuery query){
        // ResultSearchList result = this.getView(query);
        ResultUpdateRecord resultUpdateRecord = new ResultUpdateRecord(
                query.getClassName(),
                query.getUuid(),
                query.getLanguage(),
                query.getJsonEncodedBase64Object()
        );
        ArrayList<String> columns = resultUpdateRecord.getColumns();
        HashMap<String,String> condition = new HashMap<>();
        ResultUpdateRecord result = UpdateEngine.updateRecordByUUID(
                resultUpdateRecord,
                resultUpdateRecord.getLanguage(),
                dataSource,
                instanceDictionary,
                null
        );
        return result.toJSON(languageDictionary, dataSource, instanceDictionary);
    }

    public String viewCreateByParam (CreateByParametersQuery query){
        ResultCreateRecord resultCreateRecord = new ResultCreateRecord(
                query.getClassName(),
                query.getLanguage(),
                query.getJsonEncodedBase64Object()
        );

        ResultCreateRecord result = CreateEngine.createRecordByParameters(
                resultCreateRecord,
                resultCreateRecord.getLanguage(),
                dataSource,
                instanceDictionary,
                null
        );

        return result.toJSON(languageDictionary, dataSource, instanceDictionary);
    }
}
