// Copyright 2021 LinkedIn Corporation. All rights reserved.
// Licensed under the BSD-2 Clause license.
// See LICENSE in the project root for license information.

package com.linkedin.cdi.keys;

import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.linkedin.cdi.configuration.MultistageProperties;
import java.util.Iterator;
import java.util.List;
import org.apache.gobblin.configuration.State;
import org.apache.gobblin.source.workunit.WorkUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.linkedin.cdi.configuration.PropertyCollection.*;


/**
 * These attributes are defined and maintained in JsonExtractor
 *
 * @author chrli
 */
public class JsonExtractorKeys extends ExtractorKeys {
  private static final Logger LOG = LoggerFactory.getLogger(JsonExtractorKeys.class);
  final private static List<MultistageProperties> ESSENTIAL_PARAMETERS = Lists.newArrayList(
      MSTAGE_DATA_FIELD,
      MSTAGE_TOTAL_COUNT_FIELD);

  private Iterator<JsonElement> jsonElementIterator = null;
  private long totalCount;
  private long currentPageNumber = 0;
  private JsonObject pushDowns = new JsonObject();

  @Override
  public void logDebugAll(WorkUnit workUnit) {
    super.logDebugAll(workUnit);
    LOG.debug("These are values of JsonExtractor regarding to Work Unit: {}",
        workUnit == null ? "testing" : workUnit.getProp(DATASET_URN_KEY.toString()));
    LOG.debug("Total rows expected or processed: {}", totalCount);
  }

  @Override
  public void logUsage(State state) {
    super.logUsage(state);
    for (MultistageProperties p: ESSENTIAL_PARAMETERS) {
      LOG.info("Property {} ({}) has value {} ", p.toString(), p.getClassName(), p.get(state));
    }
  }

  public Iterator<JsonElement> getJsonElementIterator() {
    return jsonElementIterator;
  }

  public void setJsonElementIterator(Iterator<JsonElement> jsonElementIterator) {
    this.jsonElementIterator = jsonElementIterator;
  }

  public long getTotalCount() {
    return totalCount;
  }

  public void setTotalCount(long totalCount) {
    this.totalCount = totalCount;
  }

  public long getCurrentPageNumber() {
    return currentPageNumber;
  }

  public void setCurrentPageNumber(long currentPageNumber) {
    this.currentPageNumber = currentPageNumber;
  }

  public JsonObject getPushDowns() {
    return pushDowns;
  }

  public void setPushDowns(JsonObject pushDowns) {
    this.pushDowns = pushDowns;
  }
}
