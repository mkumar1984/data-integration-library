// Copyright 2021 LinkedIn Corporation. All rights reserved.
// Licensed under the BSD-2 Clause license.
// See LICENSE in the project root for license information.

package com.linkedin.cdi.keys;

import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.linkedin.cdi.configuration.PropertyCollection.*;


/**
 * This structure holds static Source parameters that are commonly used in JDBC Sources.
 *
 * @author chrli
 */
public class JdbcKeys extends JobKeys {
  private static final Logger LOG = LoggerFactory.getLogger(JdbcKeys.class);
  private String jdbcStatement = null;
  private JsonObject initialParameterValues = new JsonObject();
  private String separator = MSTAGE_CSV_SEPARATOR.getDefaultValue();
  private String quoteCharacter = MSTAGE_CSV_QUOTE_CHARACTER.getDefaultValue();
  private String escapeCharacter = MSTAGE_CSV_ESCAPE_CHARACTER.getDefaultValue();
  private String schemaRefactorFunction = MSTAGE_JDBC_SCHEMA_REFACTOR.getDefaultValue();

  @Override
  public void logDebugAll() {
    super.logDebugAll();
    LOG.debug("These are values in JdbcSource");
    LOG.debug("JDBC statement: {}", jdbcStatement);
    LOG.debug("Initial values of dynamic parameters: {}", initialParameterValues);
  }

  public String getJdbcStatement() {
    return jdbcStatement;
  }

  public void setJdbcStatement(String jdbcStatement) {
    this.jdbcStatement = jdbcStatement;
  }

  public JsonObject getInitialParameterValues() {
    return initialParameterValues;
  }

  public void setInitialParameterValues(JsonObject initialParameterValues) {
    this.initialParameterValues = initialParameterValues;
  }

  public String getSeparator() {
    return separator;
  }

  public void setSeparator(String separator) {
    this.separator = separator;
  }

  public String getQuoteCharacter() {
    return quoteCharacter;
  }

  public void setQuoteCharacter(String quoteCharacter) {
    this.quoteCharacter = quoteCharacter;
  }

  public String getEscapeCharacter() {
    return escapeCharacter;
  }

  public void setEscapeCharacter(String escapeCharacter) {
    this.escapeCharacter = escapeCharacter;
  }

  public String getSchemaRefactorFunction() {
    return schemaRefactorFunction;
  }

  public void setSchemaRefactorFunction(String schemaRefactorFunction) {
    this.schemaRefactorFunction = schemaRefactorFunction;
  }
}
