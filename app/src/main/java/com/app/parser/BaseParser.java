package com.app.parser;

import org.json.JSONException;

/**
 * Created by user88 on 10/23/2015.
 */
public abstract class BaseParser {

    public abstract Object   parse(String response) throws JSONException;

}
