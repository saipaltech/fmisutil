package org.saipal.fmisutil.util;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.stream.*;
import javax.xml.transform.dom.*;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.Session;
import org.hibernate.internal.SessionImpl;
import org.saipal.fmisutil.ApplicationContextProvider;
import org.saipal.fmisutil.auth.Authrepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringWriter;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Tuple;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@Service
@Scope("prototype")
public class RecordSet {
	Logger log = LoggerFactory.getLogger(RecordSet.class);
	private ResultSetMetaData m_MetaData;
	ResultSet m_Result;
	private Map<String, Integer> m_Col;
	private int p = -1;
	public int pagesize = 0;
	public int recordCount = 0;
	public int state = 0;
	private String m_RecentSQL = "";
	//FieldClass m_fields;
	public FieldClass fields;
	//public Map<String, Integer>  m_Col;
	public Map<String,String> m_colEx;
	
	public int dataSource=1;//1=Database, 2= JSON Schema, 3=REST API
	public String schemaURL="/openSQL";
	
	private Map<String,Object> json=new HashMap<String,Object>();
	
	private JSONArray schema = new JSONArray();
	private JSONArray data = new JSONArray();
	//public JSONArray status = new JSONArray();
	public clsStatus status=new clsStatus();
	private JSONObject m=new JSONObject();
	
	private boolean useResultSet=false;
	
	public RecordSet() {
		m_Col = new HashMap<String, Integer>();
		//m_fields = new FieldClass();
		status.setStatus(0,2, "SQL is not executed yet.");
	}
	
	public JSONArray getData() {
		return data;
	}
	public JSONArray getSchema() {
		return schema;
	}
	public JSONArray getStatus() {
		return status.getStatusArray();
	}
	private void raiseError(String msg) throws Exception {
		throw new Exception(msg); 
	}
	public void close() {
		this.p = -1;
		this.recordCount = 0;
		this.state = 0;
		// this.m_fields=null;
		// this.m_MetaData=null;
		this.m_RecentSQL = "";
		// this.m_Col=null;
	}

	public String getSQL() {
		return this.m_RecentSQL;
	}

	public void open(String sql, Connection c) {
		this.dataSource=1;
		this.m_RecentSQL = sql;
		if(sql.isBlank()) {
			this.state=0;
			this.status.setStatus(0, 1, "Unexpected blank SQL");
			return;
		}
		fields = new DBfields();
		ResultSet rs = null;
		ResultSetMetaData rsmd;
		
		try {
			Statement st = c.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
			rs = st.executeQuery(sql);
			rsmd = rs.getMetaData();
			
			this.m_MetaData = rsmd;
			this.fields.setMetaData();
			this.m_Result = rs;
			this.fields.setRow();
			if (rs.last() == true) {
				this.p = -1;
				this.recordCount = rs.getRow();
				if (this.recordCount > 0) {
					this.p = 0;
				}
			} else {
				
				this.p = -1;
				this.recordCount = 0;
			}
			this.state = 1;
			this.status.setStatus(1, 0, "Success");
					

		} catch (SQLException ex) {
			
			this.state = 0;
			this.status.setStatus(0, ex.getErrorCode(), ex.getMessage());
			ex.printStackTrace();
		}
		
		
		
	}

	public void open(String sql, List<String> args, Connection c) {
		this.dataSource=1;
		if(sql.isBlank()) {
			this.state=0;
			this.status.setStatus(0, 1, "Unexpected blank SQL");
			return;
		}
		fields = new DBfields();
		this.m_RecentSQL = sql;
		ResultSet rs = null;
		ResultSetMetaData rsmd;
		try {
			PreparedStatement stmt = c.prepareStatement(sql, ResultSet.TYPE_SCROLL_SENSITIVE,
					ResultSet.CONCUR_READ_ONLY);
			for (int i = 0; i < args.size(); i++) {
				stmt.setString(i + 1, args.get(i));
			}

			rs = stmt.executeQuery();
			rsmd = rs.getMetaData();
			this.m_MetaData = rsmd;
			this.fields.setMetaData();
					

			/*for (int i = 1; i <= rsmd.getColumnCount(); i++) {
				m_Col.put(rsmd.getColumnLabel(i).toLowerCase(), (i - 1));
			}*/
			this.m_Result = rs;
			this.fields.setRow();
			if (rs.last() == true) {
				this.p = -1;
				this.recordCount = rs.getRow();
				if (this.recordCount > 0) {
					this.p = 0;
				}
			} else {
				this.p = -1;
				this.recordCount = 0;
			}
			this.state = 1;
			this.status.setStatus(1, 0, "Success");
			
		} catch (SQLException ex) {
			this.state = 0;
			this.status.setStatus(0, ex.getErrorCode(), ex.getMessage());
			ex.printStackTrace();
		}
	}
//Entity Manager
	public void open(String sql, List<String> args, EntityManager em) {
		//Session session = (Session)em.getDelegate();
		//SessionImpl sessionImpl = (SessionImpl) session;
      	//this.open(sql, args, sessionImpl.connection());

      	//New Code
    		Session hibernateSession = em.unwrap(Session.class);
    		
    		hibernateSession.doWork(connection -> {
    		this.open(sql, args,connection);
    		this.toREST();
    		this.dataSource=2;
    		this.fields=null;
    		this.fields=new APIfields();
    		});
	}

	public void open(String sql, EntityManager em) {
		//Session session = (Session)em.getDelegate();
		//SessionImpl sessionImpl = (SessionImpl) session;
       // this.open(sql, sessionImpl.connection());
        Session hibernateSession = em.unwrap(Session.class);
        
		hibernateSession.doWork(connection -> {
			this.open(sql, connection);
			this.toREST();
    		this.dataSource=2;
    		this.fields=null;
    		this.fields=new APIfields();
		});
	}
	

	// ConnectionClass Dependent Function Begin
	/*
	 public void open(String sql, List<String> args, DbConnection c) {
		Connection cn = c.getConnection();
		this.open(sql, args, cn);
	}

	public void open(String sql, DbConnection c) {
		Connection cn = c.getConnection();
		this.open(sql, cn);
	}
	*/

	// DB object as parameter
	public void open(String sql, List<String> args, DB c) {
			//for JDBC Connection
			//Connection cn=c.getConnection();
			//this.open(sql, args, cn);
		
			//For EntityManager
			EntityManager cn=(EntityManager) c.getConnection();
			this.open(sql, args, cn);
		
	}

	public void open(String sql, DB c) {
		//for JDBC Connection
		//Connection cn=c.getConnection();
		//this.open(sql, args, cn);
	
		//For EntityManager
		EntityManager cn=(EntityManager) c.getConnection();
		this.open(sql,cn);
	}

	
	// ConnectionClass Dependent Function End

	public FieldClass fields(int index) {
		FieldClass ret = null;
		if(index<0 || index>=this.fields.count()) {
			return ret;
		}
		this.fields.setColIndex(index);
		/*ret=this.fields;
			if (p < 0)
			{
				return ret;
			}
			if (p >= this.recordCount)
				return ret;
		*/
		
		//this.m_fields.colindex = index;
		this.fields.setRowIndex(this.p);
		//this.m_fields.rowindex = this.p;
		// this.m_fields.setRow(this.m_Row.get(p+1));
		ret=this.fields;
		return ret;
	}

	public FieldClass fields(String colname){
		int index = 0;
		FieldClass ret=null;
		if(this.dataSource==1) {
		if (!this.m_Col.isEmpty()) {
			try {
				index = this.m_Col.get(colname.toLowerCase());
			} catch (NullPointerException e) {
				try {
					raiseError("Invalid Column Name '"+colname+"'");
				} catch (Exception e1) {
					
					e1.printStackTrace();
				}
				index = 0;
				
			}
			ret= this.fields(index);
		} else {
			try {
				raiseError("Invalid Column Name '"+colname+"'");
			} catch (Exception e1) {
				
				e1.printStackTrace();
			}
			ret= this.fields(0);
			
		}
		}
		else {
			index=this.m_Col.get(colname.toLowerCase());
			ret= this.fields(index);
		}

			return ret;
		// this.m_fields.setRow(this.m_Row.get(index));

		// return this.m_fields;
	}

	public void moveFirst() {
		if (this.recordCount > 0)
			this.p = 0;
	}

	public void moveLast() {
		this.p = this.recordCount - 1;
	}

	public void moveNext() {
		if (this.p <= this.recordCount)
			this.p++;
	}

	public void movePrevious() {
		if (this.p >= 0)
			this.p--;
	}

	public boolean BOF() {
		if (this.recordCount <= 0)
			return true;
		else if (this.p < 0)
			return true;
		else
			return false;
	}

	public boolean EOF() {
		if (this.recordCount <= 0)
			return true;
		else if (this.p >= this.recordCount)
			return true;
		else
			return false;
	}

	public int rowCount() {
		int ret = this.recordCount;
		return ret;
	}

	public int rowIndex() {
		return this.p;
	}

	public void moveto(int i) {
		this.p = i;
	}

	public String esc(String s) {
		String ret = "";
		ret = s.replaceAll("\\'", "''");
		return ret;
	}

	public Map<String,Object> toREST() {
		if(this.state!=1) {
			
			json.put("status",status.getStatusArray().toString());
			json.put("schema", schema.toString());
			json.put("data", data.toString());
			return json;
		}
		if(this.dataSource==2) {
			return json;
		}
		int colIndex = 0;
		try {
			
			int cIndex = this.rowIndex();
			if (this.state == 1 && this.recordCount > 0) {
				// this.moveto(0);
				this.m_Result.absolute(0);
				while (this.m_Result.next()) {
					colIndex++;
					int numColumns = this.m_MetaData.getColumnCount();
					
					JSONObject obj=new JSONObject();
					for (int i = 1; i < numColumns + 1; i++) {
						String column_name = this.m_MetaData.getColumnName(i);
						if(colIndex==1) {
							
							JSONObject objs=new JSONObject();
							objs.put("name", column_name);
							objs.put("order", i);
							//objs.put("datatype", this.m_MetaData.getColumnType(i));
							objs.put("datatype", this.m_MetaData.getColumnTypeName(i));
							objs.put("maxlength", this.m_MetaData.getPrecision(i));
							objs.put("scale", this.m_MetaData.getScale(i));
							objs.put("isnull", this.m_MetaData.isNullable(i));
							//objs.put("schemaname", this.m_MetaData.getSchemaName(i));
							//objs.put("tablename", this.m_MetaData.getTableName(i));
							//schema.put(objs);
							schema.put(objs);
						}
						
						Object Value=null;
						try {
								Value=this.m_Result.getObject(column_name);
						}
						catch(SQLException ex)
						{
							Value="";
						}
						if(Value==null)
							Value="";
						obj.put(column_name, Value);
					}
					data.put(obj);
					//data.put(obj);

				}
				json.put("status", status.getStatusArray().toString());
				json.put("schema",schema.toString());
				json.put("data",data.toString());
				this.m_Result.absolute(cIndex + 1);
			}
			else{
				int numColumns = this.m_MetaData.getColumnCount();
				for (int i = 1; i < numColumns + 1; i++) {
					String column_name = this.m_MetaData.getColumnName(i);
					
						JSONObject objs = new JSONObject();
						objs.put("name", column_name);
						objs.put("order", i);
						objs.put("datatype", this.m_MetaData.getColumnTypeName(i));
						objs.put("maxlength", this.m_MetaData.getPrecision(i));
						objs.put("scale", this.m_MetaData.getScale(i));
						objs.put("isnull", this.m_MetaData.isNullable(i));
						schema.put(objs);
				}
				json.put("status", status.getStatusArray().toString());
				json.put("schema",schema.toString());
				json.put("data",data.toString());
			}
		} catch (JSONException jx) {
			jx.printStackTrace();
		} catch (SQLException e) {

			e.printStackTrace();
		}
		//return json.toString();
		return json;
	}
	
	public JSONArray toJSONArray() {
		JSONArray Ljson = new JSONArray();
		if(this.dataSource==2) {
			return this.data;
		}
			int cIndex = this.rowIndex();
			if (this.state == 1 && this.recordCount > 0) {
				this.moveto(0);
				while (!this.EOF()) {
					
					int numColumns = this.fields.count();
					JSONObject obj = new JSONObject();
					for (int i = 0; i < numColumns; i++) {
						try {
							obj.put(this.fields(i).name(), this.fields(i).value());
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
					Ljson.put(obj);
					this.moveNext();
				}
				this.moveto(cIndex);
			}
		return Ljson;
	}

	public String toJSON() {
		String ret = "";
		ret = this.toJSONArray().toString();
		return ret;
	}

	public Document toXMLDocument() {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = null;
		try {
			builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		Document doc = builder.newDocument();
		Element results = doc.createElement("Results");
		doc.appendChild(results);

		int cIndex = this.rowIndex();

		if (this.state == 1 && this.recordCount > 0) {
			int colCount = this.recordCount;
			this.moveto(0);
				while (!this.EOF()) {
					Element row = doc.createElement("Row");
					results.appendChild(row);
					for (int i = 0; i < colCount; i++) {
						Element node = doc.createElement(this.fields(i).name());
						node.appendChild(doc.createTextNode(this.fields(i).value()));
						row.appendChild(node);
					}
					this.moveNext();
				}
				this.moveto(cIndex);
		}
		return doc;
	}

	
	public String toXML() {
		String ret = "";
		Document doc = null;
		doc = this.toXMLDocument();
		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer transformer;
		try {
			transformer = tf.newTransformer();
			StringWriter writer = new StringWriter();
			transformer.transform(new DOMSource(doc), new StreamResult(writer));
			String xmlString = writer.getBuffer().toString();
			ret = xmlString;
		} catch (TransformerException e) {
			// System.out.println("parsing error");
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return ret;

	}

	public List<Tuple> toTuple() {
		List<Tuple> tList = new ArrayList<>();
 		int cIndex = this.rowIndex();
			this.moveto(0);
			if (this.state == 1 && this.recordCount > 0) {
				while(!this.EOF()) {
					Tuple t;
					Map<String, Object> val=new HashMap<>();
						for (int i = 0; i <this.recordCount; i++) {
							val.put(this.fields(i).name(), this.fields(i).value());
						}
					t= (Tuple) val;
					tList.add(t);
					this.moveNext();
				}
				this.moveto(cIndex);
			}
		return tList;
	}
	
	public String toHTML() {
		StringBuilder  ret=new StringBuilder ();
		int cIndex = this.rowIndex();
		if (this.state == 1) {
			int colCount = 0;
			colCount = this.fields.count();
				ret.append("<table><tr>");
				for (int i = 0; i < colCount; i++) {
					ret.append("<th>"+this.fields(i).name()+"</th>");
				}
				ret.append("</tr>");
				if(this.recordCount > 0) {
					this.moveto(0);
					while(!this.EOF()) {
						ret.append("<tr>");
						for (int i = 0; i < colCount; i++) {
							ret.append("<td>"+this.fields(i).value()+"</td>");
						}
						ret.append("</tr>");
						this.moveNext();
					}
			}
		this.moveto(cIndex);
		}
		return ret.toString();
	}

	/*public ExcelReport toExcel() {
		int cIndex = this.rowIndex();
		ExcelReport rm=new ExcelReport(this.fields.count);
		ExcelRow r=new ExcelRow();
				
		if (this.state == 1 && this.recordCount > 0) {
			int colCount = 0;
			try {
				colCount = this.m_MetaData.getColumnCount();

			} catch (SQLException e) {
				e.printStackTrace();
			}
			try {
				
				
				for (int i = 1; i <= colCount; i++) {
					String column_name = this.m_MetaData.getColumnName(i);
					r.addColumn(new ExcelCell(column_name));
				}
				rm.addHeadRow(r);
				
				this.m_Result.absolute(0);

				while (this.m_Result.next()) {
					ExcelRow rd=new ExcelRow();
					for (int i = 1; i <= colCount; i++) {
						String value = "";
						if (this.m_Result.getObject(i) != null)
							value = this.m_Result.getObject(i).toString();
						
						rd.addColumn(new ExcelCell(value));
					}
					rm.addRow(rd);
				}
			
				this.m_Result.absolute(cIndex + 1);

			} catch (SQLException e) {

				e.printStackTrace();
			}
		}
		
		
		return rm;
	}
	
	public ExcelReport toExcel(boolean sn) {
		int cIndex = this.rowIndex();
		ExcelReport rm=new ExcelReport(this.fields.count);
		ExcelRow r=new ExcelRow();
				
		if (this.state == 1 && this.recordCount > 0) {
			int colCount = 0;
			try {
				colCount = this.m_MetaData.getColumnCount();

			} catch (SQLException e) {
				e.printStackTrace();
			}
			try {
				
				if(sn) {
					r.addColumn(new ExcelCell("SN"));
				}
				for (int i = 1; i <= colCount; i++) {
					String column_name = this.m_MetaData.getColumnName(i);
					r.addColumn(new ExcelCell(column_name));
				}
				rm.addHeadRow(r);
				
				this.m_Result.absolute(0);
				int count=0;
				while (this.m_Result.next()) {
					count++;
					ExcelRow rd=new ExcelRow();
					if(sn) {
						rd.addColumn(new ExcelCell(count+""));
					}
					for (int i = 1; i <= colCount; i++) {
						String value = "";
						if (this.m_Result.getObject(i) != null)
							value = this.m_Result.getObject(i).toString();
						
						rd.addColumn(new ExcelCell(value));
					}
					rm.addRow(rd);
				}
			
				this.m_Result.absolute(cIndex + 1);

			} catch (SQLException e) {

				e.printStackTrace();
			}
		}
		
		
		return rm;
	}
*/
	public void open(String sql,String ServiceName) {
		
		String url="lb://"+ServiceName +this.schemaURL;
		
		this.dataSource=2;

		
		Authrepo ar=ApplicationContextProvider.getBean(Authrepo.class);
		Map<String,List<String>> data=new HashMap();
		List<String> l1=new ArrayList();
		l1.add(sql);
		data.put("sql",l1);
		
		
		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(ar.token);
		headers.setContentType(MediaType.APPLICATION_JSON);
		
		
		HttpEntity<Object> requestEntity = new HttpEntity<>(data, headers);
		RestTemplate rt=ApplicationContextProvider.getBean(RestTemplate.class);//new RestTemplate();
		
		
		ResponseEntity<apiResult> r= rt.postForEntity(url, requestEntity, apiResult.class); 
		
		if(r.getStatusCodeValue()==200) {
			fields = new APIfields();
			
			apiResult result=r.getBody();
			
			this.schema=result.getSchema();
			this.status.setStatus(result.getStatus());
			this.data=result.getData();
			
			json.put("status",status.getStatusArray().toString());
			json.put("schema", schema.toString());
			json.put("data", data.toString());
			
			this.fields.setMetaData();
			this.fields.setRow();
			
			
			this.state=this.status.getStatus();
			this.recordCount=this.data.length();
			
			if(this.recordCount>0) {
				this.p=0;
			}
			else {
				this.p=-1;
			}
			if(this.status.errorNumber==0)		
				this.state = 1;
			
		}
		else {
			this.state=0;
			
		}

	}
public void open(String sql,List<String> args,String ServiceName) {
		
		String url="lb://"+ServiceName +this.schemaURL;
		
		this.dataSource=2;

		
		Authrepo ar=ApplicationContextProvider.getBean(Authrepo.class);
		Map<String,List<String>> data=new HashMap<String,List<String>>();
		List<String> l1=new ArrayList<String>();
		l1.add(sql);
		data.put("sql",l1);
		data.put("args", args);
		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(ar.token);
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<Object> requestEntity = new HttpEntity<>(data, headers);
		RestTemplate rt=ApplicationContextProvider.getBean(RestTemplate.class);//new RestTemplate();
		ResponseEntity<apiResult> r= rt.postForEntity(url, requestEntity, apiResult.class); 
		if(r.getStatusCodeValue()==200) {
			fields = new APIfields();
			apiResult result=r.getBody();
			this.schema=result.getSchema();
			this.status.setStatus(result.getStatus());
			this.data=result.getData();
			
			json.put("status",status.getStatusArray().toString());
			json.put("schema", schema.toString());
			json.put("data", data.toString());
			
			this.fields.setMetaData();
			this.fields.setRow();
			this.state=this.status.getStatus();
			this.recordCount=this.data.length();
			if(this.recordCount>0) {
				this.p=0;
			}
			else {
				this.p=-1;
			}
			if(this.status.errorNumber==0)		
				this.state = 1;
		}
		else {
			this.state=0;
		}
	}

/*
	public void openAPI(String url,String data) {
		System.out.println(url);
	}
		
*/
	
	static class apiResult{
		public  Object schema="";
		public  Object data="";
		public  Object status="";
		public JSONArray getStatus() {
			try {
				return new JSONArray(status.toString());
			} catch (JSONException e) {
				e.printStackTrace();
				return null;
			}
		}
		public JSONArray getSchema() {
			try {
				return new JSONArray(schema.toString());
			} catch (JSONException e) {
				e.printStackTrace();
				return null;
			}
		}
		public JSONArray getData() {
			try {
				return new JSONArray(data.toString());
			} catch (JSONException e) {
				e.printStackTrace();
				return null;
			}
		}
	}//end apiResult
	
	/**
	 * STATUS Class which is used to represent error and info
	 */
	public class clsStatus{
		int status=0;
		int errorNumber=2;
		String message="SQL is not executed";

		protected void setStatus(int vstatus,int verrorNumber,String vmessage) {
			this.status=vstatus;
			this.errorNumber=verrorNumber;
			this.message=vmessage;
			
		}
		protected void setStatus(JSONArray st) {
			try {
				JSONObject jo=new JSONObject(st.get(0).toString());
				this.status=(int) jo.get("status");
				this.errorNumber=(int) jo.get("errorNumber");
				this.message=(String) jo.get("message");
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
		}
		
		public JSONArray getStatusArray() {
			JSONArray statusArray = new JSONArray();
			JSONObject sobj=new JSONObject();
			try {
				sobj.put("status", this.status);
				sobj.put("errorNumber", this.errorNumber);
				sobj.put("message", this.message);
				
			} catch (JSONException e) {
				
				e.printStackTrace();
			}
			statusArray.put(sobj);
			return statusArray;
		}
		
		public JSONObject getStatusObject() {
			JSONObject sobj=new JSONObject();
			try {
				sobj.put("status", this.status);
				sobj.put("errorNumber", this.errorNumber);
				sobj.put("message", this.message);
				
			} catch (JSONException e) {
				
				e.printStackTrace();
			}
			return sobj;
		}
		
		public Map<String,Object> getStatusMap(){
			Map<String,Object> ret=new HashMap();
			ret.put("status", this.status);
			ret.put("errorNumber", this.errorNumber);
			ret.put("message", this.message);
			return ret;
		}
		
		public int getStatus() {
			return this.status;
		}		
		public int getErrorNumber() {
			return this.errorNumber;
		}
		public String getMessage() {
			return this.message;
		}
	}

	public interface FieldClass{
		public void setColIndex(int cindex);
		public void setRowIndex(int rindex);
		public int count();
		//public void setCount(int c);
		
		public void setMetaData();
		public void setRow();
		public String value();
		public Object object();
		public int getInt();
		public long getLong();
		public BigDecimal getBigDecimal();
		public short getShort();
		public SQLXML getSQLXML();
		public byte getByte();
		public float getFloat();
		public Double getDouble();
		public String getNString();
		public String getString();
		public Blob getBlob();
		public Date getDate();
		public boolean getBoolean();
		public Timestamp getTimestamp();
		public String name();
		public String type();
	}
	
	public class DBfields implements FieldClass {
		private int colindex=0;
		private int rowindex=0;
		private int m_count=0;
		/*
		 	private ResultSetMetaData m_MetaData;
			ResultSet m_Result;
		 */
		
		//private ResultSetMetaData m_MetaData;
		//ResultSet m_Row;
		public Map<String, Integer>  m_Col;
		
		private int p=0;
		public DBfields()
		{		
			m_Col=new HashMap<String, Integer>();
			m_colEx=new HashMap<String,String>();
			this.p=-1;
		}
		public void setColIndex(int cindex) {
			this.colindex=cindex;
		}
		public void setRowIndex(int rindex) {
			this.rowindex=rindex;
		}
		public int count() {
			return this.m_count;
		}
		private void raiseError(String msg) throws Exception {
			throw new Exception(msg); 
		}
		public void setMetaData() {
			//RecordSet.this.m_MetaData
			//this.m_MetaData=m;
			try {
				for (int i = 1; i <= RecordSet.this.m_MetaData.getColumnCount(); i++) {
					m_Col.put(RecordSet.this.m_MetaData.getColumnLabel(i).toLowerCase(), (i-1));
					m_colEx.put("R"+(i-1), RecordSet.this.m_MetaData.getColumnLabel(i).toString());
				}
				this.m_count=RecordSet.this.m_MetaData.getColumnCount();
			} catch (SQLException ex) {
				ex.printStackTrace();
			}
		}
		public void setRow() {
			//this.m_Row=r;
			this.p=0;
		}
		
		public String value() {
			Object ret=null;
			if(this.colindex<0)
				return "";
			if(this.colindex>this.m_count-1)
				return "";
			try {
				
				RecordSet.this.m_Result.absolute(this.rowindex+1);
				ret=RecordSet.this.m_Result.getObject(this.colindex+1);
			}catch (SQLException ex) {
				ex.printStackTrace();
			}
			if(ret==null)
				return "";
			else
				return ret.toString();
		}
		
		public Object object() {
			Object ret=null;
			if(this.colindex<0)
				return ret;
			if(this.colindex>this.m_count-1)
				return ret;
			try {
				RecordSet.this.m_Result.absolute(this.rowindex+1);
				ret=RecordSet.this.m_Result.getObject(this.colindex+1);
			}catch (SQLException ex) {
				ex.printStackTrace();
			}
			return ret;
		}
		
		public int getInt() {
			int ret=0;
			if(this.colindex<0)
				return 0;
			if(this.colindex>this.m_count-1)
				return 0;
			try {
				RecordSet.this.m_Result.absolute(this.rowindex+1);
				ret=RecordSet.this.m_Result.getInt(this.colindex+1);
			}catch (SQLException ex) {
				ex.printStackTrace();
			}
			
			return ret;
		}
		public long getLong() {
			long ret=0;
			if(this.colindex<0)
				return 0;
			if(this.colindex>this.m_count-1)
				return 0;
			try {
				RecordSet.this.m_Result.absolute(this.rowindex+1);
				ret=RecordSet.this.m_Result.getLong(this.colindex+1);
			}catch (SQLException ex) {
				ex.printStackTrace();
			}
			
			return ret;
		}
		public BigDecimal getBigDecimal() {
			BigDecimal ret=null;
			if(this.colindex<0)
				return null;
			if(this.colindex>this.m_count-1)
				return null;
			try {
				RecordSet.this.m_Result.absolute(this.rowindex+1);
				ret=RecordSet.this.m_Result.getBigDecimal(this.colindex+1);
			}catch (SQLException ex) {
				ex.printStackTrace();
			}
			
			return ret;
		}
		
		public short getShort() {
			short ret=0;
			if(this.colindex<0)
				return 0;
			if(this.colindex>this.m_count-1)
				return 0;
			try {
				RecordSet.this.m_Result.absolute(this.rowindex+1);
				ret=RecordSet.this.m_Result.getShort(this.colindex+1);
			}catch (SQLException ex) {
				ex.printStackTrace();
			}
			
			return ret;
		}
		public SQLXML getSQLXML() {
			SQLXML ret=null;
			if(this.colindex<0)
				return null;
			if(this.colindex>this.m_count-1)
				return null;
			try {
				RecordSet.this.m_Result.absolute(this.rowindex+1);
				ret=RecordSet.this.m_Result.getSQLXML(this.colindex+1);
			}catch (SQLException ex) {
				ex.printStackTrace();
			}
			
			return ret;
		}
		public byte getByte() {
			byte ret=0;
			if(this.colindex<0)
				return 0;
			if(this.colindex>this.m_count-1)
				return 0;
			try {
				RecordSet.this.m_Result.absolute(this.rowindex+1);
				ret=RecordSet.this.m_Result.getByte(this.colindex+1);
			}catch (SQLException ex) {
				ex.printStackTrace();
			}
			
			return ret;
		}
		
		public float getFloat() {
			float ret=0;
			if(this.colindex<0)
				return 0;
			if(this.colindex>this.m_count-1)
				return 0;
			try {
				RecordSet.this.m_Result.absolute(this.rowindex+1);
				ret=RecordSet.this.m_Result.getFloat(this.colindex+1);
			}catch (SQLException ex) {
				ex.printStackTrace();
			}
			
			return ret;
		}
		public Double getDouble() {
			Double ret=0.00;
			if(this.colindex<0)
				return 0.00;
			if(this.colindex>this.m_count-1)
				return 0.00;
			try {
				RecordSet.this.m_Result.absolute(this.rowindex+1);
				ret=RecordSet.this.m_Result.getDouble(this.colindex+1);
			}catch (SQLException ex) {
				ex.printStackTrace();
			}
			
			return ret;
		}
		public String getNString() {
			String ret="";
			if(this.colindex<0)
				return "";
			if(this.colindex>this.m_count-1)
				return "";
			try {
				RecordSet.this.m_Result.absolute(this.rowindex+1);
				ret=RecordSet.this.m_Result.getNString(this.colindex+1);
			}catch (SQLException ex) {
				ex.printStackTrace();
			}
			
			return ret;
		}
		
		public String getString() {
			String ret="";
			if(this.colindex<0)
				return "";
			if(this.colindex>this.m_count-1)
				return "";
			try {
				RecordSet.this.m_Result.absolute(this.rowindex+1);
				ret=RecordSet.this.m_Result.getString(this.colindex+1);
			}catch (SQLException ex) {
				ex.printStackTrace();
			}
			
			return ret;
		}
		
		public Blob getBlob() {
			Blob ret=null;
			if(this.colindex<0)
				return null;
			if(this.colindex>this.m_count-1)
				return null;
			try {
				RecordSet.this.m_Result.absolute(this.rowindex+1);
				ret=RecordSet.this.m_Result.getBlob(this.colindex+1);
			}catch (SQLException ex) {
				ex.printStackTrace();
			}
			return ret;
		}
		
		public Date getDate() {
			Date ret=new Date();
			if(this.colindex<0)
				return null;
			if(this.colindex>this.m_count-1)
				return null;
			try {
				RecordSet.this.m_Result.absolute(this.rowindex+1);
				ret=RecordSet.this.m_Result.getDate(this.colindex+1);
			}catch (SQLException ex) {
				ex.printStackTrace();
			}
			return ret;
		}
		
		public boolean getBoolean() {
			boolean ret=false;
			if(this.colindex<0)
				return false;
			if(this.colindex>this.m_count-1)
				return false;
			try {
				RecordSet.this.m_Result.absolute(this.rowindex+1);
				ret=RecordSet.this.m_Result.getBoolean(this.colindex+1);
			}catch (SQLException ex) {
				ex.printStackTrace();
			}
			return ret;
		}
		public Timestamp getTimestamp() {
			Timestamp ret=null;
			if(this.colindex<0)
				return null;
			if(this.colindex>this.m_count-1)
				return null;
			try {
				RecordSet.this.m_Result.absolute(this.rowindex+1);
				ret=RecordSet.this.m_Result.getTimestamp(this.colindex+1);
			}catch (SQLException ex) {
				ex.printStackTrace();
			}
			return ret;
		}
		
		public String name()
		{
			int index=this.colindex+1;
			String ret="";
			try {
				ret= RecordSet.this.m_MetaData.getColumnLabel(index);
				}catch (SQLException ex) {
					ex.printStackTrace();
				}
			return ret;
		}
		
		public String type()
		{
			int index=this.colindex+1;
			String ret="";
			try {
				ret= RecordSet.this.m_MetaData.getColumnTypeName(index);
				}catch (SQLException ex) {
					ex.printStackTrace();
				}
			return ret;
		}
				
	}
	
	public class APIfields implements FieldClass {
		private int colindex=0;
		private int rowindex=0;
		public int count=0;
		//private ResultSetMetaData m_MetaData;
		//ResultSet m_Row;
		//public Map<String, Integer>  m_Col;
		
		private int p=0;
		public APIfields()
		{		
			m_Col=new HashMap<String, Integer>();
			m_colEx=new HashMap<String,String>();
			this.p=-1;
		}
		public void setColIndex(int cindex) {
			this.colindex=cindex;
		}
		public void setRowIndex(int rindex) {
			this.rowindex=rindex;
		}
		public int count() {
			return count;
		}
		private void raiseError(String msg) throws Exception {
			throw new Exception(msg); 
		}
		public void setMetaData() {
	
			
			this.count=RecordSet.this.schema.length();
			for(int i=0;i<this.count;i++) {
				try {
					JSONObject col=new JSONObject(RecordSet.this.schema.get(i).toString());
					int order=Integer.parseInt(col.get("order").toString());
					order=order-1;
					m_Col.put(col.get("name").toString().toLowerCase(),order);
					m_colEx.put("R"+order,col.get("name").toString());
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			
		}
		public void setRow() {
			this.p=0;
		}
		
		public String value() {
			Object ret=null;
			if(this.colindex<0)
				return "";
			if(this.colindex>this.count-1)
				return "";
			
			JSONObject row;
			try {
				row = new JSONObject(RecordSet.this.data.get(this.rowindex).toString());
				ret=row.get(m_colEx.get("R"+this.colindex).toString());
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if(ret==null)
				return "";
			else
				return ret.toString();
		}
		
		public Object object() {
			Object ret=null;
			if(this.colindex<0)
				return "";
			if(this.colindex>this.count-1)
				return "";
			JSONObject row;
			try {
				row = new JSONObject(RecordSet.this.data.get(this.rowindex).toString());
				ret=row.get(m_colEx.get("R"+this.colindex).toString());
			} catch (JSONException e) {
				
				e.printStackTrace();
			}
			if(ret==null)
				return "";
			else
				return ret;
		}
		
		public int getInt() {
			Object ret=null;
			if(this.colindex<0)
				return 0;
			if(this.colindex>this.count-1)
				return 0;
			
			JSONObject row;
			try {
				row = new JSONObject(RecordSet.this.data.get(this.rowindex).toString());
				ret=row.get(m_colEx.get("R"+this.colindex).toString());
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if(ret==null)
				return 0;
			else
				return (int)ret;
		}
		public long getLong() {
			Object ret=null;
			if(this.colindex<0)
				return 0;
			if(this.colindex>this.count-1)
				return 0;
			
			JSONObject row;
			try {
				row = new JSONObject(RecordSet.this.data.get(this.rowindex).toString());
				ret=row.get(m_colEx.get("R"+this.colindex).toString());
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if(ret==null)
				return 0;
			else
				return (long)ret;
		}
		public BigDecimal getBigDecimal() {
			Object ret=null;
			if(this.colindex<0)
				return new BigDecimal("0");
			if(this.colindex>this.count-1)
				return new BigDecimal("0");
			
			JSONObject row;
			try {
				row = new JSONObject(RecordSet.this.data.get(this.rowindex).toString());
				ret=row.get(m_colEx.get("R"+this.colindex).toString());
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if(ret==null)
				return new BigDecimal("0");
			else
				return new BigDecimal(ret.toString());
		}
		
		public short getShort() {
			Object ret=null;
			if(this.colindex<0)
				return 0;
			if(this.colindex>this.count-1)
				return 0;
			
			JSONObject row;
			try {
				row = new JSONObject(RecordSet.this.data.get(this.rowindex).toString());
				ret=row.get(m_colEx.get("R"+this.colindex).toString());
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if(ret==null)
				return 0;
			else
				return (short)ret;
		}
		public SQLXML getSQLXML() {
			Object ret=null;
			if(this.colindex<0)
				return null;
			if(this.colindex>this.count-1)
				return null;
			
			JSONObject row;
			try {
				row = new JSONObject(RecordSet.this.data.get(this.rowindex).toString());
				ret=row.get(m_colEx.get("R"+this.colindex).toString());
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if(ret==null)
				return null;
			else
				return (SQLXML)ret;
		}
		public byte getByte() {
			Object ret=null;
			if(this.colindex<0)
				return 0;
			if(this.colindex>this.count-1)
				return 0;
			
			JSONObject row;
			try {
				row = new JSONObject(RecordSet.this.data.get(this.rowindex).toString());
				ret=row.get(m_colEx.get("R"+this.colindex).toString());
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if(ret==null)
				return 0;
			else
				return (byte)ret;
		}
		
		public float getFloat() {
			Object ret=null;
			if(this.colindex<0)
				return 0;
			if(this.colindex>this.count-1)
				return 0;
			
			JSONObject row;
			try {
				row = new JSONObject(RecordSet.this.data.get(this.rowindex).toString());
				ret=row.get(m_colEx.get("R"+this.colindex).toString());
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if(ret==null)
				return 0;
			else
				return (float)ret;
		}
		public Double getDouble() {
			Object ret=null;
			if(this.colindex<0)
				return 0.0;
			if(this.colindex>this.count-1)
				return 0.0;
			
			JSONObject row;
			try {
				row = new JSONObject(RecordSet.this.data.get(this.rowindex).toString());
				ret=row.get(m_colEx.get("R"+this.colindex).toString());
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if(ret==null)
				return 0.0;
			else
				return (Double)ret;
		}
		public String getNString() {
			Object ret=null;
			if(this.colindex<0)
				return "";
			if(this.colindex>this.count-1)
				return "";
			
			JSONObject row;
			try {
				row = new JSONObject(RecordSet.this.data.get(this.rowindex).toString());
				ret=row.get(m_colEx.get("R"+this.colindex).toString());
			} catch (JSONException e) {
				
				e.printStackTrace();
			}
			
			if(ret==null)
				return "";
			else
				return ret.toString();
		}
		
		public String getString() {
			Object ret=null;
			if(this.colindex<0)
				return "";
			if(this.colindex>this.count-1)
				return "";
			
			JSONObject row;
			try {
				row = new JSONObject(RecordSet.this.data.get(this.rowindex).toString());
				ret=row.get(m_colEx.get("R"+this.colindex).toString());
			} catch (JSONException e) {
				
				e.printStackTrace();
			}
			
			if(ret==null)
				return "";
			else
				return ret.toString();
		}
		
		public Blob getBlob() {
			Object ret=null;
			if(this.colindex<0)
				return null;
			if(this.colindex>this.count-1)
				return null;
			
			JSONObject row;
			try {
				row = new JSONObject(RecordSet.this.data.get(this.rowindex).toString());
				ret=row.get(m_colEx.get("R"+this.colindex).toString());
			} catch (JSONException e) {
				
				e.printStackTrace();
			}
			
			if(ret==null)
				return null;
			else
				return (Blob) ret;
		}
		
		public Date getDate() {
			Object ret=null;
			if(this.colindex<0)
				return null;
			if(this.colindex>this.count-1)
				return null;
			
			JSONObject row;
			try {
				row = new JSONObject(RecordSet.this.data.get(this.rowindex).toString());
				ret=row.get(m_colEx.get("R"+this.colindex).toString());
			} catch (JSONException e) {
				
				e.printStackTrace();
			}
			
			if(ret==null)
				return null;
			else
				return (Date) ret;
		}
		
		public boolean getBoolean() {
			Object ret=null;
			if(this.colindex<0)
				return false;
			if(this.colindex>this.count-1)
				return false;
			
			JSONObject row;
			try {
				row = new JSONObject(RecordSet.this.data.get(this.rowindex).toString());
				ret=row.get(m_colEx.get("R"+this.colindex).toString());
			} catch (JSONException e) {
				
				e.printStackTrace();
			}
			
			if(ret==null)
				return false;
			else
				return (boolean) ret;
		}
		public Timestamp getTimestamp() {
			Object ret=null;
			if(this.colindex<0)
				return null;
			if(this.colindex>this.count-1)
				return null;
			
			JSONObject row;
			try {
				row = new JSONObject(RecordSet.this.data.get(this.rowindex).toString());
				ret=row.get(m_colEx.get("R"+this.colindex).toString());
			} catch (JSONException e) {
				
				e.printStackTrace();
			}
			
			if(ret==null)
				return null;
			else
				return (Timestamp) ret;
		}
		
		public String name()
		{
			return m_colEx.get("R"+this.colindex).toString();
			
		}
		
		public String type()
		{
			String ret="";
			JSONObject t;
			try {
				t = new JSONObject(RecordSet.this.schema.get(this.colindex).toString());
				ret= t.getString("datatype");
			} catch (JSONException e) {
				
				e.printStackTrace();
			}
			return ret;
			
		}
				
	}
	
	/**
	 * For Excel 
	 * To use excel function imort following
	 * import org.saipal.fmis.excel.ExcelCell;
		import org.saipal.fmis.excel.ExcelReport;
		import org.saipal.fmis.excel.ExcelRow;
	 */
	/*
	public ExcelReport toExcel() {
		int cIndex = this.rowIndex();
		ExcelReport rm=new ExcelReport(this.fields.count);
		ExcelRow r=new ExcelRow();
				
		if (this.state == 1) {
			int colCount = this.fields.count();
			for (int i =0 ; i < colCount; i++) {
					r.addColumn(new ExcelCell(this.fields(i).name()));
				}
				rm.addHeadRow(r);
				
			if(this.recordCount > 0) {
				this.moveto(0);
				while (!this.EOF()) {
					ExcelRow rd=new ExcelRow();
					for (int i = 0; i < colCount; i++) {
						rd.addColumn(new ExcelCell(this.fields(i).value()));
					}
					rm.addRow(rd);
					this.moveNext();
				}
			
				this.moveto(cIndex);
			}
			
		}
		return rm;
	}
	
	public ExcelReport toExcel(boolean sn) {
		int cIndex = this.rowIndex();
		ExcelReport rm=new ExcelReport(this.fields.count);
		ExcelRow r=new ExcelRow();
				
		if (this.state == 1) {
			int colCount = this.fields.count();

				if(sn) {
					r.addColumn(new ExcelCell("SN"));
				}
				for (int i = 0; i < colCount; i++) {
					r.addColumn(new ExcelCell(this.fields(i).name()));
				}
				rm.addHeadRow(r);
				
				if(this.recordCount > 0) {
				this.m_Result.absolute(0);
				int count=0;
				while (!this.EOF()) {
					count++;
					ExcelRow rd=new ExcelRow();
					if(sn) {
						rd.addColumn(new ExcelCell(count+""));
					}
					for (int i = 1; i <= colCount; i++) {
						rd.addColumn(new ExcelCell(this.fields(i).value()));
					}
					rm.addRow(rd);
					this.moveNext();
				}
			
				this.moveto(cIndex);
				}
			
		}
		return rm;
	}
	*/
	 
}
