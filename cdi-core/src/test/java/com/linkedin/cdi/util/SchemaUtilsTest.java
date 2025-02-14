// Copyright 2021 LinkedIn Corporation. All rights reserved.
// Licensed under the BSD-2 Clause license.
// See LICENSE in the project root for license information.

package com.linkedin.cdi.util;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import java.util.Arrays;
import java.util.List;
import org.testng.Assert;
import org.testng.annotations.Test;

import static com.linkedin.cdi.util.JsonUtils.*;


public class SchemaUtilsTest {

  @Test
  public void testIsValidOutputSchema() {
    // valid schema
    List<String> schemaColumns = Arrays.asList("a", "b");
    List<String> sourceColumns = Arrays.asList("a", "B", "C");
    Assert.assertTrue(SchemaUtils.isValidOutputSchema(schemaColumns, sourceColumns));

    // valid schema
    schemaColumns = Arrays.asList("a", "c");
    sourceColumns = Arrays.asList("a", "B", "C");
    Assert.assertTrue(SchemaUtils.isValidOutputSchema(schemaColumns, sourceColumns));

    // some columns in the schema is nowhere to be found in the source
    schemaColumns = Arrays.asList("a", "e");
    sourceColumns = Arrays.asList("a", "B", "C");
    Assert.assertFalse(SchemaUtils.isValidOutputSchema(schemaColumns, sourceColumns));

    // order mismatch
    schemaColumns = Arrays.asList("c", "a", "b");
    sourceColumns = Arrays.asList("a", "B", "C");
    Assert.assertFalse(SchemaUtils.isValidOutputSchema(schemaColumns, sourceColumns));
  }

  @Test
  public void testIsNullable() {
    String schema = "[{\"columnName\":\"token\",\"isNullable\":\"false\",\"dataType\":{\"type\":\"string\"}}]";
    JsonArray schemaArray = GSON.fromJson(schema, JsonArray.class);
    Assert.assertFalse(SchemaUtils.isNullable(Lists.newArrayList("token"), schemaArray));
    Assert.assertTrue(SchemaUtils.isNullable(Lists.newArrayList("token", "url"), schemaArray));

    schema = "[{\"columnName\":\"token\",\"isNullable\":\"false\",\"dataType\":{\"type\":\"record\",\"name\":\"token\",\"values\":[{\"columnName\":\"url\",\"isNullable\":\"false\",\"dataType\":{\"type\":\"string\"}}]}}]";
    schemaArray = GSON.fromJson(schema, JsonArray.class);
    Assert.assertFalse(SchemaUtils.isNullable(Lists.newArrayList("token"), schemaArray));
    Assert.assertFalse(SchemaUtils.isNullable(Lists.newArrayList("token", "url"), schemaArray));
    Assert.assertFalse(SchemaUtils.isNullable("token.url", schemaArray));

    schema = "[{\"columnName\":\"token\",\"isNullable\":\"false\",\"dataType\":{\"type\":\"record\",\"name\":\"token\",\"values\":[{\"columnName\":\"url\",\"isNullable\":\"true\",\"dataType\":{\"type\":\"string\"}}]}}]";
    schemaArray = GSON.fromJson(schema, JsonArray.class);
    Assert.assertFalse(SchemaUtils.isNullable(Lists.newArrayList("token"), schemaArray));
    Assert.assertTrue(SchemaUtils.isNullable(Lists.newArrayList("token", "url"), schemaArray));

    schema = "[{\"columnName\":\"token\",\"isNullable\":\"true\",\"dataType\":{\"type\":\"record\",\"name\":\"token\",\"values\":[{\"columnName\":\"url\",\"isNullable\":\"false\",\"dataType\":{\"type\":\"string\"}}]}}]";
    schemaArray = GSON.fromJson(schema, JsonArray.class);
    Assert.assertTrue(SchemaUtils.isNullable(Lists.newArrayList("token"), schemaArray));
    Assert.assertTrue(SchemaUtils.isNullable(Lists.newArrayList("token", "url"), schemaArray));

    schema = "[{\"columnName\":\"token\", \"dataType\":{\"type\":\"record\",\"name\":\"token\",\"values\":[{\"columnName\":\"url\",\"isNullable\":\"false\",\"dataType\":{\"type\":\"string\"}}]}}]";
    schemaArray = GSON.fromJson(schema, JsonArray.class);
    Assert.assertTrue(SchemaUtils.isNullable(Lists.newArrayList("token"), schemaArray));
    Assert.assertTrue(SchemaUtils.isNullable(Lists.newArrayList("token", "url"), schemaArray));

    schema = "[{\"columnName\":\"token\",\"isNullable\":\"false\",\"dataType\":{\"type\":\"record\",\"name\":\"token\",\"values\":[{\"columnName\":\"url\",\"dataType\":{\"type\":\"string\"}}]}}]";
    schemaArray = GSON.fromJson(schema, JsonArray.class);
    Assert.assertFalse(SchemaUtils.isNullable(Lists.newArrayList("token"), schemaArray));
    Assert.assertTrue(SchemaUtils.isNullable(Lists.newArrayList("token", "url"), schemaArray));

    schema = "[{\"columnName\":\"token\",\"isNullable\":\"false\",\"dataType\":{\"type\":\"array\",\"name\":\"token\",\"items\":{\"name\":\"contextItem\",\"dataType\":{\"name\":\"contextItem\",\"type\":\"record\",\"values\":[{\"columnName\":\"system\",\"isNullable\":\"false\",\"dataType\":{\"type\":\"string\"}}]}}}}]";
    schemaArray = GSON.fromJson(schema, JsonArray.class);
    Assert.assertFalse(SchemaUtils.isNullable(Lists.newArrayList("token"), schemaArray));

    // token.url not exist in schema
    Assert.assertTrue(SchemaUtils.isNullable(Lists.newArrayList("token", "url"), schemaArray));
    Assert.assertFalse(SchemaUtils.isNullable(Lists.newArrayList("token", "system"), schemaArray));

    schema = "[{\"columnName\":\"token\",\"isNullable\":\"false\",\"dataType\":{\"type\":\"array\",\"name\":\"token\",\"items\":{\"name\":\"contextItem\",\"dataType\":{\"name\":\"contextItem\",\"type\":\"record\",\"values\":[{\"columnName\":\"system\",\"isNullable\":\"true\",\"dataType\":{\"type\":\"string\"}}]}}}}]";
    schemaArray = GSON.fromJson(schema, JsonArray.class);
    Assert.assertTrue(SchemaUtils.isNullable(Lists.newArrayList("token", "system"), schemaArray));

  }
}