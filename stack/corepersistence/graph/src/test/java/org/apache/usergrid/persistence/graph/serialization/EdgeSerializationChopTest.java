package org.apache.usergrid.persistence.graph.serialization;


import java.util.Iterator;
import java.util.UUID;

import org.jukito.JukitoRunner;
import org.jukito.UseModules;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.safehaus.chop.api.IterationChop;

import org.apache.usergrid.persistence.collection.OrganizationScope;
import org.apache.usergrid.persistence.collection.cassandra.CassandraRule;
import org.apache.usergrid.persistence.collection.guice.MigrationManagerRule;
import org.apache.usergrid.persistence.graph.Edge;
import org.apache.usergrid.persistence.graph.SearchByEdge;
import org.apache.usergrid.persistence.graph.guice.GraphModule;
import org.apache.usergrid.persistence.graph.guice.TestGraphModule;
import org.apache.usergrid.persistence.model.entity.Id;
import org.apache.usergrid.persistence.model.util.UUIDGenerator;

import com.google.inject.Inject;
import com.netflix.astyanax.connectionpool.exceptions.ConnectionException;

import static org.apache.usergrid.persistence.graph.test.util.EdgeTestUtils.createEdge;
import static org.apache.usergrid.persistence.graph.test.util.EdgeTestUtils.createGetByEdge;
import static org.apache.usergrid.persistence.graph.test.util.EdgeTestUtils.createId;
import static org.apache.usergrid.persistence.graph.test.util.EdgeTestUtils.createSearchByEdge;
import static org.apache.usergrid.persistence.graph.test.util.EdgeTestUtils.createSearchByEdgeAndId;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/**
 * Test for use with Judo CHOP to stress test
 *
 */
@IterationChop( iterations = 10, threads = 2 )
@RunWith( JukitoRunner.class )
@UseModules( { TestGraphModule.class } )
public class EdgeSerializationChopTest {

    @ClassRule
    public static CassandraRule rule = new CassandraRule();


    @Inject
    @Rule
    public MigrationManagerRule migrationManagerRule;


    @Inject
    protected EdgeSerialization serialization;

    protected OrganizationScope scope;



    /**
     * Static UUID so ALL nodes write to this as the source
     */
    private static final UUID ORG_ID = UUID.fromString("5697ad38-8dd8-11e3-8436-600308a690e3");



    /**
     * Static UUID so ALL nodes write to this as the source
     */
    private static final UUID SOURCE_NODE_ID = UUID.fromString("5697ad38-8dd8-11e3-8436-600308a690e2");


    @Before
    public void setup() {
        scope = mock( OrganizationScope.class );

        Id orgId = mock( Id.class );

        when( orgId.getType() ).thenReturn( "organization" );
        when( orgId.getUuid() ).thenReturn( ORG_ID );

        when( scope.getOrganization() ).thenReturn( orgId );
    }


    /**
     * Tests loading elements and retrieving them from the same source
     */
    @Test
    public void mixedEdgeTypes() throws ConnectionException {


        final Id sourceId = createId( SOURCE_NODE_ID, "source");
        final Id targetId = createId("target");


        final Edge edge = createEdge( sourceId, "edge", targetId );

        serialization.writeEdge( scope, edge ).execute();


        UUID now = UUIDGenerator.newTimeUUID();

        //get our edges out by name

        Iterator<Edge> results = serialization.getEdgesFromSource( scope, createSearchByEdge( sourceId, "edge", now, null ) );

        boolean found = false;

        while(!found && results.hasNext()){
            if(edge.equals(results.next())){
                found = true;
                break;
            }
        }

        assertTrue("Found entity", found);

    }

}
