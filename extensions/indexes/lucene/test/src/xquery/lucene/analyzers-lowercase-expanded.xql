xquery version "3.0";

(: 

Test wildcard queries in combination with case sensitive indices, and
whether the addition of a new option
<lowercase-expanded>yes|no</lowercase-expanded> (default: yes), works
as expected.

See
https://wiki.apache.org/lucene-java/LuceneFAQ#Are_Wildcard.2C_Prefix.2C_and_Fuzzy_queries_case_sensitive.3F

 :)

module namespace analyze="http://exist-db.org/xquery/lucene/test/analyzers";

declare namespace test="http://exist-db.org/xquery/xqsuite";

(: the standard analyzer, should be case insensitive :)
declare variable $analyze:XCONF1 :=
    <collection xmlns="http://exist-db.org/collection-config/1.0">
        <index xmlns:xs="http://www.w3.org/2001/XMLSchema">
        <lucene>
        <analyzer class="org.apache.lucene.analysis.core.StandardAnalyzer"/>
	<!-- parser makes no difference? -->
        <!-- <parser class="org.apache.lucene.analysis.core.StandardAnalyzer"/> -->
        <text qname="p"/>
        </lucene>
        </index>
        <triggers>
            <trigger class="org.exist.extensions.exquery.restxq.impl.RestXqTrigger"/>
        </triggers>
    </collection>;


(: simple whitespace analyzer, should be case sensitive :)
declare variable $analyze:XCONF2 :=
    <collection xmlns="http://exist-db.org/collection-config/1.0">
        <index xmlns:xs="http://www.w3.org/2001/XMLSchema">
        <lucene>
        <analyzer class="org.apache.lucene.analysis.core.WhitespaceAnalyzer"/>
	<!-- parser makes no difference? -->
        <!-- <parser class="org.apache.lucene.analysis.core.WhitespaceAnalyzer"/> -->
                <text qname="p"/>
            </lucene>
        </index>
        <triggers>
            <trigger class="org.exist.extensions.exquery.restxq.impl.RestXqTrigger"/>
        </triggers>
    </collection>;


declare
    %test:setUp
function analyze:setup() {
    let $testCol := xmldb:create-collection("/db", "lucenetest")
    let $testCol1 := xmldb:create-collection("/db/lucenetest", "test1")
    let $testCol2 := xmldb:create-collection("/db/lucenetest", "test2")
    let $confCol := xmldb:create-collection("/db/system/config/db", "lucenetest")
    let $confCol1 := xmldb:create-collection("/db/system/config/db/lucenetest", "test1")
    let $confCol2 := xmldb:create-collection("/db/system/config/db/lucenetest", "test2")
    let $testdoc :=
	<test>
	<p id="upper">AAAAABBBBBCCCCC</p>
	<p id="lower">aaaaabbbbbccccc</p>
	<p id="mixed">aaaAAbbbBBcccCC</p>
	</test>
    return (
        xmldb:store($confCol1, "collection.xconf", $analyze:XCONF1),
        xmldb:store($testCol1, "test.xml", $testdoc),
        xmldb:store($confCol2, "collection.xconf", $analyze:XCONF2),
        xmldb:store($testCol2, "test.xml", $testdoc)
    )
};

(: declare :)
(:    %test:tearDown :)
(: function analyze:tearDown() { :)
(:    xmldb:remove("/db/lucenetest"), :)
(:    xmldb:remove("/db/system/config/db/lucenetest") :)
(: }; :)



declare 
    %test:args("AAAA*")
    %test:assertEquals("<p id='upper'>AAAAABBBBBCCCCC</p>", "<p id='lower'>aaaaabbbbbccccc</p>", "<p id='mixed'>aaaAAbbbBBcccCC</p>")
    %test:args("aaaa*")
%test:assertEquals("<p id='upper'>AAAAABBBBBCCCCC</p>", "<p id='lower'>aaaaabbbbbccccc</p>", "<p id='mixed'>aaaAAbbbBBcccCC</p>")
    %test:args("AAaaA*")
    %test:assertEquals("<p id='upper'>AAAAABBBBBCCCCC</p>", "<p id='lower'>aaaaabbbbbccccc</p>", "<p id='mixed'>aaaAAbbbBBcccCC</p>")
function analyze:case-insensitive-wildcard-default($querystring as xs:string) {
	collection("/db/lucenetest/test1")//p[ft:query(., $querystring)]
};

declare 
    %test:args("AAAAABBBBBCCCCC")
    %test:assertEquals("<p id='upper'>AAAAABBBBBCCCCC</p>", "<p id='lower'>aaaaabbbbbccccc</p>", "<p id='mixed'>aaaAAbbbBBcccCC</p>")
    %test:args("AAAAABBBBBCCCCC")
%test:assertEquals("<p id='upper'>AAAAABBBBBCCCCC</p>", "<p id='lower'>aaaaabbbbbccccc</p>", "<p id='mixed'>aaaAAbbbBBcccCC</p>")
    %test:args("AAAAABBBBBCCCCC")
    %test:assertEquals("<p id='upper'>AAAAABBBBBCCCCC</p>", "<p id='lower'>aaaaabbbbbccccc</p>", "<p id='mixed'>aaaAAbbbBBcccCC</p>")
function analyze:case-insensitive-term-default($querystring as xs:string) {
	collection("/db/lucenetest/test1")//p[ft:query(., $querystring)]
};


declare 
    %test:args("AAAA*")
%test:assertEquals("<p id='upper'>AAAAABBBBBCCCCC</p>", "<p id='lower'>aaaaabbbbbccccc</p>", "<p id='mixed'>aaaAAbbbBBcccCC</p>")
    %test:args("aaaa*")
%test:assertEquals("<p id='upper'>AAAAABBBBBCCCCC</p>", "<p id='lower'>aaaaabbbbbccccc</p>", "<p id='mixed'>aaaAAbbbBBcccCC</p>")
    %test:args("AAaaA*")
    %test:assertEquals("<p id='upper'>AAAAABBBBBCCCCC</p>", "<p id='lower'>aaaaabbbbbccccc</p>", "<p id='mixed'>aaaAAbbbBBcccCC</p>")
function analyze:case-insensitive-wildcard-filter-rewrite-no($querystring as xs:string) {
	let $options := <options>
        <filter-rewrite>no</filter-rewrite>
        </options>
	return collection("/db/lucenetest/test1")//p[ft:query(., $querystring, $options)]
};



declare 
    %test:args("AAAA*")
    %test:assertEquals("<p id='upper'>AAAAABBBBBCCCCC</p>", "<p id='lower'>aaaaabbbbbccccc</p>", "<p id='mixed'>aaaAAbbbBBcccCC</p>")
    %test:args("aaaa*")
    %test:assertEquals("<p id='upper'>AAAAABBBBBCCCCC</p>", "<p id='lower'>aaaaabbbbbccccc</p>", "<p id='mixed'>aaaAAbbbBBcccCC</p>")
    %test:args("AAaaA*")
    %test:assertEquals("<p id='upper'>AAAAABBBBBCCCCC</p>", "<p id='lower'>aaaaabbbbbccccc</p>", "<p id='mixed'>aaaAAbbbBBcccCC</p>")
function analyze:case-insensitive-filter-rewrite-yes($querystring as xs:string) {
	let $options := <options>
        <filter-rewrite>yes</filter-rewrite>
        </options>
	return collection("/db/lucenetest/test1")//p[ft:query(., $querystring, $options)]
};



declare 
    %test:args("AAAAABBBBBCCCCC")
    %test:assertEquals("<p id='upper'>AAAAABBBBBCCCCC</p>")
    %test:args("aaaaabbbbbccccc")
    %test:assertEquals("<p id='lower'>aaaaabbbbbccccc</p>")
    %test:args("aaaAAbbbBBcccCC")
    %test:assertEquals("<p id='mixed'>aaaAAbbbBBcccCC</p>")
function analyze:case-sensitive-term-default($querystring as xs:string) {
	collection("/db/lucenetest/test2")//p[ft:query(., $querystring)]
};


(: 

all wildcard queries with non-lowercase letters should collapse to
match on lowercase string: the query is lowercased, but the index is
mixed case

 :)
declare 
    %test:args("AAAA*")
    %test:assertEquals("<p id='lower'>aaaaabbbbbccccc</p>")
    %test:args("aaaa*")
    %test:assertEquals("<p id='lower'>aaaaabbbbbccccc</p>")
    %test:args("aaaAA*")
    %test:assertEquals("<p id='lower'>aaaaabbbbbccccc</p>")
function analyze:case-sensitive-wildcard-default($querystring as xs:string) {
	collection("/db/lucenetest/test2")//p[ft:query(., $querystring)]
};

(: 

adding new option lowercase-expanded turns off
setLowercaseExpandedTerms, which enables case sensitive matching for
wildcards

 :)
declare 
    %test:args("AAAA*")
    %test:assertEquals("<p id='upper'>AAAAABBBBBCCCCC</p>")
    %test:args("aaaa*")
    %test:assertEquals("<p id='lower'>aaaaabbbbbccccc</p>")
    %test:args("aaaAA*")
    %test:assertEquals("<p id='mixed'>aaaAAbbbBBcccCC</p>")
function analyze:case-sensitive-wildcard-lowercase-expanded-no($querystring as xs:string) {
	let $options := <options>
	<lowercase-expanded>no</lowercase-expanded>
        </options>
	return collection("/db/lucenetest/test2")//p[ft:query(., $querystring, $options)]
};


declare 
    %test:args("*BB?B*")
    %test:assertEquals("<p id='upper'>AAAAABBBBBCCCCC</p>")
    %test:args("*bb?b*")
    %test:assertEquals("<p id='lower'>aaaaabbbbbccccc</p>")
    %test:args("?aaAAbb*")
    %test:assertEquals("<p id='mixed'>aaaAAbbbBBcccCC</p>")
function analyze:case-sensitive-leading-wildcard-lowercase-expanded-no($querystring as xs:string) {
	let $options := <options>
	<leading-wildcard>yes</leading-wildcard>
	<lowercase-expanded>no</lowercase-expanded>
        </options>
	return collection("/db/lucenetest/test2")//p[ft:query(., $querystring, $options)]
};



