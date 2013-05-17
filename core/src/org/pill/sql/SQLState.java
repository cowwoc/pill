package org.pill.sql;

import com.google.common.collect.Maps;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SQLState codes according to the SQL:2008 standard.
 * <p/>
 * @author Gili Tzabari
 * @see http://en.wikipedia.org/wiki/SQL#cite_note-39
 * @see http://kb.askmonty.org/v/sqlstate-codes
 */
public enum SQLState
{
	/**
	 * Completion of the operation was successful and did not result in any type of warning or
	 * exception condition.
	 */
	SUCCESSFUL_COMPLETION("00000"),
	/**
	 * The operation completed with a warning.
	 */
	WARNING("01000"),
	/**
	 * Returned if you DELETE with and without a Cursor in the same transaction.
	 */
	WARNING_CURSOR_OPERATION_CONFLICT("01001"),
	/**
	 * There was an error during execution of the CLI function SQLDisconnect, but you won't be able to
	 * see the details because the SQLDisconnect succeeded.
	 */
	WARNING_DISCONNECT_ERROR("01002"),
	/**
	 * Null values were eliminated from the argument of a column function.
	 */
	WARNING_NULL_VALUE_ELIMINATED_IN_SET_FUNCTION("01003"),
	/**
	 * The value of a string was truncated when assigned to another string data type with a shorter
	 * length.
	 */
	WARNING_STRING_TRUNCATION_ON_READ("01004"),
	/**
	 * Every descriptor area has multiple IDAs. You need one IDA per Column of a result set, or one
	 * per parameter. Either reduce the number of Columns in the select list or reduce the number of
	 * ?s in the SQL statement as a whole.
	 */
	WARNING_INSUFFICIENT_ITEM_DESCRIPTOR_AREAS("01005"),
	/**
	 * A privilege was not revoked.
	 */
	WARNING_PRIVILEGE_NOT_REVOKED("01006"),
	/**
	 * A privilege was not granted.
	 */
	WARNING_PRIVILEGE_NOT_GRANTED("01007"),
	/**
	 * Suppose you say "CREATE TABLE ... CHECK (<condition>)", and the length of <condition> is larger
	 * than what can be stored in the INFORMATION_SCHEMA View, CHECK_CONSTRAINTS, in its CHECK_CLAUSE
	 * Column. The Table will still be created — this warning only means you won't be able to see the
	 * entire information about the Table when you look at INFORMATION_SCHEMA. See also SQLState 0100A
	 * and 0100B.
	 */
	WARNING_SEARCH_CONDITION_TOO_LONG("01009"),
	/**
	 * This is the same as warning 01009 except that instead of a search condition (as in a CHECK
	 * clause), you're using a query condition (usually SELECT). Thus, if you say "CREATE VIEW ..."
	 * with a very long query, the size of Column VIEW_DEFINITION in View VIEWS in INFORMATION_SCHEMA
	 * is a limiting factor.
	 */
	WARNING_QUERY_EXPRESSION_TOO_LONG("0100A"),
	/**
	 * This is the same as warning 01009 except that instead of a search condition (as in a CHECK
	 * clause), you're using a default value.
	 */
	WARNING_DEFAULT_VALUE_TOO_LONG("0100B"),
	/**
	 * One or more ad hoc result sets were returned from the procedure.
	 */
	WARNING_DYNAMIC_RESULT_SETS_RETURNED("0100C"),
	/**
	 * The cursor that was closed has been reopened on the next result set within the chain.
	 */
	WARNING_ADDITIONAL_RESULT_SETS_RETURNED("0100D"),
	/**
	 * The procedure returned too many result sets.
	 */
	WARNING_TOO_MANY_RESULT_SETS("0100E"),
	/**
	 * Returned if the character representation of the triggered SQL statement cannot be represented
	 * in the Information Schema without truncation.
	 */
	WARNING_STATEMENT_TOO_LONG("0100F"),
	/**
	 * Column cannot be mapped to XML.
	 */
	WARNING_COLUMN_MAPPING_TO_XML("01010"),
	/**
	 * The specified SQL-Java path was too long for information schema.
	 */
	WARNING_PATH_TOO_LONG("01011"),
	/**
	 * The maximum number of elements in the target array is less than the number of elements in the
	 * source array and the extra source elements are all NULL. The database will assign as many of
	 * the source element values to the target elements as is possible.
	 */
	WARNING_ARRAY_TRUNCATION("0102F"),
	/**
	 * One of the following exceptions occurred:
	 * <p/>
	 * <ol> <li>The result of the SELECT INTO statement or the subselect of the INSERT statement was
	 * an empty table.</li> <li>The number of rows identified in the searched UPDATE or DELETE
	 * statement was zero./<li> <li>The position of the cursor referenced in the FETCH statement was
	 * before the first row or after the last row of the result table.</li> <li>The fetch orientation
	 * is invalid.</li> </ol>
	 */
	NO_DATA("02000"),
	/**
	 * No additional result sets returned.
	 */
	NO_DATA_NO_ADDITIONAL_RESULTS("02001"),
	/**
	 * An error occurred while executing dynamic SQL.
	 */
	DYNAMIC_SQL_ERROR("07000"),
	/**
	 * You might encounter this error if you set the length of a descriptor, then
	 * <code>EXECUTE ... USING descriptor</code>. Often this exception results from consistency-check
	 * failure during SQLExecute.
	 */
	DYNAMIC_SQL_ERROR_USING_CLAUSE_MISMATCH_PARAMETERS("07001"),
	/**
	 * Often this exception results from consistency-check failure during SQLExecute.
	 * <p/>
	 * Sometimes this exception results from an incorrect number of parameters. See also SQLSTATE
	 * 07008.
	 */
	DYNAMIC_SQL_ERROR_USING_CLAUSE_MISMATCH_SPECIFICATIONS("07002"),
	/**
	 * Returned if prepared statement does not conform to the Format and Syntax Rules of a dynamic
	 * single row select statement.
	 */
	DYNAMIC_SQL_ERROR_CURSOR_SPECIFICATION("07003"),
	/**
	 * You cannot simply EXECUTE an SQL statement which has dynamic parameters — you also need to use
	 * a USING clause. See also SQLSTATE 07007.
	 */
	DYNAMIC_SQL_ERROR_USING_CLAUSE_REQUIRED_FOR_PARAMETERS("07004"),
	/**
	 * The statement name of the cursor identifies a prepared statement that cannot be associated with
	 * a cursor.
	 */
	DYNAMIC_SQL_ERROR_PREPARED_STATEMENT_NOT_A_CURSOR_SPECIFICATION("07005"),
	/**
	 * An input variable, transition variable, or parameter marker cannot be used, because of its data
	 * type.
	 */
	DYNAMIC_SQL_ERROR_DATA_TYPE_VIOLATION("07006"),
	/**
	 * You cannot simply EXECUTE an SQL statement which has result fields — you also need to use a
	 * USING clause. See also SQLSTATE 07004.
	 */
	DYNAMIC_SQL_ERROR_USING_CLAUSE_REQUIRED_FOR_RESULT_FIELDS("07007"),
	/**
	 * Using the embedded SQL ALLOCATE DESCRIPTOR statement, you allocated a 5-item descriptor. Now
	 * you are trying to use the sixth item in that descriptor. See also SQLSTATE 07009.
	 */
	DYNAMIC_SQL_ERROR_INVALID_DESCRIPTOR_COUNT("07008"),
	/**
	 * you are using a CLI descriptor function (such as SQLBindCol or SQLBindParameter) and the Column
	 * number is less than 1 or greater than the maximum number of Columns. Or, you are using the
	 * embedded SQL ALLOCATE DESCRIPTOR statement with a size which is less than 1 or greater than an
	 * implementation-defined maximum. See also SQLSTATE 07008.
	 */
	DYNAMIC_SQL_ERROR_INVALID_DESCRIPTOR_INDEX("07009"),
	/**
	 * The supplied input or output arguments could not be transformed to the types expected by the
	 * dynamic SQL script.
	 */
	DYNAMIC_SQL_ERROR_TRANSFORM_FUNCTION_VIOLATION("0700B"),
	/**
	 * Attempted to GET DESCRIPTOR DATA on a descriptor area whose type is ARRAY, ARRAY LOCATOR,
	 * MULTISET, or MULTISET LOCATOR.
	 */
	DYNAMIC_SQL_ERROR_UNDEFINED_DATA_VALUE("0700C"),
	/**
	 * Specified a descriptor item name DATA on a descriptor area whose type is ARRAY, ARRAY LOCATOR,
	 * MULTISET, or MULTISET LOCATOR.
	 */
	DYNAMIC_SQL_ERROR_INVALID_DATA_TARGET("0700D"),
	/**
	 * Invalid LEVEL specified in SET DESCRIPTOR statement.
	 */
	DYNAMIC_SQL_ERROR_INVALID_LEVEL_VALUE("0700E"),
	/**
	 * Specified an invalid datetime interval.
	 */
	DYNAMIC_SQL_ERROR_INVALID_DATETIME_INTERVAL_CODE("0700F"),
	/**
	 * Unable to establish a database connection.
	 */
	CONNECTION_EXCEPTION("08000"),
	/**
	 * Returned if the client is unable to connect to the database.
	 */
	CONNECTION_EXCEPTION_CANNOT_ESTABLISH("08001"),
	/**
	 * Returned if a connection with the specified name is already established.
	 */
	CONNECTION_EXCEPTION_NAME_IN_USE("08002"),
	/**
	 * Attempted to disconnect a non-existent database connection.
	 */
	CONNECTION_EXCEPTION_DOES_NOT_EXIST("08003"),
	/**
	 * Returned if the server rejected the client's connection request.
	 */
	CONNECTION_EXCEPTION_REJECTED("08004"),
	/**
	 * The specified connection could not be selected.
	 */
	CONNECTION_EXCEPTION_FAILURE("08006"),
	/**
	 * Connection lost while committing or rolling back a transaction. The client cannot verify
	 * whether the transaction was committed successfully, rolled back or left active.
	 */
	CONNECTION_EXCEPTION_TRANSACTION_RESOLUTION_UNKNOWN("08007"),
	/**
	 * A triggered SQL statement failed.
	 */
	TRIGGERED_ACTION_EXCEPTION("09000"),
	/**
	 * The "feature not supported" class identifies exception conditions that relate to features
	 * you're trying to use, but that your DBMS hasn't implemented. The Standard does not specify what
	 * will cause this SQLSTATE, possibly because the expectation is that all features will be
	 * supported.
	 */
	FEATURE_NOT_SUPPORTED("0A000"),
	/**
	 * A single transaction cannot be performed on multiple servers. Such a feature is sophisticated
	 * and rare.
	 */
	FEATURE_NOT_SUPPORTED_MULTIPLE_SERVER_TRANSACTIONS("0A001"),
	/**
	 * An error occurred specifying a target for data.
	 */
	INVALID_TARGET_TYPE_SPECIFICATION("0D000"),
	/**
	 * An error occurred while specifying Schema paths.
	 */
	INVALID_SCHEMA_NAME_LIST_SPECIFICATION("0E000"),
	/**
	 * The "locator exception" class identifies exception conditions that relate to locators: BLOB and
	 * CLOB data types, and their values.
	 */
	LOCATOR_EXCEPTION("0F000"),
	/**
	 * The locator value does not currently represent any value.
	 */
	LOCATOR_EXCEPTION_INVALID_SPECIFICATION("0F001"),
	RESIGNAL_WHEN_HANDLER_NOT_ACTIVE("0K000"),
	/**
	 * The specified grantor may not GRANT access.
	 */
	INVALID_GRANTOR("0L000"),
	/**
	 * Returned if the SQL-session context of the current SQL-session does not include a result set
	 * sequence brought into existence by an invocation of SQL-invoked procedure by the active
	 * SQL-invoked routine.
	 */
	INVALID_SQL_INVOKED_PROCEDURE_REFERENCE("0M000"),
	/**
	 * An error has occurred mapping SQL to XML or vice versa.
	 */
	XML_MAPPING_ERROR("0N000"),
	XML_MAPPING_ERROR_UNMAPPABLE_NAME("0N001"),
	/**
	 * A character cannot be mapped to a valid XML character.
	 */
	XML_MAPPING_ERROR_INVALID_CHARACTER("0N002"),
	/**
	 * Invalid role specification. The specified role does not exist or is not granted to the
	 * specified user.
	 */
	INVALID_ROLE_SPECIFICATION("0P000"),
	/**
	 * A
	 * <code>Transform Group name</code> could be invalid if it is used as a qualifier or as the
	 * argument of SET TRANSFORM GROUP, and does not refer to an existing Transform Group or is not a
	 * valid
	 * <code>identifier</code>.
	 */
	INVALID_TRANSFORM_GROUP_NAME_SPECIFICATION("0S000"),
	/**
	 * Invoked a dynamic update or delete statement but the cursor specification conflicts with the
	 * specification of the table it is operating over.
	 */
	TARGET_TABLE_DISAGREES_WITH_CURSOR_SPECIFICATION("0T000"),
	/**
	 * Attempted to assign a value to a non-updatable column.
	 */
	CANNOT_ASSIGN_TO_NON_UPDATABLE_COLUMN("0U000"),
	/**
	 * Returned if any object column is directly or indirectly referenced in the
	 * <code>order by</code> clause of a dynamic cursor definition.
	 */
	CANNOT_ASSIGN_TO_ORDERING_COLUMN("0V000"),
	/**
	 * The statement is not allowed in a trigger.
	 */
	PROHIBITED_STATEMENT_ENCOUNTERED_DURING_TRIGGER("0W000"),
	INVALID_FOREIGN_SERVER_SPECIFICATION("0X000"),
	PASS_THROUGH_SPECIFIED_CONDITION("0Y000"),
	PASS_THROUGH_SPECIFIED_CONDITION_INVALID_CURSOR_OPTION("0Y001"),
	PASS_THROUGH_SPECIFIED_CONDITION_INVALID_CURSOR_ALLOCATION("0Y002"),
	/**
	 * An error occurred while invoking a diagnostics function.
	 */
	DIAGNOSTICS_EXCEPTION("0Z000"),
	/**
	 * Attempted to PUSH an operation onto the diagnostics stack but the number of operations in the
	 * stack exceed the implementation-dependent maximum.
	 */
	DIAGNOSTICS_EXCEPTION_MAXIMUM_DIAGNOSTICS_STACK_EXCEEDED("0Z001"),
	DIAGNOSTICS_EXCEPTION_ACCESSED_WITHOUT_ACTIVE_HANDLER("0Z002"),
	/**
	 * An error has occurred executing an XQuery statement.
	 */
	XQUERY_ERROR("10000"),
	/**
	 * None of the case statements matched the input.
	 */
	CASE_NOT_FOUND("20000"),
	/**
	 * The result of a SELECT INTO, scalar fullselect, or subquery of a basic predicate returned more
	 * than one value.
	 */
	CARDINALITY_VIOLATION("21000"),
	/**
	 * The specified data was inappropriate for the column type.
	 */
	DATA_EXCEPTION("22000"),
	/**
	 * Character data, right truncation occurred; for example, an update or insert value is a string
	 * that is too long for the column, or a datetime value cannot be assigned to a host variable,
	 * because it is too small. No truncation actually occurs since the SQL statement fails. See
	 * SQLSTATE 01004.
	 */
	DATA_EXCEPTION_STRING_TRUNCATION("22001"),
	/**
	 * A null value, or the absence of an indicator parameter was detected; for example, the null
	 * value cannot be assigned to a host variable, because no indicator variable is specified.
	 */
	DATA_EXCEPTION_NULL_VALUE_NO_INDICATOR_PARAMETER("22002"),
	/**
	 * A numeric value is out of range. Often this is the result of an arithmetic overflow. For
	 * example, "UPDATE ... SET SMALLINT_COLUMN = 9999999999".
	 */
	DATA_EXCEPTION_NUMERIC_VALUE_OUT_OF_RANGE("22003"),
	/**
	 * A null value is not allowed.
	 */
	DATA_EXCEPTION_NULL_VALUE_NOT_ALLOWED_BY_FUNCTION("22004"),
	/**
	 * An error occurred on assignment.
	 */
	DATA_EXCEPTION_ERROR_IN_ASSIGNMENT("22005"),
	/**
	 * Specified an interval with an invalid format. For example, a year-month interval should contain
	 * only a year integer, a '-' separator, and a month integer. See also SQLSTATE 22015.
	 */
	DATA_EXCEPTION_INVALID_INTERVAL_FORMAT("22006"),
	/**
	 * An invalid datetime format was detected; that is, an invalid string representation or value was
	 * specified. See also SQLSTATE 22008, 22018.
	 */
	DATA_EXCEPTION_INVALID_DATETIME_FORMAT("22007"),
	/**
	 * Datetime field overflow occurred; for example, an arithmetic operation on a date or timestamp
	 * has a result that is not within the valid range of dates. See also SQLSTATE 22007.
	 */
	DATA_EXCEPTION_DATETIME_FIELD_OVERFLOW("22008"),
	/**
	 * The time zone displacement value is outside the range -12:59 to 14:00.
	 * <p/>
	 * This could happen for "SET LOCAL TIME ZONE INTERVAL '22:00' HOUR TO MINUTE;", or for "TIMESTAMP
	 * '1994-01-01 02:00:00+10:00'". (In the latter case, it is the result of the calculation that is
	 * a problem.)
	 */
	DATA_EXCEPTION_INVALID_TIME_ZONE_DISPLACEMENT_VALUE("22009"),
	/**
	 * Attempted to use an invalid escape character.
	 */
	DATA_EXCEPTION_ESCAPE_CHARACTER_CONFLICT("2200B"),
	/**
	 * A required escape character was missing or in the wrong location.
	 */
	DATA_EXCEPTION_INVALID_USE_OF_ESCAPE_CHARACTER("2200C"),
	/**
	 * Returned if the length of the escape octet is not one.
	 */
	DATA_EXCEPTION_INVALID_ESCAPE_OCTET("2200D"),
	/**
	 * Attempted to assign a value to an array index whose value was null.
	 */
	DATA_EXCEPTION_NULL_VALUE_IN_ARRAY_TARGET("2200E"),
	/**
	 * Character strings must have a length of one.
	 */
	DATA_EXCEPTION_ZERO_LENGTH_CHARACTER_STRING("2200F"),
	/**
	 * Returned if the return value of a type-preserving function is not compatible with most specific
	 * return type of the function.
	 */
	DATA_EXCEPTION_MOST_SPECIFIC_TYPE_MISMATCH("2200G"),
	/**
	 * Returned if a sequence generator cannot generate any more numbers because it has already
	 * generated its maximum value and it is configured with NO CYCLE.
	 */
	DATA_EXCEPTION_SEQUENCE_GENERATOR_LIMIT_EXCEEDED("2200H"),
	DATA_EXCEPTION_NONIDENTICAL_NOTATIONS_WITH_SAME_NAME("2200J"),
	DATA_EXCEPTION_NONIDENTICAL_UNPARSED_NOTATIONS_WITH_SAME_NAME("2200K"),
	/**
	 * An XML value is not a well-formed document with a single root element.
	 */
	DATA_EXCEPTION_NOT_AN_XML_DOCUMENT("2200L"),
	/**
	 * A value failed to parse as a well-formed XML document or validate according to the XML schema.
	 */
	DATA_EXCEPTION_INVALID_XML_DOCUMENT("2200M"),
	DATA_EXCEPTION_INVALID_XML_CONTENT("2200N"),
	/**
	 * The result of an aggregate function is out of the range of an interval type.
	 */
	DATA_EXCEPTION_INTERVAL_VALUE_OUT_OF_RANGE("2200P"),
	/**
	 * The result of an aggregate function is out of the range of a multiset type.
	 */
	DATA_EXCEPTION_MULTISET_VALUE_OVERFLOW("2200Q"),
	DATA_EXCEPTION_XML_VALUE_OVERFLOW("2200R"),
	/**
	 * The XML comment is not valid.
	 */
	DATA_EXCEPTION_INVALID_COMMENT("2200S"),
	/**
	 * The XML processing instruction is not valid.
	 */
	DATA_EXCEPTION_INVALID_PROCESSING_INSTRUCTION("2200T"),
	DATA_EXCEPTION_NOT_XQUERY_NODE("2200U"),
	DATA_EXCEPTION_INVALID_XQUERY_ITEM("2200V"),
	/**
	 * An XML value contained data that could not be serialized.
	 */
	DATA_EXCEPTION_XQUERY_SERIALIZATION_ERROR("2200W"),
	/**
	 * The value of the indicator variable is less than zero but is not equal to -1 (SQL_NULL_DATA).
	 */
	DATA_EXCEPTION_INVALID_INDICATOR_PARAMETER_VALUE("22010"),
	/**
	 * A substring error occurred; for example, an argument of SUBSTR is out of range.
	 */
	DATA_EXCEPTION_SUBSTRING_ERROR("22011"),
	/**
	 * Attempted to divide a number by zero.
	 */
	DATA_EXCEPTION_DIVISION_BY_ZERO("22012"),
	/**
	 * Returned if the window frame bound preceding or following a WINDOW function is negative or
	 * null.
	 * <p/>
	 * @see http://en.wikipedia.org/wiki/Select_%28SQL%29#Window_function
	 */
	DATA_EXCEPTION_INVALID_SIZE_IN_WINDOW_FUNCTION("22013"),
	/**
	 * The value of an interval field exceeded its maximum value. See also SQLSTATE 22006.
	 */
	DATA_EXCEPTION_INTERVAL_FIELD_OVERFLOW("22015"),
	DATA_EXCEPTION_INVALID_DATALINK_DATA("22017"),
	/**
	 * Tried to convert a value to a data type where the conversion is undefined, or when an error
	 * occurred trying to convert.
	 */
	DATA_EXCEPTION_INVALID_CHARACTER_VALUE_FOR_CAST("22018"),
	/**
	 * The LIKE predicate has an invalid escape character.
	 */
	DATA_EXCEPTION_INVALID_ESCAPE_CHARACTER("22019"),
	/**
	 * A null argument was passed into a datalink constructor.
	 */
	DATA_EXCEPTION_NULL_ARGUMENT_DATALINK_CONSTRUCTOR("2201A"),
	/**
	 * Returned if the specified regular expression does not have a valid format.
	 */
	DATA_EXCEPTION_INVALID_REGULAR_EXPRESSION("2201B"),
	/**
	 * Attempted to insert a null row into a table that disallows them.
	 */
	DATA_EXCEPTION_NULL_ROW_NOT_PERMITTED_IN_TABLE("2201C"),
	/**
	 * The datalink length exceeds the maximum length.
	 */
	DATA_EXCEPTION_DATALINK_TOO_LONG("2201D"),
	/**
	 * Passed an invalid argument into a LOG function.
	 */
	DATA_EXCEPTION_INVALID_ARGUMENT_FOR_LOGARITHM("2201E"),
	/**
	 * Passed an invalid argument into a POWER function.
	 */
	DATA_EXCEPTION_INVALID_ARGUMENT_FOR_POWER_FUNCTION("2201F"),
	/**
	 * Passed an invalid argument into a WIDTH_BUCKET function.
	 */
	DATA_EXCEPTION_INVALID_ARGUMENT_FOR_WIDTH_BUCKET_FUNCTION("2201G"),
	DATA_EXCEPTION_XQUERY_SEQUENCE_CANNOT_BE_VALIDATED("2201J"),
	DATA_EXCEPTION_XQUERY_DOCUMENT_NODE_CANNOT_BE_VALIDATED("2201K"),
	DATA_EXCEPTION_SCHEMA_NOT_FOUND("2201L"),
	DATA_EXCEPTION_ELEMENT_NAMESPACE_NOT_FOUND("2201M"),
	DATA_EXCEPTION_GLOBAL_ELEMENT_NOT_DECLARED("2201N"),
	DATA_EXCEPTION_NO_ELEMENT_WITH_SPECIFIED_QNAME("2201P"),
	DATA_EXCEPTION_NO_ELEMENT_WITH_SPECIFIED_NAMESPACE("2201Q"),
	DATA_EXCEPTION_VALIDATION_FAILURE("2201R"),
	/**
	 * The regular expression specified in the xquery expression is invalid.
	 */
	DATA_EXCEPTION_INVALID_XQUERY_REGULAR_EXPRESSION("2201S"),
	/**
	 * The specified xquery option flag is invalid.
	 */
	DATA_EXCEPTION_INVALID_XQUERY_OPTION_FLAG("2201T"),
	/**
	 * Attempted to replace a substring that matches an XQuery regular expression with a replacement
	 * character string, but the matching substring is a zero-length string.
	 */
	DATA_EXCEPTION_ZERO_LENGTH_STRING("2201U"),
	/**
	 * The replacement string specified in the xquery expression is invalid.
	 */
	DATA_EXCEPTION_INVALID_XQUERY_REPLACEMENT_STRING("2201V"),
	/**
	 * A character is not in the coded character set or the conversion is not supported.
	 */
	DATA_EXCEPTION_CHARACTER_NOT_IN_REPERTOIRE("22021"),
	/**
	 * Indicator is too small for size value.
	 */
	DATA_EXCEPTION_INDICATOR_OVERFLOW("22022"),
	/**
	 * A parameter or host variable value is invalid.
	 */
	DATA_EXCEPTION_INVALID_PARAMETER_VALUE("22023"),
	/**
	 * A NULL-terminated input host variable or parameter did not contain a NULL.
	 */
	DATA_EXCEPTION_UNTERMINATED_C_STRING("22024"),
	/**
	 * The LIKE predicate string pattern contains an invalid occurrence of an escape character.
	 */
	DATA_EXCEPTION_INVALID_ESCAPE_SEQUENCE("22025"),
	/**
	 * Attempted to update a bit string but the specified value does not match the length of the bit
	 * string.
	 */
	DATA_EXCEPTION_STRING_DATA_LENGTH_MISMATCH("22026"),
	/**
	 * Attempted to invoke the TRIM function with a first argument whose length was greater than one
	 * character.
	 */
	DATA_EXCEPTION_TRIM_ERROR("22027"),
	/**
	 * Returned when an operation inserts a non-character code point into a unicode string.
	 */
	DATA_EXCEPTION_NONCHARACTER_IN_UCS_STRING("22029"),
	DATA_EXCEPTION_NULL_VALUE_IN_FIELD_REFERENCE("2202A"),
	/**
	 * Attempted to invoke a mutator function on NULL.
	 */
	DATA_EXCEPTION_NULL_INSTANCE_USED_IN_MUTATOR_FUNCTION("2202D"),
	/**
	 * Attempted to reference an array index which is out of range.
	 */
	DATA_EXCEPTION_ARRAY_ELEMENT_ERROR("2202E"),
	/**
	 * The maximum number of elements in the target array is less than the number of elements in the
	 * source array and the extra source elements are not all NULL.
	 */
	DATA_EXCEPTION_ARRAY_TRUNCATION("2202F"),
	/**
	 * Invalid repeat argument in SAMPLE clause.
	 */
	DATA_EXCEPTION_INVALID_REPEAT_ARGUMENT_IN_SAMPLE_CLAUSE("2202G"),
	/**
	 * The sample size was less than 0 or more than 100.
	 */
	DATA_EXCEPTION_INVALID_SAMPLE_SIZE("2202H"),
	/**
	 * The operation violated a table constraint.
	 */
	INTEGRITY_CONSTRAINT_VIOLATION("23000"),
	/**
	 * The update or delete of a parent key is prevented by a RESTRICT update or delete rule. See also
	 * SQLSTATE 40002.
	 */
	INTEGRITY_CONSTRAINT_VIOLATION_RESTRICT("23001"),
	/**
	 * The cursor is closed or has no current row.
	 */
	INVALID_CURSOR_STATE("24000"),
	/**
	 * The operation violated the transaction constraints.
	 */
	INVALID_TRANSACTION_STATE("25000"),
	/**
	 * START TRANSACTION or DISCONNECT or SET SESSION AUTHORIZATION or SET ROLE statements cannot be
	 * issued if a transaction has already been started.
	 */
	INVALID_TRANSACTION_STATE_ACTIVE_SQL_TRANSACTION("25001"),
	/**
	 * SET TRANSACTION LOCAL ..., which applies only in multiple-server contexts, is illegal if a
	 * local transaction is already happening.
	 */
	INVALID_TRANSACTION_STATE_ALREADY_ACTIVE_FOR_BRANCH("25002"),
	/**
	 * Returned if the transaction access mode of the SQL-transaction is read-only and transaction
	 * access mode specifies READWRITE.
	 */
	INVALID_TRANSACTION_STATE_BRANCH_ACCESS_MODE("25003"),
	/**
	 * Returned if the isolation level of the SQL-transaction is SERIALIZABLE and level of isolation
	 * specifies anything except SERIALIZABLE, or if the isolation level of the SQL-transaction is
	 * REPEATABLE READ and level of isolation specifies anything except REPEATABLE READ or
	 * SERIALIZABLE, or if the isolation level of the SQL-transaction is READ COMMITTED and level of
	 * isolation specifies READ UNCOMMITTED.
	 */
	INVALID_TRANSACTION_STATE_BRANCH_ISOLATION_LEVEL("25004"),
	/**
	 * Returned if SET LOCAL TRANSACTION is executed and there is no active transaction.
	 */
	INVALID_TRANSACTION_STATE_NO_ACTIVE_TRANSACTION_FOR_BRANCH("25005"),
	/**
	 * An update operation is not valid because the transaction is read-only.
	 */
	INVALID_TRANSACTION_STATE_READ_ONLY_TRANSACTION("25006"),
	/**
	 * Some DBMSs do not allow SQL-Schema statements (such as CREATE) to be mixed with SQL-data
	 * statements (such as INSERT) in the same transaction.
	 */
	INVALID_TRANSACTION_STATE_SCHEMA_AND_DATA_MIXING("25007"),
	/**
	 * The SET TRANSACTION statement cannot be used to change isolation level if there is a held
	 * Cursor made with a different isolation level left over from the last transaction.
	 */
	INVALID_TRANSACTION_STATE_HELD_CURSOR_ISOLATION_LEVEL("25008"),
	/**
	 * Probable cause: you failed to PREPARE an SQL statement and now you are trying to EXECUTE it.
	 */
	INVALID_SQL_STATEMENT_NAME("26000"),
	/**
	 * An attempt was made to modify the target table of the MERGE statement by a constraint or
	 * trigger. See also SQLSTATE 09000, 40004.
	 */
	TRIGGERED_DATA_CHANGE_VIOLATION("27000"),
	/**
	 * Authorization name is invalid.
	 */
	INVALID_AUTHORIZATION_SPECIFICATION("28000"),
	/**
	 * Attempted to "REVOKE GRANT OPTION FOR" with dependent privileges and without a "CASCADE".
	 */
	DEPENDENT_PRIVILEGE_DESCRIPTORS_STILL_EXIST("2B000"),
	/**
	 * Presumably an invalid Character set name would be one that begins with a digit, contains a
	 * non-Latin letter, etc.
	 */
	INVALID_CHARACTER_SET_NAME("2C000"),
	/**
	 * An error occurred trying to commit or rollback a transaction.
	 */
	INVALID_TRANSACTION_TERMINATION("2D000"),
	/**
	 * For a CONNECT statement, the argument must be a valid
	 * <code>identifier</code>.
	 */
	INVALID_CONNECTION_NAME("2E000"),
	/**
	 * SQL routine is a procedure or function which is written in SQL. SQLSTATE class 2F identifies
	 * exception conditions that relate to SQL routines. (Exceptions for non-SQL routines are class
	 * 38.)
	 */
	SQL_ROUTINE_EXCEPTION("2F000"),
	/**
	 * The SQL function attempted to modify data, but the function was not defined as MODIFIES SQL
	 * DATA.
	 */
	SQL_ROUTINE_EXCEPTION_FUNCTION_NOT_ALLOWED_TO_MODIFY("2F002"),
	/**
	 * The statement is not allowed in a function or procedure.
	 */
	SQL_ROUTINE_EXCEPTION_PROHIBITED_SQL_STATEMENT("2F003"),
	/**
	 * The SQL function attempted to read data, but the function was not defined as READS SQL DATA.
	 */
	SQL_ROUTINE_EXCEPTION_FUNCTION_NOT_ALLOWED_TO_READ("2F004"),
	/**
	 * The function did not execute a RETURN statement.
	 */
	SQL_ROUTINE_EXCEPTION_FUNCTION_MISSING_RETURN("2F005"),
	/**
	 * A
	 * <code>Collation name</code> could be invalid if it is used as a qualifier or as the argument of
	 * SET COLLATION, and does not refer to an existing Collation or is not a valid
	 * <code>identifier</code>.
	 */
	INVALID_COLLATION_NAME("2H000"),
	/**
	 * Attempted to refer to a statement that should have been prepared, but was not.
	 */
	INVALID_SQL_STATEMENT_IDENTIFIER("30000"),
	/**
	 * Returned if, in embedded SQL, you use "EXECUTE ... USING DESCRIPTOR 'X';", a descriptor named X
	 * must exist.
	 */
	INVALID_SQL_DESCRIPTOR_NAME("33000"),
	/**
	 * Cursor name is invalid.
	 */
	INVALID_CURSOR_NAME("34000"),
	/**
	 * With embedded SQL, you get this by saying "GET DIAGNOSTICS EXCEPTION 0". With the CLI, you get
	 * this by calling SQLGetDiagRec or SQLGetDiagField with a RecordNumber parameter less than 1. If
	 * RecordNumber is greater than the number of status records, you don't get this error. Instead,
	 * you get an NO_DATA return code.
	 */
	INVALID_CONDITION_NUMBER("35000"),
	/**
	 * The "cursor sensitivity exception" class identifies exception conditions that relate to Cursors
	 * and their sensitivity attribute.
	 * <p/>
	 * If a holdable cursor is open during an SQL-transaction
	 * <code>T</code> and it is held open for a subsequent SQL-transaction, then whether any
	 * significant changes made to SQL-data (by
	 * <code>T</code> or any subsequent SQL-transaction in which the cursor is held open) are visible
	 * through that cursor in the subsequent SQL-transaction before that cursor is closed is
	 * determined as follows:
	 * <p/>
	 * - If the cursor is insensitive, then significant changes are not visible. - If the cursor is
	 * sensitive, then the visibility of significant changes is implementation-defined. - If the
	 * cursor is asensitive, then the visibility of significant changes.
	 */
	CURSOR_SENSITIVITY_EXCEPTION("36000"),
	/**
	 * A cursor is insensitive, and the SQL-implementation is unable to guarantee that significant
	 * changes will be invisible through the cursor during the SQL-transaction in which it is opened
	 * and every subsequent SQL-transaction during which it may be held open.
	 */
	CURSOR_SENSITIVITY_EXCEPTION_REQUEST_REJECTED("36001"),
	/**
	 * Returned if a sensitive cursor has not been held into a subsequent SQL-transaction, and the
	 * change resulting from the successful execution of this statement could not be made visible to
	 * the cursor.
	 * <p/>
	 * For example, an attempt was made to execute a positioned DELETE statement, but there is a
	 * sensitive Cursor open, and (for some implementation-dependent reason) the effects of the DELETE
	 * cannot be made visible via that Cursor.
	 */
	CURSOR_SENSITIVITY_EXCEPTION_REQUEST_FAILED("36002"),
	/**
	 * An error occurred executing an external routine.
	 * <p/>
	 * External routines are stored procedures implemented by non-SQL languages (i.e. Java).
	 */
	EXTERNAL_ROUTINE_EXCEPTION("38000"),
	/**
	 * The external routine is not allowed to execute SQL statements.
	 */
	EXTERNAL_ROUTINE_EXCEPTION_CONTAINING_SQL_NOT_PERMITTED("38001"),
	/**
	 * The routine attempted to modify data, but the routine was not defined as MODIFIES SQL DATA.
	 */
	EXTERNAL_ROUTINE_EXCEPTION_NOT_ALLOWED_TO_MODIFY_DATA("38002"),
	/**
	 * The statement is not allowed in a routine.
	 */
	EXTERNAL_ROUTINE_EXCEPTION_PROHIBITED_SQL_STATEMENT("38003"),
	/**
	 * The external routine attempted to read data, but the routine was not defined as READS SQL DATA.
	 */
	EXTERNAL_ROUTINE_EXCEPTION_NOT_ALLOWED_TO_READ_DATA("38004"),
	/**
	 * An error occurred before or after invoking an external routine.
	 * <p/>
	 * External routines are stored procedures implemented by non-SQL languages (i.e. Java).
	 */
	EXTERNAL_ROUTINE_INVOCATION_EXCEPTION("39000"),
	/**
	 * A null value is not allowed for an IN or INOUT argument when using PARAMETER STYLE GENERAL or
	 * an argument that is a Java™ primitive type.
	 */
	EXTERNAL_ROUTINE_INVOCATION_EXCEPTION_NULL_VALUE_NOT_ALLOWED("39004"),
	/**
	 * An error occurred using a savepoint.
	 */
	SAVEPOINT_EXCEPTION("3B000"),
	/**
	 * The savepoint is not valid.
	 */
	SAVEPOINT_EXCEPTION_INVALID_SPECIFICATION("3B001"),
	/**
	 * The maximum number of savepoints has been reached.
	 */
	SAVEPOINT_EXCEPTION_TOO_MANY("3B002"),
	/**
	 * Returned if the statement contains a preparable dynamic cursor name that is ambiguous.
	 */
	AMBIGUOUS_CURSOR_NAME("3C000"),
	/**
	 * A
	 * <code>Catalog name</code> could be invalid if it is used as a qualifier or as the argument of
	 * SET CATALOG, and does not refer to an existing Catalog or is not a valid
	 * <code>identifier</code>.
	 */
	INVALID_CATALOG_NAME("3D000"),
	/**
	 * The schema (collection) name is invalid.
	 */
	INVALID_SCHEMA_NAME("3F000"),
	/**
	 * An error has triggered a rollback the transaction.
	 */
	TRANSACTION_ROLLBACK("40000"),
	/**
	 * The database engine has detected a deadlock. The transaction of this session has been rolled
	 * back to solve the problem. A deadlock occurs when a session tries to lock a table another
	 * session has locked, while the other session wants to lock a table the first session has locked.
	 * As an example, session 1 has locked table A, while session 2 has locked table B. If session 1
	 * now tries to lock table B and session 2 tries to lock table A, a deadlock has occurred.
	 * Deadlocks that involve more than two sessions are also possible. To solve deadlock problems, an
	 * application should lock tables always in the same order, such as always lock table A before
	 * locking table B. For details, see <a href="http://en.wikipedia.org/wiki/Deadlock">Wikipedia
	 * Deadlock</a>.
	 */
	TRANSACTION_ROLLBACK_SERIALIZATION_FAILURE("40001"),
	/**
	 * This occurs for COMMIT, if there were deferred Constraints (deferred Constraints aren't checked
	 * until COMMIT time unless SET CONSTRAINTS IMMEDIATE is executed). So: you asked for COMMIT, and
	 * what you got was ROLLBACK. See also SQLSTATE 23000.
	 */
	TRANSACTION_ROLLBACK_INTEGRITY_CONSTRAINT_VIOLATION("40002"),
	/**
	 * The SQL-Connection was lost during execution of an SQL statement.
	 */
	TRANSACTION_ROLLBACK_STATEMENT_COMPLETION_UNKNOWN("40003"),
	/**
	 * This occurs for COMMIT, if there was a deferred Constraint — presumably a FOREIGN KEY
	 * Constraint unless Triggers are supported by the DBMS — and there was an attempt to violate the
	 * Constraint. See also SQLSTATE 09000, 27000.
	 */
	TRANSACTION_ROLLBACK_TRIGGERED_ACTION_EXCEPTION_AT_COMMIT("40004"),
	/**
	 * Syntax errors include not just grammar or spelling errors, but "bind problems" such as failure
	 * to find an Object. Access violations are due to lack of Privileges. A high security DBMS will
	 * try to hide from the user whether the problem is "you don't have access to X" as opposed to "X
	 * isn't there"; that's why these two different categories are lumped together in one SQLSTATE
	 * (thus users can't discover what the Table names are by trying out all the possibilities).
	 */
	SYNTAX_ERROR_OR_ACCESS_RULE_VIOLATION("42000"),
	/**
	 * The INSERT or UPDATE is not allowed, because a resulting row does not satisfy the view
	 * definition.
	 */
	WITH_CHECK_OPTION_VIOLATION("44000"),
	/**
	 * A user-defined exception was not handled.
	 */
	UNHANDLED_USER_EXCEPTION("45000"),
	/**
	 * An error has occurred with the object language binding (OLB).
	 */
	OLB_SPECIFIC_ERROR("46000"),
	/**
	 * The URL that was specified on the install or replace JAR procedure did not identify a valid JAR
	 * file.
	 */
	OLB_SPECIFIC_ERROR_INVALID_URL("46001"),
	/**
	 * The JAR name that was specified on the install, replace, or remove JAR procedure was invalid.
	 * For example, this message could be issued for one of the following reasons:
	 * <p/>
	 * 1. The JAR name might have the improper format 2. The JAR procedure cannot be replaced or
	 * removed because it does not exist 3. The JAR procedure cannot be installed because it already
	 * exists
	 */
	OLB_SPECIFIC_ERROR_INVALID_JAR_NAME("46002"),
	/**
	 * The specified class in the jar file is currently in use by a defined routine, or the
	 * replacement jar file does not contain the specified class for which a routine is defined.
	 */
	OLB_SPECIFIC_ERROR_INVALID_CLASS_DELETION("46003"),
	OLB_SPECIFIC_ERROR_INVALID_REPLACEMENT("46005"),
	/**
	 * Attempted to replace a JAR which has already been uninstalled.
	 */
	OLB_SPECIFIC_ERROR_CANNOT_REPLACE_UNINSTALLED_JAR("4600A"),
	/**
	 * Attempted to remove a JAR which has already been uninstalled.
	 */
	OLB_SPECIFIC_ERROR_CANNOT_REMOVE_UNINSTALLED_JAR("4600B"),
	/**
	 * The jar cannot be removed. It is in use.
	 */
	OLB_SPECIFIC_ERROR_INVALID_JAR_REMOVAL("4600C"),
	/**
	 * The value provided for the new Java path is invalid.
	 */
	OLB_SPECIFIC_ERROR_INVALID_PATH("4600D"),
	/**
	 * The alter of the jar failed because the specified path references itself.
	 */
	OLB_SPECIFIC_ERROR_SELF_REFERENCING_PATH("4600E"),
	OLB_SPECIFIC_ERROR_INVALID_JAR_NAME_IN_PATH("46102"),
	/**
	 * A Java exception occurred while loading a Java class. This error could be caused by a missing
	 * class or an I/O error occurring when reading the class.
	 */
	OLB_SPECIFIC_ERROR_UNRESOLVED_CLASS_NAME("46103"),
	/**
	 * The specified OLB feature is not supported.
	 */
	OLB_SPECIFIC_ERROR_UNSUPPORTED_FEATURE("46110"),
	/**
	 * The class declaration was invalid.
	 */
	OLB_SPECIFIC_ERROR_INVALID_CLASS_DECLARATION("46120"),
	/**
	 * The column name was invalid.
	 */
	OLB_SPECIFIC_ERROR_INVALID_COLUMN_NAME("46121"),
	/**
	 * The operation contained an invalid number of columns.
	 */
	OLB_SPECIFIC_ERROR_INVALID_NUMBER_OF_COLUMNS("46122"),
	/**
	 * The profile had an invalid state and may be corrupt.
	 */
	OLB_SPECIFIC_ERROR_INVALID_PROFILE_STATE("46130"),
	/**
	 * An error has occurred while using a foreign data wrapper (FDW).
	 */
	FDW_ERROR("HV000"),
	FDW_ERROR_MEMORY_ALLOCATION("HV001"),
	FDW_ERROR_DYNAMIC_PARAMETER_VALUE_NEEDED("HV002"),
	FDW_ERROR_INVALID_DATA_TYPE("HV004"),
	FDW_ERROR_COLUMN_NAME_NOT_FOUND("HV005"),
	FDW_ERROR_INVALID_TYPE_DESCRIPTORS("HV006"),
	FDW_ERROR_INVALID_COLUMN_NAME("HV007"),
	FDW_ERROR_INVALID_COLUMN_NUMBER("HV008"),
	FDW_ERROR_INVALID_USE_OF_NULL_POINTER("HV009"),
	FDW_ERROR_INVALID_STRING_FORMAT("HV00A"),
	FDW_ERROR_INVALID_HANDLE("HV00B"),
	FDW_ERROR_INVALID_OPTION_INDEX("HV00C"),
	FDW_ERROR_INVALID_OPTION_NAME("HV00D"),
	FDW_ERROR_OPTION_NAME_NOT_FOUND("HV00J"),
	FDW_ERROR_REPLY_HANDLE("HV00K"),
	FDW_ERROR_CANNOT_CREATE_EXECUTION("HV00L"),
	FDW_ERROR_CANNOT_CREATE_REPLY("HV00M"),
	FDW_ERROR_CANNOT_CREATE_CONNECTION("HV00N"),
	FDW_ERROR_NO_SCHEMAS("HV00P"),
	FDW_ERROR_SCHEMA_NOT_FOUND("HV00Q"),
	FDW_ERROR_TABLE_NOT_FOUND("HV00R"),
	FDW_ERROR_FUNCTION_SEQUENCE_ERROR("HV010"),
	FDW_ERROR_TOO_MANY_HANDLES("HV014"),
	FDW_ERROR_INCONSISTENT_DESCRIPTOR_INFORMATION("HV021"),
	FDW_ERROR_INVALID_ATTRIBUTE_VALUE("HV024"),
	FDW_ERROR_INVALID_STRING_OR_BUFFER_LENGTH("HV090"),
	FDW_ERROR_INVALID_DESCRIPTOR_FIELD_IDENTIFIER("HV091"),
	/**
	 * An error has occurred while using a datalink.
	 */
	DATALINK_ERROR("HW000"),
	DATALINK_ERROR_FILE_NOT_LINKED("HW001"),
	DATALINK_ERROR_FILE_ALREADY_LINKED("HW002"),
	DATALINK_ERROR_FILE_DOES_NOT_EXIST("HW003"),
	DATALINK_ERROR_INVALID_WRITE_TOKEN("HW004"),
	DATALINK_ERROR_INVALID_CONSTRUCTION("HW005"),
	DATALINK_ERROR_INVALID_WRITE_PERMISSION_FOR_UPDATE("HW006"),
	DATALINK_ERROR_REFERENCE_NOT_VALID("HW007"),
	/**
	 * An error occurred in the glue code sitting between SQL and the native programming language.
	 */
	CLI_ERROR("HY000"),
	/**
	 * An error occurred while an SQL-client interacted with an SQL-server across a communications
	 * network using an RDA Application Context.
	 */
	REMOTE_DATABASE_ACCESS("HZ000"),
	REMOTE_DATABASE_ACCESS_ATTRIBUTE_NOT_PERMITTED("HZ301"),
	REMOTE_DATABASE_ACCESS_AUTHENTICATION_FAILURE("HZ302"),
	REMOTE_DATABASE_ACCESS_DUPLICATE_REQUEST_IDENT("HZ303"),
	REMOTE_DATABASE_ACCESS_ENCODING_NOT_SUPPORTED("HZ304"),
	REMOTE_DATABASE_ACCESS_FEATURE_NOT_SUPPORTED_MULTIPLE_SERVER_TRANSACTIONS("HZ305"),
	REMOTE_DATABASE_ACCESS_INVALID_ATTRIBUTE_TYPE("HZ306"),
	REMOTE_DATABASE_ACCESS_INVALID_FETCH_COUNT("HZ307"),
	REMOTE_DATABASE_ACCESS_INVALID_MESSAGE_TYPE("HZ308"),
	REMOTE_DATABASE_ACCESS_INVALID_OPERATION_SEQUENCE("HZ309"),
	REMOTE_DATABASE_ACCESS_INVALID_TRANSACTION_OPERATION_CODE("HZ310"),
	REMOTE_DATABASE_ACCESS_MISMATCH_BETWEEN_DESCRIPTOR_AND_ROW("HZ311"),
	REMOTE_DATABASE_ACCESS_NO_CONNECTION_HANDLE_AVAILABLE("HZ312"),
	REMOTE_DATABASE_ACCESS_NUMBER_OF_VALUES_DOES_NOT_MATCH_NUMBER_OF_ITEM_DESCRIPTORS("HZ313"),
	REMOTE_DATABASE_ACCESS_TRANSACTION_CANNOT_COMMIT("HZ314"),
	REMOTE_DATABASE_ACCESS_TRANSACTION_STATE_UNKNOWN("HZ315"),
	REMOTE_DATABASE_ACCESS_TRANSPORT_FAILURE("HZ316"),
	REMOTE_DATABASE_ACCESS_UNEXPECTED_PARAMETER_DESCRIPTOR("HZ317"),
	REMOTE_DATABASE_ACCESS_UNEXPECTED_ROW_DESCRIPTOR("HZ318"),
	REMOTE_DATABASE_ACCESS_UNEXPECTED_ROWS("HZ319"),
	REMOTE_DATABASE_ACCESS_VERSION_NOT_SUPPORTED("HZ320"),
	REMOTE_DATABASE_ACCESS_TCPIP_ERROR("HZ321"),
	REMOTE_DATABASE_ACCESS_TLS_ALERT("HZ322"),
	/**
	 * An implementation-specific SQLState.
	 */
	IMPLEMENTATION_SPECIFIC("IMPLEMENTATION_SPECIFIC");
	private static final Map<String, SQLState> codeToEnum = Maps.newHashMap();
	private final String code;
	private final Logger log = LoggerFactory.getLogger(SQLState.class);

	/**
	 * Creates a new SQLState.
	 * <p/>
	 * @param code the 5-character SQLState code
	 */
	SQLState(String code)
	{
		log.debug("Entering. code: {}", code);
		this.code = code;
		assert (code.length() == 5 || code.equals("IMPLEMENTATION_SPECIFIC")): code;
		log.debug("Returning");
	}

	static
	{
		Logger log = LoggerFactory.getLogger(SQLState.class);
		log.debug("Entering");
		for (SQLState value : values())
		{
			SQLState result = codeToEnum.put(value.getCode(), value);
			assert (result == null): value.getCode();
		}
		log.debug("Returning");
	}

	/**
	 * Returns the code associated with the enum.
	 * <p/>
	 * @return the code
	 */
	public String getCode()
	{
		log.debug("Returning {}", code);
		return code;
	}

	/**
	 * Returns the SQLState associated with the specified code.
	 * <p/>
	 * @param code the code (5 characters)
	 * @return the SQLState
	 */
	public static SQLState fromCode(String code)
	{
		Logger log = LoggerFactory.getLogger(SQLState.class);
		SQLState result = codeToEnum.get(code);
		if (result == null)
			return IMPLEMENTATION_SPECIFIC;
		log.debug("Returning {}", result);
		return result;
	}

	/**
	 * Returns the SQLState category.
	 * <p/>
	 * @return the SQLState category
	 */
	public Category getCategory()
	{
		Category result = Category.fromCode(code.substring(0, 2));
		log.debug("Returning {}", result);
		return result;
	}

	@SuppressWarnings("PublicInnerClass")
	public enum Category
	{
		/**
		 * Completed successfully.
		 */
		SUCCESS("00"),
		/**
		 * Completed with a warning.
		 */
		WARNING("01"),
		/**
		 * The operation did not return any data.
		 */
		NO_MORE_DATA("02"),
		/**
		 * An error occurred while executing dynamic SQL.
		 */
		DYNAMIC_SQL_ERROR("07"),
		/**
		 * Unable to establish a database connection.
		 */
		CONNECTION_EXCEPTION("08"),
		/**
		 * A triggered SQL statement failed.
		 */
		TRIGGERED_ACTION_EXCEPTION("09"),
		/**
		 * The specified feature is not supported.
		 */
		FEATURE_NOT_SUPPORTED("0A"),
		/**
		 * An error occurred specifying a target for data.
		 */
		INVALID_TARGET_TYPE_SPECIFICATION("0D"),
		/**
		 * An error occurred while specifying Schema paths.
		 */
		INVALID_SCHEMA_NAME_LIST_SPECIFICATION("0E"),
		/**
		 * The "locator exception" class identifies exception conditions that relate to locators: BLOB
		 * and CLOB data types, and their values.
		 */
		LOCATOR_EXCEPTION("0F"),
		RESIGNAL_WHEN_HANDLER_NOT_ACTIVE("0K"),
		/**
		 * The specified grantor may not GRANT access.
		 */
		INVALID_GRANTOR("0L"),
		/**
		 * Returned if the SQL-session context of the current SQL-session does not include a result set
		 * sequence brought into existence by an invocation of SQL-invoked procedure by the active
		 * SQL-invoked routine.
		 */
		INVALID_SQL_INVOKED_PROCEDURE_REFERENCE("0M"),
		/**
		 * An error has occurred mapping XML to SQL.
		 */
		XML_MAPPING_ERROR("0N"),
		/**
		 * Invalid role specification. The specified role does not exist or is not granted to the
		 * specified user.
		 */
		INVALID_ROLE_SPECIFICATION("0P"),
		/**
		 * A
		 * <code>Transform Group name</code> could be invalid if it is used as a qualifier or as the
		 * argument of SET TRANSFORM GROUP, and does not refer to an existing Transform Group or is not
		 * a valid
		 * <code>identifier</code>.
		 */
		INVALID_TRANSFORM_GROUP_NAME_SPECIFICATION("0S"),
		/**
		 * Invoked a dynamic update or delete statement but the cursor specification conflicts with the
		 * specification of the table it is operating over.
		 */
		TARGET_TABLE_DISAGREES_WITH_CURSOR_SPECIFICATION("0T"),
		/**
		 * Attempted to assign a value to a non-updatable column.
		 */
		CANNOT_ASSIGN_TO_NON_UPDATABLE_COLUMN("0U"),
		/**
		 * If any object column is directly or indirectly referenced in the
		 * <code>order by</code> clause of a dynamic cursor definition.
		 */
		CANNOT_ASSIGN_TO_ORDERING_COLUMN("0V"),
		/**
		 * The statement is not allowed in a trigger.
		 */
		PROHIBITED_STATEMENT_ENCOUNTERED_DURING_TRIGGER("0W"),
		INVALID_FOREIGN_SERVER_SPECIFICATION("0X"),
		PASS_THROUGH_SPECIFIED_CONDITION("0Y"),
		/**
		 * An error occurred while invoking a diagnostics function.
		 */
		DIAGNOSTICS_EXCEPTION("0Z"),
		/**
		 * An error has occurred executing an XQuery statement.
		 */
		XQUERY_ERROR("10"),
		/**
		 * None of the case statements matched the input.
		 */
		CASE_NOT_FOUND("20"),
		/**
		 * The result of a SELECT INTO, scalar fullselect, or subquery of a basic predicate returned
		 * more than one value.
		 */
		CARDINALITY_VIOLATION("21"),
		/**
		 * The specified data was inappropriate for the column type.
		 */
		DATA_EXCEPTION("22"),
		/**
		 * The operation violated a table constraint.
		 */
		INTEGRITY_CONSTRAINT_VIOLATION("23"),
		/**
		 * The cursor is closed or has no current row.
		 */
		INVALID_CURSOR_STATE("24"),
		/**
		 * The operation violated transaction constraints.
		 */
		INVALID_TRANSACTION_STATE("25"),
		/**
		 * Probable cause: you failed to PREPARE an SQL statement and now you are trying to EXECUTE it.
		 */
		INVALID_SQL_STATEMENT_NAME("26"),
		/**
		 * An attempt was made to modify the target table of the MERGE statement by a constraint or
		 * trigger. See also SQLSTATE 09000, 40004.
		 */
		TRIGGERED_DATA_CHANGE_VIOLATION("27"),
		/**
		 * Authorization name is invalid.
		 */
		INVALID_AUTHORIZATION_SPECIFICATION("28"),
		/**
		 * Attempted to "REVOKE GRANT OPTION FOR" with dependent privileges and without a "CASCADE".
		 */
		DEPENDENT_PRIVILEGE_DESCRIPTORS_STILL_EXIST("2B"),
		/**
		 * Presumably an invalid Character set name would be one that begins with a digit, contains a
		 * non-Latin letter, etc.
		 */
		INVALID_CHARACTER_SET_NAME("2C"),
		/**
		 * An error occurred trying to commit or rollback a transaction.
		 */
		INVALID_TRANSACTION_TERMINATION("2D"),
		/**
		 * For a CONNECT statement, the argument must be a valid
		 * <code>identifier</code>.
		 */
		INVALID_CONNECTION_NAME("2E"),
		/**
		 * SQL routine is a procedure or function which is written in SQL. SQLSTATE class 2F identifies
		 * exception conditions that relate to SQL routines. (Exceptions for non-SQL routines are class
		 * 38.)
		 */
		SQL_ROUTINE_EXCEPTION("2F"),
		/**
		 * A
		 * <code>Collation name</code> could be invalid if it is used as a qualifier or as the argument
		 * of SET COLLATION, and does not refer to an existing Collation or is not a valid
		 * <code>identifier</code>.
		 */
		INVALID_COLLATION_NAME("2H"),
		/**
		 * Attempted to refer to a statement that should have been prepared, but was not.
		 */
		INVALID_SQL_STATEMENT_IDENTIFIER("30"),
		/**
		 * Returned if, in embedded SQL, you use "EXECUTE ... USING DESCRIPTOR 'X';", a descriptor named
		 * X must exist.
		 */
		INVALID_SQL_DESCRIPTOR_NAME("33"),
		/**
		 * Cursor name is invalid.
		 */
		INVALID_CURSOR_NAME("34"),
		/**
		 * With embedded SQL, you get this by saying "GET DIAGNOSTICS EXCEPTION 0". With the CLI, you
		 * get this by calling SQLGetDiagRec or SQLGetDiagField with a RecordNumber parameter less than
		 * 1. If RecordNumber is greater than the number of status records, you don't get this error.
		 * Instead, you get an NO_DATA return code.
		 */
		INVALID_CONDITION_NUMBER("35"),
		/**
		 * The "cursor sensitivity exception" class identifies exception conditions that relate to
		 * Cursors and their sensitivity attribute.
		 */
		CURSOR_SENSITIVITY_EXCEPTION("36"),
		/**
		 * An error occurred executing an external routine.
		 * <p/>
		 * External routines are stored procedures implemented by non-SQL languages (i.e. Java).
		 */
		EXTERNAL_ROUTINE_EXCEPTION("38"),
		/**
		 * An error occurred before or after invoking an external routine.
		 * <p/>
		 * External routines are stored procedures implemented by non-SQL languages (i.e. Java).
		 */
		EXTERNAL_ROUTINE_INVOCATION_EXCEPTION("39"),
		/**
		 * An error occurred using a savepoint.
		 */
		SAVEPOINT_EXCEPTION("3B"),
		/**
		 * If the statement contains a preparable dynamic cursor name that is ambiguous.
		 */
		AMBIGUOUS_CURSOR_NAME("3C"),
		/**
		 * A
		 * <code>Catalog name</code> could be invalid if it is used as a qualifier or as the argument of
		 * SET CATALOG, and does not refer to an existing Catalog or is not a valid
		 * <code>identifier</code>.
		 */
		INVALID_CATALOG_NAME("3D"),
		/**
		 * The schema (collection) name is invalid.
		 */
		INVALID_SCHEMA_NAME("3F"),
		/**
		 * An error has triggered a rollback the transaction.
		 */
		TRANSACTION_ROLLBACK("40"),
		/**
		 * Syntax errors include not just grammar or spelling errors, but "bind problems" such as
		 * failure to find an Object. Access violations are due to lack of Privileges. A high security
		 * DBMS will try to hide from the user whether the problem is "you don't have access to X" as
		 * opposed to "X isn't there"; that's why these two different categories are lumped together in
		 * one SQLSTATE (thus users can't discover what the Table names are by trying out all the
		 * possibilities).
		 */
		SYNTAX_ERROR_OR_ACCESS_RULE_VIOLATION("42"),
		/**
		 * The INSERT or UPDATE is not allowed, because a resulting row does not satisfy the view
		 * definition.
		 */
		WITH_CHECK_OPTION_VIOLATION("44"),
		/**
		 * A user-defined exception was not handled.
		 */
		UNHANDLED_USER_EXCEPTION("45"),
		/**
		 * An error has occurred with the object language binding.
		 */
		OLB_SPECIFIC_ERROR("46"),
		/**
		 * An error has occurred while using a foreign data wrapper (FDW).
		 */
		FDW_ERROR("HV"),
		/**
		 * An error has occurred while using a datalink.
		 */
		DATALINK_ERROR("HW"),
		/**
		 * An error occurred in the glue code sitting between SQL and the native programming language.
		 */
		CLI_ERROR("HY"),
		/**
		 * An error occurred while an SQL-client interacted with an SQL-server across a communications
		 * network using an RDA Application Context.
		 */
		REMOTE_DATABASE_ACCESS("HZ"),
		/**
		 * An implementation-specific SQLState.
		 */
		IMPLEMENTATION_SPECIFIC("IMPLEMENTATION_SPECIFIC");
		private final String code;
		private static final Map<String, Category> codeToEnum = Maps.newHashMap();
		private final Logger log = LoggerFactory.getLogger(Category.class);

		/**
		 * Creates a new Category.
		 * <p/>
		 * @param code the 2-character Category code
		 */
		Category(String code)
		{
			log.debug("Entering. code: {}", code);
			this.code = code;
			assert (code.length() == 2 || code.equals("IMPLEMENTATION_SPECIFIC")): code;
			log.debug("Returning");
		}

		static
		{
			Logger log = LoggerFactory.getLogger(Category.class);
			log.debug("Entering");
			for (Category value : values())
				codeToEnum.put(value.getCode(), value);
			log.debug("Returning");
		}

		/**
		 * Returns the code associated with the enum.
		 * <p/>
		 * @return the code
		 */
		public String getCode()
		{
			log.debug("Returning {}", code);
			return code;
		}

		/**
		 * Returns the Category associated with the specified code.
		 * <p/>
		 * @param code the category code (2 characters)
		 * @return the Category
		 */
		public static Category fromCode(String code)
		{
			Logger log = LoggerFactory.getLogger(Category.class);
			log.debug("Entering. code: {}", code);
			Category result = codeToEnum.get(code);
			if (result == null)
				return IMPLEMENTATION_SPECIFIC;
			log.debug("Returning {}", result);
			return result;
		}
	};
}
