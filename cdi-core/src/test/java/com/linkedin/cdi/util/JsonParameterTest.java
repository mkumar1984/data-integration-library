// Copyright 2021 LinkedIn Corporation. All rights reserved.
// Licensed under the BSD-2 Clause license.
// See LICENSE in the project root for license information.

package com.linkedin.cdi.util;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import gobblin.configuration.SourceState;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import org.apache.gobblin.configuration.State;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static com.linkedin.cdi.configuration.PropertyCollection.*;


/**
 * Unit test for {@link JsonParameter}
 * @author chrli
 *
 */
@Test(groups = {"org.apache.gobblin.util"})
public class JsonParameterTest {
  private Gson gson;
  private State state;

  @BeforeClass
  public void setup() {
    gson = new Gson();
    state = new State();
  }

  @Test
  public void testJsonArrayParameters() {

    String results = "{\"jsonarray_parameter\":[{\"jsonkey\":\"jsonvalue\"},{\"jsonkey\":\"jsonvalue\"}]}";

    JsonArray jsonArray = gson.fromJson(new InputStreamReader(this.getClass().getResourceAsStream("/util/parameter-jsonarray.json")), JsonArray.class);
    Assert.assertEquals(results, JsonParameter.getParametersAsJsonString(jsonArray.toString(), new JsonObject(), new State()));
  }

  @Test
  public void testParameterEncryption() {
    String expected = "{\"test-parameter\":\"password\"}";
    String encrypted = "ENC(M6nV+j0lhqZ36RgvuF5TQMyNvBtXmkPl)";
    String masterKeyLoc = this.getClass().getResource("/key/master.key").toString();
    SourceState state = new SourceState();
    state.setProp(ENCRYPT_KEY_LOC.toString(), masterKeyLoc);
    JsonArray jsonArray = gson.fromJson(new InputStreamReader(this.getClass().getResourceAsStream("/json/parameter-encryption.json")), JsonArray.class);
    Assert.assertEquals(expected, JsonParameter.getParametersAsJsonString(jsonArray.toString(), new JsonObject(), state));
  }

  @Test
  public void testJsonParameterConstructor() {
    JsonObject object = new JsonObject();
    JsonParameter jsonParameter = new JsonParameter(null, object, state);
    Assert.assertEquals(jsonParameter.getParametersAsJson(), object);
    Assert.assertEquals(jsonParameter.getParametersAsJsonString(), "{}");

    String parameterString = "[{\"type\":\"JSONOBJECT\",\"value\":\"testValue\",\"name\":\"testName\",\"value\":{\"valueType\":\"testValue\"}}]";
    jsonParameter = new JsonParameter(parameterString, object, state);
    Assert.assertEquals(jsonParameter.getParametersAsJsonString(), "{\"testName\":{\"valueType\":\"testValue\"}}");

    parameterString = "[{\"type\":\"WATERMARK\",\"name\":\"system\",\"value\":\"low\",\"format\":\"datetime\"}]";
    jsonParameter = new JsonParameter(parameterString, object, state);
    Assert.assertEquals(jsonParameter.getParametersAsJson(), object);

    parameterString = "[{\"type\":\"WATERMARK\",\"name\":\"system\",\"value\":\"low\",\"format\":\"datetime\",\"timezone\":\"America/Los_Angeles\"}]";
    jsonParameter = new JsonParameter(parameterString, gson.fromJson("{\"watermark\":{\"low\":-100,\"high\":1564642800}}", JsonObject.class), state);
    Assert.assertEquals(jsonParameter.getParametersAsJson(), object);

    jsonParameter = new JsonParameter(parameterString, gson.fromJson("{\"mark\":{\"low\":-100,\"high\":1564642800}}", JsonObject.class), state);
    Assert.assertEquals(jsonParameter.getParametersAsJson(), object);

    parameterString = "[{\"type\":\"SESSION\",\"name\":\"system\"}]";
    jsonParameter = new JsonParameter(parameterString, gson.fromJson("{\"session\": \"testSession\"}", JsonObject.class), state);
    Assert.assertEquals(jsonParameter.getParametersAsJsonString(), "{\"system\":\"testSession\"}");

    jsonParameter = new JsonParameter(parameterString, gson.fromJson("{\"no_session\":{\"name\": \"records.cursor\"}}", JsonObject.class), state);
    Assert.assertEquals(jsonParameter.getParametersAsJson(), object);

    parameterString = "[{\"type\":\"PAGESTART\",\"name\":\"page\"}]";
    jsonParameter = new JsonParameter(parameterString, gson.fromJson("{\"pagestart\": 10}", JsonObject.class), state);
    Assert.assertEquals(jsonParameter.getParametersAsJsonString(), "{\"page\":10}");

    jsonParameter = new JsonParameter(parameterString, gson.fromJson("{\"no_pagestart\":{\"name\": \"records.cursor\"}}", JsonObject.class), state);
    Assert.assertEquals(jsonParameter.getParametersAsJson(), object);

    parameterString = "[{\"type\":\"PAGENO\",\"name\":\"num\"}]";
    jsonParameter = new JsonParameter(parameterString, gson.fromJson("{\"pageno\": 9}", JsonObject.class), state);
    Assert.assertEquals(jsonParameter.getParametersAsJsonString(), "{\"num\":9}");

    jsonParameter = new JsonParameter(parameterString, gson.fromJson("{\"no_pageno\":{\"name\": \"records.cursor\"}}", JsonObject.class), state);
    Assert.assertEquals(jsonParameter.getParametersAsJson(), object);

    parameterString = "[{\"type\":\"PAGESIZE\",\"name\":\"num\"}]";
    jsonParameter = new JsonParameter(parameterString, gson.fromJson("{\"pagesize\": {\"name\": \"records.cursor\"}}", JsonObject.class), state);
    Assert.assertEquals(jsonParameter.getParametersAsJson(), object);

    jsonParameter = new JsonParameter(parameterString, gson.fromJson("{\"pagesize\":\"\"}", JsonObject.class), state);
    Assert.assertEquals(jsonParameter.getParametersAsJson(), object);
  }

  /**
   * Test JsonParameter constructor with invalid parameter
   * Expected: IllegalArgumentException
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testJsonParameterConstructorWithInvalidInput() {
    String parameterString = "[{\"type\":\"RANDOM\",\"name\":\"num\"}]";
    JsonObject values = gson.fromJson("{\"pagesize\": {\"name\": \"records.cursor\"}}", JsonObject.class);
    JsonParameter jsonParameter = new JsonParameter(parameterString, values, state);
    jsonParameter.getParametersAsJson();
  }

  /**
   * Test valueCheck when bRequirePrimitive is false, or bAllowBlank is true
   * @throws Exception
   */
  @Test
  public void testValueCheck() throws Exception {
    String parameterString = "[{\"type\":\"pagesize\",\"name\":\"num\"}]";
    JsonParameter jsonParameter = new JsonParameter(parameterString, gson.fromJson("{\"pagesize\":10}", JsonObject.class), new State());
    JsonObject values = gson.fromJson("{\"test\": \"testValue\"}", JsonObject.class);
    Method method = JsonParameter.class.getDeclaredMethod("valueCheck", JsonObject.class, String.class, boolean.class, boolean.class);
    method.setAccessible(true);
    Assert.assertTrue((Boolean) method.invoke(jsonParameter, values, "test", false, true));
  }

  /**
   * Test LIST type parameter with choices based on extract mode
   *
   * Scenario 1: Full load mode, value is an array with 2 elements
   * Input  1: full load mode, ms.paraemters = [{"name": "column", "value": ["createdDate", "updatedDate"]}]
   * Output 1: {"column":"createdDate"}
   *
   * Scenario 2:  Incremental load mode, values is an array with 2 elements
   * Input  2: incremental load mode, ms.paraemters = [{"name": "column", "value": ["createdDate", "updatedDate"]}]
   * Output 2: {"column":"createdDate"}
   *
   * Scenario 3: Incremental load mode, value is a primitive
   * Input  3: incremental load mode, ms.paraemters = [{"name": "column", "value": "createdDate"}]
   * Output 3: {"column":"createdDate"}
   *
   * Scenario 4: Incremental load mode, value is an array with 1 element
   * Input  4: incremental load mode, ms.paraemters = [{"name": "column", "value": ["createdDate"]}]
   * Output 4: {"column":"createdDate"}
   *
   * Scenario 5: Incremental load mode, value is an array with 0 element
   * Input  5: incremental load mode, ms.paraemters = [{"name": "column", "value": []}]
   * Output 5: {"column":""}
   */
  @Test
  public void testListParameterByExtractMode() {
    JsonArray msParameters = gson.fromJson("[{\"name\": \"column\", \"value\": [\"createdDate\", \"updatedDate\"]}]", JsonArray.class);
    JsonArray msParameters2 = gson.fromJson("[{\"name\": \"column\", \"value\": \"createdDate\"}]", JsonArray.class);
    JsonArray msParameters3 = gson.fromJson("[{\"name\": \"column\", \"value\": [\"createdDate\"]}]", JsonArray.class);
    JsonArray msParameters4 = gson.fromJson("[{\"name\": \"column\", \"value\": []}]", JsonArray.class);
    SourceState state = new SourceState();

    String expected = "{\"column\":\"createdDate\"}";
    state.setProp(EXTRACT_IS_FULL.toString(), true);
    Assert.assertEquals(expected, JsonParameter.getParametersAsJsonString(msParameters.toString(), new JsonObject(), state));

    expected = "{\"column\":\"updatedDate\"}";
    state.setProp(EXTRACT_IS_FULL.toString(), false);
    Assert.assertEquals(expected, JsonParameter.getParametersAsJsonString(msParameters.toString(), new JsonObject(), state));

    expected = "{\"column\":\"createdDate\"}";
    state.setProp(EXTRACT_IS_FULL.toString(), false);
    Assert.assertEquals(expected, JsonParameter.getParametersAsJsonString(msParameters2.toString(), new JsonObject(), state));

    expected = "{\"column\":\"createdDate\"}";
    state.setProp(EXTRACT_IS_FULL.toString(), false);
    Assert.assertEquals(expected, JsonParameter.getParametersAsJsonString(msParameters3.toString(), new JsonObject(), state));

    expected = "{\"column\":\"\"}";
    state.setProp(EXTRACT_IS_FULL.toString(), false);
    Assert.assertEquals(expected, JsonParameter.getParametersAsJsonString(msParameters4.toString(), new JsonObject(), state));
  }
}