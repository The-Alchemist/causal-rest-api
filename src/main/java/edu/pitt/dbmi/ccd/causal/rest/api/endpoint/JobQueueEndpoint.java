/*
 * Copyright (C) 2016 University of Pittsburgh.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package edu.pitt.dbmi.ccd.causal.rest.api.endpoint;

import edu.pitt.dbmi.ccd.causal.rest.api.Role;
import edu.pitt.dbmi.ccd.causal.rest.api.dto.FgsContinuousNewJob;
import edu.pitt.dbmi.ccd.causal.rest.api.dto.FgsDiscreteNewJob;
import edu.pitt.dbmi.ccd.causal.rest.api.dto.GfciContinuousNewJob;
import edu.pitt.dbmi.ccd.causal.rest.api.dto.JobInfoDTO;
import edu.pitt.dbmi.ccd.causal.rest.api.service.JobQueueEndpointService;
import java.io.IOException;
import java.util.List;
import javax.annotation.security.RolesAllowed;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.GenericEntity;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author Zhou Yuan (zhy19@pitt.edu)
 */
@Path("/{uid}/jobs")
public class JobQueueEndpoint {

    private final JobQueueEndpointService jobQueueEndpointService;

    @Autowired
    public JobQueueEndpoint(JobQueueEndpointService jobQueueEndpointService) {
        this.jobQueueEndpointService = jobQueueEndpointService;
    }

    /**
     * Adding a new job and run GFCI continuous
     *
     * @param username
     * @param newJob
     * @return 201 Created status code with new job ID
     * @throws IOException
     */
    @POST
    @Path("/gfcic")
    @Consumes(APPLICATION_JSON)
    @Produces({APPLICATION_JSON, APPLICATION_XML})
    @RolesAllowed(Role.USER)
    public Response addGfciContinuousNewJob(@PathParam("uid") Long uid, @Valid GfciContinuousNewJob newJob) throws IOException {
        JobInfoDTO jobInfo = jobQueueEndpointService.addGfciContinuousNewJob(uid, newJob);
        GenericEntity<JobInfoDTO> jobRequestEntity = new GenericEntity<JobInfoDTO>(jobInfo) {
        };
        // Return 201 Created status code and the job id in body
        return Response.status(Status.CREATED).entity(jobRequestEntity).build();
    }

    /**
     * Adding a new job and run FGS continuous
     *
     * @param uid
     * @param newJob
     * @return 201 Created status code with new job ID
     * @throws IOException
     */
    @POST
    @Path("/fgsc")
    @Consumes(APPLICATION_JSON)
    @Produces({APPLICATION_JSON, APPLICATION_XML})
    @RolesAllowed(Role.USER)
    public Response addFgsContinuousNewJob(@PathParam("uid") Long uid, @Valid FgsContinuousNewJob newJob) throws IOException {
        JobInfoDTO jobInfo = jobQueueEndpointService.addFgsContinuousNewJob(uid, newJob);
        GenericEntity<JobInfoDTO> jobRequestEntity = new GenericEntity<JobInfoDTO>(jobInfo) {
        };
        // Return 201 Created status code and the job id in body
        return Response.status(Status.CREATED).entity(jobRequestEntity).build();
    }

    /**
     * Adding a new job and run FGS discrete
     *
     * @param uid
     * @param newJob
     * @return 201 Created status code with new job ID
     * @throws IOException
     */
    @POST
    @Path("/fgsd")
    @Consumes(APPLICATION_JSON)
    @Produces({APPLICATION_JSON, APPLICATION_XML})
    @RolesAllowed(Role.USER)
    public Response addFgsDiscreteNewJob(@PathParam("uid") Long uid, @Valid FgsDiscreteNewJob newJob) throws IOException {
        JobInfoDTO jobInfo = jobQueueEndpointService.addFgsDiscreteNewJob(uid, newJob);
        GenericEntity<JobInfoDTO> jobRequestEntity = new GenericEntity<JobInfoDTO>(jobInfo) {
        };
        // Return 201 Created status code and the job id in body
        return Response.status(Status.CREATED).entity(jobRequestEntity).build();
    }

    /**
     * List all Queued or Running jobs associated with the user
     *
     * @param uid
     * @return
     * @throws IOException
     */
    @GET
    @Produces({APPLICATION_JSON, APPLICATION_XML})
    @RolesAllowed(Role.USER)
    public Response listAllJobs(@PathParam("uid") Long uid) throws IOException {
        List<JobInfoDTO> jobInfoDTOs = jobQueueEndpointService.listAllJobs(uid);
        GenericEntity<List<JobInfoDTO>> entity = new GenericEntity<List<JobInfoDTO>>(jobInfoDTOs) {
        };

        return Response.ok(entity).build();
    }

    /**
     * Checking job status for a given job ID
     *
     * Note: job ID is not associated with user account at this moment that's
     * why we don't have an endpoint that lists all the running jobs
     *
     * @param uid
     * @param id
     * @return 200 OK status code with JobInfoDTO object or 404 NOT_FOUND
     * @throws IOException
     */
    @GET
    @Path("/{id}")
    @RolesAllowed(Role.USER)
    public Response jobStatus(@PathParam("uid") Long uid, @PathParam("id") Long id) throws IOException {
        JobInfoDTO jobInfoDTO = jobQueueEndpointService.checkJobStatus(uid, id);
        if (jobInfoDTO == null) {
            return Response.status(Status.NOT_FOUND).build();
        }
        GenericEntity<JobInfoDTO> entity = new GenericEntity<JobInfoDTO>(jobInfoDTO) {
        };
        return Response.ok(entity).build();
    }

    /**
     * Cancel a job (job status can be Queued or Running)
     *
     * @param username
     * @param id
     * @return
     * @throws IOException
     */
    @DELETE
    @Path("/{id}")
    @RolesAllowed(Role.USER)
    public Response cancelJob(@PathParam("uid") Long uid, @PathParam("id") Long id) throws IOException {
        boolean canceled = jobQueueEndpointService.cancelJob(uid, id);

        if (canceled) {
            return Response.ok("Job " + id + " has been canceled").build();
        } else {
            return Response.ok("Unable to cancel job " + id).build();
        }
    }

}
