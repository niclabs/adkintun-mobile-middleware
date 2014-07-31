package cl.niclabs.adkmobile.test.utils;

import cl.niclabs.adkmobile.utils.StringUtil;
import junit.framework.TestCase;

public class StringUtilTest extends TestCase {
	/**
	 * Test correct conversion from sql names to java names
	 */
	public void testFromSqlName() {
		assertEquals(StringUtil.fromSQLName("__This_Is_A_SQL_COLUMN"), "thisIsASqlColumn");
		assertEquals(StringUtil.fromSQLName("This_Is_A_SQL_COLUMN"), "thisIsASqlColumn");
		assertEquals(StringUtil.fromSQLName("THIS_IS_A_SQL_COLUMN"), "thisIsASqlColumn");
		assertEquals(StringUtil.fromSQLName("this_is_a_sql_column"), "thisIsASqlColumn");
		assertEquals(StringUtil.fromSQLName("_id"), "_id");
	}
	
	/**
	 * Test correct conversion from java names strings to sql names
	 */
	public void testToSqlName() {
		assertEquals(StringUtil.toSQLName("_id"), "_id");
		assertEquals(StringUtil.toSQLName("thisIsASqlColumn"), "THIS_IS_A_SQL_COLUMN");
	}
}
