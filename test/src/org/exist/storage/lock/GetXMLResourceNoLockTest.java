package org.exist.storage.lock;


import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.exist.dom.persistent.BinaryDocument;
import org.exist.EXistException;
import org.exist.TestUtils;
import org.exist.xmldb.XmldbURI;
import org.exist.test.TestConstants;
import org.exist.collections.Collection;
import org.exist.start.Main;
import org.exist.storage.BrokerPool;
import org.exist.storage.DBBroker;
import org.exist.storage.lock.Lock.LockMode;
import org.exist.storage.txn.TransactionManager;
import org.exist.storage.txn.Txn;
import org.junit.Test;
import static org.junit.Assert.*;

import org.exist.util.DatabaseConfigurationException;

/**
 * 
 * @author Patrick Bosek<patrick.bosek@jorsek.com>
 *
 */
public class GetXMLResourceNoLockTest {

	private Path dataDirBackup;
	private Main database;

    private static String EMPTY_BINARY_FILE = "What's an up dog?";
    private static XmldbURI DOCUMENT_NAME_URI = XmldbURI.create("empty.txt");
	
	@Test
	public void testCollectionMaintainsLockWhenResourceIsSelectedNoLock() throws EXistException, InterruptedException {

		storeTestResource();

        final BrokerPool pool = BrokerPool.getInstance();

		try(final DBBroker broker = pool.get(Optional.of(pool.getSecurityManager().getSystemSubject()))) {
			Collection testCollection = broker.openCollection(TestConstants.TEST_COLLECTION_URI, LockMode.READ_LOCK);
			try {

				XmldbURI docPath = TestConstants.TEST_COLLECTION_URI.append(DOCUMENT_NAME_URI);
				
				BinaryDocument binDoc = (BinaryDocument) broker.getXMLResource(docPath, LockMode.NO_LOCK);
				
				// if document is not present, null is returned
				if(binDoc == null)
					fail("Binary document '" + docPath + " does not exist.");

				
				assertEquals("Collection does not have lock!", true, testCollection.getLock().hasLock());
				
			} finally {
	    		if(testCollection != null) {
					testCollection.getLock().release(LockMode.READ_LOCK);
				}
			}
			
		} catch (Exception ex){
			fail("Error opening document" + ex);
		    
		}

		assertEquals(1, 1);
	}
	

	@Before
	public void setup() throws IOException, DatabaseConfigurationException {
		dataDirBackup = TestUtils.moveDataDirToTempAndCreateClean();
		database = TestUtils.startupDatabase();
	}
	
	@After
	public void tearDown() throws IOException, DatabaseConfigurationException {
		TestUtils.stopDatabase(database);
		TestUtils.moveDataDirBack(dataDirBackup);
	}
	

    private void storeTestResource() throws EXistException {
        
        final BrokerPool pool = BrokerPool.getInstance();

        final TransactionManager transact = pool.getTransactionManager();
        try(final DBBroker broker = pool.get(Optional.of(pool.getSecurityManager().getSystemSubject()));
                final Txn transaction = transact.beginTransaction()) {

            Collection collection = broker
                    .getOrCreateCollection(transaction, TestConstants.TEST_COLLECTION_URI);
            
            broker.saveCollection(transaction, collection);
            
            @SuppressWarnings("unused")
			BinaryDocument doc =
                    collection.addBinaryResource(transaction, broker,
                    DOCUMENT_NAME_URI , EMPTY_BINARY_FILE.getBytes(), "text/text");
            
            transact.commit(transaction);
        } catch (Exception e) {
            fail(e.getMessage());
            
        }
    }
}
