package de.jsmal.core.searchObject;
import lombok.Data;

@Data
public class ViewQuery {
    private String table;
    private String viewName;
    private String userName;

}
