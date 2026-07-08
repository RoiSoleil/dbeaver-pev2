package org.eclipse.dbeaver_pev2.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.eclipse.dbeaver_pev2.PEV2File;
import org.eclipse.dbeaver_pev2.PEV2File.PEV2Content;
import org.junit.Test;

public class PEV2FileTest {

    @Test
    public void testRoundTrip() throws Exception {
        String sql = "SELECT * FROM person";
        String plan = "{\"Plan\": {\"Node Type\": \"Result\"}}";

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PEV2File.write(baos, sql, plan);

        String written = baos.toString("UTF-8");
        assertTrue(written.contains(sql));
        assertTrue(written.contains(plan));

        InputStream is = new ByteArrayInputStream(written.getBytes("UTF-8"));
        PEV2Content content = PEV2File.read(is);

        assertEquals(sql, content.sql().trim());
        assertEquals(plan, content.plan().trim());
    }

}
