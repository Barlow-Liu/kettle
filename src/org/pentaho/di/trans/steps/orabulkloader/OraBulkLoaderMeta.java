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

package org.pentaho.di.trans.steps.orabulkloader;

import java.util.List;
import java.util.Map;

import org.drools.util.StringUtils;
import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.SQLStatement;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.shared.SharedObjectInterface;
import org.pentaho.di.trans.DatabaseImpact;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.w3c.dom.Node;


/**
 * Created on 20-feb-2007
 * 
 * @author Sven Boden
 */
public class OraBulkLoaderMeta extends BaseStepMeta implements StepMetaInterface
{
	private static Class<?> PKG = OraBulkLoaderMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$
	
	private static int DEFAULT_COMMIT_SIZE = 100000; // The bigger the better for Oracle
	private static int DEFAULT_BIND_SIZE = 0;
	private static int DEFAULT_READ_SIZE = 0;
	private static int DEFAULT_MAX_ERRORS = 50;

    /** what's the schema for the target? */
    private String schemaName;

    /** what's the table for the target? */
	private String tableName;
	
	/** Path to the sqlldr utility */
	private String sqlldr;
	
	/** Path to the control file  */
	private String controlFile;
	
	/** Path to the data file */
	private String dataFile;
	
	/** Path to the log file */
	private String logFile;
	
	/** Path to the bad file */
	private String badFile;
	
	/** Path to the discard file */
	private String discardFile;

    /** database connection */
	private DatabaseMeta databaseMeta;

    /** Field value to dateMask after lookup */
	private String fieldTable[];

    /** Field name in the stream */
	private String fieldStream[];

    /** boolean indicating if field needs to be updated */
	private String dateMask[];

	/** Commit size (ROWS) */
	private String commitSize;

	/** bindsize */
	private String bindSize;

	/** readsize */
	private String readSize;	

	/** maximum errors */
	private String maxErrors;		
	
	/** Load method */
	private String loadMethod;
	
	/** Load action */
	private String loadAction;	
	
	/** Encoding to use */
	private String encoding;
	
	/** Character set name used for Oracle */
	private String characterSetName;

    /** Direct Path? */
	private boolean directPath;
			
	/** Erase files after use */
	private boolean eraseFiles; 
	
	/** Database name override */
	private String dbNameOverride;
	
	/** Fails when sqlldr returns a warning **/
	private boolean failOnWarning;
	
	/** Fails when sqlldr returns anything else than a warning or OK **/
	private boolean failOnError;

	 /** allow Oracle to load data in parallel **/
  private boolean parallel;

	/** If not empty, use this record terminator instead of default one **/
	private String altRecordTerm;
	
	/*
	 * Do not translate following values!!! They are will end up in the job export.
	 */
	final static public String ACTION_APPEND   = "APPEND";
	final static public String ACTION_INSERT   = "INSERT";
	final static public String ACTION_REPLACE  = "REPLACE";
	final static public String ACTION_TRUNCATE = "TRUNCATE";

	/*
	 * Do not translate following values!!! They are will end up in the job export.
	 */
	final static public String METHOD_AUTO_CONCURRENT = "AUTO_CONCURRENT";
	final static public String METHOD_AUTO_END        = "AUTO_END";
	final static public String METHOD_MANUAL          = "MANUAL";
	
	/*
	 * Do not translate following values!!! They are will end up in the job export.
	 */
	final static public String DATE_MASK_DATE     = "DATE";
	final static public String DATE_MASK_DATETIME = "DATETIME";
		
	public OraBulkLoaderMeta()
	{
		super();
	}
	
	public int getCommitSizeAsInt(VariableSpace varSpace) {
    try {
      return Integer.valueOf(varSpace.environmentSubstitute(getCommitSize()));
    } catch (NumberFormatException ex) {
      return DEFAULT_COMMIT_SIZE;
    }
  }	

    /**
     * @return Returns the commitSize.
     */
    public String getCommitSize()
    {
        return commitSize;
    }

    /**
     * @param commitSize The commitSize to set.
     */
    public void setCommitSize(String commitSize)
    {
        this.commitSize = commitSize;
    }

    /**
     * @return Returns the database.
     */
    public DatabaseMeta getDatabaseMeta()
    {
        return databaseMeta;
    }

    /**
     * @param database The database to set.
     */
    public void setDatabaseMeta(DatabaseMeta database)
    {
        this.databaseMeta = database;
    }

    /**
     * @return Returns the tableName.
     */
    public String getTableName()
    {
        return tableName;
    }

    /**
     * @param tableName The tableName to set.
     */
    public void setTableName(String tableName)
    {
        this.tableName = tableName;
    }

	public String getSqlldr() {
		return sqlldr;
	}

	public void setSqlldr(String sqlldr) {
		this.sqlldr = sqlldr;
	}    
    
    /**
     * @return Returns the fieldTable.
     */
    public String[] getFieldTable()
    {
        return fieldTable;
    }

    /**
     * @param fieldTable The fieldTable to set.
     */
    public void setFieldTable(String[] updateLookup)
    {
        this.fieldTable = updateLookup;
    }

    /**
     * @return Returns the fieldStream.
     */
    public String[] getFieldStream()
    {
        return fieldStream;
    }

    /**
     * @param fieldStream The fieldStream to set.
     */
    public void setFieldStream(String[] updateStream)
    {
        this.fieldStream = updateStream;
    }

	public String[] getDateMask() {
		return dateMask;
	}

	public void setDateMask(String[] dateMask) {
		this.dateMask = dateMask;
	}
	
	public boolean isFailOnWarning() {
		return failOnWarning;
	}

	public void setFailOnWarning(boolean failOnWarning) {
		this.failOnWarning = failOnWarning;
	}

	public boolean isFailOnError() {
		return failOnError;
	}

	public void setFailOnError(boolean failOnError) {
		this.failOnError = failOnError;
	}

	public String getCharacterSetName() {
		return characterSetName;
	}

	public void setCharacterSetName(String characterSetName) {
		this.characterSetName = characterSetName;
	}

	public String getAltRecordTerm() {
		return altRecordTerm;
	}

	public void setAltRecordTerm(String altRecordTerm) {
		this.altRecordTerm = altRecordTerm;
	}

	public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters)
		throws KettleXMLException
	{
		readData(stepnode, databases);
	}

	public void allocate(int nrvalues)
	{
		fieldTable  = new String[nrvalues];
		fieldStream = new String[nrvalues];
		dateMask    = new String[nrvalues];
	}

	public Object clone()
	{
		OraBulkLoaderMeta retval = (OraBulkLoaderMeta)super.clone();
		int nrvalues  = fieldTable.length;

		retval.allocate(nrvalues);

		for (int i=0;i<nrvalues;i++)
		{
			retval.fieldTable[i]  = fieldTable[i];
			retval.fieldStream[i] = fieldStream[i];
			retval.dateMask[i]    = dateMask[i];
		}
		return retval;
	}

	private void readData(Node stepnode, List<? extends SharedObjectInterface> databases)
		throws KettleXMLException
	{
		try
		{
			// String csize, bsize, rsize, serror;
			// int nrvalues;

			String con     = XMLHandler.getTagValue(stepnode, "connection");   //$NON-NLS-1$
			databaseMeta   = DatabaseMeta.findDatabase(databases, con);
			
			commitSize          = XMLHandler.getTagValue(stepnode, "commit");       //$NON-NLS-1$
			if(StringUtils.isEmpty(commitSize)) {
			  commitSize = Integer.toString(DEFAULT_COMMIT_SIZE);
			}

			bindSize          = XMLHandler.getTagValue(stepnode, "bind_size");    //$NON-NLS-1$
			if(StringUtils.isEmpty(bindSize)) {
        bindSize = Integer.toString(DEFAULT_BIND_SIZE);
      }
			
			readSize       = XMLHandler.getTagValue(stepnode, "read_size");    //$NON-NLS-1$
			if(StringUtils.isEmpty(readSize)) {
        readSize = Integer.toString(DEFAULT_READ_SIZE);
      }

			maxErrors         = XMLHandler.getTagValue(stepnode, "errors");       //$NON-NLS-1$
			if(StringUtils.isEmpty(maxErrors)) {
        maxErrors = Integer.toString(DEFAULT_MAX_ERRORS);
      }

            schemaName     = XMLHandler.getTagValue(stepnode, "schema");       //$NON-NLS-1$
			tableName      = XMLHandler.getTagValue(stepnode, "table");        //$NON-NLS-1$
			
			loadMethod     = XMLHandler.getTagValue(stepnode, "load_method");  //$NON-NLS-1$
			loadAction     = XMLHandler.getTagValue(stepnode, "load_action");  //$NON-NLS-1$			
			sqlldr         = XMLHandler.getTagValue(stepnode, "sqlldr");       //$NON-NLS-1$
			controlFile    = XMLHandler.getTagValue(stepnode, "control_file"); //$NON-NLS-1$
			dataFile       = XMLHandler.getTagValue(stepnode, "data_file");    //$NON-NLS-1$
			logFile        = XMLHandler.getTagValue(stepnode, "log_file");     //$NON-NLS-1$
			badFile        = XMLHandler.getTagValue(stepnode, "bad_file");     //$NON-NLS-1$
			discardFile    = XMLHandler.getTagValue(stepnode, "discard_file"); //$NON-NLS-1$
			directPath     = "Y".equalsIgnoreCase( XMLHandler.getTagValue(stepnode, "direct_path")); //$NON-NLS-1$
			eraseFiles     = "Y".equalsIgnoreCase( XMLHandler.getTagValue(stepnode, "erase_files")); //$NON-NLS-1$
			encoding       = XMLHandler.getTagValue(stepnode, "encoding");         //$NON-NLS-1$
			dbNameOverride = XMLHandler.getTagValue(stepnode, "dbname_override");  //$NON-NLS-1$#
			
			characterSetName = XMLHandler.getTagValue(stepnode, "character_set");                     //$NON-NLS-1$
			failOnWarning    = "Y".equalsIgnoreCase( XMLHandler.getTagValue(stepnode, "fail_on_warning")); //$NON-NLS-1$
			failOnError      = "Y".equalsIgnoreCase( XMLHandler.getTagValue(stepnode, "fail_on_error"));   //$NON-NLS-1$
      parallel         = "Y".equalsIgnoreCase( XMLHandler.getTagValue(stepnode, "parallel"));   //$NON-NLS-1$
			altRecordTerm    = XMLHandler.getTagValue(stepnode, "alt_rec_term");                           //$NON-NLS-1$

			int nrvalues       = XMLHandler.countNodes(stepnode, "mapping");      //$NON-NLS-1$
			allocate(nrvalues);

			for (int i=0;i<nrvalues;i++)
			{
				Node vnode = XMLHandler.getSubNodeByNr(stepnode, "mapping", i);    //$NON-NLS-1$

				fieldTable[i]      = XMLHandler.getTagValue(vnode, "stream_name"); //$NON-NLS-1$
				fieldStream[i]     = XMLHandler.getTagValue(vnode, "field_name");  //$NON-NLS-1$
				if (fieldStream[i]==null) fieldStream[i]=fieldTable[i];            // default: the same name!
				String locDateMask = XMLHandler.getTagValue(vnode, "date_mask");   //$NON-NLS-1$
				if(locDateMask==null) {
					dateMask[i] = "";
				} else
                {
                    if (OraBulkLoaderMeta.DATE_MASK_DATE.equals(locDateMask) ||
                        OraBulkLoaderMeta.DATE_MASK_DATETIME.equals(locDateMask) )
                    {
                        dateMask[i] = locDateMask;
                    }
                    else
                    {
                    	dateMask[i] = "";
                    }
				}
			}
		}
		catch(Exception e)
		{
			throw new KettleXMLException(BaseMessages.getString(PKG, "OraBulkLoaderMeta.Exception.UnableToReadStepInfoFromXML"), e); //$NON-NLS-1$
		}
	}

	public void setDefault()
	{
		fieldTable   = null;
		databaseMeta = null;
		commitSize   = Integer.toString(DEFAULT_COMMIT_SIZE);
		bindSize     = Integer.toString(DEFAULT_BIND_SIZE);                 // Use platform default         
		readSize     = Integer.toString(DEFAULT_READ_SIZE);                 // Use platform default
		maxErrors    = Integer.toString(DEFAULT_MAX_ERRORS);
        schemaName   = "";                //$NON-NLS-1$
		tableName    = BaseMessages.getString(PKG, "OraBulkLoaderMeta.DefaultTableName"); //$NON-NLS-1$
		loadMethod   = METHOD_AUTO_END;
		loadAction   = ACTION_APPEND;
		sqlldr       = "sqlldr";                              //$NON-NLS-1$
		controlFile  = "control${Internal.Step.CopyNr}.cfg";  //$NON-NLS-1$
		dataFile     = "load${Internal.Step.CopyNr}.dat";     //$NON-NLS-1$
		logFile      = "";                                    //$NON-NLS-1$
		badFile      = "";                                    //$NON-NLS-1$
		discardFile  = "";                                    //$NON-NLS-1$
		encoding     = "";                                    //$NON-NLS-1$
		dbNameOverride = "";
			
		directPath   = false;
		eraseFiles   = true;
		
		characterSetName   = ""; //$NON-NLS-1$
		failOnWarning      = false;
		failOnError        = false;
    parallel           = false;
		altRecordTerm      = ""; //$NON-NLS-1$

		int nrvalues = 0;
		allocate(nrvalues);
	}

	public String getXML()
	{
        StringBuffer retval = new StringBuffer(300);

		retval.append("    ").append(XMLHandler.addTagValue("connection",   databaseMeta==null?"":databaseMeta.getName())); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		retval.append("    ").append(XMLHandler.addTagValue("commit",       commitSize));    //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("    ").append(XMLHandler.addTagValue("bind_size",    bindSize));      //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("    ").append(XMLHandler.addTagValue("read_size",    readSize));      //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("    ").append(XMLHandler.addTagValue("errors",       maxErrors));     //$NON-NLS-1$ //$NON-NLS-2$
        retval.append("    ").append(XMLHandler.addTagValue("schema",       schemaName));    //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("    ").append(XMLHandler.addTagValue("table",        tableName));     //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("    ").append(XMLHandler.addTagValue("load_method",  loadMethod));    //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("    ").append(XMLHandler.addTagValue("load_action",  loadAction));    //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("    ").append(XMLHandler.addTagValue("sqlldr",       sqlldr));        //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("    ").append(XMLHandler.addTagValue("control_file", controlFile));   //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("    ").append(XMLHandler.addTagValue("data_file",    dataFile));      //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("    ").append(XMLHandler.addTagValue("log_file",     logFile));       //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("    ").append(XMLHandler.addTagValue("bad_file",     badFile));       //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("    ").append(XMLHandler.addTagValue("discard_file", discardFile));   //$NON-NLS-1$ //$NON-NLS-2$

		retval.append("    ").append(XMLHandler.addTagValue("direct_path",  directPath));    //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("    ").append(XMLHandler.addTagValue("erase_files",  eraseFiles));    //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("    ").append(XMLHandler.addTagValue("encoding",     encoding));      //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("    ").append(XMLHandler.addTagValue("dbname_override", dbNameOverride));      //$NON-NLS-1$ //$NON-NLS-2$
		
		retval.append("    ").append(XMLHandler.addTagValue("character_set", characterSetName));   //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("    ").append(XMLHandler.addTagValue("fail_on_warning", failOnWarning));   //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("    ").append(XMLHandler.addTagValue("fail_on_error", failOnError));       //$NON-NLS-1$ //$NON-NLS-2$
    retval.append("    ").append(XMLHandler.addTagValue("parallel", parallel));       //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("    ").append(XMLHandler.addTagValue("alt_rec_term", altRecordTerm));      //$NON-NLS-1$ //$NON-NLS-2$
		
		for (int i=0;i<fieldTable.length;i++)
		{
			retval.append("      <mapping>").append(Const.CR); //$NON-NLS-1$
			retval.append("        ").append(XMLHandler.addTagValue("stream_name", fieldTable[i])); //$NON-NLS-1$ //$NON-NLS-2$
			retval.append("        ").append(XMLHandler.addTagValue("field_name",  fieldStream[i])); //$NON-NLS-1$ //$NON-NLS-2$
			retval.append("        ").append(XMLHandler.addTagValue("date_mask",   dateMask[i])); //$NON-NLS-1$ //$NON-NLS-2$
			retval.append("      </mapping>").append(Const.CR); //$NON-NLS-1$
		}

		return retval.toString();
	}

	public void readRep(Repository rep, ObjectId id_step, List<DatabaseMeta> databases, Map<String, Counter> counters)
		throws KettleException
	{
		try
		{
			databaseMeta = rep.loadDatabaseMetaFromStepAttribute(id_step, "id_connection", databases);  //$NON-NLS-1$

			commitSize     =      rep.getStepAttributeString(id_step, "commit");         //$NON-NLS-1$
			bindSize       =      rep.getStepAttributeString(id_step, "bind_size");      //$NON-NLS-1$
			readSize       =      rep.getStepAttributeString(id_step, "read_size");      //$NON-NLS-1$
			maxErrors      =      rep.getStepAttributeString(id_step, "errors");         //$NON-NLS-1$
            schemaName     =      rep.getStepAttributeString(id_step,  "schema");         //$NON-NLS-1$
			tableName      =      rep.getStepAttributeString(id_step,  "table");          //$NON-NLS-1$
			loadMethod     =      rep.getStepAttributeString(id_step,  "load_method");    //$NON-NLS-1$
			loadAction     =      rep.getStepAttributeString(id_step,  "load_action");    //$NON-NLS-1$
			sqlldr         =      rep.getStepAttributeString(id_step,  "sqlldr");         //$NON-NLS-1$			
			controlFile    =      rep.getStepAttributeString(id_step,  "control_file");   //$NON-NLS-1$
			dataFile       =      rep.getStepAttributeString(id_step,  "data_file");      //$NON-NLS-1$
			logFile        =      rep.getStepAttributeString(id_step,  "log_file");       //$NON-NLS-1$
			badFile        =      rep.getStepAttributeString(id_step,  "bad_file");       //$NON-NLS-1$
			discardFile    =      rep.getStepAttributeString(id_step,  "discard_file");   //$NON-NLS-1$
			
			directPath     =      rep.getStepAttributeBoolean(id_step, "direct_path");    //$NON-NLS-1$
			eraseFiles     =      rep.getStepAttributeBoolean(id_step, "erase_files");    //$NON-NLS-1$
			encoding       =      rep.getStepAttributeString(id_step,  "encoding");       //$NON-NLS-1$
			dbNameOverride =      rep.getStepAttributeString(id_step,  "dbname_override");//$NON-NLS-1$
			
			characterSetName =    rep.getStepAttributeString(id_step,  "character_set"); //$NON-NLS-1$
			failOnWarning    = 	  rep.getStepAttributeBoolean(id_step, "fail_on_warning");    //$NON-NLS-1$
			failOnError      =    rep.getStepAttributeBoolean(id_step, "fail_on_error");      //$NON-NLS-1$
      parallel         =    rep.getStepAttributeBoolean(id_step, "parallel");      //$NON-NLS-1$
			altRecordTerm    =    rep.getStepAttributeString(id_step,  "alt_rec_term");       //$NON-NLS-1$
			
			int nrvalues = rep.countNrStepAttributes(id_step, "stream_name");             //$NON-NLS-1$

			allocate(nrvalues);

			for (int i=0;i<nrvalues;i++)
			{
				fieldTable[i]  = rep.getStepAttributeString(id_step, i, "stream_name");   //$NON-NLS-1$
				fieldStream[i] = rep.getStepAttributeString(id_step, i, "field_name");    //$NON-NLS-1$
				dateMask[i]    = rep.getStepAttributeString(id_step, i, "date_mask");     //$NON-NLS-1$
			}
		}
		catch(Exception e)
		{
			throw new KettleException(BaseMessages.getString(PKG, "OraBulkLoaderMeta.Exception.UnexpectedErrorReadingStepInfoFromRepository"), e); //$NON-NLS-1$
		}
	}

	public void saveRep(Repository rep, ObjectId id_transformation, ObjectId id_step)
		throws KettleException
	{
		try
		{
			rep.saveDatabaseMetaStepAttribute(id_transformation, id_step, "id_connection", databaseMeta);
			rep.saveStepAttribute(id_transformation, id_step, "commit",          commitSize);    //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "bind_size",       bindSize);      //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "read_size",       readSize);      //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "errors",          maxErrors);     //$NON-NLS-1$
            rep.saveStepAttribute(id_transformation, id_step, "schema",          schemaName);    //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "table",           tableName);     //$NON-NLS-1$
			
			rep.saveStepAttribute(id_transformation, id_step, "load_method",     loadMethod);    //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "load_action",     loadAction);    //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "sqlldr",          sqlldr);        //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "control_file",    controlFile);   //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "data_file",       dataFile);      //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "log_file",        logFile);       //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "bad_file",        badFile);       //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "discard_file",    discardFile);   //$NON-NLS-1$
			
			rep.saveStepAttribute(id_transformation, id_step, "direct_path",     directPath);    //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "erase_files",     eraseFiles);    //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "encoding",        encoding);      //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "dbname_override", dbNameOverride);//$NON-NLS-1$
			
			rep.saveStepAttribute(id_transformation, id_step, "character_set", characterSetName);  //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "fail_on_warning",    failOnWarning);     //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "fail_on_error",      failOnError);       //$NON-NLS-1$			
      rep.saveStepAttribute(id_transformation, id_step, "parallel", parallel);       //$NON-NLS-1$     
			rep.saveStepAttribute(id_transformation, id_step, "alt_rec_term",       altRecordTerm);     //$NON-NLS-1$

			for (int i=0;i<fieldTable.length;i++)
			{
				rep.saveStepAttribute(id_transformation, id_step, i, "stream_name", fieldTable[i]);  //$NON-NLS-1$
				rep.saveStepAttribute(id_transformation, id_step, i, "field_name",  fieldStream[i]); //$NON-NLS-1$
				rep.saveStepAttribute(id_transformation, id_step, i, "date_mask",   dateMask[i]);    //$NON-NLS-1$
			}

			// Also, save the step-database relationship!
			if (databaseMeta!=null) rep.insertStepDatabase(id_transformation, id_step, databaseMeta.getObjectId());
		}
		catch(Exception e)
		{
			throw new KettleException(BaseMessages.getString(PKG, "OraBulkLoaderMeta.Exception.UnableToSaveStepInfoToRepository")+id_step, e); //$NON-NLS-1$
		}
	}
	
	public void getFields(RowMetaInterface rowMeta, String origin, RowMetaInterface[] info, StepMeta nextStep, VariableSpace space) throws KettleStepException
	{
		// Default: nothing changes to rowMeta
	}

	public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev, String input[], String output[], RowMetaInterface info)
	{
		CheckResult cr;
		String error_message = ""; //$NON-NLS-1$

		if (databaseMeta!=null)
		{
			Database db = new Database(loggingObject, databaseMeta);
			db.shareVariablesWith(transMeta);
			try
			{
				db.connect();

				if (!Const.isEmpty(tableName))
				{
					cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(PKG, "OraBulkLoaderMeta.CheckResult.TableNameOK"), stepMeta); //$NON-NLS-1$
					remarks.add(cr);

					boolean first=true;
					boolean error_found=false;
					error_message = ""; //$NON-NLS-1$
					
					// Check fields in table
                    String schemaTable = databaseMeta.getQuotedSchemaTableCombination(
                    		                   transMeta.environmentSubstitute(schemaName), 
                    		                   transMeta.environmentSubstitute(tableName));
					RowMetaInterface r = db.getTableFields(schemaTable);
					if (r!=null)
					{
						cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(PKG, "OraBulkLoaderMeta.CheckResult.TableExists"), stepMeta); //$NON-NLS-1$
						remarks.add(cr);

						// How about the fields to insert/dateMask in the table?
						first=true;
						error_found=false;
						error_message = ""; //$NON-NLS-1$
						
						for (int i=0;i<fieldTable.length;i++)
						{
							String field = fieldTable[i];

							ValueMetaInterface v = r.searchValueMeta(field);
							if (v==null)
							{
								if (first)
								{
									first=false;
									error_message+=BaseMessages.getString(PKG, "OraBulkLoaderMeta.CheckResult.MissingFieldsToLoadInTargetTable")+Const.CR; //$NON-NLS-1$
								}
								error_found=true;
								error_message+="\t\t"+field+Const.CR;  //$NON-NLS-1$
							}
						}
						if (error_found)
						{
							cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepMeta);
						}
						else
						{
							cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(PKG, "OraBulkLoaderMeta.CheckResult.AllFieldsFoundInTargetTable"), stepMeta); //$NON-NLS-1$
						}
						remarks.add(cr);
					}
					else
					{
						error_message=BaseMessages.getString(PKG, "OraBulkLoaderMeta.CheckResult.CouldNotReadTableInfo"); //$NON-NLS-1$
						cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepMeta);
						remarks.add(cr);
					}
				}

				// Look up fields in the input stream <prev>
				if (prev!=null && prev.size()>0)
				{
					cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(PKG, "OraBulkLoaderMeta.CheckResult.StepReceivingDatas",prev.size()+""), stepMeta); //$NON-NLS-1$ //$NON-NLS-2$
					remarks.add(cr);

					boolean first=true;
					error_message = ""; //$NON-NLS-1$
					boolean error_found = false;

					for (int i=0;i<fieldStream.length;i++)
					{
						ValueMetaInterface v = prev.searchValueMeta(fieldStream[i]);
						if (v==null)
						{
							if (first)
							{
								first=false;
								error_message+=BaseMessages.getString(PKG, "OraBulkLoaderMeta.CheckResult.MissingFieldsInInput")+Const.CR; //$NON-NLS-1$
							}
							error_found=true;
							error_message+="\t\t"+fieldStream[i]+Const.CR;  //$NON-NLS-1$
						}
					}
					if (error_found)
 					{
						cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepMeta);
					}
					else
					{
						cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(PKG, "OraBulkLoaderMeta.CheckResult.AllFieldsFoundInInput"), stepMeta); //$NON-NLS-1$
					}
					remarks.add(cr);
				}
				else
				{
					error_message=BaseMessages.getString(PKG, "OraBulkLoaderMeta.CheckResult.MissingFieldsInInput3")+Const.CR; //$NON-NLS-1$
					cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepMeta);
					remarks.add(cr);
				}
			}
			catch(KettleException e)
			{
				error_message = BaseMessages.getString(PKG, "OraBulkLoaderMeta.CheckResult.DatabaseErrorOccurred")+e.getMessage(); //$NON-NLS-1$
				cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepMeta);
				remarks.add(cr);
			}
			finally
			{
				db.disconnect();
			}
		}
		else
		{
			error_message = BaseMessages.getString(PKG, "OraBulkLoaderMeta.CheckResult.InvalidConnection"); //$NON-NLS-1$
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepMeta);
			remarks.add(cr);
		}

		// See if we have input streams leading to this step!
		if (input.length>0)
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(PKG, "OraBulkLoaderMeta.CheckResult.StepReceivingInfoFromOtherSteps"), stepMeta); //$NON-NLS-1$
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "OraBulkLoaderMeta.CheckResult.NoInputError"), stepMeta); //$NON-NLS-1$
			remarks.add(cr);
		}
	}

	public SQLStatement getSQLStatements(TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev) throws KettleStepException
	{
		SQLStatement retval = new SQLStatement(stepMeta.getName(), databaseMeta, null); // default: nothing to do!

		if (databaseMeta!=null)
		{
			if (prev!=null && prev.size()>0)
			{
                // Copy the row
                RowMetaInterface tableFields = new RowMeta();

                // Now change the field names
                for (int i=0;i<fieldTable.length;i++)
                {
                    ValueMetaInterface v = prev.searchValueMeta(fieldStream[i]);
                    if (v!=null)
                    {
                        ValueMetaInterface tableField = v.clone();
                        tableField.setName(fieldTable[i]);
                        tableFields.addValueMeta(tableField);
                    }
                    else
                    {
                        throw new KettleStepException("Unable to find field ["+fieldStream[i]+"] in the input rows");
                    }
                }

				if (!Const.isEmpty(tableName))
				{
                    Database db = new Database(loggingObject, databaseMeta);
                    db.shareVariablesWith(transMeta);
					try
					{
						db.connect();

                        String schemaTable = databaseMeta.getQuotedSchemaTableCombination(transMeta.environmentSubstitute(schemaName), 
                        		                                                          transMeta.environmentSubstitute(tableName));                        
						String sql = db.getDDL(schemaTable,
													tableFields,
													null,
													false,
													null,
													true
													);

						if (sql.length()==0) retval.setSQL(null); else retval.setSQL(sql);
					}
					catch(KettleException e)
					{
						retval.setError(BaseMessages.getString(PKG, "OraBulkLoaderMeta.GetSQL.ErrorOccurred")+e.getMessage()); //$NON-NLS-1$
					}
				}
				else
				{
					retval.setError(BaseMessages.getString(PKG, "OraBulkLoaderMeta.GetSQL.NoTableDefinedOnConnection")); //$NON-NLS-1$
				}
			}
			else
			{
				retval.setError(BaseMessages.getString(PKG, "OraBulkLoaderMeta.GetSQL.NotReceivingAnyFields")); //$NON-NLS-1$
			}
		}
		else
		{
			retval.setError(BaseMessages.getString(PKG, "OraBulkLoaderMeta.GetSQL.NoConnectionDefined")); //$NON-NLS-1$
		}

		return retval;
	}

	public void analyseImpact(List<DatabaseImpact> impact, TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev, String input[], String output[], RowMetaInterface info) throws KettleStepException
    {
        if (prev != null)
        {
            /* DEBUG CHECK THIS */
            // Insert dateMask fields : read/write
            for (int i = 0; i < fieldTable.length; i++)
            {
                ValueMetaInterface v = prev.searchValueMeta(fieldStream[i]);

                DatabaseImpact ii = new DatabaseImpact(DatabaseImpact.TYPE_IMPACT_READ_WRITE, transMeta.getName(), stepMeta.getName(), databaseMeta
                        .getDatabaseName(), transMeta.environmentSubstitute(tableName), fieldTable[i], fieldStream[i], v!=null?v.getOrigin():"?", "", "Type = " + v.toStringMeta()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                impact.add(ii);
            }
        }
	}

	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta, Trans trans)
	{
		return new OraBulkLoader(stepMeta, stepDataInterface, cnr, transMeta, trans);
	}

	public StepDataInterface getStepData()
	{
		return new OraBulkLoaderData();
	}

    public DatabaseMeta[] getUsedDatabaseConnections()
    {
        if (databaseMeta!=null)
        {
            return new DatabaseMeta[] { databaseMeta };
        }
        else
        {
            return super.getUsedDatabaseConnections();
        }
    }

	/**
	 * @return Do we want direct path loading.
	 */
	public boolean isDirectPath() {
		return directPath;
	}

	/**
	 * @param directPath do we want direct path
	 */	
	public void setDirectPath(boolean directPath) {
		this.directPath = directPath;
	}

    public RowMetaInterface getRequiredFields(VariableSpace space) throws KettleException
    {
    	String realTableName = space.environmentSubstitute(tableName);
    	String realSchemaName = space.environmentSubstitute(schemaName);
 
        if (databaseMeta!=null)
        {
            Database db = new Database(loggingObject, databaseMeta);
            try
            {
                db.connect();

                if (!Const.isEmpty(realTableName))
                {
                    String schemaTable = databaseMeta.getQuotedSchemaTableCombination(realSchemaName, realTableName);

                    // Check if this table exists...
                    if (db.checkTableExists(schemaTable))
                    {
                        return db.getTableFields(schemaTable);
                    }
                    else
                    {
                        throw new KettleException(BaseMessages.getString(PKG, "OraBulkLoaderMeta.Exception.TableNotFound"));
                    }
                }
                else
                {
                    throw new KettleException(BaseMessages.getString(PKG, "OraBulkLoaderMeta.Exception.TableNotSpecified"));
                }
            }
            catch(Exception e)
            {
                throw new KettleException(BaseMessages.getString(PKG, "OraBulkLoaderMeta.Exception.ErrorGettingFields"), e);
            }
            finally
            {
                db.disconnect();
            }
        }
        else
        {
            throw new KettleException(BaseMessages.getString(PKG, "OraBulkLoaderMeta.Exception.ConnectionNotDefined"));
        }

    }

    /**
     * @return the schemaName
     */
    public String getSchemaName()
    {
        return schemaName;
    }

    /**
     * @param schemaName the schemaName to set
     */
    public void setSchemaName(String schemaName)
    {
        this.schemaName = schemaName;
    }

	public String getBadFile() {
		return badFile;
	}

	public void setBadFile(String badFile) {
		this.badFile = badFile;
	}

	public String getControlFile() {
		return controlFile;
	}

	public void setControlFile(String controlFile) {
		this.controlFile = controlFile;
	}

	public String getDataFile() {
		return dataFile;
	}

	public void setDataFile(String dataFile) {
		this.dataFile = dataFile;
	}

	public String getDiscardFile() {
		return discardFile;
	}

	public void setDiscardFile(String discardFile) {
		this.discardFile = discardFile;
	}

	public String getLogFile() {
		return logFile;
	}

	public void setLogFile(String logFile) {
		this.logFile = logFile;
	}
	
	public void setLoadAction(String action)
	{
	    this.loadAction = action;
	}

	public String getLoadAction()
	{
	    return this.loadAction;
	}

	public void setLoadMethod(String method)
	{
	    this.loadMethod = method;
	}

	public String getLoadMethod()
	{
	    return this.loadMethod;
	}

	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public String getDelimiter() {
		return ",";
	}

	public String getEnclosure() {
		return "\"";
	}

	public boolean isEraseFiles() {
		return eraseFiles;
	}

	public void setEraseFiles(boolean eraseFiles) {
		this.eraseFiles = eraseFiles;
	}
	
	public int getBindSizeAsInt(VariableSpace varSpace) {
    try {
      return Integer.valueOf(varSpace.environmentSubstitute(getBindSize()));
    } catch (NumberFormatException ex) {
      return DEFAULT_BIND_SIZE;
    }
  }

	public String getBindSize() {
		return bindSize;
	}

	public void setBindSize(String bindSize) {
		this.bindSize = bindSize;
	}
	
	public int getMaxErrorsAsInt(VariableSpace varSpace) {
    try {
      return Integer.valueOf(varSpace.environmentSubstitute(getMaxErrors()));
    } catch (NumberFormatException ex) {
      return DEFAULT_MAX_ERRORS;
    }
  }

	public String getMaxErrors() {
		return maxErrors;
	}

	public void setMaxErrors(String maxErrors) {
		this.maxErrors = maxErrors;
	}
	
	public int getReadSizeAsInt(VariableSpace varSpace) {
	  try {
	    return Integer.valueOf(varSpace.environmentSubstitute(getReadSize()));
	  } catch (NumberFormatException ex) {
	    return DEFAULT_READ_SIZE;
	  }
	}

	public String getReadSize() {
		return readSize;
	}

	public void setReadSize(String readSize) {
		this.readSize = readSize;
	}

	public String getDbNameOverride() {
		return dbNameOverride;
	}

	public void setDbNameOverride(String dbNameOverride) {
		this.dbNameOverride = dbNameOverride;
	}

  /**
   * @return the parallel
   */
  public boolean isParallel() {
    return parallel;
  }

  /**
   * @param parallel the parallel to set
   */
  public void setParallel(boolean parallel) {
    this.parallel = parallel;
  }	
}