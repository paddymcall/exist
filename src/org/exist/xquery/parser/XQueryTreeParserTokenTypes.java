// $ANTLR 2.7.4: "XQueryTree.g" -> "XQueryTreeParser.java"$

	package org.exist.xquery.parser;

	import antlr.debug.misc.*;
	import java.io.StringReader;
	import java.io.BufferedReader;
	import java.io.InputStreamReader;
	import java.util.ArrayList;
	import java.util.List;
	import java.util.Iterator;
	import java.util.Stack;
	import org.exist.storage.BrokerPool;
	import org.exist.storage.DBBroker;
	import org.exist.storage.analysis.Tokenizer;
	import org.exist.EXistException;
	import org.exist.dom.DocumentSet;
	import org.exist.dom.DocumentImpl;
	import org.exist.dom.QName;
	import org.exist.security.PermissionDeniedException;
	import org.exist.security.User;
	import org.exist.xquery.*;
	import org.exist.xquery.value.*;
	import org.exist.xquery.functions.*;
	import org.exist.xquery.update.*;

public interface XQueryTreeParserTokenTypes {
	int EOF = 1;
	int NULL_TREE_LOOKAHEAD = 3;
	int QNAME = 4;
	int PREDICATE = 5;
	int FLWOR = 6;
	int PARENTHESIZED = 7;
	int ABSOLUTE_SLASH = 8;
	int ABSOLUTE_DSLASH = 9;
	int WILDCARD = 10;
	int PREFIX_WILDCARD = 11;
	int FUNCTION = 12;
	int UNARY_MINUS = 13;
	int UNARY_PLUS = 14;
	int XPOINTER = 15;
	int XPOINTER_ID = 16;
	int VARIABLE_REF = 17;
	int VARIABLE_BINDING = 18;
	int ELEMENT = 19;
	int ATTRIBUTE = 20;
	int ATTRIBUTE_CONTENT = 21;
	int TEXT = 22;
	int VERSION_DECL = 23;
	int NAMESPACE_DECL = 24;
	int DEF_NAMESPACE_DECL = 25;
	int DEF_COLLATION_DECL = 26;
	int DEF_FUNCTION_NS_DECL = 27;
	int GLOBAL_VAR = 28;
	int FUNCTION_DECL = 29;
	int PROLOG = 30;
	int OPTION = 31;
	int ATOMIC_TYPE = 32;
	int MODULE = 33;
	int ORDER_BY = 34;
	int POSITIONAL_VAR = 35;
	int BEFORE = 36;
	int AFTER = 37;
	int MODULE_DECL = 38;
	int ATTRIBUTE_TEST = 39;
	int COMP_ELEM_CONSTRUCTOR = 40;
	int COMP_ATTR_CONSTRUCTOR = 41;
	int COMP_TEXT_CONSTRUCTOR = 42;
	int COMP_COMMENT_CONSTRUCTOR = 43;
	int COMP_PI_CONSTRUCTOR = 44;
	int COMP_NS_CONSTRUCTOR = 45;
	int COMP_DOC_CONSTRUCTOR = 46;
	int LITERAL_xpointer = 47;
	int LPAREN = 48;
	int RPAREN = 49;
	int NCNAME = 50;
	int LITERAL_xquery = 51;
	int LITERAL_version = 52;
	int SEMICOLON = 53;
	int LITERAL_module = 54;
	int LITERAL_namespace = 55;
	int EQ = 56;
	int STRING_LITERAL = 57;
	int LITERAL_import = 58;
	int LITERAL_declare = 59;
	int LITERAL_default = 60;
	int LITERAL_xmlspace = 61;
	int LITERAL_ordering = 62;
	int LITERAL_construction = 63;
	// "base-uri" = 64
	int LITERAL_option = 65;
	int LITERAL_function = 66;
	int LITERAL_variable = 67;
	int LITERAL_encoding = 68;
	int LITERAL_collation = 69;
	int LITERAL_element = 70;
	int LITERAL_preserve = 71;
	int LITERAL_strip = 72;
	int LITERAL_ordered = 73;
	int LITERAL_unordered = 74;
	int DOLLAR = 75;
	int LCURLY = 76;
	int RCURLY = 77;
	int LITERAL_external = 78;
	int LITERAL_at = 79;
	int LITERAL_as = 80;
	int COMMA = 81;
	int LITERAL_empty = 82;
	int QUESTION = 83;
	int STAR = 84;
	int PLUS = 85;
	int LITERAL_item = 86;
	int LITERAL_for = 87;
	int LITERAL_let = 88;
	int LITERAL_some = 89;
	int LITERAL_every = 90;
	int LITERAL_if = 91;
	int LITERAL_update = 92;
	int LITERAL_replace = 93;
	int LITERAL_value = 94;
	int LITERAL_insert = 95;
	int LITERAL_delete = 96;
	int LITERAL_rename = 97;
	int LITERAL_with = 98;
	int LITERAL_into = 99;
	int LITERAL_preceding = 100;
	int LITERAL_following = 101;
	int LITERAL_where = 102;
	int LITERAL_return = 103;
	int LITERAL_in = 104;
	int COLON = 105;
	int LITERAL_order = 106;
	int LITERAL_by = 107;
	int LITERAL_ascending = 108;
	int LITERAL_descending = 109;
	int LITERAL_greatest = 110;
	int LITERAL_least = 111;
	int LITERAL_satisfies = 112;
	int LITERAL_typeswitch = 113;
	int LITERAL_case = 114;
	int LITERAL_then = 115;
	int LITERAL_else = 116;
	int LITERAL_or = 117;
	int LITERAL_and = 118;
	int LITERAL_instance = 119;
	int LITERAL_of = 120;
	int LITERAL_castable = 121;
	int LITERAL_cast = 122;
	int LT = 123;
	int GT = 124;
	int LITERAL_eq = 125;
	int LITERAL_ne = 126;
	int LITERAL_lt = 127;
	int LITERAL_le = 128;
	int LITERAL_gt = 129;
	int LITERAL_ge = 130;
	int NEQ = 131;
	int GTEQ = 132;
	int LTEQ = 133;
	int LITERAL_is = 134;
	int LITERAL_isnot = 135;
	int ANDEQ = 136;
	int OREQ = 137;
	int LITERAL_to = 138;
	int MINUS = 139;
	int LITERAL_div = 140;
	int LITERAL_idiv = 141;
	int LITERAL_mod = 142;
	int LITERAL_union = 143;
	int UNION = 144;
	int LITERAL_intersect = 145;
	int LITERAL_except = 146;
	int SLASH = 147;
	int DSLASH = 148;
	int LITERAL_text = 149;
	int LITERAL_node = 150;
	int LITERAL_attribute = 151;
	int LITERAL_comment = 152;
	// "processing-instruction" = 153
	// "document-node" = 154
	int LITERAL_document = 155;
	int SELF = 156;
	int XML_COMMENT = 157;
	int XML_PI = 158;
	int LPPAREN = 159;
	int RPPAREN = 160;
	int AT = 161;
	int PARENT = 162;
	int LITERAL_child = 163;
	int LITERAL_self = 164;
	int LITERAL_descendant = 165;
	// "descendant-or-self" = 166
	// "following-sibling" = 167
	int LITERAL_parent = 168;
	int LITERAL_ancestor = 169;
	// "ancestor-or-self" = 170
	// "preceding-sibling" = 171
	int DOUBLE_LITERAL = 172;
	int DECIMAL_LITERAL = 173;
	int INTEGER_LITERAL = 174;
	int END_TAG_START = 175;
	int QUOT = 176;
	int APOS = 177;
	int QUOT_ATTRIBUTE_CONTENT = 178;
	int APOS_ATTRIBUTE_CONTENT = 179;
	int ELEMENT_CONTENT = 180;
	int XML_COMMENT_END = 181;
	int XML_PI_END = 182;
	int XML_CDATA = 183;
	int LITERAL_collection = 184;
	int LITERAL_validate = 185;
	int XML_PI_START = 186;
	int XML_CDATA_START = 187;
	int XML_CDATA_END = 188;
	int LETTER = 189;
	int DIGITS = 190;
	int HEX_DIGITS = 191;
	int NMSTART = 192;
	int NMCHAR = 193;
	int WS = 194;
	int EXPR_COMMENT = 195;
	int PRAGMA = 196;
	int PRAGMA_CONTENT = 197;
	int PRAGMA_QNAME = 198;
	int PREDEFINED_ENTITY_REF = 199;
	int CHAR_REF = 200;
	int NEXT_TOKEN = 201;
	int CHAR = 202;
	int BASECHAR = 203;
	int IDEOGRAPHIC = 204;
	int COMBINING_CHAR = 205;
	int DIGIT = 206;
	int EXTENDER = 207;
}
