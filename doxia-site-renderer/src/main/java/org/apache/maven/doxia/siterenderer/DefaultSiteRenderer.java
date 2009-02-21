package org.apache.maven.doxia.siterenderer;

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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import java.text.DateFormat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.maven.doxia.Doxia;
import org.apache.maven.doxia.module.xhtml.decoration.render.RenderingContext;
import org.apache.maven.doxia.parser.ParseException;
import org.apache.maven.doxia.parser.Parser;
import org.apache.maven.doxia.parser.manager.ParserNotFoundException;
import org.apache.maven.doxia.site.decoration.DecorationModel;
import org.apache.maven.doxia.module.site.SiteModule;
import org.apache.maven.doxia.module.site.manager.SiteModuleManager;
import org.apache.maven.doxia.module.site.manager.SiteModuleNotFoundException;
import org.apache.maven.doxia.siterenderer.sink.SiteRendererSink;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.context.Context;

import org.codehaus.plexus.i18n.I18N;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.Os;
import org.codehaus.plexus.util.PathTool;
import org.codehaus.plexus.util.ReaderFactory;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.velocity.SiteResourceLoader;
import org.codehaus.plexus.velocity.VelocityComponent;


/**
 * @author <a href="mailto:evenisse@codehaus.org">Emmanuel Venisse</a>
 * @author <a href="mailto:vincent.siveton@gmail.com">Vincent Siveton</a>
 * @version $Id$
 * @since 1.0
 * @plexus.component role-hint="default"
 */
public class DefaultSiteRenderer
    extends AbstractLogEnabled
    implements Renderer
{
    // ----------------------------------------------------------------------
    // Requirements
    // ----------------------------------------------------------------------

    /**
     * @plexus.requirement
     */
    private VelocityComponent velocity;

    /**
     * @plexus.requirement
     */
    private SiteModuleManager siteModuleManager;

    /**
     * @plexus.requirement
     */
    private Doxia doxia;

    /**
     * @plexus.requirement
     */
    private I18N i18n;

    private static final String RESOURCE_DIR = "org/apache/maven/doxia/siterenderer/resources";

    private static final String DEFAULT_TEMPLATE = RESOURCE_DIR + "/default-site.vm";

    private static final String SKIN_TEMPLATE_LOCATION = "META-INF/maven/site.vm";

    // ----------------------------------------------------------------------
    // Renderer implementation
    // ----------------------------------------------------------------------

    /** {@inheritDoc} */
    public void render( Collection documents,
                        SiteRenderingContext siteRenderingContext,
                        File outputDirectory )
            throws RendererException, IOException
    {
        renderModule( documents, siteRenderingContext, outputDirectory );

        for ( Iterator i = siteRenderingContext.getSiteDirectories().iterator(); i.hasNext(); )
        {
            File siteDirectory = (File) i.next();
            copyResources( siteRenderingContext, new File( siteDirectory, "resources" ), outputDirectory );
        }
    }

    /** {@inheritDoc} */
    public Map locateDocumentFiles( SiteRenderingContext siteRenderingContext )
            throws IOException, RendererException
    {
        Map files = new LinkedHashMap();
        Map moduleExcludes = siteRenderingContext.getModuleExcludes();

        for ( Iterator i = siteRenderingContext.getSiteDirectories().iterator(); i.hasNext(); )
        {
            File siteDirectory = (File) i.next();
            if ( siteDirectory.exists() )
            {
                for ( Iterator j = siteModuleManager.getSiteModules().iterator(); j.hasNext(); )
                {
                    SiteModule module = (SiteModule) j.next();

                    File moduleBasedir = new File( siteDirectory, module.getSourceDirectory() );

                    if ( moduleExcludes != null && moduleExcludes.containsKey( module.getParserId() ) )
                    {
                        addModuleFiles( moduleBasedir, module, (String) moduleExcludes.get( module.getParserId() ),
                                files );
                    }
                    else
                    {
                        addModuleFiles( moduleBasedir, module, null, files );
                    }
                }
            }
        }

        for ( Iterator i = siteRenderingContext.getModules().iterator(); i.hasNext(); )
        {
            ModuleReference module = (ModuleReference) i.next();

            try
            {
                if ( moduleExcludes != null && moduleExcludes.containsKey( module.getParserId() ) )
                {
                    addModuleFiles( module.getBasedir(), siteModuleManager.getSiteModule( module.getParserId() ),
                            (String) moduleExcludes.get( module.getParserId() ), files );
                }
                else
                {
                    addModuleFiles( module.getBasedir(), siteModuleManager.getSiteModule( module.getParserId() ), null,
                            files );
                }
            }
            catch ( SiteModuleNotFoundException e )
            {
                throw new RendererException( "Unable to find module: " + e.getMessage(), e );
            }
        }
        return files;
    }

    private void addModuleFiles( File moduleBasedir,
                                 SiteModule module,
                                 String excludes,
                                 Map files )
            throws IOException, RendererException
    {
        if ( moduleBasedir.exists() )
        {
            List docs = new ArrayList();

            docs.addAll( FileUtils.getFileNames( moduleBasedir, "**/*." + module.getExtension(), excludes, false ) );

            // download.apt.vm
            docs.addAll( FileUtils.getFileNames( moduleBasedir, "**/*." + module.getExtension() + ".vm", excludes, false ) );

            for ( Iterator k = docs.iterator(); k.hasNext(); )
            {
                String doc = (String) k.next();

                RenderingContext context = new RenderingContext( moduleBasedir, doc, module.getParserId(), module.getExtension() );

                // DOXIA-111: we need a general filter here that knows how to alter the context
                if ( doc.endsWith( ".vm" ) )
                {
                    context.setAttribute( "velocity", "true" );
                }

                String key = context.getOutputName();

                if ( files.containsKey( key ) )
                {
                    DocumentRenderer renderer = (DocumentRenderer) files.get( key );

                    RenderingContext originalContext = renderer.getRenderingContext();

                    File originalDoc = new File( originalContext.getBasedir(), originalContext.getInputName() );

                    throw new RendererException( "Files '" + doc + "' clashes with existing '" + originalDoc + "'." );
                }
                // -----------------------------------------------------------------------
                // Handle key without case differences
                // -----------------------------------------------------------------------
                for ( Iterator iter = files.entrySet().iterator(); iter.hasNext(); )
                {
                    Map.Entry entry = (Map.Entry) iter.next();
                    if ( entry.getKey().toString().toLowerCase().equals( key.toLowerCase() ) )
                    {
                        DocumentRenderer renderer = (DocumentRenderer) files.get( entry.getKey() );

                        RenderingContext originalContext = renderer.getRenderingContext();

                        File originalDoc = new File( originalContext.getBasedir(), originalContext.getInputName() );

                        if ( Os.isFamily( "windows" ) )
                        {
                            throw new RendererException(
                                    "Files '" + doc + "' clashes with existing '" + originalDoc + "'." );
                        }

                        getLogger().warn( "Files '" + doc + "' could clashes with existing '" + originalDoc + "'." );
                    }
                }

                files.put( key, new DoxiaDocumentRenderer( context ) );
            }
        }
    }

    private void renderModule( Collection docs,
                               SiteRenderingContext siteRenderingContext,
                               File outputDirectory )
            throws IOException, RendererException
    {
        for ( Iterator i = docs.iterator(); i.hasNext(); )
        {
            DocumentRenderer docRenderer = (DocumentRenderer) i.next();

            RenderingContext renderingContext = docRenderer.getRenderingContext();

            File outputFile = new File( outputDirectory, docRenderer.getOutputName() );

            File inputFile = new File( renderingContext.getBasedir(), renderingContext.getInputName() );

            boolean modified = false;
            if ( !outputFile.exists() || inputFile.lastModified() > outputFile.lastModified() )
            {
                modified = true;
            }

            if ( modified || docRenderer.isOverwrite() )
            {
                if ( !outputFile.getParentFile().exists() )
                {
                    outputFile.getParentFile().mkdirs();
                }

                getLogger().debug( "Generating " + outputFile );

                OutputStreamWriter writer = new OutputStreamWriter( new FileOutputStream( outputFile ),
                        siteRenderingContext.getOutputEncoding() );

                try
                {
                    docRenderer.renderDocument( writer, this, siteRenderingContext );
                }
                finally
                {
                    IOUtil.close( writer );
                }
            }
            else
            {
                getLogger().debug( inputFile + " unchanged, not regenerating..." );
            }
        }
    }

    /** {@inheritDoc} */
    public void renderDocument( Writer writer,
                                RenderingContext renderingContext,
                                SiteRenderingContext context )
            throws RendererException, FileNotFoundException, UnsupportedEncodingException
    {
        SiteRendererSink sink = new SiteRendererSink( renderingContext );

        File doc = new File( renderingContext.getBasedir(), renderingContext.getInputName() );

        try
        {
            Reader reader = null;
            Parser parser = doxia.getParser( renderingContext.getParserId() );

            // DOXIA-111: the filter used here must be checked generally.
            if ( renderingContext.getAttribute( "velocity" ) != null )
            {
                String resource = doc.getAbsolutePath();

                try
                {
                    SiteResourceLoader.setResource( resource );

                    Context vc = createContext( sink, context );

                    StringWriter sw = new StringWriter();

                    velocity.getEngine().mergeTemplate( resource, context.getInputEncoding(), vc, sw );

                    reader = new StringReader( sw.toString() );
                }
                catch ( Exception e )
                {
                    if ( getLogger().isDebugEnabled() )
                    {
                        getLogger().error( "Error parsing " + resource + " as a velocity template, using as text.", e );
                    }
                    else
                    {
                        getLogger().error( "Error parsing " + resource + " as a velocity template, using as text." );
                    }
                }
            }
            else
            {
                switch ( parser.getType() )
                {
                    case Parser.XML_TYPE:
                        reader = ReaderFactory.newXmlReader( doc );
                        break;

                    case Parser.TXT_TYPE:
                    case Parser.UNKNOWN_TYPE:
                    default:
                        reader = ReaderFactory.newReader( doc, context.getInputEncoding() );
                }
            }

            doxia.parse( reader, renderingContext.getParserId(), sink );

            generateDocument( writer, sink, context );
        }
        catch ( ParserNotFoundException e )
        {
            throw new RendererException( "Error getting a parser for " + doc + ": " + e.getMessage() );
        }
        catch ( ParseException e )
        {
            getLogger().error( "Error parsing " + doc + ": line [" + e.getLineNumber() + "] " + e.getMessage(), e );
        }
        catch ( IOException e )
        {
            getLogger().error( "Error parsing " + doc + " to detect encoding", e );
        }
        finally
        {
            sink.flush();

            sink.close();
        }
    }

    private Context createContext( SiteRendererSink sink,
                                   SiteRenderingContext siteRenderingContext )
    {
        VelocityContext context = new VelocityContext();

        // ----------------------------------------------------------------------
        // Data objects
        // ----------------------------------------------------------------------

        RenderingContext renderingContext = sink.getRenderingContext();
        context.put( "relativePath", renderingContext.getRelativePath() );

        // Add infos from document
        context.put( "authors", sink.getAuthors() );

        String title = "";
        if ( siteRenderingContext.getDecoration().getName() != null )
        {
            title = siteRenderingContext.getDecoration().getName();
        }
        else if ( siteRenderingContext.getDefaultWindowTitle() != null )
        {
            title = siteRenderingContext.getDefaultWindowTitle();
        }

        if ( title.length() > 0 )
        {
            title += " - ";
        }
        title += sink.getTitle();

        context.put( "title", title );

        context.put( "bodyContent", sink.getBody() );

        context.put( "decoration", siteRenderingContext.getDecoration() );

        context.put( "currentDate", new Date() );

        Locale locale = siteRenderingContext.getLocale();
        context.put( "dateFormat", DateFormat.getDateInstance( DateFormat.DEFAULT, locale ) );

        String currentFileName = renderingContext.getOutputName().replace( '\\', '/' );
        context.put( "currentFileName", currentFileName );

        context.put( "alignedFileName", PathTool.calculateLink( currentFileName, renderingContext.getRelativePath() ) );

        context.put( "locale", locale );

        // Add user properties
        Map templateProperties = siteRenderingContext.getTemplateProperties();

        if ( templateProperties != null )
        {
            for ( Iterator i = templateProperties.keySet().iterator(); i.hasNext(); )
            {
                String key = (String) i.next();

                context.put( key, templateProperties.get( key ) );
            }
        }

        // ----------------------------------------------------------------------
        // Tools
        // ----------------------------------------------------------------------

        context.put( "PathTool", new PathTool() );

        context.put( "FileUtils", new FileUtils() );

        context.put( "StringUtils", new StringUtils() );

        context.put( "i18n", i18n );

        return context;
    }

    /** {@inheritDoc} */
    public void generateDocument( Writer writer,
                                  SiteRendererSink sink,
                                  SiteRenderingContext siteRenderingContext )
            throws RendererException
    {
        Context context = createContext( sink, siteRenderingContext );

        writeTemplate( writer, context, siteRenderingContext );
    }

    private void writeTemplate( Writer writer,
                                Context context,
                                SiteRenderingContext siteContext )
            throws RendererException
    {
        ClassLoader old = null;

        if ( siteContext.getTemplateClassLoader() != null )
        {
            // -------------------------------------------------------------------------
            // If no template classloader was set we'll just use the context classloader
            // -------------------------------------------------------------------------

            old = Thread.currentThread().getContextClassLoader();

            Thread.currentThread().setContextClassLoader( siteContext.getTemplateClassLoader() );
        }

        try
        {
            processTemplate( siteContext.getTemplateName(), context, writer );
        }
        finally
        {
            IOUtil.close( writer );

            if ( old != null )
            {
                Thread.currentThread().setContextClassLoader( old );
            }
        }
    }

    /**
     * @noinspection OverlyBroadCatchBlock,UnusedCatchParameter
     */
    private void processTemplate( String templateName,
                                  Context context,
                                  Writer writer )
            throws RendererException
    {
        Template template;

        try
        {
            template = velocity.getEngine().getTemplate( templateName );
        }
        catch ( Exception e )
        {
            throw new RendererException( "Could not find the template '" + templateName );
        }

        try
        {
            template.merge( context, writer );
        }
        catch ( Exception e )
        {
            throw new RendererException( "Error while generating code.", e );
        }
    }

    /** {@inheritDoc} */
    public SiteRenderingContext createContextForSkin( File skinFile,
                                                      Map attributes,
                                                      DecorationModel decoration,
                                                      String defaultWindowTitle,
                                                      Locale locale )
            throws IOException
    {
        SiteRenderingContext context = new SiteRenderingContext();

        // TODO: plexus-archiver, if it could do the excludes
        ZipFile zipFile = new ZipFile( skinFile );
        try
        {
            if ( zipFile.getEntry( SKIN_TEMPLATE_LOCATION ) != null )
            {
                context.setTemplateName( SKIN_TEMPLATE_LOCATION );
                context.setTemplateClassLoader( new URLClassLoader( new URL[]{skinFile.toURL()} ) );
            }
            else
            {
                context.setTemplateName( DEFAULT_TEMPLATE );
                context.setTemplateClassLoader( getClass().getClassLoader() );
                context.setUsingDefaultTemplate( true );
            }
        }
        finally
        {
            closeZipFile( zipFile );
        }

        context.setTemplateProperties( attributes );
        context.setLocale( locale );
        context.setDecoration( decoration );
        context.setDefaultWindowTitle( defaultWindowTitle );
        context.setSkinJarFile( skinFile );

        return context;
    }

    /** {@inheritDoc} */
    public SiteRenderingContext createContextForTemplate( File templateFile,
                                                          File skinFile,
                                                          Map attributes,
                                                          DecorationModel decoration,
                                                          String defaultWindowTitle,
                                                          Locale locale )
            throws MalformedURLException
    {
        SiteRenderingContext context = new SiteRenderingContext();

        context.setTemplateName( templateFile.getName() );
        context.setTemplateClassLoader( new URLClassLoader( new URL[]{templateFile.getParentFile().toURI().toURL()} ) );

        context.setTemplateProperties( attributes );
        context.setLocale( locale );
        context.setDecoration( decoration );
        context.setDefaultWindowTitle( defaultWindowTitle );
        context.setSkinJarFile( skinFile );

        return context;
    }

    private void closeZipFile( ZipFile zipFile )
    {
        // TODO: move to plexus utils
        try
        {
            zipFile.close();
        }
        catch ( IOException e )
        {
            // ignore
        }
    }

    /** {@inheritDoc} */
    public void copyResources( SiteRenderingContext siteRenderingContext,
                               File resourcesDirectory,
                               File outputDirectory  )
            throws IOException
    {
        if ( siteRenderingContext.getSkinJarFile() != null )
        {
            // TODO: plexus-archiver, if it could do the excludes
            ZipFile file = new ZipFile( siteRenderingContext.getSkinJarFile() );
            try
            {
                for ( Enumeration e = file.entries(); e.hasMoreElements(); )
                {
                    ZipEntry entry = (ZipEntry) e.nextElement();

                    if ( !entry.getName().startsWith( "META-INF/" ) )
                    {
                        File destFile = new File( outputDirectory, entry.getName() );
                        if ( !entry.isDirectory() )
                        {
                            destFile.getParentFile().mkdirs();

                            copyFileFromZip( file, entry, destFile );
                        }
                        else
                        {
                            destFile.mkdirs();
                        }
                    }
                }
            }
            finally
            {
                file.close();
            }
        }

        if ( siteRenderingContext.isUsingDefaultTemplate() )
        {
            InputStream resourceList = getClass().getClassLoader()
                    .getResourceAsStream( RESOURCE_DIR + "/resources.txt" );

            if ( resourceList != null )
            {
                LineNumberReader reader = new LineNumberReader( new InputStreamReader( resourceList ) );

                String line = reader.readLine();

                while ( line != null )
                {
                    InputStream is = getClass().getClassLoader().getResourceAsStream( RESOURCE_DIR + "/" + line );

                    if ( is == null )
                    {
                        throw new IOException( "The resource " + line + " doesn't exist." );
                    }

                    File outputFile = new File( outputDirectory, line );

                    if ( !outputFile.getParentFile().exists() )
                    {
                        outputFile.getParentFile().mkdirs();
                    }

                    FileOutputStream w = new FileOutputStream( outputFile );

                    IOUtil.copy( is, w );

                    IOUtil.close( is );

                    IOUtil.close( w );

                    line = reader.readLine();
                }
            }
        }

        // Copy extra site resources
        if ( resourcesDirectory != null && resourcesDirectory.exists() )
        {
            copyDirectory( resourcesDirectory, outputDirectory );
        }

        // Check for the existence of /css/site.css
        File siteCssFile = new File( outputDirectory, "/css/site.css" );
        if ( !siteCssFile.exists() )
        {
            // Create the subdirectory css if it doesn't exist, DOXIA-151
            File cssDirectory = new File( outputDirectory, "/css/" );
            boolean created = cssDirectory.mkdirs();
            if ( created && getLogger().isDebugEnabled() )
            {
                getLogger().debug(
                    "The directory '" + cssDirectory.getAbsolutePath() + "' did not exist. It was created." );
            }

            // If the file is not there - create an empty file, DOXIA-86
            if ( getLogger().isDebugEnabled() )
            {
                getLogger().debug(
                    "The file '" + siteCssFile.getAbsolutePath() + "' does not exists. Creating an empty file." );
            }
            FileWriter w = new FileWriter( siteCssFile );
            
            //put something in the file so it's not completely empty - DOXIA-290
            w.write( "/* You can override this file with your own styles */" );
            IOUtil.close( w );
        }
    }

    private void copyFileFromZip( ZipFile file,
                                  ZipEntry entry,
                                  File destFile )
            throws IOException
    {
        FileOutputStream fos = new FileOutputStream( destFile );

        try
        {
            IOUtil.copy( file.getInputStream( entry ), fos );
        }
        finally
        {
            IOUtil.close( fos );
        }
    }

    /**
     * Copy the directory
     *
     * @param source      source file to be copied
     * @param destination destination file
     * @throws java.io.IOException if any
     */
    protected void copyDirectory( File source,
                                  File destination )
            throws IOException
    {
        if ( source.exists() )
        {
            DirectoryScanner scanner = new DirectoryScanner();

            String[] includedResources = {"**/**"};

            scanner.setIncludes( includedResources );

            scanner.addDefaultExcludes();

            scanner.setBasedir( source );

            scanner.scan();

            List includedFiles = Arrays.asList( scanner.getIncludedFiles() );

            for ( Iterator j = includedFiles.iterator(); j.hasNext(); )
            {
                String name = (String) j.next();

                File sourceFile = new File( source, name );

                File destinationFile = new File( destination, name );

                FileUtils.copyFile( sourceFile, destinationFile );
            }
        }
    }

}
