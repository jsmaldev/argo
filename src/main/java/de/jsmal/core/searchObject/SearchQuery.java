package de.jsmal.core.searchObject;

import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;

@Data
public class SearchQuery {
    private String table;
    private ArrayList<String> columns;
    private ArrayList<String> fields;
    private ArrayList<String> values;
    private HashMap<String, String> condition;
    private ArrayList<Integer> limit;
    private ArrayList<Integer> order;
    private boolean returnAllColumns;
}
