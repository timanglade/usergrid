/*******************************************************************************
 * Copyright 2012 Apigee Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.apache.usergrid.rest.applications.events;


import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.apache.usergrid.mq.QueuePosition;
import org.apache.usergrid.mq.QueueQuery;
import org.apache.usergrid.mq.QueueResults;
import org.apache.usergrid.persistence.entities.User;
import org.apache.usergrid.rest.applications.ApplicationResource;
import org.apache.usergrid.rest.applications.ServiceResource;

import com.sun.jersey.api.json.JSONWithPadding;


@Component("org.apache.usergrid.rest.applications.events.EventsResource")
@Scope("prototype")
@Produces(MediaType.APPLICATION_JSON)
public class EventsResource extends ServiceResource {

    public static final Logger logger = LoggerFactory.getLogger( EventsResource.class );

    String errorMsg;
    User user;


    public EventsResource() {
    }


    @Override
    @GET
    public JSONWithPadding executeGet( @Context UriInfo ui,
                                       @QueryParam("callback") @DefaultValue("callback") String callback )
            throws Exception {
        QueueQuery query = QueueQuery.fromQueryParams( ui.getQueryParameters() );
        if ( query == null ) {
            query = new QueueQuery();
        }
        query.setPosition( QueuePosition.START );
        QueueResults results = ( ( ApplicationResource ) parent ).getQueues().getFromQueue( "/events", query );
        return new JSONWithPadding( results, callback );
    }
}
