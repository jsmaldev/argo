package de.jsmal.core.updateObject;

import lombok.Data;

@Data
public class UpdateByUUIDQuery {
    private String className;
    private String language;
    private String uuid;
    private String b_key;
    private String jsonEncodedBase64Object;
}
