/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.core.plugins;

import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.ProtectionDomain;

import org.pentaho.di.i18n.BaseMessages;

public class KettleURLClassLoader extends URLClassLoader
{
  private static Class<?> PKG = KettleURLClassLoader.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

    private String name;
    
    public KettleURLClassLoader(URL[] url, ClassLoader classLoader)
    {
        super(url, classLoader);
    }
    
    public KettleURLClassLoader(URL[] url, ClassLoader classLoader, String name)
    {
        this(url, classLoader);
        this.name = name;
    }
    
    public String toString()
    {
        return super.toString()+" : "+name;
    }
    
    public void setName(String name)
    {
        this.name = name;
    }
    
    public String getName()
    {
        return name;
    }
    
    
    
    @Override
    protected synchronized Class<?> loadClass(String arg0, boolean arg1)
        throws ClassNotFoundException {
      Class<?> clz = null;
      if((clz = findLoadedClass(arg0)) != null){
        if(arg1){
          resolveClass(clz);
        }
        return clz;
      }
      try{
        if ((clz = findClass(arg0)) != null){
          if(arg1){
            resolveClass(clz);
          }
          return clz;
        }
      } catch(ClassNotFoundException e){
        
      } catch(NoClassDefFoundError e){
        
      }

      if((clz = getParent().loadClass(arg0)) != null){
        if(arg1){
          resolveClass(clz);
        }
        return clz; 
      }
      throw new ClassNotFoundException("Could not find :"+arg0);
    }

    /*
        Cglib doe's not creates custom class loader (to access package methotds and classes ) it uses reflection to invoke "defineClass", 
        but you can call protected method in subclass without problems:
    */
    public Class<?> loadClass(String name, ProtectionDomain protectionDomain) 
    {
        Class<?> loaded = findLoadedClass(name);
        if (loaded == null)
        {
            // Get the jar, load the bytes from the jar file, construct class from scratch as in snippet below...

            /*
            
            loaded = super.findClass(name);
            
            URL url = super.findResource(newName);
            
            InputStream clis = getResourceAsStream(newName);
            
            */
           
            String newName = name.replace('.','/');
            InputStream is = super.getResourceAsStream(newName);
            byte[] driverBytes = toBytes( is );
            
            loaded = super.defineClass(name, driverBytes, 0, driverBytes.length, protectionDomain);

        }
        return loaded;
    }
    
    private byte[] toBytes(InputStream is)
    {
        byte[] retval = new byte[0];
        try
        {
            int a = is.available();
          while (a>0)
          {
              byte[] buffer = new byte[a];
              is.read(buffer);
              
              byte[] newretval = new byte[retval.length+a];
              
              for (int i=0;i<retval.length;i++) newretval[i] = retval[i]; // old part
              for (int i=0;i<a;i++) newretval[retval.length+i] = buffer[i]; // new part
              
              retval = newretval;
              
              a = is.available(); // see what's left
          }
            return retval; 
        }
        catch(Exception e)
        {
            System.out.println(BaseMessages.getString(PKG, "KettleURLClassLoader.Exception.UnableToReadClass")+e.toString()); //$NON-NLS-1$
            return null;
        }
    }
}
