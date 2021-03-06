/*
 * The Fascinator - Plugin - Transformer - JsonVelocity
 * Copyright (C) 2010-2011 University of Southern Queensland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package com.googlecode.fascinator.transformer.jsonVelocity;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import org.joda.time.DateTimeZone;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.googlecode.fascinator.api.PluginManager;
import com.googlecode.fascinator.api.storage.DigitalObject;
import com.googlecode.fascinator.api.storage.Storage;
import com.googlecode.fascinator.common.JsonSimple;
import com.googlecode.fascinator.common.JsonSimpleConfig;
import com.googlecode.fascinator.common.storage.StorageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests the JsonVelocityTransformer
 *
 * @author Linda Octalina
 */
public class JsonVelocityTransformerTest {
    private static Logger log = LoggerFactory.getLogger(JsonVelocityTransformerTest.class);

    private JsonVelocityTransformer jsonVelocityTransformer;

    private Util util;

    @SuppressWarnings("unused")
    private JsonSimpleConfig config;

    private JsonSimple tfpackage;

    private Storage ram;

    private DigitalObject sourceObject, outputObject;

    @Before
    public void init() throws Exception {
        util = new Util();
        tfpackage = new JsonSimple(new File(getClass().getResource(
                "/object.tfpackage").toURI()));
    }

    @After
    public void close() throws Exception {
        if (sourceObject != null) {
            sourceObject.close();
        }
        if (ram != null) {
            ram.shutdown();
        }
    }

    private void transform() throws Exception {
        // Storage
        ram = PluginManager.getStorage("file-system");
        ram.init("{}");

        File file = new File(getClass().getResource("/test-config.json")
                .toURI());
        config = new JsonSimpleConfig(file);
        jsonVelocityTransformer = new JsonVelocityTransformer();
        jsonVelocityTransformer.init(file);

        File source = new File(getClass().getResource("/object.tfpackage")
                .toURI());
        sourceObject = StorageUtils.storeFile(ram, source);
        outputObject = jsonVelocityTransformer.transform(sourceObject, "{}");
    }

    @Test
    public void transformFormat() throws Exception {
        transform();

        Set<String> payloadIdList = outputObject.getPayloadIdList();
        Assert.assertTrue(payloadIdList.contains("marc.xml"));
        Assert.assertTrue(payloadIdList.contains("oai_dc.xml"));
        Assert.assertTrue(payloadIdList.contains("rif_cs.xml"));
        Assert.assertTrue(payloadIdList.contains("vivo.xml"));
        Assert.assertEquals(payloadIdList.size(), 5);

        // For debugging
        //for (String payloadId : payloadIdList) {
        //    if (!payloadId.equals("object.tfpackage")) {
        //        Payload p = sourceObject.getPayload(payloadId);
        //        System.out.println(IOUtils.toString(p.open()));
        //        p.close();
        //    }
        //}

    }

    // Base object test
    @Test
    public void getSingleValue() throws IOException, URISyntaxException {
        Assert.assertEquals(tfpackage.getString(null, "dc:title"), "title1");
        Assert.assertEquals(tfpackage.getString(null, "dc:language"), "eng");
        Assert.assertEquals(tfpackage.getString(null, "dc:description"),
                "description");
    }

    // Utility test
    @Test
    public void getSubjectList() {
        Map<String, Object> subjectMap = util.getList(tfpackage, "dc:subject");
        Assert.assertEquals(3, subjectMap.size());

        // Keyword == 5
        Map<String, Object> keywords = (Map<String, Object>) subjectMap
                .get("keywords");
        Assert.assertEquals(2, keywords.size());
        Map<String, Object> keywords2 = util.getList(
                tfpackage, "dc:subject.keywords");
        Assert.assertEquals(2, keywords2.size());

        // anzsrc:for == 2
        Map<String, Object> anzsrcFor = (Map<String, Object>) subjectMap
                .get("anzsrc:for");
        Assert.assertEquals(2, anzsrcFor.size());
        Map<String, Object> anzsrcFor2 = util.getList(
                tfpackage, "dc:subject.anzsrc:for");
        Assert.assertEquals(1, anzsrcFor2.size());

        // anzsrc:seo == 2
        Map<String, Object> anzsrcSeo = (Map<String, Object>) subjectMap
                .get("anzsrc:seo");
        Assert.assertEquals(2, anzsrcSeo.size());
        Map<String, Object> anzsrcSeo2 = util.getList(
                tfpackage, "dc:subject.anzsrc:seo");
        Assert.assertEquals(1, anzsrcSeo2.size());
    }

    @Test
    public void testDate() {
        DateTimeZone.setDefault(DateTimeZone.forID("Australia/Brisbane"));
        DateTimeZone currentZone = DateTimeZone.getDefault();
        log.info("current zone is: " + currentZone);

        String dateInput = "2010";
        String result = util.getW3CDateTime(dateInput);
        String expected = "2010-01-01T00:00:00.000+10:00";
        Assert.assertEquals(expected, result);

        dateInput = "2010-10";
        result = util.getW3CDateTime(dateInput);
        expected = "2010-10-01T00:00:00.000+10:00";
        Assert.assertEquals(expected, result);

        dateInput = "2010-10-28";
        result = util.getW3CDateTime(dateInput);
        expected = "2010-10-28T00:00:00.000+10:00";
        Assert.assertEquals(expected, result);
    }

}
