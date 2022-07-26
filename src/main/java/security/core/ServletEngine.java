package security.core;

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
}
