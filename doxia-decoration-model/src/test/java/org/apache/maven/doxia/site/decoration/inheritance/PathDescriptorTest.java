package org.apache.maven.doxia.site.decoration.inheritance;

/*
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

import java.io.File;

import org.codehaus.plexus.util.StringUtils;

import junit.framework.TestCase;

/**
 * Test the PathDescriptor creation under various circumstances.
 *
 * @author <a href="mailto:henning@apache.org">Henning P. Schmiedehausen</a>
 * @version $Id$
 */
public class PathDescriptorTest
    extends TestCase
{
    public void testAbsPath()
        throws Exception
    {
        String path = "absolutePath";

        PathDescriptor desc = new PathDescriptor( "/" + path );

        assertTrue( desc.isFile() );
        assertTrue( desc.isRelative() );
        assertNull( desc.getBaseUrl() );
        assertNull( desc.getPathUrl() );
        assertNotNull( desc.getPath() );
        assertNotNull( desc.getLocation() );
        assertEquals( "wrong path", path, desc.getPath() );
        assertEquals( "wrong location", path, desc.getLocation() );
    }

    public void testRelPath()
        throws Exception
    {
        String path = "relativePath";

        PathDescriptor desc = new PathDescriptor( path );

        assertTrue( desc.isFile() );
        assertTrue( desc.isRelative() );
        assertNull( desc.getBaseUrl() );
        assertNull( desc.getPathUrl() );
        assertNotNull( desc.getPath() );
        assertNotNull( desc.getLocation() );
        assertEquals( "wrong path", path, desc.getPath() );
        assertEquals( "wrong location", path, desc.getLocation() );
    }

    public void testEmptyAbsPath()
        throws Exception
    {
        String path = "";

        PathDescriptor desc = new PathDescriptor( "/" + path );

        assertTrue( desc.isFile() );
        assertTrue( desc.isRelative() );
        assertNull( desc.getBaseUrl() );
        assertNull( desc.getPathUrl() );
        assertNotNull( desc.getPath() );
        assertNotNull( desc.getLocation() );
        assertEquals( "wrong path", path, desc.getPath() );
        assertEquals( "wrong location", path, desc.getLocation() );
    }

    public void testEmptyRelPath()
        throws Exception
    {
        String path = "";

        PathDescriptor desc = new PathDescriptor( path );

        assertTrue( desc.isFile() );
        assertTrue( desc.isRelative() );
        assertNull( desc.getBaseUrl() );
        assertNull( desc.getPathUrl() );
        assertNotNull( desc.getPath() );
        assertNotNull( desc.getLocation() );
        assertEquals( "wrong path", path, desc.getPath() );
        assertEquals( "wrong location", path, desc.getLocation() );
    }

    public void testNullPath()
        throws Exception
    {
        String path = null;

        PathDescriptor desc = new PathDescriptor( path );

        assertTrue( desc.isFile() );
        assertTrue( desc.isRelative() );
        assertNull( desc.getBaseUrl() );
        assertNull( desc.getPathUrl() );
        assertNull( desc.getPath() );
        assertNull( desc.getLocation() );
        assertEquals( "wrong path", path, desc.getPath() );
        assertEquals( "wrong location", path, desc.getLocation() );
    }

    public void testNullBaseAbsPath()
        throws Exception
    {
        String base = null;
        String path = "absolutePath";

        PathDescriptor desc = new PathDescriptor( base, "/" + path );

        assertTrue( desc.isFile() );
        assertTrue( desc.isRelative() );
        assertNull( desc.getBaseUrl() );
        assertNull( desc.getPathUrl() );
        assertNotNull( desc.getPath() );
        assertNotNull( desc.getLocation() );
        assertEquals( "wrong path", path, desc.getPath() );
        assertEquals( "wrong location", path, desc.getLocation() );
    }

    public void testNullBaseRelPath()
        throws Exception
    {
        String base = null;
        String path = "relativePath";

        PathDescriptor desc = new PathDescriptor( base, path );

        assertTrue( desc.isFile() );
        assertTrue( desc.isRelative() );
        assertNull( desc.getBaseUrl() );
        assertNull( desc.getPathUrl() );
        assertNotNull( desc.getPath() );
        assertNotNull( desc.getLocation() );
        assertEquals( "wrong path", path, desc.getPath() );
        assertEquals( "wrong location", path, desc.getLocation() );
    }

    public void testNullBaseEmptyAbsPath()
        throws Exception
    {
        String base = null;
        String path = "";

        PathDescriptor desc = new PathDescriptor( base, "/" + path );

        assertTrue( desc.isFile() );
        assertTrue( desc.isRelative() );
        assertNull( desc.getBaseUrl() );
        assertNull( desc.getPathUrl() );
        assertNotNull( desc.getPath() );
        assertNotNull( desc.getLocation() );
        assertEquals( "wrong path", path, desc.getPath() );
        assertEquals( "wrong location", path, desc.getLocation() );
    }

    public void testNullBaseEmptyRelPath()
        throws Exception
    {
        String base = null;
        String path = "";

        PathDescriptor desc = new PathDescriptor( base, path );

        assertTrue( desc.isFile() );
        assertTrue( desc.isRelative() );
        assertNull( desc.getBaseUrl() );
        assertNull( desc.getPathUrl() );
        assertNotNull( desc.getPath() );
        assertNotNull( desc.getLocation() );
        assertEquals( "wrong path", path, desc.getPath() );
        assertEquals( "wrong location", path, desc.getLocation() );
    }

    public void testNullBaseNullPath()
        throws Exception
    {
        String base = null;
        String path = null;

        PathDescriptor desc = new PathDescriptor( base, path );

        assertTrue( desc.isFile() );
        assertTrue( desc.isRelative() );
        assertNull( desc.getBaseUrl() );
        assertNull( desc.getPathUrl() );
        assertNull( desc.getPath() );
        assertNull( desc.getLocation() );
        assertEquals( "wrong path", path, desc.getPath() );
        assertEquals( "wrong location", path, desc.getLocation() );
    }

    public void testUrlBaseAbsPath()
        throws Exception
    {
        String base = "http://maven.apache.org/";
        String path = "absolutePath";

        PathDescriptor desc = new PathDescriptor( base, "/" + path );

        assertFalse( desc.isFile() );
        assertFalse( desc.isRelative() );
        assertNotNull( desc.getBaseUrl() );
        assertNotNull( desc.getPathUrl() );
        assertNotNull( desc.getPath() );
        assertNotNull( desc.getLocation() );
        assertEquals( "wrong path", "/" + path, desc.getPath() );
        assertEquals( "wrong location", base + path, desc.getLocation() );
    }

    public void testUrlBaseRelPath()
        throws Exception
    {
        String base = "http://maven.apache.org/";
        String path = "relativePath";

        PathDescriptor desc = new PathDescriptor( base, path );

        assertFalse( desc.isFile() );
        assertFalse( desc.isRelative() );
        assertNotNull( desc.getBaseUrl() );
        assertNotNull( desc.getPathUrl() );
        assertNotNull( desc.getPath() );
        assertNotNull( desc.getLocation() );
        assertEquals( "wrong path", "/" + path, desc.getPath() );
        assertEquals( "wrong location", base + path, desc.getLocation() );
    }

    public void testUrlBaseEmptyAbsPath()
        throws Exception
    {
        String base = "http://maven.apache.org/";
        String path = "";

        PathDescriptor desc = new PathDescriptor( base, "/" + path );

        assertFalse( desc.isFile() );
        assertFalse( desc.isRelative() );
        assertNotNull( desc.getBaseUrl() );
        assertNotNull( desc.getPathUrl() );
        assertNotNull( desc.getPath() );
        assertNotNull( desc.getLocation() );
        assertEquals( "wrong path", "/" + path, desc.getPath() );
        assertEquals( "wrong location", base + path, desc.getLocation() );
    }

    public void testUrlBaseEmptyRelPath()
        throws Exception
    {
        String base = "http://maven.apache.org/";
        String path = "";

        PathDescriptor desc = new PathDescriptor( base, path );

        assertFalse( desc.isFile() );
        assertFalse( desc.isRelative() );
        assertNotNull( desc.getBaseUrl() );
        assertNotNull( desc.getPathUrl() );
        assertNotNull( desc.getPath() );
        assertNotNull( desc.getLocation() );
        assertEquals( "wrong path", "/" + path, desc.getPath() );
        assertEquals( "wrong location", base + path, desc.getLocation() );
    }

    public void testUrlBaseNullPath()
        throws Exception
    {
        String base = "http://maven.apache.org/";
        String path = null;

        PathDescriptor desc = new PathDescriptor( base, path );

        assertFalse( desc.isFile() );
        assertFalse( desc.isRelative() );
        assertNotNull( desc.getBaseUrl() );
        assertNotNull( desc.getPathUrl() );
        assertNotNull( desc.getPath() );
        assertNotNull( desc.getLocation() );
        assertEquals( "wrong path", "/", desc.getPath() );
        assertEquals( "wrong location", base, desc.getLocation() );
    }

    public void testFileBaseAbsPath()
        throws Exception
    {
        String base = "/tmp/foo";
        String path = "absolutePath";

        PathDescriptor desc = new PathDescriptor( "file://" + base, "/" + path );

        assertTrue( desc.isFile() );
        assertFalse( desc.isRelative() );
        assertNotNull( desc.getBaseUrl() );
        assertNotNull( desc.getPathUrl() );
        assertNotNull( desc.getPath() );
        assertNotNull( desc.getLocation() );
        if ( System.getProperty( "os.name" ).toLowerCase().indexOf( "windows" ) != -1 )
        {
            String s = StringUtils.replace( new File( base + "/" + path ).toURL().toString(), "file:", "" );
            assertEquals( "wrong path", s, desc.getPath() );
            assertEquals( "wrong location", s, desc.getLocation() );
        }
        else
        {
            assertEquals( "wrong path", base + "/" + path, desc.getPath() );
            assertEquals( "wrong location", base + "/" + path, desc.getLocation() );
        }
    }

    public void testFileBaseRelPath()
        throws Exception
    {
        String base = "/tmp/foo";
        String path = "relativePath";

        PathDescriptor desc = new PathDescriptor( "file://" + base, path );

        assertTrue( desc.isFile() );
        assertFalse( desc.isRelative() );
        assertNotNull( desc.getBaseUrl() );
        assertNotNull( desc.getPathUrl() );
        assertNotNull( desc.getPath() );
        assertNotNull( desc.getLocation() );
        if ( System.getProperty( "os.name" ).toLowerCase().indexOf( "windows" ) != -1 )
        {
            String s = StringUtils.replace( new File( base + "/" + path ).toURL().toString(), "file:", "" );
            assertEquals( "wrong path", s, desc.getPath() );
            assertEquals( "wrong location", s, desc.getLocation() );
        }
        else
        {
            assertEquals( "wrong path", base + "/" + path, desc.getPath() );
            assertEquals( "wrong location", base + "/" + path, desc.getLocation() );
        }
    }

    public void testFileBaseEmptyAbsPath()
        throws Exception
    {
        String base = "/tmp/foo";
        String path = "";

        PathDescriptor desc = new PathDescriptor( "file://" + base, "/" + path );

        assertTrue( desc.isFile() );
        assertFalse( desc.isRelative() );
        assertNotNull( desc.getBaseUrl() );
        assertNotNull( desc.getPathUrl() );
        assertNotNull( desc.getPath() );
        assertNotNull( desc.getLocation() );
        if ( System.getProperty( "os.name" ).toLowerCase().indexOf( "windows" ) != -1 )
        {
            String s = StringUtils.replace( new File( base ).toURL().toString(), "file:", "" );
            assertEquals( "wrong path", s, desc.getPath() );
            assertEquals( "wrong location", s, desc.getLocation() );
        }
        else
        {
            assertEquals( "wrong path", base, desc.getPath() );
            assertEquals( "wrong location", base, desc.getLocation() );
        }
    }

    public void testFileBaseEmptyRelPath()
        throws Exception
    {
        String base = "/tmp/foo";
        String path = "";

        PathDescriptor desc = new PathDescriptor( "file://" + base, path );

        assertTrue( desc.isFile() );
        assertFalse( desc.isRelative() );
        assertNotNull( desc.getBaseUrl() );
        assertNotNull( desc.getPathUrl() );
        assertNotNull( desc.getPath() );
        assertNotNull( desc.getLocation() );
        if ( System.getProperty( "os.name" ).toLowerCase().indexOf( "windows" ) != -1 )
        {
            String s = StringUtils.replace( new File( base ).toURL().toString(), "file:", "" );
            assertEquals( "wrong path", s, desc.getPath() );
            assertEquals( "wrong location", s, desc.getLocation() );
        }
        else
        {
            assertEquals( "wrong path", base, desc.getPath() );
            assertEquals( "wrong location", base, desc.getLocation() );
        }
    }

    public void testFileBaseNullPath()
        throws Exception
    {
        String base = "/tmp/foo";
        String path = null;

        PathDescriptor desc = new PathDescriptor( "file://" + base, path );

        assertTrue( desc.isFile() );
        assertFalse( desc.isRelative() );
        assertNotNull( desc.getBaseUrl() );
        assertNotNull( desc.getPathUrl() );
        assertNotNull( desc.getPath() );
        assertNotNull( desc.getLocation() );
        assertEquals( "wrong path", base, desc.getPath() );
        assertEquals( "wrong location", base, desc.getLocation() );
    }

    public void testPathBaseAbsPath()
        throws Exception
    {
        String base = "/tmp/foo";
        String path = "absolutePath";

        PathDescriptor desc = new PathDescriptor( base, "/" + path );

        assertTrue( desc.isFile() );
        assertFalse( desc.isRelative() );
        assertNotNull( desc.getBaseUrl() );
        assertNotNull( desc.getPathUrl() );
        assertNotNull( desc.getPath() );
        assertNotNull( desc.getLocation() );
        if ( System.getProperty( "os.name" ).toLowerCase().indexOf( "windows" ) != -1 )
        {
            String s = StringUtils.replace( new File( base + "/" + path ).toURL().toString(), "file:", "" );
            assertEquals( "wrong path", s, desc.getPath() );
            assertEquals( "wrong location", s, desc.getLocation() );
        }
        else
        {
            assertEquals( "wrong path", base + "/" + path, desc.getPath() );
            assertEquals( "wrong location", base + "/" + path, desc.getLocation() );
        }
    }

    public void testPathBaseRelPath()
        throws Exception
    {
        String base = "/tmp/foo";
        String path = "relativePath";

        PathDescriptor desc = new PathDescriptor( base, path );

        assertTrue( desc.isFile() );
        assertFalse( desc.isRelative() );
        assertNotNull( desc.getBaseUrl() );
        assertNotNull( desc.getPathUrl() );
        assertNotNull( desc.getPath() );
        assertNotNull( desc.getLocation() );
        if ( System.getProperty( "os.name" ).toLowerCase().indexOf( "windows" ) != -1 )
        {
            String s = StringUtils.replace( new File( base + "/" + path ).toURL().toString(), "file:", "" );
            assertEquals( "wrong path", s, desc.getPath() );
            assertEquals( "wrong location", s, desc.getLocation() );
        }
        else
        {
            assertEquals( "wrong path", base + "/" + path, desc.getPath() );
            assertEquals( "wrong location", base + "/" + path, desc.getLocation() );
        }
    }

    public void testPathBaseEmptyAbsPath()
        throws Exception
    {
        String base = "/tmp/foo";
        String path = "";

        PathDescriptor desc = new PathDescriptor( base, "/" + path );

        assertTrue( desc.isFile() );
        assertFalse( desc.isRelative() );
        assertNotNull( desc.getBaseUrl() );
        assertNotNull( desc.getPathUrl() );
        assertNotNull( desc.getPath() );
        assertNotNull( desc.getLocation() );
        if ( System.getProperty( "os.name" ).toLowerCase().indexOf( "windows" ) != -1 )
        {
            String s = StringUtils.replace( new File( base ).toURL().toString(), "file:", "" );
            assertEquals( "wrong path", s, desc.getPath() );
            assertEquals( "wrong location", s, desc.getLocation() );
        }
        else
        {
            assertEquals( "wrong path", base, desc.getPath() );
            assertEquals( "wrong location", base, desc.getLocation() );
        }
    }

    public void testPathBaseEmptyRelPath()
        throws Exception
    {
        String base = "/tmp/foo";
        String path = "";

        PathDescriptor desc = new PathDescriptor( base, path );

        assertTrue( desc.isFile() );
        assertFalse( desc.isRelative() );
        assertNotNull( desc.getBaseUrl() );
        assertNotNull( desc.getPathUrl() );
        assertNotNull( desc.getPath() );
        assertNotNull( desc.getLocation() );
        if ( System.getProperty( "os.name" ).toLowerCase().indexOf( "windows" ) != -1 )
        {
            String s = StringUtils.replace( new File( base ).toURL().toString(), "file:", "" );
            assertEquals( "wrong path", s, desc.getPath() );
            assertEquals( "wrong location", s, desc.getLocation() );
        }
        else
        {
            assertEquals( "wrong path", base, desc.getPath() );
            assertEquals( "wrong location", base, desc.getLocation() );
        }
    }

    public void testPathBaseNullPath()
        throws Exception
    {
        String base = "/tmp/foo";
        String path = null;

        PathDescriptor desc = new PathDescriptor( base, path );

        assertTrue( desc.isFile() );
        assertFalse( desc.isRelative() );
        assertNotNull( desc.getBaseUrl() );
        assertNotNull( desc.getPathUrl() );
        assertNotNull( desc.getPath() );
        assertNotNull( desc.getLocation() );
        if ( System.getProperty( "os.name" ).toLowerCase().indexOf( "windows" ) != -1 )
        {
            String s = StringUtils.replace( new File( base ).toURL().toString(), "file:", "" );
            assertEquals( "wrong path", s, desc.getPath() );
            assertEquals( "wrong location", s, desc.getLocation() );
        }
        else
        {
            assertEquals( "wrong path", base, desc.getPath() );
            assertEquals( "wrong location", base, desc.getLocation() );
        }
    }
}
