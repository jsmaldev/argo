package de.jsmal.core.createObject;

import lombok.Data;

@Data
public class CreateByParametersQuery {
    private String className;
    private String language;
    private String uuid;
//    private String b_key;
    private String jsonEncodedBase64Object;
}
