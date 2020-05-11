/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.pinot.spi.utils;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import org.apache.commons.io.IOUtils;
import org.apache.pinot.spi.ingestion.batch.spec.SegmentGenerationJobSpec;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.yaml.snakeyaml.Yaml;


public class JinjaTemplateUtilsTest {

  @Test
  public void testDefaultRenderTemplate() {
    Date today = new Date(Instant.now().toEpochMilli());
    Date yesterday = new Date(Instant.now().minus(1, ChronoUnit.DAYS).toEpochMilli());
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    Assert.assertEquals(JinjaTemplateUtils.renderTemplate("{{ today }}"), dateFormat.format(today));
    Assert.assertEquals(JinjaTemplateUtils.renderTemplate("{{ yesterday }}"), dateFormat.format(yesterday));
  }

  @Test
  public void testRenderTemplateWithGivenContextMap() {
    Map<String, Object> contextMap = new HashMap<>();
    contextMap.put("first_date_2020", "2020-01-01");
    contextMap.put("name", "xiang");
    contextMap.put("ts", 1577836800);
    contextMap.put("yyyy", "2020");
    contextMap.put("YYYY", "1919");
    contextMap.put("MM", "05");
    contextMap.put("dd", "06");
    Assert.assertEquals(JinjaTemplateUtils.renderTemplate("{{ first_date_2020 }}", contextMap), "2020-01-01");
    Assert.assertEquals(JinjaTemplateUtils.renderTemplate("{{first_date_2020}}", contextMap), "2020-01-01");
    Assert.assertEquals(JinjaTemplateUtils.renderTemplate("{{ name }}", contextMap), "xiang");
    Assert.assertEquals(JinjaTemplateUtils.renderTemplate("{{ name|upper }}", contextMap), "XIANG");
    Assert.assertEquals(JinjaTemplateUtils.renderTemplate("{{ ts }}", contextMap), "1577836800");
    Assert.assertEquals(JinjaTemplateUtils.renderTemplate("/var/rawdata/{{ yyyy }}/{{ MM }}/{{ dd }}", contextMap),
        "/var/rawdata/2020/05/06");
    Assert.assertEquals(JinjaTemplateUtils.renderTemplate("/var/rawdata/{{yyyy}}/{{MM}}/{{dd}}", contextMap),
        "/var/rawdata/2020/05/06");
    Assert.assertEquals(JinjaTemplateUtils.renderTemplate("/var/rawdata/{{YYYY}}/{{MM}}/{{dd}}", contextMap),
        "/var/rawdata/1919/05/06");
  }

  @Test
  public void testIngestionJobTemplate()
      throws IOException {
    InputStream resourceAsStream =
        JinjaTemplateUtils.class.getClassLoader().getResourceAsStream("ingestionJobSpecTemplate.yaml");
    String yamlTemplate = IOUtils.toString(resourceAsStream);
    Map<String, Object> context =
        JinjaTemplateUtils.getTemplateContext(Arrays.asList("year=2020", "month=05", "day=06"));
    String yamlStr = JinjaTemplateUtils.renderTemplate(yamlTemplate, context);
    SegmentGenerationJobSpec spec = new Yaml().loadAs(yamlStr, SegmentGenerationJobSpec.class);
    Assert.assertEquals(spec.getInputDirURI(), "file:///path/to/input/2020/05/06");
    Assert.assertEquals(spec.getOutputDirURI(), "file:///path/to/output/2020/05/06");
  }
}