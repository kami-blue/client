// 
// Decompiled by Procyon v0.5.36
// 

package org.reflections.serializers;

import org.reflections.Store;
import org.dom4j.DocumentFactory;
import java.io.Writer;
import java.io.StringWriter;
import java.io.IOException;
import java.io.OutputStream;
import org.dom4j.io.XMLWriter;
import org.dom4j.io.OutputFormat;
import java.io.FileOutputStream;
import org.reflections.util.Utils;
import java.io.File;
import java.util.Iterator;
import org.dom4j.Document;
import java.lang.reflect.Constructor;
import org.dom4j.DocumentException;
import org.reflections.ReflectionsException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.reflections.Configuration;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.Reflections;
import java.io.InputStream;

public class XmlSerializer implements Serializer
{
    @Override
    public Reflections read(final InputStream inputStream) {
        Reflections reflections;
        try {
            final Constructor<Reflections> constructor = Reflections.class.getDeclaredConstructor((Class<?>[])new Class[0]);
            constructor.setAccessible(true);
            reflections = constructor.newInstance(new Object[0]);
        }
        catch (Exception e5) {
            reflections = new Reflections(new ConfigurationBuilder());
        }
        try {
            final Document document = new SAXReader().read(inputStream);
            for (final Object e1 : document.getRootElement().elements()) {
                final Element index = (Element)e1;
                for (final Object e2 : index.elements()) {
                    final Element entry = (Element)e2;
                    final Element key = entry.element("key");
                    final Element values = entry.element("values");
                    for (final Object o3 : values.elements()) {
                        final Element value = (Element)o3;
                        reflections.getStore().getOrCreate(index.getName()).put((Object)key.getText(), (Object)value.getText());
                    }
                }
            }
        }
        catch (DocumentException e3) {
            throw new ReflectionsException("could not read.", (Throwable)e3);
        }
        catch (Throwable e4) {
            throw new RuntimeException("Could not read. Make sure relevant dependencies exist on classpath.", e4);
        }
        return reflections;
    }
    
    @Override
    public File save(final Reflections reflections, final String filename) {
        final File file = Utils.prepareFile(filename);
        try {
            final Document document = this.createDocument(reflections);
            final XMLWriter xmlWriter = new XMLWriter((OutputStream)new FileOutputStream(file), OutputFormat.createPrettyPrint());
            xmlWriter.write(document);
            xmlWriter.close();
        }
        catch (IOException e) {
            throw new ReflectionsException("could not save to file " + filename, e);
        }
        catch (Throwable e2) {
            throw new RuntimeException("Could not save to file " + filename + ". Make sure relevant dependencies exist on classpath.", e2);
        }
        return file;
    }
    
    @Override
    public String toString(final Reflections reflections) {
        final Document document = this.createDocument(reflections);
        try {
            final StringWriter writer = new StringWriter();
            final XMLWriter xmlWriter = new XMLWriter((Writer)writer, OutputFormat.createPrettyPrint());
            xmlWriter.write(document);
            xmlWriter.close();
            return writer.toString();
        }
        catch (IOException e) {
            throw new RuntimeException();
        }
    }
    
    private Document createDocument(final Reflections reflections) {
        final Store map = reflections.getStore();
        final Document document = DocumentFactory.getInstance().createDocument();
        final Element root = document.addElement("Reflections");
        for (final String indexName : map.keySet()) {
            final Element indexElement = root.addElement(indexName);
            for (final String key : map.get(indexName).keySet()) {
                final Element entryElement = indexElement.addElement("entry");
                entryElement.addElement("key").setText(key);
                final Element valuesElement = entryElement.addElement("values");
                for (final String value : map.get(indexName).get((Object)key)) {
                    valuesElement.addElement("value").setText(value);
                }
            }
        }
        return document;
    }
}
