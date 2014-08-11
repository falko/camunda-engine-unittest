/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.unittest;

import java.util.List;

import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.runtime.EventSubscription;
import org.camunda.bpm.engine.runtime.Incident;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;

import static org.junit.Assert.*;

import org.junit.Rule;
import org.junit.Test;

/**
 * @author Daniel Meyer
 *
 */
public class SimpleTestCase {

  @Rule
  public ProcessEngineRule rule = new ProcessEngineRule();

  @Test
  @Deployment(resources = {"testProcess.bpmn"})
  public void shouldExecuteProcess() {

    RuntimeService runtimeService = rule.getRuntimeService();
    ManagementService managementService = rule.getManagementService();

    ProcessInstance pi = runtimeService.startProcessInstanceByKey("testProcess");
    assertFalse("Process instance should not be ended", pi.isEnded());
    assertEquals(1, runtimeService.createProcessInstanceQuery().count());
    
    EventSubscription eventSubscription = runtimeService.createEventSubscriptionQuery().singleResult();
    assertNotNull(eventSubscription);

    executeAvailableJobs();
    
    Incident incident = runtimeService.createIncidentQuery().processInstanceId(pi.getId()).singleResult();
    assertNotNull(incident);
    
    runtimeService.correlateMessage("cancel");

    // now the process instance should be ended
    assertEquals(0, runtimeService.createProcessInstanceQuery().count());

  }

  public void executeAvailableJobs() {
	    ManagementService managementService = rule.getManagementService();
		List<Job> jobs = managementService.createJobQuery().withRetriesLeft().list();

	    if (jobs.isEmpty()) {
	      return;
	    }

	    for (Job job : jobs) {
	      try {
	        managementService.executeJob(job.getId());
	      } catch (Exception e) {};
	    }

	    executeAvailableJobs();
	  }
}
